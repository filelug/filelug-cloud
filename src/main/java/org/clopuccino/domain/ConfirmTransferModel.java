package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>ConfirmTransferModel</code> represents the download/upload status and its group id, if any.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfirmTransferModel {

    @JsonProperty("status")
    private String status;

    @JsonProperty("transferKey")
    private String transferKey;

    @JsonIgnore
    private String groupId;

    public ConfirmTransferModel() {
        super();
    }

    public ConfirmTransferModel(String transferKey, String status, String groupId) {
        this.transferKey = transferKey;
        this.status = status;
        this.groupId = groupId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransferKey() {
        return transferKey;
    }

    public void setTransferKey(String transferKey) {
        this.transferKey = transferKey;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
