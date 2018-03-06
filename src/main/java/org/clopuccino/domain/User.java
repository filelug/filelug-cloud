package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.clopuccino.Constants;

/**
 * <code>User</code> represent the user.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User implements Cloneable {

    @JsonProperty("account")
    private String account;

    @JsonProperty("country-id")
    private String countryId;

    @JsonProperty("phone")
    private String phoneNumber;

    @JsonProperty("passwd")
    private String passwd;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("showHidden")
    private Boolean showHidden;

    @JsonProperty("locale")
    private String locale;

    @JsonIgnore
    private Boolean verified;

    @JsonIgnore
    private String verifyCode;

    @JsonIgnore
    private Long verifyLetterSentTimestamp;

    @JsonIgnore
    private String resetPasswordSecurityCode;

    @JsonIgnore
    private Long resetPasswordSecurityCodeSentTimestamp;

    @JsonIgnore
    private String unverifiedUserEmail;

    @JsonIgnore
    private String userEmail;

    @JsonIgnore
    private String changeEmailSecurityCode;

    @JsonIgnore
    private Long changeEmailSecurityCodeSentTimestamp;

    @JsonProperty
    private String unverifiedPhoneNumber;

    @JsonIgnore
    private String changePhoneNumberSecurityCode;

    @JsonIgnore
    private Long changePhoneNumberSecurityCodeSentTimestamp;

    @JsonIgnore
    private Boolean shouldUpdatePhoneNumber;

    @JsonIgnore
    private Long availableTransferBytes;

    @JsonIgnore
    private Boolean unlimitedTransfer;

    @JsonIgnore
    private Long downloadFileSizeLimitInBytes;

    @JsonProperty
    private Long uploadFileSizeLimitInBytes;

    @JsonIgnore
    private Boolean deletable;

    public static String generateUniqueUserId(String countryId, String phoneNumber) {
        return new IdGenerators(countryId + phoneNumber).generateId(Constants.DEFAULT_USER_ID_BYTE_COUNT);
    }

    public User() {
    }

    /* Used when we don't need the values of non-json properties */
    public User(String account, String countryId, String phoneNumber, String passwd, String nickname, Boolean showHidden, String locale) {
        this.account = account;
        this.countryId = countryId;
        this.phoneNumber = phoneNumber;
        this.passwd = passwd;
        this.nickname = nickname;
        this.showHidden = showHidden;
        this.locale = locale;
    }

    @Override
    public User clone() throws CloneNotSupportedException {
        return (User) super.clone();
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

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Boolean getShowHidden() {
        return showHidden;
    }

    public void setShowHidden(Boolean showHidden) {
        this.showHidden = showHidden;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }

    public Long getVerifyLetterSentTimestamp() {
        return verifyLetterSentTimestamp;
    }

    public void setVerifyLetterSentTimestamp(Long verifyLetterSentTimestamp) {
        this.verifyLetterSentTimestamp = verifyLetterSentTimestamp;
    }

    public Long getAvailableTransferBytes() {
        return availableTransferBytes;
    }

    public void setAvailableTransferBytes(Long availableTransferBytes) {
        this.availableTransferBytes = availableTransferBytes;
    }

    public Boolean getUnlimitedTransfer() {
        return unlimitedTransfer;
    }

    public void setUnlimitedTransfer(Boolean unlimitedTransfer) {
        this.unlimitedTransfer = unlimitedTransfer;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getResetPasswordSecurityCode() {
        return resetPasswordSecurityCode;
    }

    public void setResetPasswordSecurityCode(String resetPasswordSecurityCode) {
        this.resetPasswordSecurityCode = resetPasswordSecurityCode;
    }

    public Long getResetPasswordSecurityCodeSentTimestamp() {
        return resetPasswordSecurityCodeSentTimestamp;
    }

    public void setResetPasswordSecurityCodeSentTimestamp(Long resetPasswordSecurityCodeSentTimestamp) {
        this.resetPasswordSecurityCodeSentTimestamp = resetPasswordSecurityCodeSentTimestamp;
    }

    public Boolean getShouldUpdatePhoneNumber() {
        return shouldUpdatePhoneNumber;
    }

    public void setShouldUpdatePhoneNumber(Boolean shouldUpdatePhoneNumber) {
        this.shouldUpdatePhoneNumber = shouldUpdatePhoneNumber;
    }

    public String getUnverifiedUserEmail() {
        return unverifiedUserEmail;
    }

    public void setUnverifiedUserEmail(String unverifiedUserEmail) {
        this.unverifiedUserEmail = unverifiedUserEmail;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getChangeEmailSecurityCode() {
        return changeEmailSecurityCode;
    }

    public void setChangeEmailSecurityCode(String changeEmailSecurityCode) {
        this.changeEmailSecurityCode = changeEmailSecurityCode;
    }

    public Long getChangeEmailSecurityCodeSentTimestamp() {
        return changeEmailSecurityCodeSentTimestamp;
    }

    public void setChangeEmailSecurityCodeSentTimestamp(Long changeEmailSecurityCodeSentTimestamp) {
        this.changeEmailSecurityCodeSentTimestamp = changeEmailSecurityCodeSentTimestamp;
    }

    public String getUnverifiedPhoneNumber() {
        return unverifiedPhoneNumber;
    }

    public void setUnverifiedPhoneNumber(String unverifiedPhoneNumber) {
        this.unverifiedPhoneNumber = unverifiedPhoneNumber;
    }

    public String getChangePhoneNumberSecurityCode() {
        return changePhoneNumberSecurityCode;
    }

    public void setChangePhoneNumberSecurityCode(String changePhoneNumberSecurityCode) {
        this.changePhoneNumberSecurityCode = changePhoneNumberSecurityCode;
    }

    public Long getChangePhoneNumberSecurityCodeSentTimestamp() {
        return changePhoneNumberSecurityCodeSentTimestamp;
    }

    public void setChangePhoneNumberSecurityCodeSentTimestamp(Long changePhoneNumberSecurityCodeSentTimestamp) {
        this.changePhoneNumberSecurityCodeSentTimestamp = changePhoneNumberSecurityCodeSentTimestamp;
    }

    public Long getDownloadFileSizeLimitInBytes() {
        return downloadFileSizeLimitInBytes;
    }

    public void setDownloadFileSizeLimitInBytes(Long downloadFileSizeLimitInBytes) {
        this.downloadFileSizeLimitInBytes = downloadFileSizeLimitInBytes;
    }

    public Long getUploadFileSizeLimitInBytes() {
        return uploadFileSizeLimitInBytes;
    }

    public void setUploadFileSizeLimitInBytes(Long uploadFileSizeLimitInBytes) {
        this.uploadFileSizeLimitInBytes = uploadFileSizeLimitInBytes;
    }

    public Boolean getDeletable() {
        return deletable;
    }

    public void setDeletable(Boolean deletable) {
        this.deletable = deletable;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("account='").append(account).append('\'');
        sb.append(", countryId='").append(countryId).append('\'');
        sb.append(", phoneNumber='").append(phoneNumber).append('\'');
        sb.append(", passwd='").append(passwd).append('\'');
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", showHidden=").append(showHidden);
        sb.append(", locale='").append(locale).append('\'');
        sb.append(", verified=").append(verified);
        sb.append(", verifyCode='").append(verifyCode).append('\'');
        sb.append(", verifyLetterSentTimestamp=").append(verifyLetterSentTimestamp);
        sb.append(", resetPasswordSecurityCode='").append(resetPasswordSecurityCode).append('\'');
        sb.append(", resetPasswordSecurityCodeSentTimestamp=").append(resetPasswordSecurityCodeSentTimestamp);
        sb.append(", unverifiedUserEmail='").append(unverifiedUserEmail).append('\'');
        sb.append(", userEmail='").append(userEmail).append('\'');
        sb.append(", changeEmailSecurityCode='").append(changeEmailSecurityCode).append('\'');
        sb.append(", changeEmailSecurityCodeSentTimestamp=").append(changeEmailSecurityCodeSentTimestamp);
        sb.append(", unverifiedPhoneNumber='").append(unverifiedPhoneNumber).append('\'');
        sb.append(", changePhoneNumberSecurityCode='").append(changePhoneNumberSecurityCode).append('\'');
        sb.append(", changePhoneNumberSecurityCodeSentTimestamp=").append(changePhoneNumberSecurityCodeSentTimestamp);
        sb.append(", shouldUpdatePhoneNumber=").append(shouldUpdatePhoneNumber);
        sb.append(", availableTransferBytes=").append(availableTransferBytes);
        sb.append(", unlimitedTransfer=").append(unlimitedTransfer);
        sb.append(", downloadFileSizeLimitInBytes=").append(downloadFileSizeLimitInBytes);
        sb.append(", uploadFileSizeLimitInBytes=").append(uploadFileSizeLimitInBytes);
        sb.append(", deletable=").append(deletable);
        sb.append('}');
        return sb.toString();
    }
}
