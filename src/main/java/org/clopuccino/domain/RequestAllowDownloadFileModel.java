package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RequestAllowDownloadFileModel</code> represents the request information if file allowed to download for websocket.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestAllowDownloadFileModel extends RequestModel {

    // unique key for the download for all download-requests for the same repository
    @JsonProperty("transferKey")
    private String transferKey;

    @JsonProperty("path")
    private String path;

    @JsonProperty("availableBytes")
    private Long availableBytes;

    public RequestAllowDownloadFileModel() {
        super();
    }

    public RequestAllowDownloadFileModel(Integer sid, String operatorId, String transferKey, String path, Long availableBytes, String locale) {
        super(sid, operatorId, locale);

        this.transferKey = transferKey;
        this.path = path;
        this.availableBytes = availableBytes;
    }

    public String getTransferKey() {
        return transferKey;
    }

    public void setTransferKey(String transferKey) {
        this.transferKey = transferKey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getAvailableBytes() {
        return availableBytes;
    }

    public void setAvailableBytes(Long availableBytes) {
        this.availableBytes = availableBytes;
    }
}
