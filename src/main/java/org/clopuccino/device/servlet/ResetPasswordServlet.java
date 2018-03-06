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
import org.clopuccino.service.ComputerService;
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
 * <code>ResetPasswordServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "reset-password", displayName = "reset-password", description = "Reset Password", urlPatterns = {"/user/reset-password"})
public class ResetPasswordServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ResetPasswordServlet.class.getSimpleName());

    private static final long serialVersionUID = 3949433786754098872L;

    private final UserDao userDao;

    private final UserComputerDao userComputerDao;

    private final ClientSessionService clientSessionService;

    private final ComputerService computerService;

    public ResetPasswordServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);

        computerService = new ComputerService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            ResetPasswordModel registerModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), ResetPasswordModel.class);

            String userId = registerModel.getAccount();
            String password = registerModel.getPassword();
            String encryptedSecurityCode = registerModel.getSecurityCode();
            String verification = registerModel.getVerification();
            String clientLocale = registerModel.getLocale();

            String emptySha256 = DigestUtils.sha256Hex("");

            if (userId == null || password == null || encryptedSecurityCode == null || verification == null
                || userId.trim().length() < 1 || password.trim().length() < 1 || encryptedSecurityCode.trim().length() < 1 || verification.trim().length() < 1
                || password.equals(emptySha256)
                || encryptedSecurityCode.equals(emptySha256)
                || verification.equals(emptySha256)) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                // Check if user already registered

                User user = userDao.findUserById(userId);

                if (user == null) {
                    /* user not found or user id is not consistant with country id and phone number */
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
                } else if (!verification.equals(Utility.generateResetPasswordVerification(userId, user.getCountryId(), user.getPhoneNumber(), password, encryptedSecurityCode))) {
                    LOGGER.warn("Be careful that user: " + userId + " (" + user.getCountryId() + ")" + user.getPhoneNumber() + " is trying to hack the verification code for ResetPassword");

                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(String.valueOf(errorMessage));
                    resp.getWriter().flush();
                } else if (!encryptedSecurityCode.equals(DigestUtils.sha256Hex(user.getResetPasswordSecurityCode()))) {
                    /* security code not expected */
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "incorrect.security.code");

                    LOGGER.info("Reset Password From: '" + req.getServerName() + ":" + req.getServletPath() + "' " + errorMessage);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(Constants.HTTP_STATUS_INCORRECT_SECURITY_CODE);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    /* change password in db */
                    userDao.updatePassword(userId, password, true);

                    /* remove all sessions of this user from devices */
                    clientSessionService.removeClientSessionsByUser(userId);

                    /* ask all computers connecting using this account to reconnect */
                    userComputerDao.updateReconnectByUserId(userId, Boolean.TRUE);

                    String responseMessage = computerService.findComputerNamesByUserId(userId);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(responseMessage);
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
