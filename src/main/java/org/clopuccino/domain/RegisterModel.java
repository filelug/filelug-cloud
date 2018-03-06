package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RegisterModel</code> is the model for register service.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterModel {

    @JsonProperty("account")
    protected String account;

    @JsonProperty("country-id")
    protected String countryId;

    @JsonProperty("phone")
    protected String phoneNumber;

    @JsonProperty("passwd")
    protected String password;

    @JsonProperty("nickname")
    protected String nickname;

    @JsonProperty("verification")
    protected String verification;

    @JsonProperty("email")
    protected String email;

    @JsonProperty("timestamp")
    protected Long timestamp;

    @JsonProperty("ignore-advanced-check")
    protected Boolean ignorePasswordCheck;

    @JsonProperty("locale")
    protected String locale;

    public RegisterModel() {
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getIgnorePasswordCheck() {
        return ignorePasswordCheck;
    }

    public void setIgnorePasswordCheck(Boolean ignorePasswordCheck) {
        this.ignorePasswordCheck = ignorePasswordCheck;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RegisterModel{");
        sb.append("account='").append(account).append('\'');
        sb.append(", countryId='").append(countryId).append('\'');
        sb.append(", phoneNumber='").append(phoneNumber).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", verification='").append(verification).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", ignorePasswordCheck=").append(ignorePasswordCheck);
        sb.append(", locale='").append(locale).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
