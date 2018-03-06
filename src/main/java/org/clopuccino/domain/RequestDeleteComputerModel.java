package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RequestFindFileByPathModel</code> represents the request information to delete the computer for websocket.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestDeleteComputerModel extends RequestModel {

    @JsonProperty("verification")
    private String verification;

    public RequestDeleteComputerModel() {
        super();
    }

    public RequestDeleteComputerModel(Integer sid, String operatorId, String locale, String verification) {
        super(sid, operatorId, locale);

        this.verification = verification;
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }
}
