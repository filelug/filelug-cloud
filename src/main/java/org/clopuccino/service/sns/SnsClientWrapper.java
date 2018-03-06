package org.clopuccino.service.sns;

import ch.qos.logback.classic.Logger;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.EndpointDisabledException;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.clopuccino.Utility;
import org.clopuccino.domain.DeviceToken;
import org.clopuccino.service.BaseService;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <code>SnsClientWrapper</code> wraps AmazonSNS object to provide push service for SNS.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class SnsClientWrapper {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SnsClientWrapper.class.getSimpleName());

    private static final Map<DeviceToken.NotificationType, Map<String, MessageAttributeValue>> attributesMap = new HashMap<>();

    private final ObjectMapper objectMapper = Utility.createObjectMapper();

    private final AmazonSNSClient snsClient;

    private boolean developmentMode;

    static {
        attributesMap.put(DeviceToken.NotificationType.GCM, null);
        attributesMap.put(DeviceToken.NotificationType.APNS, null);
        attributesMap.put(DeviceToken.NotificationType.APNS_SANDBOX, null);
        attributesMap.put(DeviceToken.NotificationType.BAIDU, addBaiduNotificationAttributes());
        attributesMap.put(DeviceToken.NotificationType.WNS, addWNSNotificationAttributes());
    }

    public SnsClientWrapper(AmazonSNSClient client) {
        BaseService baseService = new BaseService();
        developmentMode = !baseService.getRepositoryUseHttps();

        // DEBUG: Remove when in production
//        developmentMode = false;

        this.snsClient = client;
    }

    public void sendApnsNotification(List<String> tokens, String notificationMessage) {
        for (String token : tokens) {
            String applicationName = null;
            DeviceToken.NotificationType notificationType = null;

            try {
                ApnsEndpointArn endpointArn = new ApnsEndpointArn(token, developmentMode, snsClient);

                applicationName = endpointArn.getApplicationName();
                notificationType = endpointArn.getNotificationType();
                String endpointArnString = endpointArn.getEndpointArn();

                // Publish a push notification to an Endpoint.
                PublishResult publishResult = publish(endpointArnString, notificationType, notificationMessage);

                // DEBUG
//                LOGGER.info(String.format("Notification('%s') pushed. message id='%s', token='%s', application name='%s'", (notificationType != null ? notificationType.name() : "(Empty)"), publishResult.getMessageId(), token, applicationName));

                LOGGER.debug(String.format("Notification('%s') pushed. message id='%s', token='%s', application name='%s'", (notificationType != null ? notificationType.name() : "(Empty)"), publishResult.getMessageId(), token, applicationName));
            } catch (Exception e) {
                if (EndpointDisabledException.class.isInstance(e.getClass())) {
                    LOGGER.error(String.format("Failed to push notification('%s') with token='%s', application name='%s'\nIgnore this if developers are trying download/upload files using un-released version of Filellug iOS.", (notificationType != null ? notificationType.name() : "(Empty)"), token, applicationName), e);
                } else {
                    LOGGER.error(String.format("Failed to push notification('%s') with token='%s', application name='%s'", (notificationType != null ? notificationType.name() : "(Empty)"), token, applicationName), e);
                }
            }
        }
    }

    public void sendGcmNotification(List<String> tokens, String notificationMessage) {
        for (String token : tokens) {
            String applicationName = null;
            DeviceToken.NotificationType notificationType = null;

            try {
                GcmEndpointArn endpointArn = new GcmEndpointArn(token, snsClient);

                applicationName = endpointArn.getApplicationName();
                notificationType = endpointArn.getNotificationType();
                String endpointArnString = endpointArn.getEndpointArn();

                // Publish a push notification to an Endpoint.
                PublishResult publishResult = publish(endpointArnString, notificationType, notificationMessage);

                // DEBUG
//                LOGGER.info(String.format("Notification('%s') pushed. message id='%s', token='%s', application name='%s'", (notificationType != null ? notificationType.name() : "(Empty)"), publishResult.getMessageId(), token, applicationName));

                LOGGER.debug(String.format("Notification('%s') pushed. message id='%s', token='%s', application name='%s'", (notificationType != null ? notificationType.name() : "(Empty)"), publishResult.getMessageId(), token, applicationName));
            } catch (Exception e) {
                LOGGER.error(String.format("Failed to push notification('%s') with token='%s', application name='%s'", (notificationType != null ? notificationType.name() : "(Empty)"), token, applicationName), e);
            }
        }
    }

    public String jsonify(Object message) {
        String jsonString = null;

        try {
            jsonString = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            LOGGER.error("Failed to generate json string for sns push notification. original object:\n" + message);
        }

        return jsonString;
    }

    public AmazonSNSClient getSnsClient() {
        return snsClient;
    }

    private static Map<String, MessageAttributeValue> getValidNotificationAttributes(Map<String, MessageAttributeValue> notificationAttributes) {
        Map<String, MessageAttributeValue> validAttributes = new HashMap<>();

        if (notificationAttributes == null)
            return validAttributes;

        for (Map.Entry<String, MessageAttributeValue> entry : notificationAttributes.entrySet()) {
            if (!StringUtils.isBlank(entry.getValue().getStringValue())) {
                validAttributes.put(entry.getKey(), entry.getValue());
            }
        }

        return validAttributes;
    }

    private PublishResult publish(String endpointArn, DeviceToken.NotificationType notificationType, String notificationMessage) {
        PublishRequest publishRequest = new PublishRequest();

        Map<String, MessageAttributeValue> notificationAttributes = getValidNotificationAttributes(attributesMap.get(notificationType));

        if (notificationAttributes != null && !notificationAttributes.isEmpty()) {
            publishRequest.setMessageAttributes(notificationAttributes);
        }

        publishRequest.setMessageStructure("json");
        Map<String, String> messageMap = new HashMap<>();
        messageMap.put(notificationType.name(), notificationMessage);
        String message = jsonify(messageMap);
        publishRequest.setMessage(message);

        // For direct publish to mobile end points, topicArn is not relevant.
        publishRequest.setTargetArn(endpointArn);

        LOGGER.debug(String.format("Will push message to SNS endpoint: '%s'.\nType: '%s'. Message:\n'%s'", endpointArn, notificationType.name(), message));

        return snsClient.publish(publishRequest);
    }

    private static Map<String, MessageAttributeValue> addBaiduNotificationAttributes() {
        Map<String, MessageAttributeValue> notificationAttributes = new HashMap<>();
        notificationAttributes.put("AWS.SNS.MOBILE.BAIDU.DeployStatus",
                                   new MessageAttributeValue().withDataType("String").withStringValue("1"));
        notificationAttributes.put("AWS.SNS.MOBILE.BAIDU.MessageKey",
                                   new MessageAttributeValue().withDataType("String").withStringValue("default-channel-msg-key"));
        notificationAttributes.put("AWS.SNS.MOBILE.BAIDU.MessageType",
                                   new MessageAttributeValue().withDataType("String").withStringValue("0"));

        return notificationAttributes;
    }

    private static Map<String, MessageAttributeValue> addWNSNotificationAttributes() {
        Map<String, MessageAttributeValue> notificationAttributes = new HashMap<>();
        notificationAttributes.put("AWS.SNS.MOBILE.WNS.CachePolicy",
                                   new MessageAttributeValue().withDataType("String").withStringValue("cache"));
        notificationAttributes.put("AWS.SNS.MOBILE.WNS.Type",
                                   new MessageAttributeValue().withDataType("String").withStringValue("wns/badge"));

        return notificationAttributes;
    }
}