package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>TransferHistoryModel</code> for the information of a downloaded/uploaded file
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferHistoryModel {

    @JsonProperty("computer-group")
    private String computerGroup;

    @JsonProperty("computer-name")
    private String computerName;

    @JsonProperty("fileSize")
    private long fileSize;

    @JsonProperty("endTimestamp")
    private long endTimestamp;

    @JsonProperty("filename")
    private String filename;

    public TransferHistoryModel() {
    }

    public TransferHistoryModel(String computerGroup, String computerName, long fileSize, long endTimestamp, String filename) {
        this.computerGroup = computerGroup;
        this.computerName = computerName;
        this.fileSize = fileSize;
        this.endTimestamp = endTimestamp;
        this.filename = filename;
    }

    public String getComputerGroup() {
        return computerGroup;
    }

    public void setComputerGroup(String computerGroup) {
        this.computerGroup = computerGroup;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
