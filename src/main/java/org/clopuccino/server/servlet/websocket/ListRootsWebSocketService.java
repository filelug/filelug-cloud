package org.clopuccino.server.servlet.websocket;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.domain.ResponseListRootsModel;
import org.clopuccino.domain.ResponseRootDirectoryArrayModel;
import org.clopuccino.domain.RootDirectory;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * <code>ListRootsWebSocketService</code> handles services to get root directories.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ListRootsWebSocketService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ListRootsWebSocketService.class.getSimpleName());

    private final Session session;

    private final String message;

    private final ConnectSocket connectSocket;

    public ListRootsWebSocketService(Session session, String message, ConnectSocket connectSocket) {
        this.session = session;
        this.message = message;
        this.connectSocket = connectSocket;
    }

    public void onListAllRootDirectoriesWebSocket() {
        // receive message from server with rootDirectories information

        HttpServletResponse resp = connectSocket.getHttpServletResponse();

        if (resp != null && !resp.isCommitted()) {
            try {
                ObjectMapper mapper = Utility.createObjectMapper();

                ResponseRootDirectoryArrayModel responseModel = mapper.readValue(message, ResponseRootDirectoryArrayModel.class);

                Integer status = responseModel.getStatus();

                if (status != null && HttpServletResponse.SC_OK == status) {
                    List<RootDirectory> rootDirectories = responseModel.getRootDirectories();

                    if (rootDirectories == null) {
                        rootDirectories = new ArrayList<>();
                    }

                    String rootDirectoriesJson = mapper.writeValueAsString(rootDirectories);

//                    // DEBUG
//                    LOGGER.info(String.format("Prepared root directories to response for service findAllRootDirectories: %s", rootDirectoriesJson));

                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                    resp.setStatus(status);
                    resp.getWriter().write(rootDirectoriesJson);
                    resp.getWriter().flush();
                } else {
                    String error = responseModel.getError();

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(error);
                    resp.getWriter().flush();
                }
            } catch (Exception e) {
                String errorMessage = String.format("Error on processing response message of service 'findAllRootDirectories'. Message from desktop: %s; error message: %s", message, e.getMessage());

                LOGGER.error(errorMessage);

                try {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } catch (Exception e1) {
                    // ignored
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

    public void onListAllRootsWebSocket() {
        // receive message from server with file roots information

        HttpServletResponse resp = connectSocket.getHttpServletResponse();

        if (resp != null) {
            try {
                ObjectMapper mapper = Utility.createObjectMapper();

                ResponseListRootsModel responseModel = mapper.readValue(message, ResponseListRootsModel.class);

                Integer status = responseModel.getStatus();

                if (status != null && HttpServletResponse.SC_OK == status) {
                    List<RootDirectory> roots = responseModel.getRoots();

                    if (roots == null) {
                        roots = new ArrayList<>();
                    }

                    String rootsJson = mapper.writeValueAsString(roots);

//                    // DEBUG
//                    LOGGER.info(String.format("Prepared file roots to response for service listRoots: %s", rootsJson));

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
                String errorMessage = String.format("Error on processing response message of service 'listRoots'. Message from desktop: %s; error message: %s", message, e.getMessage());

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
            LOGGER.error("HttpServletResponse must be assigned when initiate ConnectSocket!");
        }
    }
}
