package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>SMSNotification</code> represents the SMS result notification.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SMSNotification implements Cloneable {

    @JsonProperty("sms-id")
    private String smsId;

    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @JsonProperty("deliver-timestamp")
    private Long deliverTimestamp;

    @JsonProperty("status-update-timestamp")
    private Long statusUpdateTimestamp;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("status-message")
    private String statusMessage;

    @JsonProperty("userId")
    private String userId;


    public SMSNotification() {
    }

    public SMSNotification(String smsId, String phoneNumber, Long deliverTimestamp, Long statusUpdateTimestamp, Integer status, String statusMessage, String userId) {
        this.smsId = smsId;
        this.phoneNumber = phoneNumber;
        this.deliverTimestamp = deliverTimestamp;
        this.statusUpdateTimestamp = statusUpdateTimestamp;
        this.status = status;
        this.statusMessage = statusMessage;
        this.userId = userId;
    }

    public String getSmsId() {
        return smsId;
    }

    public void setSmsId(String smsId) {
        this.smsId = smsId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Long getDeliverTimestamp() {
        return deliverTimestamp;
    }

    public void setDeliverTimestamp(Long deliverTimestamp) {
        this.deliverTimestamp = deliverTimestamp;
    }

    public Long getStatusUpdateTimestamp() {
        return statusUpdateTimestamp;
    }

    public void setStatusUpdateTimestamp(Long statusUpdateTimestamp) {
        this.statusUpdateTimestamp = statusUpdateTimestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SMSNotification{");
        sb.append("smsId='").append(smsId).append('\'');
        sb.append(", phoneNumber='").append(phoneNumber).append('\'');
        sb.append(", deliverTimestamp=").append(deliverTimestamp);
        sb.append(", statusUpdateTimestamp=").append(statusUpdateTimestamp);
        sb.append(", status=").append(status);
        sb.append(", statusMessage='").append(statusMessage).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
