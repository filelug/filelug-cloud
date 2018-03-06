package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>Computer</code> represents the computer data.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Computer implements Cloneable {

    public enum Type {
        GENERAL, PREMIUM
    }

    @JsonProperty("computer-id")
    private Long computerId;

    @JsonProperty("computer-group")
    private String groupName;

    @JsonProperty("computer-name")
    private String computerName;

    @JsonProperty("recoveryKey")
    private String recoveryKey;

    @JsonProperty("admin")
    private String userId;


    public Computer() {
    }

    public Computer(Long computerId, String groupName, String computerName, String recoveryKey, String userId) {
        this.computerId = computerId;

        setGroupName(groupName);

        this.computerName = computerName;
        this.recoveryKey = recoveryKey;
        this.userId = userId;
    }

    public static boolean suppoertedGroupName(String groupName) {
        boolean supported = true;

        try {
            Type.valueOf(groupName);
        } catch (Exception e) {
            supported = false;
        }

        return supported;
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
        try {
            Type groupType = Type.valueOf(groupName);

            this.groupName = groupType.name();
        } catch (Exception e) {
            this.groupName = Type.GENERAL.name();
        }
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public String getRecoveryKey() {
        return recoveryKey;
    }

    public void setRecoveryKey(String recoveryKey) {
        this.recoveryKey = recoveryKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Computer{");
        sb.append("computerId=").append(computerId);
        sb.append(", groupName='").append(groupName).append('\'');
        sb.append(", computerName='").append(computerName).append('\'');
        sb.append(", recoveryKey='").append(recoveryKey).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
