package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RootDirectory</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RootDirectory implements Cloneable {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("path")
    private String path;

    @JsonProperty("realPath")
    private String realPath;

    // label to display for this root directory
    @JsonProperty("label")
    private String label;

    // user who owns this root directory
    @JsonProperty("userId")
    private String userId;

    // default use name() as the json value 
    @JsonProperty("type")
    private HierarchicalModelType type;

    @JsonProperty("disk")
    private Boolean disk;

    public RootDirectory() {
    }

    @Override
    public RootDirectory clone() throws CloneNotSupportedException {
        return (RootDirectory) super.clone();
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

    public Boolean getDisk() {
        return disk;
    }

    public void setDisk(Boolean disk) {
        this.disk = disk;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RootDirectory{");
        sb.append("id=").append(id);
        sb.append(", path='").append(path).append('\'');
        sb.append(", realPath='").append(realPath).append('\'');
        sb.append(", label='").append(label).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", type=").append(type);
        sb.append(", disk=").append(disk);
        sb.append('}');
        return sb.toString();
    }
}
