package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.*;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.*;
import org.clopuccino.server.servlet.websocket.ConnectSocket;
import org.clopuccino.server.servlet.Sid;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.service.HttpFileByteRange;
import org.clopuccino.service.NetworkService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.io.*;
import java.util.Enumeration;
import java.util.concurrent.TimeoutException;

/**
 * <code>UploadFileServlet4</code> supports resumable file uploads.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "upload-file-from-device4", asyncSupported = true, displayName = "upload-file-from-device4", description = "Upload file from device(V4)", urlPatterns = {"/directory/dupload4"})
public class UploadFileServlet4 extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(UploadFileServlet4.class.getSimpleName());

    private static final long serialVersionUID = -2082328244124126422L;

    private final FileUploadDao fileUploadDao;

    private final UserDao userDao;

    private final ClientSessionService clientSessionService;

    private final UserComputerDao userComputerDao;

    private final ComputerDao computerDao;

    private final FileUploadGroupDao fileUploadGroupDao;

    private final NetworkService networkService;

    private final UserComputerPropertiesDao userComputerPropertiesDao;

    public UploadFileServlet4() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        fileUploadDao = new FileUploadDao(dbAccess);

        userDao = new UserDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        fileUploadGroupDao = new FileUploadGroupDao(dbAccess);

        networkService = new NetworkService();

        userComputerPropertiesDao = new UserComputerPropertiesDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        internalDoPost(req, resp, false, LOGGER);
    }

    protected void internalDoPost(HttpServletRequest req, HttpServletResponse resp, boolean isV3, Logger logger) throws ServletException, IOException {
        try {

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Request Upload Headers(%s):", (isV3 ? "V3" : "V4")));
                Enumeration<String> headerNames = req.getHeaderNames();
                for (; headerNames.hasMoreElements(); ) {
                    String name = headerNames.nextElement();
                    String value = req.getHeader(name);
                    logger.debug(name + ": " + value);
                }
            }
                /* upload key -- already a base64 string */
            final String uploadKey = req.getHeader(Constants.HTTP_HEADER_NAME_UPLOAD_KEY);

                /* upload directory */
            String encodedUploadDirectory = req.getHeader(Constants.HTTP_HEADER_NAME_UPLOAD_DIRECTORY);

                /* upload file name */
            String encodedUploadFilename = req.getHeader(Constants.HTTP_HEADER_NAME_UPLOAD_FILE_NAME);

                /* size in byte */
            String uploadFileSize = req.getHeader(Constants.HTTP_HEADER_NAME_UPLOAD_FILE_SIZE);

            // file range, for resume upload only
            String fileRange = req.getHeader(Constants.HTTP_HEADER_NAME_FILE_RANGE);

            boolean isUploadedButUnconfirmed = checkIfRequestUploadedButUnconfirmed(req);

            // file last modified, for resume upload only
            // If the request did not have a header of the specified name, this method returns -1.
            // If the header can't be converted to a date, the method throws an IllegalArgumentException
            long fileLastModifiedInMillis = 0;

            String fileLastModifiedString = req.getHeader(Constants.HTTP_HEADER_NAME_FILE_LAST_MODIFIED);

            if (fileLastModifiedString != null) {
                try {
                    fileLastModifiedInMillis = Long.parseLong(fileLastModifiedString);
                } catch (Exception e) {
                    // ignored here, process it below
                }
            }

            // validate client session id

            String clientSessionId = req.getHeader(Constants.HTTP_HEADER_FILELUG_SESSION_ID_NAME);

            if (clientSessionId == null || clientSessionId.trim().length() < 1) {
                clientSessionId = req.getHeader(Constants.HTTP_HEADER_AUTHORIZATION_NAME);
            }

            if (clientSessionId == null || clientSessionId.trim().length() < 1) {
                // session not provided

                String errorMessage = ClopuccinoMessages.getMessage("session.not.provided");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                ClientSession clientSession = clientSessionService.findClientSessionBySessionId(clientSessionId);

                if (clientSession == null) {
                    // session not provided

                    String errorMessage = ClopuccinoMessages.getMessage("session.not.exists");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else if (fileRange == null && clientSession.checkTimeout()) {
                    // invalid session for non-resume upload

                    String errorMessage = ClopuccinoMessages.getMessage("invalid.session");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    // update last access timestamp ONLY for session that is NOT timeout

                    if (!clientSession.checkTimeout()) {
                        clientSessionService.updateClientSessionLastAccessTimestamp(clientSessionId, System.currentTimeMillis());
                    }

                    if (uploadKey == null || uploadKey.trim().length() < 1) {
                        String errorMessage = ClopuccinoMessages.getMessage("param.null.or.empty", "upload/download key");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else if (encodedUploadDirectory == null || encodedUploadDirectory.trim().length() < 1) {
                        String errorMessage = ClopuccinoMessages.getMessage("empty.upload.directory");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else if (encodedUploadFilename == null || encodedUploadFilename.trim().length() < 1) {
                        String errorMessage = ClopuccinoMessages.getMessage("empty.upload.filename");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else if (uploadFileSize == null || uploadFileSize.trim().length() < 1 || Utility.positiveLongFromString(uploadFileSize) == -1) {
                        final String userId = clientSession.getUserId();

                        logger.error("User '" + userId + "' is interrupting file uploading data by changing the upload file size to: " + uploadFileSize);

                        String errorMessage = ClopuccinoMessages.getMessage("illegal.file.size", uploadFileSize);

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else if (fileLastModifiedInMillis < 1) {
                        String errorMessage = ClopuccinoMessages.getMessage("illegal.file.lastModified", fileLastModifiedString);

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else if (fileRange != null && !HttpFileByteRange.validFileRange(fileRange)) {
                        String errorMessage = ClopuccinoMessages.getMessage("illegal.file.range", fileRange);

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else {
                        final String userId = clientSession.getUserId();

                        final Long computerId = clientSession.getComputerId();

                        String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

                        String clientLocale = clientSession.getLocale();

                        final String uploadDirectory = Utility.decodeBase64String(encodedUploadDirectory, Constants.BASE64_CONVERSION_CHARSET);
                        final String uploadFilename = Utility.decodeBase64String(encodedUploadFilename, Constants.BASE64_CONVERSION_CHARSET);
                        final long newFileSize = Utility.positiveLongFromString(uploadFileSize);

                        long availableTransferBytes = Constants.FAKE_AVAILABLE_BYTES_FOR_UNLIMITED_TRANSFER_USER;

                        if (availableTransferBytes < newFileSize) {
                            String fileSizeForRepresentation = Utility.representationFileSizeFromBytes(availableTransferBytes);
                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "not.enough.transfer.bytes", fileSizeForRepresentation);

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            resp.getWriter().write(errorMessage);
                            resp.getWriter().flush();
                        } else {
                            // Check the upload file size limit here.

                            long uploadFileSizeLimitInBytes = userDao.findUploadFileSizeLimitInBytes(userId);

                            long uploadFileSizeLong = Long.parseLong(uploadFileSize);

                            if (uploadFileSizeLimitInBytes < uploadFileSizeLong) {
                                String errorMessage = ClopuccinoMessages.getMessage("exceed.upload.size.limit", Utility.representationFileSizeFromBytes(uploadFileSizeLong));

                                logger.error("Uploading file size limit exceeded for user: " + userId + "\n" + errorMessage);

                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.setStatus(Constants.HTTP_STATUS_FILE_SIZE_LIMIT_EXCEEDED);
                                resp.getWriter().write(errorMessage);
                                resp.getWriter().flush();
                            } else {
                                // No matter File-Range exists or not, if oldFileUpload exists and the status is success, response status of 409.
                                // If header File-Range exists, if oldFileUpload not exists, create one; if oldFileUpload exists (status != success), update it.
                                // If header File-Range not exists, but oldFileUpload exists (status != success), update it.

                                FileUpload oldFileUpload = fileUploadDao.findFileUploadForUploadKey(uploadKey);

                                final Computer computer = computerDao.findComputerById(computerId);

                                if (computer == null) {
                                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "computer.not.found");

                                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                    resp.setStatus(Constants.HTTP_STATUS_COMPUTER_NOT_FOUND);
                                    resp.getWriter().write(errorMessage);
                                    resp.getWriter().flush();
                                } else {
                                    // validate desktop version

                                    String desktopVersion = userComputerPropertiesDao.findPropertyValue(userComputerId, org.clopuccino.PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);
//                                    // find the owner of the computer
//                                    String computerOwner = computerDao.findComputerOwnerById(computerId);
//
//                                    String ownerUserComputerId;
//
//                                    if (computerOwner != null && computerOwner.trim().length() > 0) {
//                                        ownerUserComputerId = Utility.generateUserComputerIdFrom(computerOwner, computerId);
//                                    } else {
//                                        ownerUserComputerId = userComputerId;
//                                    }
//
//                                    String desktopVersion = userComputerPropertiesDao.findPropertyValue(ownerUserComputerId, org.clopuccino.PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);

                                    if (desktopVersion != null && Version.valid(desktopVersion)
                                        && isV3
                                        && new Version(desktopVersion).compareTo(new Version(Constants.INITIAL_VERSION_TO_V2)) >= 0) {

                                        // Incompatible with version of desktop that is equal to or larger than 2.0.0

                                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                        resp.setStatus(Constants.HTTP_STATUS_DEVICE_VERSION_TOO_OLD);
                                        resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "device.need.update", computer.getComputerName()));
                                        resp.getWriter().flush();
                                    } else {
                                        boolean requestDesktopToDownload = false;

                                        if (oldFileUpload != null) {
                                            // oldFileUpload exists and the status is NOT success

                                            if (isUploadedButUnconfirmed) {
                                                // Make sure the followings or response 400:
                                                // 1) header「File-Last-Modified」== file_uploaded.source_file_last_modified
                                                // 2) file_uploaded.status == DatabaseConstants.TRANSFER_STATUS_DEVICE_UPLOADED_BUT_UNCONFIRMED
                                                // 3) header「fileSize」== file_uploaded.file_size
                                                // 4) header「updir」== file_uploaded.directory
                                                // 5) header 「upname」== file_uploaded.filename

                                                if (!validateFileLastModifiedDate(oldFileUpload.getSourceFileLastModifiedTimestamp(), fileLastModifiedInMillis)) {
                                                    String inconsistantData = ClopuccinoMessages.localizedMessage(clientLocale, "fileLastModifiedDate");

                                                    responseWithInconsistantDataName(inconsistantData, resp, clientLocale);
                                                } else if (oldFileUpload.getStatus() != null && !oldFileUpload.getStatus().equals(DatabaseConstants.TRANSFER_STATUS_DEVICE_UPLOADED_BUT_UNCONFIRMED)) {
                                                    String inconsistantData = ClopuccinoMessages.localizedMessage(clientLocale, "status");

                                                    responseWithInconsistantDataName(inconsistantData, resp, clientLocale);
                                                } else if (!validateFileSize(oldFileUpload.getFileSize(), uploadFileSizeLong)) {
                                                    String inconsistantData = ClopuccinoMessages.localizedMessage(clientLocale, "fileSize");

                                                    responseWithInconsistantDataName(inconsistantData, resp, clientLocale);
                                                } else if (!validateFileUploadDirectory(oldFileUpload.getDirectory(), uploadDirectory)) {
                                                    String inconsistantData = ClopuccinoMessages.localizedMessage(clientLocale, "fileUploadDirectory");

                                                    responseWithInconsistantDataName(inconsistantData, resp, clientLocale);
                                                } else if (!validateFilename(oldFileUpload.getFilename(), uploadFilename)) {
                                                    String inconsistantData = ClopuccinoMessages.localizedMessage(clientLocale, "filename");

                                                    responseWithInconsistantDataName(inconsistantData, resp, clientLocale);
                                                } else {
                                                    requestDesktopToDownload = true;
                                                }
                                            } else if (fileRange == null || HttpFileByteRange.isFileByteRangeFromStart(fileRange)) {
                                                // upload from start

                                                // create tmp file and update table before writing data in, oldFileUpload != null

                                                File tmpFile = File.createTempFile(Constants.TMP_UPLOAD_FILE_PREFIX, Utility.genereateTmpFileSuffix(uploadFilename));

                                                long startTimestamp = System.currentTimeMillis();

                                                String fromIp = networkService.getClientIpAddress(req);

                                                String fromHost = networkService.getClientHostname(req);

                                                fileUploadDao.updateWithNewTmpFile(uploadKey, tmpFile.getAbsolutePath(), uploadFileSizeLong, startTimestamp, DatabaseConstants.TRANSFER_STATUS_PROCESSING, fileLastModifiedInMillis, fromIp, fromHost);

                                                // writing data to tmp file

                                                try (OutputStream outputStream = new FileOutputStream(tmpFile)) {
                                                    ByteStreams.copy(req.getInputStream(), outputStream);

                                                    // DEBUG:
//                                                logger.info("[Old exists, No Resume] Total bytes copied: " + bytesCopied);

                                                    // save upload record to db, including upload group id, if any -- only after tmp file saved successfully.

                                                    fileUploadDao.updateTmpFileWrittenResult(uploadKey, DatabaseConstants.TRANSFER_STATUS_DEVICE_UPLOADED_BUT_UNCONFIRMED, (uploadFileSizeLong - 1));

                                                    requestDesktopToDownload = true;

                                                    logger.debug("File copied to \"" + tmpFile.getAbsolutePath() + "\" successfully.");
                                                } catch (Exception e) {
                                                    long writtenSize = tmpFile.length();

                                                    fileUploadDao.updateTmpFileWrittenResult(uploadKey, DatabaseConstants.TRANSFER_STATUS_FAILURE, (writtenSize - 1));

                                                    String logMessage;

                                                    Throwable cause = e.getCause();

                                                    if ((cause != null && TimeoutException.class.isInstance(cause)) || EOFException.class.isInstance(e)) {
                                                        logMessage = "Timeout to upload file: " + uploadFilename + "\" to tmp: " + tmpFile.getAbsolutePath() + "\nupload key: " + uploadKey + "\nBytes written to tmp file: " + writtenSize;
                                                    } else {
                                                        logMessage = "Failed to copy file \"" + uploadFilename + "\" to tmp: " + tmpFile.getAbsolutePath() + "\nupload key: " + uploadKey + "\nBytes written to tmp file: " + writtenSize;
                                                    }

                                                    String exceptionMessage = e.getMessage();

                                                    if (exceptionMessage != null && exceptionMessage.toLowerCase().contains("early eof")) {
                                                        logger.warn("Client may stop uploading file. '" + exceptionMessage + "'");
                                                    } else {
                                                        logger.error(logMessage, e);
                                                    }

                                                    HttpFileByteRange httpFileByteRange;

                                                    if (writtenSize > 1) {
                                                        httpFileByteRange = new HttpFileByteRange(0, (writtenSize - 1));
                                                    } else {
                                                        httpFileByteRange = new HttpFileByteRange(0);
                                                    }

                                                    resp.setHeader(Constants.HTTP_HEADER_NAME_FILE_RANGE, httpFileByteRange.toString());

                                                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "failed.upload.file.try.later", uploadFilename);

                                                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                                    resp.getWriter().write(errorMessage);
                                                    resp.getWriter().flush();
                                                }
                                            } else {
                                                // resume upload

                                                // Do not use HttpFileByteRange.parseContentRange(String), which throws exception when no end value!
                                                HttpFileByteRange httpFileByteRange = HttpFileByteRange.parse(fileRange);

                                                long startIndexInRequest = httpFileByteRange.getStart();

                                                String tmpFilePath = oldFileUpload.getTmpFile();

                                                // Make sure the followings or response 400:
                                                // 1) header「File-Last-Modified」== file_uploaded.source_file_last_modified
                                                // 2) header「File-Range」== "bytes=" + file_uploaded.transferred_byte_index + "-"
                                                // 3) header「fileSize」== file_uploaded.file_size
                                                // 4) header「updir」== file_uploaded.directory
                                                // 5) header 「upname」== file_uploaded.filename

                                                if (!validateFileLastModifiedDate(oldFileUpload.getSourceFileLastModifiedTimestamp(), fileLastModifiedInMillis)) {
                                                    String inconsistantData = ClopuccinoMessages.localizedMessage(clientLocale, "fileLastModifiedDate");

                                                    responseWithInconsistantDataName(inconsistantData, resp, clientLocale);
                                                } else if (!validateFileRange(tmpFilePath, oldFileUpload.getTransferredByteIndex(), startIndexInRequest)) {
                                                    String inconsistantData = ClopuccinoMessages.localizedMessage(clientLocale, "fileRange");

                                                    responseWithInconsistantDataName(inconsistantData, resp, clientLocale);
                                                } else if (!validateFileSize(oldFileUpload.getFileSize(), uploadFileSizeLong)) {
                                                    String inconsistantData = ClopuccinoMessages.localizedMessage(clientLocale, "fileSize");

                                                    responseWithInconsistantDataName(inconsistantData, resp, clientLocale);
                                                } else if (!validateFileUploadDirectory(oldFileUpload.getDirectory(), uploadDirectory)) {
                                                    String inconsistantData = ClopuccinoMessages.localizedMessage(clientLocale, "fileUploadDirectory");

                                                    responseWithInconsistantDataName(inconsistantData, resp, clientLocale);
                                                } else if (!validateFilename(oldFileUpload.getFilename(), uploadFilename)) {
                                                    String inconsistantData = ClopuccinoMessages.localizedMessage(clientLocale, "filename");

                                                    responseWithInconsistantDataName(inconsistantData, resp, clientLocale);
                                                } else {
                                                    long startTimestamp = System.currentTimeMillis();

                                                    String fromIp = networkService.getClientIpAddress(req);

                                                    String fromHost = networkService.getClientHostname(req);

                                                    fileUploadDao.updateWithExistingTmpFile(uploadKey, startTimestamp, DatabaseConstants.TRANSFER_STATUS_PROCESSING, fromIp, fromHost);

                                                    // writing data to tmp file

                                                    try (OutputStream outputStream = new FileOutputStream(new File(tmpFilePath), true)) { // append content to the end of file
                                                        long bytesCopied = ByteStreams.copy(req.getInputStream(), outputStream);

                                                        // DEBUG:
//                                                    logger.info("[Old exists, Resumed] total bytes copied: " + bytesCopied);

                                                        // save upload record to db, including upload group id, if any -- only after tmp file saved successfully.

                                                        fileUploadDao.updateTmpFileWrittenResult(uploadKey, DatabaseConstants.TRANSFER_STATUS_DEVICE_UPLOADED_BUT_UNCONFIRMED, (uploadFileSizeLong - 1));

                                                        requestDesktopToDownload = true;

                                                        logger.debug("File copied to \"" + tmpFilePath + "\" successfully.");
                                                    } catch (Exception e) {
                                                        long writtenSize = new File(tmpFilePath).length();

                                                        fileUploadDao.updateTmpFileWrittenResult(uploadKey, DatabaseConstants.TRANSFER_STATUS_FAILURE, (writtenSize - 1));

                                                        String logMessage;

                                                        Throwable cause = e.getCause();

                                                        if ((cause != null && TimeoutException.class.isInstance(cause)) || EOFException.class.isInstance(e)) {
                                                            logMessage = "Timeout to upload file: " + uploadFilename + "\" to tmp: " + tmpFilePath + "\nupload key: " + uploadKey + "\nBytes written to tmp file: " + writtenSize;
                                                        } else {
                                                            logMessage = "Failed to copy file \"" + uploadFilename + "\" to tmp: " + tmpFilePath + "\nupload key: " + uploadKey + "\nBytes written to tmp file: " + writtenSize;
                                                        }

                                                        String exceptionMessage = e.getMessage();

                                                        if (exceptionMessage != null && exceptionMessage.toLowerCase().contains("early eof")) {
                                                            logger.warn("Client may stop uploading file. '" + exceptionMessage + "'");
                                                        } else {
                                                            logger.error(logMessage, e);
                                                        }

                                                        HttpFileByteRange responseFileByteRange;

                                                        if (writtenSize > 1) {
                                                            responseFileByteRange = new HttpFileByteRange(0, (writtenSize - 1));
                                                        } else {
                                                            responseFileByteRange = new HttpFileByteRange(0);
                                                        }

                                                        resp.setHeader(Constants.HTTP_HEADER_NAME_FILE_RANGE, responseFileByteRange.toString());

                                                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "failed.upload.file.try.later", uploadFilename);

                                                        resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                                        resp.getWriter().write(errorMessage);
                                                        resp.getWriter().flush();
                                                    }
                                                }
                                            }
                                        } else {
                                            // oldFileUpload NOT exists

                                            if (fileRange == null || HttpFileByteRange.isFileByteRangeFromStart(fileRange)) {
                                                // Upload from start.
                                                // Creates file_uploaded when file written to the tmp file, successfully or failed.

                                                // Check if upload key belongs to some upload group
                                                String uploadGroupId = fileUploadGroupDao.findFileUploadGroupIdByUploadKey(uploadKey);

                                                // create tmp file
                                                File tmpFile = File.createTempFile(Constants.TMP_UPLOAD_FILE_PREFIX, Utility.genereateTmpFileSuffix(uploadFilename));

                                                long startTimestamp = System.currentTimeMillis();

                                                String fromIp = networkService.getClientIpAddress(req);

                                                String fromHost = networkService.getClientHostname(req);

                                                try (OutputStream outputStream = new FileOutputStream(tmpFile)) {
                                                    long bytesCopied = ByteStreams.copy(req.getInputStream(), outputStream);

                                                    // DEBUG:
//                                                logger.info("[No FileUpload, No Resume] Total bytes copied: " + bytesCopied);

                                                    // save upload record to db, including upload group id, if any -- only after tmp file saved successfully.

                                                    fileUploadDao.createFileUploaded(uploadKey, userId, computerId, computer.getGroupName(), computer.getComputerName(), uploadDirectory, uploadFilename, newFileSize, uploadGroupId, startTimestamp, DatabaseConstants.TRANSFER_STATUS_DEVICE_UPLOADED_BUT_UNCONFIRMED, tmpFile.getAbsolutePath(), startTimestamp, (uploadFileSizeLong - 1), fileLastModifiedInMillis, fromIp, fromHost);

                                                    requestDesktopToDownload = true;

                                                    logger.debug("File copied to \"" + tmpFile.getAbsolutePath() + "\" successfully.");
                                                } catch (Exception e) {
                                                    long writtenSize = tmpFile.length();

                                                    fileUploadDao.createFileUploaded(uploadKey, userId, computerId, computer.getGroupName(), computer.getComputerName(), uploadDirectory, uploadFilename, newFileSize, uploadGroupId, startTimestamp, DatabaseConstants.TRANSFER_STATUS_FAILURE, tmpFile.getAbsolutePath(), startTimestamp, (writtenSize - 1), fileLastModifiedInMillis, fromIp, fromHost);

                                                    String logMessage;

                                                    Throwable cause = e.getCause();

                                                    if ((cause != null && TimeoutException.class.isInstance(cause)) || EOFException.class.isInstance(e)) {
                                                        logMessage = "Timeout to upload file: " + uploadFilename + "\" to tmp: " + tmpFile.getAbsolutePath() + "\nupload key: " + uploadKey + "\nBytes written to tmp file: " + writtenSize;
                                                    } else {
                                                        logMessage = "Failed to copy file \"" + uploadFilename + "\" to tmp: " + tmpFile.getAbsolutePath() + "\nupload key: " + uploadKey + "\nBytes written to tmp file: " + writtenSize;
                                                    }

                                                    String exceptionMessage = e.getMessage();

                                                    if (exceptionMessage != null && exceptionMessage.toLowerCase().contains("early eof")) {
                                                        logger.warn("Client may stop uploading file. '" + exceptionMessage + "'");
                                                    } else {
                                                        logger.error(logMessage, e);
                                                    }

                                                    HttpFileByteRange httpFileByteRange;

                                                    if (writtenSize > 1) {
                                                        httpFileByteRange = new HttpFileByteRange(0, (writtenSize - 1));
                                                    } else {
                                                        httpFileByteRange = new HttpFileByteRange(0);
                                                    }

                                                    resp.setHeader(Constants.HTTP_HEADER_NAME_FILE_RANGE, httpFileByteRange.toString());

                                                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "failed.upload.file.try.later", uploadFilename);

                                                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                                    resp.getWriter().write(errorMessage);
                                                    resp.getWriter().flush();
                                                }
                                            } else {
                                                // When file_uploaded not found for the upload key, the file can only upload from start

                                                String inconsistantData = ClopuccinoMessages.localizedMessage(clientLocale, "fileRange");

                                                responseWithInconsistantDataName(inconsistantData, resp, clientLocale);
                                            }
                                        }

                                        if (requestDesktopToDownload) {
                                            // get socket by user and validate it
                                            ConnectSocket socket = ConnectSocket.getInstance(userComputerId);

                                            if (socket != null && socket.validate(true)) {
                                                // send message to server

                                                int sid = isV3 ? Sid.UPLOAD_FILE2 : Sid.UPLOAD_FILE2_V2;

                                                RequestFileUploadModel requestModel = new RequestFileUploadModel(sid, userId, uploadDirectory, uploadFilename, uploadKey, clientSessionId, clientLocale);

                                                ObjectMapper mapper = Utility.createObjectMapper();

                                                String requestJson = mapper.writeValueAsString(requestModel);

                                                Session socketSession = socket.getSession();

                                                socketSession.getAsyncRemote().sendText(requestJson);
//                                            socketSession.getRemote().sendStringByFuture(requestJson);

                                                // response OK

                                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                                resp.setStatus(HttpServletResponse.SC_OK);
                                                resp.getWriter().write(uploadKey);
                                                resp.getWriter().flush();
                                            } else {
                                                // DO NOT delete tmp file AT THIS MOMENT

                                                // update reconnect flag to true -- run only AFTER file deleted
                                                userComputerDao.updateReconnectByUserComputerId(userComputerId, Boolean.TRUE);

                                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "server.not.connected");

                                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                                resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                                                resp.getWriter().write(errorMessage);
                                                resp.getWriter().flush();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(String.format("Error on uploading file(%s).", isV3 ? "V3" : "V4"), e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }

    private boolean checkIfRequestUploadedButUnconfirmed(HttpServletRequest req) {
        boolean isUploadedButUnconfirmed;

        try {
            int value = req.getIntHeader(Constants.HTTP_HEADER_NAME_UPLOADED_BUT_UNCONFIRMED);

            isUploadedButUnconfirmed = (value == 1);
        } catch (Exception e) {
            isUploadedButUnconfirmed = false;
        }

        return isUploadedButUnconfirmed;
    }

    private void responseWithInconsistantDataName(String inconsistantData, HttpServletResponse resp, String clientLocale) throws IOException {
        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "data.inconsistant.need.delete.and.upload.again", inconsistantData);

        resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().write(errorMessage);
        resp.getWriter().flush();
    }

    private boolean validateFileLastModifiedDate(Long valueInDB, long valueInRequest) {
        return valueInDB != null && valueInDB == valueInRequest;
    }

    private boolean validateFileRange(String tmpFilePath, Long transferredByteIndexInDB, long startIndexInRequest) {
        File tmpFile = new File(tmpFilePath);

        return (tmpFile.exists()
                && tmpFile.isFile()
                && transferredByteIndexInDB != null
                && tmpFile.length() == transferredByteIndexInDB + 1
                && transferredByteIndexInDB + 1 == startIndexInRequest);
    }

    private boolean validateFileSize(Long fileSizeInDB, long fileSizeInRequest) {
        return fileSizeInDB != null && fileSizeInDB == fileSizeInRequest;
    }

    private boolean validateFileUploadDirectory(String uploadDirectoryInDB, String uploadDirectoryInRequest) {
        return uploadDirectoryInDB != null && uploadDirectoryInDB.equalsIgnoreCase(uploadDirectoryInRequest);
    }

    private boolean validateFilename(String uploadFilenameInDB, String uploadFilenameInRequest) {
        return uploadFilenameInDB != null && uploadFilenameInDB.equals(uploadFilenameInRequest);
    }
}
