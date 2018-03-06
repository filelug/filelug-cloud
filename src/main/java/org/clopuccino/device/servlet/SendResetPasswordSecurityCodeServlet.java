package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
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

/**
 * <code>SendResetPasswordSecurityCodeServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "send-reset-password-security-code", displayName = "send-reset-password-security-code", description = "Send Reset Password Security Code", urlPatterns = {"/user/reset-code"})
public class SendResetPasswordSecurityCodeServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SendResetPasswordSecurityCodeServlet.class.getSimpleName());

    private static final long serialVersionUID = -1830747609502649012L;

    protected final UserDao userDao;

    public SendResetPasswordSecurityCodeServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            ResetPasswordModel resetPasswordModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), ResetPasswordModel.class);

            String userId = resetPasswordModel.getAccount();
            String verification = resetPasswordModel.getVerification();
            String clientLocale = resetPasswordModel.getLocale();

            if (userId == null || verification == null
                || userId.trim().length() < 1 || verification.trim().length() < 1
                || verification.equals(DigestUtils.sha256Hex(""))) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                // Check if user already registered

                User user = userDao.findUserById(userId);

                if (user == null) {
                    /* user id not found */
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else if (!user.getVerified()) {
                    /* user not verified yet */
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "account.not.verified", user.getPhoneNumber());

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    /* check verification */
                    String countryId = user.getCountryId();
                    String phoneNumber = user.getPhoneNumber();

                    if (!verification.equals(Utility.generateVerificationForSecurityCode(userId, countryId, phoneNumber))) {
                        LOGGER.warn("Be careful that user: (" + countryId + ")" + phoneNumber + " is trying to hack the verification code for SendResetPasswordSecurityCode");

                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().write(String.valueOf(errorMessage));
                        resp.getWriter().flush();
                    } else {
                        String securityCode = Utility.generateSecurityCode();

                        userDao.updateResetSecurityCode(userId, securityCode);

                        // Check if should update phone number

                        if (user.getShouldUpdatePhoneNumber()) {
                            // send email with security code to user's email address

                            String sentToEmail = user.getUserEmail();

                            if (sentToEmail != null && sentToEmail.trim().length() > 0) {
                                MailSender sender = MailSenderFactory.createResetPasswordMailSender(securityCode, sentToEmail, userId, clientLocale);

                                sender.send();

                                resp.setStatus(HttpServletResponse.SC_OK);
                                resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                // response message is the sending target
                                resp.getWriter().write(sentToEmail);
                                resp.getWriter().flush();
                            } else {
                                // email address not set

                                resp.setStatus(Constants.HTTP_STATUS_EMPTY_USER_EMAIL);
                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "email.not.set"));
                                resp.getWriter().flush();
                            }
                        } else {
                            // send sms to the phone

                            SMSSender sender = SMSSenderFactory.createResetPasswordSmsSender(countryId, userId, clientLocale);

                            if (sender != null) {
                                sender.send();

                                resp.setStatus(HttpServletResponse.SC_OK);
                                resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                // response message is the sending target
                                resp.getWriter().write(phoneNumber);
                                resp.getWriter().flush();
                            } else {
                                // country not supported

                                resp.setStatus(Constants.HTTP_STATUS_COUNTRY_NOT_SUPPORTED);
                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "country.not.support"));
                                resp.getWriter().flush();
                            }
                        }
                    }
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
