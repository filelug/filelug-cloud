package org.clopuccino.domain;


/**
 * <code>AccountKit</code> represents the account kit data.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class AccountKit implements Cloneable {

    private long accountKitId;

    private long createdTimestamp;

    private String userAccessToken;

    private String authorizationCode;

    private String authorizationCodeEncrypted;

    // id from invoking service of facebook account kit to get user data using user access token
    private String accountKitUserId;

    // "+" && country code && national phone number
    private String countryPhoneNumber;

    // country code without prefix "+"
    private String countryPrefix;

    // phone number only, no country code, and no prefix "0"
    private String nationalPhoneNumber;

    // the auth_user_id from ref table: auth_user
    private String userId;

    public AccountKit() {
    }

    public AccountKit(long accountKitId, long createdTimestamp, String userAccessToken, String authorizationCode, String authorizationCodeEncrypted, String accountKitUserId, String countryPhoneNumber, String countryPrefix, String nationalPhoneNumber, String userId) {
        this.accountKitId = accountKitId;
        this.createdTimestamp = createdTimestamp;
        this.userAccessToken = userAccessToken;
        this.authorizationCode = authorizationCode;
        this.authorizationCodeEncrypted = authorizationCodeEncrypted;
        this.accountKitUserId = accountKitUserId;
        this.countryPhoneNumber = countryPhoneNumber;
        this.countryPrefix = countryPrefix;
        this.nationalPhoneNumber = nationalPhoneNumber;
        this.userId = userId;
    }

    public long getAccountKitId() {
        return accountKitId;
    }

    public void setAccountKitId(long accountKitId) {
        this.accountKitId = accountKitId;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getUserAccessToken() {
        return userAccessToken;
    }

    public void setUserAccessToken(String userAccessToken) {
        this.userAccessToken = userAccessToken;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getAuthorizationCodeEncrypted() {
        return authorizationCodeEncrypted;
    }

    public void setAuthorizationCodeEncrypted(String authorizationCodeEncrypted) {
        this.authorizationCodeEncrypted = authorizationCodeEncrypted;
    }

    public String getAccountKitUserId() {
        return accountKitUserId;
    }

    public void setAccountKitUserId(String accountKitUserId) {
        this.accountKitUserId = accountKitUserId;
    }

    public String getCountryPhoneNumber() {
        return countryPhoneNumber;
    }

    public void setCountryPhoneNumber(String countryPhoneNumber) {
        this.countryPhoneNumber = countryPhoneNumber;
    }

    public String getCountryPrefix() {
        return countryPrefix;
    }

    public void setCountryPrefix(String countryPrefix) {
        this.countryPrefix = countryPrefix;
    }

    public String getNationalPhoneNumber() {
        return nationalPhoneNumber;
    }

    public void setNationalPhoneNumber(String nationalPhoneNumber) {
        this.nationalPhoneNumber = nationalPhoneNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AccountKit{");
        sb.append("accountKitId=").append(accountKitId);
        sb.append(", createdTimestamp=").append(createdTimestamp);
        sb.append(", userAccessToken='").append(userAccessToken).append('\'');
        sb.append(", authorizationCode='").append(authorizationCode).append('\'');
        sb.append(", authorizationCodeEncrypted='").append(authorizationCodeEncrypted).append('\'');
        sb.append(", accountKitUserId='").append(accountKitUserId).append('\'');
        sb.append(", countryPhoneNumber='").append(countryPhoneNumber).append('\'');
        sb.append(", countryPrefix='").append(countryPrefix).append('\'');
        sb.append(", nationalPhoneNumber='").append(nationalPhoneNumber).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
