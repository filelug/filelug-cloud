package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.ApplyConnectionDao;
import org.clopuccino.dao.UserComputerPropertiesDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ApplyConnection;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.ConnectModel;
import org.clopuccino.domain.User;
import org.clopuccino.service.ClientSessionService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <code>AbstractDeleteUserServlet</code> handles different versions to delete user.
 *
 * @author masonhsieh
 * @version 1.0
 */
public abstract class AbstractDeleteUserServlet extends HttpServlet {

    private static final long serialVersionUID = 7254413947388863991L;

    private final ClientSessionService clientSessionService;

    private final UserDao userDao;

    private final ApplyConnectionDao applyConnectionDao;

    private final UserComputerPropertiesDao userComputerPropertiesDao;

    public AbstractDeleteUserServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);

        userDao = new UserDao(dbAccess);

        applyConnectionDao = new ApplyConnectionDao(dbAccess);

        userComputerPropertiesDao = new UserComputerPropertiesDao(dbAccess);
    }

    abstract protected Logger getLogger();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp, boolean verificationCodeWithPassword) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            ObjectMapper mapper = Utility.createObjectMapper();

            ConnectModel connectModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), ConnectModel.class);

            final String userId = connectModel.getAccount();
            final String verification = connectModel.getVerification();
            final String clientLocale = connectModel.getLocale();

            if (userId == null || verification == null
                || userId.trim().length() < 1 || verification.trim().length() < 1) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                String clientSessionUserId = clientSession.getUserId();

                if (!clientSessionUserId.equals(userId)) {
                    throw new Exception("Permission denied to delete user.");
                }

                String clientSessionId = clientSession.getSessionId();

                User user = userDao.findUserById(userId);

                if (user == null || !user.getVerified()) {
                    /* user not found */
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else if (!verification.equals(generateVerificationCode(verificationCodeWithPassword, user, clientSessionId))) {
                    getLogger().warn("User: " + userId + " is testing verification code for deleting user service");

                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(String.valueOf(errorMessage));
                    resp.getWriter().flush();
                } else if (user.getDeletable() != null && !user.getDeletable()) {
                    getLogger().warn("User: " + userId + " is not deletable");

                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.deletable");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(String.valueOf(errorMessage));
                    resp.getWriter().flush();
                } else {
                    // Found data that excluding self-approved
                    List<ApplyConnection> applyConnections = applyConnectionDao.findByAdminUserId(userId, true);

                    Set<String> computerNames = new HashSet<>();
                    if (applyConnections != null && applyConnections.size() > 0) {
                        for (ApplyConnection applyConnection : applyConnections) {
                            String computerName = applyConnection.getComputerName();
                            if (applyConnection.getApproved() && computerName != null && computerName.trim().length() > 0) {
                                computerNames.add(computerName);
                            }
                        }
                    }

                    if (computerNames.size() > 0) {
                        // there's at least one non-admin user allowed to access the computers that owned by the user.

                        String[] computerNameStringArray = computerNames.toArray(new String[computerNames.size()]);

                        String message = ClopuccinoMessages.localizedMessage(clientLocale, "delete.failure.for.allowed.user.found", Arrays.toString(computerNameStringArray));

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(Constants.HTTP_STATUS_USER_ALLOWED_NON_ADMIN_USERS);
                        resp.getWriter().write(message);
                        resp.getWriter().flush();
                    } else {
                        userDao.deleteUser(userId);

                        // delete non-fk user tables data

                        Utility.getExecutorService().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    applyConnectionDao.deleteByUserWithAdminOrApproved(userId);

                                    userComputerPropertiesDao.deletePropertiesByUserId(userId);
                                } catch (Exception e) {
                                    getLogger().error("Failed to delete apply_connection or user_computer_properties for user: " + userId + "\n" + e.getMessage(), e);
                                }
                            }
                        });

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write("OK");
                        resp.getWriter().flush();
                    }
                }
            }
        } catch (Exception e) {
            getLogger().error("Error on deleting user.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }

    }

    private String generateVerificationCode(boolean verificationCodeWithPassword, User user, String clientSessionId) {
        if (verificationCodeWithPassword) {
            return Utility.generateDeleteUserVerification(user.getAccount(), user.getPasswd(), user.getNickname(), clientSessionId);
        } else {
            return Utility.generateDeleteUserVerification(user.getAccount(), user.getNickname(), clientSessionId);
        }
    }
}
