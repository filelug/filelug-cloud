package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.*;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.*;
import org.clopuccino.server.servlet.Sid;
import org.clopuccino.server.servlet.websocket.ConnectSocket;
import org.clopuccino.service.ClientSessionService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <code>CreateFileUploadSummary2</code> records information for upload summary for ready-to-upload files.
 * It services ONLY for device with version of 2.x or above.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "create-file-upload-summary2", displayName = "create-file-upload-summary2", description = "Create file-uploads summary (V2)", urlPatterns = {"/directory/dupload-sum2"})
public class CreateFileUploadSummary2 extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CreateFileUploadSummary2.class.getSimpleName());

    private static final long serialVersionUID = -4605730233773951836L;

    private final ClientSessionService clientSessionService;

    private final ComputerDao computerDao;

    private final UserComputerDao userComputerDao;

    private final FileUploadGroupDao fileUploadGroupDao;

    private final FileUploadDao fileUploadDao;

    private final UserComputerPropertiesDao userComputerPropertiesDao;

    public CreateFileUploadSummary2() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        fileUploadGroupDao = new FileUploadGroupDao(dbAccess);

        fileUploadDao = new FileUploadDao(dbAccess);

        userComputerPropertiesDao = new UserComputerPropertiesDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        internalDoPost(req, resp, false);
    }

    protected Logger getLogger() {
        return LOGGER;
    }

    protected void internalDoPost(HttpServletRequest req, HttpServletResponse resp, boolean isVersion1) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            final String userId = clientSession.getUserId();

            final Long computerId = clientSession.getComputerId();

            String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

            String clientLocale = clientSession.getLocale();

            ObjectMapper mapper = Utility.createObjectMapper();

            FileUploadGroup fileUploadGroup = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), FileUploadGroup.class);

            if (fileUploadGroup != null) {
                final String uploadGroupId = fileUploadGroup.getUploadGroupId();
                // uplodd directory includes subdirectory value, e.g.
                // upload directory ends with 'subdirectoryValue' below, if any.
                String uploadDirectory = fileUploadGroup.getUploadGroupDirectory();

                Integer subdirectoryType = fileUploadGroup.getSubdirectoryType();
                Integer descriptionType = fileUploadGroup.getDescriptionType();
                Integer notificationType = fileUploadGroup.getNotificationType();
                String subdirectoryValue = fileUploadGroup.getSubdirectoryValue();
                String descriptionValue = fileUploadGroup.getDescriptionValue();
                List<String> uploadKeys = fileUploadGroup.getUploadKeys();

                if (uploadGroupId == null || uploadGroupId.trim().length() < 1) {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "upload group id"));
                    resp.getWriter().flush();
                } else if (uploadDirectory == null || uploadDirectory.trim().length() < 1) {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "upload directory"));
                    resp.getWriter().flush();
                } else if (subdirectoryType == null) {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "type of subdirectory"));
                    resp.getWriter().flush();
                } else if (descriptionType == null) {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "type of description"));
                    resp.getWriter().flush();
                } else if (notificationType == null) {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "type of notification"));
                    resp.getWriter().flush();
                } else if (subdirectoryType != 0 && (subdirectoryValue == null || subdirectoryValue.trim().length() < 1)) {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "subdirectory"));
                    resp.getWriter().flush();
                } else if (descriptionType != 0 && (descriptionValue == null || descriptionValue.trim().length() < 1)) {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "description"));
                    resp.getWriter().flush();
                } else if (uploadKeys == null || uploadKeys.size() < 1) {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "file upload keys"));
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
//                        // find the owner of the computer
//                        String computerOwner = computerDao.findComputerOwnerById(computerId);
//
//                        String ownerUserComputerId;
//
//                        if (computerOwner != null && computerOwner.trim().length() > 0) {
//                            ownerUserComputerId = Utility.generateUserComputerIdFrom(computerOwner, computerId);
//                        } else {
//                            ownerUserComputerId = userComputerId;
//                        }
//
//                        String desktopVersion = userComputerPropertiesDao.findPropertyValue(ownerUserComputerId, org.clopuccino.PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);

                        if (desktopVersion != null && Version.valid(desktopVersion)
                            && isVersion1
                            && new Version(desktopVersion).compareTo(new Version(Constants.INITIAL_VERSION_TO_V2)) >= 0) {

                            // Incompatible with version of desktop that is equal to or larger than 2.0.0

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(Constants.HTTP_STATUS_DEVICE_VERSION_TOO_OLD);
                            resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "device.need.update", computer.getComputerName()));
                            resp.getWriter().flush();
                        } else {
                            // get socket by user and validate it
                            ConnectSocket socket = ConnectSocket.getInstance(userComputerId);

                            if (socket != null && socket.validate(true)) {

                                // If any of upload key exists in upload detail, get the file_upload_group_id
                                // to check if any file_uploaded found for this file_upload_group_id,
                                // if found, it means there are already files uploading.
                                // if not found, delete the file_upload_group by this file-upload_group_id
                                // before creating new one.

                                String previousUploadGroupId = null;

                                for (String currentUploadKey : uploadKeys) {
                                    previousUploadGroupId = fileUploadGroupDao.findFileUploadGroupIdByUploadKey(currentUploadKey);

                                    if (previousUploadGroupId != null) {
                                        break;
                                    }
                                }

                                boolean duplicatedFileUploaded = false;

                                if (previousUploadGroupId != null) {
                                    // check if any file_uploaded for this file_upload_group_id

                                    duplicatedFileUploaded = fileUploadDao.existingFileUploadForUploadGroupId(previousUploadGroupId);

                                    if (duplicatedFileUploaded) {
                                        // transfer key can only corelated to one upload group id, return duplicated

                                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "upload.data.duplicated.reupload.again");
                                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                        resp.setStatus(HttpServletResponse.SC_CONFLICT);
                                        resp.getWriter().write(errorMessage);
                                        resp.getWriter().flush();
                                    } else {
                                        // delete upload group by the previous upload grouup id and its related detail before creating new.
                                        fileUploadGroupDao.deleteFileUploadGroupById(previousUploadGroupId);
                                    }
                                }

                                if (!duplicatedFileUploaded) {
                                    // save FileUploadGroup in repository db

                                    fileUploadGroup.setUserId(userId);
                                    fileUploadGroup.setComputerId(computerId);
                                    fileUploadGroup.setCreatedInDesktopTimestamp(0L);

                                    boolean success = fileUploadGroupDao.createFileUploadGroupWithDetail(fileUploadGroup);

                                    if (success) {
                                        // prepare subdirectory and description in desktop
                                        RequestFileUploadGroupModel requestFileUploadGroupModel = new RequestFileUploadGroupModel(Sid.UPLOAD_FILE_GROUP, userId, clientLocale, fileUploadGroup);

                                        String requestJson = mapper.writeValueAsString(requestFileUploadGroupModel);

                                        socket.setHttpServletResponse(resp);
                                        CountDownLatch closeLatch = new CountDownLatch(1);
                                        socket.setCloseLatch(closeLatch);

                                        Session socketSession = socket.getSession();
                                        socketSession.getAsyncRemote().sendText(requestJson);

                                        // current thread blocked until ConnectSocket using resp to response to the client */
                                        closeLatch.await(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                                        // normally, it is because of timeout to connect to desktop
                                        if (!resp.isCommitted()) {
                                            Utility.getExecutorService().execute(() -> {
                                                // delete this file_upload_group and its related details
                                                fileUploadGroupDao.deleteFileUploadGroupById(uploadGroupId);
                                            });

                                            // update reconnect flag to true
                                            userComputerDao.updateReconnectByUserComputerId(userComputerId, Boolean.TRUE);

                                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "desktop.connect.timeout.try.again");

                                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                            resp.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
                                            resp.getWriter().write(errorMessage);
                                            resp.getWriter().flush();
                                        }
                                    } else {
                                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "failed.deal.with.file.upload.summary.data");

                                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                        resp.getWriter().write(errorMessage);
                                        resp.getWriter().flush();
                                    }
                                }
                            } else {
                                // update reconnect flag to true
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
            } else {
                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "upload group id"));
                resp.getWriter().flush();
            }
        } catch (Exception e) {
            getLogger().error("Error on creating file upload summary.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
