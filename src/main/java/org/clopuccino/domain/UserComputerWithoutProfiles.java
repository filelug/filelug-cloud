package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>UserComputerWithoutProfiles</code> represents user computer table data without profile-related properties.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserComputerWithoutProfiles implements Cloneable {

    @JsonProperty("user-computer-id")
    private String userComputerId;

    @JsonProperty("user-id")
    private String userId;

    @JsonProperty("computer-id")
    private Long computerId;

    @JsonProperty("computer-admin-id")
    private String computerAdminId;

    @JsonProperty("computer-group")
    private String groupName;

    @JsonProperty("computer-name")
    private String computerName;

    @JsonIgnore
    private String encryptedUserComputerId;

    @JsonProperty("lug-server-id")
    private String lugServerId;

    @JsonIgnore
    private Boolean socketConnected;

    @JsonIgnore
    private Boolean needReconnect;

    // true if the user allowed to access the shortcue (and symlink, alias) file in the computer.
    @JsonIgnore
    private Boolean allowAlias;

    public UserComputerWithoutProfiles() {
    }

    public UserComputerWithoutProfiles(String userComputerId, String userId, Long computerId, String computerAdminId, String groupName, String computerName, String encryptedUserComputerId, String lugServerId, Boolean socketConnected, Boolean needReconnect, Boolean allowAlias) {
        this.userComputerId = userComputerId;
        this.userId = userId;
        this.computerId = computerId;
        this.computerAdminId = computerAdminId;
        this.groupName = groupName;
        this.computerName = computerName;
        this.encryptedUserComputerId = encryptedUserComputerId;
        this.lugServerId = lugServerId;
        this.socketConnected = socketConnected;
        this.needReconnect = needReconnect;
        this.allowAlias = allowAlias;
    }

    public String getUserComputerId() {
        return userComputerId;
    }

    public void setUserComputerId(String userComputerId) {
        this.userComputerId = userComputerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getComputerId() {
        return computerId;
    }

    public void setComputerId(Long computerId) {
        this.computerId = computerId;
    }

    public String getComputerAdminId() {
        return computerAdminId;
    }

    public void setComputerAdminId(String computerAdminId) {
        this.computerAdminId = computerAdminId;
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

    public String getEncryptedUserComputerId() {
        return encryptedUserComputerId;
    }

    public void setEncryptedUserComputerId(String encryptedUserComputerId) {
        this.encryptedUserComputerId = encryptedUserComputerId;
    }

    public String getLugServerId() {
        return lugServerId;
    }

    public void setLugServerId(String lugServerId) {
        this.lugServerId = lugServerId;
    }

    public Boolean isSocketConnected() {
        return socketConnected;
    }

    public void setSocketConnected(Boolean socketConnected) {
        this.socketConnected = socketConnected;
    }

    public Boolean isNeedReconnect() {
        return needReconnect;
    }

    public void setNeedReconnect(Boolean needReconnect) {
        this.needReconnect = needReconnect;
    }

    public Boolean isAllowAlias() {
        return allowAlias;
    }

    public void setAllowAlias(Boolean allowAlias) {
        this.allowAlias = allowAlias;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserComputerWithoutProfiles{");
        sb.append("allowAlias=").append(allowAlias);
        sb.append(", userComputerId='").append(userComputerId).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", computerId=").append(computerId);
        sb.append(", computerAdminId='").append(computerAdminId).append('\'');
        sb.append(", groupName='").append(groupName).append('\'');
        sb.append(", computerName='").append(computerName).append('\'');
        sb.append(", encryptedUserComputerId='").append(encryptedUserComputerId).append('\'');
        sb.append(", lugServerId='").append(lugServerId).append('\'');
        sb.append(", socketConnected=").append(socketConnected);
        sb.append(", needReconnect=").append(needReconnect);
        sb.append('}');
        return sb.toString();
    }
}
