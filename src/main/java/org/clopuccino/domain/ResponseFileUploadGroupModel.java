package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>ResponseFileUploadGroupModel</code> represents the response information of Sid.UPLOAD_FILE_GROUP.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseFileUploadGroupModel extends ResponseModel {

    // unique key for all the upload requests in repository
    @JsonProperty("upload-group-id")
    private String uploadGroupId;

    @JsonProperty("created-in-desktop-timestamp")
    private Long createdInDesktopTimestamp;

    @JsonProperty("created-in-desktop-status")
    private String createdInDesktopStatus; // success or failure

    public ResponseFileUploadGroupModel() {
        super();
    }

    public ResponseFileUploadGroupModel(Integer sid, Integer status, String error, String operatorId, Long timestamp, String uploadGroupId, Long createdInDesktopTimestamp, String createdInDesktopStatus) {
        super(sid, status, error, operatorId, timestamp);

        this.uploadGroupId = uploadGroupId;
        this.createdInDesktopTimestamp = createdInDesktopTimestamp;
        this.createdInDesktopStatus = createdInDesktopStatus;
    }

    public ResponseFileUploadGroupModel(Integer sid, Integer status, String error, String operatorId, Long timestamp) {
        this(sid, status, error, operatorId, timestamp, null, null, null);
    }

    public String getUploadGroupId() {
        return uploadGroupId;
    }

    public void setUploadGroupId(String uploadGroupId) {
        this.uploadGroupId = uploadGroupId;
    }

    public Long getCreatedInDesktopTimestamp() {
        return createdInDesktopTimestamp;
    }

    public void setCreatedInDesktopTimestamp(Long createdInDesktopTimestamp) {
        this.createdInDesktopTimestamp = createdInDesktopTimestamp;
    }

    public String getCreatedInDesktopStatus() {
        return createdInDesktopStatus;
    }

    public void setCreatedInDesktopStatus(String createdInDesktopStatus) {
        this.createdInDesktopStatus = createdInDesktopStatus;
    }
}
