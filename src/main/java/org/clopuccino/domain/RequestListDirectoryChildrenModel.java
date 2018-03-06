package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RequestListDirectoryChildrenModel</code> represents the request information of list directory children for websocket.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestListDirectoryChildrenModel extends RequestModel {

    @JsonProperty("path")
    private String path;

    @JsonProperty("showHidden")
    private Boolean showHidden;

    public RequestListDirectoryChildrenModel() {
        super();
    }

    public RequestListDirectoryChildrenModel(Integer sid, String operatorId, String locale, String path, Boolean showHidden) {
        super(sid, operatorId, locale);

        this.path = path;
        this.showHidden = showHidden;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getShowHidden() {
        return showHidden;
    }

    public void setShowHidden(Boolean showHidden) {
        this.showHidden = showHidden;
    }
}
