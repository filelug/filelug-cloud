package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.ComputerDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
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

/**
 * <code>CreateComputerServlet</code> checks if the computer name is available.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "check-computer-available", displayName = "check-computer-available", description = "Check if computer name available", urlPatterns = {"/computer/check-available"})
public class CheckComputerAvailableServlet extends HttpServlet {
    private static final long serialVersionUID = 498984390755364176L;

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CheckComputerAvailableServlet.class.getSimpleName());

    private final UserDao userDao;

    private final ComputerDao computerDao;


    public CheckComputerAvailableServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);
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
            String clientLocale = computerModel.getLocale();
            String computerGroup = computerModel.getGroupName();
            String computerName = computerModel.getComputerName();

            if (userId == null || password == null || nickname == null || verification == null || computerGroup == null || computerName == null
                || userId.trim().length() < 1 || password.trim().length() < 1 || nickname.trim().length() < 1 || verification.trim().length() < 1 || computerGroup.trim().length() < 1 || computerName.trim().length() < 1
                || password.equals(DigestUtils.sha256Hex(""))) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (computerName.trim().length() < Constants.MIN_COMPUTER_NAME_LENGTH || computerName.trim().length() > Constants.MAX_COMPUTER_NAME_LENGTH) {
                String errorMessage = ClopuccinoMessages.getMessage("computer.name.length.limit", String.valueOf(Constants.MIN_COMPUTER_NAME_LENGTH), String.valueOf(Constants.MAX_COMPUTER_NAME_LENGTH));

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(String.valueOf(errorMessage));
                resp.getWriter().flush();
            } else {
                User user = userDao.findUserById(userId);

                if (user != null) {
                /* if verified */
                    Boolean verified = user.getVerified();

                    if (verified == null || !verified) {
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "account.not.verified", user.getPhoneNumber());

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else {
                    /* validate with password */
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
                            } else if (Computer.suppoertedGroupName(computerGroup)) {
                                // Use findComputerByNameForUser(userId, computerGroup, computerName) to check if computer name duplicated for the same user.
                                Computer computer = computerDao.findComputerByNameForUser(userId, computerGroup, computerName);

                                String recoveryKey = computerModel.getRecoveryKey();

                                if (recoveryKey != null && recoveryKey.trim().length() > 0) {
                                    // Desktop wants to restore the computer

                                    if (computer == null || (computer.getRecoveryKey() != null && computer.getRecoveryKey().equals(recoveryKey))) {
                                        resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                        resp.setStatus(HttpServletResponse.SC_OK);
                                        resp.getWriter().write("'" + computerName + "' is available for now.");
                                        resp.getWriter().flush();
                                    } else {
                                        // computer found, but two recovery keys are not the same

                                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "computer.duplicated");

                                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                        resp.setStatus(HttpServletResponse.SC_CONFLICT);
                                        resp.getWriter().write(errorMessage);
                                        resp.getWriter().flush();
                                    }
                                } else {
                                    // Desktop wants a new computer

                                    if (computer == null) {
                                        resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                        resp.setStatus(HttpServletResponse.SC_OK);
                                        resp.getWriter().write("'" + computerName + "' is available for now.");
                                        resp.getWriter().flush();
                                    } else {
                                        // computer found

                                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "computer.duplicated");

                                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                        resp.setStatus(HttpServletResponse.SC_CONFLICT);
                                        resp.getWriter().write(errorMessage);
                                        resp.getWriter().flush();
                                    }
                                }
                            } else {
                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                resp.getWriter().write(errorMessage);
                                resp.getWriter().flush();
                            }
                        } else {
                        /* incorrect password */
                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "password.not.correct", user.getPhoneNumber());

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            resp.getWriter().write(errorMessage);
                            resp.getWriter().flush();
                        }
                    }
                } else {
                /* user id not found */
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on checking if the computer name available.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
