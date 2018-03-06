package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RequestFindFileByPathModel</code> represents the request information of find file/directory by path for websocket.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestFindFileByPathModel extends RequestModel {

    @JsonProperty("path")
    private String path;

    @JsonProperty("calculateSize")
    private Boolean calculateSize;

    public RequestFindFileByPathModel() {
        super();
    }

    public RequestFindFileByPathModel(Integer sid, String operatorId, String path, Boolean calculateSize, String locale) {
        super(sid, operatorId, locale);

        this.path = path;
        this.calculateSize = calculateSize;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getCalculateSize() {
        return calculateSize;
    }

    public void setCalculateSize(Boolean calculateSize) {
        this.calculateSize = calculateSize;
    }
}
