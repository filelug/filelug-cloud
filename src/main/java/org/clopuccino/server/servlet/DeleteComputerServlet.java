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
 * <code>DeleteComputerServlet</code> deletes computer information
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "delete-computer", displayName = "delete-computer", description = "Delete computer", urlPatterns = {"/computer/delete"})
public class DeleteComputerServlet extends HttpServlet {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DeleteComputerServlet.class.getSimpleName());

    private static final long serialVersionUID = 2447029960410992646L;

    private final UserDao userDao;

    private final ComputerDao computerDao;


    public DeleteComputerServlet() {
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
            Long computerId = computerModel.getComputerId();
            String recoveryKey = computerModel.getRecoveryKey();

            if (userId == null || password == null || nickname == null || verification == null || computerId == null || recoveryKey == null
                || userId.trim().length() < 1 || password.trim().length() < 1 || nickname.trim().length() < 1 || verification.trim().length() < 1 || computerId < 0 || recoveryKey.trim().length() < 1 || recoveryKey.trim().length() < 1
                || password.equals(DigestUtils.sha256Hex(""))) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
                                // 確認電腦存在
                                Computer computer = computerDao.findComputerById(computerId);

                                if (computer == null) {
                                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                    resp.getWriter().write("Computer Not Found");
                                    resp.getWriter().flush();
                                } else {
                                    if (computer.getRecoveryKey().equals(recoveryKey)) {
                                        // DO NOT delete related client sessions of devices because the computer id of this session
                                        // can be updated when the device connects to another computer.
//                                        clientSessionService.removeClientSessionsByComputer(computerId);

                                        /* delete the old computer name, cascading for related UserComputers */
                                        computerDao.deleteComputerById(computerId);

                                        resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                        resp.setStatus(HttpServletResponse.SC_OK);
                                        resp.getWriter().write("Computer Deleted");
                                        resp.getWriter().flush();
                                    } else {
                                        resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                        resp.getWriter().write("Computer Not Found");
                                        resp.getWriter().flush();
                                    }
                                }
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
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error("Error on deleting computer.", e);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
