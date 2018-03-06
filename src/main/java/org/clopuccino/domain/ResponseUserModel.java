package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>ResponseUserModel</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseUserModel extends ResponseModel {

    @JsonProperty("approved-user")
    private ApprovedUserModel approvedUserModel;

    public ResponseUserModel() {
        super();
    }

    public ResponseUserModel(Integer sid, Integer status, String error, String operatorId, Long timestamp, ApprovedUserModel approvedUserModel) {
        super(sid, status, error, operatorId, timestamp);
        this.approvedUserModel = approvedUserModel;
    }

    public ApprovedUserModel getApprovedUserModel() {
        return approvedUserModel;
    }

    public void setApprovedUserModel(ApprovedUserModel approvedUserModel) {
        this.approvedUserModel = approvedUserModel;
    }
}
