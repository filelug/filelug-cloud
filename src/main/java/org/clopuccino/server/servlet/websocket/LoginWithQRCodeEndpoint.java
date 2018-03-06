package org.clopuccino.server.servlet.websocket;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.clopuccino.*;
import org.clopuccino.PropertyConstants;
import org.clopuccino.dao.*;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.*;
import org.clopuccino.server.servlet.Sid;
import org.clopuccino.service.ConnectionDispatchService;
import org.clopuccino.service.CountryService;
import org.clopuccino.service.DeviceTokenService;
import org.clopuccino.service.LoginService;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Hashtable;
import java.util.Properties;

/**
 * <code>LoginWithQRCodeEndpoint</code> is a web socket server endpoint to serve login from computer.
 *
 * @author masonhsieh
 * @version 1.0
 */
@ServerEndpoint("/ws/user/qrcode-login")
public class LoginWithQRCodeEndpoint {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(LoginWithQRCodeEndpoint.class.getSimpleName());

    /**
     * The class instance will be added to instances after qr code generated successfully.
     * key=QR code
     */
    private static final Hashtable<String, LoginWithQRCodeEndpoint> instances = new Hashtable<>();

    private Session session;

    private Long computerId;

    private String recoveryKey;

    private String computerGroup;

    private String computerName;

    private String locale;

    private DeviceToken deviceToken;

    private Properties systemProperties;

    private final UserDao userDao;

    private final ComputerDao computerDao;

    private final LoginService loginService;

    private final UserComputerDao userComputerDao;

    private final UserComputerPropertiesDao userComputerPropertiesDao;

    private final DeviceTokenService deviceTokenService;

    private final ApplyConnectionDao applyConnectionDao;

    private final CountryService countryService;

    private static void putInstance(String qrcode, LoginWithQRCodeEndpoint endpoint) {
        instances.put(qrcode, endpoint);
    }

    public static void removeInstance(String qrcode) {
        instances.remove(qrcode);
    }

    public static LoginWithQRCodeEndpoint getInstance(String qrcode) {
        return instances.get(qrcode);
    }

    public LoginWithQRCodeEndpoint() {
        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        loginService = new LoginService(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        userComputerPropertiesDao = new UserComputerPropertiesDao(dbAccess);

        deviceTokenService = new DeviceTokenService(dbAccess);

        applyConnectionDao = new ApplyConnectionDao(dbAccess);

        countryService = new CountryService();
    }

    public Session getSession() {
        return session;
    }

    public Long getComputerId() {
        return computerId;
    }

    public String getRecoveryKey() {
        return recoveryKey;
    }

    public String getComputerGroup() {
        return computerGroup;
    }

    public String getComputerName() {
        return computerName;
    }

    public String getLocale() {
        return locale;
    }

    public DeviceToken getDeviceToken() {
        return deviceToken;
    }

    public Properties getSystemProperties() {
        return systemProperties;
    }

    @OnOpen
    public void onOpen(Session session) {
//        // DEBUG
//        LOGGER.info("Login socket opened with session: " + session.getId());

        session.setMaxBinaryMessageBufferSize(Integer.MAX_VALUE);
        session.setMaxTextMessageBufferSize(Integer.MAX_VALUE);
        session.setMaxIdleTimeout(Constants.IDLE_TIMEOUT_IN_SECONDS_TO_CONNECT_SOCKET * 1000);

        this.session = session;
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        LOGGER.debug(String.format("Login socket closed with session: %s\nReason: (code: %d)%s", session.getId(), closeReason.getCloseCode().getCode(), closeReason.getReasonPhrase()));
    }

    @OnError
    public void onError(Session session, Throwable t) {
        LOGGER.error(String.format("Login socket error for computer: '%s', session: '%s', error:\n%s", computerName, session.getId(), (t != null ? t.getMessage() : "(Empty error message)")), t);
    }

    public boolean isOpen() {
        return session != null && session.isOpen();
    }

    @OnMessage
    public void onMessage(Session session, String text) {
//        // DEBUG
//        LOGGER.info("Message received: " + text);

        ObjectMapper mapper = Utility.createObjectMapper();

        try {
            JsonNode rootNode = mapper.readTree(text);

            JsonNode sidNode = rootNode.findValue("sid");

            if (sidNode != null && sidNode.isNumber()) {
                int sid = sidNode.intValue();

                if (sid == Sid.GET_QR_CODE_V2) {
                    onRequestQRCodeMessage(session, rootNode);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on receiving message:\n" + text, e);
        }
    }

    private void onRequestQRCodeMessage(Session session, JsonNode rootNode) {
        /*
        {
            "sid" : 21101,
            "computer-id" : 3837763637383939, // 只有當有安裝以前版本，並曾經成功連線過，且沒有「回復應用程式初始狀態」時才會有此值。
            "recovery-key":"012336272652",     // 只有當有安裝以前版本，並曾經成功連線過，且沒有「回復應用程式初始狀態」時才會有此值。
            "computer-group" : "GENERAL",
            "computer-name" : "WILLIAM GREGOR",
            "verification" : "bf94b4d1c5305b5b3f96abdab89eba05b994ea10a249aaa3acb28aa13533040cc4b24cec2783fc3402011f3a52065c16",
            "locale" : "zh_TW",
            "device-token":
                        // 此值可不提供。若提供，則下面所有子項目除了「badge-number」可不提供之外，其他都要提供。
            {
                "device-token" : "1e39b345af9b036a2fc1066f2689143746f7d1220c23ff1491619a544a167c61",
                "notification-type" : "APNS",
                "device-type" : "OSX",
                "device-version" : "10.12.2",           // 作業系統版本
                "filelug-version" : "2.0.0",           // Filelug APP 大版號
                "filelug-build" : "2016.12.25.01",     // Filelug APP 小版號
                "badge-number" : 0                     // 此值可不提供
            },
            "sysprops" :
            {
                "desktop.version": "2.0.0",
                "locale": "zh_TW", // instead of "desktop.locale"
                "file.encoding": "MacRoman",
                "java.vm.info": "mixed mode",
                "user.dir": "/Users/user1/projects/clopuccino",
                "line.separator": "\n",
                "user.name": "user1",
                "user.home": "/Users/user1",
                "java.home": "/Library/Java/JavaVirtualMachines/jdk1.7.0_40.jdk/Contents/Home",
                "java.class.version": "51.0",
                "user.language": "zh",
                "sun.jnu.encoding": "MacRoman",
                "java.io.tmpdir": "/var/folders/42/0xh_py3d4zs2dgbt2pdfybzh0000gn/T/",
                "file.separator": "/",
                "java.vm.vendor": "Apple Inc.",
                "java.specification.version": "1.7",
                "java.runtime.version": "jdk1.7.0_40",
                "user.timezone": "Asia/Taipei",
                "java.vendor": "Apple Inc.",
                "sun.io.unicode.encoding": "UnicodeLittle",
                "os.arch": "x86_64",
                "path.separator": ":",
                "os.name": "Mac OS X",
                "os.version": "10.8.5",
                "java.version": "1.7.0_40",
                "user.country": "TW"
            }
        }
        */

        try {
            JsonNode computerIdNode = rootNode.get(PropertyConstants.PROPERTY_NAME_COMPUTER_ID);
            JsonNode recoveryKeyNode = rootNode.get(PropertyConstants.PROPERTY_NAME_RECOVERY_KEY);
            JsonNode computerGroupNode = rootNode.get(PropertyConstants.PROPERTY_NAME_COMPUTER_GROUP);
            JsonNode computerNameNode = rootNode.get(PropertyConstants.PROPERTY_NAME_COMPUTER_NAME);
            JsonNode realVerificationNode = rootNode.get(PropertyConstants.PROPERTY_NAME_VERIFICATION);
            JsonNode localeNode = rootNode.get(PropertyConstants.PROPERTY_NAME_LOCALE);
            JsonNode deviceTokenNode = rootNode.get(PropertyConstants.PROPERTY_NAME_DEVICE_TOKEN);
            JsonNode systemPropertiesNode = rootNode.get(PropertyConstants.PROPERTY_NAME_SYSTEM_PROPERTIES);

            if (computerGroupNode == null || computerGroupNode.textValue() == null
                || computerNameNode == null || computerNameNode.textValue() == null
                || realVerificationNode == null || realVerificationNode.textValue() == null
                || localeNode == null || localeNode.textValue() == null
                || deviceTokenNode == null
                || systemPropertiesNode == null) {
                String errorMessage = ClopuccinoMessages.getMessage("at.least.one.empty.account.property");

                sendBadRequestText(session, Sid.GET_QR_CODE_V2, errorMessage);
            } else {
                String computerGroup = computerGroupNode.textValue();
                String computerName = computerNameNode.textValue();
                String realVerification = realVerificationNode.textValue();
                String locale = localeNode.textValue();

                if (!realVerification.equals(Utility.generateVerificationToLogin(computerGroup, computerName, locale))) {
                    String errorMessage = ClopuccinoMessages.getMessage("Incorrect verification code");

                    LOGGER.error(String.format("[ATTACKING] %s\ncomputer group: %s\ncomputer name: %s\nlocale: %s\nsession from: %s", errorMessage, computerGroup, computerName, locale, session.getRequestURI().toString()));

                    sendBadRequestText(session, Sid.GET_QR_CODE_V2, errorMessage);
                } else {
                    // save the properties for later we get user id from device

                    String userId = null;

                    if (computerIdNode != null && computerIdNode.asLong(-1) > 0) {
                        long computerIdToVerify = computerIdNode.asLong();

                        if (recoveryKeyNode != null && recoveryKeyNode.textValue() != null) {
                            String recoveryKeyToVerify = recoveryKeyNode.textValue();

                            Computer foundComputer = computerDao.findComputerById(computerIdToVerify);

                            if (foundComputer != null && recoveryKeyToVerify.equals(foundComputer.getRecoveryKey())) {
                                this.computerId = computerIdToVerify;
                                this.recoveryKey = recoveryKeyToVerify;

                                // The service could be invoked after all users, including administrator of the computer removed from this computer,
                                // so the creator of the computer could not have the permission to access the computer anymore.
                                // To make sure the creator of the computer still access to this user computer, check if UserComputer with the userComputerId exists.
//                                userId = foundComputer.getUserId();

                                String computerCreator = foundComputer.getUserId();

                                String userComputerId = Utility.generateUserComputerIdFrom(computerCreator, computerId);

                                if (userComputerDao.findUserComputerById(userComputerId) != null) {
                                    userId = computerCreator;
                                }
                            }
                        }
                    }

                    this.computerGroup = computerGroup;
                    this.computerName = computerName;
                    this.locale = locale;

                    // device token

                    DeviceToken deviceTokenObject = DeviceTokenService.prepareDeviceToken(deviceTokenNode, locale);

                    if (deviceTokenObject != null) {
                        this.deviceToken = deviceTokenObject;
                    }

                    // sysprops

                    ObjectMapper mapper = Utility.createObjectMapper();

                    Properties properties = mapper.treeToValue(systemPropertiesNode, Properties.class);

                    if (properties != null) {
                        this.systemProperties = properties;
                    }

                    // login user OR return QR code

                    if (this.computerId != null && userId != null) {
                        // The desktop does not need QR code because the computer already exists.
                        // send Sid 21102 directly back to the desktop to ask it to build web socket.

                        // user computer

                        String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

                        UserComputer userComputer = userComputerDao.findUserComputerById(userComputerId);

                        String lugServerId = ConnectionDispatchService.dispatchConnectionBy(userId, computerId);

                        if (userComputer == null) {
                            // not exists, create

                            String encryptedUserComputerId = Utility.generateEncryptedUserComputerIdFrom(userId, computerId);

                            // for admin user, the default value to the allow-alias is true.
                            userComputer = new UserComputer(userComputerId, userId, computerId, userId, computerGroup, computerName, encryptedUserComputerId, lugServerId, false, false, true);

                            userComputer = userComputerDao.createUserComputer(userComputer);
                        } else {
                            // already exists, update

                            userComputer.setLugServerId(lugServerId);
                            userComputer.setSocketConnected(false);
                            userComputer.setNeedReconnect(false);

                            userComputerDao.updateUserComputerConnectionStatus(userComputer);
                        }

                        // Apply connection, don't update the approved timestamp and the approved status if it already exists.

                        if (applyConnectionDao.findApplyConnectionByApplyUserAndComputerId(userId, computerId) == null) {
                            // create connection, always approve connection when connected by admin

                            long currentTimestamp = System.currentTimeMillis();

                            ApplyConnection applyConnection = new ApplyConnection(null, currentTimestamp, userId, computerId, true, userId, currentTimestamp);

                            applyConnectionDao.createOrUpdateApplyConnection(applyConnection);
                        }

                        // save device token

                        String deviceTokenString = null;

                        if (deviceTokenObject != null) {
                            deviceTokenString = deviceTokenObject.getDeviceToken();

                            // The device token object from computer contains no userId, need to add it here
                            deviceTokenObject.setAccount(userId);

                            try {
                                deviceTokenService.createOrUploadDeviceToken(deviceTokenObject, locale);
                            } catch (Exception e) {
                                String message = "Error on creating or updating device token: " + deviceTokenObject;
                                LOGGER.error(message, e);

                                throw new Exception(message);
                            }
                        }

                        // save system properties

                        if (systemProperties != null) {
                            userComputerPropertiesDao.createUserComputerProperties(userComputerId, systemProperties);
                        }

                        // login user, get session id for computer and send result to computer

                        loginUserAndSendResultMessageToComputer(userId, computerId, lugServerId, deviceTokenString);
                    } else {
                        // create qrcode,
                        // create map with key: qrcode and value: this
                        // send message to computer

                        String qrcode = Utility.generateQRCode(computerGroup, computerName, locale, System.currentTimeMillis());

                        putInstance(qrcode, this);

                        /*
                        {
                            "sid" : 21101,
                            "status" : 200,
                            "error" : "",
                            "timestamp" : 1386505788544, // date time in millis
                            "qr-code" : "FOIF8QJO3I48OJFDOFHDUOGBLDSHCNOIFHIEHFUHGDT67GJKBHEIMDOI"
                        }
                        */

                        ObjectNode objectNode = mapper.createObjectNode();

                        objectNode.put(PropertyConstants.PROPERTY_NAME_SID, Sid.GET_QR_CODE_V2);
                        objectNode.put(PropertyConstants.PROPERTY_NAME_STATUS, HttpServletResponse.SC_OK);
                        objectNode.put(PropertyConstants.PROPERTY_NAME_TIMESTAMP, System.currentTimeMillis());
                        objectNode.put(PropertyConstants.PROPERTY_NAME_QR_CODE, qrcode);

                        session.getBasicRemote().sendText(mapper.writeValueAsString(objectNode));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to send qr code to computer: " + computerName, e);

            String errorMessage = e.getMessage();

            sendBadRequestText(session, Sid.GET_QR_CODE_V2, errorMessage);
        }
    }

    public void loginUserAndSendResultMessageToComputer(String userId, long computerId, String lugServerId, String deviceTokenString) throws Exception {
        // login user to get sssion id.
        // get nickname, show hidden from User
        // get computer group, computer name recovery key from Computer
        // send message to computer

        User user = userDao.findUserById(userId);

        if (user == null) {
            String errorMessage = ClopuccinoMessages.localizedMessage(locale, "user.not.found");

            throw new IllegalArgumentException(errorMessage);
        } else {
            Computer computer = computerDao.findComputerById(computerId);

            if (computer == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(locale, "computer.not.found");

                throw new IllegalArgumentException(errorMessage);
            } else {
                String sessionId = loginService.loginWithComputer(user, computerId, deviceTokenString, locale);

                String countryId = user.getCountryId();

                int countryCode = countryService.findCountryCodeByCountryId(countryId);

                String phoneNumber = user.getPhoneNumber();

                String phoneWithCountry = CountryService.phoneWithCountryFrom(countryCode, phoneNumber);

                String nickname = user.getNickname();

                boolean showHidden = user.getShowHidden() != null ? user.getShowHidden() : false;

                String computerGroup = computer.getGroupName();

                String computerName = computer.getComputerName();

                String recoveryKey = computer.getRecoveryKey();

                // send login result to desktop

                ObjectMapper mapper = Utility.createObjectMapper();

                ObjectNode rootNode = mapper.createObjectNode();

                rootNode.put(PropertyConstants.PROPERTY_NAME_SID, Sid.LOGIN_BY_QR_CODE_V2);
                rootNode.put(PropertyConstants.PROPERTY_NAME_STATUS, HttpServletResponse.SC_OK);
                rootNode.put(PropertyConstants.PROPERTY_NAME_TIMESTAMP, System.currentTimeMillis());
                rootNode.put(PropertyConstants.PROPERTY_NAME_ACCOUNT, userId);
                rootNode.put(PropertyConstants.PROPERTY_NAME_COUNTRY_ID, countryId);
                rootNode.put(PropertyConstants.PROPERTY_NAME_COUNTRY_CODE, Integer.valueOf(countryCode));
                rootNode.put(PropertyConstants.PROPERTY_NAME_PHONE_NUMBER, phoneNumber);
                rootNode.put(PropertyConstants.PROPERTY_NAME_PHONE_NUMBER_WITH_COUNTRY, phoneWithCountry);
                rootNode.put(PropertyConstants.PROPERTY_NAME_NICKNAME, nickname);
                rootNode.put(PropertyConstants.PROPERTY_NAME_SHOW_HIDDEN, showHidden);
                rootNode.put(PropertyConstants.PROPERTY_NAME_SESSION_ID, sessionId);
                rootNode.put(PropertyConstants.PROPERTY_NAME_COMPUTER_ID, computerId);
                rootNode.put(PropertyConstants.PROPERTY_NAME_COMPUTER_GROUP, computerGroup);
                rootNode.put(PropertyConstants.PROPERTY_NAME_COMPUTER_NAME, computerName);
                rootNode.put(PropertyConstants.PROPERTY_NAME_RECOVERY_KEY, recoveryKey);
                rootNode.put(PropertyConstants.PROPERTY_NAME_LUG_SERVER_ID, lugServerId);

                session.getBasicRemote().sendText(mapper.writeValueAsString(rootNode));
            }
        }
    }

    private void sendBadRequestText(Session session, int sid, String errorMessage) {
        try {
            ResponseModel responseModel = new ResponseModel(sid, HttpServletResponse.SC_BAD_REQUEST, errorMessage, null, System.currentTimeMillis());

            ObjectMapper mapper = Utility.createObjectMapper();

            session.getBasicRemote().sendText(mapper.writeValueAsString(responseModel));

        } catch (Exception e) {
            LOGGER.error(errorMessage, e);
        } finally {
            closeSessionWithBadDataStatusCode(session, errorMessage);
        }
    }

    private void closeSessionWithBadDataStatusCode(final Session session, final String reason) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, reason));
        } catch (Exception e) {
            /* ignored */
        }
    }

}
