package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>FileUploadQueryModel</code> is used for JSON to convert to string
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileUploadModel {

    @JsonProperty("transferKey")
    private String transferKey;

    @JsonProperty("transferredSize")
    private Long transferredSize;

    @JsonProperty("fileSize")
    private Long fileSize;

    @JsonProperty("fileLastModifiedDate")
    private Long fileLastModifiedDate;

    public FileUploadModel() {}

    public FileUploadModel(String transferKey, Long transferredSize, Long fileSize, Long fileLastModifiedDate) {
        this.transferKey = transferKey;
        this.transferredSize = transferredSize;
        this.fileSize = fileSize;
        this.fileLastModifiedDate = fileLastModifiedDate;
    }

    public String getTransferKey() {
        return transferKey;
    }

    public void setTransferKey(String transferKey) {
        this.transferKey = transferKey;
    }

    public Long getTransferredSize() {
        return transferredSize;
    }

    public void setTransferredSize(Long transferredSize) {
        this.transferredSize = transferredSize;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getFileLastModifiedDate() {
        return fileLastModifiedDate;
    }

    public void setFileLastModifiedDate(Long fileLastModifiedDate) {
        this.fileLastModifiedDate = fileLastModifiedDate;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileUploadModel{");
        sb.append("transferKey='").append(transferKey).append('\'');
        sb.append(", transferredSize=").append(transferredSize);
        sb.append(", fileSize=").append(fileSize);
        sb.append(", fileLastModifiedDate=").append(fileLastModifiedDate);
        sb.append('}');
        return sb.toString();
    }
}
