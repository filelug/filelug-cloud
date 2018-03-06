package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.clopuccino.*;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.DeviceToken;
import org.clopuccino.domain.User;
import org.clopuccino.service.ClientSessionService;
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

/**
 * <code>LoginWithSessionIdServlet</code> takes an valid or invalid session id and responses new user session id.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "login-with-session-id", displayName = "login-with-session-id", description = "Exchange an old session id with a new session id", urlPatterns = {"/user/loginse"})
public class LoginWithSessionIdServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(LoginWithSessionIdServlet.class.getSimpleName());

    private static final long serialVersionUID = 4221577994949874785L;

    private final UserDao userDao;

    private final ClientSessionService clientSessionService;

    private final LoginService loginService;

    private final CountryService countryService;

    public LoginWithSessionIdServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);

        loginService = new LoginService(dbAccess);

        countryService = new CountryService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         /* check json input */
        ObjectMapper mapper = Utility.createObjectMapper();

        try {
            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode oldSessionIdNode = jsonNode.get("sessionId");

            JsonNode verificationNode = jsonNode.get("verification");

            JsonNode localeNode = jsonNode.get("locale");

            if (localeNode == null || localeNode.textValue() == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(ClopuccinoMessages.DEFAULT_LOCALE, "param.null.or.empty", "device local");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (oldSessionIdNode == null || oldSessionIdNode.textValue() == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(localeNode.textValue(), "param.null.or.empty", "session id");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (verificationNode == null || verificationNode.textValue() == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(localeNode.textValue(), "param.null.or.empty", "verification code");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                String oldSessionId = oldSessionIdNode.textValue();

                String locale = localeNode.textValue();

                // make sure the session id exists and is valid

                ClientSession clientSession = clientSessionService.findClientSessionBySessionId(oldSessionId);

                if (clientSession == null) {
                    // session not found

                    String errorMessage = ClopuccinoMessages.localizedMessage(locale, "session.not.exists");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
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
                        String userId = clientSession.getUserId();

                        User user = userDao.findUserById(userId);

                        if (user != null) {
                            // User found

                            // Make sure the verification is identical, the phone number must trim the starting '0', if any.

                            String countryId = user.getCountryId();

                            String phoneNumber = user.getPhoneNumber();

                            if (phoneNumber.startsWith("0")) {
                                phoneNumber = phoneNumber.substring(1);
                            }

                            String expectedVerification = Utility.generateVerificationForSecurityCode(userId, countryId, phoneNumber);

                            if (!expectedVerification.equals(verificationNode.textValue())) {
                                LOGGER.warn("Be careful that user: " + userId + "(" + user.getNickname() + ") is trying to hack the service login-with-session-id");

                                String errorMessage = ClopuccinoMessages.localizedMessage(locale, "error");

                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                resp.getWriter().write(String.valueOf(errorMessage));
                                resp.getWriter().flush();
                            } else {
                                int countryCodeInt = countryService.findCountryCodeByCountryId(countryId);

                                String phoneWithCountry = CountryService.phoneWithCountryFrom(countryCodeInt, phoneNumber);

                                String nickname = user.getNickname();

                                String verifiedEmail = user.getUserEmail();

                                String unverifiedEmail = user.getUnverifiedUserEmail();

                                // if both email(either unverified or verified) and nickname hava values, set PROPERTY_NAME_NEED_CREATE_OR_UPDATE_USER_PROFILE to false

                                boolean noEmailFound = (verifiedEmail == null || verifiedEmail.length() < 1) && (unverifiedEmail == null || unverifiedEmail.length() < 1);

                                boolean needCreateOrUpdateUserProfile = (noEmailFound || nickname == null || nickname.length() < 1);

                                // If the original session already binds a computer, the computer should be set to the new session.
                                Long computerId = clientSession.getComputerId();

                                // login to get session id
                                String newSessionId = loginService.createDeviceTokenAndLoginUser(user, deviceTokenObject, computerId, locale);

                                ObjectNode responseNode = mapper.createObjectNode();

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

                                responseNode.put("oldSessionId", oldSessionId);
                                responseNode.put("newSessionId", newSessionId);

                                String responseString = mapper.writeValueAsString(responseNode);

                                resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                resp.setStatus(HttpServletResponse.SC_OK);
                                resp.getWriter().write(responseString);

                                try {
                                    resp.getWriter().flush();
                                } finally {
                                    // set the old session is replaced by the new one
                                    clientSessionService.updateClientSessionReplacedBy(newSessionId, oldSessionId);
                                }
                            }

                        } else {
                            // User not found

                            String errorMessage = ClopuccinoMessages.localizedMessage(locale, "user.not.found");

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            resp.getWriter().write(errorMessage);
                            resp.getWriter().flush();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on user login with session id", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        }
    }
}
