package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RequestListDirectoryChildrenModel</code> represents the request information of list directory children for websocket.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestDownloadFileModel extends RequestModel {

    @JsonProperty("path")
    private String path;

    // keep the value of download key
    @JsonProperty("clientSessionId")
    private String clientSessionId;

    // for header RANGE
    @JsonProperty("range")
    private String range;

    @JsonProperty("availableBytes")
    private Long availableBytes;

    @JsonProperty("downloadSizeLimitInBytes")
    private Long downloadSizeLimitInBytes;

    public RequestDownloadFileModel(Integer sid, String operatorId, String path, String clientSessionId, String locale, String range, Long availableBytes, Long downloadSizeLimitInBytes) {
        super(sid, operatorId, locale);

        this.path = path;

        this.clientSessionId = clientSessionId;

        this.range = range;

        this.availableBytes = availableBytes;

        this.downloadSizeLimitInBytes = downloadSizeLimitInBytes;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getClientSessionId() {
        return clientSessionId;
    }

    public void setClientSessionId(String clientSessionId) {
        this.clientSessionId = clientSessionId;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public Long getAvailableBytes() {
        return availableBytes;
    }

    public void setAvailableBytes(Long availableBytes) {
        this.availableBytes = availableBytes;
    }

    public Long getDownloadSizeLimitInBytes() {
        return downloadSizeLimitInBytes;
    }

    public void setDownloadSizeLimitInBytes(Long downloadSizeLimitInBytes) {
        this.downloadSizeLimitInBytes = downloadSizeLimitInBytes;
    }
}
