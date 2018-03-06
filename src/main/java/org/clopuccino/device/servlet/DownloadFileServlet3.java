package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpHeaders;
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
import org.clopuccino.service.NetworkService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <code>DownloadFileServlet2</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "download-file-to-device3", displayName = "download-file-to-device3", description = "Download file to device (V3)", urlPatterns = {"/directory/ddownload3"})
public class DownloadFileServlet3 extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DownloadFileServlet3.class.getSimpleName());

    private static final long serialVersionUID = -2114361586375326074L;

    private final FileDownloadDao fileDownloadDao;

    private final UserDao userDao;

    private final ClientSessionService clientSessionService;

    private final UserComputerDao userComputerDao;

    private final ComputerDao computerDao;

    private final FileDownloadGroupDao fileDownloadGroupDao;

    private final NetworkService networkService;

    private final UserComputerPropertiesDao userComputerPropertiesDao;

    public DownloadFileServlet3() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        fileDownloadDao = new FileDownloadDao(dbAccess);

        userDao = new UserDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        fileDownloadGroupDao = new FileDownloadGroupDao(dbAccess);

        networkService = new NetworkService();

        userComputerPropertiesDao = new UserComputerPropertiesDao(dbAccess);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        internalDoGet(req, resp, false, LOGGER);
    }

    protected void internalDoGet(HttpServletRequest req, HttpServletResponse resp, boolean isV2, Logger logger) throws ServletException, IOException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Request Download Headers (%s):", isV2 ? "V2" : "V3"));
                Enumeration<String> headerNames = req.getHeaderNames();
                for (; headerNames.hasMoreElements();) {
                    String name = headerNames.nextElement();
                    String value = req.getHeader(name);
                    logger.debug(name + ": " + value);
                }
            }

            // validate client session id

            String clientSessionId = req.getHeader(Constants.HTTP_HEADER_FILELUG_SESSION_ID_NAME);

            if (clientSessionId == null || clientSessionId.trim().length() < 1) {
                clientSessionId = req.getHeader(Constants.HTTP_HEADER_AUTHORIZATION_NAME);
            }

            if (clientSessionId == null || clientSessionId.trim().length() < 1) {
                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                resp.getWriter().flush();
            } else {
                ClientSession clientSession = clientSessionService.findClientSessionBySessionId(clientSessionId);

                // check range -- 續傳專用
                String range = req.getHeader(HttpHeaders.RANGE);

                // parameter t (transfer key)

                if (clientSession == null) {
                    // session not found

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().flush();

                    logger.warn(String.format("Session not found.\nSession id: '%s'\nRequest URI: '%s'", clientSessionId, req.getRequestURI()));
                } else if (range == null && clientSession.checkTimeout()) {
                    // invalid session for non-resume download

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().flush();

                    logger.warn(String.format("Session timeout.\nSession id: '%s'\nRequest URI: '%s'", clientSessionId, req.getRequestURI()));
                } else {
                    // update last access timestamp ONLY for session that is NOT timeout

                    if (!clientSession.checkTimeout()) {
                        clientSessionService.updateClientSessionLastAccessTimestamp(clientSessionId, System.currentTimeMillis());
                    }

                    final String userId = clientSession.getUserId();

                    final Long computerId = clientSession.getComputerId();

                    String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

                    String clientLocale = clientSession.getLocale();

                    final String encodedDownloadKey = req.getParameter(Constants.HTTP_PARAM_NAME_TRANSFER_KEY);

                    if (encodedDownloadKey == null || encodedDownloadKey.trim().length() < 1) {
                        String errorMessage = ClopuccinoMessages.getMessage("param.null.or.empty", "download key");

                        logger.error("Download failed: " + errorMessage);

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                        // To avoid regarding as the partial content of the downloaded file, do not write anything other than file content into response body
//                        resp.getWriter().write(errorMessage);

                        resp.getWriter().flush();
                    } else {
                        // Check if download key belongs to some download group

                        FileDownloadGroupDetail fileDownloadGroupDetail = fileDownloadGroupDao.findFileDownloadGroupDetailByDownloadKey(encodedDownloadKey);

                        if (fileDownloadGroupDetail == null) {
                            String errorMessage = ClopuccinoMessages.getMessage("download.group.not.found");

                            logger.error("Download failed with download key: " + encodedDownloadKey + "; error message:\n" + errorMessage);

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                            // To avoid regarding as the partial content of the downloaded file, do not write anything other than file content into response body
//                            resp.getWriter().write(errorMessage);

                            resp.getWriter().flush();
                        } else {
                            FileDownload fileDownload = fileDownloadDao.findFileDownloadForDownloadKey(encodedDownloadKey);

                            if (fileDownload != null && fileDownload.getStatus().equals(DatabaseConstants.TRANSFER_STATUS_SUCCESS)) {
                                // check if download key duplicated and the file already downloaded successfully.

                                logger.error("Download failed: Duplicated download key: " + encodedDownloadKey);

                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.setStatus(HttpServletResponse.SC_CONFLICT);

                                // To avoid regarding as the partial content of the downloaded file, do not write anything other than file content into response body
//                                resp.getWriter().write("Duplicated download key");

                                resp.getWriter().flush();
                            } else {
                                // make sure computer exists
                                final Computer computer = computerDao.findComputerById(computerId);

                                if (computer == null) {
                                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "computer.not.found");

                                    logger.error("Download failed with download key: " + encodedDownloadKey + "; error message:\n" + errorMessage);

                                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                    resp.setStatus(Constants.HTTP_STATUS_COMPUTER_NOT_FOUND);

                                    // To avoid regarding as the partial content of the downloaded file, do not write anything other than file content into response body
//                                    resp.getWriter().write(errorMessage);

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
                                        && isV2
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
                                            String filePath = fileDownloadGroupDetail.getFilePath();

                                            long availableBytes = Constants.FAKE_AVAILABLE_BYTES_FOR_UNLIMITED_TRANSFER_USER;

                                            // Get the download size limit and sent to the desktop
                                            long downloadFileSizeLimitInBytes = userDao.findDownloadFileSizeLimitInBytes(userId);

                                            // Use the download key as the client session id

                                            int sid = isV2 ? Sid.DOWNLOAD_FILE2 : Sid.DOWNLOAD_FILE2_V2;

                                            RequestDownloadFileModel requestModel = new RequestDownloadFileModel(sid, userId, filePath, encodedDownloadKey, clientLocale, range, availableBytes, downloadFileSizeLimitInBytes);

                                            ObjectMapper mapper = Utility.createObjectMapper();

                                            String requestJson = mapper.writeValueAsString(requestModel);

                                            socket.setHttpServletResponse(resp);
                                            CountDownLatch closeLatch = new CountDownLatch(1);
                                            socket.setCloseLatch(closeLatch);

                                            // prepare Content-Disposition for response
                                            String userAgent = req.getHeader(HttpHeaders.USER_AGENT);

                                            String fileName = FilenameUtils.getName(filePath);

                                            String contentDisposition;
                                            if (userAgent.contains("Chrome") || userAgent.contains("MSIE")) {
                                                // Chrome and IE8: filename=[encoded filename]
                                                contentDisposition = "attachment; filename=" + Utility.realUrlEncode(fileName);
                                            } else if (userAgent.contains("Safari") && userAgent.contains("Version/5")) {
                                                // Safari(5): filename=[plain file name string]
                                                contentDisposition = "attachment; filename=" + new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
                                            } else {
                                                // Safari(6) and Firefox: filename*=UTF-8''[encoded filename]
                                                contentDisposition = "attachment; filename*=UTF-8''" + Utility.realUrlEncode(fileName);
                                            }

                                            resp.setHeader("Content-Disposition", contentDisposition);

                                            String downloadGroupId = fileDownloadGroupDetail.getDownloadGroupId();

                                            String toIp = networkService.getClientIpAddress(req);
                                            String toHost = networkService.getClientHostname(req);

                                            // save download record to db - for top-ups and history
                                            if (fileDownload != null) {
                                                fileDownload.setEndTimestamp(0L);
                                                fileDownload.setStatus(DatabaseConstants.TRANSFER_STATUS_PROCESSING);
                                                fileDownload.setToIp(toIp);
                                                fileDownload.setToHost(toHost);

                                                fileDownloadDao.updateFileDownload(fileDownload);
                                            } else {
                                                fileDownloadDao.createFileDownloaded(encodedDownloadKey, userId, computerId, computer.getGroupName(), computer.getComputerName(), filePath, downloadGroupId, System.currentTimeMillis(), toIp, toHost);
                                            }

                                            // map response with client session and file path
                                            ClientDownloadResponseUtility.put(encodedDownloadKey, new CloseLatchAndDownloadResponse(closeLatch, resp));

                                            Session socketSession = socket.getSession();
                                            socketSession.getAsyncRemote().sendText(requestJson);
//                                        socketSession.getRemote().sendStringByFuture(requestJson);

                                            // current thread blocked until ConnectSocket using resp to response to the client
                                            closeLatch.await(Constants.DEFAULT_TRANSFER_FILE_CLOSE_LATCH_WAIT_IN_SECONDS, TimeUnit.SECONDS);

                                            // normally, it is because of timeout to connect to desktop
                                            if (!resp.isCommitted()) {
                                                // update reconnect flag to true
                                                userComputerDao.updateReconnectByUserComputerId(userComputerId, Boolean.TRUE);

                                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "desktop.connect.timeout.try.again");

                                                logger.error("Download failed with download key: " + encodedDownloadKey + "; error message:\n" + errorMessage);

                                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                                resp.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);

                                                // To avoid regarding as the partial content of the downloaded file, do not write anything other than file content into response body
//                                            resp.getWriter().write(errorMessage);

                                                resp.getWriter().flush();
                                            }
                                        } else {
                                            // update reconnect flag to true
                                            userComputerDao.updateReconnectByUserComputerId(userComputerId, Boolean.TRUE);

                                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "server.not.connected");

                                            logger.error("Download failed with download key: " + encodedDownloadKey + "; error message:\n" + errorMessage);

                                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);

                                            // To avoid regarding as the partial content of the downloaded file, do not write anything other than file content into response body
//                                        resp.getWriter().write(errorMessage);

                                            resp.getWriter().flush();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            String errorMessage = e.getClass().getName() + ": " + e.getMessage();

            logger.error("Download failed: " + errorMessage);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            // To avoid regarding as the partial content of the downloaded file, do not write anything other than file content into response body
//            resp.getWriter().write(errorMessage);

            resp.getWriter().flush();
        }
    }
}
