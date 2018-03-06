package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>DeviceToken</code> represents the device token for remote notification.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceToken {

    public enum NotificationType {
        APNS, APNS_SANDBOX, GCM, BAIDU, WNS, NONE
    }

    public enum DeviceType {
        MOBILE, DESKTOP, IOS, OSX, WATCH_OS, WIN_PHONE, WIN_DESKTOP, ANDROID, CHROME, LINUX_DESKTOP
    }

    public static boolean validNotificationType(String notificationType) {
        boolean valid;

        try {
            NotificationType.valueOf(notificationType);

            valid = true;
        } catch (Exception e) {
            valid = false;
        }

        return valid;
    }

    public static boolean validDeviceType(String deviceType) {
        boolean valid;

        try {
            DeviceType.valueOf(deviceType);

            valid = true;
        } catch (Exception e) {
            valid = false;
        }

        return valid;
    }

    public static boolean validDeviceVersion(String deviceVersion) {
        return Version.valid(deviceVersion);
    }

    @JsonIgnore
    private Long sequenceId;

    @JsonProperty("device-token")
    private String deviceToken;

    @JsonProperty("notification-type")
    private String notificationType;

    @JsonProperty("device-type")
    private String deviceType;

    @JsonProperty("device-version")
    private String deviceVersion;

    @JsonProperty("filelug-version")
    private String filelugVersion;

    @JsonProperty("filelug-build")
    private String filelugBuild;

    @JsonProperty("badge-number")
    private Integer badgeNumber;

    @JsonProperty("increment-badge-number")
    private Integer incrementBadgeNumber;

    @JsonProperty("account")
    private String account;


    public DeviceToken() {
    }

    public DeviceToken(Long sequenceId, String deviceToken, String notificationType, String deviceType, String deviceVersion, String filelugVersion, String filelugBuild, Integer badgeNumber, Integer incrementBadgeNumber, String account) {
        this.sequenceId = sequenceId;
        this.deviceToken = deviceToken;
        this.notificationType = notificationType;
        this.deviceType = deviceType;
        this.deviceVersion = deviceVersion;
        this.filelugVersion = filelugVersion;
        this.filelugBuild = filelugBuild;
        this.badgeNumber = badgeNumber;
        this.incrementBadgeNumber = incrementBadgeNumber;
        this.account = account;
    }

//    public DeviceToken(Long sequenceId, String deviceToken, String notificationType, String deviceType, String deviceVersion, Integer badgeNumber, Integer incrementBadgeNumber, String account) {
//        this.sequenceId = sequenceId;
//        this.deviceToken = deviceToken;
//        this.notificationType = notificationType;
//        this.deviceType = deviceType;
//        this.deviceVersion = deviceVersion;
//        this.badgeNumber = badgeNumber;
//        this.incrementBadgeNumber = incrementBadgeNumber;
//        this.account = account;
//    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public String getFilelugVersion() {
        return filelugVersion;
    }

    public void setFilelugVersion(String filelugVersion) {
        this.filelugVersion = filelugVersion;
    }

    public String getFilelugBuild() {
        return filelugBuild;
    }

    public void setFilelugBuild(String filelugBuild) {
        this.filelugBuild = filelugBuild;
    }

    public Integer getBadgeNumber() {
        return badgeNumber;
    }

    public void setBadgeNumber(Integer badgeNumber) {
        this.badgeNumber = badgeNumber;
    }

    public Integer getIncrementBadgeNumber() {
        return incrementBadgeNumber;
    }

    public void setIncrementBadgeNumber(Integer incrementBadgeNumber) {
        this.incrementBadgeNumber = incrementBadgeNumber;
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeviceToken{");
        sb.append("account='").append(account).append('\'');
        sb.append(", sequenceId=").append(sequenceId);
        sb.append(", deviceToken='").append(deviceToken).append('\'');
        sb.append(", notificationType='").append(notificationType).append('\'');
        sb.append(", deviceType='").append(deviceType).append('\'');
        sb.append(", deviceVersion='").append(deviceVersion).append('\'');
        sb.append(", filelugVersion='").append(filelugVersion).append('\'');
        sb.append(", filelugBuild='").append(filelugBuild).append('\'');
        sb.append(", badgeNumber=").append(badgeNumber);
        sb.append(", incrementBadgeNumber=").append(incrementBadgeNumber);
        sb.append('}');
        return sb.toString();
    }
}
