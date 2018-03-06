package org.clopuccino.service;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.*;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.dao.UserComputerPropertiesDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.DeviceToken;
import org.clopuccino.domain.User;
import org.clopuccino.domain.UserComputer;
import org.clopuccino.domain.Version;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

/**
 * <code>LoginService</code> provides common utility for login and relogin servlets
 *
 * @author masonhsieh
 * @version 1.0
 */
public class LoginService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("LOGIN_SRV");

    private final UserComputerDao userComputerDao;

    private final UserComputerPropertiesDao userComputerPropertiesDao;

    private final ClientSessionService clientSessionService;

    private final DeviceTokenService deviceTokenService;


    public LoginService(DatabaseAccess dbAccess) {
        DatabaseAccess localDbAccess;

        if (dbAccess == null) {
            localDbAccess = DatabaseUtility.createDatabaseAccess();
        } else {
            localDbAccess = dbAccess;
        }

        userComputerDao = new UserComputerDao(localDbAccess);

        userComputerPropertiesDao = new UserComputerPropertiesDao(localDbAccess);

        clientSessionService = new ClientSessionService(localDbAccess);

        deviceTokenService = new DeviceTokenService(localDbAccess);
    }

    public String createDeviceTokenAndLoginUser(User user, DeviceToken deviceTokenObject, Long computerId, String locale) throws Exception {
        String userSessionId = null;

        if (user.getAccount() != null) {
            if (deviceTokenObject != null) {
                deviceTokenObject.setAccount(user.getAccount());

                try {
                    deviceTokenService.createOrUploadDeviceToken(deviceTokenObject, locale);
                } catch (Exception e) {
                    String message = "Error on creating or updating device token: " + deviceTokenObject;
                    LOGGER.error(message, e);

                    throw new Exception(message);
                }
            }

            try {
                if (computerId == null) {
                    userSessionId = loginWithoutComputer(user, (deviceTokenObject != null ? deviceTokenObject.getDeviceToken() : null), locale);
                } else {
                    userSessionId = loginWithComputer(user, computerId, (deviceTokenObject != null ? deviceTokenObject.getDeviceToken() : null), locale);
                }
            } catch (Exception e) {
                LOGGER.error("Error on login user: " + user.getAccount(), e);

                throw new Exception("Error on login user with phone number: " + user.getPhoneNumber());
            }
        }

        return userSessionId;
    }

    public void doLoginWithoutComputer(HttpServletRequest req, HttpServletResponse resp, User user, DeviceToken deviceToken) throws ServletException, IOException {
        // 不用確認 computer 與 user_computer 存在

        try {
            String token;

            if (deviceToken != null) {
                token = deviceToken.getDeviceToken();
            } else {
                token = null;
            }

            responseLogin2Success(user, resp, token);
        } catch (Exception e) {
            String errorMessage = e.getClass().getName() + ": " + e.getMessage();

            LOGGER.error("Login2 failed. error: " + errorMessage, e);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        }
    }

    public void doLoginWithComputer(HttpServletRequest req, HttpServletResponse resp, String clientLocale, Long computerId, String deviceVersion, String deviceBuild, User user, DeviceToken deviceToken) throws ServletException, IOException {
        String userId = user.getAccount();

        String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

        UserComputer userComputer = userComputerDao.findUserComputerById(userComputerId);

        if (userComputer != null) {
            Boolean socketConnected = userComputer.isSocketConnected();

            try {
                if (socketConnected != null && socketConnected) {
                    LOGGER.debug("Connection found between repository and server for user '" + user.getNickname() + "'(id=" + userId + "), computer id: " + computerId);

                    if (deviceVersion != null && deviceBuild != null && deviceVersion.trim().length() > 0 && deviceBuild.trim().length() > 0) {
                        // check if desktop version and device version matches

                        String desktopVersion = userComputerPropertiesDao.findPropertyValue(userComputerId, PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);
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
//                        String desktopVersion = userComputerPropertiesDao.findPropertyValue(ownerUserComputerId, PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);

                        if (desktopVersion == null) {
                            desktopVersion = Constants.DEFAULT_DESKTOP_VERSION;
                        }

                        if (Version.valid(desktopVersion) && new Version(desktopVersion).compareTo(new Version("1.0.5")) < 0) {
                            // Incompatible with version of desktop that is lower than 1.0.5

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(Constants.HTTP_STATUS_DESKTOP_VERSION_TOO_OLD);
                            resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "desktop.need.update", userComputer.getComputerName()));
                            resp.getWriter().flush();
                        } else {
                            // Compare the major part of the version

                            int versionCompared = compareMajorVersion(deviceVersion, deviceBuild, desktopVersion);

                            if (versionCompared == 0) {
                                String lugServerId = userComputer.getLugServerId();

                                responseLoginSuccess(user, userComputer, resp, Boolean.TRUE, lugServerId, deviceToken);
                            } else if (versionCompared > 0) {
                                // device is newer

                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.setStatus(Constants.HTTP_STATUS_DESKTOP_VERSION_TOO_OLD);
                                resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "desktop.need.update", userComputer.getComputerName()));
                                resp.getWriter().flush();
                            } else {
                                // desktop is newer

                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.setStatus(Constants.HTTP_STATUS_DEVICE_VERSION_TOO_OLD);
                                resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "device.need.update", userComputer.getComputerName()));
                                resp.getWriter().flush();
                            }
                        }
                    } else {
                        // skip comparing version

                        String lugServerId = userComputer.getLugServerId();

                        responseLoginSuccess(user, userComputer, resp, Boolean.TRUE, lugServerId, deviceToken);
                    }
                } else {
                    LOGGER.info("Connection between desktop and server not exists or invalid for user: '" + user.getNickname() + "'(id=" + userId + ").");

                    // update reconnect to true -- only for checkDesktopSocket

                    Boolean reconnect = userComputer.isNeedReconnect();

                    if (reconnect == null || !reconnect) {
                        // update reconnect flag to true
                        userComputerDao.updateReconnectByUserComputerId(userComputerId, Boolean.TRUE);
                    }

                    responseLoginSuccess(user, userComputer, resp, Boolean.FALSE, null, deviceToken);
                }
            } catch (Exception e) {
                String errorMessage = e.getClass().getName() + ": " + e.getMessage();

                LOGGER.error("Login failed. error: " + errorMessage, e);

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            }
        } else {
            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "server.not.setup");

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        }
    }

    /**
     * Login the specified user without computer information and return the session id.
     *
     * @param user User to login.
     * @param deviceToken The unique token string of the device to request the login.
     * @param clientLocale The locale of the device to request the login.
     *
     * @return The session id.
     */
    public String loginWithoutComputer(User user, String deviceToken, String clientLocale) throws Exception {
        String userId = user.getAccount();

        Boolean showHidden = user.getShowHidden();

        long pseudoComputerId = ComputerService.pseudoComputerId();

        String pseudoUserComputerId = Utility.generateUserComputerIdFrom(userId, pseudoComputerId);

        /* register new session id */
        String clientSessionId = ClientSessionService.generateUniqueSessionId(pseudoUserComputerId);

        clientSessionService.appendClientSession(clientSessionId, userId, showHidden, clientLocale, pseudoComputerId, pseudoUserComputerId, deviceToken);

        return clientSessionId;
    }

    /**
     * Login the specified user with computer information and return the session id.
     *
     * @param user User to login.
     * @param computerId computer id.
     * @param deviceToken The unique token string of the device to request the login.
     * @param clientLocale The locale of the device to request the login.
     *
     * @return The session id.
     */
    public String loginWithComputer(User user, long computerId, String deviceToken, String clientLocale) throws Exception {
        String userId = user.getAccount();

        Boolean showHidden = user.getShowHidden();

        String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

        /* register new session id */
        String clientSessionId = ClientSessionService.generateUniqueSessionId(userComputerId);

        clientSessionService.appendClientSession(clientSessionId, userId, showHidden, clientLocale, computerId, userComputerId, deviceToken);

        return clientSessionId;
    }

    public int compareMajorVersion(String deviceVersion, String deviceBuild, String desktopVersion) {
        int result = 0;

        if (deviceVersion != null && desktopVersion != null) {
            try {
                Version deviceVersionObj = new Version(deviceVersion);

                Version desktopVersionObj = new Version(desktopVersion);

                int deviceMajorVersionNumber = deviceVersionObj.getMajorNumber();

                int desktopMajorVersionNumber = desktopVersionObj.getMajorNumber();

                if (deviceMajorVersionNumber == desktopMajorVersionNumber) {
                    result = 0;
                } else if (deviceMajorVersionNumber > desktopMajorVersionNumber) {
                    result = 1;

                    LOGGER.info("Device version '" + deviceVersion + "' is newer than desktop version '" + desktopVersion + "'");
                } else {
                    result = -1;

                    LOGGER.info("Device version '" + deviceVersion + "' is older than desktop version '" + desktopVersion + "'");
                }
            } catch (Exception e) {
                result = 0;
            }
        }

        return result;
    }

    public Properties prepareResponsePropertiesForConnectingToComputer(UserComputer userComputer, Boolean socketConnected, String lugServerId) throws Exception {
        String userComputerId = userComputer.getUserComputerId();
        Long computerId = userComputer.getComputerId();
        String computerAdminId = userComputer.getComputerAdminId();
        String computerGroup = userComputer.getGroupName();
        String computerName = userComputer.getComputerName();

        String uploadDirectory = userComputer.getUploadDirectory();

        Integer uploadSubdirectoryType = userComputer.getUploadSubdirectoryType();
        String uploadSubdirectoryValue = userComputer.getUploadSubdirectoryValue();
        Integer uploadDescriptionType = userComputer.getUploadDescriptionType();
        String uploadDescriptionValue = userComputer.getUploadDescriptionValue();
        Integer uploadNotificationType = userComputer.getUploadNotificationType();

        String downloadDirectory = userComputer.getDownloadDirectory();

        Integer downloadSubdirectoryType = userComputer.getDownloadSubdirectoryType();
        String downloadSubdirectoryValue = userComputer.getDownloadSubdirectoryValue();
        Integer downloadDescriptionType = userComputer.getDownloadDescriptionType();
        String downloadDescriptionValue = userComputer.getDownloadDescriptionValue();
        Integer downloadNotificationType = userComputer.getDownloadNotificationType();

        /* 取得 server 註冊在 repository 的 system properties */
        Properties returnProperties = userComputerPropertiesDao.findPropertiesByUserComputerId(userComputerId);

        returnProperties.put("computer-id", computerId);
        returnProperties.put("computer-admin-id", computerAdminId);
        returnProperties.put("computer-group", computerGroup);
        returnProperties.put("computer-name", computerName);
        returnProperties.put("socket-connected", socketConnected);

        if (lugServerId != null && lugServerId.trim().length() > 0) {
            returnProperties.put("lug-server-id", lugServerId);
        }

        // "upload-directory"
        if (uploadDirectory != null && uploadDirectory.trim().length() > 0) {
            returnProperties.put("upload-directory", uploadDirectory);
        }

        // "upload-subdirectory-type"
        if (uploadSubdirectoryType != null) {
            returnProperties.put("upload-subdirectory-type", uploadSubdirectoryType);
        }

        // "upload-subdirectory-value"
        if (uploadSubdirectoryValue != null && uploadSubdirectoryValue.trim().length() > 0) {
            returnProperties.put("upload-subdirectory-value", uploadSubdirectoryValue);
        }

        // "upload-description-type"
        if (uploadDescriptionType != null) {
            returnProperties.put("upload-description-type", uploadDescriptionType);
        }

        // "upload-description-value"
        if (uploadDescriptionValue != null && uploadDescriptionValue.trim().length() > 0) {
            returnProperties.put("upload-description-value", uploadDescriptionValue);
        }

        // "upload-notification-type"
        if (uploadNotificationType != null) {
            returnProperties.put("upload-notification-type", uploadNotificationType);
        }

        // "download-directory"
        if (downloadDirectory != null && downloadDirectory.trim().length() > 0) {
            returnProperties.put("download-directory", downloadDirectory);
        }

        // "download-subdirectory-type"
        if (downloadSubdirectoryType != null) {
            returnProperties.put("download-subdirectory-type", downloadSubdirectoryType);
        }

        // "download-subdirectory-value"
        if (downloadSubdirectoryValue != null && downloadSubdirectoryValue.trim().length() > 0) {
            returnProperties.put("download-subdirectory-value", downloadSubdirectoryValue);
        }

        // "download-description-type"
        if (downloadDescriptionType != null) {
            returnProperties.put("download-description-type", downloadDescriptionType);
        }

        // "download-description-value"
        if (downloadDescriptionValue != null && downloadDescriptionValue.trim().length() > 0) {
            returnProperties.put("download-description-value", downloadDescriptionValue);
        }

        // "download-notification-type"
        if (downloadNotificationType != null) {
            returnProperties.put("download-notification-type", downloadNotificationType);
        }

        return returnProperties;
    }

    private void responseLoginSuccess(User user, UserComputer userComputer, HttpServletResponse resp, Boolean socketConnected, String lugServerId, DeviceToken deviceToken) throws Exception {
        String userId = user.getAccount();
        Boolean showHidden = user.getShowHidden();
        String nickname = user.getNickname();
        String email = user.getUserEmail();
        String phoneNumber = user.getPhoneNumber();
        String countryId = user.getCountryId();

        String userComputerId = userComputer.getUserComputerId();
        Long computerId = userComputer.getComputerId();
        String computerAdminId = userComputer.getComputerAdminId();
        String computerGroup = userComputer.getGroupName();
        String computerName = userComputer.getComputerName();

        String uploadDirectory = userComputer.getUploadDirectory();

        Integer uploadSubdirectoryType = userComputer.getUploadSubdirectoryType();
        String uploadSubdirectoryValue = userComputer.getUploadSubdirectoryValue();
        Integer uploadDescriptionType = userComputer.getUploadDescriptionType();
        String uploadDescriptionValue = userComputer.getUploadDescriptionValue();
        Integer uploadNotificationType = userComputer.getUploadNotificationType();

        String downloadDirectory = userComputer.getDownloadDirectory();

        Integer downloadSubdirectoryType = userComputer.getDownloadSubdirectoryType();
        String downloadSubdirectoryValue = userComputer.getDownloadSubdirectoryValue();
        Integer downloadDescriptionType = userComputer.getDownloadDescriptionType();
        String downloadDescriptionValue = userComputer.getDownloadDescriptionValue();
        Integer downloadNotificationType = userComputer.getDownloadNotificationType();

        /* register new session id */
        String clientSessionId = ClientSessionService.generateUniqueSessionId(userComputerId);

        // country and phone number is not required.
        User foundUser = new User(userId, null, null, user.getPasswd(), user.getNickname(), showHidden, user.getLocale());

        String token;
        if (deviceToken != null) {
            token = deviceToken.getDeviceToken();
        } else {
            token = null;
        }

        clientSessionService.appendClientSession(clientSessionId, foundUser, userComputer, token);

        /* 取得 server 註冊在 repository 的 system properties */
        Properties returnProperties = userComputerPropertiesDao.findPropertiesByUserComputerId(userComputerId);

        returnProperties.put("account", userId);
        returnProperties.put("sessionId", clientSessionId);

        // for client to update nickname after change-nickname
        returnProperties.put("nickname", nickname);

        // for client to update phone data after change-phone-number
        returnProperties.put("country-id", countryId);
        returnProperties.put("phone", phoneNumber);

        returnProperties.put("showHidden", showHidden);
        returnProperties.put("computer-id", computerId);
        returnProperties.put("computer-admin-id", computerAdminId);
        returnProperties.put("computer-group", computerGroup);
        returnProperties.put("computer-name", computerName);
        returnProperties.put("socket-connected", socketConnected);

        if (email != null && email.trim().length() > 0) {
            returnProperties.put("email", email);
        }

        if (lugServerId != null && lugServerId.trim().length() > 0) {
            returnProperties.put("lug-server-id", lugServerId);
        }

        // "upload-directory"
        if (uploadDirectory != null && uploadDirectory.trim().length() > 0) {
            returnProperties.put("upload-directory", uploadDirectory);
        }

        // "upload-subdirectory-type"
        if (uploadSubdirectoryType != null) {
            returnProperties.put("upload-subdirectory-type", uploadSubdirectoryType);
        }

        // "upload-subdirectory-value"
        if (uploadSubdirectoryValue != null && uploadSubdirectoryValue.trim().length() > 0) {
            returnProperties.put("upload-subdirectory-value", uploadSubdirectoryValue);
        }

        // "upload-description-type"
        if (uploadDescriptionType != null) {
            returnProperties.put("upload-description-type", uploadDescriptionType);
        }

        // "upload-description-value"
        if (uploadDescriptionValue != null && uploadDescriptionValue.trim().length() > 0) {
            returnProperties.put("upload-description-value", uploadDescriptionValue);
        }

        // "upload-notification-type"
        if (uploadNotificationType != null) {
            returnProperties.put("upload-notification-type", uploadNotificationType);
        }

        // "download-directory"
        if (downloadDirectory != null && downloadDirectory.trim().length() > 0) {
            returnProperties.put("download-directory", downloadDirectory);
        }

        // "download-subdirectory-type"
        if (downloadSubdirectoryType != null) {
            returnProperties.put("download-subdirectory-type", downloadSubdirectoryType);
        }

        // "download-subdirectory-value"
        if (downloadSubdirectoryValue != null && downloadSubdirectoryValue.trim().length() > 0) {
            returnProperties.put("download-subdirectory-value", downloadSubdirectoryValue);
        }

        // "download-description-type"
        if (downloadDescriptionType != null) {
            returnProperties.put("download-description-type", downloadDescriptionType);
        }

        // "download-description-value"
        if (downloadDescriptionValue != null && downloadDescriptionValue.trim().length() > 0) {
            returnProperties.put("download-description-value", downloadDescriptionValue);
        }

        // "download-notification-type"
        if (downloadNotificationType != null) {
            returnProperties.put("download-notification-type", downloadNotificationType);
        }

        ObjectMapper mapper = Utility.createObjectMapper();

        PropertiesSerializer propertiesSerializer = new PropertiesSerializer();
        propertiesSerializer.addToObjectMapper(mapper);

        String returnJson = mapper.writeValueAsString(returnProperties);

        resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(returnJson);
        resp.getWriter().flush();
    }

    private void responseLogin2Success(User user, HttpServletResponse resp, String deviceToken) throws Exception {
        String userId = user.getAccount();
        String nickname = user.getNickname();
        String email = user.getUserEmail();
        String phoneNumber = user.getPhoneNumber();
        String countryId = user.getCountryId();
        Boolean showHidden = user.getShowHidden();

        long pseudoComputerId = ComputerService.pseudoComputerId();

        String pseudoUserComputerId = Utility.generateUserComputerIdFrom(userId, pseudoComputerId);

        /* register new session id */
        String clientSessionId = ClientSessionService.generateUniqueSessionId(pseudoUserComputerId);

        // country and phone number is not required.
        User foundUser = new User(userId, null, null, user.getPasswd(), nickname, showHidden, user.getLocale());

        clientSessionService.appendClientSession(clientSessionId, foundUser, pseudoComputerId, pseudoUserComputerId, deviceToken);

        Properties returnProperties = new Properties();

        returnProperties.put("account", userId);
        returnProperties.put("sessionId", clientSessionId);

        // for client to update nickname after change-nickname
        returnProperties.put("nickname", nickname);

        // for client to update phone data after change-phone-number
        returnProperties.put("country-id", countryId);
        returnProperties.put("phone", phoneNumber);

        returnProperties.put("showHidden", showHidden);

        if (email != null && email.trim().length() > 0) {
            returnProperties.put("email", email);
        }

        ObjectMapper mapper = Utility.createObjectMapper();
        
        PropertiesSerializer propertiesSerializer = new PropertiesSerializer();
        propertiesSerializer.addToObjectMapper(mapper);

        String returnJson = mapper.writeValueAsString(returnProperties);

        resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(returnJson);
        resp.getWriter().flush();
    }
}
