package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * <code>FileDownloadGroup</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileDownloadGroup {

    public static final Integer DEFAULT_SUBDIRECTORY_TYPE = 0;

    public static final Integer DEFAULT_DESCRIPTION_TYPE = 0;

    public static final Integer DEFAULT_NOTIFICATION_TYPE = 2;

    @JsonProperty("download-group-id")
    private String downloadGroupId;

    @JsonProperty("download-dir")
    private String downloadGroupDirectory;

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

    @JsonProperty("download-key-paths")
    private Map<String, String> downloadKeyAndPaths;

    @JsonIgnore
    private String userId;

    @JsonIgnore
    private Long computerId;

    public FileDownloadGroup() {
    }

    public FileDownloadGroup(String downloadGroupId,
                             String downloadGroupDirectory,
                             Integer subdirectoryType,
                             Integer descriptionType,
                             Integer notificationType,
                             String subdirectoryValue,
                             String descriptionValue,
                             Map<String, String> downloadKeyAndPaths,
                             String userId,
                             Long computerId) {
        this.downloadGroupId = downloadGroupId;
        this.downloadGroupDirectory = downloadGroupDirectory;
        this.subdirectoryType = subdirectoryType;
        this.descriptionType = descriptionType;
        this.notificationType = notificationType;
        this.descriptionValue = descriptionValue;
        this.subdirectoryValue = subdirectoryValue;
        this.downloadKeyAndPaths = downloadKeyAndPaths;
        this.userId = userId;
        this.computerId = computerId;
    }

    public Long getComputerId() {
        return computerId;
    }

    public void setComputerId(Long computerId) {
        this.computerId = computerId;
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

    public String getDownloadGroupDirectory() {
        return downloadGroupDirectory;
    }

    public void setDownloadGroupDirectory(String downloadGroupDirectory) {
        this.downloadGroupDirectory = downloadGroupDirectory;
    }

    public String getDownloadGroupId() {
        return downloadGroupId;
    }

    public void setDownloadGroupId(String downloadGroupId) {
        this.downloadGroupId = downloadGroupId;
    }

    public Map<String, String> getDownloadKeyAndPaths() {
        return downloadKeyAndPaths;
    }

    public void setDownloadKeyAndPaths(Map<String, String> downloadKeyAndPaths) {
        this.downloadKeyAndPaths = downloadKeyAndPaths;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileDownloadGroup{");
        sb.append("computerId=").append(computerId);
        sb.append(", downloadGroupId='").append(downloadGroupId).append('\'');
        sb.append(", downloadGroupDirectory='").append(downloadGroupDirectory).append('\'');
        sb.append(", subdirectoryType=").append(subdirectoryType);
        sb.append(", descriptionType=").append(descriptionType);
        sb.append(", notificationType=").append(notificationType);
        sb.append(", subdirectoryValue='").append(subdirectoryValue).append('\'');
        sb.append(", descriptionValue='").append(descriptionValue).append('\'');
        sb.append(", downloadKeyAndPaths=").append(downloadKeyAndPaths);
        sb.append(", userId='").append(userId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
