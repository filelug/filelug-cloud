package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RequestFileUploadModel</code> represents the request information of uploading file for websocket.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestFileUploadModel extends RequestModel {

    @JsonProperty("parent")
    private String directory;

    @JsonProperty("name")
    private String filename;

    @JsonProperty("transferKey")
    private String transferKey;

    @JsonProperty("clientSessionId")
    private String clientSessionId;

    public RequestFileUploadModel() {
        super();
    }

    public RequestFileUploadModel(Integer sid, String operatorId, String directory, String filename, String transferKey, String clientSessionId, String locale) {
        super(sid, operatorId, locale);

        this.directory = directory;

        this.filename = filename;

        this.transferKey = transferKey;

        this.clientSessionId = clientSessionId;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getTransferKey() {
        return transferKey;
    }

    public void setTransferKey(String transferKey) {
        this.transferKey = transferKey;
    }

    public String getClientSessionId() {
        return clientSessionId;
    }

    public void setClientSessionId(String clientSessionId) {
        this.clientSessionId = clientSessionId;
    }
}
