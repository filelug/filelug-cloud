package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>UserComputer</code> represents user computer table data.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserComputer extends UserComputerWithoutProfiles {

    @JsonProperty("upload-directory")
    private String uploadDirectory;

    @JsonProperty("upload-subdirectory-type")
    private Integer uploadSubdirectoryType;

    @JsonProperty("upload-subdirectory-value")
    private String uploadSubdirectoryValue;

    @JsonProperty("upload-description-type")
    private Integer uploadDescriptionType;

    @JsonProperty("upload-description-value")
    private String uploadDescriptionValue;

    @JsonProperty("upload-notification-type")
    private Integer uploadNotificationType;

    @JsonProperty("download-directory")
    private String downloadDirectory;

    @JsonProperty("download-subdirectory-type")
    private Integer downloadSubdirectoryType;

    @JsonProperty("download-subdirectory-value")
    private String downloadSubdirectoryValue;

    @JsonProperty("download-description-type")
    private Integer downloadDescriptionType;

    @JsonProperty("download-description-value")
    private String downloadDescriptionValue;

    @JsonProperty("download-notification-type")
    private Integer downloadNotificationType;

    public UserComputer() {
    }

    public UserComputer(String userComputerId, String userId, Long computerId, String computerAdminId, String groupName, String computerName, String encryptedUserComputerId, String lugServerId, Boolean socketConnected, Boolean needReconnect, Boolean allowAlias,
                        String uploadDirectory,
                        Integer uploadSubdirectoryType, String uploadSubdirectoryValue, Integer uploadDescriptionType, String uploadDescriptionValue, Integer uploadNotificationType,
                        String downloadDirectory,
                        Integer downloadSubdirectoryType, String downloadSubdirectoryValue, Integer downloadDescriptionType, String downloadDescriptionValue, Integer downloadNotificationType) {
        this(userComputerId, userId, computerId, computerAdminId, groupName, computerName, encryptedUserComputerId, lugServerId, socketConnected, needReconnect, allowAlias);

//        this.userComputerId = userComputerId;
//        this.userId = userId;
//        this.computerId = computerId;
//        this.computerAdminId = computerAdminId;
//        this.groupName = groupName;
//        this.computerName = computerName;
//        this.encryptedUserComputerId = encryptedUserComputerId;
//        this.lugServerId = lugServerId;
//        this.socketConnected = socketConnected;
//        this.needReconnect = needReconnect;
//        this.allowAlias = allowAlias;

        this.uploadDirectory = uploadDirectory;

        this.uploadSubdirectoryType = uploadSubdirectoryType;
        this.uploadSubdirectoryValue = uploadSubdirectoryValue;
        this.uploadDescriptionType = uploadDescriptionType;
        this.uploadDescriptionValue = uploadDescriptionValue;
        this.uploadNotificationType = uploadNotificationType;

        this.downloadDirectory = downloadDirectory;

        this.downloadSubdirectoryType = downloadSubdirectoryType;
        this.downloadSubdirectoryValue = downloadSubdirectoryValue;
        this.downloadDescriptionType = downloadDescriptionType;
        this.downloadDescriptionValue = downloadDescriptionValue;
        this.downloadNotificationType = downloadNotificationType;
    }

    public UserComputer(String userComputerId, String userId, Long computerId, String computerAdminId, String groupName, String computerName, String encryptedUserComputerId, String lugServerId, Boolean socketConnected, Boolean needReconnect, Boolean allowAlias) {
        super(userComputerId, userId, computerId, computerAdminId, groupName, computerName, encryptedUserComputerId, lugServerId, socketConnected, needReconnect, allowAlias);
    }

    public String getUploadDirectory() {
        return uploadDirectory;
    }

    public void setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }

    public String getDownloadDescriptionValue() {
        return downloadDescriptionValue;
    }

    public void setDownloadDescriptionValue(String downloadDescriptionValue) {
        this.downloadDescriptionValue = downloadDescriptionValue;
    }

    public Integer getDownloadDescriptionType() {
        return downloadDescriptionType;
    }

    public void setDownloadDescriptionType(Integer downloadDescriptionType) {
        this.downloadDescriptionType = downloadDescriptionType;
    }

    public String getDownloadDirectory() {
        return downloadDirectory;
    }

    public void setDownloadDirectory(String downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
    }

    public Integer getDownloadNotificationType() {
        return downloadNotificationType;
    }

    public void setDownloadNotificationType(Integer downloadNotificationType) {
        this.downloadNotificationType = downloadNotificationType;
    }

    public String getDownloadSubdirectoryValue() {
        return downloadSubdirectoryValue;
    }

    public void setDownloadSubdirectoryValue(String downloadSubdirectoryValue) {
        this.downloadSubdirectoryValue = downloadSubdirectoryValue;
    }

    public Integer getDownloadSubdirectoryType() {
        return downloadSubdirectoryType;
    }

    public void setDownloadSubdirectoryType(Integer downloadSubdirectoryType) {
        this.downloadSubdirectoryType = downloadSubdirectoryType;
    }

    public String getUploadDescriptionValue() {
        return uploadDescriptionValue;
    }

    public void setUploadDescriptionValue(String uploadDescriptionValue) {
        this.uploadDescriptionValue = uploadDescriptionValue;
    }

    public Integer getUploadDescriptionType() {
        return uploadDescriptionType;
    }

    public void setUploadDescriptionType(Integer uploadDescriptionType) {
        this.uploadDescriptionType = uploadDescriptionType;
    }

    public Integer getUploadNotificationType() {
        return uploadNotificationType;
    }

    public void setUploadNotificationType(Integer uploadNotificationType) {
        this.uploadNotificationType = uploadNotificationType;
    }

    public String getUploadSubdirectoryValue() {
        return uploadSubdirectoryValue;
    }

    public void setUploadSubdirectoryValue(String uploadSubdirectoryValue) {
        this.uploadSubdirectoryValue = uploadSubdirectoryValue;
    }

    public Integer getUploadSubdirectoryType() {
        return uploadSubdirectoryType;
    }

    public void setUploadSubdirectoryType(Integer uploadSubdirectoryType) {
        this.uploadSubdirectoryType = uploadSubdirectoryType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserComputer{");

        sb.append("allowAlias=").append(isAllowAlias());
        sb.append(", userComputerId='").append(getUserComputerId()).append('\'');
        sb.append(", userId='").append(getUserId()).append('\'');
        sb.append(", computerId=").append(getComputerId());
        sb.append(", computerAdminId='").append(getComputerAdminId()).append('\'');
        sb.append(", groupName='").append(getGroupName()).append('\'');
        sb.append(", computerName='").append(getComputerName()).append('\'');
        sb.append(", encryptedUserComputerId='").append(getEncryptedUserComputerId()).append('\'');
        sb.append(", lugServerId='").append(getLugServerId()).append('\'');
        sb.append(", socketConnected=").append(isSocketConnected());
        sb.append(", needReconnect=").append(isNeedReconnect());

        sb.append(", uploadDirectory='").append(uploadDirectory).append('\'');

        sb.append(", uploadSubdirectoryType=").append(uploadSubdirectoryType);
        sb.append(", uploadSubdirectoryValue='").append(uploadSubdirectoryValue).append('\'');
        sb.append(", uploadDescriptionType=").append(uploadDescriptionType);
        sb.append(", uploadDescriptionValue='").append(uploadDescriptionValue).append('\'');
        sb.append(", uploadNotificationType=").append(uploadNotificationType);

        sb.append(", downloadDirectory='").append(downloadDirectory).append('\'');

        sb.append(", downloadSubdirectoryType=").append(downloadSubdirectoryType);
        sb.append(", downloadSubdirectoryValue='").append(downloadSubdirectoryValue).append('\'');
        sb.append(", downloadDescriptionType=").append(downloadDescriptionType);
        sb.append(", downloadDescriptionValue='").append(downloadDescriptionValue).append('\'');
        sb.append(", downloadNotificationType=").append(downloadNotificationType);
        sb.append('}');

        return sb.toString();
    }
}
