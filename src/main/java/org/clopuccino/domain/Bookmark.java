package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>Bookmark</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Bookmark implements Cloneable {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("path")
    private String path;

    @JsonProperty("realPath")
    private String realPath;

    /* label to display for this fountain */
    @JsonProperty("label")
    private String label;

    /* user who uses this fountain */
    @JsonProperty("userId")
    private String userId;

    /* default use name() as the json value, such as FILE and DIRECTORY */
    @JsonProperty("type")
    private HierarchicalModelType type;

    public Bookmark() {
    }

    public Bookmark(Long id, String path, String realPath, String label, String userId, HierarchicalModelType type) {
        this.id = id;
        this.path = path;
        this.realPath = realPath;
        this.label = label;
        this.userId = userId;
        this.type = type;
    }

    @Override
    public Bookmark clone() throws CloneNotSupportedException {
        return (Bookmark) super.clone();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRealPath() {
        return realPath;
    }

    public void setRealPath(String realPath) {
        this.realPath = realPath;
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
        final StringBuilder sb = new StringBuilder("Bookmark{");
        sb.append("id=").append(id);
        sb.append(", path='").append(path).append('\'');
        sb.append(", realPath='").append(realPath).append('\'');
        sb.append(", label='").append(label).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }
}
