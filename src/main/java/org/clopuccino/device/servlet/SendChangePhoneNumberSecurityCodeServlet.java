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
import org.clopuccino.domain.ChangePhoneNumberModel;
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
 * <code>SendChangePhoneNumberSecurityCodeServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "send-change-phone-number-security-code", displayName = "send-change-phone-number-security-code", description = "Send Chagne Phone Number Security Code", urlPatterns = {"/user/change-phone-number-code"})
public class SendChangePhoneNumberSecurityCodeServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SendChangePhoneNumberSecurityCodeServlet.class.getSimpleName());

    private static final long serialVersionUID = -7950547800838066796L;

    protected final UserDao userDao;

    public SendChangePhoneNumberSecurityCodeServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            ChangePhoneNumberModel changePhoneNumberModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), ChangePhoneNumberModel.class);

            String userId = changePhoneNumberModel.getAccount();
            String verification = changePhoneNumberModel.getVerification();

            String password = changePhoneNumberModel.getPassword();
            String newCountryId = changePhoneNumberModel.getCountryId();
            String newPhoneNumber = changePhoneNumberModel.getPhoneNumber();

            String clientLocale = changePhoneNumberModel.getLocale();

            if (userId == null || verification == null || password == null || newCountryId == null || newPhoneNumber == null
                || userId.trim().length() < 1 || verification.trim().length() < 1 || password.trim().length() < 1 || newCountryId.trim().length() < 1 || newPhoneNumber.trim().length() < 1
                || verification.equals(DigestUtils.sha256Hex(""))
                || password.equals(DigestUtils.sha256Hex(""))) {
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
                } else if (!password.equals(user.getPasswd())) {
                    /* incorrect password */
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "password.not.correct", user.getNickname());

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    /* check verification */

                    if (!verification.equals(Utility.generateVerificationForChangePhoneNumberSecurityCode(userId, newCountryId, newPhoneNumber, password))) {
                        LOGGER.warn("Be careful that user: '" + userId + "' is trying to hack the verification code for SendChangePhoneNumberSecurityCode");

                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().write(String.valueOf(errorMessage));
                        resp.getWriter().flush();
                    } else {
                        // Check if new phone number has been used by others

                        String duplicatedUserId = null;

                        List<User> users = userDao.findUsersByPhone(newCountryId, newPhoneNumber, false);

                        if (users != null && users.size() > 0) {
                            for (User currentUser : users) {
                                if (currentUser.getVerified()) {
                                    duplicatedUserId = currentUser.getAccount();

                                    break;
                                }
                            }
                        }

                        if (duplicatedUserId != null) {
                            LOGGER.warn("New phone number: '" + newPhoneNumber + "' is used by user: " + duplicatedUserId);

                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.should.update.phone.number", newPhoneNumber);

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(Constants.HTTP_STATUS_SHOULD_UPDATE_PHONE_NUMBER);
                            resp.getWriter().write(String.valueOf(errorMessage));
                            resp.getWriter().flush();
                        } else {
                            String securityCode = Utility.generateSecurityCode();

                            userDao.updateChangePhoneNumberSecurityCode(userId, newPhoneNumber, securityCode);

                            SMSSender sender = SMSSenderFactory.createChangePhoneNumberSmsSender(newCountryId, newPhoneNumber, userId, clientLocale);

                            if (sender != null) {
                                sender.send();

                                resp.setStatus(HttpServletResponse.SC_OK);
                                resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                resp.getWriter().write("Security code sent.");
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
