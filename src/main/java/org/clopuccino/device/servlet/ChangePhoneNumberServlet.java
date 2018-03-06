package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.service.ClientSessionService;
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
 * <code>ChangePhoneNumberServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "change-phone-number", displayName = "change-phone-number", description = "Chagne Phone Number", urlPatterns = {"/user/change-phone-number"})
public class ChangePhoneNumberServlet extends HttpServlet {

    private static final long serialVersionUID = -6262053162600514101L;

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ChangePhoneNumberServlet.class.getSimpleName());

    protected final UserDao userDao;

    private final UserComputerDao userComputerDao;

    private final ClientSessionService clientSessionService;

    public ChangePhoneNumberServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            ChangePhoneNumberModel changePhoneNumberModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), ChangePhoneNumberModel.class);

            String userId = changePhoneNumberModel.getAccount();
            String verification = changePhoneNumberModel.getVerification();

            String encryptedSecurityCode = changePhoneNumberModel.getSecurityCode();
            String newCountryId = changePhoneNumberModel.getCountryId();
            String newPhoneNumber = changePhoneNumberModel.getPhoneNumber();

            String clientLocale = changePhoneNumberModel.getLocale();

            if (userId == null || verification == null || encryptedSecurityCode == null || newCountryId == null || newPhoneNumber == null
                || userId.trim().length() < 1 || verification.trim().length() < 1 || encryptedSecurityCode.trim().length() < 1 || newCountryId.trim().length() < 1 || newPhoneNumber.trim().length() < 1
                || verification.equals(DigestUtils.sha256Hex(""))
                || encryptedSecurityCode.equals(DigestUtils.sha256Hex(""))) {
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
                } else if (user.getChangePhoneNumberSecurityCode() == null || !encryptedSecurityCode.equals(DigestUtils.sha256Hex(user.getChangePhoneNumberSecurityCode()))) {
                    /* security code not expected */
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "incorrect.security.code");

                    LOGGER.warn("Change Phone Number From: '" + req.getServerName() + ":" + req.getServletPath() + "' user: " + userId + "\n" + errorMessage);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(Constants.HTTP_STATUS_INCORRECT_SECURITY_CODE);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else if (user.getUnverifiedPhoneNumber() == null || !user.getUnverifiedPhoneNumber().equals(newPhoneNumber)) {
                    /* ask user to send security code again */
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "send.security.code.to.phone.number", newPhoneNumber);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else if (newPhoneNumber.equalsIgnoreCase(user.getPhoneNumber()) && newCountryId.equalsIgnoreCase(user.getCountryId())) {
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "same.phone.number");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    /* check verification */

                    if (!verification.equals(Utility.generateChangePhoneNumberVerification(userId, newCountryId, newPhoneNumber, user.getPasswd(), encryptedSecurityCode))) {
                        LOGGER.warn("Be careful that user: '" + userId + "' is trying to hack the verification code for ChangePhoneNumber");

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
                            userDao.updatePhoneNumberById(userId, newCountryId, newPhoneNumber, true);

                            /* remove all sessions of this user from devices */
                            clientSessionService.removeClientSessionsByUser(userId);

                            /* ask all computers connecting using this account to reconnect */
                            userComputerDao.updateReconnectByUserId(userId, Boolean.TRUE);

                            resp.setStatus(HttpServletResponse.SC_OK);
                            resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                            resp.getWriter().write("OK");
                            resp.getWriter().flush();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on changing phone number.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
