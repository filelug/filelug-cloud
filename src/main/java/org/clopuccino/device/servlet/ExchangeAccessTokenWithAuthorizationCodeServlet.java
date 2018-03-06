package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;
import org.clopuccino.*;
import org.clopuccino.dao.AccountKitDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.AccountKit;
import org.clopuccino.service.CountryService;
import org.clopuccino.service.FacebookAccountKitService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * <code>ExchangeAccessTokenWithAuthorizationCodeServlet</code> gets the user access token with the authorization code,
 * and get the user phone and country code by using the access token.
 * <br>
 * No user will be created or updated for this service.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "exchange-access-token-with-authorization-code", displayName = "exchange-access-token-with-authorization-code", description = "Exchange access token with the authorization code", urlPatterns = {"/user/tokenac"})
public class ExchangeAccessTokenWithAuthorizationCodeServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ExchangeAccessTokenWithAuthorizationCodeServlet.class.getSimpleName());

    private static final long serialVersionUID = 287889970285418178L;

    private final AccountKitDao accountKitDao;

    private final FacebookAccountKitService facebookAccountKitService;

    private final CountryService countryService;

    public ExchangeAccessTokenWithAuthorizationCodeServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        accountKitDao = new AccountKitDao(dbAccess);

        facebookAccountKitService = new FacebookAccountKitService();

        countryService = new CountryService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         /* check json input */
        ObjectMapper mapper = Utility.createObjectMapper();

        try {
            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode authorizationCodeNode = jsonNode.get("code");

            JsonNode localeNode = jsonNode.get("locale");

            JsonNode verificationNode = jsonNode.get("verification");

            if (localeNode == null || localeNode.textValue() == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(ClopuccinoMessages.DEFAULT_LOCALE, "param.null.or.empty", "device local");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (authorizationCodeNode == null || authorizationCodeNode.textValue() == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(localeNode.textValue(), "param.null.or.empty", "authorization code");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (verificationNode == null || verificationNode.textValue() == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(localeNode.textValue(), "param.null.or.empty", "verify code");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                String authorizationCode = authorizationCodeNode.textValue();

                String locale = localeNode.textValue();

                String verification = verificationNode.textValue();

                String expectedVerification = Utility.generateVerificationForExchangeAccessToken(authorizationCode, locale);


                if (!verification.equals(expectedVerification)) {
                    String message = String.format("Someone is trying to access the service with incorrect verification: '%s'\nauthorization code: '%s'\nlocale: '%s'", verification, authorizationCode, locale);

                    LOGGER.warn(message);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("ERROR");
                    resp.getWriter().flush();
                } else {
                    HttpResponse accessTokenResponse = facebookAccountKitService.exchangeUserAccessTokenWithAuthorizationCode(authorizationCode);

                    StatusLine accessTokenResponseStatusLine = accessTokenResponse.getStatusLine();

                    int responseStatus = 0;
                    String responseString = null;

                    // response json from facebook account kit api even if response status is not 200
                    String accessTokenResponseString = EntityUtils.toString(accessTokenResponse.getEntity(), Charset.forName("UTF-8"));

                    if (accessTokenResponseStatusLine != null && accessTokenResponseStatusLine.getStatusCode() == HttpServletResponse.SC_OK) {
                        /* format of accessTokenResponseString:
                         * {
                         *  "id" : account_kit_user_id,
                         *  "access_token" : account_access_token,
                         *  "token_refresh_interval_sec" : refresh_interval
                         * }
                         */

                        HashMap<String, Object> accessTokenResponseMap = mapper.readValue(accessTokenResponseString, new TypeReference<HashMap<String, Object>>() {
                        });

                        Object userAccessToken = accessTokenResponseMap.get("access_token");

                        if (userAccessToken != null && String.class.isInstance(userAccessToken)) {
                            // Get phone number from user access token

                            HttpResponse phoneNumberResponse = facebookAccountKitService.findUserWithUserAccessToken((String) userAccessToken);

                            StatusLine phoneNumberResponseStatusLine = phoneNumberResponse.getStatusLine();

                            // response json from facebook account kit api even if response status is not 200
                            String phoneNumberResponseString = EntityUtils.toString(phoneNumberResponse.getEntity(), Charset.forName("UTF-8"));

                            if (phoneNumberResponseStatusLine != null && phoneNumberResponseStatusLine.getStatusCode() == HttpServletResponse.SC_OK) {
                                /* format of phoneNumberResponseString:
                                 * {
                                 *  "id":"12345",
                                 *  "phone":
                                 *  {
                                 *      "number":"+15551234567",
                                 *      "country_prefix": "1",
                                 *      "national_number": "5551234567"
                                 *  }
                                 * }
                                 */

                                // save authorization code and user access token string to db account_kit

                                AccountKit accountKit = accountKitDao.findAccountKitByAuthorizationCode(authorizationCode);

                                if (accountKit == null) {
                                    accountKit = new AccountKit();

                                    accountKit.setCreatedTimestamp(System.currentTimeMillis());
                                    accountKit.setUserAccessToken((String) userAccessToken);
                                    accountKit.setAuthorizationCode(authorizationCode);

                                    // the encypted authorization code will be generated on creating in DAO

                                    accountKit = accountKitDao.createAccountKit(accountKit);
                                } else {
                                    // update if the AccountKit with the authorization code already exists

                                    accountKit.setUserAccessToken((String) userAccessToken);

                                    accountKitDao.updateAccountKit(accountKit);
                                }

                                HashMap<String, Object> phoneNumberResponseMap = mapper.readValue(phoneNumberResponseString, new TypeReference<HashMap<String, Object>>() {
                                });

                                Object accountKitUserId = phoneNumberResponseMap.get("id");

                                Object phoneNumberSubMap = phoneNumberResponseMap.get("phone");

                                if (phoneNumberSubMap != null && Map.class.isInstance(phoneNumberSubMap)) {
                                    Object countryPhoneNumber = ((Map) phoneNumberSubMap).get("number");
                                    String countryCode = (String) ((Map) phoneNumberSubMap).get("country_prefix");
                                    String phoneNumberWithoutZeroPrefix = (String) ((Map) phoneNumberSubMap).get("national_number");

                                    if (countryCode != null && phoneNumberWithoutZeroPrefix != null) {
                                        int countryCodeInt = 0;
                                        try {
                                            countryCodeInt = Integer.parseInt(countryCode);
                                        } catch (Exception e) {
                                            responseStatus = HttpServletResponse.SC_BAD_REQUEST;
                                            responseString = String.format("Country code '%s' is not a integer.", countryCode);
                                        }

                                        if (countryCodeInt > 0) {
                                            String phoneWithCountry;

                                            if (countryPhoneNumber == null || !String.class.isInstance(countryPhoneNumber)) {
                                                phoneWithCountry = CountryService.phoneWithCountryFrom(countryCodeInt, phoneNumberWithoutZeroPrefix);
                                            } else {
                                                phoneWithCountry = (String) countryPhoneNumber;
                                            }

                                            String countryId = countryService.findCountryIdByCountryCode(countryCodeInt, phoneWithCountry);
//                                            String countryId = countryDao.findCountryIdByCountryCode(countryCodeInt);

                                            if (countryId != null) {
                                                // Update phone number info to db account_kit with pk: userAccessToken

                                                if (accountKitUserId != null && String.class.isInstance(accountKitUserId)) {
                                                    accountKit.setAccountKitUserId((String) accountKitUserId);
                                                }

                                                accountKit.setCountryPhoneNumber(phoneWithCountry);

                                                accountKit.setCountryPrefix(countryCode);

                                                accountKit.setNationalPhoneNumber(phoneNumberWithoutZeroPrefix);

                                                final AccountKit finalAccountKit = accountKit;

                                                Utility.getExecutorService().execute(() -> {
                                                    // update db account_kit

                                                    try {
                                                        accountKitDao.updateAccountKit(finalAccountKit);
                                                    } catch (Exception e) {
                                                        LOGGER.error("Error on updating account kit\n" + finalAccountKit, e);
                                                    }
                                                });

                                                /*
                                                { 
                                                    "country-id" : "TW", 
                                                    "country-code" : 886, 
                                                    "phone" : "975009123", 
                                                    "phone-with-country" : "+886975009123", 
                                                    "verification" : "QWR98B4UBS9O7V2M0"
                                                 }
                                                */

                                                ObjectNode responseNode = mapper.createObjectNode();

                                                responseNode.put(PropertyConstants.PROPERTY_NAME_COUNTRY_ID, countryId);
                                                responseNode.put(PropertyConstants.PROPERTY_NAME_COUNTRY_CODE, Integer.valueOf(countryCodeInt));
                                                responseNode.put(PropertyConstants.PROPERTY_NAME_PHONE_NUMBER, phoneNumberWithoutZeroPrefix);
                                                responseNode.put(PropertyConstants.PROPERTY_NAME_PHONE_NUMBER_WITH_COUNTRY, phoneWithCountry);

                                                String verificationToResponse = Utility.generateVerificationForSecurityCode(countryId, phoneNumberWithoutZeroPrefix);

                                                responseNode.put(PropertyConstants.PROPERTY_NAME_VERIFICATION, verificationToResponse);

                                                responseStatus = HttpServletResponse.SC_OK;
                                                responseString = mapper.writeValueAsString(responseNode);
                                            } else {
                                                // country not supported - status 464

                                                responseStatus = Constants.HTTP_STATUS_COUNTRY_NOT_SUPPORTED;
                                                responseString = ClopuccinoMessages.localizedMessage(locale, "country.not.support2", countryCode);
                                            }
                                        }
                                    } else {
                                        // Incorrect formate of Facebook Account Kit service response to get the phone number information from user access token

                                        LOGGER.error(String.format("Incorrect formate of Facebook Account Kit service response to get the phone number information from user access token.\nAuthorization code: '%s'\nUser access token: '%s'\nCountry code: '%s'\nPhone number: '%s'", authorizationCode, userAccessToken, countryCode, phoneNumberWithoutZeroPrefix));

                                        responseStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                                        responseString = "Incorrect format of phone number data";
                                    }
                                }
                            } else {
                                if (phoneNumberResponseStatusLine != null) {
                                    LOGGER.error(String.format("Error on requesting Facebook Account Kit service to get the phone number information from user access token.\nAuthorization code: '%s'\nUser access token: '%s'\nresponse status: '%d'\nresponse body:\n%s", authorizationCode, userAccessToken, phoneNumberResponseStatusLine.getStatusCode(), phoneNumberResponseString));
                                } else {
                                    LOGGER.error(String.format("Error on requesting Facebook Account Kit service to get the phone number information from user access token.\nAuthorization code: '%s'\nUser access token: '%s'\nresponse body:\n%s", authorizationCode, userAccessToken, phoneNumberResponseString));
                                }

                                responseStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                                responseString = "Error on getting phone number data";
                            }
                        } else {
                            LOGGER.error(String.format("Error on requesting Facebook Account Kit service to get the user access token with authorization code.\nAuthorization code: '%s'\nUser access token response map: '%s'", authorizationCode, accessTokenResponseMap));

                            responseStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                            responseString = "Error on getting user access token";
                        }
                    } else {
                        if (accessTokenResponseStatusLine != null) {
                            LOGGER.error(String.format("Error on requesting Facebook Account Kit service to get the user access token with authorization code.\nAuthorization code: '%s'\nresponse status: '%d'\nresponse body:\n%s", authorizationCode, accessTokenResponseStatusLine.getStatusCode(), accessTokenResponseString));
                        } else {
                            LOGGER.error(String.format("Error on requesting Facebook Account Kit service to get the user access token with authorization code.\nAuthorization code: '%s'\nresponse body:\n%s", authorizationCode, accessTokenResponseString));
                        }

                        responseStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                        responseString = "Error on getting user access token";
                    }

                    resp.setContentType(responseStatus == HttpServletResponse.SC_OK ? Constants.CONTENT_TYPE_JSON_UTF8 : Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(responseStatus != 0 ? responseStatus : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(responseString != null ? responseString : "");
                    resp.getWriter().flush();
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

//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//         /* check json input */
//        ObjectMapper mapper = Utility.createObjectMapper();
//
//        try {
//            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));
//
//            JsonNode authorizationCodeNode = jsonNode.get("code");
//
//            JsonNode localeNode = jsonNode.get("locale");
//
//            JsonNode verificationNode = jsonNode.get("verification");
//
//            if (localeNode == null || localeNode.textValue() == null) {
//                String errorMessage = ClopuccinoMessages.localizedMessage(ClopuccinoMessages.DEFAULT_LOCALE, "param.null.or.empty", "device local");
//
//                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                resp.getWriter().write(errorMessage);
//                resp.getWriter().flush();
//            } else if (authorizationCodeNode == null || authorizationCodeNode.textValue() == null) {
//                String errorMessage = ClopuccinoMessages.localizedMessage(localeNode.textValue(), "param.null.or.empty", "authorization code");
//
//                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                resp.getWriter().write(errorMessage);
//                resp.getWriter().flush();
//            } else if (verificationNode == null || verificationNode.textValue() == null) {
//                String errorMessage = ClopuccinoMessages.localizedMessage(localeNode.textValue(), "param.null.or.empty", "verify code");
//
//                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                resp.getWriter().write(errorMessage);
//                resp.getWriter().flush();
//            } else {
//                String authorizationCode = authorizationCodeNode.textValue();
//
//                String locale = localeNode.textValue();
//
//                String verification = verificationNode.textValue();
//
//                String expectedVerification = Utility.generateVerificationForExchangeAccessToken(authorizationCode, locale);
//
//
//                if (!verification.equals(expectedVerification)) {
//                    String message = String.format("Someone is trying to access the service with incorrect verification: '%s'\nauthorization code: '%s'\nlocale: '%s'", verification, authorizationCode, locale);
//
//                    LOGGER.warn(message);
//
//                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                    resp.getWriter().write("ERROR");
//                    resp.getWriter().flush();
//                } else {
//                    HttpResponse accessTokenResponse = facebookAccountKitService.exchangeUserAccessTokenWithAuthorizationCode(authorizationCode);
//
//                    StatusLine accessTokenResponseStatusLine = accessTokenResponse.getStatusLine();
//
//                    int responseStatus = 0;
//                    String responseString = null;
//
//                    // response json from facebook account kit api even if response status is not 200
//                    String accessTokenResponseString = EntityUtils.toString(accessTokenResponse.getEntity(), Charset.forName("UTF-8"));
//
//                    if (accessTokenResponseStatusLine != null && accessTokenResponseStatusLine.getStatusCode() == HttpServletResponse.SC_OK) {
//                        /* format of accessTokenResponseString:
//                         * {
//                         *  "id" : account_kit_user_id,
//                         *  "access_token" : account_access_token,
//                         *  "token_refresh_interval_sec" : refresh_interval
//                         * }
//                         */
//
//                        HashMap<String, Object> accessTokenResponseMap = mapper.readValue(accessTokenResponseString, new TypeReference<HashMap<String, Object>>() {
//                        });
//
//                        Object userAccessToken = accessTokenResponseMap.get("access_token");
//
//                        if (userAccessToken != null && String.class.isInstance(userAccessToken)) {
//                            // Get phone number from user access token
//
//                            HttpResponse phoneNumberResponse = facebookAccountKitService.findUserWithUserAccessToken((String) userAccessToken);
//
//                            StatusLine phoneNumberResponseStatusLine = phoneNumberResponse.getStatusLine();
//
//                            // response json from facebook account kit api even if response status is not 200
//                            String phoneNumberResponseString = EntityUtils.toString(phoneNumberResponse.getEntity(), Charset.forName("UTF-8"));
//
//                            if (phoneNumberResponseStatusLine != null && phoneNumberResponseStatusLine.getStatusCode() == HttpServletResponse.SC_OK) {
//                                /* format of phoneNumberResponseString:
//                                 * {
//                                 *  "id":"12345",
//                                 *  "phone":
//                                 *  {
//                                 *      "number":"+15551234567",
//                                 *      "country_prefix": "1",
//                                 *      "national_number": "5551234567"
//                                 *  }
//                                 * }
//                                 */
//
//                                // save authorization code and user access token string to db account_kit
//
//                                AccountKit accountKit = accountKitDao.findAccountKitByAuthorizationCode(authorizationCode);
//
//                                if (accountKit == null) {
//                                    accountKit = new AccountKit();
//
//                                    accountKit.setCreatedTimestamp(System.currentTimeMillis());
//                                    accountKit.setUserAccessToken((String) userAccessToken);
//                                    accountKit.setAuthorizationCode(authorizationCode);
//
//                                    // the encypted authorization code will be generated on creating in DAO
//
//                                    accountKit = accountKitDao.createAccountKit(accountKit);
//                                } else {
//                                    // update if the AccountKit with the authorization code already exists
//
//                                    accountKit.setUserAccessToken((String) userAccessToken);
//
//                                    accountKitDao.updateAccountKit(accountKit);
//                                }
//
//                                HashMap<String, Object> phoneNumberResponseMap = mapper.readValue(phoneNumberResponseString, new TypeReference<HashMap<String, Object>>() {
//                                });
//
//                                Object accountKitUserId = phoneNumberResponseMap.get("id");
//
//                                Object phoneNumberSubMap = phoneNumberResponseMap.get("phone");
//
//                                if (phoneNumberSubMap != null && Map.class.isInstance(phoneNumberSubMap)) {
//                                    Object countryPhoneNumber = ((Map) phoneNumberSubMap).get("number");
//                                    String countryCode = (String) ((Map) phoneNumberSubMap).get("country_prefix");
//                                    String phoneNumberWithoutZeroPrefix = (String) ((Map) phoneNumberSubMap).get("national_number");
//
//                                    if (countryCode != null && phoneNumberWithoutZeroPrefix != null) {
//                                        int countryCodeInt = 0;
//                                        try {
//                                            countryCodeInt = Integer.parseInt(countryCode);
//                                        } catch (Exception e) {
//                                            responseStatus = HttpServletResponse.SC_BAD_REQUEST;
//                                            responseString = String.format("Country code '%s' is not a integer.", countryCode);
//                                        }
//
//                                        if (countryCodeInt > 0) {
//                                            String countryId = countryService.findCountryIdByCountryCode(countryCodeInt, accountKit.getCountryPhoneNumber());
////                                            String countryId = countryDao.findCountryIdByCountryCode(countryCodeInt);
//
//                                            if (countryId != null) {
//                                                // Update phone number info to db account_kit with pk: userAccessToken
//
//                                                if (accountKitUserId != null && String.class.isInstance(accountKitUserId)) {
//                                                    accountKit.setAccountKitUserId((String) accountKitUserId);
//                                                }
//
//                                                String phoneWithCountry;
//
//                                                if (countryPhoneNumber == null || !String.class.isInstance(countryPhoneNumber)) {
//                                                    phoneWithCountry = CountryService.phoneWithCountryFrom(countryCodeInt, phoneNumberWithoutZeroPrefix);
//                                                } else {
//                                                    phoneWithCountry = (String) countryPhoneNumber;
//                                                }
//
//                                                accountKit.setCountryPhoneNumber(phoneWithCountry);
//
//                                                accountKit.setCountryPrefix(countryCode);
//
//                                                accountKit.setNationalPhoneNumber(phoneNumberWithoutZeroPrefix);
//
//                                                final AccountKit finalAccountKit = accountKit;
//
//                                                Utility.getExecutorService().execute(() -> {
//                                                    // update db account_kit
//
//                                                    try {
//                                                        accountKitDao.updateAccountKit(finalAccountKit);
//                                                    } catch (Exception e) {
//                                                        LOGGER.error("Error on updating account kit\n" + finalAccountKit, e);
//                                                    }
//                                                });
//
//                                                /*
//                                                { 
//                                                    "country-id" : "TW", 
//                                                    "country-code" : 886, 
//                                                    "phone" : "975009123", 
//                                                    "phone-with-country" : "+886975009123", 
//                                                    "verification" : "QWR98B4UBS9O7V2M0"
//                                                 }
//                                                */
//
//                                                ObjectNode responseNode = mapper.createObjectNode();
//
//                                                responseNode.put(PropertyConstants.PROPERTY_NAME_COUNTRY_ID, countryId);
//                                                responseNode.put(PropertyConstants.PROPERTY_NAME_COUNTRY_CODE, Integer.valueOf(countryCodeInt));
//                                                responseNode.put(PropertyConstants.PROPERTY_NAME_PHONE_NUMBER, phoneNumberWithoutZeroPrefix);
//                                                responseNode.put(PropertyConstants.PROPERTY_NAME_PHONE_NUMBER_WITH_COUNTRY, phoneWithCountry);
//
//                                                String verificationToResponse = Utility.generateVerificationForSecurityCode(countryId, phoneNumberWithoutZeroPrefix);
//
//                                                responseNode.put(PropertyConstants.PROPERTY_NAME_VERIFICATION, verificationToResponse);
//
//                                                responseStatus = HttpServletResponse.SC_OK;
//                                                responseString = mapper.writeValueAsString(responseNode);
//                                            } else {
//                                                // country not supported - status 464
//
//                                                responseStatus = Constants.HTTP_STATUS_COUNTRY_NOT_SUPPORTED;
//                                                responseString = ClopuccinoMessages.localizedMessage(locale, "country.not.support2", countryCode);
//                                            }
//                                        }
//                                    } else {
//                                        // Incorrect formate of Facebook Account Kit service response to get the phone number information from user access token
//
//                                        LOGGER.error(String.format("Incorrect formate of Facebook Account Kit service response to get the phone number information from user access token.\nAuthorization code: '%s'\nUser access token: '%s'\nCountry code: '%s'\nPhone number: '%s'", authorizationCode, userAccessToken, countryCode, phoneNumberWithoutZeroPrefix));
//
//                                        responseStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
//                                        responseString = "Incorrect format of phone number data";
//                                    }
//                                }
//                            } else {
//                                if (phoneNumberResponseStatusLine != null) {
//                                    LOGGER.error(String.format("Error on requesting Facebook Account Kit service to get the phone number information from user access token.\nAuthorization code: '%s'\nUser access token: '%s'\nresponse status: '%d'\nresponse body:\n%s", authorizationCode, userAccessToken, phoneNumberResponseStatusLine.getStatusCode(), phoneNumberResponseString));
//                                } else {
//                                    LOGGER.error(String.format("Error on requesting Facebook Account Kit service to get the phone number information from user access token.\nAuthorization code: '%s'\nUser access token: '%s'\nresponse body:\n%s", authorizationCode, userAccessToken, phoneNumberResponseString));
//                                }
//
//                                responseStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
//                                responseString = "Error on getting phone number data";
//                            }
//                        } else {
//                            LOGGER.error(String.format("Error on requesting Facebook Account Kit service to get the user access token with authorization code.\nAuthorization code: '%s'\nUser access token response map: '%s'", authorizationCode, accessTokenResponseMap));
//
//                            responseStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
//                            responseString = "Error on getting user access token";
//                        }
//                    } else {
//                        if (accessTokenResponseStatusLine != null) {
//                            LOGGER.error(String.format("Error on requesting Facebook Account Kit service to get the user access token with authorization code.\nAuthorization code: '%s'\nresponse status: '%d'\nresponse body:\n%s", authorizationCode, accessTokenResponseStatusLine.getStatusCode(), accessTokenResponseString));
//                        } else {
//                            LOGGER.error(String.format("Error on requesting Facebook Account Kit service to get the user access token with authorization code.\nAuthorization code: '%s'\nresponse body:\n%s", authorizationCode, accessTokenResponseString));
//                        }
//
//                        responseStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
//                        responseString = "Error on getting user access token";
//                    }
//
//                    resp.setContentType(responseStatus == HttpServletResponse.SC_OK ? Constants.CONTENT_TYPE_JSON_UTF8 : Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//                    resp.setStatus(responseStatus != 0 ? responseStatus : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                    resp.getWriter().write(responseString != null ? responseString : "");
//                    resp.getWriter().flush();
//                }
//            }
//        } catch (Exception e) {
//            LOGGER.error("Error on verifying authorization code", e);
//
//            String errorMessage = e.getMessage();
//
//            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            resp.getWriter().write(errorMessage);
//            resp.getWriter().flush();
//        }
//    }
}
