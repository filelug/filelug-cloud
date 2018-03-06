package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.PostgresqlDatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.Bookmark;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.RequestBookmarkArrayModel;
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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <code>SynchronizeBookmarksServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "synchronize-bookmarks", displayName = "synchronize-bookmarks", description = "synchronize bookmarks", urlPatterns = {"/bookmarks/synchronize"})
public class SynchronizeBookmarksServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SynchronizeBookmarksServlet.class.getSimpleName());

    private static final long serialVersionUID = 5335160253097998281L;

    private final ClientSessionService clientSessionService;

    private final UserComputerDao userComputerDao;


    public SynchronizeBookmarksServlet() {
        super();

        DatabaseAccess dbAccess = new PostgresqlDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            final String userId = clientSession.getUserId();

            final Long computerId = clientSession.getComputerId();

            String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

            String clientLocale = clientSession.getLocale();

            // get socket by user and validate it
            ConnectSocket socket = ConnectSocket.getInstance(userComputerId);

            if (socket != null && socket.validate(true)) {
                        /* check json input */
                ObjectMapper mapper = Utility.createObjectMapper();

                List<Bookmark> bookmarks;

                try {
                    bookmarks = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), new TypeReference<List<Bookmark>>() {
                    });

                    if (bookmarks == null) {
                        bookmarks = new ArrayList<>();
                    }

                    // send message to server
                    RequestBookmarkArrayModel requestModel = new RequestBookmarkArrayModel(Sid.SYNCHRONIZE_BOOKMARKS, userId, clientLocale, bookmarks);

                    String requestJson = mapper.writeValueAsString(requestModel);

                    socket.setHttpServletResponse(resp);
                    CountDownLatch closeLatch = new CountDownLatch(1);
                    socket.setCloseLatch(closeLatch);

                    Session socketSession = socket.getSession();
                    socketSession.getAsyncRemote().sendText(requestJson);
//                    socketSession.getRemote().sendStringByFuture(requestJson);

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
                } catch (IOException e) {
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "bookmark.incorrect.create.data.format");

                    LOGGER.error(errorMessage, e);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
            LOGGER.error("Error on sychronizing bookmarks.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
