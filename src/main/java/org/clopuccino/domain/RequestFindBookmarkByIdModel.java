package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RequestFindBookmarkByIdModel</code> represents the request information of find bookmark by id.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestFindBookmarkByIdModel extends RequestModel {

    @JsonProperty("id")
    private Long bookmarkId;

    public RequestFindBookmarkByIdModel() {
        super();
    }

    public RequestFindBookmarkByIdModel(Integer sid, String operatorId, Long bookmarkId, String locale) {
        super(sid, operatorId, locale);

        this.bookmarkId = bookmarkId;
    }

    public Long getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(Long bookmarkId) {
        this.bookmarkId = bookmarkId;
    }
}
