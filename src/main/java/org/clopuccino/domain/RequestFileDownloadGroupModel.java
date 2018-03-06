package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;

/**
 * <code>RequestFileDownloadGroupModel</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestFileDownloadGroupModel extends RequestModel {

    @JsonProperty("download-group-id")
    private String downloadGroupId;

    @JsonProperty("file-paths")
    private Collection<String> filePaths;

    @JsonProperty("file-size-limit")
    private Long fileSizeLimitInBytes;

    public RequestFileDownloadGroupModel(Integer sid, String operatorId, String locale, String downloadGroupId, Collection<String> filePaths, Long fileSizeLimitInBytes) {
        super(sid, operatorId, locale);

        this.downloadGroupId = downloadGroupId;

        this.filePaths = filePaths;

        this.fileSizeLimitInBytes = fileSizeLimitInBytes;
    }

    public String getDownloadGroupId() {
        return downloadGroupId;
    }

    public void setDownloadGroupId(String downloadGroupId) {
        this.downloadGroupId = downloadGroupId;
    }

    public Collection<String> getFilePaths() {
        return filePaths;
    }

    public void setFilePaths(Collection<String> filePaths) {
        this.filePaths = filePaths;
    }

    public Long getFileSizeLimitInBytes() {
        return fileSizeLimitInBytes;
    }

    public void setFileSizeLimitInBytes(Long fileSizeLimitInBytes) {
        this.fileSizeLimitInBytes = fileSizeLimitInBytes;
    }
}
