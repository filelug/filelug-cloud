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
import org.clopuccino.domain.Version;
import org.clopuccino.server.servlet.Sid;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.Computer;
import org.clopuccino.domain.RequestFileUploadModel;
import org.clopuccino.server.servlet.websocket.ConnectSocket;
import org.clopuccino.service.NetworkService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.io.*;
import java.util.Enumeration;
import java.util.concurrent.TimeoutException;

/**
 * <code>AbstractUploadFileServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public abstract class AbstractUploadFileServlet extends HttpServlet {

    private static final long serialVersionUID = -487757983812942187L;

    private final FileUploadDao fileUploadDao;

    private final UserDao userDao;

    private final ClientSessionService clientSessionService;

    private final UserComputerDao userComputerDao;

    private final ComputerDao computerDao;

    private final FileUploadGroupDao fileUploadGroupDao;

    private final NetworkService networkService;

    private final UserComputerPropertiesDao userComputerPropertiesDao;

    public abstract Logger getLogger();

    public abstract Integer getDesktopSid();


    public AbstractUploadFileServlet() {
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
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Request Upload Headers:");
                Enumeration<String> headerNames = req.getHeaderNames();
                for (; headerNames.hasMoreElements(); ) {
                    String name = headerNames.nextElement();
                    String value = req.getHeader(name);
                    getLogger().debug(name + ": " + value);
                }
            }

            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            // upload key -- already a base64 string
            final String uploadKey = req.getHeader(Constants.HTTP_HEADER_NAME_UPLOAD_KEY);

            // upload directory
            String encodedUploadDirectory = req.getHeader(Constants.HTTP_HEADER_NAME_UPLOAD_DIRECTORY);

            // upload file name
            String encodedUploadFilename = req.getHeader(Constants.HTTP_HEADER_NAME_UPLOAD_FILE_NAME);

            // size in byte
            String uploadFileSize = req.getHeader(Constants.HTTP_HEADER_NAME_UPLOAD_FILE_SIZE);

            // upload-group-id
//                String uploadGroupId = req.getHeader(Constants.HTTP_HEADER_NAME_UPLOAD_GROUP_ID);

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
//                ClientSession clientSession = clientSessionService.findClientSessionBySessionId(clientSessionId);
                final String userId = clientSession.getUserId();

                getLogger().error("User '" + userId + "' is interrupting file uploading data by changing the upload file size to: " + uploadFileSize);

                String errorMessage = ClopuccinoMessages.getMessage("illegal.file.size", uploadFileSize);

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                final String clientSessionId = clientSession.getSessionId();

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

                        getLogger().error("Uploading file size limit exceeded for user: " + userId + "\n" + errorMessage);

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(Constants.HTTP_STATUS_FILE_SIZE_LIMIT_EXCEEDED);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else {
                        // Under broken network, some clients may resent the upload automatically.
                        // We check here to see if upload key already in file_uploaded.
                        // Upload key can created in file_uploaded only when all the bytes of
                        // this file uploaded completed.
                        if (fileUploadDao.existingFileUploadForUploadKey(uploadKey)) {
                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_CONFLICT);
                            resp.getWriter().write("Duplicated upload key");
                            resp.getWriter().flush();
                        } else {
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
//                                // find the owner of the computer
//                                String computerOwner = computerDao.findComputerOwnerById(computerId);
//
//                                String ownerUserComputerId;
//
//                                if (computerOwner != null && computerOwner.trim().length() > 0) {
//                                    ownerUserComputerId = Utility.generateUserComputerIdFrom(computerOwner, computerId);
//                                } else {
//                                    ownerUserComputerId = userComputerId;
//                                }
//
//                                String desktopVersion = userComputerPropertiesDao.findPropertyValue(ownerUserComputerId, org.clopuccino.PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);

                                if (desktopVersion != null && Version.valid(desktopVersion)
                                    && (getDesktopSid() == Sid.UPLOAD_FILE || getDesktopSid() == Sid.UPLOAD_FILE2)
                                    && new Version(desktopVersion).compareTo(new Version(Constants.INITIAL_VERSION_TO_V2)) >= 0) {

                                    // Incompatible with version of desktop that is equal to or larger than 2.0.0

                                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                    resp.setStatus(Constants.HTTP_STATUS_DEVICE_VERSION_TOO_OLD);
                                    resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "device.need.update", computer.getComputerName()));
                                    resp.getWriter().flush();
                                } else {
                                    // for upload-cache mechanism, no matter if desktop connected,
                                    // the file should upload to server first.
                                    // So it should record this file_uploaded before checking if socket connected.

                                    // Check if upload key belongs to some upload group
                                    String uploadGroupId = fileUploadGroupDao.findFileUploadGroupIdByUploadKey(uploadKey);

                                    // create tmp file
                                    File tmpFile = File.createTempFile(Constants.TMP_UPLOAD_FILE_PREFIX, Utility.genereateTmpFileSuffix(uploadFilename));

                                    // save to tmp file

                                    try (OutputStream outputStream = new FileOutputStream(tmpFile)) {
                                        ByteStreams.copy(req.getInputStream(), outputStream);

                                        getLogger().debug("File copied to \"" + tmpFile.getAbsolutePath() + "\" successfully.");
                                    } catch (Exception e) {
                                        String logMessage;

                                        Throwable cause = e.getCause();

                                        if ((cause != null && TimeoutException.class.isInstance(cause)) || EOFException.class.isInstance(e)) {
                                            logMessage = "Timeout to upload file: " + uploadFilename + "\" to tmp: " + tmpFile.getAbsolutePath() + "\nupload key: " + uploadKey + "\nDevice tries to upload again.";
                                        } else {
                                            logMessage = "Failed to copy file \"" + uploadFilename + "\" to tmp: " + tmpFile.getAbsolutePath() + "\nupload key: " + uploadKey;
                                        }

                                        getLogger().error(logMessage, e);

                                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "failed.upload.file.try.later", uploadFilename);

                                        throw new IOException(errorMessage);
                                    }

                                    // save upload record to db, including upload group id, if any -- only after tmp file saved successfully.

                                    long currentTimestamp = System.currentTimeMillis();

                                    String fromIp = networkService.getClientIpAddress(req);

                                    String fromHost = networkService.getClientHostname(req);

                                    fileUploadDao.createFileUploaded(uploadKey, userId, computerId, computer.getGroupName(), computer.getComputerName(), uploadDirectory, uploadFilename, newFileSize, uploadGroupId, currentTimestamp, DatabaseConstants.TRANSFER_STATUS_PROCESSING, tmpFile.getAbsolutePath(), currentTimestamp, -1, 0, fromIp, fromHost);

                                    // The new upload key has added to the table file_upload_group_detail when device request to replace old upload key with new one.
                                    // It's no need to create again.
                                    // User from device may delete the previous file upload data with the same transfer key before re-upload the same file.
                                    // So save transfer key to table: upload group detail, if not exists
//                                        if (uploadGroupId != null) {
//                                            fileUploadGroupDao.createFileUploadGroupDetailIfNotExists(uploadGroupId, uploadKey);
//                                        }

                                    // get socket by user and validate it
                                    ConnectSocket socket = ConnectSocket.getInstance(userComputerId);

                                    if (socket != null && socket.validate(true)) {
                                        // send message to server

                                        RequestFileUploadModel requestModel = new RequestFileUploadModel(getDesktopSid(), userId, uploadDirectory, uploadFilename, uploadKey, clientSessionId, clientLocale);

                                        ObjectMapper mapper = Utility.createObjectMapper();

                                        String requestJson = mapper.writeValueAsString(requestModel);

                                        Session socketSession = socket.getSession();

                                        socketSession.getAsyncRemote().sendText(requestJson);
//                                    socketSession.getRemote().sendStringByFuture(requestJson);

                                        // response OK

                                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                        resp.setStatus(HttpServletResponse.SC_OK);
                                        resp.getWriter().write(uploadKey);
                                        resp.getWriter().flush();
                                    } else {
                                        // delete tmp file
                                        if (tmpFile.exists() && tmpFile.isFile()) {
                                            String tmpFileAbsolutePath = tmpFile.getAbsolutePath();

                                            try {
                                                if (tmpFile.delete()) {
                                                    getLogger().debug("Deleted tmp file: " + tmpFileAbsolutePath);

                                                    fileUploadDao.updateFileUploadTmpFileDeletedTimestamp(uploadKey, System.currentTimeMillis());
                                                }
                                            } catch (Exception e) {
                                                getLogger().error("Failed to delete file: \"" + tmpFileAbsolutePath + "\". You need to delete it manually.", e);
                                            }
                                        }

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
        } catch (Exception e) {
            getLogger().error("Error on uploading file.", e);

            String errorMessage = e.getMessage();

            if (resp != null) {
                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
                resp.getWriter().flush();
            }
        }
    } 
}
