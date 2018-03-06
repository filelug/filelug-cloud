package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.*;
import org.clopuccino.dao.ApplyConnectionDao;
import org.clopuccino.dao.ComputerDao;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ApplyConnection;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.User;
import org.clopuccino.server.servlet.websocket.ConnectSocket;
import org.clopuccino.server.servlet.websocket.ConnectSocketUtilities;
import org.clopuccino.service.ClientSessionService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.CloseReason;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <code>RemoveAllUsersFromComputerServlet</code> removes the all users from the computer.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "remove-all-users-from-computer", displayName = "remove-all-users-from-computer", description = "Remove all users from computer.", urlPatterns = {"/computer/rmusers"})
public class RemoveAllUsersFromComputerServlet extends HttpServlet {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(RemoveAllUsersFromComputerServlet.class.getSimpleName());

    private static final long serialVersionUID = 5476298066886956361L;

    private final UserDao userDao;

    private final ComputerDao computerDao;

    private final UserComputerDao userComputerDao;

    private final ApplyConnectionDao applyConnectionDao;

    private final ClientSessionService clientSessionService;


    public RemoveAllUsersFromComputerServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        applyConnectionDao = new ApplyConnectionDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String sessionId = clientSession.getSessionId();

            String userId = clientSession.getUserId();

            Long computerId = clientSession.getComputerId();

            String userComputerId = clientSession.getUserComputerId();

            String clientLocale = clientSession.getLocale();

            String computerName = computerDao.findComputerNameByComputerId(computerId);

            if (computerName == null) {
                // computer not exists

                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "computer.not.found");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(Constants.HTTP_STATUS_COMPUTER_NOT_FOUND);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                // check json input
                ObjectMapper mapper = Utility.createObjectMapper();

                JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

                JsonNode verificationNode = jsonNode.get(PropertyConstants.PROPERTY_NAME_VERIFICATION);

                if (verificationNode == null || verificationNode.textValue() == null || !verificationNode.textValue().equals(Utility.generateRemoveAdminVerification(userId, computerId, sessionId))) {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("ERROR");
                    resp.getWriter().flush();
                } else {
                    // check if the user is the admin of this computer

                    ApplyConnection applyConnection = applyConnectionDao.findApplyConnectionByApplyUserAndComputerId(userId, computerId);

                    if (applyConnection == null || !userId.equals(applyConnection.getApprovedUser())) {
                        // User is not the administrator of this computer

                        User user = userDao.findUserById(userId);

                        String userNickname = (user != null) ? user.getNickname() : "";

                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.computer.admin", computerName, userNickname);

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else {
                        // Close the socket from server, not desktop.

                        ConnectSocket socket = ConnectSocket.getInstance(userComputerId);

                        if (socket != null) {
                            ConnectSocket.removeInstance(userComputerId);

                            ConnectSocketUtilities.closeSession(socket.getSession(), CloseReason.CloseCodes.NORMAL_CLOSURE, "Remove current administrator from computer.");
                        }

                        // Delete the following data in db, so all current users of this computer can't connect to it again:
                        // 1. UserComputer: with the computerId.
                        // 2. ClientSession with the sessionId -> If deleted with the computerId, the user in the device will need to login using Facebook Account Kit
                        // 3. ApplyConnection: with this computerId

                        userComputerDao.deleteUserComputerByComputerId(computerId);

                        clientSessionService.removeClientSessionsBySessionId(sessionId);

                        applyConnectionDao.deleteApprovedUsersByComputerId(computerId);

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write("OK");
                        resp.getWriter().flush();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on changing computer administrator.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
