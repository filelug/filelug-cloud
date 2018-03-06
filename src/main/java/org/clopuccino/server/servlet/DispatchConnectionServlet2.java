package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.PropertyConstants;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.service.ConnectionDispatchService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <code>DispatchConnectionServlet</code> requests a lug server to connect to.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "dispatch-connection2", displayName = "dispatch-connection2", description = "Request the lug server to connect(V2)", urlPatterns = {"/computer/dispatch2"})
public class DispatchConnectionServlet2 extends HttpServlet {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DispatchConnectionServlet2.class.getSimpleName());

    private static final long serialVersionUID = 2418889563768235715L;

    private final UserComputerDao userComputerDao;

    private final ClientSessionService clientSessionService;

    public DispatchConnectionServlet2() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userComputerDao = new UserComputerDao(dbAccess);

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

                    String userId = clientSession.getUserId();

                    long computerId = clientSession.getComputerId();

                    String lugServerId = ConnectionDispatchService.dispatchConnectionBy(userId, computerId);

                    // update to UserComputer

                    String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

                    userComputerDao.updateLugServerById(userComputerId, lugServerId);

                    // response with new lug server id

                    ObjectMapper mapper = Utility.createObjectMapper();

                    ObjectNode returnNode = mapper.createObjectNode();

                    returnNode.put(PropertyConstants.PROPERTY_NAME_LUG_SERVER_ID, lugServerId);

                    String jsonString = mapper.writeValueAsString(returnNode);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(jsonString);
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error("Error on dispatching connection.", e);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
