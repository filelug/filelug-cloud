package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>ResponseUserComputerModel</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseUserComputerModel extends ResponseModel {

    @JsonProperty("computer-id")
    private Long computerId;

    @JsonProperty("computer-group")
    private String computerGroup;

    @JsonProperty("computer-name")
    private String computerName;

    @JsonProperty("approved-user")
    private ApprovedUserModel approvedUserModel;

    public ResponseUserComputerModel() {
        super();
    }

    public ResponseUserComputerModel(Integer sid, Integer status, String error, String operatorId, Long timestamp, long computerId, String computerGroup, String computerName, ApprovedUserModel approvedUserModel) {
        super(sid, status, error, operatorId, timestamp);

        this.computerId = computerId;
        this.computerGroup = computerGroup;
        this.computerName = computerName;

        this.approvedUserModel = approvedUserModel;
    }

    public Long getComputerId() {
        return computerId;
    }

    public void setComputerId(Long computerId) {
        this.computerId = computerId;
    }

    public String getComputerGroup() {
        return computerGroup;
    }

    public void setComputerGroup(String computerGroup) {
        this.computerGroup = computerGroup;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public ApprovedUserModel getApprovedUserModel() {
        return approvedUserModel;
    }

    public void setApprovedUserModel(ApprovedUserModel approvedUserModel) {
        this.approvedUserModel = approvedUserModel;
    }
}
