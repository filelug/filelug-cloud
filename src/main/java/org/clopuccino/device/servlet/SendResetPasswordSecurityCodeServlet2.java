package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ResetPasswordModel;
import org.clopuccino.domain.User;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * <code>SendResetPasswordSecurityCodeServlet2</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "send-reset-password-security-code2", displayName = "send-reset-password-security-code2", description = "Send Reset Password Security Code with Email Only", urlPatterns = {"/user/reset-code2"})
public class SendResetPasswordSecurityCodeServlet2 extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SendResetPasswordSecurityCodeServlet2.class.getSimpleName());

    private static final long serialVersionUID = 7966433573499639943L;

    protected final UserDao userDao;

    public SendResetPasswordSecurityCodeServlet2() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            ResetPasswordModel resetPasswordModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), ResetPasswordModel.class);

            String inputUserId = resetPasswordModel.getAccount();
            String inputCountryId = resetPasswordModel.getCountryId();
            String inputPhoneNumber = resetPasswordModel.getPhoneNumber();
            String inputEmail = resetPasswordModel.getEmail();
            String inputVerification = resetPasswordModel.getVerification();
            String clientLocale = resetPasswordModel.getLocale();

            boolean keepWalking = true;
            User user = null;

            if (inputUserId != null && inputUserId.trim().length() > 0 && inputVerification != null && inputVerification.trim().length() > 0) {
                // Use user id to find user

                user = userDao.findUserById(inputUserId);
            } else if (inputCountryId != null && inputCountryId.trim().length() > 0
                       && inputPhoneNumber != null && inputPhoneNumber.trim().length() > 0
                       && inputVerification != null && inputVerification.trim().length() > 0) {
                // Use phone number and country id to find possible users.

                List<User> users = userDao.findVerifiedUsersByPhone(inputCountryId, inputPhoneNumber, false);

                if (users != null) {
                    if (users.size() > 1) {
                        // Ask user to provide email to confirm if email not provided.

                        if (inputEmail == null || inputEmail.trim().length() < 1) {
                            // if not a user set email
                            boolean notAUserSetEmail = true;

                            for (User currentUser : users) {
                                String userEmail = currentUser.getUserEmail();

                                if (userEmail != null && userEmail.trim().length() > 0) {
                                    notAUserSetEmail = false;

                                    break;
                                }
                            }

                            if (notAUserSetEmail) {
                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "email.not.set2");

                                resp.setStatus(Constants.HTTP_STATUS_EMPTY_USER_EMAIL);
                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.getWriter().write(errorMessage);
                                resp.getWriter().flush();

                                keepWalking = false;
                            } else {
                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "provide.email.to.confirm");

                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                                resp.getWriter().write(errorMessage);
                                resp.getWriter().flush();

                                keepWalking = false;
                            }
                        } else {
                            boolean notAUserSetEmail = true;

                            for (User currentUser : users) {
                                String currentUserEmail = currentUser.getUserEmail();

                                if (currentUserEmail != null && currentUserEmail.trim().length() > 0) {
                                    notAUserSetEmail = false;
                                }

                                if (currentUserEmail != null && currentUserEmail.equalsIgnoreCase(inputEmail)) {
                                    user = currentUser;

                                    break;
                                }
                            }

                            if (user == null) {
                                // no matched email for users

                                keepWalking = false;

                                if (notAUserSetEmail) {
                                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "email.not.set2");

                                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                    resp.getWriter().write(errorMessage);
                                    resp.getWriter().flush();
                                } else {
                                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "email.not.same.try.again");

                                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                                    resp.getWriter().write(errorMessage);
                                    resp.getWriter().flush();
                                }
                            }
                        }
                    } else if (users.size() > 0) {
                        // Only one
                        user = users.get(0);
                    }
                }
            } else {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();

                keepWalking = false;
            }

            if (keepWalking) {
                if (user != null) {
                    // check verification

                    String userCountryId = user.getCountryId();
                    String userPhoneNumber = user.getPhoneNumber();

                    if (!inputVerification.equals(Utility.generateVerificationForSecurityCode(userCountryId, userPhoneNumber))) {
                        LOGGER.warn("Be careful that user: (" + userCountryId + ")" + userPhoneNumber + " is trying to hack the verification code for SendResetPasswordSecurityCode2");

                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().write(String.valueOf(errorMessage));
                        resp.getWriter().flush();
                    } else {
                        String userId = user.getAccount();
                        String userEmail = user.getUserEmail();

                        String securityCode = Utility.generateSecurityCode();

                        userDao.updateResetSecurityCode(userId, securityCode);

                        // Always send email with security code to user's email address

                        if (userEmail != null && userEmail.trim().length() > 0) {
                            MailSender sender = MailSenderFactory.createResetPasswordMailSender(securityCode, userEmail, userId, clientLocale);

                            sender.send();

                            ResetPasswordModel responseModel = new ResetPasswordModel();
                            responseModel.setAccount(userId);
                            responseModel.setEmail(userEmail);
                            String responseString = mapper.writeValueAsString(responseModel);

                            resp.setStatus(HttpServletResponse.SC_OK);
                            resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                            resp.getWriter().write(responseString);
                            resp.getWriter().flush();
                        } else {
                            // email address not set

                            resp.setStatus(Constants.HTTP_STATUS_EMPTY_USER_EMAIL);
                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "email.not.set2"));
                            resp.getWriter().flush();
                        }
                    }
                } else {
                    // User not found

                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            String errorMessage = e.getClass().getName() + ": " + e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        }

    }
}
