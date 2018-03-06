package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>ChangePasswordModel</code> is the model for change password service.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChangePasswordModel {

    @JsonProperty("sid")
    private Integer sid;

    @JsonProperty("account")
    private String account;

    @JsonProperty("old-passwd")
    private String oldPassword;

    @JsonProperty("new-passwd")
    private String newPassword;

    @JsonProperty("verification")
    private String verification;

    public ChangePasswordModel() {
    }

    public ChangePasswordModel(Integer sid, String account, String oldPassword, String newPassword, String verification) {
        this();

        this.sid = sid;
        this.account = account;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.verification = verification;
    }

    public Integer getSid() {
        return sid;
    }

    public void setSid(Integer sid) {
        this.sid = sid;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }
}
