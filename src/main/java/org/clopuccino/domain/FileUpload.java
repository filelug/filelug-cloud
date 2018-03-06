package org.clopuccino.domain;

/**
 * <code>FileUpload</code> represents the file upload data
 *
 * @author masonhsieh
 * @version 1.0
 */
public class FileUpload {
    private String transferKey;
    private String userId;
    private Long computerId;
    private String groupName;
    private String computerName;
    private String filename;
    private String directory;
    private Long fileSize;
    private String uploadGroupId;
    private Long startTimestamp;
    private Long endTimestamp;
    private String status;
    // Absolute path of the tmp file
    private String tmpFile;
    // the file byte index that already saved to tmpFile
    private Long transferredByteIndex;
    // the last modified date of the source file from the request client
    private Long sourceFileLastModifiedTimestamp;

    public FileUpload() {
    }

    public FileUpload(String transferKey, String userId, Long computerId, String groupName, String computerName, String filename, String directory, Long fileSize, String uploadGroupId, Long startTimestamp, Long endTimestamp, String status, String tmpFile, Long transferredByteIndex, Long sourceFileLastModifiedTimestamp) {
        this.transferKey = transferKey;
        this.userId = userId;
        this.computerId = computerId;
        this.groupName = groupName;
        this.computerName = computerName;
        this.filename = filename;
        this.directory = directory;
        this.fileSize = fileSize;
        this.uploadGroupId = uploadGroupId;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.status = status;
        this.tmpFile = tmpFile;
        this.transferredByteIndex = transferredByteIndex;
        this.sourceFileLastModifiedTimestamp = sourceFileLastModifiedTimestamp;
    }

    public Long getComputerId() {
        return computerId;
    }

    public void setComputerId(Long computerId) {
        this.computerId = computerId;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getUploadGroupId() {
        return uploadGroupId;
    }

    public void setUploadGroupId(String uploadGroupId) {
        this.uploadGroupId = uploadGroupId;
    }

    public String getTmpFile() {
        return tmpFile;
    }

    public void setTmpFile(String tmpFile) {
        this.tmpFile = tmpFile;
    }

    public Long getTransferredByteIndex() {
        return transferredByteIndex;
    }

    public void setTransferredByteIndex(Long transferredByteIndex) {
        this.transferredByteIndex = transferredByteIndex;
    }

    public Long getSourceFileLastModifiedTimestamp() {
        return sourceFileLastModifiedTimestamp;
    }

    public void setSourceFileLastModifiedTimestamp(Long sourceFileLastModifiedTimestamp) {
        this.sourceFileLastModifiedTimestamp = sourceFileLastModifiedTimestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileUpload{");
        sb.append("transferKey='").append(transferKey).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", computerId=").append(computerId);
        sb.append(", groupName='").append(groupName).append('\'');
        sb.append(", computerName='").append(computerName).append('\'');
        sb.append(", filename='").append(filename).append('\'');
        sb.append(", directory='").append(directory).append('\'');
        sb.append(", fileSize=").append(fileSize);
        sb.append(", uploadGroupId='").append(uploadGroupId).append('\'');
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append(", endTimestamp=").append(endTimestamp);
        sb.append(", status='").append(status).append('\'');
        sb.append(", tmpFile='").append(tmpFile).append('\'');
        sb.append(", transferredByteIndex=").append(transferredByteIndex);
        sb.append(", sourceFileLastModifiedTimestamp=").append(sourceFileLastModifiedTimestamp);
        sb.append('}');
        return sb.toString();
    }
}
