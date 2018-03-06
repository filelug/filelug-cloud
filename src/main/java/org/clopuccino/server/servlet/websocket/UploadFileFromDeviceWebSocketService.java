package org.clopuccino.server.servlet.websocket;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.dao.FileUploadDao;
import org.clopuccino.dao.FileUploadGroupDao;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.*;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.service.sns.SnsMobilePushService;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * <code>UploadFileFromDeviceWebSocketService</code> handles SID: UPLOAD_FILE, UPLOAD_FILE2, and UPLOAD_FILE2_V2, to upload a file from device to desktop.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class UploadFileFromDeviceWebSocketService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(UploadFileFromDeviceWebSocketService.class.getSimpleName());

    private final Session session;

    private final String message;

    private final ConnectSocket connectSocket;

    public UploadFileFromDeviceWebSocketService(Session session, String message, ConnectSocket connectSocket) {
        this.session = session;
        this.message = message;
        this.connectSocket = connectSocket;
    }

    public void onFileUploadedToDesktop() {
        // response of requesting to upload file content(V1)

        HttpServletResponse resp = connectSocket.getResp();

        CountDownLatch closeLatch = connectSocket.getCloseLatch();

        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            ResponseModel responseModel = mapper.readValue(message, ResponseModel.class);

            Integer status = responseModel.getStatus();

            if (status != null && HttpServletResponse.SC_OK == status) {
                /* 表示 server 已經調用 DownloadFileServlet 傳送資料，因此不可調用 resp 回傳資訊 */
                LOGGER.info("Server is downloading file content. Waiting for the process to complete.");
            } else {
                if (resp != null) {
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
            String errorMessage = String.format("Error on processing response message of service 'uploadFile'. Message from desktop: %s; error message: %s", message, e.getMessage());

            LOGGER.error(errorMessage);

            try {
                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } catch (Exception e1) {
                    /* ignored */
            } finally {
                closeLatch.countDown();
            }
        }
    } // end onFileUploadedToDesktop(Session, String)

    public void onFileUploadedToDesktop2() {
        // Get the upload processing result, update result to db and then return response, service for UPLOAD_FILE2 and UPLOAD_FILE2_V2

        ObjectMapper mapper = Utility.createObjectMapper();

        String transferKey = null;
        String uploadStatus = null;
        String clientSessionId = null;

        ResponseFileUploadModel responseModel;
        try {
            responseModel = mapper.readValue(message, ResponseFileUploadModel.class);
        } catch (Exception e) {
            responseModel = null;

            LOGGER.error("Error on parsing response file upload model.\n" + message, e);
        }

        if (responseModel != null) {
            transferKey = responseModel.getTransferKey();
            uploadStatus = responseModel.getUploadStatus();
            clientSessionId = responseModel.getDeviceSessionId();

            if (uploadStatus != null && uploadStatus.equals(DatabaseConstants.TRANSFER_STATUS_FAILURE)) {
                // so when resume upload from device, do not have to upload file from device again.

                uploadStatus = DatabaseConstants.TRANSFER_STATUS_DEVICE_UPLOADED_BUT_UNCONFIRMED;
            } else if (uploadStatus == null || uploadStatus.trim().length() < 1) {
                uploadStatus = DatabaseConstants.TRANSFER_STATUS_FAILURE;
            }

            FileUploadDao fileUploadDao = connectSocket.getFileUploadDao();

            FileUploadGroupDao fileUploadGroupDao = connectSocket.getFileUploadGroupDao();

            // Make sure the file_uploaded is not deleted yet! If deleted, return failure for file_uploaded not found.

            final FileUpload fileUpload = fileUploadDao.findFileUploadForUploadKey(transferKey);

            if (fileUpload != null) {
                fileUploadDao.updateFileUploadStatus(transferKey, uploadStatus, System.currentTimeMillis());

                // delete tmp file if exists, under one of the following conditions meet:
                // (1) if the upload status is success
                // (2) if the request from device is version 2, instead of version 3.

                Long lastModifiedTimestamp = fileUpload.getSourceFileLastModifiedTimestamp();

                if (uploadStatus.equals(DatabaseConstants.TRANSFER_STATUS_SUCCESS) || lastModifiedTimestamp == null || lastModifiedTimestamp == 0L) {
                    String tmpFileAbsolutePath = fileUpload.getTmpFile();

                    Utility.deleteUploadTmpFileAndUpdateRecord(tmpFileAbsolutePath, fileUploadDao, transferKey);
                }

                final String finalTransferKey = transferKey;
                final String finalUploadStatus = uploadStatus;
                final String finalClientSessionId = clientSessionId;

                Utility.getExecutorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        String uploadGroupId = fileUpload.getUploadGroupId();
                        String filename = fileUpload.getFilename();

                        if (uploadGroupId == null) {
                            // notify device for upload file from old-version device app.
                            sendRemoteNotificationOnOneFileUploaded(finalTransferKey, finalUploadStatus, finalClientSessionId, filename);
                        } else {
                            // get notification type

                            FileUploadGroup fileUploadGroup = fileUploadGroupDao.findFileUploadGroupByUploadGroupId(uploadGroupId, true);

                            Integer notificationType = fileUploadGroup.getNotificationType();

                            if (notificationType != null) {
                                if (notificationType == 1) {
                                    // notify on each file
                                    sendRemoteNotificationOnOneFileUploaded(finalTransferKey, finalUploadStatus, finalClientSessionId, filename);
                                } else if (notificationType == 2) {
                                    // notify on alll files, or on file upload failure
                                    if (finalUploadStatus.equals(DatabaseConstants.TRANSFER_STATUS_FAILURE)
                                        || finalUploadStatus.equals(DatabaseConstants.TRANSFER_STATUS_DEVICE_UPLOADED_BUT_UNCONFIRMED)) {
                                        sendRemoteNotificationOnOneFileUploaded(finalTransferKey, DatabaseConstants.TRANSFER_STATUS_FAILURE, finalClientSessionId, filename);
                                    } else {
                                        // check if all files in the upload group uploaded successfully

                                        if (finalUploadStatus.equals(DatabaseConstants.TRANSFER_STATUS_SUCCESS)) {
                                            // check others uploads

                                            List<String> uploadKeys = fileUploadGroup.getUploadKeys();

                                            if (uploadKeys != null) {
                                                int filesOriginalCount = uploadKeys.size();

                                                if (filesOriginalCount > 0) {
                                                    uploadKeys.remove(finalTransferKey);

                                                    if (uploadKeys.size() > 0) {
                                                        boolean allSuccess = true;

                                                        for (String anotherUploadKey : uploadKeys) {
                                                            // check if the status is success, if not, mark and break

                                                            String anotherUploadStatus = fileUploadDao.findFileUploadStatusForUploadKey(anotherUploadKey);

                                                            // DEBUG
//                                                            LOGGER.debug("Another upload status is " + anotherUploadStatus);

                                                            if (anotherUploadStatus == null || !anotherUploadStatus.equals(DatabaseConstants.TRANSFER_STATUS_SUCCESS)) {
                                                                allSuccess = false;

                                                                break;
                                                            }
                                                        }

                                                        if (allSuccess) {
                                                            // DEBUG
//                                                            LOGGER.debug("All files uploads successfully.");

                                                            // notify all success
                                                            sendRemoteNotificationOnAllFilesUploadedSuccessfully(finalClientSessionId, filename, filesOriginalCount, uploadGroupId);
                                                        }
                                                    } else {
                                                        // if there's only one upload and uploaded successfully, send notification directly
                                                        sendRemoteNotificationOnOneFileUploaded(finalTransferKey, finalUploadStatus, finalClientSessionId, filename);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                });
            }
        }
    } // end onFileUploadedToDesktop2(Session, String)

    private void sendRemoteNotificationOnOneFileUploaded(final String uploadKey, final String uploadStatus, final String clientSessionId, final String filename) {
        if (clientSessionId != null) {
            String deviceToken = null;
            String userId = null;

            ClientSessionService clientSessionService = connectSocket.getClientSessionService();

            SnsMobilePushService pushService = connectSocket.getPushService();

            try {
                ClientSession clientSession = clientSessionService.findClientSessionBySessionId(clientSessionId);

                if (clientSession != null) {
                    deviceToken = clientSession.getDeviceToken();
                    userId = clientSession.getUserId();
                    String clientLocale = clientSession.getLocale();

                    // DEBUG
//                    LOGGER.info(String.format("[NOTIFICATION]User: %s, Device token: %s", userId, deviceToken));

                    if (deviceToken != null && deviceToken.trim().length() > 0 && userId != null) {
                        // FIX: Also make sure if the transfer key relates to some upload-group-id

//                        filename = fileUploadDao.findFileUploadFilenameForUploadKey(uploadKey);

                        // DEBUG
//                        LOGGER.info(String.format("[NOTIFICATION]Filename: %s", filename));

                        pushService.sendForOneFileUploaded(userId, deviceToken, clientLocale, filename, uploadKey, uploadStatus);
                    } else {
                        LOGGER.debug("Failed to send push notification because device token or user id is empty.\ndevice token: " + deviceToken + ", user id: " + userId);
                    }
                }
            } catch (Exception e) {
                // Catch this exception to prevent device not response.

                LOGGER.error(String.format("Failed to push notification for file upload result.\nuser: %s\ndevice token: %s\nfilename: %s\ntransfer key: %s\ntransfer status: %s", userId, deviceToken, filename, uploadKey, uploadStatus), e);
            }
        }
    }

    private void sendRemoteNotificationOnAllFilesUploadedSuccessfully(String clientSessionId, String filename, int filesCount, String uploadGroupId) {
        if (clientSessionId != null) {
            String deviceToken = null;
            String userId = null;

            ClientSessionService clientSessionService = connectSocket.getClientSessionService();

            SnsMobilePushService pushService = connectSocket.getPushService();

            try {
                ClientSession clientSession = clientSessionService.findClientSessionBySessionId(clientSessionId);

                if (clientSession != null) {
                    deviceToken = clientSession.getDeviceToken();
                    userId = clientSession.getUserId();
                    String clientLocale = clientSession.getLocale();

                    // DEBUG
//                    LOGGER.info(String.format("[NOTIFICATION]User: %s, Device token: %s", userId, deviceToken));

                    if (deviceToken != null && deviceToken.trim().length() > 0 && userId != null) {
                        // FIX: Also make sure if the transfer key relates to some upload-group-id

//                        filename = fileUploadDao.findFileUploadFilenameForUploadKey(uploadKey);

                        // DEBUG
//                        LOGGER.info(String.format("[NOTIFICATION]Filename: %s", filename));

                        pushService.sendForAllFilesUploadedSuccessfully(userId, deviceToken, clientLocale, filename, filesCount, uploadGroupId);
                    } else {
                        LOGGER.debug("Failed to send push notification on all files uploaded succefully because device token or user id is empty.\ndevice token: " + deviceToken + ", user id: " + userId);
                    }
                }
            } catch (Exception e) {
                // Catch this exception to prevent device not response.

                LOGGER.error(String.format("Failed to push notification on all files uploaded succefully.\nuser: %s\ndevice token: %s\nfilename: %s\nfiles count: %d", userId, deviceToken, filename, filesCount), e);
            }
        }
    }
}
