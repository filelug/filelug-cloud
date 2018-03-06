package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>ResponseFileDownloadGroupModel</code> represents the response information of Sid.DOWNLOAD_FILE_GROUP.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseFileDownloadGroupModel extends ResponseModel {

    @JsonProperty("download-group-id")
    private String downloadGroupId;

    public ResponseFileDownloadGroupModel() {
        super();
    }

    public ResponseFileDownloadGroupModel(Integer sid, Integer status, String error, String operatorId, Long timestamp, String downloadGroupId) {
        super(sid, status, error, operatorId, timestamp);

        this.downloadGroupId = downloadGroupId;
    }

    public ResponseFileDownloadGroupModel(Integer sid, Integer status, String error, String operatorId, Long timestamp) {
        this(sid, status, error, operatorId, timestamp, null);
    }

    public String getDownloadGroupId() {
        return downloadGroupId;
    }

    public void setDownloadGroupId(String downloadGroupId) {
        this.downloadGroupId = downloadGroupId;
    }
}
