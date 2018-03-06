package org.clopuccino.service;

import ch.qos.logback.classic.Logger;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.clopuccino.Constants;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>FacebookService</code> provides Facebook-related services
 *
 * @author masonhsieh
 * @version 1.0
 */
public class FacebookAccountKitService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FacebookAccountKitService.class.getSimpleName());

    private String appId;

    private String version;

    private String appSecret;

    // based url to Facebook services, end with slash ('/')
    private String baseUrl;

    private String appAccessToken;

    private BaseService baseService;

    public FacebookAccountKitService() {
        this.appId = Constants.FACEBOOK_FILELUG_APP_ID;
        this.version = Constants.FACEBOOK_ACCOUNT_KIT_API_VERSION;
        this.appSecret = Constants.FACEBOOK_ACCOUNT_KIT_APP_SECRETE;

        this.baseUrl = String.format("https://graph.accountkit.com/%s/", this.version);

        this.appAccessToken = String.format("AA|%s|%s", this.appId, this.appSecret);

        this.baseService = new BaseService();

//        this.crossSiteRequestForgery = UUID.randomUUID().toString();
//
//        this.accountKitMeUrl = String.format("https://graph.accountkit.com/%s/me", this.version);
//
//        this.accountKitTokenExchangeUrl = String.format("https://graph.accountkit.com/%s/access_token", this.version);
    }

    public String getAppId() {
        return appId;
    }

    public String getVersion() {
        return version;
    }

    public String getAppSecret() {
        return appSecret;
    }



    /**
     * Get the user access token with the authorization code.
     *
     * Request:<br>
     *     GET https://graph.accountkit.com/v1.0/access_token?grant_type=authorization_code&code={authorization_code}&access_token=AA|{facebook_app_id}|{<app_secret>}
     * <br>
     * Response json format:<br>
     * <code>
     * {
     *  "id" : account_kit_user_id,
     *  "access_token" : account_access_token,
     *  "token_refresh_interval_sec" : refresh_interval
     * }
     * </code>
     * @param authorizationCode The authorization code
     */
    public HttpResponse exchangeUserAccessTokenWithAuthorizationCode(String authorizationCode) throws Exception {
        List<NameValuePair> params = new ArrayList<>();

        params.add(new BasicNameValuePair("grant_type", "authorization_code"));
        params.add(new BasicNameValuePair("code", authorizationCode));
        params.add(new BasicNameValuePair("access_token", this.appAccessToken));

        return this.baseService.doGet(this.baseUrl, "access_token", null, params, null);
    }

    /**
     * Get the user data with the user access token.
     *
     * Request:<br>
     *     GET https://graph.accountkit.com/v1.0/me/?access_token=<access_token>
     * Response json format:<br>
     * <code>
     * {
     *  "id":"12345",
     *  "phone":
     *  {
     *      "number":"+15551234567",
     *      "country_prefix": "1",
     *      "national_number": "5551234567"
     *  }
     * }
     * </code>
     * @param userAccessToken The user access token
     */
    public HttpResponse findUserWithUserAccessToken(String userAccessToken) throws Exception {
        List<NameValuePair> params = new ArrayList<>();

        params.add(new BasicNameValuePair("access_token", userAccessToken));
        params.add(new BasicNameValuePair("appsecret_proof", generateAppSecretProof(userAccessToken)));

        return this.baseService.doGet(this.baseUrl, "me", null, params, null);
    }

    private String generateAppSecretProof(String userAccessToken) {

        String result;

        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");

            SecretKeySpec secret_key = new SecretKeySpec(this.appSecret.getBytes("UTF-8"), "HmacSHA256");

            sha256_HMAC.init(secret_key);

            result = Hex.encodeHexString(sha256_HMAC.doFinal(userAccessToken.getBytes("UTF-8")));
        } catch (Exception e) {
            result = null;

            LOGGER.error("Error on generating app scret proof with user access token: " + userAccessToken, e);
        }

        return result;

//        return DigestUtils.sha256Hex(userAccessToken + this.appSecret);
    }

}
