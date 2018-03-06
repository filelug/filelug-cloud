package org.clopuccino.server.servlet.websocket;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.*;
import org.clopuccino.PropertyConstants;
import org.clopuccino.dao.*;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.*;
import org.clopuccino.server.servlet.Sid;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.service.DeviceTokenService;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.CloseReason;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * <code>ConnectWebSocket</code> handles web socket received message Sid.CONNECT & Sid.CONNECT_V2
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ConnectWebSocketService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ConnectWebSocketService.class.getSimpleName());

    private final Session session;

    private final String message;

    private final ConnectSocket connectSocket;

    private final ComputerDao computerDao;

    private final UserDao userDao;

    private final ApplyConnectionDao applyConnectionDao;

    private final UserComputerDao userComputerDao;

    private final UserComputerPropertiesDao userComputerPropertiesDao;

    private final FilelugPropertiesDao filelugPropertiesDao;

    private final ClientSessionService clientSessionService;

    private final DeviceTokenService deviceTokenService;

    public ConnectWebSocketService(final Session session, final String message, final ConnectSocket connectSocket) {
        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        computerDao = new ComputerDao(dbAccess);

        userDao = new UserDao(dbAccess);

        applyConnectionDao = new ApplyConnectionDao(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        userComputerPropertiesDao = new UserComputerPropertiesDao(dbAccess);

        filelugPropertiesDao = new FilelugPropertiesDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);

        deviceTokenService = new DeviceTokenService(dbAccess);

        this.session = session;

        this.message = message;

        this.connectSocket = connectSocket;
    }

    public void messageReceived() {
        /* process connection request from server and response to server */
        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            ConnectModel connectModel = mapper.readValue(message, ConnectModel.class);

            // throw incompatible version exception on desktop version less than 1.0.5
            processOnDesktopLessThanVersion_1_0_5(connectModel);

            if (connectModel.getAdminAccount() != null && connectModel.getAdminAccount().trim().length() > 0) {
                onComputerUserConnectWebSocket(mapper, connectModel);
            } else {
                onComputerAdminConnectWebSocket(mapper, connectModel);
            }
        } catch (IncompatibleVersionException e) {
            int httpStatusCode = Constants.HTTP_STATUS_INCOMPATIBLE_VERSION;

            processOnIncompatibleVersionMessageException(session, Sid.CONNECT, e.getMessage(), httpStatusCode, true);
        } catch (JsonProcessingException e) {
            int httpStatusCode = HttpServletResponse.SC_BAD_REQUEST;

            processOnMessageException(session, Sid.CONNECT, e, httpStatusCode, true);
        } catch (SQLException e) {
            int httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

            processOnMessageException(session, Sid.CONNECT, e, httpStatusCode, false);
        } catch (Exception e) {
            int httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

            processOnMessageException(session, Sid.CONNECT, e, httpStatusCode, true);
        }
    }

    private void processOnDesktopLessThanVersion_1_0_5(ConnectModel connectModel) throws IncompatibleVersionException {
        if (connectModel != null && connectModel.getProperties() != null) {
            Properties properties = connectModel.getProperties();

            String desktopVersion = (String) properties.get(PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);

            if (desktopVersion != null
                && Version.valid(desktopVersion)
                && new Version(desktopVersion).compareTo(new Version("1.0.5")) < 0) {
                String errorMessage = ClopuccinoMessages.localizedMessage(connectModel.getLocale(), "update.software.first");
                throw new IncompatibleVersionException(errorMessage);
            }
        }
    }

    private void onComputerUserConnectWebSocket(ObjectMapper mapper, ConnectModel connectModel) throws Exception {
        /* process connection request from desktop administrator */

        final String userId = connectModel.getAccount();
        final String adminUserId = connectModel.getAdminAccount();
        final String adminPassword = connectModel.getPassword();
        final String adminNickname = connectModel.getNickname();
        final String verification = connectModel.getVerification();

        final Long computerId = connectModel.getComputerId();

        final String localeString = connectModel.getLocale();
        String lugServerId = connectModel.getLugServerId();

        Locale clientLocale = ClopuccinoMessages.getLocaleFromJavaLocaleString(localeString);

        connectSocket.setClientLocale(clientLocale);

        if (userId == null || adminPassword == null || adminNickname == null || verification == null || computerId == null
            || userId.trim().length() < 1 || adminPassword.trim().length() < 1 || adminNickname.trim().length() < 1 || verification.trim().length() < 1) {
            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");
            ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_BAD_REQUEST, errorMessage, null, System.currentTimeMillis());

            RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

            Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//            Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

            ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
        } else {
            // checking if computer exists must be prior than checking if user exists

            Computer computer = computerDao.findComputerById(computerId);

            if (computer == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "computer.not.found");

                ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_COMPUTER_NOT_FOUND, errorMessage, userId, System.currentTimeMillis());

                RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
            } else {
                User user = userDao.findUserById(userId);

                if (user == null) {
                    /* user not exists */
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                    ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_FORBIDDEN, errorMessage, userId, System.currentTimeMillis());

                    RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                    Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                    Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                    future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                    ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                } else {
                    /* if verified */
                    Boolean verified = user.getVerified();

                    if (verified == null || !verified) {
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "account.not.verified", user.getPhoneNumber());

                        ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_UNAUTHORIZED, errorMessage, userId, System.currentTimeMillis());

                        RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                        Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                        Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                        future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                        ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                    } else if (user.getShouldUpdatePhoneNumber()) {
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.should.update.phone.number.from.device", user.getPhoneNumber());

                        ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_SHOULD_UPDATE_PHONE_NUMBER, errorMessage, userId, System.currentTimeMillis());

                        RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                        Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                        Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                        future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                        ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                    } else {
                        /* validate with admin password */
                        User admin = userDao.findUserById(adminUserId);

                        if (admin == null) {
                            /* admin not exists */

                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                            ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_FORBIDDEN, errorMessage, userId, System.currentTimeMillis());

                            RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                            Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                            Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                            ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                        } else {
                            /* if admin verified */

                            Boolean adminVerified = admin.getVerified();

                            if (adminVerified == null || !adminVerified) {
                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "account.not.verified", admin.getPhoneNumber());

                                ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_FORBIDDEN, errorMessage, userId, System.currentTimeMillis());

                                RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                                Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                                Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                                future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                                ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                            } else {
                                /* validate with admin password */

                                String foundPasswd = admin.getPasswd();

                                if (adminPassword.equals(foundPasswd)) {
                                    // Checking verification code after checking password
                                    if (!verification.equals(Utility.generateVerification(adminUserId, adminPassword, adminNickname))) {
                                        LOGGER.warn("User: " + userId + " is testing verification code for connect");

                                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");
                                        ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage, null, System.currentTimeMillis());

                                        RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                                        Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                                        Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                                        future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                                        ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                                    } else if (!adminUserId.equals(computer.getUserId())) { // make sure the admin of the computer is the correct one
                                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.computer.admin", computer.getComputerName(), admin.getNickname());

                                        ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_USER_NOT_ADMIN, errorMessage, userId, null, System.currentTimeMillis());

                                        RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                                        Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                                        Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                                        future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                                        ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                                    } else if (admin.getShouldUpdatePhoneNumber()) {
                                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.should.update.phone.number.from.device", admin.getPhoneNumber());

                                        ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_SHOULD_UPDATE_PHONE_NUMBER, errorMessage, adminUserId, null, System.currentTimeMillis());

                                        RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                                        Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                                        Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                                        future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                                        ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                                    } else {
                                        /* check apply-connection */

                                        String computerAdminId = computer.getUserId();

                                        ApplyConnection applyConnection = applyConnectionDao.findApplyConnectionByApplyUserAndComputerId(userId, computerId);

                                        if (applyConnection == null) {
                                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.apply.connection", user.getNickname(), computer.getComputerName());

                                            ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_USER_NOT_APPLY_CONNECTION_YET, errorMessage, userId, System.currentTimeMillis());

                                            RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                                            Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                                            Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                                            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                                            ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                                        } else if (applyConnection.getApproved() != null && !applyConnection.getApproved()) {
                                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "apply.connection.not.approved.yet", user.getNickname(), computer.getComputerName());

                                            ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_APPLY_CONNECTION_NOT_APPROVED_YET, errorMessage, userId, System.currentTimeMillis());

                                            RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                                            Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                                            Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                                            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                                            ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                                        } else {
                                            /* TODO: update the version and build of the computer */

                                            String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

                                            /* create linkage between this instance and user, session */

                                            connectSocket.setUserId(userId);
                                            connectSocket.setUserComputerId(userComputerId);

                                            updateLastAccessTimeToNow();

                                            if (lugServerId == null || lugServerId.trim().length() < 1) {
                                                lugServerId = Constants.AA_SERVER_ID_AS_LUG_SERVER;

                                                LOGGER.warn("Use AA server as lug server because empty lug server id received from desktop of user: " + userId + ", computer id: " + computerId);
                                            }

                                            /* disconnect old socket, if any */
                                            try {
                                                ConnectSocket oldSocket = ConnectSocket.getInstance(userComputerId);

                                                if (oldSocket != null) {
                                                    ConnectSocket.removeInstance(userComputerId);

                                                    ConnectSocketUtilities.closeSession(oldSocket.getSession(), CloseReason.CloseCodes.NORMAL_CLOSURE, "Will replaced by new socket.");
//                                                    oldSocket.getSession().disconnect();

                                                    LOGGER.debug("Disconnect old socket of user computer: " + userComputerId);
                                                }
                                            } catch (Exception e) {
                                                /* ignored */
                                            }

                                            /* MAKE SURE THAT
                                             * dealing with user-computer first before user-computer-properties
                                             * so data wirtten to user-computer-properties will not fail because of foreign key user-computer-id not found
                                             */

                                            /* create linkage between this instance and userId */
                                            ConnectSocket.putInstance(userComputerId, connectSocket);
                                            /* create/update user computer in DB */
                                            UserComputer currentUserComputer = userComputerDao.findUserComputerById(userComputerId);

                                            if (currentUserComputer == null) {
                                                String encryptedUserComputerId = Utility.generateEncryptedUserComputerIdFrom(userId, computerId);

                                                // for non-admin user, the default value to the allow-alias is false.
                                                UserComputer userComputer = new UserComputer(userComputerId, userId, computerId, computerAdminId, null, null, encryptedUserComputerId, lugServerId, true, false, false);

                                                userComputerDao.createUserComputer(userComputer);
                                            } else {
                                                currentUserComputer.setLugServerId(lugServerId);
                                                currentUserComputer.setSocketConnected(true);
                                                currentUserComputer.setNeedReconnect(false);

                                                userComputerDao.updateUserComputerConnectionStatus(currentUserComputer);
                                            }

                                            /* delete first regardless of any newly-coming properties */
                                            userComputerPropertiesDao.deletePropertiesByUserComputerId(userComputerId);

                                            Properties properties = connectModel.getProperties();

                                            boolean notifyUpdate = false;

                                            String currentDesktopVersion = null;
                                            String latestDesktopVersion = null;

                                            if (properties != null && properties.size() > 0) {
                                                currentDesktopVersion = properties.getProperty(PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);

                                                if (currentDesktopVersion != null && Version.valid(currentDesktopVersion)) {
                                                    latestDesktopVersion = filelugPropertiesDao.findValueByKey(Constants.FILELUG_PROPERTY_KEY_DESKTOP_LATEST_VERSION);

                                                    if (Version.valid(latestDesktopVersion)) {
                                                        Version currentVersion = new Version(currentDesktopVersion);
                                                        Version latestVersion = new Version(latestDesktopVersion);

                                                        notifyUpdate = latestVersion.compareTo(currentVersion) > 0;
                                                    }
                                                }

                                                userComputerPropertiesDao.createUserComputerProperties(userComputerId, properties);
                                            }

                                            boolean allowAlias = userComputerDao.findAllowAliasById(userComputerId);

                                            ApprovedUserModel approvedUserModel = new ApprovedUserModel(userId, user.getCountryId(), user.getPhoneNumber(), user.getNickname(), user.getShowHidden(), allowAlias, null);

                                            ResponseUserModel responseModel = new ResponseUserModel(Sid.CONNECT, HttpServletResponse.SC_OK, null, userId, System.currentTimeMillis(), approvedUserModel);

                                            RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                                            asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                                            session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                                            if (notifyUpdate) {
                                                String downloadUrl = filelugPropertiesDao.findValueByKey(Constants.FILELUG_PROPERTY_KEY_DESKTOP_DOWNLOAD_URL);

                                                if (downloadUrl == null || downloadUrl.trim().length() < 1) {
                                                    downloadUrl = Constants.DEFAULT_DESKTOP_DOWNLOAD_URL;
                                                }

                                                RequestVersionModel requestVersionModel = new RequestVersionModel(Sid.NEW_SOFTWARE_NOTIFY, userId, ClopuccinoMessages.DEFAULT_LOCALE_STRING, currentDesktopVersion, latestDesktopVersion, downloadUrl);

                                                asyncRemote.sendText(mapper.writeValueAsString(requestVersionModel));

//                                                session.getRemote().sendStringByFuture(mapper.writeValueAsString(requestVersionModel));
                                            }
                                        }
                                    }
                                } else {
                                    /* password not correct */

                                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "password.not.correct", adminUserId);

                                    ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_UNAUTHORIZED, errorMessage, adminUserId, System.currentTimeMillis());

                                    RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                                    Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                                    Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                                    future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                                    ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void onComputerAdminConnectWebSocket(ObjectMapper mapper, ConnectModel connectModel) throws Exception {
        /* process connection request from desktop admin */

        final String userId = connectModel.getAccount();
        final String encryptedPassword = connectModel.getPassword();
        final String nickname = connectModel.getNickname();
        final String verification = connectModel.getVerification();
        final Long computerId = connectModel.getComputerId();
        final String localeString = connectModel.getLocale();
        String lugServerId = connectModel.getLugServerId();

        Locale clientLocale = ClopuccinoMessages.getLocaleFromJavaLocaleString(localeString);

        connectSocket.setClientLocale(clientLocale);

        if (userId == null || encryptedPassword == null || nickname == null || verification == null || computerId == null
            || userId.trim().length() < 1 || encryptedPassword.trim().length() < 1 || nickname.trim().length() < 1 || verification.trim().length() < 1) {
            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");
            ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_BAD_REQUEST, errorMessage, null, System.currentTimeMillis());

            RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

            Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//            Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

            ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
        } else {
            // checking if computer exists must be prior than checking if user exists

            Computer computer = computerDao.findComputerById(computerId);

            if (computer == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "computer.not.found");

                ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_COMPUTER_NOT_FOUND, errorMessage, userId, System.currentTimeMillis());

                RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
            } else {
                User user = userDao.findUserById(userId);

                if (user == null) {
                    /* user not exists */
                    String errorMessage = "'" + userId + "' not registered yet. You should register this account to respository first.";

                    ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_FORBIDDEN, errorMessage, userId, System.currentTimeMillis());

                    RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                    Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                    Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                    future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                    ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                } else {
                    /* if verified */
                    Boolean verified = user.getVerified();

                    if (verified == null || !verified) {
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "account.not.verified", user.getPhoneNumber());

                        ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_UNAUTHORIZED, errorMessage, userId, System.currentTimeMillis());

                        RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                        Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                        Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                        future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                        ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                    } else {
                        /* validate with password */
                        String passwordInResp = user.getPasswd();

                        if (encryptedPassword.equals(passwordInResp)) {
                            // Check verification code after checking password
                            if (!verification.equals(Utility.generateVerification(userId, encryptedPassword, nickname))) {
                                LOGGER.warn("User: " + userId + " is testing verification code for connect");

                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");
                                ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage, null, System.currentTimeMillis());

                                RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                                Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                                Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                                future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                                ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                            } else if (!userId.equals(computer.getUserId())) {
                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.computer.admin", computer.getComputerName(), user.getNickname());

                                ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_USER_NOT_ADMIN, errorMessage, userId, System.currentTimeMillis());

                                RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                                Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                                Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                                future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                                ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                            } else if (user.getShouldUpdatePhoneNumber()) {
                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.should.update.phone.number.from.device", user.getPhoneNumber());

                                ResponseModel responseModel = new ResponseModel(Sid.CONNECT, Constants.HTTP_STATUS_SHOULD_UPDATE_PHONE_NUMBER, errorMessage, userId, System.currentTimeMillis());

                                RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                                Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                                Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                                future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                                ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                            } else {
                                /* TODO: update the version and build of the computer */

                                /* self-approved if the user should */
                                Boolean shouldSelfApproved = connectModel.getShouldSelfApproved();

                                String computerAdminId = computer.getUserId();

                                if (shouldSelfApproved != null && shouldSelfApproved) {
                                    long currentTimestamp = System.currentTimeMillis();
                                    ApplyConnection applyConnection = new ApplyConnection(null, currentTimestamp, userId, computerId, true, userId, currentTimestamp);

                                    applyConnectionDao.createOrUpdateApplyConnection(applyConnection);
                                }

                                /* create properties for the user-computer if not exists */
                                String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

                                /* create linkage between this instance and user, session */

                                connectSocket.setUserId(userId);
                                connectSocket.setUserComputerId(userComputerId);

                                updateLastAccessTimeToNow();

                                if (lugServerId == null || lugServerId.trim().length() < 1) {
                                    lugServerId = Constants.AA_SERVER_ID_AS_LUG_SERVER;

                                    LOGGER.warn("Use AA server as lug server because empty lug server id received from desktop of user: " + userId + ", computer id: " + computerId);
                                }

                                /* disconnect old socket, if any */
                                try {
                                    ConnectSocket oldSocket = ConnectSocket.getInstance(userComputerId);

                                    if (oldSocket != null) {
                                        ConnectSocket.removeInstance(userComputerId);

                                        ConnectSocketUtilities.closeSession(oldSocket.getSession(), CloseReason.CloseCodes.NORMAL_CLOSURE, "Will replaced by new socket.");
//                                        oldSocket.getSession().disconnect();

                                        LOGGER.debug("Disconnect old socket of user computer: " + userComputerId);
                                    }
                                } catch (Exception e) {
                                    /* ignored */
                                }

                                /* MAKE SURE THAT
                                 * dealing with user-computer first before user-computer-properties
                                 * so data wirtten to user-computer-properties will not fail because of foreign key user-computer-id not found
                                 */

                                /* create linkage between this instance and userId */
                                ConnectSocket.putInstance(userComputerId, connectSocket);

                                /* create/update user computer in DB */
                                UserComputer currentUserComputer = userComputerDao.findUserComputerById(userComputerId);

                                if (currentUserComputer == null) {
                                    String encryptedUserComputerId = Utility.generateEncryptedUserComputerIdFrom(userId, computerId);

                                    // for admin user, the default value to the allow-alias is true.
                                    UserComputer userComputer = new UserComputer(userComputerId, userId, computerId, computerAdminId, null, null, encryptedUserComputerId, lugServerId, true, false, true);

                                    userComputerDao.createUserComputer(userComputer);
                                } else {
                                    currentUserComputer.setLugServerId(lugServerId);
                                    currentUserComputer.setSocketConnected(true);
                                    currentUserComputer.setNeedReconnect(false);

                                    // for admin user, the default value to the allow-alias is true.
                                    currentUserComputer.setAllowAlias(Boolean.TRUE);

                                    userComputerDao.updateUserComputerConnectionStatus(currentUserComputer);
                                }

                                Properties properties = connectModel.getProperties();

                                if (properties != null && properties.size() > 0) {
//                                    String oldDesktopVersion = userComputerPropertiesDao.findPropertyValue(userComputerId, PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);
                                    String currentDesktopVersion = properties.getProperty(PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);

                                    // To prevent device login using Facebook Account Kit multiple times, do not remove ClientSessions when desktop upgrades:
                                    // 1: when device upgrades from 1.x to 2.x
                                    // 2: when desktop upgrades from 1.x to 2.x, the device needs to login again because the sessions are deleted.

//                                    // clear client sessions for this computer if version of the computer is not the same with the current one
//                                    // If no old desktop version, don't have to remove client session
//
//                                    if (oldDesktopVersion != null && !oldDesktopVersion.equals(currentDesktopVersion)) {
//                                        clientSessionService.removeClientSessionsByComputer(computerId);
//
//                                        LOGGER.info("Computer '" + computerId + "' upgraded from version: '" + oldDesktopVersion + "' to version: '" + currentDesktopVersion + "'. So all client sessions connected with this computer were just deleted.");
//                                    }

                                    // delete and re-create all properties for this user computer

                                    userComputerPropertiesDao.deletePropertiesByUserComputerId(userComputerId);

                                    userComputerPropertiesDao.createUserComputerProperties(userComputerId, properties);

                                    // Notify desktop to update to the latest version

                                    boolean notifyUpdate = false;
                                    String latestDesktopVersion = null;

                                    if (currentDesktopVersion != null && Version.valid(currentDesktopVersion)) {
                                        latestDesktopVersion = filelugPropertiesDao.findValueByKey(Constants.FILELUG_PROPERTY_KEY_DESKTOP_LATEST_VERSION);

                                        if (Version.valid(latestDesktopVersion)) {
                                            Version currentVersion = new Version(currentDesktopVersion);
                                            Version latestVersion = new Version(latestDesktopVersion);

                                            notifyUpdate = latestVersion.compareTo(currentVersion) > 0;
                                        }
                                    }

                                    if (notifyUpdate) {
                                        String downloadUrl = filelugPropertiesDao.findValueByKey(Constants.FILELUG_PROPERTY_KEY_DESKTOP_DOWNLOAD_URL);

                                        if (downloadUrl == null || downloadUrl.trim().length() < 1) {
                                            downloadUrl = Constants.DEFAULT_DESKTOP_DOWNLOAD_URL;
                                        }

                                        RequestVersionModel requestVersionModel = new RequestVersionModel(Sid.NEW_SOFTWARE_NOTIFY, userId, ClopuccinoMessages.DEFAULT_LOCALE_STRING, currentDesktopVersion, latestDesktopVersion, downloadUrl);

                                        RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                                        asyncRemote.sendText(mapper.writeValueAsString(requestVersionModel));

//                                        session.getRemote().sendStringByFuture(mapper.writeValueAsString(requestVersionModel));
                                    }
                                }

                                // return message to desktop

                                boolean allowAlias = userComputerDao.findAllowAliasById(userComputerId);

                                ApprovedUserModel approvedUserModel = new ApprovedUserModel(userId, user.getCountryId(), user.getPhoneNumber(), user.getNickname(), user.getShowHidden(), allowAlias, null);

                                ResponseUserModel responseModel = new ResponseUserModel(Sid.CONNECT, HttpServletResponse.SC_OK, null, userId, System.currentTimeMillis(), approvedUserModel);

                                RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                                asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                                session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));
                            }
                        } else {
                            /* password not correct */
                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "password.not.correct", user.getNickname());

                            ResponseModel responseModel = new ResponseModel(Sid.CONNECT, HttpServletResponse.SC_UNAUTHORIZED, errorMessage, userId, System.currentTimeMillis());

                            RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                            Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//                            Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

                            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                            ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                        }
                    }
                }
            }
        }
    }

    public void messageReceivedV2() {
        // process connection request from server and response to server

        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            ConnectModel connectModel = mapper.readValue(message, ConnectModel.class);

            connectFromComputer(mapper, connectModel);
        } catch (IncompatibleVersionException e) {
            int httpStatusCode = Constants.HTTP_STATUS_INCOMPATIBLE_VERSION;

            processOnIncompatibleVersionMessageException(session, Sid.CONNECT_V2, e.getMessage(), httpStatusCode, true);
        } catch (JsonProcessingException e) {
            int httpStatusCode = HttpServletResponse.SC_BAD_REQUEST;

            processOnMessageException(session, Sid.CONNECT_V2, e, httpStatusCode, true);
        } catch (SQLException e) {
            int httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

            processOnMessageException(session, Sid.CONNECT_V2, e, httpStatusCode, false);
        } catch (Exception e) {
            int httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

            processOnMessageException(session, Sid.CONNECT_V2, e, httpStatusCode, true);
        }
    }

    private void connectFromComputer(ObjectMapper mapper, ConnectModel connectModel) throws Exception {
        // process connection request from desktop admin or non-admin

        final String sessionId = connectModel.getSessionId();
        String lugServerId = connectModel.getLugServerId();
        final String locale = connectModel.getLocale();

        Locale clientLocale = ClopuccinoMessages.getLocaleFromJavaLocaleString(locale);

        connectSocket.setClientLocale(clientLocale);

        if (sessionId == null || lugServerId == null || sessionId.trim().length() < 1 || lugServerId.trim().length() < 1) {
            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");
            ResponseModel responseModel = new ResponseModel(Sid.CONNECT_V2, HttpServletResponse.SC_BAD_REQUEST, errorMessage, null, System.currentTimeMillis());

            RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

            Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

            ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
        } else {
            ClientSession currentClientSession = clientSessionService.findClientSessionBySessionId(sessionId);

            if (currentClientSession == null) {
                // session not found, need login first

                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "session.not.exists");

                ResponseModel responseModel = new ResponseModel(Sid.CONNECT_V2, HttpServletResponse.SC_UNAUTHORIZED, errorMessage, null, System.currentTimeMillis());

                RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

                future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
            } else {
                // Check if ApplyConnection exists and approved. If ApplyConnection exists and approved, we assume User, Computer, UserComputer all exist.

                final String userId = currentClientSession.getUserId();

                final long computerId = currentClientSession.getComputerId();

                final String userComputerId = currentClientSession.getUserComputerId();

                ApplyConnection applyConnection = applyConnectionDao.findApplyConnectionByApplyUserAndComputerId(userId, computerId);

                if (applyConnection == null || applyConnection.getApproved() == null || !applyConnection.getApproved()) {
                    // not applied yet or denied.

                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "connection.denied");

                    ResponseModel responseModel = new ResponseModel(Sid.CONNECT_V2, HttpServletResponse.SC_FORBIDDEN, errorMessage, null, System.currentTimeMillis());

                    RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                    Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

                    future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                    ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
                } else {
                    // device token

                    String deviceTokenString = currentClientSession.getDeviceToken();

                    DeviceToken deviceTokenObject = connectModel.getDeviceToken();

                    if (deviceTokenObject != null) {
                        // The device token object from computer contains no userId, need to add it here
                        deviceTokenObject.setAccount(userId);

                        deviceTokenService.createOrUploadDeviceToken(deviceTokenObject, locale);

                        // update the device token string only after device token created or updated,
                        // or use the current one in ClientSession
                        deviceTokenString = deviceTokenObject.getDeviceToken();
                    }

                    // Check if user session timeout, update last access time ONLY if not timeout.
                    // If user session has timed out, return a new one.

                    String newSessionId = sessionId;

                    if (currentClientSession.checkTimeout()) {
                        // create new ClientSession

                        String clientSessionId = ClientSessionService.generateUniqueSessionId(userComputerId);

                        boolean showHidden = currentClientSession.isShowHidden();

                        clientSessionService.appendClientSession(clientSessionId, userId, showHidden, locale, computerId, userComputerId, deviceTokenString);

                        newSessionId = clientSessionId;
                    } else {
                        // update last access timestamp with the current ClientSession

                        clientSessionService.updateClientSessionLastAccessTimestamp(sessionId, System.currentTimeMillis());
                    }

                    // Update UserComputerProperties

                    boolean notifyUpdate = false;
                    String currentDesktopVersion = null;
                    String latestDesktopVersion = null;

                    Properties properties = connectModel.getProperties();

                    if (properties != null && properties.size() > 0) {
                        currentDesktopVersion = properties.getProperty(PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);

                        // To prevent device login using Facebook Account Kit multiple times, do not remove ClientSessions when desktop upgrades
//                        // clear client sessions for this computer if version of the computer is not the same with the current one
//                        // If no old desktop version, don't have to remove client session
//
//                        String oldDesktopVersion = userComputerPropertiesDao.findPropertyValue(userComputerId, PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);
//
//                        if (oldDesktopVersion != null && !oldDesktopVersion.equals(currentDesktopVersion)) {
//                            clientSessionService.removeClientSessionsByComputer(computerId);
//
//                            LOGGER.info("Computer '" + computerId + "' upgraded from version: '" + oldDesktopVersion + "' to version: '" + currentDesktopVersion + "'. So all client sessions connected with this computer were just deleted.");
//                        }

                        // delete and re-create all properties for this user computer

                        userComputerPropertiesDao.deletePropertiesByUserComputerId(userComputerId);

                        userComputerPropertiesDao.createUserComputerProperties(userComputerId, properties);

                        // Notify desktop to update to the latest version

                        if (currentDesktopVersion != null && Version.valid(currentDesktopVersion)) {
                            latestDesktopVersion = filelugPropertiesDao.findValueByKey(Constants.FILELUG_PROPERTY_KEY_DESKTOP_LATEST_VERSION);

                            if (Version.valid(latestDesktopVersion)) {
                                Version currentVersion = new Version(currentDesktopVersion);
                                Version latestVersion = new Version(latestDesktopVersion);

                                notifyUpdate = latestVersion.compareTo(currentVersion) > 0;
                            }
                        }
                    }

                    // create linkage between this instance and user, session

                    connectSocket.setUserId(userId);
                    connectSocket.setUserComputerId(userComputerId);

                    updateLastAccessTimeToNow();

                    // disconnect old socket, if any
                    try {
                        ConnectSocket oldSocket = ConnectSocket.getInstance(userComputerId);

                        if (oldSocket != null && oldSocket != connectSocket) {
                            ConnectSocket.removeInstance(userComputerId);

                            LOGGER.debug("Removed old socket mapping before mapping new socket with the same user computer: " + userComputerId);

                            if (oldSocket.getSession() != null && oldSocket.getSession() != connectSocket.getSession()) {
                                ConnectSocketUtilities.closeSession(oldSocket.getSession(), CloseReason.CloseCodes.NORMAL_CLOSURE, "Will replaced by new socket.");
//                            oldSocket.getSession().disconnect();
                            }
                        }
                    } catch (Exception e) {
                        // ignored
                    }

                    ConnectSocket.putInstance(userComputerId, connectSocket);

                    // update UserComputer,
                    // MUST AFTER new ConnectSocket added to the list so the columns socketConnected and needReconnect can be updated

                    UserComputer userComputer = userComputerDao.findUserComputerById(userComputerId);

                    // userComputer assumed not null

                    String computerGroup = null;

                    String computerName = null;

                    if (userComputer != null) {
                        computerGroup = userComputer.getGroupName();
                        computerName = userComputer.getComputerName();

                        userComputer.setLugServerId(lugServerId);
                        userComputer.setSocketConnected(true);
                        userComputer.setNeedReconnect(false);

                        // for admin user, the default value to the allow-alias is true.
                        userComputer.setAllowAlias(Boolean.TRUE);

                        userComputerDao.updateUserComputerConnectionStatus(userComputer);

//                        // DEBUG
//                        LOGGER.info("UserComputer updated when connecting with computer.\n" + userComputer);
                    }

                    // return message to desktop

                    Boolean allowAlias = userComputer != null ? userComputer.isAllowAlias() : null;

                    if (allowAlias == null) {
                        allowAlias = userComputerDao.findAllowAliasById(userComputerId);
                    }

                    User user = userDao.findUserById(userId);

                    String nickname = user != null ? user.getNickname() : null;

                    Boolean showHidden = user != null ? user.getShowHidden() : null;

                    ApprovedUserModel approvedUserModel = new ApprovedUserModel(userId, nickname, showHidden, allowAlias, newSessionId);

                    ResponseUserComputerModel responseModel = new ResponseUserComputerModel(Sid.CONNECT_V2, HttpServletResponse.SC_OK, null, userId, System.currentTimeMillis(), computerId, computerGroup, computerName, approvedUserModel);

                    RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

                    asyncRemote.sendText(mapper.writeValueAsString(responseModel));

                    // notify desktop to update software if needed

                    if (notifyUpdate) {
                        String downloadUrl = filelugPropertiesDao.findValueByKey(Constants.FILELUG_PROPERTY_KEY_DESKTOP_DOWNLOAD_URL);

                        if (downloadUrl == null || downloadUrl.trim().length() < 1) {
                            downloadUrl = Constants.DEFAULT_DESKTOP_DOWNLOAD_URL;
                        }

                        RequestVersionModel requestVersionModel = new RequestVersionModel(Sid.NEW_SOFTWARE_NOTIFY, userId, ClopuccinoMessages.DEFAULT_LOCALE_STRING, currentDesktopVersion, latestDesktopVersion, downloadUrl);

                        Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(requestVersionModel));
                    }
                }
            }
        }
    }

    private void updateLastAccessTimeToNow() {
        connectSocket.setLastAccessTime(System.currentTimeMillis());
    }

    private void processOnIncompatibleVersionMessageException(Session session, Integer sid, String errorMessage, int httpStatusCode, boolean needCloseAndDisconnect) {
        ResponseModelWithoutClientSessionId responseModel = new ResponseModelWithoutClientSessionId(sid != null ? sid : null, httpStatusCode, errorMessage, connectSocket.getUserId(), System.currentTimeMillis());

        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

            Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//            Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e1) {
            /* ignored */
        } finally {
            if (needCloseAndDisconnect) {
                ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
            }
        }
    }

    private void processOnMessageException(Session session, Integer sid, Exception e, int httpStatusCode, boolean needCloseAndDisconnect) {
        String errorMessage = String.format("Error on processing received message.%n%s%n%s%n", e.getClass().getName(), e.getMessage());

        ResponseModel responseModel = new ResponseModel(sid != null ? sid : null, httpStatusCode, errorMessage, connectSocket.getUserId(), System.currentTimeMillis());

        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();

            Future<Void> future = asyncRemote.sendText(mapper.writeValueAsString(responseModel));

//            Future future = session.getRemote().sendStringByFuture(mapper.writeValueAsString(responseModel));

            future.get(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e1) {
            /* ignored */
        } finally {
            if (needCloseAndDisconnect) {
                ConnectSocketUtilities.closeSessionWithBadDataStatusCode(session, errorMessage);
            }
        }
    }

}

