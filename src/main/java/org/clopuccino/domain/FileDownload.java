package org.clopuccino.domain;

/**
 * <code>FileDownload</code> represents the file download data
 *
 * @author masonhsieh
 * @version 1.0
 */
public class FileDownload {
    private String transferKey;
    private String userId;
    private Long computerId;
    private String groupName;
    private String computerName;
    private String filePath;
    private Long fileSize;
    private Long startTimestamp;
    private Long endTimestamp;
    private String status;
    private String downloadGroupId;

    // Absolute path of the tmp file
    private String tmpFile;

    private Long tmpFileCreatedTimestamp;
    private Long tmpFileDeletedTimestamp;

    private String fromIp;
    private String fromHost;
    private String toIp;
    private String toHost;


    public FileDownload() {
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

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDownloadGroupId() {
        return downloadGroupId;
    }

    public void setDownloadGroupId(String downloadGroupId) {
        this.downloadGroupId = downloadGroupId;
    }

    public String getTmpFile() {
        return tmpFile;
    }

    public void setTmpFile(String tmpFile) {
        this.tmpFile = tmpFile;
    }

    public String getTransferKey() {
        return transferKey;
    }

    public void setTransferKey(String transferKey) {
        this.transferKey = transferKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getTmpFileCreatedTimestamp() {
        return tmpFileCreatedTimestamp;
    }

    public void setTmpFileCreatedTimestamp(Long tmpFileCreatedTimestamp) {
        this.tmpFileCreatedTimestamp = tmpFileCreatedTimestamp;
    }

    public Long getTmpFileDeletedTimestamp() {
        return tmpFileDeletedTimestamp;
    }

    public void setTmpFileDeletedTimestamp(Long tmpFileDeletedTimestamp) {
        this.tmpFileDeletedTimestamp = tmpFileDeletedTimestamp;
    }

    public String getFromHost() {
        return fromHost;
    }

    public void setFromHost(String fromHost) {
        this.fromHost = fromHost;
    }

    public String getFromIp() {
        return fromIp;
    }

    public void setFromIp(String fromIp) {
        this.fromIp = fromIp;
    }

    public String getToHost() {
        return toHost;
    }

    public void setToHost(String toHost) {
        this.toHost = toHost;
    }

    public String getToIp() {
        return toIp;
    }

    public void setToIp(String toIp) {
        this.toIp = toIp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileDownload{");
        sb.append("computerId=").append(computerId);
        sb.append(", transferKey='").append(transferKey).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", groupName='").append(groupName).append('\'');
        sb.append(", computerName='").append(computerName).append('\'');
        sb.append(", filePath='").append(filePath).append('\'');
        sb.append(", fileSize=").append(fileSize);
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append(", endTimestamp=").append(endTimestamp);
        sb.append(", status='").append(status).append('\'');
        sb.append(", downloadGroupId='").append(downloadGroupId).append('\'');
        sb.append(", tmpFile='").append(tmpFile).append('\'');
        sb.append(", tmpFileCreatedTimestamp=").append(tmpFileCreatedTimestamp);
        sb.append(", tmpFileDeletedTimestamp=").append(tmpFileDeletedTimestamp);
//        sb.append(", fromIp='").append(fromIp).append('\'');
//        sb.append(", fromHost='").append(fromHost).append('\'');
//        sb.append(", toIp='").append(toIp).append('\'');
//        sb.append(", toHost='").append(toHost).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
