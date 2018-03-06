package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.service.ComputerService;
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
 * <code>FindFileByPathServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "change-password", displayName = "change-password", description = "Change the password of the current user", urlPatterns = {"/user/password"})
public class ChangePasswordServlet extends HttpServlet {

    private static final long serialVersionUID = 9168422562236458930L;

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ChangePasswordServlet.class.getSimpleName());

    private final UserDao userDao;

    private final UserComputerDao userComputerDao;

    private final ClientSessionService clientSessionService;

    private final ComputerService computerService;

    public ChangePasswordServlet() {
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
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String userId = clientSession.getUserId();

            String clientLocale = clientSession.getLocale();

                    /* check json input */
            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode oldPasswordNode = jsonNode.get("old-passwd");

            JsonNode newPasswordNode = jsonNode.get("new-passwd");

            if (oldPasswordNode == null || newPasswordNode == null
                || oldPasswordNode.textValue() == null || newPasswordNode.textValue() == null) {
                        /* old or new password not found or invalid */
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "password");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                        /* update the password of the user */
                User user = userDao.findUserById(userId);

                if (user == null) {
                            /* user not exists */
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                            /* validate with password */
                    String oldPassword = oldPasswordNode.textValue();
                    String newPassword = newPasswordNode.textValue();

                    String passwordInResp = user.getPasswd();

                    if (oldPassword.equals(passwordInResp)) {
                                /* change password in db */
                        userDao.updatePassword(userId, newPassword, false);

                                /* remove all sessions of this user from devices */
                        clientSessionService.removeClientSessionsByUser(userId);

                                /* ask all computers connecting using this account to reconnect */
                        userComputerDao.updateReconnectByUserId(userId, Boolean.TRUE);

                        String responseMessage = computerService.findComputerNamesByUserId(userId);

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write(responseMessage);
                        resp.getWriter().flush();
                    } else {
                                /* password not correct */
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "password.not.correct", user.getNickname());

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on changing password.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    } 
}
