package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.*;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ChangeComputerModel;
import org.clopuccino.domain.Computer;
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
 * <code>ChangeComputerServlet</code> changes computer information
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "change-computer", displayName = "change-computer", description = "Change computer", urlPatterns = {"/computer/change"})
public class ChangeComputerServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ChangeComputerServlet.class.getSimpleName());

    private static final long serialVersionUID = 7149440387075464088L;

    private final UserDao userDao;

    private final ComputerDao computerDao;

    private final FileUploadDao fileUploadDao;

    private final FileDownloadDao fileDownloadDao;


    public ChangeComputerServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        fileUploadDao = new FileUploadDao(dbAccess);

        fileDownloadDao = new FileDownloadDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            ChangeComputerModel computerModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), ChangeComputerModel.class);

            final String userId = computerModel.getAccount();
            final String password = computerModel.getPassword();
            final String nickname = computerModel.getNickname();
            final String verification = computerModel.getVerification();
            final String clientLocale = computerModel.getLocale();
            final Long computerId = computerModel.getComputerId();
            final String computerRecoveryKey = computerModel.getOldRecoveryKey();
            final String computerNewGroup = computerModel.getNewGroupName();
            final String computerNewName = computerModel.getNewComputerName();

            if (userId == null || password == null || nickname == null || verification == null || computerId == null || computerRecoveryKey == null || computerNewGroup == null || computerNewName == null
                || userId.trim().length() < 1 || password.trim().length() < 1 || nickname.trim().length() < 1 || verification.trim().length() < 1 || computerId < 0 || computerRecoveryKey.trim().length() < 1 || computerNewGroup.trim().length() < 1 || computerNewName.trim().length() < 1
                || password.equals(DigestUtils.sha256Hex(""))) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
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
                            /* check if computer exists */
                                Computer computer = computerDao.findComputerById(computerId);

                                if (computer == null) {
                                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                    resp.getWriter().write("NOT FOUND");
                                    resp.getWriter().flush();
                                } else {
                                    if (Computer.suppoertedGroupName(computerNewGroup)) {
                                        // Use findComputerByNameForUser(userId, computerGroup, computerName) to check if computer name duplicated for the same user.

                                        Computer newComputer = computerDao.findComputerByNameForUser(userId, computerNewGroup, computerNewName);

                                        if (newComputer != null) {
                                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "computer.duplicated");

                                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                            resp.setStatus(HttpServletResponse.SC_CONFLICT);
                                            resp.getWriter().write(errorMessage);
                                            resp.getWriter().flush();
                                        } else {
                                            // make sure the user is the admin of the computer

                                            if (!userId.equals(computer.getUserId())) {
                                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.computer.admin", computer.getComputerName(), user.getNickname());

                                                resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                                resp.setStatus(Constants.HTTP_STATUS_USER_NOT_ADMIN);
                                                resp.getWriter().write(errorMessage);
                                                resp.getWriter().flush();
                                            } else {
//                                            final Long computerId = computer.getComputerId();

                                                // update computer with group and name

                                                computer.setGroupName(computerNewGroup);
                                                computer.setComputerName(computerNewName);

                                                computerDao.updateComputer(computer);

                                            /* Encrypted value is composed from userId and computerId, and they all remain the same after the computer name changed,
                                             * so it is not necessary to update the value
                                             */
//                                            final String encryptedUserComputerId = Utility.generateEncryptedUserComputerIdFrom(userId, computerId);
//                                            userComputerDao.updateEncryptedUserComputerIdByComputerId(computerId, encryptedUserComputerId);

                                                Utility.getExecutorService().execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        fileUploadDao.updateFileUploadComputerName(computerId, computerNewGroup, computerNewName);
                                                        fileDownloadDao.updateFileDownloadComputerName(computerId, computerNewGroup, computerNewName);
                                                    }
                                                });

                                                String responseString = mapper.writeValueAsString(computer);

                                                resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                                resp.setStatus(HttpServletResponse.SC_OK);
                                                resp.getWriter().write(responseString);
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
                                }
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
            LOGGER.error("Error on update computer information.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
