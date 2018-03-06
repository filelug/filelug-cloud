package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>ResponseModel</code> represents the basic response information for websocket.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseModel {

    @JsonProperty("sid")
    private Integer sid;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("error")
    private String error;

    @JsonProperty("operatorId")
    private String operatorId;

    // The session id of the connected device
    @JsonProperty("clientSessionId")
    private String deviceSessionId;

    @JsonProperty("timestamp")
    private Long timestamp;

    public ResponseModel() {
    }

    public ResponseModel(Integer sid, Integer status, String error, String operatorId, String deviceSessionId, Long timestamp) {
        this.sid = sid;
        this.status = status;
        this.error = error;
        this.operatorId = operatorId;
        this.deviceSessionId = deviceSessionId;
        this.timestamp = timestamp;
    }

    public ResponseModel(Integer sid, Integer status, String error, String operatorId, Long timestamp) {
        this(sid, status, error, operatorId, null, timestamp);
    }

    public Integer getSid() {
        return sid;
    }

    public void setSid(Integer sid) {
        this.sid = sid;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDeviceSessionId() {
        return deviceSessionId;
    }

    public void setDeviceSessionId(String deviceSessionId) {
        this.deviceSessionId = deviceSessionId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
