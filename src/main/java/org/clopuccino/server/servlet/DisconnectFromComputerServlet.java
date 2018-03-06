package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ClientSession;
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

/**
 * <code>DisconnectFromComputerServlet</code> disconnect from server.
 * The service does not apply ClientSessionFilter because if the session is invalid(exists, but timeout),
 * the connection between desktop and server is disconnected anyway and there's not error status code 401 returned.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "disconnect-from-computer", displayName = "disconnect-from-computer", description = "Disconnect from server", urlPatterns = {"/computer/disconnect"})
public class DisconnectFromComputerServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DisconnectFromComputerServlet.class.getSimpleName());

    private static final long serialVersionUID = 6282383377462818134L;

    private final ClientSessionService clientSessionService;

    public DisconnectFromComputerServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // validate client session id

            String clientSessionId = req.getHeader(Constants.HTTP_HEADER_FILELUG_SESSION_ID_NAME);

            if (clientSessionId == null || clientSessionId.trim().length() < 1) {
                clientSessionId = req.getHeader(Constants.HTTP_HEADER_AUTHORIZATION_NAME);
            }

            if (clientSessionId == null || clientSessionId.trim().length() < 1) {
                String errorMessage = ClopuccinoMessages.localizedMessage(ClopuccinoMessages.DEFAULT_LOCALE, "session.not.provided");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();

                LOGGER.warn("Session not provided for service: " + req.getRequestURI());
            } else {
                ClientSession clientSession = clientSessionService.findClientSessionBySessionId(clientSessionId);

                if (clientSession == null) {
                    // session not found

                    String errorMessage = ClopuccinoMessages.localizedMessage(ClopuccinoMessages.DEFAULT_LOCALE, "session.not.exists");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();

                    LOGGER.warn(String.format("Session not found.\nSession id: '%s'\nRequest URI: '%s'", clientSessionId, req.getRequestURI()));
                } else {
                    // update session last access timestamp only if the session is not timeout

                    if (!clientSession.checkTimeout()) {
                        long lastAccessTimestamp = System.currentTimeMillis();

                        clientSessionService.updateClientSessionLastAccessTimestamp(clientSessionId, lastAccessTimestamp);
                    }

                    String userComputerId = clientSession.getUserComputerId();

                    // When invoiked from desktop will NOT disconnect NOR close the socket session.
                    // So the socket of the server should do the close.

                    ConnectSocket socket = ConnectSocket.getInstance(userComputerId);

                    if (socket != null) {
                        ConnectSocket.removeInstance(userComputerId);

                        ConnectSocketUtilities.closeSession(socket.getSession(), CloseReason.CloseCodes.NORMAL_CLOSURE, "Request to close socket by client.");
                    }

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("OK");
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(e.getMessage() != null ? e.getMessage() : "ERROR");
            resp.getWriter().flush();
        }
    }
}
