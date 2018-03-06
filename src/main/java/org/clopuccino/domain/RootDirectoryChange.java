package org.clopuccino.domain;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RootDirectoryChange</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RootDirectoryChange implements Cloneable {

    @JsonProperty("changeId")
    private Long changeId;

    @JsonProperty("changeTimestamp")
    private Long changeTimestamp;

    /* default use name() as the json value, such as ADD, UPDATE or DELETE */
    @JsonProperty("changeType")
    private ChangeType changeType;

    @JsonProperty("id")
    private Long rootDirectoryId;

    @JsonProperty("path")
    private String path;

    /* label to display for this rootDirectory */
    @JsonProperty("label")
    private String label;

    /* user who uses this rootDirectory */
    @JsonProperty("userId")
    private String userId;

    public RootDirectoryChange() {
    }

    public RootDirectoryChange(Long changeId, Long changeTimestamp, ChangeType changeType, Long rootDirectoryId, String path, String label, String userId) {
        this.changeId = changeId;
        this.changeTimestamp = changeTimestamp;
        this.changeType = changeType;
        this.rootDirectoryId = rootDirectoryId;
        this.path = path;
        this.label = label;
        this.userId = userId;
    }

    @Override
    public RootDirectoryChange clone() throws CloneNotSupportedException {
        return (RootDirectoryChange) super.clone();
    }

    public Long getChangeId() {
        return changeId;
    }

    public void setChangeId(Long changeId) {
        this.changeId = changeId;
    }

    public Long getChangeTimestamp() {
        return changeTimestamp;
    }

    public void setChangeTimestamp(Long changeTimestamp) {
        this.changeTimestamp = changeTimestamp;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public Long getRootDirectoryId() {
        return rootDirectoryId;
    }

    public void setRootDirectoryId(Long rootDirectoryId) {
        this.rootDirectoryId = rootDirectoryId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RootDirectoryChange{");
        sb.append("changeId=").append(changeId);
        sb.append(", changeTimestamp=").append(changeTimestamp);
        sb.append(", changeType=").append(changeType);
        sb.append(", rootDirectoryId=").append(rootDirectoryId);
        sb.append(", path='").append(path).append('\'');
        sb.append(", label='").append(label).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
