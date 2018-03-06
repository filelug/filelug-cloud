package org.clopuccino.server.servlet.websocket;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.dao.FileUploadGroupDao;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.ResponseFileUploadGroupModel;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.util.concurrent.CountDownLatch;

/**
 * <code>UploadFileWebSocketService</code> handles SID: UPLOAD_FILE_GROUP, to create a upload-file group.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class UploadFileGroupWebSocketService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(UploadFileGroupWebSocketService.class.getSimpleName());

    private final Session session;

    private final String message;

    private final ConnectSocket connectSocket;

    public UploadFileGroupWebSocketService(Session session, String message, ConnectSocket connectSocket) {
        this.session = session;
        this.message = message;
        this.connectSocket = connectSocket;
    }

    public void onUploadFileGroupWebSocket() {
        // Get the result of creating file-upload group information in desktop, and update result to db and then return response

        ObjectMapper mapper = Utility.createObjectMapper();

        Integer status = null;
        String uploadGroupId = null;
        Long createdInDesktopTimestamp = null;
        String createdInDesktopStatus = null;

        FileUploadGroupDao fileUploadGroupDao = connectSocket.getFileUploadGroupDao();

        HttpServletResponse resp = connectSocket.getResp();

        CountDownLatch closeLatch = connectSocket.getCloseLatch();

        try {
            ResponseFileUploadGroupModel responseModel = mapper.readValue(message, ResponseFileUploadGroupModel.class);

            status = responseModel.getStatus();
            uploadGroupId = responseModel.getUploadGroupId();
            createdInDesktopTimestamp = responseModel.getCreatedInDesktopTimestamp();
            createdInDesktopStatus = responseModel.getCreatedInDesktopStatus();

            if (status == null) {
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }

            if (uploadGroupId != null) {
                if (status.equals(HttpServletResponse.SC_OK)) {
                    if (createdInDesktopStatus == null || createdInDesktopStatus.trim().length() < 1) {
                        createdInDesktopStatus = DatabaseConstants.CREATED_IN_DESKTOP_STATUS_FAILURE;
                    }

                    if (createdInDesktopTimestamp == null || createdInDesktopTimestamp < 1) {
                        createdInDesktopTimestamp = System.currentTimeMillis();
                    }

                    fileUploadGroupDao.updateFileUploadGroupCreatedInDesktopStatus(uploadGroupId, createdInDesktopTimestamp, createdInDesktopStatus);
                } else {
                    // delete this file_upload_group and its related details
                    fileUploadGroupDao.deleteFileUploadGroupById(uploadGroupId);
                }
            }

            if (resp != null && !resp.isCommitted()) {
                if (HttpServletResponse.SC_OK == status && uploadGroupId != null) {
                    // desktop has saved the file successfully

                    try {
                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(status);
                        resp.getWriter().write(uploadGroupId);
                        resp.getWriter().flush();
                    } finally {
                        closeLatch.countDown();
                    }

                    LOGGER.debug(String.format("File upload summary created successfully. File upload group id: %s", uploadGroupId));
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
            String errorMessage = String.format("Error on processing response message of service 'create file upload summary'. Message from desktop: %s; error message: %s", message, e.getMessage());

            LOGGER.error(errorMessage);

            if (status == null) {
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }

            // update status if transfer key is not null

            if (uploadGroupId != null) {
                if (status.equals(HttpServletResponse.SC_OK)) {
                    if (createdInDesktopStatus == null || createdInDesktopStatus.trim().length() < 1) {
                        createdInDesktopStatus = DatabaseConstants.CREATED_IN_DESKTOP_STATUS_FAILURE;
                    }

                    if (createdInDesktopTimestamp == null || createdInDesktopTimestamp < 1) {
                        createdInDesktopTimestamp = System.currentTimeMillis();
                    }

                    fileUploadGroupDao.updateFileUploadGroupCreatedInDesktopStatus(uploadGroupId, createdInDesktopTimestamp, createdInDesktopStatus);
                } else {
                    // delete this file_upload_group and its related details
                    fileUploadGroupDao.deleteFileUploadGroupById(uploadGroupId);
                }
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
