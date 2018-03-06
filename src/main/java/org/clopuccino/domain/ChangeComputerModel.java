package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>ChangeConnectModel</code> is the model for changing computer name service.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChangeComputerModel {

    @JsonProperty("account")
    private String account;

    @JsonProperty("passwd")
    private String password;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("verification")
    private String verification;

    @JsonProperty("locale")
    private String locale;

    @JsonProperty("computer-id")
    private Long computerId;

//    @JsonProperty("old-computer-group")
//    private String oldGroupName;
//
//    @JsonProperty("old-computer-name")
//    private String oldComputerName;

    @JsonProperty("old-recoveryKey")
    private String oldRecoveryKey;

    @JsonProperty("new-computer-group")
    private String newGroupName;

    @JsonProperty("new-computer-name")
    private String newComputerName;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Long getComputerId() {
        return computerId;
    }

    public void setComputerId(Long computerId) {
        this.computerId = computerId;
    }

    //    public String getOldGroupName() {
//        return oldGroupName;
//    }
//
//    public void setOldGroupName(String oldGroupName) {
//        this.oldGroupName = oldGroupName;
//    }
//
//    public String getOldComputerName() {
//        return oldComputerName;
//    }
//
//    public void setOldComputerName(String oldComputerName) {
//        this.oldComputerName = oldComputerName;
//    }

    public String getOldRecoveryKey() {
        return oldRecoveryKey;
    }

    public void setOldRecoveryKey(String oldRecoveryKey) {
        this.oldRecoveryKey = oldRecoveryKey;
    }

    public String getNewGroupName() {
        return newGroupName;
    }

    public void setNewGroupName(String newGroupName) {
        this.newGroupName = newGroupName;
    }

    public String getNewComputerName() {
        return newComputerName;
    }

    public void setNewComputerName(String newComputerName) {
        this.newComputerName = newComputerName;
    }
}
