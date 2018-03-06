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
import org.clopuccino.service.ComputerService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <code>CreateFileDownloadSummary2</code> records information for download summary for ready-to-download files.
 * It services ONLY for device with version of 2.x or above.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "create-file-download-summary2", displayName = "create-file-download-summary2", description = "Create file-downloads summary (V2)", urlPatterns = {"/directory/ddownload-sum2"})
public class CreateFileDownloadSummary2 extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CreateFileDownloadSummary2.class.getSimpleName());

    private static final long serialVersionUID = 4712779751511414885L;

    private final ClientSessionService clientSessionService;

    private final UserDao userDao;

    private final ComputerDao computerDao;

    private final UserComputerDao userComputerDao;

    private final FileDownloadGroupDao fileDownloadGroupDao;

    private final FileDownloadDao fileDownloadDao;

    private final ComputerService computerService;

    private final UserComputerPropertiesDao userComputerPropertiesDao;

    public CreateFileDownloadSummary2() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);

        userDao = new UserDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        fileDownloadGroupDao = new FileDownloadGroupDao(dbAccess);

        fileDownloadDao = new FileDownloadDao(dbAccess);

        computerService = new ComputerService(dbAccess);

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

            // Make sure the version of the connected-computer is equals to or larger than 1.1.5 so it supports group-download and resumable downloads.

            String desktopTooOldMessage = computerService.messageIfConnectedDesktopVersionSmallerThan("1.1.5", computerId, userComputerId, clientLocale);

            if (desktopTooOldMessage != null) {
                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(Constants.HTTP_STATUS_DESKTOP_VERSION_TOO_OLD);
                resp.getWriter().write(desktopTooOldMessage);
                resp.getWriter().flush();
            } else {
                ObjectMapper mapper = Utility.createObjectMapper();

                FileDownloadGroup fileDownloadGroup = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), FileDownloadGroup.class);

                if (fileDownloadGroup != null) {
                    final String downloadGroupId = fileDownloadGroup.getDownloadGroupId();
                    Integer notificationType = fileDownloadGroup.getNotificationType();
                    Map<String, String> downloadKeyAndPaths = fileDownloadGroup.getDownloadKeyAndPaths();

//                        String downloadDirectory = fileDownloadGroup.getDownloadGroupDirectory();
//                        Integer subdirectoryType = fileDownloadGroup.getSubdirectoryType();
//                        Integer descriptionType = fileDownloadGroup.getDescriptionType();
//                        String subdirectoryValue = fileDownloadGroup.getSubdirectoryValue();
//                        String descriptionValue = fileDownloadGroup.getDescriptionValue();

                    if (downloadGroupId == null || downloadGroupId.trim().length() < 1) {
                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "download group id"));
                        resp.getWriter().flush();
                    } else if (notificationType == null) {
                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "type of notification"));
                        resp.getWriter().flush();
                    } else if (downloadKeyAndPaths == null || downloadKeyAndPaths.size() < 1) {
                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "file download key & paths"));
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
//                            // find the owner of the computer
//                            String computerOwner = computerDao.findComputerOwnerById(computerId);
//
//                            String ownerUserComputerId;
//
//                            if (computerOwner != null && computerOwner.trim().length() > 0) {
//                                ownerUserComputerId = Utility.generateUserComputerIdFrom(computerOwner, computerId);
//                            } else {
//                                ownerUserComputerId = userComputerId;
//                            }
//
//                            String desktopVersion = userComputerPropertiesDao.findPropertyValue(ownerUserComputerId, org.clopuccino.PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);

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
                                    // If any of download key exists in download group detail, get the file_download_group_id
                                    // to check if any file_downloaded found for this file_download_group_id,
                                    // if found, it means there are already files downloading.
                                    // if not found, delete the file_download_group by this file-download_group_id
                                    // before creating new one.

                                    String previousDownloadGroupId = null;

                                    Set<String> downloadKeys = downloadKeyAndPaths.keySet();

                                    for (String currentDownloadKey : downloadKeys) {
                                        previousDownloadGroupId = fileDownloadGroupDao.findFileDownloadGroupIdByDownloadKey(currentDownloadKey);

                                        if (previousDownloadGroupId != null) {
                                            break;
                                        }
                                    }

                                    boolean duplicatedFileDownloaded = false;

                                    if (previousDownloadGroupId != null) {
                                        // check if any file_downloaded for this file_download_group_id

                                        duplicatedFileDownloaded = fileDownloadDao.existingFileDownloadForDownloadGroupId(previousDownloadGroupId);

                                        if (duplicatedFileDownloaded) {
                                            // transfer key can only corelated to one download group id, return duplicated

                                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "download.data.duplicated.redownload.again");
                                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                            resp.setStatus(HttpServletResponse.SC_CONFLICT);
                                            resp.getWriter().write(errorMessage);
                                            resp.getWriter().flush();
                                        } else {
                                            // delete download group by the previous download grouup id and its related detail before creating new.
                                            fileDownloadGroupDao.deleteFileDownloadGroupById(previousDownloadGroupId);
                                        }
                                    }

                                    if (!duplicatedFileDownloaded) {
                                        // save FileDownloadGroup in server db

                                        fileDownloadGroup.setUserId(userId);
                                        fileDownloadGroup.setComputerId(computerId);

                                        boolean success = fileDownloadGroupDao.createFileDownloadGroupWithDetail(fileDownloadGroup);

                                        if (success) {
                                            // validate file paths in desktop

                                            Collection<String> filePaths = downloadKeyAndPaths.values();

                                            // Get the download size limit and sent to the desktop
                                            long downloadFileSizeLimitInBytes = userDao.findDownloadFileSizeLimitInBytes(userId);

                                            RequestFileDownloadGroupModel requestFileDownloadGroupModel = new RequestFileDownloadGroupModel(Sid.DOWNLOAD_FILE_GROUP, userId, clientLocale, downloadGroupId, filePaths, downloadFileSizeLimitInBytes);

                                            String requestJson = mapper.writeValueAsString(requestFileDownloadGroupModel);

                                            socket.setHttpServletResponse(resp);
                                            CountDownLatch closeLatch = new CountDownLatch(1);
                                            socket.setCloseLatch(closeLatch);

                                            Session socketSession = socket.getSession();
                                            socketSession.getAsyncRemote().sendText(requestJson);
//                                        socketSession.getRemote().sendStringByFuture(requestJson);

                                            // DEBUG
                                            getLogger().debug("Request to create file download summary sent to desktop.");

                                            // current thread blocked until ConnectSocket using resp to response to the client */
                                            closeLatch.await(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                                            // normally, it is because of timeout to connect to desktop
                                            if (!resp.isCommitted()) {
                                                Utility.getExecutorService().execute(() -> {
                                                    // delete this file_download_group and its related details
                                                    fileDownloadGroupDao.deleteFileDownloadGroupById(downloadGroupId);
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
                                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "failed.deal.with.file.download.summary.data");

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
                    resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "download group id"));
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            getLogger().error("Error on creating file download summary.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
