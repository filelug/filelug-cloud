package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * <code>SendChangeUserEmailSecurityCodeServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "send-change-user-email-security-code", displayName = "send-change-user-email-security-code", description = "Send Change-User-Email Security Code", urlPatterns = {"/user/change-email-code"})
public class SendChangeUserEmailSecurityCodeServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SendChangeUserEmailSecurityCodeServlet.class.getSimpleName());

    private static final long serialVersionUID = 6265587985265127016L;

    private final ClientSessionService clientSessionService;

    protected final UserDao userDao;

    public SendChangeUserEmailSecurityCodeServlet() {
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

            ChangeUserEmailModel changeUserEmailModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), ChangeUserEmailModel.class);

            String newEmail = changeUserEmailModel.getNewEmail();

            String clientLocale = clientSession.getLocale();

            if (newEmail == null || newEmail.trim().length() < 1) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                String userId = clientSession.getUserId();

                // check if new email is well-formatted

                EmailValidator emailValidator = new EmailValidator();

                if (!emailValidator.validate(newEmail)) {
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "incorrect.email.format", newEmail);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    User user = userDao.findUserById(userId);

                    if (user != null) {
                        String verifiedEmail = user.getUserEmail();

                        if (verifiedEmail != null && verifiedEmail.equalsIgnoreCase(newEmail)) {
                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "email.verified");

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_CONFLICT);
                            resp.getWriter().write(errorMessage);
                            resp.getWriter().flush();
                        } else {
                            String securityCode = Utility.generateSecurityCode();

                            // send mail
                            MailSender sender = MailSenderFactory.createChangeEmailMailSender(securityCode, newEmail, userId, clientLocale);

                            sender.send();

                            resp.setStatus(HttpServletResponse.SC_OK);
                            resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                            resp.getWriter().write("Security code sent.");
                            resp.getWriter().flush();
                        }
                    } else {
                        // user not found
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on sending security code to verify new email.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }

    }
}
