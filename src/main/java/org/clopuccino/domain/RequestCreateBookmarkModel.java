package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RequestCreateBookmarkModel</code> represents the request of creating one bookmark.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestCreateBookmarkModel extends RequestModel {

    @JsonProperty("path")
    private String path;

    @JsonProperty("label")
    private String label;

    @JsonProperty("userId")
    private String userId;

    public RequestCreateBookmarkModel() {
        super();
    }

    public RequestCreateBookmarkModel(Integer sid, String operatorId, String path, String label, String userId, String locale) {
        super(sid, operatorId, locale);

        this.path = path;
        this.label = label;
        this.userId = userId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
