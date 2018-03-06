package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/**
 * <code>DesktopUser</code> extends User to communicate with dekstop.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApprovedUserModel implements Cloneable {

    @JsonProperty("account")
    private String account;

    @JsonProperty("country-id")
    private String countryId;

    @JsonProperty("phone")
    private String phoneNumber;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("showHidden")
    private Boolean showHidden;

    @JsonProperty("allowAlias")
    private Boolean allowAlias;

    // The session id used for the connected desktop
    @JsonProperty("session-id")
    private String desktopSessionId;

    public ApprovedUserModel() {
    }

    public ApprovedUserModel(String account, String nickname, Boolean showHidden, Boolean allowAlias, String desktopSessionId) {
        this(account, null, null, nickname, showHidden, allowAlias, desktopSessionId);
    }

    public ApprovedUserModel(String account, String countryId, String phoneNumber, String nickname, Boolean showHidden, Boolean allowAlias, String desktopSessionId) {
        this.account = account;
        this.countryId = countryId;
        this.phoneNumber = phoneNumber;
        this.nickname = nickname;
        this.showHidden = showHidden;
        this.allowAlias = allowAlias;
        this.desktopSessionId = desktopSessionId;
    }

    @Override
    public ApprovedUserModel clone() throws CloneNotSupportedException {
        // shallow clone, not a deep one
        return (ApprovedUserModel) super.clone();
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean isShowHidden() {
        return showHidden;
    }

    public void setShowHidden(Boolean showHidden) {
        this.showHidden = showHidden;
    }

    public Boolean isAllowAlias() {
        return allowAlias;
    }

    public void setAllowAlias(Boolean allowAlias) {
        this.allowAlias = allowAlias;
    }

    public String getDesktopSessionId() {
        return desktopSessionId;
    }

    public void setDesktopSessionId(String desktopSessionId) {
        this.desktopSessionId = desktopSessionId;
    }
}
