package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>ResponseFileUploadModel</code> represents the response information of Sid.UPLOAD_FILE.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseFileUploadModel extends ResponseModel {

    // unique key for all the upload requests in repository
    @JsonProperty("transferKey")
    private String transferKey;

    @JsonProperty("upload-status")
    private String uploadStatus; // success or failure

    public ResponseFileUploadModel() {
        super();
    }

    public ResponseFileUploadModel(Integer sid, Integer status, String error, String operatorId, String clientSessionId, Long timestamp, String transferKey, String uploadStatus) {
        super(sid, status, error, operatorId, clientSessionId, timestamp);
        this.transferKey = transferKey;
        this.uploadStatus = uploadStatus;
    }

    public String getTransferKey() {
        return transferKey;
    }

    public void setTransferKey(String transferKey) {
        this.transferKey = transferKey;
    }

    public String getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(String uploadStatus) {
        this.uploadStatus = uploadStatus;
    }
}
