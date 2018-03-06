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
public class RequestFileRenameModel extends RequestModel {

    @JsonProperty("path")
    private String path;

    @JsonProperty("filename")
    private String filename;

    public RequestFileRenameModel() {
        super();
    }

    public RequestFileRenameModel(Integer sid, String operatorId, String path, String filename, String locale) {
        super(sid, operatorId, locale);

        this.path = path;
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
