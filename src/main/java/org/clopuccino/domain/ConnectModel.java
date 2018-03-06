package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Properties;

/**
 * <code>ConnectModel</code> is the model for connect service.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectModel {

    @JsonProperty("sid")
    private Integer sid;

    @JsonProperty("session-id")
    private String sessionId;

    @JsonProperty("country-id")
    private String countryId;

    @JsonProperty("phone")
    private String phoneNumber;

    @JsonProperty("account")
    private String account;

    @JsonProperty("adminAccount")
    private String adminAccount;

    @JsonProperty("passwd")
    private String password;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("verification")
    private String verification;

    @JsonProperty("showHidden")
    private Boolean showHidden;

    @JsonProperty("locale")
    private String locale;

    @JsonProperty("computer-id")
    private Long computerId;

    @JsonProperty("computer-group")
    private String groupName;

    @JsonProperty("computer-name")
    private String computerName;

    @JsonProperty("lug-server-id")
    private String lugServerId;

    @JsonProperty("device-version")
    private String deviceVersion;

    @JsonProperty("device-build")
    private String deviceBuild;

    @JsonProperty("device-token")
    private DeviceToken deviceToken;

    @JsonProperty("sysprops")
    private Properties properties;

    @JsonProperty("should-self-approved")
    private Boolean shouldSelfApproved;

    public ConnectModel() {
    }

    public Integer getSid() {
        return sid;
    }

    public void setSid(Integer sid) {
        this.sid = sid;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAdminAccount() {
        return adminAccount;
    }

    public void setAdminAccount(String adminAccount) {
        this.adminAccount = adminAccount;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }

    public Boolean getShowHidden() {
        return showHidden;
    }

    public void setShowHidden(Boolean showHidden) {
        this.showHidden = showHidden;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Long getComputerId() {
        return computerId;
    }

    public void setComputerId(Long computerId) {
        this.computerId = computerId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public String getLugServerId() {
        return lugServerId;
    }

    public void setLugServerId(String lugServerId) {
        this.lugServerId = lugServerId;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public String getDeviceBuild() {
        return deviceBuild;
    }

    public void setDeviceBuild(String deviceBuild) {
        this.deviceBuild = deviceBuild;
    }

    public DeviceToken getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(DeviceToken deviceToken) {
        this.deviceToken = deviceToken;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void putProperty(String key, String value) {
        if (properties == null) {
            properties = new Properties();
        }

        properties.put(key, value);
    }

    public void removeProperty(String key) {
        if (properties != null) {
            properties.remove(key);
        }
    }

    public Boolean getShouldSelfApproved() {
        return shouldSelfApproved;
    }

    public void setShouldSelfApproved(Boolean shouldSelfApproved) {
        this.shouldSelfApproved = shouldSelfApproved;
    }
}
