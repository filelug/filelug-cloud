package org.clopuccino.server.servlet.websocket;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.dao.FileDownloadGroupDao;
import org.clopuccino.domain.ResponseFileDownloadGroupModel;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.util.concurrent.CountDownLatch;

/**
 * <code>DownloadFileGroupWebSocketService</code> handles SID: DOWNLOAD_FILE_GROUP, to create a download-file group.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class DownloadFileGroupWebSocketService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DownloadFileGroupWebSocketService.class.getSimpleName());

    private final Session session;

    private final String message;

    private final ConnectSocket connectSocket;

    public DownloadFileGroupWebSocketService(Session session, String message, ConnectSocket connectSocket) {
        this.session = session;
        this.message = message;
        this.connectSocket = connectSocket;
    }

    public void onDownloadFileGroupWebSocket() {
        // Get the result of validating file paths of the file-download group information, and return response

        final FileDownloadGroupDao fileDownloadGroupDao = connectSocket.getFileDownloadGroupDao();

        final HttpServletResponse resp = connectSocket.getResp();

        final CountDownLatch closeLatch = connectSocket.getCloseLatch();

        ObjectMapper mapper = Utility.createObjectMapper();

        Integer status = null;
        String downloadGroupId = null;

        try {
            ResponseFileDownloadGroupModel responseModel = mapper.readValue(message, ResponseFileDownloadGroupModel.class);

            status = responseModel.getStatus();
            downloadGroupId = responseModel.getDownloadGroupId();

            if (status == null) {
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }

            if (!status.equals(HttpServletResponse.SC_OK) && downloadGroupId != null) {
                // delete this file_download_group and its related details

                fileDownloadGroupDao.deleteFileDownloadGroupById(downloadGroupId);
            }

            if (resp != null && !resp.isCommitted()) {
                if (HttpServletResponse.SC_OK == status && downloadGroupId != null) {
                    // desktop has saved the file successfully

                    try {
                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(status);
                        resp.getWriter().write(downloadGroupId);
                        resp.getWriter().flush();
                    } finally {
                        closeLatch.countDown();
                    }

                    LOGGER.debug(String.format("File download summary created successfully. File download group id: %s", downloadGroupId));
                } else {
                    try {
                        String error = responseModel.getError();

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(status);
                        resp.getWriter().write(error != null ? error : "");
                        resp.getWriter().flush();
                    } finally {
                        closeLatch.countDown();
                    }
                }
            }
        } catch (Exception e) {
            String errorMessage = String.format("Error on processing response message of service 'create file download summary'. Message from desktop: %s; error message: %s", message, e.getMessage());

            LOGGER.error(errorMessage);

            if (status == null) {
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }

            // update status if transfer key is not null

            if (!status.equals(HttpServletResponse.SC_OK) && downloadGroupId != null) {
                // delete this file_download_group and its related details

                fileDownloadGroupDao.deleteFileDownloadGroupById(downloadGroupId);
            }

            try {
                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(status);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } catch (Exception e1) {
                // ignored
            } finally {
                closeLatch.countDown();
            }
        }
    }
}
