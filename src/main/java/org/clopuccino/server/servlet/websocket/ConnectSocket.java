package org.clopuccino.server.servlet.websocket;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.time.DateUtils;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.*;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.*;
import org.clopuccino.server.servlet.Sid;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.service.sns.SnsMobilePushService;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * <code>ConnectSocket</code> handles the connection between server and repository.
 */
//@WebSocket(maxIdleTime = 600000, maxTextMessageSize = Integer.MAX_VALUE, maxBinaryMessageSize = Integer.MAX_VALUE)
//@WebSocket
@ServerEndpoint("/connect")
public class ConnectSocket {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ConnectSocket.class.getSimpleName());

    /*
     * The class instance will be added to instances after connection built successfully.
     * key=user computer id, with lower cases.
     */
    private static final Hashtable<String, ConnectSocket> instances = new Hashtable<>();

    private static ScheduledExecutorService deleteInvalidConnectSocketService;

    private static boolean stopDelete = false;

    private final FileDownloadDao fileDownloadDao;

    private final UserDao userDao;

    private final ComputerDao computerDao;

    private final UserComputerDao userComputerDao;

    private final ApplyConnectionDao applyConnectionDao;

    private final UserComputerPropertiesDao userComputerPropertiesDao;

    private final FilelugPropertiesDao filelugPropertiesDao;

    private final FileUploadDao fileUploadDao;

    private final FileUploadGroupDao fileUploadGroupDao;

    private final FileDownloadGroupDao fileDownloadGroupDao;

    private final ClientSessionService clientSessionService;

    private final SnsMobilePushService pushService;

    private Session session;

    private String userComputerId;

    private String userId;

    /* in ms */
    private Long lastAccessTime;

    private Locale clientLocale;

    private HttpServletResponse resp;

    private CountDownLatch closeLatch;


    public static void putInstance(String userComputerId, ConnectSocket socket) {
        instances.put(userComputerId.toLowerCase(), socket);
    }

    public static void removeInstance(String userComputerId) {
        instances.remove(userComputerId.toLowerCase());
    }

    public static ConnectSocket getInstance(String userComputerId) {
        return instances.get(userComputerId.toLowerCase());
    }

    public static boolean aliveInstance(String userComputerId) {
        ConnectSocket socket = instances.get(userComputerId.toLowerCase());

        return socket != null && socket.getSession() != null && socket.getSession().isOpen() && !socket.checkTimeout(Constants.DEFAULT_CONNECT_SOCKET_IDLE_TIMEOUT_IN_SECONDS);
    }

    public static boolean isStopDelete() {
        return ConnectSocket.stopDelete;
    }

    public static void setStopDelete(boolean stopDelete) {
        ConnectSocket.stopDelete = stopDelete;
    }

    public static void startDeletingInvalidConnectSockets(Integer initialDelayInSeconds, Integer periodInSeconds) {
        if (deleteInvalidConnectSocketService == null) {
            if (periodInSeconds == null || periodInSeconds < Constants.DEFAULT_DELETE_INVALID_CONNECT_SOCKET_INTERVAL) {
                periodInSeconds = Constants.DEFAULT_DELETE_INVALID_CONNECT_SOCKET_INTERVAL;
            }

            deleteInvalidConnectSocketService = Executors.newSingleThreadScheduledExecutor();
            deleteInvalidConnectSocketService.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    if (!stopDelete) {
                        try {
                            Set<String> invalidConnectSocketKeys = findAndDisconnectInvalidConnectSocketKeys(Constants.DEFAULT_CONNECT_SOCKET_IDLE_TIMEOUT_IN_SECONDS);

                            if (invalidConnectSocketKeys.size() > 0) {
                                for (String key : invalidConnectSocketKeys) {
                                    removeInstance(key);

                                    LOGGER.info("Removed invalid connect socket for user: " + key);
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.error("Error on processing timeout client session deletion!\n" + e.getMessage(), e);
                        }
                    }
                }
            }, initialDelayInSeconds, periodInSeconds, TimeUnit.SECONDS);
        }
    }

    public static void terminateDeleteInvalidConnectSocketService() {
        if (deleteInvalidConnectSocketService != null) {
            setStopDelete(true);

            Utility.shutdownAndAwaitTermination(deleteInvalidConnectSocketService, Constants.DEFAULT_AWAIT_TERMINATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        }
    }

    public static Set<String> findAndDisconnectInvalidConnectSocketKeys(Integer timeout) throws Exception {
        Set<String> keys = new HashSet<>();

        /* remove items in loop -- using Iterator */
        Iterator<Map.Entry<String, ConnectSocket>> iterator = instances.entrySet().iterator();

        for (; iterator.hasNext(); ) {
            Map.Entry<String, ConnectSocket> entry = iterator.next();

            String userComputerId = entry.getKey();

            ConnectSocket connectSocket = entry.getValue();

            if (connectSocket == null || connectSocket.getSession() == null) {
                keys.add(userComputerId);

                LOGGER.info("Connect socket for user computer (with lower case): " + userComputerId + " is null or the socket session is null. Ready to delete it.");
            } else if (!connectSocket.getSession().isOpen()) {
                try {
                    /* This should be invoked from desktop service when its socket idle timeout  */
//                    connectSocket.getSession().close(StatusCode.NORMAL, "Socket not opened.");
//                    connectSocket.getSession().disconnect();
                    ConnectSocketUtilities.closeSession(connectSocket.getSession(), CloseReason.CloseCodes.NORMAL_CLOSURE, "Socket not opened.");
                } catch (Throwable t) {
                    /* ignored */
                } finally {
                    keys.add(userComputerId);

                    LOGGER.info("The socket session of the connect socket for user computer (with lower case): " + userComputerId + " is not opened. Ready to delete it.");
                }
            } else if (connectSocket.checkTimeout(timeout)) {
                try {
                    /* This should be invoked from desktop service when its socket idle timeout  */
//                    connectSocket.getSession().close(StatusCode.NORMAL, "Socket timeout.");
//                    connectSocket.getSession().disconnect();
                    ConnectSocketUtilities.closeSession(connectSocket.getSession(), CloseReason.CloseCodes.NORMAL_CLOSURE, "Socket timeout.");
                } catch (Throwable t) {
                    /* ignored */
                } finally {
                    keys.add(userComputerId);

                    LOGGER.info("The socket session of the connect socket for user: " + userComputerId + " is timeout (" + timeout + " seconds). Ready to delete it.");
                }
            }
        }

        return keys;
    }

    public static void closeAllConnectSockets() throws Exception {
        if (instances != null && instances.size() > 0) {
            Set<Map.Entry<String, ConnectSocket>> entries = instances.entrySet();

            // Use iterator to remove ConnectSocket in loop
            Iterator<Map.Entry<String, ConnectSocket>> iterator = entries.iterator();

            for (;iterator.hasNext(); ) {
                Map.Entry<String, ConnectSocket> entry = iterator.next();

                String currentUserComputerId = entry.getKey();
                ConnectSocket currentConnectSocket = entry.getValue();

                if (currentConnectSocket != null) {
                    currentConnectSocket.countDownCloseLatch();

                    currentConnectSocket.updateSocketConnectedByUserComputerId(currentUserComputerId, false);
                }

                iterator.remove();
            }
        }
    }

    public ConnectSocket() {
        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        fileDownloadDao = new FileDownloadDao(dbAccess);

        userDao = new UserDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        applyConnectionDao = new ApplyConnectionDao(dbAccess);

        userComputerPropertiesDao = new UserComputerPropertiesDao(dbAccess);

        filelugPropertiesDao = new FilelugPropertiesDao(dbAccess);

        fileUploadDao = new FileUploadDao(dbAccess);

        fileUploadGroupDao = new FileUploadGroupDao(dbAccess);

        fileDownloadGroupDao = new FileDownloadGroupDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);

        pushService = new SnsMobilePushService(dbAccess);
    }

    /**
     *
     * @return true if time out
     */
    public boolean checkTimeout(Integer timeout) {
        if (timeout == null) {
            timeout = Constants.DEFAULT_CONNECT_SOCKET_IDLE_TIMEOUT_IN_SECONDS;
        }

        Date currentDate = new Date();

        Date lastAccessDate = new Date();
        lastAccessDate.setTime(lastAccessTime);
        lastAccessDate = DateUtils.addSeconds(lastAccessDate, timeout);

        return currentDate.compareTo(lastAccessDate) > 0;
    }

    public boolean validate(boolean updateAccessTimeToNowIfValid) {
        boolean valid = false;

        if (getSession() != null && getSession().isOpen() && !checkTimeout(Constants.DEFAULT_CONNECT_SOCKET_IDLE_TIMEOUT_IN_SECONDS)) {
            if (updateAccessTimeToNowIfValid) {
                updateLastAccessTimeToNow();
            }

            valid = true;
        }

        return valid;
    }

    public FileDownloadDao getFileDownloadDao() {
        return fileDownloadDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public ComputerDao getComputerDao() {
        return computerDao;
    }

    public UserComputerDao getUserComputerDao() {
        return userComputerDao;
    }

    public ApplyConnectionDao getApplyConnectionDao() {
        return applyConnectionDao;
    }

    public UserComputerPropertiesDao getUserComputerPropertiesDao() {
        return userComputerPropertiesDao;
    }

    public FilelugPropertiesDao getFilelugPropertiesDao() {
        return filelugPropertiesDao;
    }

    public FileUploadDao getFileUploadDao() {
        return fileUploadDao;
    }

    public FileUploadGroupDao getFileUploadGroupDao() {
        return fileUploadGroupDao;
    }

    public FileDownloadGroupDao getFileDownloadGroupDao() {
        return fileDownloadGroupDao;
    }

    public ClientSessionService getClientSessionService() {
        return clientSessionService;
    }

    public SnsMobilePushService getPushService() {
        return pushService;
    }

    public HttpServletResponse getResp() {
        return resp;
    }

    public String getUserComputerId() {
        return userComputerId;
    }

    public void setUserComputerId(String userComputerId) {
        this.userComputerId = userComputerId;
    }

    public Session getSession() {
        return session;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Locale getClientLocale() {
        return clientLocale;
    }

    public void setClientLocale(Locale clientLocale) {
        this.clientLocale = clientLocale;
    }

    public Long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(Long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public void updateLastAccessTimeToNow() {
        lastAccessTime = System.currentTimeMillis();
    }

    public CountDownLatch getCloseLatch() {
        return closeLatch;
    }

    public void setCloseLatch(CountDownLatch closeLatch) {
        this.closeLatch = closeLatch;
    }

    public HttpServletResponse getHttpServletResponse() {
        return resp;
    }

    public void setHttpServletResponse(HttpServletResponse resp) {
        this.resp = resp;
    }

    /**
     * When receiving connection open events
     */
//    @OnWebSocketConnect
    @OnOpen
    public void onConnect(Session session) {
        /* DEBUG */
        LOGGER.debug("Connection opening from client: " + session.getRequestURI().toString());

        session.setMaxBinaryMessageBufferSize(Integer.MAX_VALUE);
        session.setMaxTextMessageBufferSize(Integer.MAX_VALUE);
        session.setMaxIdleTimeout(Constants.IDLE_TIMEOUT_IN_SECONDS_TO_CONNECT_SOCKET * 1000);

        this.session = session;
    }

    /**
     * When receiving connection close events
     * desktop端調用 ConnectSocket.session.close() 時，repository端對應的session會被關閉，並同時呼叫此method。
     */
//    @OnWebSocketClose
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        LOGGER.debug(String.format("Socket closed with close code: %d - reason: %s", closeReason.getCloseCode().getCode(), closeReason.getReasonPhrase()));

        countDownCloseLatch();

        // FIX: Removed to see if it works to reconnect for the same user.
//        updateSocketConnectedByUserComputerId(userComputerId, false);

        this.session = null;
    }
//    public void onClose(Session session, int statusCode, String reason) {
//        LOGGER.debug(String.format("Socket closed with status: %d - reason: %s", statusCode, reason));
//
//        countDownCloseLatch();
//
//        /* remove linkage */
//
////        updateSocketConnectedByUserComputerId(userComputerId, false);
//
////        if (userComputerId != null) {
////            /* when diconnected another socket after new socket created, invoke this causes null properties for the new socket */
////            userComputerPropertiesDao.deletePropertiesByUserComputerId(userComputerId);
////
////            /* Do not remove ConnectSocket from Map here to prevent asynchronized removed newly created ConnectSocket  */
////            removeInstance(userComputerId);
////        }
//
//        if (session == this.session) {
//            updateSocketConnectedByUserComputerId(userComputerId, false);
//
//            this.session = null;
//        }
//    }

    @OnError
    public void onError(Session session, Throwable t) {
        String errorMessage = String.format("Socket error for user computer: '%s', error:\n%s", userComputerId, t != null ? t.getMessage() : "(Empty error message)");

        if (t != null && SocketTimeoutException.class.isInstance(t) && t.getMessage().toLowerCase().contains("timeout on read")) {
            errorMessage = errorMessage + "\n" + "Check if the version of Filelug desktop is tool old if you cannot find other errors.";
        }

        LOGGER.error(errorMessage, t);
    }

    public void updateSocketConnectedByUserComputerId(String userComputerId, boolean connected) {
        if (userComputerId != null) {
            userComputerDao.updateSocketConnectedById(userComputerId, connected);
        }
    }

    public void countDownCloseLatch() {
        if (closeLatch != null) {
            closeLatch.countDown();
        }
    }

//    @OnWebSocketFrame
//    public void onFrame(Session session, Frame frame) {
//        Frame.Type type = frame.getType();
//
//        String typeName;
//        switch (type) {
//            case CLOSE:
//                typeName = "CLOSE";
//                break;
//            case PING:
//                typeName = "PING";
//                break;
//            case PONG:
//                typeName = "PONG";
//                break;
//            case CONTINUATION:
//                typeName = "CONTINUATION";
//                break;
//            case TEXT:
//                typeName = "TEXT";
//                break;
//            case BINARY:
//                typeName = "BINARY";
//                break;
//            default:
//                typeName = "UNKNOWN";
//        }
//
//        LOGGER.debug("'" + typeName + "' Frame called from: " + session.getRequestURI().toString());
//
////        if (frame.getType() == Frame.Type.PONG || frame.getType() == Frame.Type.PING) {
////            String message;
////            if (frame.hasPayload()) {
////                try {
////                    message = new String(frame.getPayload().array(), "UTF-8");
////                } catch (Exception e) {
////                    message = "(" + e.getMessage()+ ")";
////                }
////            } else {
////                message = "(empty)";
////            }
////
////            LOGGER.info("Connection " + (frame.getType() == Frame.Type.PONG ? "PONG" : "PING") + " message received: " + message);
////        }
//    }

    /**
     * When receive Binary or Text Message events
     */
//    @OnWebSocketMessage
    @OnMessage
    public void onMessage(Session session, String message) {
        Integer sid = Utility.findSidFromJson(message);

//        // DEBUG
//        LOGGER.debug(String.format("SID '%d' message received: %s%nMessage:%n%s%n", sid, session.getRequestURI().toString(), message));

        if (sid == null) {
            onReceiveFromUnsupportedWebSocket(session, message);

//            try {
//                ResponseModel responseModel = new ResponseModel(null, HttpServletResponse.SC_BAD_REQUEST, "No sid", userId, System.currentTimeMillis());
//
//                ObjectMapper mapper = Utility.createObjectMapper();
//
//                RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();
//
//                asyncRemote.setSendTimeout(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_MILLIS);
//
//                asyncRemote.sendText(mapper.writeValueAsString(responseModel));
//
////                Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
////
////                future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//            } catch (Exception e) {
//                int httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
//
//                ConnectSocketUtilities.processOnMessageException(userId, session, null, e, httpStatusCode, false);
//            }
        } else {
            switch (sid) {
                case Sid.CONNECT:
                    onConnectWebSocket(session, message);

                    break;
                case Sid.CONNECT_V2:
                    onConnectFromComputerWebSocket(session, message);

                    break;
                case Sid.LIST_ALL_BOOKMARKS:
                    onListAllBookmarksWebSocket(session, message);

                    break;
                case Sid.FIND_BOOKMARK_BY_ID:
                    onFindBookmarkByIdWebSocket(session, message);

                    break;
                case Sid.CREATE_BOOKMARK:
                    onCreateBookmarkWebSocket(session, message);

                    break;
                case Sid.UPDATE_BOOKMARK:
                    onUpdateBookmarkWebSocket(session, message);

                    break;
                case Sid.DELETE_BOOKMARK_BY_ID:
                    onDeleteBookmarkByIdWebSocket(session, message);

                    break;
                case Sid.SYNCHRONIZE_BOOKMARKS:
                    onSynchronizeBookmarksWebSocket(session, message);

                    break;
                case Sid.LIST_CHILDREN:
                    onListDirectoryChildrenWebSocket(session, message);

                    break;
                case Sid.FIND_BY_PATH:
                    onFindFileByPathWebSocket(session, message);

                    break;
                case Sid.DOWNLOAD_FILE:
                    downloadFileToDeviceWebSocket(session, message);

                    break;
                case Sid.DOWNLOAD_FILE2:
                case Sid.DOWNLOAD_FILE2_V2:
                    downloadFileToDeviceWebSocket2(session, message);

                    break;
                case Sid.FILE_RENAME:
                    onFileRenameWebSocket(session, message);

                    break;
                case Sid.UPLOAD_FILE:
                    uploadFileFromDeviceWebSocket(session, message);

                    break;
                case Sid.UPLOAD_FILE2 :
                case Sid.UPLOAD_FILE2_V2:
                    uploadFileFromDeviceWebSocket2(session, message);

                    break;
                case Sid.UPLOAD_FILE_GROUP:
                    onUploadFileGroupWebSocket(session, message);

                    break;
                case Sid.DELETE_UPLOAD_FILE:
                    onDeleteFileUploadWebSocket(session, message);

                    break;
                case Sid.DOWNLOAD_FILE_GROUP:
                    onDownloadFileGroupWebSocket(session, message);

                    break;
                case Sid.LIST_ALL_ROOT_DIRECTORIES:
                    onListAllRootDirectoriesWebSocket(session, message);

                    break;
                case Sid.LIST_ALL_ROOT_DIRECTORIES_V2:
                    onListAllRootsWebSocket(session, message);

                    break;
                case Sid.UNSUPPORTED:
                    onReceiveFromUnsupportedWebSocket(session, message);

                    break;
                default:
                    LOGGER.error(String.format("Unsupported message received.%nSid '%d'%nMessage:%n%s", sid, message));

                    ConnectSocketUtilities.onUnsupportedWebSocket(userId, session, message, sid);
            }
        }
    }

    private void onListAllRootDirectoriesWebSocket(final Session session, String message) {
        ListRootsWebSocketService listRootsWebSocketService = new ListRootsWebSocketService(session, message, this);

        listRootsWebSocketService.onListAllRootDirectoriesWebSocket();

//        /* receive message from server with rootDirectories information */
//                                                                                              o
//        if (resp != null) {
//            try {
//                ObjectMapper mapper = Utility.createObjectMapper();
//
//                ResponseRootDirectoryArrayModel responseModel = mapper.readValue(message, ResponseRootDirectoryArrayModel.class);
//
//                Integer status = responseModel.getStatus();
//
//                if (status != null && HttpServletResponse.SC_OK == status) {
//                    List<RootDirectory> rootDirectories = responseModel.getRootDirectories();
//
//                    if (rootDirectories == null) {
//                        rootDirectories = new ArrayList<>();
//                    }
//
//                    String rootDirectoriesJson = mapper.writeValueAsString(rootDirectories);
//
//                    /* DEBUG */
//                    LOGGER.debug(String.format("Prepared root directories to response for service findAllRootDirectories: %s", rootDirectoriesJson));
//
//                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
//                    resp.setStatus(status);
//                    resp.getWriter().write(rootDirectoriesJson);
//                    resp.getWriter().flush();
//                } else {
//                    String error = responseModel.getError();
//
//                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                    resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                    resp.getWriter().write(error);
//                    resp.getWriter().flush();
//                }
//            } catch (Exception e) {
//                String errorMessage = String.format("Error on processing response message of service 'findAllRootDirectories'. Message from desktop: %s; error message: %s", message, e.getMessage());
//
//                LOGGER.error(errorMessage);
//
//                try {
//                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                    resp.getWriter().write(errorMessage);
//                    resp.getWriter().flush();
//                } catch (Exception e1) {
//                    /* ignored */
//                }
//            } finally {
//                closeLatch.countDown();
//            }
//        } else {
//            LOGGER.error("HttpServletResponse must be assigned when initiate ConnectSocket!");
//        }
    }

    private void onListAllRootsWebSocket(final Session session, String message) {
        ListRootsWebSocketService listRootsWebSocketService = new ListRootsWebSocketService(session, message, this);

        listRootsWebSocketService.onListAllRootsWebSocket();

//        /* receive message from server with file roots information */
//
//        try {
//            /* only if response haven't its status code and headers written */
//            if (resp != null && !resp.isCommitted()) {
//                ObjectMapper mapper = Utility.createObjectMapper();
//
//                ResponseListRootsModel responseModel = mapper.readValue(message, ResponseListRootsModel.class);
//
//                Integer status = responseModel.getStatus();
//
//                if (status != null && HttpServletResponse.SC_OK == status) {
//                    List<RootDirectory> roots = responseModel.getRoots();
//
//                    if (roots == null) {
//                        roots = new ArrayList<>();
//                    }
//
//                    String rootsJson = mapper.writeValueAsString(roots);
//
//                    /* DEBUG */
//                    LOGGER.debug(String.format("Prepared file roots to response for service listRoots: %s", rootsJson));
//
//                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
//                    resp.setStatus(status);
//                    resp.getWriter().write(rootsJson);
//                    resp.getWriter().flush();
//                } else {
//                    String error = responseModel.getError();
//
//                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                    resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                    resp.getWriter().write(error);
//                    resp.getWriter().flush();
//                }
//            } else {
//                LOGGER.error("HttpServletResponse must be assigned when initiate ConnectSocket!");
//            }
//        } catch (Exception e) {
//            String errorMessage = String.format("Error on processing response message of service 'listRoots'. Message from desktop: %s; error message: %s", message, e.getMessage());
//
//            LOGGER.error(errorMessage);
//
//            try {
//                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                resp.getWriter().write(errorMessage);
//                resp.getWriter().flush();
//            } catch (Exception e1) {
//                    /* ignored */
//            }
//        } finally {
//            closeLatch.countDown();
//        }
    }

    private void downloadFileToDeviceWebSocket(final Session session, String message) {

        DownloadFileToDeviceWebSocketService downloadFileToDeviceWebSocketService = new DownloadFileToDeviceWebSocketService(session, message, this);

        downloadFileToDeviceWebSocketService.onStartTransferingFileToDevice();


//        /* requested to download file content */
//
//        try {
//            ObjectMapper mapper = Utility.createObjectMapper();
//
//            final ResponseFileDownloadModel responseModel = mapper.readValue(message, ResponseFileDownloadModel.class);
//
//            Integer status = responseModel.getStatus();
//
//            final String transferKey = responseModel.getTransferKey();
//
//            // 儲存 file size with download key(download key is the value of client session id)
//            // 成功（200）就更新 file size
//            // 不成功（200以外）就更新status為failure
//
//            if (status != null && HttpServletResponse.SC_OK == status) {
//                Utility.getExecutorService().execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        fileDownloadDao.updateFileDownloadSize(transferKey, responseModel.getFileSize());
//                    }
//                });
//
//                /* 表示 server 已經調用 UploadFileServlet 傳送資料，因此不可調用 resp 回傳資訊 */
//                LOGGER.debug("Desktop is uploading file content. Waiting for the process to complete.");
//            } else {
//                Utility.getExecutorService().execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        fileDownloadDao.updateFileDownloadStatus(transferKey, DatabaseConstants.TRANSFER_STATUS_FAILURE, System.currentTimeMillis());
//                    }
//                });
//
//                if (resp != null && !resp.isCommitted()) {
//                    try {
//                        String error = responseModel.getError();
//
//                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                        resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                        resp.getWriter().write(error);
//                        resp.getWriter().flush();
//                    } finally {
//                        closeLatch.countDown();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            String errorMessage = String.format("Error on processing response message of service 'downloadFile'. Message from desktop: %s; error message: %s", message, e.getMessage());
//
//            LOGGER.error(errorMessage);
//
//            try {
//                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                resp.getWriter().write(errorMessage);
//                resp.getWriter().flush();
//            } catch (Exception e1) {
//                // ignored
//            } finally {
//                closeLatch.countDown();
//            }
//        }
    }

    private void downloadFileToDeviceWebSocket2(final Session session, String message) {

        DownloadFileToDeviceWebSocketService downloadFileToDeviceWebSocketService = new DownloadFileToDeviceWebSocketService(session, message, this);

        downloadFileToDeviceWebSocketService.onStartTransferingFileToDevice2();

//        // requested to download file content (V2), consider partial content
//
//        try {
//            ObjectMapper mapper = Utility.createObjectMapper();
//
//            final ResponseFileDownloadModel responseModel = mapper.readValue(message, ResponseFileDownloadModel.class);
//
//            Integer status = responseModel.getStatus();
//
//            final String transferKey = responseModel.getTransferKey();
//
//            // 儲存 file size with download key(download key is the value of client session id)
//            // 成功（200 or 206）就更新 file size
//            // 不成功（200 與 206 以外）就更新status為failure
//
//            if (status != null && (HttpServletResponse.SC_OK == status || HttpServletResponse.SC_PARTIAL_CONTENT == status)) {
//                Utility.getExecutorService().execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        fileDownloadDao.updateFileDownloadSize(transferKey, responseModel.getFileSize());
//                    }
//                });
//
//                // 表示 server 已經調用 UploadFileServlet 傳送資料，因此不可調用 resp 回傳資訊
//                LOGGER.debug("Desktop is uploading file content. Waiting for the process to complete.");
//            } else {
//                Utility.getExecutorService().execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        fileDownloadDao.updateFileDownloadStatus(transferKey, DatabaseConstants.TRANSFER_STATUS_FAILURE, System.currentTimeMillis());
//                    }
//                });
//
//                if (resp != null && !resp.isCommitted()) {
//                    try {
//                        String error = responseModel.getError();
//
//                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                        resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                        resp.getWriter().write(error);
//                        resp.getWriter().flush();
//                    } finally {
//                        closeLatch.countDown();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            String errorMessage = String.format("Error on processing response message of service 'downloadFile(V2)'. Message from desktop: %s; error message: %s", message, e.getMessage());
//
//            LOGGER.error(errorMessage);
//
//            try {
//                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                resp.getWriter().write(errorMessage);
//                resp.getWriter().flush();
//            } catch (Exception e1) {
//                // ignored
//            } finally {
//                closeLatch.countDown();
//            }
//        }
    }

    private void onFindBookmarkByIdWebSocket(final Session session, String message) {
        /* requested to find the information of the specified bookmark */

        if (resp != null) {
            try {
                ObjectMapper mapper = Utility.createObjectMapper();

                ResponseBookmarkModel responseModel = mapper.readValue(message, ResponseBookmarkModel.class);

                Integer status = responseModel.getStatus();

                if (status != null && HttpServletResponse.SC_OK == status) {
                    Bookmark bookmark = responseModel.getBookmark();

                    String rootsJson = mapper.writeValueAsString(bookmark);

                    /* DEBUG */
                    LOGGER.debug(String.format("Prepared bookmark information to response for service findBookmarkById: %s", rootsJson));

                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                    resp.setStatus(status);
                    resp.getWriter().write(rootsJson);
                    resp.getWriter().flush();
                } else {
                    String error = responseModel.getError();

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(error);
                    resp.getWriter().flush();
                }
            } catch (Exception e) {
                String errorMessage = String.format("Error on processing response message of service 'findBookmarkById'. Message from desktop: %s; error message: %s", message, e.getMessage());

                LOGGER.error(errorMessage);

                try {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } catch (Exception e1) {
                    /* ignored */
                }
            } finally {
                closeLatch.countDown();
            }
        } else {
            LOGGER.error("HttpServletResponse must be assigned when initiate ConnectSocket!");
        }
    } // end onFindBookmarkByIdWebSocket(Session, String)

    private void uploadFileFromDeviceWebSocket(final Session session, String message) {

        UploadFileFromDeviceWebSocketService uploadFileFromDeviceWebSocketService = new UploadFileFromDeviceWebSocketService(session, message, this);

        uploadFileFromDeviceWebSocketService.onFileUploadedToDesktop();

//        /* response of requesting to upload file content */
//
//        try {
//            ObjectMapper mapper = Utility.createObjectMapper();
//
//            ResponseModel responseModel = mapper.readValue(message, ResponseModel.class);
//
//            Integer status = responseModel.getStatus();
//
//            if (status != null && HttpServletResponse.SC_OK == status) {
//                /* 表示 server 已經調用 DownloadFileServlet 傳送資料，因此不可調用 resp 回傳資訊 */
//                LOGGER.info("Server is downloading file content. Waiting for the process to complete.");
//            } else {
//                if (resp != null) {
//                    try {
//                        String error = responseModel.getError();
//
//                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                        resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                        resp.getWriter().write(error);
//                        resp.getWriter().flush();
//                    } finally {
//                        closeLatch.countDown();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            String errorMessage = String.format("Error on processing response message of service 'uploadFile'. Message from desktop: %s; error message: %s", message, e.getMessage());
//
//            LOGGER.error(errorMessage);
//
//            try {
//                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                resp.getWriter().write(errorMessage);
//                resp.getWriter().flush();
//            } catch (Exception e1) {
//                    /* ignored */
//            } finally {
//                closeLatch.countDown();
//            }
//        }
    }

    private void uploadFileFromDeviceWebSocket2(final Session session, String message) {

        UploadFileFromDeviceWebSocketService uploadFileFromDeviceWebSocketService = new UploadFileFromDeviceWebSocketService(session, message, this);

        uploadFileFromDeviceWebSocketService.onFileUploadedToDesktop2();

//        // Get the upload processing result, update result to db and then return response
//
//        ObjectMapper mapper = Utility.createObjectMapper();
//
//        String transferKey = null;
//        String uploadStatus = null;
//        String clientSessionId = null;
//
//        ResponseFileUploadModel responseModel;
//        try {
//            responseModel = mapper.readValue(message, ResponseFileUploadModel.class);
//        } catch (Exception e) {
//            responseModel = null;
//
//            LOGGER.error("Error on parsing response file upload model.\n" + message, e);
//        }
//
//        if (responseModel != null) {
//            transferKey = responseModel.getTransferKey();
//            uploadStatus = responseModel.getUploadStatus();
//            clientSessionId = responseModel.getDeviceSessionId();
//
//            if (uploadStatus != null && uploadStatus.equals(DatabaseConstants.TRANSFER_STATUS_FAILURE)) {
//                // so when resume upload from device, do not have to upload file from device again.
//
//                uploadStatus = DatabaseConstants.TRANSFER_STATUS_DEVICE_UPLOADED_BUT_UNCONFIRMED;
//            } else if (uploadStatus == null || uploadStatus.trim().length() < 1) {
//                uploadStatus = DatabaseConstants.TRANSFER_STATUS_FAILURE;
//            }
//
//            // Make sure the file_uploaded is not deleted yet! If deleted, return failure for file_uploaded not found.
//
//            final FileUpload fileUpload = fileUploadDao.findFileUploadForUploadKey(transferKey);
//
//            if (fileUpload != null) {
//                fileUploadDao.updateFileUploadStatus(transferKey, uploadStatus, System.currentTimeMillis());
//
//                // delete tmp file if exists, under one of the following conditions meet:
//                // (1) if the upload status is success
//                // (2) if the request from device is version 2, instead of version 3.
//
//                Long lastModifiedTimestamp = fileUpload.getSourceFileLastModifiedTimestamp();
//
//                if (uploadStatus.equals(DatabaseConstants.TRANSFER_STATUS_SUCCESS) || lastModifiedTimestamp == null || lastModifiedTimestamp == 0L) {
//                    String tmpFileAbsolutePath = fileUpload.getTmpFile();
//
//                    Utility.deleteUploadTmpFileAndUpdateRecord(tmpFileAbsolutePath, fileUploadDao, transferKey);
//                }
//
//                final String finalTransferKey = transferKey;
//                final String finalUploadStatus = uploadStatus;
//                final String finalClientSessionId = clientSessionId;
//
//                Utility.getExecutorService().execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        String uploadGroupId = fileUpload.getUploadGroupId();
//                        String filename = fileUpload.getFilename();
//
//                        if (uploadGroupId == null) {
//                            // notify device for upload file from old-version device app.
//                            sendRemoteNotificationOnOneFileUploaded(finalTransferKey, finalUploadStatus, finalClientSessionId, filename);
//                        } else {
//                            // get notification type
//
//                            FileUploadGroup fileUploadGroup = fileUploadGroupDao.findFileUploadGroupByUploadGroupId(uploadGroupId, true);
//
//                            Integer notificationType = fileUploadGroup.getNotificationType();
//
//                            if (notificationType != null) {
//                                if (notificationType == 1) {
//                                    // notify on each file
//                                    sendRemoteNotificationOnOneFileUploaded(finalTransferKey, finalUploadStatus, finalClientSessionId, filename);
//                                } else if (notificationType == 2) {
//                                    // notify on alll files, or on file upload failure
//                                    if (finalUploadStatus.equals(DatabaseConstants.TRANSFER_STATUS_FAILURE)
//                                        || finalUploadStatus.equals(DatabaseConstants.TRANSFER_STATUS_DEVICE_UPLOADED_BUT_UNCONFIRMED)) {
//                                        sendRemoteNotificationOnOneFileUploaded(finalTransferKey, DatabaseConstants.TRANSFER_STATUS_FAILURE, finalClientSessionId, filename);
//                                    } else {
//                                        // check if all files in the upload group uploaded successfully
//
//                                        if (finalUploadStatus.equals(DatabaseConstants.TRANSFER_STATUS_SUCCESS)) {
//                                            // check others uploads
//
//                                            List<String> uploadKeys = fileUploadGroup.getUploadKeys();
//
//                                            if (uploadKeys != null) {
//                                                int filesOriginalCount = uploadKeys.size();
//
//                                                if (filesOriginalCount > 0) {
//                                                    uploadKeys.remove(finalTransferKey);
//
//                                                    if (uploadKeys.size() > 0) {
//                                                        boolean allSuccess = true;
//
//                                                        for (String anotherUploadKey : uploadKeys) {
//                                                            // check if the status is success, if not, mark and break
//
//                                                            String anotherUploadStatus = fileUploadDao.findFileUploadStatusForUploadKey(anotherUploadKey);
//
//                                                            // DEBUG
////                                                            LOGGER.debug("Another upload status is " + anotherUploadStatus);
//
//                                                            if (anotherUploadStatus == null || !anotherUploadStatus.equals(DatabaseConstants.TRANSFER_STATUS_SUCCESS)) {
//                                                                allSuccess = false;
//
//                                                                break;
//                                                            }
//                                                        }
//
//                                                        if (allSuccess) {
//                                                            // DEBUG
////                                                            LOGGER.debug("All files uploads successfully.");
//
//                                                            // notify all success
//                                                            sendRemoteNotificationOnAllFilesUploadedSuccessfully(finalClientSessionId, filename, filesOriginalCount, uploadGroupId);
//                                                        }
//                                                    } else {
//                                                        // if there's only one upload and uploaded successfully, send notification directly
//                                                        sendRemoteNotificationOnOneFileUploaded(finalTransferKey, finalUploadStatus, finalClientSessionId, filename);
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                    }
//                });
//            }
//        }
    }

//    private void sendRemoteNotificationOnOneFileUploaded(final String uploadKey, final String uploadStatus, final String clientSessionId, final String filename) {
//        if (clientSessionId != null) {
//            String deviceToken = null;
//            String userId = null;
//
//            try {
//                ClientSession clientSession = clientSessionService.findClientSessionBySessionId(clientSessionId);
//
//                if (clientSession != null) {
//                    deviceToken = clientSession.getDeviceToken();
//                    userId = clientSession.getUserId();
//                    String clientLocale = clientSession.getLocale();
//
//                    // DEBUG
////                    LOGGER.info(String.format("[NOTIFICATION]User: %s, Device token: %s", userId, deviceToken));
//
//                    if (deviceToken != null && deviceToken.trim().length() > 0 && userId != null) {
//                        // FIX: Also make sure if the transfer key relates to some upload-group-id
//
////                        filename = fileUploadDao.findFileUploadFilenameForUploadKey(uploadKey);
//
//                        // DEBUG
////                        LOGGER.info(String.format("[NOTIFICATION]Filename: %s", filename));
//
//                        pushService.sendForOneFileUploaded(userId, deviceToken, clientLocale, filename, uploadKey, uploadStatus);
//                    } else {
//                        LOGGER.debug("Failed to send push notification because device token or user id is empty.\ndevice token: " + deviceToken + ", user id: " + userId);
//                    }
//                }
//            } catch (Exception e) {
//                // Catch this exception to prevent device not response.
//
//                LOGGER.error(String.format("Failed to push notification for file upload result.\nuser: %s\ndevice token: %s\nfilename: %s\ntransfer key: %s\ntransfer status: %s", userId, deviceToken, filename, uploadKey, uploadStatus), e);
//            }
//        }
//    }
//
//    private void sendRemoteNotificationOnAllFilesUploadedSuccessfully(String clientSessionId, String filename, int filesCount, String uploadGroupId) {
//        if (clientSessionId != null) {
//            String deviceToken = null;
//            String userId = null;
//
//            try {
//                ClientSession clientSession = clientSessionService.findClientSessionBySessionId(clientSessionId);
//
//                if (clientSession != null) {
//                    deviceToken = clientSession.getDeviceToken();
//                    userId = clientSession.getUserId();
//                    String clientLocale = clientSession.getLocale();
//
//                    // DEBUG
////                    LOGGER.info(String.format("[NOTIFICATION]User: %s, Device token: %s", userId, deviceToken));
//
//                    if (deviceToken != null && deviceToken.trim().length() > 0 && userId != null) {
//                        // FIX: Also make sure if the transfer key relates to some upload-group-id
//
////                        filename = fileUploadDao.findFileUploadFilenameForUploadKey(uploadKey);
//
//                        // DEBUG
////                        LOGGER.info(String.format("[NOTIFICATION]Filename: %s", filename));
//
//                        pushService.sendForAllFilesUploadedSuccessfully(userId, deviceToken, clientLocale, filename, filesCount, uploadGroupId);
//                    } else {
//                        LOGGER.debug("Failed to send push notification on all files uploaded succefully because device token or user id is empty.\ndevice token: " + deviceToken + ", user id: " + userId);
//                    }
//                }
//            } catch (Exception e) {
//                // Catch this exception to prevent device not response.
//
//                LOGGER.error(String.format("Failed to push notification on all files uploaded succefully.\nuser: %s\ndevice token: %s\nfilename: %s\nfiles count: %d", userId, deviceToken, filename, filesCount), e);
//            }
//        }
//    }

    private void onUploadFileGroupWebSocket(final Session session, String message) {
        UploadFileGroupWebSocketService uploadFileGroupWebSocketService = new UploadFileGroupWebSocketService(session, message, this);

        uploadFileGroupWebSocketService.onUploadFileGroupWebSocket();

//        // Get the result of creating file-upload group information in desktop, and update result to db and then return response
//
//        ObjectMapper mapper = Utility.createObjectMapper();
//
//        Integer status = null;
//        String uploadGroupId = null;
//        Long createdInDesktopTimestamp = null;
//        String createdInDesktopStatus = null;
//
//        try {
//            ResponseFileUploadGroupModel responseModel = mapper.readValue(message, ResponseFileUploadGroupModel.class);
//
//            status = responseModel.getStatus();
//            uploadGroupId = responseModel.getUploadGroupId();
//            createdInDesktopTimestamp = responseModel.getCreatedInDesktopTimestamp();
//            createdInDesktopStatus = responseModel.getCreatedInDesktopStatus();
//
//            if (status == null) {
//                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
//            }
//
//            if (uploadGroupId != null) {
//                if (status.equals(HttpServletResponse.SC_OK)) {
//                    if (createdInDesktopStatus == null || createdInDesktopStatus.trim().length() < 1) {
//                        createdInDesktopStatus = DatabaseConstants.CREATED_IN_DESKTOP_STATUS_FAILURE;
//                    }
//
//                    if (createdInDesktopTimestamp == null || createdInDesktopTimestamp < 1) {
//                        createdInDesktopTimestamp = System.currentTimeMillis();
//                    }
//
//                    fileUploadGroupDao.updateFileUploadGroupCreatedInDesktopStatus(uploadGroupId, createdInDesktopTimestamp, createdInDesktopStatus);
//                } else {
//                    // delete this file_upload_group and its related details
//                    fileUploadGroupDao.deleteFileUploadGroupById(uploadGroupId);
//                }
//            }
//
//            if (resp != null && !resp.isCommitted()) {
//                if (HttpServletResponse.SC_OK == status && uploadGroupId != null) {
//                    // desktop has saved the file successfully
//
//                    try {
//                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                        resp.setStatus(status);
//                        resp.getWriter().write(uploadGroupId);
//                        resp.getWriter().flush();
//                    } finally {
//                        closeLatch.countDown();
//                    }
//
//                    LOGGER.debug(String.format("File upload summary created successfully. File upload group id: %s", uploadGroupId));
//                } else {
//                    try {
//                        String error = responseModel.getError();
//
//                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                        resp.setStatus(status);
//                        resp.getWriter().write(error != null ? error : "");
//                        resp.getWriter().flush();
//                    } finally {
//                        closeLatch.countDown();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            String errorMessage = String.format("Error on processing response message of service 'create file upload summary'. Message from desktop: %s; error message: %s", message, e.getMessage());
//
//            LOGGER.error(errorMessage);
//
//            if (status == null) {
//                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
//            }
//
//            // update status if transfer key is not null
//
//            if (uploadGroupId != null) {
//                if (status.equals(HttpServletResponse.SC_OK)) {
//                    if (createdInDesktopStatus == null || createdInDesktopStatus.trim().length() < 1) {
//                        createdInDesktopStatus = DatabaseConstants.CREATED_IN_DESKTOP_STATUS_FAILURE;
//                    }
//
//                    if (createdInDesktopTimestamp == null || createdInDesktopTimestamp < 1) {
//                        createdInDesktopTimestamp = System.currentTimeMillis();
//                    }
//
//                    fileUploadGroupDao.updateFileUploadGroupCreatedInDesktopStatus(uploadGroupId, createdInDesktopTimestamp, createdInDesktopStatus);
//                } else {
//                    // delete this file_upload_group and its related details
//                    fileUploadGroupDao.deleteFileUploadGroupById(uploadGroupId);
//                }
//            }
//
//            try {
//                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                resp.setStatus(status);
//                resp.getWriter().write(errorMessage);
//                resp.getWriter().flush();
//            } catch (Exception e1) {
//                // ignored
//            } finally {
//                closeLatch.countDown();
//            }
//        }
    }

    private void onDeleteFileUploadWebSocket(final Session session, String message) {
        // Get the result of deleting file-upload information in desktop, and then return response

        ObjectMapper mapper = Utility.createObjectMapper();

        Integer status = null;

        try {
            ResponseModel responseModel = mapper.readValue(message, ResponseModel.class);

            status = responseModel.getStatus();

            if (status == null) {
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }

            if (resp != null && !resp.isCommitted()) {
                if (HttpServletResponse.SC_OK == status) {
                    // desktop has saved the file successfully

                    try {
                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(status);
                        resp.getWriter().write("success");
                        resp.getWriter().flush();
                    } finally {
                        closeLatch.countDown();
                    }

                    LOGGER.debug("Delete file upload successfully.");
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
            String errorMessage = String.format("Error on processing response message of service 'delete file upload'. Message from desktop: %s; error message: %s", message, e.getMessage());

            LOGGER.error(errorMessage);

            if (status == null) {
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
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
    } // end onDeleteFileUploadWebSocket(Session, String)

    private void onDownloadFileGroupWebSocket(final Session session, String message) {
        DownloadFileGroupWebSocketService downloadFileGroupWebSocketService = new DownloadFileGroupWebSocketService(session, message, this);

        downloadFileGroupWebSocketService.onDownloadFileGroupWebSocket();

//        // Get the result of validating file paths of the file-download group information, and return response
//
//        ObjectMapper mapper = Utility.createObjectMapper();
//
//        Integer status = null;
//        String downloadGroupId = null;
//
//        try {
//            ResponseFileDownloadGroupModel responseModel = mapper.readValue(message, ResponseFileDownloadGroupModel.class);
//
//            status = responseModel.getStatus();
//            downloadGroupId = responseModel.getDownloadGroupId();
//
//            if (status == null) {
//                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
//            }
//
//            if (!status.equals(HttpServletResponse.SC_OK) && downloadGroupId != null) {
//                // delete this file_download_group and its related details
//
//                fileDownloadGroupDao.deleteFileDownloadGroupById(downloadGroupId);
//            }
//
//            if (resp != null && !resp.isCommitted()) {
//                if (HttpServletResponse.SC_OK == status && downloadGroupId != null) {
//                    // desktop has saved the file successfully
//
//                    try {
//                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                        resp.setStatus(status);
//                        resp.getWriter().write(downloadGroupId);
//                        resp.getWriter().flush();
//                    } finally {
//                        closeLatch.countDown();
//                    }
//
//                    LOGGER.debug(String.format("File download summary created successfully. File download group id: %s", downloadGroupId));
//                } else {
//                    try {
//                        String error = responseModel.getError();
//
//                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                        resp.setStatus(status);
//                        resp.getWriter().write(error != null ? error : "");
//                        resp.getWriter().flush();
//                    } finally {
//                        closeLatch.countDown();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            String errorMessage = String.format("Error on processing response message of service 'create file download summary'. Message from desktop: %s; error message: %s", message, e.getMessage());
//
//            LOGGER.error(errorMessage);
//
//            if (status == null) {
//                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
//            }
//
//            // update status if transfer key is not null
//
//            if (!status.equals(HttpServletResponse.SC_OK) && downloadGroupId != null) {
//                // delete this file_download_group and its related details
//
//                fileDownloadGroupDao.deleteFileDownloadGroupById(downloadGroupId);
//            }
//
//            try {
//                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                resp.setStatus(status);
//                resp.getWriter().write(errorMessage);
//                resp.getWriter().flush();
//            } catch (Exception e1) {
//                // ignored
//            } finally {
//                closeLatch.countDown();
//            }
//        }
    }

    private void onDeleteBookmarkByIdWebSocket(final Session session, String message) {
        /* requested to delete the bookmark by id */

        if (resp != null) {
            try {
                ObjectMapper mapper = Utility.createObjectMapper();

                ResponseBookmarkModel responseModel = mapper.readValue(message, ResponseBookmarkModel.class);

                Integer status = responseModel.getStatus();

                if (status != null && HttpServletResponse.SC_OK == status) {
                    Bookmark bookmark = responseModel.getBookmark();

                    String rootsJson = mapper.writeValueAsString(bookmark);

                    /* DEBUG */
                    LOGGER.debug(String.format("Prepared bookmark information to response for service deleteBookmarkById: %s", rootsJson));

                    resp.setHeader(Constants.HTTP_HEADER_NAME_CHANGE_TIMESTAMP, String.valueOf(responseModel.getTimestamp()));

                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                    resp.setStatus(status);
                    resp.getWriter().write(rootsJson);
                    resp.getWriter().flush();
                } else {
                    String error = responseModel.getError();

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(error);
                    resp.getWriter().flush();
                }
            } catch (Exception e) {
                String errorMessage = String.format("Error on processing response message of service 'deleteBookmarkById'. Message from desktop: %s; error message: %s", message, e.getMessage());

                LOGGER.error(errorMessage);

                try {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } catch (Exception e1) {
                    /* ignored */
                }
            } finally {
                closeLatch.countDown();
            }
        } else {
            LOGGER.error("HttpServletResponse must be assigned when initiate ConnectSocket!");
        }
    } // end onDeleteBookmarkByIdWebSocket(Session, String)

    private void onCreateBookmarkWebSocket(final Session session, String message) {
        /* requested to create one bookmark */

        if (resp != null) {
            try {
                ObjectMapper mapper = Utility.createObjectMapper();

                ResponseBookmarkModel responseModel = mapper.readValue(message, ResponseBookmarkModel.class);

                Integer status = responseModel.getStatus();

                if (status != null && HttpServletResponse.SC_OK == status) {
                    Bookmark bookmark = responseModel.getBookmark();

                    String rootsJson = mapper.writeValueAsString(bookmark);

                    /* DEBUG */
                    LOGGER.debug(String.format("Prepared bookmark information to response for service createBookmark: %s", rootsJson));

                    resp.setHeader(Constants.HTTP_HEADER_NAME_CHANGE_TIMESTAMP, String.valueOf(responseModel.getTimestamp()));

                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                    resp.setStatus(status);
                    resp.getWriter().write(rootsJson);
                    resp.getWriter().flush();
                } else {
                    String error = responseModel.getError();

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(error);
                    resp.getWriter().flush();
                }
            } catch (Exception e) {
                String errorMessage = String.format("Error on processing response message of service 'createBookmark'. Message from desktop: %s; error message: %s", message, e.getMessage());

                LOGGER.error(errorMessage);

                try {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } catch (Exception e1) {
                    /* ignored */
                }
            } finally {
                closeLatch.countDown();
            }
        } else {
            LOGGER.error("HttpServletResponse must be assigned when initiate ConnectSocket!");
        }
    } // end onCreateBookmarkWebSocket(Session, String)

    private void onSynchronizeBookmarksWebSocket(final Session session, String message) {
        /* requested to synchronize bookmarks */

        if (resp != null) {
            try {
                ObjectMapper mapper = Utility.createObjectMapper();

                ResponseBookmarkArrayModel responseModel = mapper.readValue(message, ResponseBookmarkArrayModel.class);

                Integer status = responseModel.getStatus();

                if (status != null && HttpServletResponse.SC_OK == status) {
                    List<Bookmark> allBookmarks = responseModel.getBookmarks();

                    String rootsJson = mapper.writeValueAsString(allBookmarks);

                    /* DEBUG */
                    LOGGER.debug(String.format("Prepared bookmark information to response for service synchronizeBookmarks: %s", rootsJson));

                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                    resp.setStatus(status);
                    resp.getWriter().write(rootsJson);
                    resp.getWriter().flush();
                } else {
                    String error = responseModel.getError();

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(error);
                    resp.getWriter().flush();
                }
            } catch (Exception e) {
                String errorMessage = String.format("Error on processing response message of service 'synchronizeBookmarks'. Message from desktop: %s; error message: %s", message, e.getMessage());

                LOGGER.error(errorMessage);

                try {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } catch (Exception e1) {
                    /* ignored */
                }
            } finally {
                closeLatch.countDown();
            }
        } else {
            LOGGER.error("HttpServletResponse must be assigned when initiate ConnectSocket!");
        }
    } // end onSynchronizeBookmarksWebSocket(Session, String)

    private void onUpdateBookmarkWebSocket(final Session session, String message) {
        /* requested to update one bookmark */

        if (resp != null) {
            try {
                ObjectMapper mapper = Utility.createObjectMapper();

                ResponseBookmarkModel responseModel = mapper.readValue(message, ResponseBookmarkModel.class);

                Integer status = responseModel.getStatus();

                if (status != null && HttpServletResponse.SC_OK == status) {
                    Bookmark bookmark = responseModel.getBookmark();

                    String rootsJson = mapper.writeValueAsString(bookmark);

                    /* DEBUG */
                    LOGGER.debug(String.format("Prepared bookmark information to response for service updateBookmark: %s", rootsJson));

                    resp.setHeader(Constants.HTTP_HEADER_NAME_CHANGE_TIMESTAMP, String.valueOf(responseModel.getTimestamp()));

                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                    resp.setStatus(status);
                    resp.getWriter().write(rootsJson);
                    resp.getWriter().flush();
                } else {
                    String error = responseModel.getError();

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(error);
                    resp.getWriter().flush();
                }
            } catch (Exception e) {
                String errorMessage = String.format("Error on processing response message of service 'updateBookmark'. Message from desktop: %s; error message: %s", message, e.getMessage());

                LOGGER.error(errorMessage);

                try {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } catch (Exception e1) {
                    /* ignored */
                }
            } finally {
                closeLatch.countDown();
            }
        } else {
            LOGGER.error("HttpServletResponse must be assigned when initiate ConnectSocket!");
        }
    } // end onUpdateBookmarkWebSocket(Session, String)

    private void onFindFileByPathWebSocket(final Session session, String message) {
        FindByPathWebSocketService findByPathWebSocketService = new FindByPathWebSocketService(session, message, this);

        findByPathWebSocketService.onFindFileByPathWebSocket();

//        /* requested to find the information of the specified file or directory */
//
//        if (resp != null) {
//            try {
//                ObjectMapper mapper = Utility.createObjectMapper();
//
//                ResponseFindFileByPathModel responseModel = mapper.readValue(message, ResponseFindFileByPathModel.class);
//
//                Integer status = responseModel.getStatus();
//
//                if (status != null && HttpServletResponse.SC_OK == status) {
//                    HierarchicalModel model = responseModel.getResult();
//
//                    String rootsJson = mapper.writeValueAsString(model);
//
//                    /* DEBUG */
//                    LOGGER.debug(String.format("Prepared file or directory information to response for service findFileByPath: %s", rootsJson));
//
//                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
//                    resp.setStatus(status);
//                    resp.getWriter().write(rootsJson);
//                    resp.getWriter().flush();
//                } else {
//                    String error = responseModel.getError();
//
//                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                    resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                    resp.getWriter().write(error);
//                    resp.getWriter().flush();
//                }
//            } catch (Exception e) {
//                String errorMessage = String.format("Error on processing response message of service 'findFileByPath'. Message from desktop: %s; error message: %s", message, e.getMessage());
//
//                LOGGER.error(errorMessage);
//
//                try {
//                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                    resp.getWriter().write(errorMessage);
//                    resp.getWriter().flush();
//                } catch (Exception e1) {
//                    /* ignored */
//                }
//            } finally {
//                closeLatch.countDown();
//            }
//        } else {
//            LOGGER.error("HttpServletResponse must be assigned when initiate ConnectSocket!");
//        }
    }

    private void onFileRenameWebSocket(final Session session, String message) {
        /* response to rename file or directory */

        if (resp != null) {
            try {
                ObjectMapper mapper = Utility.createObjectMapper();

                ResponseFileRenameModel responseModel = mapper.readValue(message, ResponseFileRenameModel.class);

                Integer status = responseModel.getStatus();

                if (status != null && HttpServletResponse.SC_OK == status) {
                    FileRenameModel model = responseModel.getResult();

                    String rootsJson = mapper.writeValueAsString(model);

                    /* DEBUG */
                    LOGGER.debug(String.format("Prepared file or directory information to response for service findFileByPath: %s", rootsJson));

                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                    resp.setStatus(status);
                    resp.getWriter().write(rootsJson);
                    resp.getWriter().flush();
                } else {
                    String error = responseModel.getError();

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(error);
                    resp.getWriter().flush();
                }
            } catch (Exception e) {
                String errorMessage = String.format("Error on processing response message of service 'findFileByPath'. Message from desktop: %s; error message: %s", message, e.getMessage());

                LOGGER.error(errorMessage);

                try {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } catch (Exception e1) {
                    /* ignored */
                }
            } finally {
                closeLatch.countDown();
            }
        } else {
            LOGGER.error("HttpServletResponse must be assigned when initiate ConnectSocket!");
        }
    } // end onFileRenameWebSocket(Session, String)

    private void onListDirectoryChildrenWebSocket(final Session session, String message) {
        ListDirectoryChildrenWebSocketService listDirectoryChildrenWebSocketService = new ListDirectoryChildrenWebSocketService(session, message, this);

        listDirectoryChildrenWebSocketService.onListDirectoryChildrenWebSocket();

//        /* receive message from server with directory children information */
//
//        if (resp != null) {
//            try {
//                ObjectMapper mapper = Utility.createObjectMapper();
//
//                ResponseListDirectoryChildrenModel responseModel = mapper.readValue(message, ResponseListDirectoryChildrenModel.class);
//
//                Integer status = responseModel.getStatus();
//
//                if (status != null && HttpServletResponse.SC_OK == status) {
//                    List<HierarchicalModel> children = responseModel.getChildren();
//
//                    if (children == null) {
//                        children = new ArrayList<>();
//                    }
//
//                    String rootsJson = mapper.writeValueAsString(children);
//
//                    /* DEBUG */
//                    LOGGER.debug(String.format("Prepared directory children to response for service listDirectoryChildren: %s", rootsJson));
//
//                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
//                    resp.setStatus(status);
//                    resp.getWriter().write(rootsJson);
//                    resp.getWriter().flush();
//                } else {
//                    String error = responseModel.getError();
//
//                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                    resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                    resp.getWriter().write(error);
//                    resp.getWriter().flush();
//                }
//            } catch (Exception e) {
//                String errorMessage = String.format("Error on processing response message of service 'listDirectoryChildren'. Message from desktop: %s; error message: %s", message, e.getMessage());
//
//                LOGGER.error(errorMessage);
//
//                try {
//                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                    resp.getWriter().write(errorMessage);
//                    resp.getWriter().flush();
//                } catch (Exception e1) {
//                    /* ignored */
//                }
//            } finally {
//                closeLatch.countDown();
//            }
//        } else {
//            LOGGER.error("HttpServletResponse must be assigned when initiate ConnectSocket!");
//        }
    }

//    private void onListBookmarksAndRootsWebSocket(final Session session, String message) {
//        /* receive message from server with bookmarks and roots information */
//
//        if (resp != null) {
//            try {
//                ObjectMapper mapper = Utility.createObjectMapper();
//
//                ResponseListBookmarksAndRootsModel receivedModel = mapper.readValue(message, ResponseListBookmarksAndRootsModel.class);
//
//                Integer status = receivedModel.getStatus();
//
//                if (status != null && HttpServletResponse.SC_OK == status) {
//                    List<Bookmark> bookmarks = receivedModel.getBookmarks();
//
//                    if (bookmarks == null) {
//                        bookmarks = new ArrayList<>();
//                    }
//
//                    ArrayNode bookmarkNodes = mapper.createArrayNode();
//
//                    for (Bookmark bookmark : bookmarks) {
//                        ObjectNode bookmarkNode = mapper.createObjectNode();
//                        bookmarkNode.put("id", bookmark.getId());
//                        bookmarkNode.put("label", bookmark.getLabel());
//                        bookmarkNode.put("path", bookmark.getPath());
//                        bookmarkNode.put("type", bookmark.getType().toString());
//
//                        bookmarkNodes.add(bookmarkNode);
//                    }
//
//                    List<String> roots = receivedModel.getRoots();
//
//                    if (roots == null) {
//                        roots = new ArrayList<>();
//                    }
//
//                    ArrayNode rootNodes = mapper.createArrayNode();
//
//                    for (String root : roots) {
//                        rootNodes.add(root);
//                    }
//
//                    ObjectNode responseNode = mapper.createObjectNode();
//                    responseNode.put("bookmarks", bookmarkNodes);
//                    responseNode.put("roots", rootNodes);
//
//                    String responseJson = mapper.writeValueAsString(responseNode);
//
//                    /* DEBUG */
//                    LOGGER.debug(String.format("Prepared bookmarks and roots to response for service listBookmarksAndRoots: %s", responseJson));
//
//                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
//                    resp.setStatus(status);
//                    resp.getWriter().write(responseJson);
//                    resp.getWriter().flush();
//                } else {
//                    String error = receivedModel.getError();
//
//                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                    resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                    resp.getWriter().write(error);
//                    resp.getWriter().flush();
//                }
//            } catch (Exception e) {
//                String errorMessage = String.format("Error on processing response message of service 'listBookmarksAndRoots'. Message from desktop: %s; error message: %s", message, e.getMessage());
//
//                LOGGER.error(errorMessage);
//
//                try {
//                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                    resp.getWriter().write(errorMessage);
//                    resp.getWriter().flush();
//                } catch (Exception e1) {
//                    /* ignored */
//                }
//            } finally {
//                closeLatch.countDown();
//            }
//        } else {
//            LOGGER.error("HttpServletResponse must be assigned when initiate ConnectSocket!");
//        }
//    } // end onListBookmarksAndRootsWebSocket(Session, String)

    private void onListAllBookmarksWebSocket(final Session session, String message) {
        /* receive message from server with bookmarks information */

        if (resp != null) {
            try {
                ObjectMapper mapper = Utility.createObjectMapper();

                ResponseBookmarkArrayModel responseModel = mapper.readValue(message, ResponseBookmarkArrayModel.class);

                Integer status = responseModel.getStatus();

                if (status != null && HttpServletResponse.SC_OK == status) {
                    List<Bookmark> bookmarks = responseModel.getBookmarks();

                    if (bookmarks == null) {
                        bookmarks = new ArrayList<>();
                    }

                    String bookmarksJson = mapper.writeValueAsString(bookmarks);

                    /* DEBUG */
                    LOGGER.debug(String.format("Prepared bookmarks to response for service findAllBookmarks: %s", bookmarksJson));

                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                    resp.setStatus(status);
                    resp.getWriter().write(bookmarksJson);
                    resp.getWriter().flush();
                } else {
                    String error = responseModel.getError();

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(error);
                    resp.getWriter().flush();
                }
            } catch (Exception e) {
                String errorMessage = String.format("Error on processing response message of service 'findAllBookmarks'. Message from desktop: %s; error message: %s", message, e.getMessage());

                LOGGER.error(errorMessage);

                try {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } catch (Exception e1) {
                    /* ignored */
                }
            } finally {
                closeLatch.countDown();
            }
        } else {
            LOGGER.error("HttpServletResponse must be assigned when initiate ConnectSocket!");
        }
    } // end onListAllBookmarksWebSocket(Session, String)

//    private void processOnMessageException(Session session, Integer sid, Exception e, int httpStatusCode, boolean needCloseAndDisconnect) {
//        String errorMessage = String.format("Error on processing received message.%n%s%n%s%n", e.getClass().getName(), e.getMessage());
//
//        ResponseModel responseModel = new ResponseModel(sid != null ? sid : null, httpStatusCode, errorMessage, userId, System.currentTimeMillis());
//
//        try {
//            ObjectMapper mapper = Utility.createObjectMapper();
//
//            Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//        } catch (Exception e1) {
//            /* ignored */
//        } finally {
//            if (needCloseAndDisconnect) {
//                closeSessionWithBadDataStatusCode(session, errorMessage);
//            }
//        }
//    }

//    private void processOnIncompatibleVersionMessageException(Session session, Integer sid, String errorMessage, int httpStatusCode, boolean needCloseAndDisconnect) {
//        ResponseModelWithoutClientSessionId responseModel = new ResponseModelWithoutClientSessionId(sid != null ? sid : null, httpStatusCode, errorMessage, userId, System.currentTimeMillis());
//
//        try {
//            ObjectMapper mapper = Utility.createObjectMapper();
//
//            Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//        } catch (Exception e1) {
//            /* ignored */
//        } finally {
//            if (needCloseAndDisconnect) {
//                closeSessionWithBadDataStatusCode(session, errorMessage);
//            }
//        }
//    }

    private void onConnectWebSocket(final Session session, String message) {
        /* process connection request from server and response to server */

        ConnectWebSocketService connectWebSocketService = new ConnectWebSocketService(session, message, this);

        connectWebSocketService.messageReceived();

//        try {
//            ObjectMapper mapper = Utility.createObjectMapper();
//
//            ConnectModel connectModel = mapper.readValue(message, ConnectModel.class);
//
//            // throw incompatible version exception on desktop version less than 1.0.5
//            processOnDesktopLessThanVersion_1_0_5(session, connectModel);
//
//            if (connectModel.getAdminAccount() != null && connectModel.getAdminAccount().trim().length() > 0) {
//                onComputerUserConnectWebSocket(session, mapper, connectModel);
//            } else {
//                onComputerAdminConnectWebSocket(session, mapper, connectModel);
//            }
//        } catch (IncompatibleVersionException e) {
//            int httpStatusCode = Constants.HTTP_STATUS_INCOMPATIBLE_VERSION;
//
//            processOnIncompatibleVersionMessageException(session, Sid.CONNECT, e.getMessage(), httpStatusCode, true);
//        } catch (JsonProcessingException e) {
//            int httpStatusCode = HttpServletResponse.SC_BAD_REQUEST;
//
//            processOnMessageException(session, Sid.CONNECT, e, httpStatusCode, true);
//        } catch (SQLException e) {
//            int httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
//
//            processOnMessageException(session, Sid.CONNECT, e, httpStatusCode, false);
//        } catch (Exception e) {
//            int httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
//
//            processOnMessageException(session, Sid.CONNECT, e, httpStatusCode, true);
//        }
    }

    private void onConnectFromComputerWebSocket(final Session session, String message) {
        ConnectWebSocketService connectWebSocketService = new ConnectWebSocketService(session, message, this);

        connectWebSocketService.messageReceivedV2();
    }

//    private void processOnDesktopLessThanVersion_1_0_5(final Session session, ConnectModel connectModel) throws IncompatibleVersionException {
//        if (connectModel != null && connectModel.getProperties() != null) {
//            Properties properties = connectModel.getProperties();
//
//            String desktopVersion = (String) properties.get(PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);
//
//            if (desktopVersion != null
//                && Version.valid(desktopVersion)
//                && new Version(desktopVersion).compareTo(new Version("1.0.5")) < 0) {
//                String errorMessage = ClopuccinoMessages.localizedMessage(connectModel.getLocale(), "update.software.first");
//                throw new IncompatibleVersionException(errorMessage);
//            }
//        }
//    }
//
//    private void onComputerUserConnectWebSocket(final Session session, ObjectMapper mapper, ConnectModel connectModel) throws Exception {
//        /* process connection request from desktop administrator */
//
//        final String userId = connectModel.getAccount();
//        final String adminUserId = connectModel.getAdminAccount();
//        final String adminPassword = connectModel.getPassword();
//        final String adminNickname = connectModel.getNickname();
//        final String verification = connectModel.getVerification();
//
//        final Long computerId = connectModel.getComputerId();
//
//        final String localeString = connectModel.getLocale();
//        String lugServerId = connectModel.getLugServerId();
//
//        this.clientLocale = ClopuccinoMessages.getLocaleFromJavaLocaleString(localeString);
//
//        if (userId == null || adminPassword == null || adminNickname == null || verification == null || computerId == null
//            || userId.trim().length() < 1 || adminPassword.trim().length() < 1 || adminNickname.trim().length() < 1 || verification.trim().length() < 1) {
//            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");
//            ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_BAD_REQUEST, errorMessage, null, System.currentTimeMillis());
//
//            Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//            closeSessionWithBadDataStatusCode(session, errorMessage);
//        } else {
//            // checking if computer exists must be prior than checking if user exists
//
//            Computer computer = computerDao.findComputerById(computerId);
//
//            if (computer == null) {
//                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "computer.not.found");
//
//                ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_COMPUTER_NOT_FOUND, errorMessage, userId, System.currentTimeMillis());
//
//                Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                closeSessionWithBadDataStatusCode(session, errorMessage);
//            } else {
//                User user = userDao.findUserById(userId);
//
//                if (user == null) {
//                    /* user not exists */
//                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");
//
//                    ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_FORBIDDEN, errorMessage, userId, System.currentTimeMillis());
//
//                    Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                    future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                    closeSessionWithBadDataStatusCode(session, errorMessage);
//                } else {
//                    /* if verified */
//                    Boolean verified = user.getVerified();
//
//                    if (verified == null || !verified) {
//                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "account.not.verified", user.getPhoneNumber());
//
//                        ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_UNAUTHORIZED, errorMessage, userId, System.currentTimeMillis());
//
//                        Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                        future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                        closeSessionWithBadDataStatusCode(session, errorMessage);
//                    } else if (user.getShouldUpdatePhoneNumber()) {
//                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.should.update.phone.number.from.device", user.getPhoneNumber());
//
//                        ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_SHOULD_UPDATE_PHONE_NUMBER, errorMessage, userId, System.currentTimeMillis());
//
//                        Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                        future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                        closeSessionWithBadDataStatusCode(session, errorMessage);
//                    } else {
//                        /* validate with admin password */
//                        User admin = userDao.findUserById(adminUserId);
//
//                        if (admin == null) {
//                            /* admin not exists */
//
//                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");
//
//                            ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_FORBIDDEN, errorMessage, userId, System.currentTimeMillis());
//
//                            Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                            closeSessionWithBadDataStatusCode(session, errorMessage);
//                        } else {
//                            /* if admin verified */
//
//                            Boolean adminVerified = admin.getVerified();
//
//                            if (adminVerified == null || !adminVerified) {
//                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "account.not.verified", admin.getPhoneNumber());
//
//                                ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_FORBIDDEN, errorMessage, userId, System.currentTimeMillis());
//
//                                Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                                future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                                closeSessionWithBadDataStatusCode(session, errorMessage);
//                            } else {
//                                /* validate with admin password */
//
//                                String foundPasswd = admin.getPasswd();
//
//                                if (adminPassword.equals(foundPasswd)) {
//                                    // Checking verification code after checking password
//                                    if (!verification.equals(Utility.generateVerification(adminUserId, adminPassword, adminNickname))) {
//                                        LOGGER.warn("User: " + userId + " is testing verification code for connect");
//
//                                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");
//                                        ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage, null, System.currentTimeMillis());
//
//                                        Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                                        future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                                        closeSessionWithBadDataStatusCode(session, errorMessage);
//                                    } else if (!adminUserId.equals(computer.getUserId())) { // make sure the admin of the computer is the correct one
//                                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.computer.admin", computer.getComputerName(), admin.getNickname());
//
//                                        ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_USER_NOT_ADMIN, errorMessage, userId, null, System.currentTimeMillis());
//
//                                        Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                                        future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                                        closeSessionWithBadDataStatusCode(session, errorMessage);
//                                    } else if (admin.getShouldUpdatePhoneNumber()) {
//                                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.should.update.phone.number.from.device", admin.getPhoneNumber());
//
//                                        ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_SHOULD_UPDATE_PHONE_NUMBER, errorMessage, adminUserId, null, System.currentTimeMillis());
//
//                                        Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                                        future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                                        closeSessionWithBadDataStatusCode(session, errorMessage);
//                                    } else {
//                                        /* check apply-connection */
//
//                                        String computerAdminId = computer.getUserId();
//
//                                        ApplyConnection applyConnection = applyConnectionDao.findApplyConnectionByApplyUserAndComputerId(userId, computerId);
//
//                                        if (applyConnection == null) {
//                                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.apply.connection", user.getNickname(), computer.getComputerName());
//
//                                            ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_USER_NOT_APPLY_CONNECTION_YET, errorMessage, userId, System.currentTimeMillis());
//
//                                            Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                                            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                                            closeSessionWithBadDataStatusCode(session, errorMessage);
//                                        } else if (applyConnection.getApproved() != null && !applyConnection.getApproved()) {
//                                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "apply.connection.not.approved.yet", user.getNickname(), computer.getComputerName());
//
//                                            ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_APPLY_CONNECTION_NOT_APPROVED_YET, errorMessage, userId, System.currentTimeMillis());
//
//                                            Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                                            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                                            closeSessionWithBadDataStatusCode(session, errorMessage);
//                                        } else {
//                                            /* TODO: update the version and build of the computer */
//
//                                            String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);
//
//                                            /* create linkage between this instance and user, session */
//
//                                            this.userId = userId;
//                                            this.userComputerId = userComputerId;
//
//                                            updateLastAccessTimeToNow();
//
//                                            if (lugServerId == null || lugServerId.trim().length() < 1) {
//                                                lugServerId = Constants.AA_SERVER_ID_AS_LUG_SERVER;
//
//                                                LOGGER.warn("Use AA server as lug server because empty lug server id received from desktop of user: " + userId + ", computer id: " + computerId);
//                                            }
//
//                                            /* disconnect old socket, if any */
//                                            try {
//                                                ConnectSocket oldSocket = ConnectSocket.getInstance(userComputerId);
//
//                                                if (oldSocket != null) {
//                                                    removeInstance(userComputerId);
//
//                                                    oldSocket.getSession().disconnect();
//
//                                                    LOGGER.debug("Disconnect old socket of user computer: " + userComputerId);
//                                                }
//                                            } catch (Exception e) {
//                                                /* ignored */
//                                            }
//
//                                            /* MAKE SURE THAT
//                                             * dealing with user-computer first before user-computer-properties
//                                             * so data wirtten to user-computer-properties will not fail because of foreign key user-computer-id not found
//                                             */
//
//                                            /* create linkage between this instance and userId */
//                                            putInstance(userComputerId, ConnectSocket.this);
//                                            /* create/update user computer in DB */
//                                            UserComputer currentUserComputer = userComputerDao.findUserComputerById(userComputerId);
//
//                                            if (currentUserComputer == null) {
//                                                String encryptedUserComputerId = Utility.generateEncryptedUserComputerIdFrom(userId, computerId);
//
//                                                // for non-admin user, the default value to the allow-alias is false.
//                                                UserComputer userComputer = new UserComputer(userComputerId, userId, computerId, computerAdminId, null, null, encryptedUserComputerId, lugServerId, true, false, false);
//
//                                                userComputerDao.createUserComputer(userComputer);
//                                            } else {
//                                                currentUserComputer.setLugServerId(lugServerId);
//                                                currentUserComputer.setSocketConnected(true);
//                                                currentUserComputer.setNeedReconnect(false);
//
//                                                userComputerDao.updateUserComputerConnectionStatus(currentUserComputer);
//                                            }
//
//                                            /* delete first regardless of any newly-coming properties */
//                                            userComputerPropertiesDao.deletePropertiesByUserComputerId(userComputerId);
//
//                                            Properties properties = connectModel.getProperties();
//
//                                            boolean notifyUpdate = false;
//
//                                            String currentDesktopVersion = null;
//                                            String latestDesktopVersion = null;
//
//                                            if (properties != null && properties.size() > 0) {
//                                                currentDesktopVersion = properties.getProperty(PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);
//
//                                                if (currentDesktopVersion != null && Version.valid(currentDesktopVersion)) {
//                                                    latestDesktopVersion = filelugPropertiesDao.findValueByKey(Constants.FILELUG_PROPERTY_KEY_DESKTOP_LATEST_VERSION);
//
//                                                    if (Version.valid(latestDesktopVersion)) {
//                                                        Version currentVersion = new Version(currentDesktopVersion);
//                                                        Version latestVersion = new Version(latestDesktopVersion);
//
//                                                        notifyUpdate = latestVersion.compareTo(currentVersion) > 0;
//                                                    }
//                                                }
//
//                                                userComputerPropertiesDao.createUserComputerProperties(userComputerId, properties);
//                                            }
//
//                                            boolean allowAlias = userComputerDao.findAllowAliasById(userComputerId);
//
//                                            ApprovedUserModel approvedUserModel = new ApprovedUserModel(userId, user.getCountryId(), user.getPhoneNumber(), user.getNickname(), user.getShowHidden(), allowAlias);
//
//                                            ResponseUserModel responseModel = new ResponseUserModel(Sid.CONNECT, HttpServletResponse.SC_OK, null, userId, System.currentTimeMillis(), approvedUserModel);
//
//                                            session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                                            if (notifyUpdate) {
//                                                String downloadUrl = filelugPropertiesDao.findValueByKey(Constants.FILELUG_PROPERTY_KEY_DESKTOP_DOWNLOAD_URL);
//
//                                                if (downloadUrl == null || downloadUrl.trim().length() < 1) {
//                                                    downloadUrl = Constants.DEFAULT_DESKTOP_DOWNLOAD_URL;
//                                                }
//
//                                                RequestVersionModel requestVersionModel = new RequestVersionModel(Sid.NEW_SOFTWARE_NOTIFY, userId, ClopuccinoMessages.DEFAULT_LOCALE_STRING, currentDesktopVersion, latestDesktopVersion, downloadUrl);
//
//                                                session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(requestVersionModel));
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    /* password not correct */
//
//                                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "password.not.correct", adminUserId);
//
//                                    ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_UNAUTHORIZED, errorMessage, adminUserId, System.currentTimeMillis());
//
//                                    Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                                    future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                                    closeSessionWithBadDataStatusCode(session, errorMessage);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    } // end onComputerUserConnectWebSocket(Session, ObjectMapper, ConnectModel)
//
//    private void onComputerAdminConnectWebSocket(final Session session, ObjectMapper mapper, ConnectModel connectModel) throws Exception {
//        /* process connection request from desktop admin */
//
//        final String userId = connectModel.getAccount();
//        final String encryptedPassword = connectModel.getPassword();
//        final String nickname = connectModel.getNickname();
//        final String verification = connectModel.getVerification();
//        final Long computerId = connectModel.getComputerId();
//        final String localeString = connectModel.getLocale();
//        String lugServerId = connectModel.getLugServerId();
//
//        this.clientLocale = ClopuccinoMessages.getLocaleFromJavaLocaleString(localeString);
//
//        if (userId == null || encryptedPassword == null || nickname == null || verification == null || computerId == null
//            || userId.trim().length() < 1 || encryptedPassword.trim().length() < 1 || nickname.trim().length() < 1 || verification.trim().length() < 1) {
//            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");
//            ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_BAD_REQUEST, errorMessage, null, System.currentTimeMillis());
//
//            Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//            closeSessionWithBadDataStatusCode(session, errorMessage);
//        } else {
//            // checking if computer exists must be prior than checking if user exists
//
//            Computer computer = computerDao.findComputerById(computerId);
//
//            if (computer == null) {
//                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "computer.not.found");
//
//                ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_COMPUTER_NOT_FOUND, errorMessage, userId, System.currentTimeMillis());
//
//                Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                closeSessionWithBadDataStatusCode(session, errorMessage);
//            } else {
//                User user = userDao.findUserById(userId);
//
//                if (user == null) {
//                    /* user not exists */
//                    String errorMessage = "'" + userId + "' not registered yet. You should register this account to respository first.";
//
//                    ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_FORBIDDEN, errorMessage, userId, System.currentTimeMillis());
//
//                    Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                    future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                    closeSessionWithBadDataStatusCode(session, errorMessage);
//                } else {
//                    /* if verified */
//                    Boolean verified = user.getVerified();
//
//                    if (verified == null || !verified) {
//                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "account.not.verified", user.getPhoneNumber());
//
//                        ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_UNAUTHORIZED, errorMessage, userId, System.currentTimeMillis());
//
//                        Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                        future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                        closeSessionWithBadDataStatusCode(session, errorMessage);
//                    } else {
//                        /* validate with password */
//                        String passwordInResp = user.getPasswd();
//
//                        if (encryptedPassword.equals(passwordInResp)) {
//                            // Check verification code after checking password
//                            if (!verification.equals(Utility.generateVerification(userId, encryptedPassword, nickname))) {
//                                LOGGER.warn("User: " + userId + " is testing verification code for connect");
//
//                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");
//                                ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage, null, System.currentTimeMillis());
//
//                                Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                                future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                                closeSessionWithBadDataStatusCode(session, errorMessage);
//                            } else if (!userId.equals(computer.getUserId())) {
//                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.computer.admin", computer.getComputerName(), user.getNickname());
//
//                                ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_USER_NOT_ADMIN, errorMessage, userId, System.currentTimeMillis());
//
//                                Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                                future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                                closeSessionWithBadDataStatusCode(session, errorMessage);
//                            } else if (user.getShouldUpdatePhoneNumber()) {
//                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.should.update.phone.number.from.device", user.getPhoneNumber());
//
//                                ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_SHOULD_UPDATE_PHONE_NUMBER, errorMessage, userId, System.currentTimeMillis());
//
//                                Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                                future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                                closeSessionWithBadDataStatusCode(session, errorMessage);
//                            } else {
//                                /* TODO: update the version and build of the computer */
//
//                                /* self-approved if the user should */
//                                Boolean shouldSelfApproved = connectModel.getShouldSelfApproved();
//
//                                String computerAdminId = computer.getUserId();
//
//                                if (shouldSelfApproved != null && shouldSelfApproved) {
//                                    long currentTimestamp = System.currentTimeMillis();
//                                    ApplyConnection applyConnection = new ApplyConnection(null, currentTimestamp, userId, computerId, true, userId, currentTimestamp);
//
//                                    applyConnectionDao.createOrUpdateApplyConnection(applyConnection);
//                                }
//
//                                /* create properties for the user-computer if not exists */
//                                String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);
//
//                                /* create linkage between this instance and user, session */
//
//                                this.userId = userId;
//                                this.userComputerId = userComputerId;
//
//                                updateLastAccessTimeToNow();
//
//                                if (lugServerId == null || lugServerId.trim().length() < 1) {
//                                    lugServerId = Constants.AA_SERVER_ID_AS_LUG_SERVER;
//
//                                    LOGGER.warn("Use AA server as lug server because empty lug server id received from desktop of user: " + userId + ", computer id: " + computerId);
//                                }
//
//                                /* disconnect old socket, if any */
//                                try {
//                                    ConnectSocket oldSocket = ConnectSocket.getInstance(userComputerId);
//
//                                    if (oldSocket != null) {
//                                        removeInstance(userComputerId);
//
//                                        oldSocket.getSession().disconnect();
//
//                                        LOGGER.debug("Disconnect old socket of user computer: " + userComputerId);
//                                    }
//                                } catch (Exception e) {
//                                    /* ignored */
//                                }
//
//                                /* MAKE SURE THAT
//                                 * dealing with user-computer first before user-computer-properties
//                                 * so data wirtten to user-computer-properties will not fail because of foreign key user-computer-id not found
//                                 */
//
//                                /* create linkage between this instance and userId */
//                                putInstance(userComputerId, ConnectSocket.this);
//
//                                /* create/update user computer in DB */
//                                UserComputer currentUserComputer = userComputerDao.findUserComputerById(userComputerId);
//
//                                if (currentUserComputer == null) {
//                                    String encryptedUserComputerId = Utility.generateEncryptedUserComputerIdFrom(userId, computerId);
//
//                                    // for admin user, the default value to the allow-alias is true.
//                                    UserComputer userComputer = new UserComputer(userComputerId, userId, computerId, computerAdminId, null, null, encryptedUserComputerId, lugServerId, true, false, true);
//
//                                    userComputerDao.createUserComputer(userComputer);
//                                } else {
//                                    currentUserComputer.setLugServerId(lugServerId);
//                                    currentUserComputer.setSocketConnected(true);
//                                    currentUserComputer.setNeedReconnect(false);
//
//                                    // for admin user, the default value to the allow-alias is true.
//                                    currentUserComputer.setAllowAlias(Boolean.TRUE);
//
//                                    userComputerDao.updateUserComputerConnectionStatus(currentUserComputer);
//                                }
//
//                                Properties properties = connectModel.getProperties();
//
//                                if (properties != null && properties.size() > 0) {
//                                    String oldDesktopVersion = userComputerPropertiesDao.findPropertyValue(userComputerId, PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);
//                                    String currentDesktopVersion = properties.getProperty(PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);
//
//                                    // To prevent device login using Facebook Account Kit multiple times, do not remove ClientSessions when desktop upgrades:
//                                    // 1: when device upgrades from 1.x to 2.x
//                                    // 2: when desktop upgrades from 1.x to 2.x, the device needs to login again because the sessions are deleted.
//
////                                    // clear client sessions for this computer if version of the computer is not the same with the current one
////                                    // If no old desktop version, don't have to remove client session
////
////                                    if (oldDesktopVersion != null && !oldDesktopVersion.equals(currentDesktopVersion)) {
////                                        clientSessionService.removeClientSessionsByComputer(computerId);
////
////                                        LOGGER.info("Computer '" + computerId + "' upgraded from version: '" + oldDesktopVersion + "' to version: '" + currentDesktopVersion + "'. So all client sessions connected with this computer were just deleted.");
////                                    }
//
//                                    // delete and re-create all properties for this user computer
//
//                                    userComputerPropertiesDao.deletePropertiesByUserComputerId(userComputerId);
//
//                                    userComputerPropertiesDao.createUserComputerProperties(userComputerId, properties);
//
//                                    // Notify desktop to update to the latest version
//
//                                    boolean notifyUpdate = false;
//                                    String latestDesktopVersion = null;
//
//                                    if (currentDesktopVersion != null && Version.valid(currentDesktopVersion)) {
//                                        latestDesktopVersion = filelugPropertiesDao.findValueByKey(Constants.FILELUG_PROPERTY_KEY_DESKTOP_LATEST_VERSION);
//
//                                        if (Version.valid(latestDesktopVersion)) {
//                                            Version currentVersion = new Version(currentDesktopVersion);
//                                            Version latestVersion = new Version(latestDesktopVersion);
//
//                                            notifyUpdate = latestVersion.compareTo(currentVersion) > 0;
//                                        }
//                                    }
//
//                                    if (notifyUpdate) {
//                                        String downloadUrl = filelugPropertiesDao.findValueByKey(Constants.FILELUG_PROPERTY_KEY_DESKTOP_DOWNLOAD_URL);
//
//                                        if (downloadUrl == null || downloadUrl.trim().length() < 1) {
//                                            downloadUrl = Constants.DEFAULT_DESKTOP_DOWNLOAD_URL;
//                                        }
//
//                                        RequestVersionModel requestVersionModel = new RequestVersionModel(Sid.NEW_SOFTWARE_NOTIFY, userId, ClopuccinoMessages.DEFAULT_LOCALE_STRING, currentDesktopVersion, latestDesktopVersion, downloadUrl);
//
//                                        session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(requestVersionModel));
//                                    }
//                                }
//
//                                // return message to desktop
//
//                                boolean allowAlias = userComputerDao.findAllowAliasById(userComputerId);
//
//                                ApprovedUserModel approvedUserModel = new ApprovedUserModel(userId, user.getCountryId(), user.getPhoneNumber(), user.getNickname(), user.getShowHidden(), allowAlias);
//
//                                ResponseUserModel responseModel = new ResponseUserModel(Sid.CONNECT, HttpServletResponse.SC_OK, null, userId, System.currentTimeMillis(), approvedUserModel);
//
//                                session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//                            }
//                        } else {
//                            /* password not correct */
//                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "password.not.correct", user.getNickname());
//
//                            ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_UNAUTHORIZED, errorMessage, userId, System.currentTimeMillis());
//
//                            Future future = session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//
//                            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
//
//                            closeSessionWithBadDataStatusCode(session, errorMessage);
//                        }
//                    }
//                }
//            }
//        }
//    } // end onComputerAdminConnectWebSocket(Session, ObjectMapper, ConnectModel)

//    private void onUnsupportedWebSocket(Session session, String message, Integer sid) {
//        try {
//            String errorMessage = sid + " is an unsupported service or you need to connect first in order to use this service.";
//            ResponseModel responseModel = new ResponseModel(Sid.UNSUPPORTED, HttpServletResponse.SC_NOT_FOUND, errorMessage, userId, System.currentTimeMillis());
//
//            ObjectMapper mapper = Utility.createObjectMapper();
//
//            session.getBasicRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
//        } catch (JsonProcessingException e) {
//            int httpStatusCode = HttpServletResponse.SC_BAD_REQUEST;
//
//            processOnMessageException(session, Sid.UNSUPPORTED, e, httpStatusCode, false);
//        } catch (Exception e) {
//            int httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
//
//            processOnMessageException(session, Sid.UNSUPPORTED, e, httpStatusCode, false);
//        }
//    } // end onUnsupportedWebSocket(Session, String, Integer)

    private void onReceiveFromUnsupportedWebSocket(final Session session, String message) {
        LOGGER.error(String.format("Unsupported message received.%nMessage:%n%s", message));

        if (resp != null && !resp.isCommitted()) {
            try {
                ObjectMapper mapper = Utility.createObjectMapper();

                ResponseModel responseModel = mapper.readValue(message, ResponseModel.class);

                Integer status = responseModel.getStatus();
                String errorMessage = responseModel.getError();

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(status);
                resp.getWriter().write(errorMessage != null ? errorMessage : "");
                resp.getWriter().flush();
            } catch (Exception e) {
                String errorMessage = String.format("Error on response unsupported service to client. Message from repository: %s Error Message: %s", message, e.getMessage());
                LOGGER.error(errorMessage);

                try {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } catch (Exception e1) {
                    /* ignored */
                }
            } finally {
                closeLatch.countDown();
            }
        } else {
            LOGGER.error("HttpServletResponse must be assigned when initiate ConnectSocket!");
        }
    }
}
