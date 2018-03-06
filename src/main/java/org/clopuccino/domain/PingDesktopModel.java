package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>PingDesktopModel</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PingDesktopModel {

    @JsonProperty("download-size-limit")
    private long downloadSizeLimit;

    @JsonProperty("upload-size-limit")
    private long uploadSizeLimit;

    public PingDesktopModel() {
    }

    public PingDesktopModel(long downloadSizeLimit, long uploadSizeLimit) {
        this.downloadSizeLimit = downloadSizeLimit;
        this.uploadSizeLimit = uploadSizeLimit;
    }

    public long getDownloadSizeLimit() {
        return downloadSizeLimit;
    }

    public void setDownloadSizeLimit(long downloadSizeLimit) {
        this.downloadSizeLimit = downloadSizeLimit;
    }

    public long getUploadSizeLimit() {
        return uploadSizeLimit;
    }

    public void setUploadSizeLimit(long uploadSizeLimit) {
        this.uploadSizeLimit = uploadSizeLimit;
    }
}
