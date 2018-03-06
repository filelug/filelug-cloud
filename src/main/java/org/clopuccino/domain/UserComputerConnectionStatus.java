package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>UserComputerConnectionStatus</code> represents connection status of a user computer.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserComputerConnectionStatus {

    @JsonProperty("user-id")
    private String userId;

    @JsonProperty("connected")
    private Boolean socketConnected;

    @JsonProperty("need-reconnect")
    private Boolean needReconnect;

    @JsonIgnore
    private Long computerId;

    public UserComputerConnectionStatus() {
    }

    public UserComputerConnectionStatus(String userId, Boolean needReconnect, Boolean socketConnected) {
        this.userId = userId;
        this.needReconnect = needReconnect;
        this.socketConnected = socketConnected;
    }

    public Boolean getNeedReconnect() {
        return needReconnect;
    }

    public void setNeedReconnect(Boolean needReconnect) {
        this.needReconnect = needReconnect;
    }

    public Boolean getSocketConnected() {
        return socketConnected;
    }

    public void setSocketConnected(Boolean socketConnected) {
        this.socketConnected = socketConnected;
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
}
