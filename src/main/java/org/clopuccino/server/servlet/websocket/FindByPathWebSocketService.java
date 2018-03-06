package org.clopuccino.server.servlet.websocket;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.domain.HierarchicalModel;
import org.clopuccino.domain.ResponseFindFileByPathModel;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.util.concurrent.CountDownLatch;

/**
 * <code>FindByPathWebSocketService</code> handles SID: FIND_BY_PATH, to get the information of a file or directory of the specified file path.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class FindByPathWebSocketService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FindByPathWebSocketService.class.getSimpleName());

    private final Session session;

    private final String message;

    private final ConnectSocket connectSocket;

    public FindByPathWebSocketService(Session session, String message, ConnectSocket connectSocket) {
        this.session = session;
        this.message = message;
        this.connectSocket = connectSocket;
    }

    public void onFindFileByPathWebSocket() {
        // requested to find the information of the specified file or directory

        HttpServletResponse resp = connectSocket.getHttpServletResponse();

        if (resp != null && !resp.isCommitted()) {
            try {
                ObjectMapper mapper = Utility.createObjectMapper();

                ResponseFindFileByPathModel responseModel = mapper.readValue(message, ResponseFindFileByPathModel.class);

                Integer status = responseModel.getStatus();

                if (status != null && HttpServletResponse.SC_OK == status) {
                    HierarchicalModel model = responseModel.getResult();

                    String rootsJson = mapper.writeValueAsString(model);

                    /* DEBUG */
                    LOGGER.debug(String.format("Prepared file or directory information to response for service findFileByPath: %s", rootsJson));

                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                    resp.setStatus(status);
                    resp.getWriter().write(rootsJson);
                    resp.getWriter().flush();
                } else {
                    String error = responseModel.getError();

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(error);
                    resp.getWriter().flush();
                }
            } catch (Exception e) {
                String errorMessage = String.format("Error on processing response message of service 'findFileByPath'. Message from desktop: %s; error message: %s", message, e.getMessage());

                LOGGER.error(errorMessage);

                try {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } catch (Exception e1) {
                    /* ignored */
                }
            } finally {
                CountDownLatch closeLatch = connectSocket.getCloseLatch();

                if (closeLatch != null) {
                    closeLatch.countDown();
                }
            }
        } else {
            LOGGER.error("HttpServletResponse not found or committed.");
        }
    }
}
