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
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.device.EmailValidator;
import org.clopuccino.domain.ChangeUserEmailModel;
import org.clopuccino.domain.ClientSession;
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
 * <code>ChangeUserEmailServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "change-user-email", displayName = "change-user-email", description = "Change User Email", urlPatterns = {"/user/change-email"})
public class ChangeUserEmailServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ChangeUserEmailServlet.class.getSimpleName());

    private static final long serialVersionUID = 2425452919214206462L;

    private final ClientSessionService clientSessionService;

    private final UserDao userDao;

    public ChangeUserEmailServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);

        userDao = new UserDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            ObjectMapper mapper = Utility.createObjectMapper();

            ChangeUserEmailModel userEmailModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), ChangeUserEmailModel.class);

            String newEmail = userEmailModel.getNewEmail();
            String encryptedSecurityCode = userEmailModel.getSecurityCode();
            String clientLocale = clientSession.getLocale();

            String emptySha256 = DigestUtils.sha256Hex("");

            if (newEmail == null || encryptedSecurityCode == null
                || newEmail.trim().length() < 1 || encryptedSecurityCode.trim().length() < 1
                || encryptedSecurityCode.equals(emptySha256)) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                String userId = clientSession.getUserId();

                User user = userDao.findUserById(userId);

                if (user == null) {
                    // user not found 
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else if (user.getChangeEmailSecurityCode() == null || !encryptedSecurityCode.equals(DigestUtils.sha256Hex(user.getChangeEmailSecurityCode()))) {
                    // security code not expected 
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "incorrect.security.code");

                    LOGGER.warn("Change User Email From: '" + req.getServerName() + ":" + req.getServletPath() + "' user: " + userId + "\n" + errorMessage);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(Constants.HTTP_STATUS_INCORRECT_SECURITY_CODE);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else if (user.getUnverifiedUserEmail() == null || !user.getUnverifiedUserEmail().equalsIgnoreCase(newEmail)) {
                    // ask user to send security code again
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "send.security.code.to.email", newEmail);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else if (newEmail.equalsIgnoreCase(user.getUserEmail())) {
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "same.email");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else if (!new EmailValidator().validate(newEmail)) {
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "incorrect.email.format", newEmail);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    // change user email in db and clear change-email related data 
                    userDao.updateEmail(userId, newEmail, true);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("OK");
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on change user email.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ?  errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
