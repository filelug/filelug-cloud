package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.ComputerDao;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.*;
import org.clopuccino.service.ConnectionDispatchService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <code>DispatchConnectionServlet</code> requests a lug server to connect to.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "dispatch-connection", displayName = "dispatch-connection", description = "Request the lug server to connect", urlPatterns = {"/computer/dispatch"})
public class DispatchConnectionServlet extends HttpServlet {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DispatchConnectionServlet.class.getSimpleName());

    private static final long serialVersionUID = 1490378759516474631L;

    private final UserDao userDao;

    private final ComputerDao computerDao;

    private final UserComputerDao userComputerDao;

    public DispatchConnectionServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            ConnectModel connectModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), ConnectModel.class);

            String userId = connectModel.getAccount();
            String password = connectModel.getPassword();
            String nickname = connectModel.getNickname();
            String verification = connectModel.getVerification();
            String clientLocale = connectModel.getLocale();
            Long computerId = connectModel.getComputerId();

            if (userId == null || password == null || nickname == null || verification == null || computerId == null
                || userId.trim().length() < 1 || password.trim().length() < 1 || nickname.trim().length() < 1 || verification.trim().length() < 1
                || password.equals(DigestUtils.sha256Hex(""))) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                // checking if computer exists must be prior than checking if user exists

                Computer computer = computerDao.findComputerById(computerId);

                if (computer == null) {
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "computer.not.found");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(Constants.HTTP_STATUS_COMPUTER_NOT_FOUND);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    User user = userDao.findUserById(userId);

                    if (user != null) {
                        // if verified
                        Boolean verified = user.getVerified();

                        if (verified == null || !verified) {
                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "account.not.verified", user.getPhoneNumber());

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            resp.getWriter().write(errorMessage);
                            resp.getWriter().flush();
                        } else {
                            // validate with password
                            String foundPasswd = user.getPasswd();

                            if (password.equals(foundPasswd)) {
                                // Check verification code after checking password
                                if (!verification.equals(Utility.generateVerification(userId, password, nickname))) {
                                    LOGGER.warn("User: " + userId + " is testing verification code for login from device");

                                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");

                                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                    resp.getWriter().write(String.valueOf(errorMessage));
                                    resp.getWriter().flush();
                                } else {
                                    String lugServerId = ConnectionDispatchService.dispatchConnectionBy(userId, computerId);

                                    // update to UserComputer

                                    String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

                                    userComputerDao.updateLugServerById(userComputerId, lugServerId);

                                    // response with new lug server id

                                    UserComputerWithoutProfiles userComputer = new UserComputerWithoutProfiles(null, userId, computerId, null, null, null, null, lugServerId, null, null, null);
                                    String responseString = mapper.writeValueAsString(userComputer);

                                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                    resp.setStatus(HttpServletResponse.SC_OK);
                                    resp.getWriter().write(responseString);
                                    resp.getWriter().flush();
                                }
                            } else {
                                // incorrect password
                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "password.not.correct", user.getPhoneNumber());

                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                resp.getWriter().write(errorMessage);
                                resp.getWriter().flush();
                            }
                        }
                    } else {
                        // user id not found
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    }
                }
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error("Error on dispatching connection.", e);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
