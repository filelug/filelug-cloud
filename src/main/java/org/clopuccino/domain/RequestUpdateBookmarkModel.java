package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RequestUpdateBookmarkModel</code> represents the request of update one bookmark.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestUpdateBookmarkModel extends RequestModel {

    @JsonProperty("id")
    private Long bookmarkId;

    @JsonProperty("path")
    private String path;

    /* label to display for this bookmark */
    @JsonProperty("label")
    private String label;

    public RequestUpdateBookmarkModel() {
        super();
    }

    public RequestUpdateBookmarkModel(Integer sid, String operatorId, Long bookmarkId, String path, String label, String locale) {
        super(sid, operatorId, locale);

        this.bookmarkId = bookmarkId;
        this.path = path;
        this.label = label;
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

    public Long getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(Long bookmarkId) {
        this.bookmarkId = bookmarkId;
    }
}
