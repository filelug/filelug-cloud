package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>ChangeAdministratorModel</code> is the model for change administratorio service.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChangeAdministratorModel {

    @JsonProperty("old-account")
    private String oldAdminUserId;

    @JsonProperty("old-passwd")
    private String oldAdminPassword;

    @JsonProperty("verification")
    private String verification;

    @JsonProperty("new-account")
    private String newAdminUserId;

    @JsonProperty("new-passwd")
    private String newAdminPassword;

    @JsonProperty("computer-id")
    private Long computerId;

    @JsonProperty("recoveryKey")
    private String recoveryKey;

    @JsonProperty("locale")
    private String locale;

    public ChangeAdministratorModel() {
    }

    public Long getComputerId() {
        return computerId;
    }

    public void setComputerId(Long computerId) {
        this.computerId = computerId;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getNewAdminPassword() {
        return newAdminPassword;
    }

    public void setNewAdminPassword(String newAdminPassword) {
        this.newAdminPassword = newAdminPassword;
    }

    public String getNewAdminUserId() {
        return newAdminUserId;
    }

    public void setNewAdminUserId(String newAdminUserId) {
        this.newAdminUserId = newAdminUserId;
    }

    public String getOldAdminPassword() {
        return oldAdminPassword;
    }

    public void setOldAdminPassword(String oldAdminPassword) {
        this.oldAdminPassword = oldAdminPassword;
    }

    public String getOldAdminUserId() {
        return oldAdminUserId;
    }

    public void setOldAdminUserId(String oldAdminUserId) {
        this.oldAdminUserId = oldAdminUserId;
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }

    public String getRecoveryKey() {
        return recoveryKey;
    }

    public void setRecoveryKey(String recoveryKey) {
        this.recoveryKey = recoveryKey;
    }
}
