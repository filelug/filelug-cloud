package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RequestDeleteFileUploadModel</code> represents the request information of delete file upload data for websocket.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestDeleteFileUploadModel extends RequestModel {

    @JsonProperty("transferKey")
    private String transferKey;

    @JsonProperty("clientSessionId")
    private String clientSessionId;

    public RequestDeleteFileUploadModel() {
        super();
    }

    public RequestDeleteFileUploadModel(Integer sid, String operatorId, String transferKey, String clientSessionId, String locale) {
        super(sid, operatorId, locale);

        this.transferKey = transferKey;

        this.clientSessionId = clientSessionId;
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
