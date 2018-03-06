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
import org.clopuccino.domain.Bookmark;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.RequestUpdateBookmarkModel;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <code>UpdateBookmarkServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "update-bookmark", displayName = "update-bookmark", description = "find bookmark by id", urlPatterns = {"/bookmarks/change"})
public class UpdateBookmarkServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(UpdateBookmarkServlet.class.getSimpleName());

    private static final long serialVersionUID = 435643761315019479L;

    private final ClientSessionService clientSessionService;

    private final UserComputerDao userComputerDao;


    public UpdateBookmarkServlet() {
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

                    /* get socket by user and validate it */
            ConnectSocket socket = ConnectSocket.getInstance(userComputerId);

            if (socket != null && socket.validate(true)) {
                        /* check json input */
                ObjectMapper mapper = Utility.createObjectMapper();

                Bookmark inputBookmark = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), Bookmark.class);

                Long bookmarkId = inputBookmark.getId();
                String bookmarkPath = inputBookmark.getPath();
                String bookmarkLabel = inputBookmark.getLabel();

                if (bookmarkId == null) {
                            /* path not found or invalid */
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "bookmark.id.empty");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else if (bookmarkPath == null || bookmarkPath.trim().length() < 1) {
                            /* path is empty */
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "bookmark.path.not.null");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else if (bookmarkLabel == null || bookmarkLabel.trim().length() < 1) {
                            /* label is empty */
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "bookmark.label.not.null");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    // send message to server
                    RequestUpdateBookmarkModel requestModel = new RequestUpdateBookmarkModel(Sid.UPDATE_BOOKMARK, userId, bookmarkId, bookmarkPath, bookmarkLabel, clientLocale);

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
            LOGGER.error("Error on updating bookmarks.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
