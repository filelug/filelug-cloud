package org.clopuccino.service;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.dao.DeviceTokenDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.DeviceToken;
import org.clopuccino.domain.Version;
import org.slf4j.LoggerFactory;

/**
 * <code>DeviceTokenService</code> provides device token related services for servlets and other services.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class DeviceTokenService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("DEV_TOKEN_SRV");

    private final DeviceTokenDao deviceTokenDao;

    private final UserDao userDao;

//    private final LoginService loginService;


    public DeviceTokenService(DatabaseAccess dbAccess) {
        DatabaseAccess localDbAccess;

        if (dbAccess == null) {
            localDbAccess = DatabaseUtility.createDatabaseAccess();
        } else {
            localDbAccess = dbAccess;
        }

        deviceTokenDao = new DeviceTokenDao(localDbAccess);

        userDao = new UserDao(localDbAccess);

//        loginService = new LoginService(localDbAccess);
    }

    // return null if not created successfully
    public DeviceToken createOrUploadDeviceToken(DeviceToken deviceToken, String clientLocale) {
        DeviceToken successDeviceToken = null;

        String deviceTokenString = deviceToken.getDeviceToken();
        String notificationType = deviceToken.getNotificationType();
        String deviceType = deviceToken.getDeviceType();
        String deviceVersion = deviceToken.getDeviceVersion();
        String account = deviceToken.getAccount();

        if (deviceTokenString == null || deviceTokenString.trim().length() < 1) {
            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "device token");

            LOGGER.error("Skipped creating/update device token: " + deviceToken.toString() + "\n" + errorMessage);
        } else if (notificationType == null || notificationType.trim().length() < 1 || !DeviceToken.validNotificationType(notificationType)) {
            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "notification type");

            LOGGER.error("Skipped creating/update device token: " + deviceToken.toString() + "\n" + errorMessage);
        } else if (deviceType == null || deviceType.trim().length() < 1 || !DeviceToken.validDeviceType(deviceType)) {
            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "device type");

            LOGGER.error("Skipped creating/update device token: " + deviceToken.toString() + "\n" + errorMessage);
        } else if (deviceVersion == null || deviceVersion.trim().length() < 1) {
            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "device version");

            LOGGER.error("Skipped creating/update device token: " + deviceToken.toString() + "\n" + errorMessage);
        } else if (account == null || account.trim().length() < 1) {
            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "user account");

            LOGGER.error("Skipped creating/update device token: " + deviceToken.toString() + "\n" + errorMessage);
        } else if (!userDao.findExistsById(account)) {
            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

            LOGGER.error("Skipped creating/update device token: " + deviceToken.toString() + "\n" + errorMessage);
        } else {
            successDeviceToken = deviceTokenDao.createOrUpdateDeviceToken(deviceToken);

            if (successDeviceToken != null) {
                LOGGER.debug("Device token created/updated: " + successDeviceToken);
            } else {
                LOGGER.debug("Failed to create/update device token: " + deviceToken);
            }
        }

        return successDeviceToken;
    }

    /**
     *
     * @param deviceTokenObjectNode The format of the JSON like:<br>
     *                              { <br>
     *                                  "device-token" : "1e39b345af9b036a2fc1066f2689143746f7d1220c23ff1491619a544a167c61",<br>
     *                                  "notification-type" : "APNS",<br>
     *                                  "device-type" : "IOS",<br>
     *                                  "device-version" : "10.1.1",           // 作業系統版本<br>
     *                                  "filelug-version" : "1.5.2",           // Filelug APP 大版號<br>
     *                                  "filelug-build" : "2016.09.24.01",     // Filelug APP 小版號<br>
     *                                  "badge-number" : 0                     // 此值可不提供<br>
     *                              }
     * @param locale The locale used to generate i18n error message.
     * @return The <code>DeviceToken</code> object or null if deviceTokenObjectNode is null.
     * @throws IllegalArgumentException Thrown if error occurred
     */
    public static DeviceToken prepareDeviceToken(JsonNode deviceTokenObjectNode, String locale) throws IllegalArgumentException {
        boolean illegalDeviceToken = false;

        String illegalDeviceTokenMessage = null;

        DeviceToken deviceTokenObject = null;

        if (deviceTokenObjectNode != null) {
            /* "device-token":
                {
                    "device-token" : "1e39b345af9b036a2fc1066f2689143746f7d1220c23ff1491619a544a167c61",
                    "notification-type" : "APNS",
                    "device-type" : "IOS",
                    "device-version" : "10.1.1",           // iOS/Android 作業系統版本
                    "filelug-version" : "1.5.2",           // Filelug APP 大版號
                    "filelug-build" : "2016.09.24.01",     // Filelug APP 小版號
                    "badge-number" : 0                     // 此值可不提供
                }
            */

            // device-token

            String deviceTokenString = null;

            JsonNode deviceTokenStringNode = deviceTokenObjectNode.get("device-token");

            if ((deviceTokenStringNode == null || deviceTokenStringNode.textValue() == null)) {
                illegalDeviceToken = true;

                illegalDeviceTokenMessage = ClopuccinoMessages.localizedMessage(locale, "param.null.or.empty", "device token");
            } else {
                deviceTokenString = deviceTokenStringNode.textValue();
            }

            // notification-type

            String notificationType = null;

            JsonNode notificationTypeNode = deviceTokenObjectNode.get("notification-type");

            if (!illegalDeviceToken) {
                if (notificationTypeNode == null || notificationTypeNode.textValue() == null){
                    illegalDeviceToken = true;

                    illegalDeviceTokenMessage = ClopuccinoMessages.localizedMessage(locale, "param.null.or.empty", "notification type");
                } else if (!DeviceToken.validNotificationType(notificationTypeNode.textValue())) {
                    illegalDeviceToken = true;

                    illegalDeviceTokenMessage = ClopuccinoMessages.localizedMessage(locale, "param.invalid", "notification type");
                }
            }

            if (notificationTypeNode != null) {
                notificationType = notificationTypeNode.textValue();
            }

            // device-type

            String deviceType = null;

            JsonNode deviceTypeNode = deviceTokenObjectNode.get("device-type");

            if (!illegalDeviceToken) {
                if (deviceTypeNode == null || deviceTypeNode.textValue() == null) {
                    illegalDeviceToken = true;

                    illegalDeviceTokenMessage = ClopuccinoMessages.localizedMessage(locale, "param.null.or.empty", "device type");
                } else if (!DeviceToken.validDeviceType(deviceTypeNode.textValue())) {
                    illegalDeviceToken = true;

                    illegalDeviceTokenMessage = ClopuccinoMessages.localizedMessage(locale, "param.invalid", "device type");
                }
            }

            if (deviceTypeNode != null) {
                deviceType = deviceTypeNode.textValue();
            }

            // device-version

            String deviceVersion = null;

            JsonNode deviceVersionNode = deviceTokenObjectNode.get("device-version");

            if (!illegalDeviceToken) {
                if (deviceVersionNode == null || deviceVersionNode.textValue() == null) {
                    illegalDeviceToken = true;

                    illegalDeviceTokenMessage = ClopuccinoMessages.localizedMessage(locale, "param.null.or.empty", "device version");

                    // Do not validate device version because it could be like: '4.4.0-47-generic' for Linux
//                } else if (!DeviceToken.validDeviceVersion(deviceVersionNode.textValue())) {
//                    illegalDeviceToken = true;
//
//                    illegalDeviceTokenMessage = ClopuccinoMessages.localizedMessage(locale, "param.invalid", "device version");
                }
            }

            if (deviceVersionNode != null) {
                deviceVersion = Version.extractVersionFrom(deviceVersionNode.textValue());

                if (deviceVersion == null) {
                    deviceVersion = deviceVersionNode.textValue();

                    LOGGER.warn(String.format("Device version '%s' is not an invalid version", deviceVersion));
                }
            }

            // filelug-version

            String filelugVersion = null;

            JsonNode filelugVersionNode = deviceTokenObjectNode.get("filelug-version");

            if (!illegalDeviceToken) {
                if (filelugVersionNode == null || filelugVersionNode.textValue() == null) {
                    illegalDeviceToken = true;

                    illegalDeviceTokenMessage = ClopuccinoMessages.localizedMessage(locale, "param.null.or.empty", "filelug version");
                }
            }

            if (filelugVersionNode != null) {
                filelugVersion = filelugVersionNode.textValue();
            }

            // filelug-build

            String filelugBuild = null;

            JsonNode filelugBuildNode = deviceTokenObjectNode.get("filelug-build");

            if (!illegalDeviceToken) {
                if (filelugBuildNode == null || filelugBuildNode.textValue() == null) {
                    illegalDeviceToken = true;

                    illegalDeviceTokenMessage = ClopuccinoMessages.localizedMessage(locale, "param.null.or.empty", "filelug build");
                }
            }

            if (filelugBuildNode != null) {
                filelugBuild = filelugBuildNode.textValue();
            }

            // badge-number: optional, default 0

            int badgeNumber = 0;

            JsonNode badgeNumberNode = deviceTokenObjectNode.get("badge-number");

            if (!illegalDeviceToken && badgeNumberNode != null && !badgeNumberNode.isNumber()) {
                illegalDeviceToken = true;

                illegalDeviceTokenMessage = ClopuccinoMessages.localizedMessage(locale, "param.null.or.empty", "badge number");
            }

            if (badgeNumberNode != null && badgeNumberNode.isNumber()) {
                badgeNumber = badgeNumberNode.intValue();
            }

            if (!illegalDeviceToken) {
                deviceTokenObject = new DeviceToken(null, deviceTokenString, notificationType, deviceType, deviceVersion, filelugVersion, filelugBuild, badgeNumber, null, null);
            }
        }

        if (illegalDeviceToken) {
            if (illegalDeviceTokenMessage != null) {
                throw new IllegalArgumentException(illegalDeviceTokenMessage);
            } else {
                throw new IllegalArgumentException(ClopuccinoMessages.localizedMessage(locale, "param.invalid", "device token object"));
            }
        } else {
            return deviceTokenObject;
        }
    }

    public boolean isDeviceVersion2OrAbove(String deviceTokenId, String userId) {
        String filelugVersion = deviceTokenDao.findDeviceFilelugVersionByTokenAndUser(deviceTokenId, userId);

        return filelugVersion != null && Version.valid(filelugVersion) && new Version(filelugVersion).compareTo(new Version(Constants.INITIAL_VERSION_TO_V2)) >= 0;
    }
}
