package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RequestAllowUploadFileModel</code> represents the request information if file allowed to upload for websocket.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestAllowUploadFileModel extends RequestModel {

    // unique key for the upload for all upload-requests for the same repository
    @JsonProperty("transferKey")
    private String transferKey;

    @JsonProperty("directory")
    private String directory;

    @JsonProperty("filename")
    private String filename;

    public RequestAllowUploadFileModel() {
        super();
    }

    public RequestAllowUploadFileModel(Integer sid, String operatorId, String transferKey, String directory, String filename, String locale) {
        super(sid, operatorId, locale);

        this.transferKey = transferKey;
        this.directory = directory;
        this.filename = filename;
    }

    public String getTransferKey() {
        return transferKey;
    }

    public void setTransferKey(String transferKey) {
        this.transferKey = transferKey;
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
}
