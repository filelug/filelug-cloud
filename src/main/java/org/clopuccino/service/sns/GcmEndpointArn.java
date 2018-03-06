package org.clopuccino.service.sns;

import ch.qos.logback.classic.Logger;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.clopuccino.domain.DeviceToken;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

/**
 * <code>GcmEndpointArn</code> provides endpoint ARN for GCM push notification.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class GcmEndpointArn extends AbstractEndpointArn {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(GcmEndpointArn.class.getSimpleName());

    private AmazonSNSClient client;

    public GcmEndpointArn(String token, AmazonSNSClient client) {
        this.token = token;

        this.client = client;

        preparePlatformInformation();
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
        return getPlatformApplicationArnByType(DeviceToken.NotificationType.GCM);
    }

    private void preparePlatformInformation() {
        notificationType = DeviceToken.NotificationType.GCM;

        Hashtable<String, String> platformApplications = getPlatformApplications();

        if (platformApplications != null && platformApplications.size() > 0) {
            applicationName = platformApplications.get(notificationType.name());
        } else {
            LOGGER.warn("Failed to set application name becasuse No platform application exists.");
        }

    }
}
