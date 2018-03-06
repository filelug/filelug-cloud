package org.clopuccino.domain;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>BookmarkChange</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookmarkChange implements Cloneable {

    @JsonProperty("changeId")
    private Long changeId;

    @JsonProperty("changeTimestamp")
    private Long changeTimestamp;

    /* default use name() as the json value, such as ADD, UPDATE or DELETE */
    @JsonProperty("changeType")
    private ChangeType changeType;

    @JsonProperty("id")
    private Long bookmarkId;

    @JsonProperty("path")
    private String path;

    /* label to display for this bookmark */
    @JsonProperty("label")
    private String label;

    /* user who uses this bookmark */
    @JsonProperty("userId")
    private String userId;

    /* default use name() as the json value, such as FILE, DIRECTORY or WINDOWNS_SHORTCUT */
    @JsonProperty("type")
    private HierarchicalModelType type;

    public BookmarkChange() {
    }

    public BookmarkChange(Long changeId, Long changeTimestamp, ChangeType changeType, Long bookmarkId, String path, String label, HierarchicalModelType type, String userId) {
        this.changeId = changeId;
        this.changeTimestamp = changeTimestamp;
        this.changeType = changeType;
        this.bookmarkId = bookmarkId;
        this.path = path;
        this.label = label;
        this.type = type;
        this.userId = userId;
    }

    @Override
    public BookmarkChange clone() throws CloneNotSupportedException {
        return (BookmarkChange) super.clone();
    }

    public void setBookmarkId(Long bookmarkId) {
        this.bookmarkId = bookmarkId;
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

    public HierarchicalModelType getType() {
        return type;
    }

    public void setType(HierarchicalModelType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BookmarkChange{");
        sb.append("bookmarkId=").append(bookmarkId);
        sb.append(", changeId=").append(changeId);
        sb.append(", changeTimestamp=").append(changeTimestamp);
        sb.append(", changeType=").append(changeType);
        sb.append(", path='").append(path).append('\'');
        sb.append(", label='").append(label).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }
}
