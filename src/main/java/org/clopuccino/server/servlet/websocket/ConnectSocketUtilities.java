package org.clopuccino.server.servlet.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.domain.ResponseModel;
import org.clopuccino.server.servlet.Sid;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.CloseReason;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ConnectSocketUtilities {

    public static void onUnsupportedWebSocket(String userId, Session session, String message, Integer sid) {
        try {
            String errorMessage = sid + " is an unsupported service or you need to connect first in order to use this service.";
            ResponseModel responseModel = new ResponseModel(Sid.UNSUPPORTED, HttpServletResponse.SC_NOT_FOUND, errorMessage, userId, System.currentTimeMillis());

            ObjectMapper mapper = Utility.createObjectMapper();

            RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

            asyncRemote.setSendTimeout(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_MILLIS);

            asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//            session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
        } catch (JsonProcessingException e) {
            int httpStatusCode = HttpServletResponse.SC_BAD_REQUEST;

            processOnMessageException(userId, session, Sid.UNSUPPORTED, e, httpStatusCode, false);
        } catch (Exception e) {
            int httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

            processOnMessageException(userId, session, Sid.UNSUPPORTED, e, httpStatusCode, false);
        }
    } // end onUnsupportedWebSocket(Session, String, Integer)

    public static void processOnMessageException(String userId, Session session, Integer sid, Exception e, int httpStatusCode, boolean needCloseAndDisconnect) {
        String errorMessage = String.format("Error on processing received message.%n%s%n%s%n", e.getClass().getName(), e.getMessage());

        ResponseModel responseModel = new ResponseModel(sid != null ? sid : null, httpStatusCode, errorMessage, userId, System.currentTimeMillis());

        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

            asyncRemote.setSendTimeout(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_MILLIS);

            asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//            Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e1) {
            /* ignored */
        } finally {
            if (needCloseAndDisconnect) {
                closeSessionWithBadDataStatusCode(session, errorMessage);
            }
        }
    }

    public static void closeSessionWithBadDataStatusCode(final Session session, final String reason) {
        closeSession(session, CloseReason.CloseCodes.UNEXPECTED_CONDITION, reason);
    }

    public static void closeSession(final Session session, final CloseReason.CloseCodes closeCodes, final String reason) {
        try {
            session.close(new CloseReason(closeCodes, reason));
        } catch (Exception e) {
            // ignored
        }
    }
}
