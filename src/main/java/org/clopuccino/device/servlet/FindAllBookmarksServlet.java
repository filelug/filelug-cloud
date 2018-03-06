package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.PostgresqlDatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.RequestModel;
import org.clopuccino.server.servlet.websocket.ConnectSocket;
import org.clopuccino.server.servlet.Sid;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <code>FindAllBookmarksServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "find-all-bookmarks", displayName = "find-all-bookmarks", description = "Find All Bookmarks", urlPatterns = {"/bookmarks"})
public class FindAllBookmarksServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FindAllBookmarksServlet.class.getSimpleName());

    private static final long serialVersionUID = -7127184090901949383L;

    private final ClientSessionService clientSessionService;

    private final UserComputerDao userComputerDao;


    public FindAllBookmarksServlet() {
        super();

        DatabaseAccess dbAccess = new PostgresqlDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            final String userId = clientSession.getUserId();

            final Long computerId = clientSession.getComputerId();

            String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

            String clientLocale = clientSession.getLocale();

                    /* get socket by user and validate it */
            ConnectSocket socket = ConnectSocket.getInstance(userComputerId);

            if (socket != null && socket.validate(true)) {
                        /* send message to server */
                ObjectMapper mapper = Utility.createObjectMapper();
                RequestModel requestModel = new RequestModel(Sid.LIST_ALL_BOOKMARKS, userId, clientLocale);
                String requestJson = mapper.writeValueAsString(requestModel);

                socket.setHttpServletResponse(resp);
                CountDownLatch closeLatch = new CountDownLatch(1);
                socket.setCloseLatch(closeLatch);

                Session socketSession = socket.getSession();

                socketSession.getAsyncRemote().sendText(requestJson);

                // current thread blocked until ConnectSocket using resp to response to the client
                closeLatch.await(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                // normally, it is because of timeout to connect to desktop
                if (!resp.isCommitted()) {
                    // update reconnect flag to true
                    userComputerDao.updateReconnectByUserComputerId(userComputerId, Boolean.TRUE);

                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "desktop.connect.timeout.try.again");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                }
            } else {
                // update reconnect flag to true
                userComputerDao.updateReconnectByUserComputerId(userComputerId, Boolean.TRUE);

                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "server.not.connected");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            }
        } catch (Exception e) {
            LOGGER.error("Error on finding all bookmarks.", e);

            String errorMessage =e.getMessage();

            if (resp != null) {
                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
                resp.getWriter().flush();
            }
        }
    } 
}
