package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.PostgresqlDatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.PingDesktopModel;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <code>PingDesktopServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "ping-desktop", displayName = "ping-desktop", description = "Ping if desktop is accessible via web socket", urlPatterns = {"/system/dping"})
public class PingDesktopServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(PingDesktopServlet.class.getSimpleName());

    private static final long serialVersionUID = 7773894122978314215L;

    private final ClientSessionService clientSessionService;

    private final UserComputerDao userComputerDao;

    private final UserDao userDao;

    public PingDesktopServlet() {
        super();

        DatabaseAccess dbAccess = new PostgresqlDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        userDao = new UserDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            final String userId = clientSession.getUserId();

            final Long computerId = clientSession.getComputerId();

            String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

            String clientLocale = clientSession.getLocale();

            // check json input
            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode accountNode = jsonNode.get("account");

            if (accountNode == null) {
                // path not found or invalid
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.not.provided.or.incorrect", "");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                String inputAccount = accountNode.textValue();

                if (userId.equals(inputAccount)) {
                    // get socket by user-computer-id and validate it
                    Object[] socketConnectedAndLugServerId = userComputerDao.findSocketConnectedAndLugServerIdByUserComputerId(userComputerId);

                    Boolean socketConnected = (Boolean) socketConnectedAndLugServerId[0];

                    if (socketConnected != null && socketConnected) {
                        // No more change for the same user computer unless the lug server is not available
//                        // client will download or upload file, so dispatch lug server id to the next one
//                        ConnectionDispatchService.nextLugServerIdIndex(userComputerId);

                        long[] downloadAndUploadFileSizeLimits = userDao.findDownloadAndUploadFileSizeLimitInBytes(userId);

                        PingDesktopModel pingDesktopModel = new PingDesktopModel(downloadAndUploadFileSizeLimits[0], downloadAndUploadFileSizeLimits[1]);

                        String responseJson = mapper.writeValueAsString(pingDesktopModel);

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write(responseJson);
                        resp.getWriter().flush();
                    } else {
                        // update reconnect flag to true
                        userComputerDao.updateReconnectByUserComputerId(userComputerId, Boolean.TRUE);

                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "server.not.connected");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    }
                } else {
                    // desktop information is not the same with the current connection
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.not.provided.or.incorrect", "");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on pinging desktop");

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
