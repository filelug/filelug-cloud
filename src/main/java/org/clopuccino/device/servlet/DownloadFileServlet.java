package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpHeaders;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.*;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.*;
import org.clopuccino.server.servlet.websocket.ConnectSocket;
import org.clopuccino.server.servlet.Sid;
import org.clopuccino.service.NetworkService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <code>DownloadFileServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "download-file-to-device", displayName = "download-file-to-device", description = "Download file to device", urlPatterns = {"/directory/ddownload"})
public class DownloadFileServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DownloadFileServlet.class.getSimpleName());

    private static final long serialVersionUID = 4602679188733572966L;

    private final FileDownloadDao fileDownloadDao;

    private final UserDao userDao;

    private final ClientSessionService clientSessionService;

    private final UserComputerDao userComputerDao;

    private final ComputerDao computerDao;

    private final DeviceTokenDao deviceTokenDao;

    private final NetworkService networkService;

    private final UserComputerPropertiesDao userComputerPropertiesDao;


    public DownloadFileServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        fileDownloadDao = new FileDownloadDao(dbAccess);

        userDao = new UserDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        deviceTokenDao = new DeviceTokenDao(dbAccess);

        networkService = new NetworkService();

        userComputerPropertiesDao = new UserComputerPropertiesDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Request Download Headers:");
                Enumeration<String> headerNames = req.getHeaderNames();
                for (; headerNames.hasMoreElements();) {
                    String name = headerNames.nextElement();
                    String value = req.getHeader(name);
                    LOGGER.debug(name + ": " + value);
                }
            }

            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            final String userId = clientSession.getUserId();

            final Long computerId = clientSession.getComputerId();

            String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

            String clientLocale = clientSession.getLocale();

            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode pathNode = jsonNode.get("path");
            JsonNode downloadKeyNode = jsonNode.get("transferKey");
            JsonNode deviceTokenNode = jsonNode.get("device-token");

            if (downloadKeyNode == null || downloadKeyNode.textValue() == null) {
                String errorMessage = ClopuccinoMessages.getMessage("param.null.or.empty", "upload/download key");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (pathNode == null || pathNode.textValue() == null) {
                // path not found or invalid
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "file path");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                // send message to desktop
                final String filePath = pathNode.textValue();
                final String downloadKey = downloadKeyNode.textValue();

                        /* check if download key duplicated.
                         * Under broken network, some clients may request the download automatically.
                         */
                if (fileDownloadDao.existingFileDownloadForDownloadKey(downloadKey)) {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    resp.getWriter().write("Duplicated download key");
                    resp.getWriter().flush();
                } else {
                    // make sure computer exists
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
                                String range = req.getHeader(HttpHeaders.RANGE);

                                long availableBytes = Constants.FAKE_AVAILABLE_BYTES_FOR_UNLIMITED_TRANSFER_USER;

                                // Get the download size limit and sent to the desktop
                                long downloadFileSizeLimitInBytes = userDao.findDownloadFileSizeLimitInBytes(userId);

                                // Use the download key as the client session id
                                RequestDownloadFileModel requestModel = new RequestDownloadFileModel(Sid.DOWNLOAD_FILE, userId, filePath, downloadKey, clientLocale, range, availableBytes, downloadFileSizeLimitInBytes);

                                String requestJson = mapper.writeValueAsString(requestModel);

                                socket.setHttpServletResponse(resp);
                                CountDownLatch closeLatch = new CountDownLatch(1);
                                socket.setCloseLatch(closeLatch);

                                    /* prepare Content-Disposition for response */
                                String userAgent = req.getHeader(HttpHeaders.USER_AGENT);

                                String fileName = FilenameUtils.getName(filePath);

                                String contentDisposition;
                                if (userAgent.contains("Chrome") || userAgent.contains("MSIE")) {
                                        /* Chrome and IE8: filename=[encoded filename] */
                                    contentDisposition = "attachment; filename=" + Utility.realUrlEncode(fileName);
                                } else if (userAgent.contains("Safari") && userAgent.contains("Version/5")) {
                                        /* Safari(5): filename=[plain file name string] */
                                    contentDisposition = "attachment; filename=" + new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
                                } else {
                                        /* Safari(6) and Firefox: filename*=UTF-8''[encoded filename] */
                                    contentDisposition = "attachment; filename*=UTF-8''" + Utility.realUrlEncode(fileName);
                                }

                                resp.setHeader("Content-Disposition", contentDisposition);

                                // get device token string from request,
                                // find the device token sequence id with device token and user id,
                                // and add it to the client response object before added to the ClientDownloadResponseUtility
                                if (deviceTokenNode != null) {
                                    String deviceTokenString = deviceTokenNode.textValue();

                                    if (deviceTokenString != null) {
                                        DeviceToken deviceToken = deviceTokenDao.findDeviceTokenByDeviceTokenAndAccount(deviceTokenString, userId);

                                        if (deviceToken != null) {
                                            Long deviceTokenSequenceId = deviceToken.getSequenceId();

                                            if (deviceTokenSequenceId != null) {
                                                resp.setHeader("device-token-id", String.valueOf(deviceTokenSequenceId));
                                            }
                                        }
                                    }
                                }

                                String toIp = networkService.getClientIpAddress(req);
                                String toHost = networkService.getClientHostname(req);

                                // save download record to db - for top-ups and history
                                fileDownloadDao.createFileDownloaded(downloadKey, userId, computerId, computer.getGroupName(), computer.getComputerName(), filePath, null, System.currentTimeMillis(), toIp, toHost);

                                // map response with client session and file path
                                ClientDownloadResponseUtility.put(downloadKey, new CloseLatchAndDownloadResponse(closeLatch, resp));

                                Session socketSession = socket.getSession();
                                socketSession.getAsyncRemote().sendText(requestJson);

                                // current thread blocked until ConnectSocket using resp to response to the client
                                closeLatch.await(Constants.DEFAULT_TRANSFER_FILE_CLOSE_LATCH_WAIT_IN_SECONDS, TimeUnit.SECONDS);

                                // normally, it is because of timeout to connect to desktop
                                if (!resp.isCommitted()) {
                                    // update reconnect flag to true
                                    userComputerDao.updateReconnectByUserComputerId(userComputerId, Boolean.TRUE);

                                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "desktop.connect.timeout.try.again");

                                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                    resp.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
                                    resp.getWriter().write(errorMessage);
                                    resp.getWriter().flush();
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
            }
        } catch (Exception e) {
            LOGGER.error("Error on downloading file.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
