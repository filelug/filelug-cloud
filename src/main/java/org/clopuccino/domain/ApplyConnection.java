package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>ApplyConnection</code> represents the apply connection data.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplyConnection implements Cloneable {

    @JsonProperty("seq-id")
    private String applyConnectionId;

    @JsonProperty("apply-timestamp")
    private Long applyTimestamp;

    @JsonProperty("apply-user")
    private String applyUser;

    @JsonProperty("computer")
    private Long computerId;

    @JsonProperty("approved")
    private Boolean approved;

    @JsonProperty("approved-user")
    private String approvedUser;

    @JsonProperty("approved-timestamp")
    private Long approvedTimestamp;

    @JsonIgnore
    private String computerName;


    public ApplyConnection() {
    }

    public ApplyConnection(String applyConnectionId, Long applyTimestamp, String applyUser, Long computerId, Boolean approved, String approvedUser, Long approvedTimestamp) {
        this.applyConnectionId = applyConnectionId;
        this.applyTimestamp = applyTimestamp;
        this.applyUser = applyUser;
        this.computerId = computerId;
        this.approved = approved;
        this.approvedUser = approvedUser;
        this.approvedTimestamp = approvedTimestamp;
    }

    public String getApplyConnectionId() {
        return applyConnectionId;
    }

    public void setApplyConnectionId(String applyConnectionId) {
        this.applyConnectionId = applyConnectionId;
    }

    public Long getApplyTimestamp() {
        return applyTimestamp;
    }

    public void setApplyTimestamp(Long applyTimestamp) {
        this.applyTimestamp = applyTimestamp;
    }

    public String getApplyUser() {
        return applyUser;
    }

    public void setApplyUser(String applyUser) {
        this.applyUser = applyUser;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public Long getApprovedTimestamp() {
        return approvedTimestamp;
    }

    public void setApprovedTimestamp(Long approvedTimestamp) {
        this.approvedTimestamp = approvedTimestamp;
    }

    public String getApprovedUser() {
        return approvedUser;
    }

    public void setApprovedUser(String approvedUser) {
        this.approvedUser = approvedUser;
    }

    public Long getComputerId() {
        return computerId;
    }

    public void setComputerId(Long computerId) {
        this.computerId = computerId;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ApplyConnection{");
        sb.append("applyConnectionId='").append(applyConnectionId).append('\'');
        sb.append(", applyTimestamp=").append(applyTimestamp);
        sb.append(", applyUser='").append(applyUser).append('\'');
        sb.append(", computerId=").append(computerId);
        sb.append(", approved=").append(approved);
        sb.append(", approvedUser='").append(approvedUser).append('\'');
        sb.append(", approvedTimestamp=").append(approvedTimestamp);
        sb.append(", computerName='").append(computerName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
