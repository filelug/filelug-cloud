package org.clopuccino.server.servlet.websocket;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.domain.*;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * <code>ListDirectoryChildrenWebSocketService</code> handles SID: LIST_CHILDREN, to get children of the specified directory.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ListDirectoryChildrenWebSocketService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ListDirectoryChildrenWebSocketService.class.getSimpleName());

    private final Session session;

    private final String message;

    private final ConnectSocket connectSocket;

    public ListDirectoryChildrenWebSocketService(Session session, String message, ConnectSocket connectSocket) {
        this.session = session;
        this.message = message;
        this.connectSocket = connectSocket;
    }

    public void onListDirectoryChildrenWebSocket() {
        // receive message from server with directory children information

        HttpServletResponse resp = connectSocket.getHttpServletResponse();

        if (resp != null && !resp.isCommitted()) {
            try {
                ObjectMapper mapper = Utility.createObjectMapper();

                ResponseListDirectoryChildrenModel responseModel = mapper.readValue(message, ResponseListDirectoryChildrenModel.class);

                Integer status = responseModel.getStatus();

                if (status != null && HttpServletResponse.SC_OK == status) {
                    List<HierarchicalModel> children = responseModel.getChildren();

                    if (children == null) {
                        children = new ArrayList<>();
                    }

                    String rootsJson = mapper.writeValueAsString(children);

                    /* DEBUG */
                    LOGGER.debug(String.format("Prepared directory children to response for service listDirectoryChildren: %s", rootsJson));

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
                String errorMessage = String.format("Error on processing response message of service 'listDirectoryChildren'. Message from desktop: %s; error message: %s", message, e.getMessage());

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
