package org.clopuccino.server.servlet.websocket;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.dao.FileDownloadDao;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.ResponseFileDownloadModel;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.util.concurrent.CountDownLatch;

/**
 * <code>DownloadFileToDeviceWebSocketService</code> handles SID: DOWNLOAD_FILE, DOWNLOAD_FILE2, and DOWNLOAD_FILE2_V2, to download a file from desktop to device.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class DownloadFileToDeviceWebSocketService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DownloadFileToDeviceWebSocketService.class.getSimpleName());

    private final Session session;

    private final String message;

    private final ConnectSocket connectSocket;

    public DownloadFileToDeviceWebSocketService(Session session, String message, ConnectSocket connectSocket) {
        this.session = session;
        this.message = message;
        this.connectSocket = connectSocket;
    }

    public void onStartTransferingFileToDevice() {
        // requested to download file content (V1)

        final FileDownloadDao fileDownloadDao = connectSocket.getFileDownloadDao();

        final HttpServletResponse resp = connectSocket.getResp();

        final CountDownLatch closeLatch = connectSocket.getCloseLatch();

        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            final ResponseFileDownloadModel responseModel = mapper.readValue(message, ResponseFileDownloadModel.class);

            Integer status = responseModel.getStatus();

            final String transferKey = responseModel.getTransferKey();

            // 儲存 file size with download key(download key is the value of client session id)
            // 成功（200）就更新 file size
            // 不成功（200以外）就更新status為failure

            if (status != null && HttpServletResponse.SC_OK == status) {
                Utility.getExecutorService().execute(() -> fileDownloadDao.updateFileDownloadSize(transferKey, responseModel.getFileSize()));

                /* 表示 server 已經調用 UploadFileServlet 傳送資料，因此不可調用 resp 回傳資訊 */
                LOGGER.debug("Desktop is uploading file content. Waiting for the process to complete.");
            } else {
                Utility.getExecutorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        fileDownloadDao.updateFileDownloadStatus(transferKey, DatabaseConstants.TRANSFER_STATUS_FAILURE, System.currentTimeMillis());
                    }
                });

                if (resp != null && !resp.isCommitted()) {
                    try {
                        String error = responseModel.getError();

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().write(error);
                        resp.getWriter().flush();
                    } finally {
                        closeLatch.countDown();
                    }
                }
            }
        } catch (Exception e) {
            String errorMessage = String.format("Error on processing response message of service 'downloadFile'. Message from desktop: %s; error message: %s", message, e.getMessage());

            LOGGER.error(errorMessage);

            try {
                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } catch (Exception e1) {
                // ignored
            } finally {
                closeLatch.countDown();
            }
        }
    }

    public void onStartTransferingFileToDevice2() {
        // requested to download file content (V2 and V3), consider partial content

        final FileDownloadDao fileDownloadDao = connectSocket.getFileDownloadDao();

        final HttpServletResponse resp = connectSocket.getResp();

        final CountDownLatch closeLatch = connectSocket.getCloseLatch();

        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            final ResponseFileDownloadModel responseModel = mapper.readValue(message, ResponseFileDownloadModel.class);

            Integer status = responseModel.getStatus();

            final String transferKey = responseModel.getTransferKey();

            // 儲存 file size with download key(download key is the value of client session id)
            // 成功（200 or 206）就更新 file size
            // 不成功（200 與 206 以外）就更新status為failure

            if (status != null && (HttpServletResponse.SC_OK == status || HttpServletResponse.SC_PARTIAL_CONTENT == status)) {
                Utility.getExecutorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        fileDownloadDao.updateFileDownloadSize(transferKey, responseModel.getFileSize());
                    }
                });

                // 表示 server 已經調用 UploadFileServlet 傳送資料，因此不可調用 resp 回傳資訊
                LOGGER.debug("Desktop is uploading file content. Waiting for the process to complete.");
            } else {
                Utility.getExecutorService().execute(() -> fileDownloadDao.updateFileDownloadStatus(transferKey, DatabaseConstants.TRANSFER_STATUS_FAILURE, System.currentTimeMillis()));

                if (resp != null && !resp.isCommitted()) {
                    try {
                        String error = responseModel.getError();

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().write(error);
                        resp.getWriter().flush();
                    } finally {
                        closeLatch.countDown();
                    }
                }
            }
        } catch (Exception e) {
            String errorMessage = String.format("Error on processing response message of service 'downloadFile(V2)'. Message from desktop: %s; error message: %s", message, e.getMessage());

            LOGGER.error(errorMessage);

            try {
                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
