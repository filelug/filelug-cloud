package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.ClientSessionDao;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.DeviceToken;
import org.clopuccino.domain.RegistrationVerificationWithDeviceTokenModel;
import org.clopuccino.domain.User;
import org.clopuccino.service.DeviceTokenService;
import org.clopuccino.service.LoginService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * <code>AbstractRegistrationVerificationServlet</code>
 * extracts commons for RegistrationVerificationServlet and RegistrationVerificationServlet2
 *
 * @author masonhsieh
 * @version 1.0
 */
public abstract class AbstractRegistrationVerificationServlet extends HttpServlet {

    private static final long serialVersionUID = -285829239395574216L;

    private UserDao userDao;

    private ClientSessionDao clientSessionDao;

    private UserComputerDao userComputerDao;

    public AbstractRegistrationVerificationServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        clientSessionDao = new ClientSessionDao(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);
    }

    abstract protected Logger getLogger();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp, boolean loginIfSuccessVerified) throws ServletException, IOException {
        ObjectMapper mapper = Utility.createObjectMapper();

        try {
            RegistrationVerificationWithDeviceTokenModel verificationModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), RegistrationVerificationWithDeviceTokenModel.class);

            String userId = verificationModel.getAccount();
            String password = verificationModel.getPassword();
            String nickname = verificationModel.getNickname();
            String verification = verificationModel.getVerification();
            String clientLocale = verificationModel.getLocale();
            String verifyCode = verificationModel.getVerifyCode();
            DeviceToken deviceToken = verificationModel.getDeviceToken();

            if (userId == null || password == null || nickname == null || verification == null || verifyCode == null
                || userId.trim().length() < 1 || password.trim().length() < 1 || nickname.trim().length() < 1 || verification.trim().length() < 1 || verifyCode.trim().length() < 1
                || password.equals(DigestUtils.sha256Hex(""))
                || verification.equals(DigestUtils.sha256Hex(""))) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (!verification.equals(Utility.generateVerification(userId, password, nickname))) {
                getLogger().warn("Be careful that user: " + userId + "(" + nickname + ") is trying to hack the verification code for registration");

                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(String.valueOf(errorMessage));
                resp.getWriter().flush();
            } else {
                User user = userDao.findUserById(userId);

                if (user != null) {
                    // user found. check if verified
                    Boolean verified = user.getVerified();

                    if (verified) {
                        getLogger().info("User: " + userId + " tries to verify again.");

                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "account.already.verified");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(Constants.HTTP_STATUS_USER_ALREADY_REGISTERED);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else {
                        String expectedVerifyCode = user.getVerifyCode();

                        if (expectedVerifyCode != null && expectedVerifyCode.equals(verifyCode)) {
                            if (userDao.updateUserVerifiedById(userId, true)) {
                                getLogger().info("User: " + userId + "(" + nickname + ") verified success.");

                                // Find other users with the same phone number
                                Utility.getExecutorService().execute(() -> {
                                    List<String> userIds = userDao.findUserIdsByPhone(user.getCountryId(), user.getPhoneNumber(), true);

                                    // if only one, it is exactly this user.

                                    if (userIds != null && userIds.size() > 1) {
                                        for (String currentUserId : userIds) {
                                            if (!currentUserId.equals(userId)) {
                                                // Delete all client sessions
                                                clientSessionDao.deleteClientSessionsByUser(currentUserId);

                                                // Set true to phone_number_should_update
                                                userDao.updateShouldUpdatePhoneNumber(currentUserId, true);

                                                getLogger().info("Update phone_number_should_update to true and delete all the client sessions for user: " + currentUserId);

                                                // Set all user_computers of the old user:
                                                // socket_connected == false
                                                // reconnect == true
                                                // so the computer will reconnect and finds that the phone number needs to be updated before connecting
                                                userComputerDao.updateReconnectByUserId(currentUserId, true);
                                            }
                                        }
                                    }
                                });

                                if (loginIfSuccessVerified) {
                                    // login automatically, just like login without computer id

                                    DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

                                    LoginService loginService = new LoginService(dbAccess);

                                    DeviceTokenService deviceTokenService = new DeviceTokenService(dbAccess);

                                    if (deviceToken != null) {
                                        // sometimes account is null, set the current user id to it.

                                        if (deviceToken.getAccount() == null) {
                                            deviceToken.setAccount(user.getAccount());
                                        }

                                        try {
                                            deviceTokenService.createOrUploadDeviceToken(deviceToken, clientLocale);
                                        } catch (Exception e) {
                                            getLogger().error("Error on processing device token: " + deviceToken, e);
                                        }
                                    } else {
                                        getLogger().warn("Device not sending device token for user: '" + user.getNickname() + "'(id=" + userId + ")");
                                    }

                                    // response with user information

                                    loginService.doLoginWithoutComputer(req, resp, user, deviceToken);
                                } else {
                                    // response without user information

                                    String successMessage = ClopuccinoMessages.localizedMessage(clientLocale, "account.success.verified");

                                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                    resp.setStatus(HttpServletResponse.SC_OK);
                                    resp.getWriter().write(successMessage);
                                    resp.getWriter().flush();
                                }
                            } else {
                                /* update failed */
                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "account.failure.update.verified");

                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                resp.getWriter().write(errorMessage);
                                resp.getWriter().flush();
                            }
                        } else {
                            /* security code not expected */
                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "incorrect.security.code");

                            getLogger().info("Verify Registration From: '" + req.getServerName() + ":" + req.getServletPath() + "' " + errorMessage);

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(Constants.HTTP_STATUS_INCORRECT_SECURITY_CODE);
                            resp.getWriter().write(errorMessage);
                            resp.getWriter().flush();
                        }
                    }
                } else {
                    /* user not found. */
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                    getLogger().error("From: '" + req.getServerName() + ":" + req.getServletPath() + "' " + errorMessage);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(Constants.HTTP_STATUS_USER_NOT_REGISTERED);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            getLogger().error("Error on verifying account.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        }
    }
}
