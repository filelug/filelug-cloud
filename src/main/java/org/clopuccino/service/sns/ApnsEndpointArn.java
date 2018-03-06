package org.clopuccino.service.sns;

import ch.qos.logback.classic.Logger;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.clopuccino.domain.DeviceToken;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

/**
 * <code>ApnsEndpointArn</code> provides endpoint ARN for APNS push notification.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ApnsEndpointArn extends AbstractEndpointArn {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ApnsEndpointArn.class.getSimpleName());

    private boolean developmentMode;

    private AmazonSNSClient client;

    public ApnsEndpointArn(String token, boolean developmentMode, AmazonSNSClient client) {
        this.token = token;

        this.developmentMode = developmentMode;

        this.client = client;

        preparePlatformInformation(developmentMode);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    public AmazonSNSClient getClient() {
        return client;
    }

    @Override
    protected String preparePlatformApplicationArn() {
        DeviceToken.NotificationType type;
        if (developmentMode) {
            type = DeviceToken.NotificationType.APNS_SANDBOX;
        } else {
            type = DeviceToken.NotificationType.APNS;
        }

        return getPlatformApplicationArnByType(type);
    }

    private void preparePlatformInformation(boolean developmentMode) {
        if (developmentMode) {
            // For Development
            notificationType = DeviceToken.NotificationType.APNS_SANDBOX;
        } else {
            // For Production
            notificationType = DeviceToken.NotificationType.APNS;
        }

        Hashtable<String, String> platformApplications = getPlatformApplications();

        if (platformApplications != null && platformApplications.size() > 0) {
            applicationName = platformApplications.get(notificationType.name());
        } else {
            LOGGER.warn("Failed to set application name becasuse No platform application exists.");
        }

    }

//    private void preparePlatformInformation(boolean developmentMode) {
//        if (developmentMode) {
//            // For Development
//            applicationName = "filelug_ios_apple_dev";
//            notificationType = DeviceToken.NotificationType.APNS_SANDBOX;
//        } else {
//            // For Production
//            applicationName = "filelug_ios_apple_prod";
//            notificationType = DeviceToken.NotificationType.APNS;
//        }
//    }
}
