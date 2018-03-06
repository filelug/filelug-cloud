package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.device.EmailValidator;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.User;
import org.clopuccino.service.ClientSessionService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <code>CreateOrUpdateUserProfileServlet</code> creates or updates user profile data,
 * including email and nickname
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "create-or-update-user-profile", displayName = "create-or-update-user-profile", description = "Create or update user profile", urlPatterns = {"/user/uprofile"})
public class CreateOrUpdateUserProfileServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CreateOrUpdateUserProfileServlet.class.getSimpleName());

    private static final long serialVersionUID = -1560621911204359101L;

    private final ClientSessionService clientSessionService;

    private final UserDao userDao;

    public CreateOrUpdateUserProfileServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);

        userDao = new UserDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String clientLocale = clientSession.getLocale();

            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode emailNode = jsonNode.get("email");

            JsonNode nicknameNode = jsonNode.get("nickname");

            if (emailNode == null || emailNode.textValue() == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "email");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (nicknameNode == null || nicknameNode.textValue() == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "nickname");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (!new EmailValidator().validate(emailNode.textValue())) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "incorrect.email.format", emailNode.textValue());

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
                } else {
                    String unverifiedEmail = emailNode.textValue();
                    String nickname = nicknameNode.textValue();

                    // update user email and nickname in db
                    userDao.createOrUpdateUserProfileWithUnverifiedEmail(userId, unverifiedEmail, nickname);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("OK");
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on creating or updating user profile.",e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
