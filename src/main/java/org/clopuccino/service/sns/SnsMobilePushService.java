package org.clopuccino.service.sns;

import ch.qos.logback.classic.Logger;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.ListPlatformApplicationsResult;
import com.amazonaws.services.sns.model.PlatformApplication;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.dao.DeviceTokenDao;
import org.clopuccino.dao.SnsApplicationDao;
import org.clopuccino.dao.TaskDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.*;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <code>SnsMobilePushService</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class SnsMobilePushService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SnsMobilePushService.class.getSimpleName());

    private AmazonSNSClient client;

    private SnsClientWrapper snsClientWrapper;

    private DatabaseAccess dbAccess;

    private DeviceTokenDao deviceTokenDao;

    private SnsApplicationDao snsApplicationDao;

    public SnsMobilePushService(DatabaseAccess dbAccess) {
        try {
            client = new PlatformApplicationFactory().prepareAmazonSnsClient();

            snsClientWrapper = new SnsClientWrapper(client);

            this.dbAccess = dbAccess;

            deviceTokenDao = new DeviceTokenDao(dbAccess);

            snsApplicationDao = new SnsApplicationDao(dbAccess);
        } catch (Exception e) {
            LOGGER.error("Failed to init mobile push service.", e);
        }
    }

    public void reloadSnsPlatformApplications() {
        // reload to memory from db
        reloadToMemoryFromDb();

        // For AAServer, reload to db from Amazon SNS and reload again
        if (Utility.isAAServer()) {
            reloadToDbFromAmazonSns();

            reloadToMemoryFromDb();
        }
    }

    /**
     * No delete: The platform application that exists in DB but not in Amazon SNS will not be deleted in DB.
     * Create: The platform application that does not exist in DB but in Amazon SNS will be created in DB.
     * Updated: The platform application ARN in DB that is different than the one in Amazon SNS will be updated.
     */
    private void reloadToDbFromAmazonSns() {
        // DON'T check if the status because
        // the time interval is too long and it's impossible that the previous one is not finished when task is invoked.

        TaskDao taskDao = new TaskDao(dbAccess);

        Task task = taskDao.findTaskById(Constants.TASK_NAME_RELOAD_SNS_APPLICATIONS);

        task.setLatestTaskStartTimestamp(System.currentTimeMillis());

        try {
            ListPlatformApplicationsResult listPlatformApplicationsResult = client.listPlatformApplications();

            List<PlatformApplication> platformApplications = listPlatformApplicationsResult.getPlatformApplications();

            if (platformApplications != null && platformApplications.size() > 0) {
                DeviceToken.NotificationType[] types = DeviceToken.NotificationType.values();

                for (PlatformApplication platformApplication : platformApplications) {
                    boolean notificationTypeFound = false;

                    String applicationArnInAws = platformApplication.getPlatformApplicationArn();

                    for (DeviceToken.NotificationType type : types) {
                        String platform = type.name();

                        if (applicationArnInAws.contains("/" + platform + "/")) {
                            notificationTypeFound = true;

                            SnsApplication currentSnsApplication = snsApplicationDao.findApplicationByPlatform(platform);

                            if (currentSnsApplication != null) {
                                String currentApplicationArn = currentSnsApplication.getApplicationArn();

                                if (!currentApplicationArn.equals(applicationArnInAws)) {
                                    // found and different --> update value in DB

                                    currentSnsApplication.setApplicationArn(applicationArnInAws);

                                    snsApplicationDao.updateSnsApplication(currentSnsApplication);

                                    LOGGER.info("Application ARN of platform '" + platform + "' in DB updated to new value '" + applicationArnInAws + "' from '" + currentApplicationArn + "'.");
                                }
                            } else {
                                // not found --> create in DB
                                SnsApplication newSnsApplication = new SnsApplication(platform, applicationArnInAws, System.currentTimeMillis());

                                newSnsApplication = snsApplicationDao.createSnsApplication(newSnsApplication);

                                LOGGER.info("Created new platform application in DB: " + newSnsApplication);
                            }

                            break;
                        }
                    }

                    if (!notificationTypeFound) {
                        LOGGER.warn("Application ARN in AWS SNS not found in notification types: '" + applicationArnInAws + "'");
                    }
                }
            }

            task.setLatestTaskStatus(TaskStatus.TASK_STATUS_SUCCESS);
            task.setLatestTaskEndTimestamp(System.currentTimeMillis());
            task.setLatestTaskErrorMessage("");
            taskDao.updateTask(task);

            LOGGER.info("Done checking and, if any, reloading db data from Amazon SNS.");
        } catch (Exception e) {
            task.setLatestTaskStatus(TaskStatus.TASK_STATUS_FAILURE);
            task.setLatestTaskEndTimestamp(System.currentTimeMillis());
            task.setLatestTaskErrorMessage(e.getMessage());
            taskDao.updateTask(task);

            LOGGER.error("Failed to reload db data from Amazon SNS.\n" + e.getMessage(), e);

            /* TODO:
            If repository can't connect to Amazon SNS, which may comes from broken internet connect,
            and we should consider to notify the administrator by another way from Amazon.
            The message should be like this:
            ================================
            Unable to execute HTTP request: sns.us-west-2.amazonaws.com
            com.amazonaws.AmazonClientException: Unable to execute HTTP request: sns.us-west-2.amazonaws.com
            :
            Caused by: java.net.UnknownHostException: sns.us-west-2.amazonaws.com
            :
            ================================
            */
        }
    }

    private void reloadToMemoryFromDb() {
        // 1/2: delete the application that does not eixst in db, and update application arn if different.

        List<SnsApplication> snsApplications = snsApplicationDao.findAllSnsApplications();

        if (snsApplications != null) {
            Hashtable<String, String> applicationsInMemory = AbstractEndpointArn.getPlatformApplications();

            if (applicationsInMemory != null && applicationsInMemory.size() > 0) {
                Set<Map.Entry<String, String>> applicationEntryInMemory = applicationsInMemory.entrySet();

                for (Map.Entry<String, String> entry : applicationEntryInMemory) {
                    String platformNameInMemory = entry.getKey();
                    String applicationArnInMemory = entry.getValue();

                    boolean foundInDb = false;
                    for (SnsApplication snsApplication : snsApplications) {
                        if (snsApplication.getPlatform().equals(platformNameInMemory)) {
                            // found, update to memory from db

                            foundInDb = true;

                            String applicationArnInDb = snsApplication.getApplicationArn();

                            if (!applicationArnInDb.equals(applicationArnInMemory)) {
                                AbstractEndpointArn.createOrUpdateApplicationArnByType(platformNameInMemory, applicationArnInDb);

                                LOGGER.info("Application ARN value (in memory) of platform: '" + platformNameInMemory + "' updated to '" + applicationArnInDb + "' from old value '" + applicationArnInMemory + "'.");
                            }

                            break;
                        }
                    }

                    if (!foundInDb) {
                        // not found, delete value in memory

                        AbstractEndpointArn.deleteApplicationByType(platformNameInMemory);

                        LOGGER.info("Deleted application (in memory) of platform: '" + platformNameInMemory + "'.");
                    }
                }
            }
        }

        // 2/2: add the applications in db to memory

        // refresh for possible new values
        snsApplications = snsApplicationDao.findAllSnsApplications();

        if (snsApplications != null && snsApplications.size() > 0) {
            Hashtable<String, String> applicationsInMemory = AbstractEndpointArn.getPlatformApplications();

            for (SnsApplication snsApplication : snsApplications) {
                String platformInDb = snsApplication.getPlatform();

                if (!applicationsInMemory.containsKey(platformInDb)) {
                    String newApplicationArn = snsApplication.getApplicationArn();
                    AbstractEndpointArn.createOrUpdateApplicationArnByType(platformInDb, newApplicationArn);

                    LOGGER.info("Added application (in memory) of platform: '" + platformInDb + "' with application ARN: " + newApplicationArn);
                }
            }
        }
    }

    public void sendForOneFileUploaded(String userId, String deviceTokenString, String locale, String filename, String transferKey, String transferStatus) {
        // absorbs and log all exception

        DeviceToken deviceToken = deviceTokenDao.findDeviceTokenByDeviceTokenAndAccount(deviceTokenString, userId);

        if (deviceToken != null) {
            List<String> tokens = new ArrayList<>();
            tokens.add(deviceTokenString);

            // IOS, OSX, WATCH_OS, WIN_PHONE, WIN_DESKTOP, ANDROID, CHROME, MOBILE, DESKTOP
            String deviceType = deviceToken.getDeviceType();

            // 7.1, 8.3, ...
            String deviceVersion = deviceToken.getDeviceVersion();

            // DEBUG
//            LOGGER.info(String.format("[NOTIFICATION]Device type: %s, version: %s", deviceType, deviceVersion));


            // incremented by 1 even if transfer status is failure
            deviceToken = deviceTokenDao.incrementBadgeNumberBy(deviceTokenString, userId, 1);

            if (deviceType.equals(DeviceToken.DeviceType.IOS.name())) {
                String message = prepareApnsMessageWithUploadingFileResult(deviceVersion, locale, filename, transferKey, transferStatus, deviceToken.getBadgeNumber());

                // DEBUG
//                LOGGER.info(String.format("[NOTIFICATION]APNS Will send message:\n%s", message));

                snsClientWrapper.sendApnsNotification(tokens, message);
            } else if (deviceType.equals(DeviceToken.DeviceType.ANDROID.name())) {
                String message = prepareGcmMessageWithUploadingFileResult(deviceVersion, deviceTokenString, locale, filename, transferKey, transferStatus, deviceToken.getBadgeNumber());

                // DEBUG
//                LOGGER.info(String.format("[NOTIFICATION]GCM Will send message:\n%s", message));

                snsClientWrapper.sendGcmNotification(tokens, message);
            } else {
                LOGGER.error("Push notification for device: " + deviceType + " not implemented yet.");
            }
        } else {
            LOGGER.warn(String.format("Cannot send upload file result because device token object not found for token string: '%s' and user id: '%s'", deviceTokenString, userId));
        }
    }

    public void sendForAllFilesUploadedSuccessfully(String userId, String deviceTokenString, String locale, String latestSuccessFilename, int filesCount, String uploadGroupId) {
        // absorbs and log all exception

        DeviceToken deviceToken = deviceTokenDao.findDeviceTokenByDeviceTokenAndAccount(deviceTokenString, userId);

        if (deviceToken != null) {
            List<String> tokens = new ArrayList<>();
            tokens.add(deviceTokenString);

            // IOS, OSX, WATCH_OS, WIN_PHONE, WIN_DESKTOP, ANDROID, CHROME, MOBILE, DESKTOP
            String deviceType = deviceToken.getDeviceType();

            // 7.1, 8.3, ...
            String deviceVersion = deviceToken.getDeviceVersion();

            // DEBUG
//            LOGGER.info(String.format("[NOTIFICATION]Device type: %s, version: %s", deviceType, deviceVersion));

            deviceToken = deviceTokenDao.incrementBadgeNumberBy(deviceTokenString, userId, 1);

            if (deviceType.equals(DeviceToken.DeviceType.IOS.name())) {
                String message = prepareApnsMessageWithAllFilesUploadedSuccessfully(deviceVersion, locale, latestSuccessFilename, filesCount, deviceToken.getBadgeNumber(), uploadGroupId);

                // DEBUG
//                LOGGER.info(String.format("[NOTIFICATION]Will send message:\n%s", message));

                snsClientWrapper.sendApnsNotification(tokens, message);
            } else if (deviceType.equals(DeviceToken.DeviceType.ANDROID.name())) {
                String message = prepareGcmMessageWithAllFilesUploadedSuccessfully(deviceVersion, deviceTokenString, locale, latestSuccessFilename, filesCount, deviceToken.getBadgeNumber(), uploadGroupId);

                // DEBUG
//                LOGGER.info(String.format("[NOTIFICATION]GCM Will send message:\n%s", message));

                snsClientWrapper.sendGcmNotification(tokens, message);
            }else {
                LOGGER.error("Push notification for device: " + deviceType + " not implemented yet.");
            }
        } else {
            LOGGER.warn(String.format("Cannot send notification on all files uploaded successfully because device token object not found for token string: '%s' and user id: '%s'", deviceTokenString, userId));
        }
    }

    // Return null if error occurred.
//    public AmazonSNSClient prepareAmazonSnsClient() {
//        AmazonSNSClient snsClient;
//
//        try {
//            String credentialFilename = "AwsCredentials.properties";
//
//            ClassLoader classLoader = getClass().getClassLoader();
//
//            InputStream credentialInputStream = classLoader.getResourceAsStream("/" + credentialFilename);
//
//            if (credentialInputStream == null) {
//                credentialInputStream = classLoader.getResourceAsStream(credentialFilename);
//            }
//
//            if (credentialInputStream == null) {
//                throw new FileNotFoundException("File not found: " + credentialFilename);
//            }
//
//            snsClient = new AmazonSNSClient(new PropertiesCredentials(credentialInputStream));
//        } catch (Exception e) {
//            snsClient = null;
//
//            LOGGER.error("Failed to creating AmazonSNSClient.\n" + e.getMessage(), e);
//        }
//
//        return snsClient;
//    }

    private String prepareApnsMessageWithUploadingFileResult(String deviceVersion, String locale, String filename, String transferKey, String transferStatus, Integer badgeNumber) {
        Map<String, Object> appMessageMap = new HashMap<>();

        String alertBody;
        String alertTitle;

        if (transferStatus != null && transferStatus.equals(DatabaseConstants.TRANSFER_STATUS_SUCCESS)) {
            alertBody = ClopuccinoMessages.localizedMessage(locale, "notify.file.upload.success", filename);

            alertTitle = ClopuccinoMessages.localizedMessage(locale, "notify.title.file.upload.success");
        } else {
            alertBody = ClopuccinoMessages.localizedMessage(locale, "notify.file.upload.failure", filename);

            alertTitle = ClopuccinoMessages.localizedMessage(locale, "notify.title.file.upload.failure");
        }

        if (isIosVersion8OrLater(deviceVersion)) {
            // alert (dictionary)
            Map<String, Object> alertMessageMap = new HashMap<>();

            alertMessageMap.put("title", alertTitle);
            alertMessageMap.put("body", alertBody);

            appMessageMap.put("alert", alertMessageMap);
        } else {
            // alert (string)
            appMessageMap.put("alert", alertBody);
        }

        // badge
        appMessageMap.put("badge", (badgeNumber != null ? badgeNumber : 0));

        // sound
        appMessageMap.put("sound", "default");

        // It seems that 1 is the default value.
        // content-available:
        // with a value of 1 lets the remote notification act as a “silent” notification.
        // When a silent notification arrives, iOS wakes up your app in the background
        // so that you can get new data from your server or do background information processing.
        // Users aren’t told about the new or changed information that results from a silent notification,
        // but they can find out about it the next time they open your app.
//            appMessageMap.put("content-available", 1);

        Map<String, Object> appleMessageMap = new HashMap<>();
        appleMessageMap.put("aps", appMessageMap);

        // custom message - message type
        appleMessageMap.put(Constants.NOTIFICATION_MESSAGE_KEY_TYPE, Constants.NOTIFICATION_MESSAGE_TYPE_UPLOAD_FILE);

        // custom message - transfer key
        appleMessageMap.put(Constants.NOTIFICATION_MESSAGE_KEY_TRANSFER_KEY, transferKey);

        // custom message - transfer status
        appleMessageMap.put(Constants.NOTIFICATION_MESSAGE_KEY_TRANSFER_STATUS, transferStatus);

        return snsClientWrapper.jsonify(appleMessageMap);
    }

    private String prepareGcmMessageWithUploadingFileResult(String deviceVersion, String deviceTokenString, String locale, String filename, String transferKey, String transferStatus, Integer badgeNumber) {
        /* GCM message sample:

            {   --> Android APP收到的資訊從此開始，不包含tag: "GCM"
                "data":
                {
                    "title" : "message title...",
                    "body" : "message detail...",
                    "badge" : 5,
                    // notification type, valid value are "upload-file" and "all-files-uploaded-successfully"
                    "fl-type" : "upload-file",
                    // upload-key
                    "transfer-key" : "1329C8D721F6E09E7936954B212B6407CA07545D1C1696B53FE164E8B199553BE",
                    // transfer status, valid values are "success" and "failure"
                    "transfer-status" : "success"
                },
                // device token
                "to" : "APA91bHun4MxP5egoKMwt2KZFBaFUH-1RYqx..."
            }

        */

        Map<String, Object> gcmMessageMap = new HashMap<>();

        String alertBody;
        String alertTitle;

        if (transferStatus != null && transferStatus.equals(DatabaseConstants.TRANSFER_STATUS_SUCCESS)) {
            alertBody = ClopuccinoMessages.localizedMessage(locale, "notify.file.upload.success", filename);

            alertTitle = ClopuccinoMessages.localizedMessage(locale, "notify.title.file.upload.success");
        } else {
            alertBody = ClopuccinoMessages.localizedMessage(locale, "notify.file.upload.failure", filename);

            alertTitle = ClopuccinoMessages.localizedMessage(locale, "notify.title.file.upload.failure");
        }

        Map<String, Object> dataMessageMap = new HashMap<>();

        // title
        dataMessageMap.put("title", alertTitle);

        // detail message
        dataMessageMap.put("body", alertBody);

        // badge number
        dataMessageMap.put("badge", (badgeNumber != null ? badgeNumber : 0));

        // message type
        dataMessageMap.put(Constants.NOTIFICATION_MESSAGE_KEY_TYPE, Constants.NOTIFICATION_MESSAGE_TYPE_UPLOAD_FILE);

        // transfer key
        dataMessageMap.put(Constants.NOTIFICATION_MESSAGE_KEY_TRANSFER_KEY, transferKey);

        // transfer status
        dataMessageMap.put(Constants.NOTIFICATION_MESSAGE_KEY_TRANSFER_STATUS, transferStatus);

        gcmMessageMap.put("data", dataMessageMap);

        gcmMessageMap.put("to", deviceTokenString);

        return snsClientWrapper.jsonify(gcmMessageMap);
    }

    // Value of filesCount should > 1 so the alert body looks reasonable.
    private String prepareApnsMessageWithAllFilesUploadedSuccessfully(String deviceVersion, String locale, String latestSuccessFilename, int filesCount, Integer badgeNumber, String uploadGroupId) {
        Map<String, Object> appMessageMap = new HashMap<>();

        String alertBody = ClopuccinoMessages.localizedMessage(locale, "notify.all.files.uploaded.successfully", String.valueOf(filesCount), latestSuccessFilename);

        String alertTitle = ClopuccinoMessages.localizedMessage(locale, "notify.title.all.files.uploaded.successfully");

        if (isIosVersion8OrLater(deviceVersion)) {
            // alert (dictionary)
            Map<String, Object> alertMessageMap = new HashMap<>();

            alertMessageMap.put("title", alertTitle);
            alertMessageMap.put("body", alertBody);

            appMessageMap.put("alert", alertMessageMap);
        } else {
            // alert (string)
            appMessageMap.put("alert", alertBody);
        }

        // badge
        appMessageMap.put("badge", (badgeNumber != null ? badgeNumber : 0));

        // sound
        appMessageMap.put("sound", "default");

        // It seems that 1 is the default value.
        // content-available:
        // with a value of 1 lets the remote notification act as a “silent” notification.
        // When a silent notification arrives, iOS wakes up your app in the background
        // so that you can get new data from your server or do background information processing.
        // Users aren’t told about the new or changed information that results from a silent notification,
        // but they can find out about it the next time they open your app.
//            appMessageMap.put("content-available", 1);

        Map<String, Object> appleMessageMap = new HashMap<>();
        appleMessageMap.put("aps", appMessageMap);

        // custom message - message type
        appleMessageMap.put(Constants.NOTIFICATION_MESSAGE_KEY_TYPE, Constants.NOTIFICATION_MESSAGE_TYPE_ALL_FILES_UPLOADED_SUCCESSFULLY);

        // upload group id
        appleMessageMap.put(Constants.NOTIFICATION_MESSAGE_KEY_UPLOAD_GROUP_ID, uploadGroupId);

        return snsClientWrapper.jsonify(appleMessageMap);
    }

    // Value of filesCount should > 1 so the alert body looks reasonable.
    private String prepareGcmMessageWithAllFilesUploadedSuccessfully(String deviceVersion, String deviceTokenString, String locale, String latestSuccessFilename, int filesCount, Integer badgeNumber, String uploadGroupId) {
        /* GCM message sample:

            {   --> Android APP收到的資訊從此開始，不包含tag: "GCM"
                "data":
                {
                    "title" : "message title...",
                    "body" : "message detail...",
                    "badge" : 5,
                    // notification type, valid value are "upload-file" and "all-files-uploaded-successfully"
                    "fl-type" : "all-files-uploaded-successfully",
                    "upload-group-id": "OTFDRjUxNzjglQUMlRTglQTklQTYuUE5HKzEzOTI2MjEwMjQzNDc="
                },
                // device token
                "to" : "APA91bHun4MxP5egoKMwt2KZFBaFUH-1RYqx..."
            }

         */

        Map<String, Object> gcmMessageMap = new HashMap<>();

        String alertBody = ClopuccinoMessages.localizedMessage(locale, "notify.all.files.uploaded.successfully", String.valueOf(filesCount), latestSuccessFilename);

        String alertTitle = ClopuccinoMessages.localizedMessage(locale, "notify.title.all.files.uploaded.successfully");

        Map<String, Object> dataMessageMap = new HashMap<>();

        // title
        dataMessageMap.put("title", alertTitle);

        // detail message
        dataMessageMap.put("body", alertBody);

        // badge number
        dataMessageMap.put("badge", (badgeNumber != null ? badgeNumber : 0));

        // message type
        dataMessageMap.put(Constants.NOTIFICATION_MESSAGE_KEY_TYPE, Constants.NOTIFICATION_MESSAGE_TYPE_ALL_FILES_UPLOADED_SUCCESSFULLY);

        // upload group id
        dataMessageMap.put(Constants.NOTIFICATION_MESSAGE_KEY_UPLOAD_GROUP_ID, uploadGroupId);

        gcmMessageMap.put("data", dataMessageMap);

        gcmMessageMap.put("to", deviceTokenString);

        return snsClientWrapper.jsonify(gcmMessageMap);
    }

    private boolean isIosVersion8OrLater(String deviceVersion) {
        boolean version8OrLater = false;

        if (Version.valid(deviceVersion)) {
            Version version = new Version(deviceVersion);

            Version version8 = new Version("8");

            version8OrLater = (version.compareTo(version8) >= 1);
        }

        return version8OrLater;
    }
}
