package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.clopuccino.*;
import org.clopuccino.dao.AccountKitDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.AccountKit;
import org.clopuccino.domain.DeviceToken;
import org.clopuccino.domain.User;
import org.clopuccino.service.CountryService;
import org.clopuccino.service.DeviceTokenService;
import org.clopuccino.service.LoginService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * <code>LoginWithAuthorizationCodeServlet</code> gets the user access token with the authorization code.
 * If the user not exists, create the user, login, and then response session id, user id, country id and phone number.
 * If the user exists, login and response session id, user id, country id, phone number, nickname, and email(if any).
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "login-with-authorization-code", displayName = "login-with-authorization-code", description = "Login with the authorization code", urlPatterns = {"/user/loginac"})
public class LoginWithAuthorizationCodeServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(LoginWithAuthorizationCodeServlet.class.getSimpleName());

    private static final long serialVersionUID = 6667550725307064235L;

    private final UserDao userDao;

    private final AccountKitDao accountKitDao;

    private final LoginService loginService;

    private final CountryService countryService;

    public LoginWithAuthorizationCodeServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        accountKitDao = new AccountKitDao(dbAccess);

        loginService = new LoginService(dbAccess);

        countryService = new CountryService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         /* check json input */
        ObjectMapper mapper = Utility.createObjectMapper();

        try {
            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode encryptedAuthorizationCodeNode = jsonNode.get("code");

            JsonNode localeNode = jsonNode.get("locale");

            if (localeNode == null || localeNode.textValue() == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(ClopuccinoMessages.DEFAULT_LOCALE, "param.null.or.empty", "device local");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (encryptedAuthorizationCodeNode == null || encryptedAuthorizationCodeNode.textValue() == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(localeNode.textValue(), "param.null.or.empty", "authorization code");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                String encryptedAuthorizationCode = encryptedAuthorizationCodeNode.textValue();

                String locale = localeNode.textValue();

                // verify device token object if provided

                JsonNode deviceTokenObjectNode = jsonNode.get("device-token");

                DeviceToken deviceTokenObject = null;

                boolean errorOnPreparingDeviceToken = false;

                try {
                    deviceTokenObject = DeviceTokenService.prepareDeviceToken(deviceTokenObjectNode, locale);
                } catch (IllegalArgumentException e) {
                    errorOnPreparingDeviceToken = true;

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(e.getMessage());
                    resp.getWriter().flush();
                }

                if (!errorOnPreparingDeviceToken) {
                    AccountKit accountKit = accountKitDao.findAccountKitByEncryptedAuthorizationCode(encryptedAuthorizationCode);

                    if (accountKit == null) {
                        String errorMessage = ClopuccinoMessages.localizedMessage(locale, "authorization.code.not.exists.need.login");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else {
                        String countryCode = accountKit.getCountryPrefix();
                        String phoneNumber = accountKit.getNationalPhoneNumber();
                        String phoneWithCountry = accountKit.getCountryPhoneNumber();

                        try {
                            int countryCodeInt = Integer.parseInt(countryCode);

                            String countryId = countryService.findCountryIdByCountryCode(countryCodeInt, phoneWithCountry);
//                            String countryId = countryDao.findCountryIdByCountryCode(countryCodeInt);

                            if (countryId != null) {

                                /*
                                {
                                    "country-id" : "TW",
                                    "country-code" : 886,
                                    "phone" : "975009123", // 號碼不在前面加上 '0' 
                                    "phone-with-country" : "+886975009123",
                                    "account" : "9aaa3acb2bf8aa13533040cc4b24cec22089eba94b4d1c11f3a520656abdab5305b5b3f905b994ea10a24c16783fc340",
                                    "sessionId": "3420CD377BAAF1B7BEDC52C374DF4BD5F9699A0D5DFAD204BD59BCB6B3668C81",
                                    "need-create-or-update-user-profile" : true,
                                    "nickname" : "Wickie",
                                    "email" : "wickie@example.com",
                                    "email-is-verified" : false
                                }
                                */

                                User foundOrCreatedUser;

                                ObjectNode responseNode = mapper.createObjectNode();

                                List<User> users = userDao.findUsersByPhone(countryId, phoneNumber, true, true);

                                if (users != null && users.size() > 0) {
                                    // User found

                                    foundOrCreatedUser = users.get(0);

                                    String userId = foundOrCreatedUser.getAccount();

                                    String nickname = foundOrCreatedUser.getNickname();

                                    String verifiedEmail = foundOrCreatedUser.getUserEmail();

                                    String unverifiedEmail = foundOrCreatedUser.getUnverifiedUserEmail();

                                    // if both email(either unverified or verified) and nickname hava values, set PROPERTY_NAME_NEED_CREATE_OR_UPDATE_USER_PROFILE to false

                                    boolean noEmailFound = (verifiedEmail == null || verifiedEmail.length() < 1) && (unverifiedEmail == null || unverifiedEmail.length() < 1);

                                    boolean needCreateOrUpdateUserProfile = (noEmailFound || nickname == null || nickname.length() < 1);

                                    responseNode.put(PropertyConstants.PROPERTY_NAME_COUNTRY_ID, countryId);
                                    responseNode.put(PropertyConstants.PROPERTY_NAME_COUNTRY_CODE, Integer.valueOf(countryCodeInt));
                                    responseNode.put(PropertyConstants.PROPERTY_NAME_PHONE_NUMBER, phoneNumber);
                                    responseNode.put(PropertyConstants.PROPERTY_NAME_PHONE_NUMBER_WITH_COUNTRY, phoneWithCountry);
                                    responseNode.put(PropertyConstants.PROPERTY_NAME_ACCOUNT, userId);
                                    responseNode.put(PropertyConstants.PROPERTY_NAME_NEED_CREATE_OR_UPDATE_USER_PROFILE, needCreateOrUpdateUserProfile);

                                    if (nickname != null && nickname.length() > 0) {
                                        responseNode.put(PropertyConstants.PROPERTY_NAME_NICKNAME, nickname);
                                    }

                                    // First check the value of verified email, if not exists, use the unverified email

                                    if (verifiedEmail != null && verifiedEmail.length() > 0) {
                                        responseNode.put(PropertyConstants.PROPERTY_NAME_EMAIL, verifiedEmail);
                                        responseNode.put(PropertyConstants.PROPERTY_NAME_EMAIL_IS_VERIFIED, Boolean.TRUE);
                                    } else if (unverifiedEmail != null && unverifiedEmail.length() > 0) {
                                        responseNode.put(PropertyConstants.PROPERTY_NAME_EMAIL, unverifiedEmail);
                                        responseNode.put(PropertyConstants.PROPERTY_NAME_EMAIL_IS_VERIFIED, Boolean.FALSE);
                                    }

                                    // updates user as verified
                                    userDao.updateUserVerifiedById(userId, true);

                                    // Bind AccountKit with user id
                                    accountKit.setUserId(userId);
                                } else {
                                    // User not found, create new

                                    User user = new User();

                                    String userId = User.generateUniqueUserId(countryId, phoneNumber);

                                    user.setAccount(userId);
                                    user.setCountryId(countryId);
                                    user.setPhoneNumber(phoneNumber);
                                    user.setVerified(true);
                                    user.setShowHidden(false);

                                    foundOrCreatedUser = userDao.createUser(user);

                                    responseNode.put(PropertyConstants.PROPERTY_NAME_COUNTRY_ID, countryId);
                                    responseNode.put(PropertyConstants.PROPERTY_NAME_COUNTRY_CODE, Integer.valueOf(countryCodeInt));
                                    responseNode.put(PropertyConstants.PROPERTY_NAME_PHONE_NUMBER, phoneNumber);
                                    responseNode.put(PropertyConstants.PROPERTY_NAME_PHONE_NUMBER_WITH_COUNTRY, phoneWithCountry);
                                    responseNode.put(PropertyConstants.PROPERTY_NAME_ACCOUNT, userId);
                                    responseNode.put("need-create-or-update-user-profile", true);

                                    // Bind AccountKit with user id
                                    accountKit.setUserId(userId);
                                }

                                // login to get session id
                                String sessionId = loginService.createDeviceTokenAndLoginUser(foundOrCreatedUser, deviceTokenObject, null, locale);

                                responseNode.put(PropertyConstants.PROPERTY_NAME_V1_SESSION_ID, sessionId);

                                final AccountKit finalAccountKit = accountKit;

                                Utility.getExecutorService().execute(() -> {
                                    // update db account_kit, including user id and session id

                                    try {
                                        accountKitDao.updateAccountKit(finalAccountKit);
                                    } catch (Exception e) {
                                        LOGGER.error("Error on updating account kit\n" + finalAccountKit, e);
                                    }
                                });

                                resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                resp.setStatus(HttpServletResponse.SC_OK);
                                resp.getWriter().write(mapper.writeValueAsString(responseNode));
                                resp.getWriter().flush();
                            } else {
                                // country not supported - status 464

                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.setStatus(Constants.HTTP_STATUS_COUNTRY_NOT_SUPPORTED);
                                resp.getWriter().write(ClopuccinoMessages.localizedMessage(locale, "country.not.support"));
                                resp.getWriter().flush();
                            }
                        } catch (NumberFormatException e) {
                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            resp.getWriter().write(String.format("Country code '%s' is not a integer.", countryCode));
                            resp.getWriter().flush();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on verifying authorization code", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        }
    }
}
