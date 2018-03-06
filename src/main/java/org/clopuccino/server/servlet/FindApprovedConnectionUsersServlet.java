package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.ApplyConnectionDao;
import org.clopuccino.dao.ComputerDao;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ApprovedUserModel;
import org.clopuccino.domain.Computer;
import org.clopuccino.domain.ComputerModel;
import org.clopuccino.domain.User;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>CheckUserExistingServlet</code> makes sure the user eixsts and the password is correct.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "find-approved-connection-users", displayName = "find-approved-connection-users", description = "Find approved connection-users by computer", urlPatterns = {"/user/approved"})
public class FindApprovedConnectionUsersServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FindApprovedConnectionUsersServlet.class.getSimpleName());

    private static final long serialVersionUID = 4390473575268604469L;

    private final UserDao userDao;

    private final ApplyConnectionDao applyConnectionDao;

    private final ComputerDao computerDao;

    private final UserComputerDao userComputerDao;


    public FindApprovedConnectionUsersServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        applyConnectionDao = new ApplyConnectionDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            ComputerModel computerModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), ComputerModel.class);

            String userId = computerModel.getAccount();
            String password = computerModel.getPassword();
            String nickname = computerModel.getNickname();
            String verification = computerModel.getVerification();
            String computerGroup = computerModel.getGroupName();
            String computerName = computerModel.getComputerName();
            String recoveryKey = computerModel.getRecoveryKey();
            String clientLocale = computerModel.getLocale();

            if (userId == null || password == null || nickname == null || verification == null || computerGroup == null || computerName == null || recoveryKey == null
                || userId.trim().length() < 1 || password.trim().length() < 1 || nickname.trim().length() < 1 || verification.trim().length() < 1 || computerGroup.trim().length() < 1 || computerName.trim().length() < 1 || recoveryKey.trim().length() < 1
                || password.equals(DigestUtils.sha256Hex(""))) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                // check recovery key to prevent hackers trying to use other's computer

                Computer computer = computerDao.findComputerByRecoveryKey(recoveryKey);

                if (computer == null) {
                    LOGGER.warn("User: " + userId + " is testing computer recovery key: " + recoveryKey + " with computer name: " + computerName);

                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(String.valueOf(errorMessage));
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
                                    LOGGER.warn("User: " + userId + " is testing verification code.");

                                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");

                                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                    resp.getWriter().write(String.valueOf(errorMessage));
                                    resp.getWriter().flush();
                                } else {
                                    // get approved users and trim unnessary information

                                    Long computerId = computer.getComputerId();
                                    List<User> tmpUsers = applyConnectionDao.findApprovedUsersByComputerId(computerId);

                                    List<ApprovedUserModel> approvedUsers = new ArrayList<>();

                                    if (tmpUsers != null && tmpUsers.size() > 0) {
                                        for (User tmpUser : tmpUsers) {
                                            String approvedUserId = tmpUser.getAccount();

                                            String userComputerId = Utility.generateUserComputerIdFrom(approvedUserId, computerId);

                                            boolean allowAlias = userComputerDao.findAllowAliasById(userComputerId);

                                            ApprovedUserModel approvedUser = new ApprovedUserModel(approvedUserId, tmpUser.getCountryId(), tmpUser.getPhoneNumber(), tmpUser.getNickname(), tmpUser.getShowHidden(), allowAlias, null);

                                            approvedUsers.add(approvedUser);
                                        }
                                    }

                                    String responseJsonString = mapper.writeValueAsString(approvedUsers);

                                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                    resp.setStatus(HttpServletResponse.SC_OK);
                                    resp.getWriter().write(responseJsonString);
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
                        // user not found
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on finding approved connection users.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
