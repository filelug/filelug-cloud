package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * <code>FileUploadGroup</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileUploadGroup {

    public static final Integer DEFAULT_SUBDIRECTORY_TYPE = 0;

    public static final Integer DEFAULT_DESCRIPTION_TYPE = 0;

    public static final Integer DEFAULT_NOTIFICATION_TYPE = 2;

    @JsonProperty("upload-group-id")
    private String uploadGroupId;

    @JsonProperty("upload-dir")
    private String uploadGroupDirectory;

    @JsonProperty("subdirectory-type")
    private Integer subdirectoryType;

    @JsonProperty("description-type")
    private Integer descriptionType;

    @JsonProperty("notification-type")
    private Integer notificationType;

    @JsonProperty("subdirectory-value")
    private String subdirectoryValue;

    @JsonProperty("description-value")
    private String descriptionValue;

    @JsonProperty("upload-keys")
    private List<String> uploadKeys;

    @JsonIgnore
    private String userId;

    @JsonIgnore
    private Long computerId;

    @JsonIgnore
    private Long createdInDesktopTimestamp;

    @JsonIgnore
    private String createdInDesktopStatus; // success or failure. If empty, meaning not created.

    public FileUploadGroup() {
    }

    public FileUploadGroup(String uploadGroupId,
                           String uploadGroupDirectory,
                           Integer subdirectoryType,
                           Integer descriptionType,
                           Integer notificationType,
                           String subdirectoryValue,
                           String descriptionValue,
                           List<String> uploadKeys,
                           String userId,
                           Long computerId,
                           Long createdInDesktopTimestamp,
                           String createdInDesktopStatus) {
        this.uploadGroupId = uploadGroupId;
        this.uploadGroupDirectory = uploadGroupDirectory;
        this.subdirectoryType = subdirectoryType;
        this.descriptionType = descriptionType;
        this.notificationType = notificationType;
        this.descriptionValue = descriptionValue;
        this.subdirectoryValue = subdirectoryValue;
        this.uploadKeys = uploadKeys;
        this.userId = userId;
        this.computerId = computerId;
        this.createdInDesktopTimestamp = createdInDesktopTimestamp;
        this.createdInDesktopStatus = createdInDesktopStatus;
    }

    public Long getComputerId() {
        return computerId;
    }

    public void setComputerId(Long computerId) {
        this.computerId = computerId;
    }

    public Long getCreatedInDesktopTimestamp() {
        return createdInDesktopTimestamp;
    }

    public void setCreatedInDesktopTimestamp(Long createdInDesktopTimestamp) {
        this.createdInDesktopTimestamp = createdInDesktopTimestamp;
    }

    public String getCreatedInDesktopStatus() {
        return createdInDesktopStatus;
    }

    public void setCreatedInDesktopStatus(String createdInDesktopStatus) {
        this.createdInDesktopStatus = createdInDesktopStatus;
    }

    public Integer getDescriptionType() {
        return descriptionType;
    }

    public void setDescriptionType(Integer descriptionType) {
        this.descriptionType = descriptionType;
    }

    public String getDescriptionValue() {
        return descriptionValue;
    }

    public void setDescriptionValue(String descriptionValue) {
        this.descriptionValue = descriptionValue;
    }

    public Integer getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(Integer notificationType) {
        this.notificationType = notificationType;
    }

    public Integer getSubdirectoryType() {
        return subdirectoryType;
    }

    public void setSubdirectoryType(Integer subdirectoryType) {
        this.subdirectoryType = subdirectoryType;
    }

    public String getSubdirectoryValue() {
        return subdirectoryValue;
    }

    public void setSubdirectoryValue(String subdirectoryValue) {
        this.subdirectoryValue = subdirectoryValue;
    }

    public String getUploadGroupDirectory() {
        return uploadGroupDirectory;
    }

    public void setUploadGroupDirectory(String uploadGroupDirectory) {
        this.uploadGroupDirectory = uploadGroupDirectory;
    }

    public String getUploadGroupId() {
        return uploadGroupId;
    }

    public void setUploadGroupId(String uploadGroupId) {
        this.uploadGroupId = uploadGroupId;
    }

    public List<String> getUploadKeys() {
        return uploadKeys;
    }

    public void setUploadKeys(List<String> uploadKeys) {
        this.uploadKeys = uploadKeys;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileUploadGroup{");
        sb.append("computerId=").append(computerId);
        sb.append(", uploadGroupId='").append(uploadGroupId).append('\'');
        sb.append(", uploadGroupDirectory='").append(uploadGroupDirectory).append('\'');
        sb.append(", subdirectoryType=").append(subdirectoryType);
        sb.append(", descriptionType=").append(descriptionType);
        sb.append(", notificationType=").append(notificationType);
        sb.append(", subdirectoryValue='").append(subdirectoryValue).append('\'');
        sb.append(", descriptionValue='").append(descriptionValue).append('\'');
        sb.append(", uploadKeys=").append(uploadKeys);
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", createdInDesktopTimestamp=").append(createdInDesktopTimestamp);
        sb.append(", createdInDesktopStatus='").append(createdInDesktopStatus).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
