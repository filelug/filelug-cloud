package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.ApplyConnectionDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ApplyConnection;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.ConnectModel;
import org.clopuccino.domain.User;
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
 * <code>CheckUserDeletableServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "check-user-deletable", displayName = "check-user-deletable", description = "Check if user deletable", urlPatterns = {"/user/check-deletable"})
public class CheckUserDeletableServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CheckUserDeletableServlet.class.getSimpleName());

    private static final long serialVersionUID = 2226632475211022469L;

    private final ClientSessionService clientSessionService;

    private final UserDao userDao;

    private final ApplyConnectionDao applyConnectionDao;

    public CheckUserDeletableServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);

        userDao = new UserDao(dbAccess);

        applyConnectionDao = new ApplyConnectionDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            ObjectMapper mapper = Utility.createObjectMapper();

            ConnectModel connectModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), ConnectModel.class);

            final String userId = connectModel.getAccount();
            final String clientLocale = connectModel.getLocale();

            if (userId == null || userId.trim().length() < 1) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                String clientSessionUserId = clientSession.getUserId();

                if (clientSessionUserId.equals(userId)) {
                    User user = userDao.findUserById(userId);

                    if (user == null || !user.getVerified()) {
                        /* user not found */
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else if (user.getDeletable() != null && !user.getDeletable()) {
                        LOGGER.warn("User: " + userId + " is not deletable");

                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.deletable");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write(String.valueOf(errorMessage));
                        resp.getWriter().flush();
                    } else {
                        // Found data that excluding self-approved
                        List<ApplyConnection> applyConnections = applyConnectionDao.findByAdminUserId(userId, false);

                        Set<String> othersAllowedAccessComputerNames = new HashSet<>();
                        Set<String> onlySelfAllowedAccessComputerNames = new HashSet<>();

                        if (applyConnections != null && applyConnections.size() > 0) {
                            for (ApplyConnection applyConnection : applyConnections) {
                                if (applyConnection.getApproved()) {
                                    String computerName = applyConnection.getComputerName();

                                    if (computerName != null && computerName.trim().length() > 0) {
                                        if (!applyConnection.getApprovedUser().equals(applyConnection.getApplyUser())) {
                                            othersAllowedAccessComputerNames.add(computerName);
                                        } else {
                                            onlySelfAllowedAccessComputerNames.add(computerName);
                                        }
                                    }

                                }
                            }
                        }

                        if (othersAllowedAccessComputerNames.size() > 0) {
                            // there's at least one non-admin user allowed to access the computers that owned by the user.

                            String[] computerNameStringArray = othersAllowedAccessComputerNames.toArray(new String[othersAllowedAccessComputerNames.size()]);

                            String message = ClopuccinoMessages.localizedMessage(clientLocale, "delete.failure.for.allowed.user.found", Arrays.toString(computerNameStringArray));

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(Constants.HTTP_STATUS_USER_ALLOWED_NON_ADMIN_USERS);
                            resp.getWriter().write(message);
                            resp.getWriter().flush();
                        } else {
                            // when size of othersAllowedAccessComputerNames is zero,
                            // onlySelfAllowedAccessComputerNames contains only self-approved computer names.

                            // respone with computers owned by this user

                            String computerNamesString;
                            if (onlySelfAllowedAccessComputerNames.size() > 0) {
                                StringBuilder builder = new StringBuilder();

                                for (String computerName : onlySelfAllowedAccessComputerNames) {
                                    builder.append(computerName);
                                    builder.append("\n");
                                }

                                computerNamesString = builder.toString();
                            } else {
                                computerNamesString = "";
                            }

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_OK);
                            resp.getWriter().write(computerNamesString);
                            resp.getWriter().flush();
                        }
                    }
                } else {
                    // invalid client session 
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "invalid.session");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on checking if user deletable.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }

    }
}
