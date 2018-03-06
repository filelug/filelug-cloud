package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.clopuccino.*;
import org.clopuccino.PropertyConstants;
import org.clopuccino.dao.*;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.*;
import org.clopuccino.service.*;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * <code>LoginApplyUserServlet</code> provides desktop to login the applied user of the computer.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "login-applied-user", displayName = "login-applied-user", description = "Login the applied user of a computer", urlPatterns = {"/user/loginau"})
public class LoginApplyUserServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(LoginApplyUserServlet.class.getSimpleName());

    private static final long serialVersionUID = -3715876169964233913L;

    private final UserDao userDao;

    private final LoginService loginService;

    private final CountryService countryService;

    private final ComputerDao computerDao;

    private final ApplyConnectionDao applyConnectionDao;

    private final ClientSessionService clientSessionService;

    private final UserComputerPropertiesDao userComputerPropertiesDao;

    private final DeviceTokenService deviceTokenService;

    private final UserComputerDao userComputerDao;

    public LoginApplyUserServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        loginService = new LoginService(dbAccess);

        countryService = new CountryService();

        computerDao = new ComputerDao(dbAccess);

        applyConnectionDao = new ApplyConnectionDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);

        userComputerPropertiesDao = new UserComputerPropertiesDao(dbAccess);

        deviceTokenService = new DeviceTokenService(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String adminId = clientSession.getUserId();

            String adminSessionId = clientSession.getSessionId();

            String locale = clientSession.getLocale();

            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode rootNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));
            
            JsonNode applyUserIdNode = rootNode.get(PropertyConstants.PROPERTY_NAME_APPLY_USER_ID);
            JsonNode computerIdNode = rootNode.get(PropertyConstants.PROPERTY_NAME_COMPUTER_ID);
            JsonNode recoveryKeyNode = rootNode.get(PropertyConstants.PROPERTY_NAME_RECOVERY_KEY);
            JsonNode realVerificationNode = rootNode.get(PropertyConstants.PROPERTY_NAME_VERIFICATION);
            JsonNode deviceTokenNode = rootNode.get(PropertyConstants.PROPERTY_NAME_DEVICE_TOKEN);
            JsonNode systemPropertiesNode = rootNode.get(PropertyConstants.PROPERTY_NAME_SYSTEM_PROPERTIES);

            if (applyUserIdNode == null || applyUserIdNode.textValue() == null
                || computerIdNode == null || !computerIdNode.isNumber()
                || recoveryKeyNode == null || recoveryKeyNode.textValue() == null
                || realVerificationNode == null || realVerificationNode.textValue() == null
                || deviceTokenNode == null
                || systemPropertiesNode == null) {
                String errorMessage = ClopuccinoMessages.getMessage("at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                // check if the admin id is the computer's administrator

                long computerId = computerIdNode.longValue();

                String recoveryKey = recoveryKeyNode.textValue();

                Computer computer = computerDao.findComputerById(computerId);

                if (computer == null || !computer.getUserId().equals(adminId) || !recoveryKey.equals(computer.getRecoveryKey())) {
                    String errorMessage = ClopuccinoMessages.getMessage(locale, "permission.denied.login.apply.user2");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    // make sure the applied user is approved to access the computer and not the admin of the computer

                    String applyUserId = applyUserIdNode.textValue();

                    User applyUser = userDao.findUserById(applyUserId);

                    ApplyConnection applyConnection = applyConnectionDao.findApplyConnectionByApplyUserAndComputerId(applyUserId, computerId);

                    Boolean approved = applyConnection.getApproved();

                    String foundAdminId = applyConnection.getApprovedUser();

                    if (applyUser == null || approved == null || !approved || !foundAdminId.equals(adminId) || applyUserId.equals(foundAdminId)) {
                        String nickname;

                        if (applyUser != null) {
                            nickname = applyUser.getNickname();
                        } else {
                            nickname = null;
                        }

                        String errorMessage;

                        if (nickname != null && nickname.trim().length() > 0) {
                            errorMessage = ClopuccinoMessages.getMessage(locale, "permission.denied.login.apply.user", nickname);
                        } else {
                            errorMessage = ClopuccinoMessages.getMessage(locale, "permission.denied.login.apply.user2");
                        }

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else {
                        String realVerification = realVerificationNode.textValue();

                        if (!realVerification.equals(Utility.generateVerificationToLoginApplyUser(adminId, applyUserId, computerId))) {
                            String errorMessage = ClopuccinoMessages.getMessage("Incorrect verification code");

                            LOGGER.error(String.format("[ATTACKING] %s%ncomputer id: %d%nsession id: %s%napplied user: %s%nadmin id: %s", errorMessage, computerId, adminSessionId, applyUserId, adminId));

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            resp.getWriter().write("ERROR");
                            resp.getWriter().flush();
                        } else {
                            // device token

                            String deviceTokenString = null;

                            DeviceToken deviceTokenObject = DeviceTokenService.prepareDeviceToken(deviceTokenNode, locale);

                            if (deviceTokenObject != null) {
                                // The device token object from computer contains no userId, need to add it here
                                deviceTokenObject.setAccount(applyUserId);

                                try {
                                    deviceTokenObject = deviceTokenService.createOrUploadDeviceToken(deviceTokenObject, locale);

                                    deviceTokenString = deviceTokenObject.getDeviceToken();
                                } catch (Exception e) {
                                    String message = "Error on creating or updating device token: " + deviceTokenObject;
                                    LOGGER.error(message, e);

                                    throw new Exception(message);
                                }
                            }

                            // user computer

                            String applyUserComputerId = Utility.generateUserComputerIdFrom(applyUserId, computerId);

                            UserComputer applyUserComputer = userComputerDao.findUserComputerById(applyUserComputerId);

                            String lugServerId = ConnectionDispatchService.dispatchConnectionBy(applyUserId, computerId);

                            boolean allowAlias = true;

                            if (applyUserComputer == null) {
                                // not exists, create

                                String encryptedUserComputerId = Utility.generateEncryptedUserComputerIdFrom(applyUserId, computerId);

                                String computerGroup = computer.getGroupName();

                                String computerName = computer.getComputerName();

                                // for admin user, the default value to the allow-alias is true.
                                applyUserComputer = new UserComputer(applyUserComputerId, applyUserId, computerId, adminId, computerGroup, computerName, encryptedUserComputerId, lugServerId, false, false, allowAlias);

                                userComputerDao.createUserComputer(applyUserComputer);
                            } else {
                                // already exists, update

                                Boolean existingAllowAlias = applyUserComputer.isAllowAlias();

                                if (existingAllowAlias != null) {
                                    allowAlias = existingAllowAlias;
                                }

                                applyUserComputer.setLugServerId(lugServerId);
                                applyUserComputer.setSocketConnected(false);
                                applyUserComputer.setNeedReconnect(false);

                                userComputerDao.updateUserComputerConnectionStatus(applyUserComputer);
                            }

                            // sysprops

                            Properties properties = mapper.treeToValue(systemPropertiesNode, Properties.class);

                            if (properties != null) {
                                userComputerPropertiesDao.createUserComputerProperties(applyUserComputerId, properties);
                            }

                            // login to get session id

                            String applyUserSessionId = loginService.loginWithComputer(applyUser, computerId, deviceTokenString, locale);

                            String countryId = applyUser.getCountryId();

                            int countryCode = countryService.findCountryCodeByCountryId(countryId);

                            String phoneNumber = applyUser.getPhoneNumber();

                            String phoneWithCountry = CountryService.phoneWithCountryFrom(countryCode, phoneNumber);

                            String nickname = applyUser.getNickname();

                            boolean showHidden = applyUser.getShowHidden() != null ? applyUser.getShowHidden() : false;

                            // send login result to desktop

                            ObjectNode returnNode = mapper.createObjectNode();

                            returnNode.put(PropertyConstants.PROPERTY_NAME_ACCOUNT, applyUserId);
                            returnNode.put(PropertyConstants.PROPERTY_NAME_COUNTRY_ID, countryId);
                            returnNode.put(PropertyConstants.PROPERTY_NAME_COUNTRY_CODE, Integer.valueOf(countryCode));
                            returnNode.put(PropertyConstants.PROPERTY_NAME_PHONE_NUMBER, phoneNumber);
                            returnNode.put(PropertyConstants.PROPERTY_NAME_PHONE_NUMBER_WITH_COUNTRY, phoneWithCountry);
                            returnNode.put(PropertyConstants.PROPERTY_NAME_NICKNAME, nickname);
                            returnNode.put(PropertyConstants.PROPERTY_NAME_SHOW_HIDDEN, showHidden);
                            returnNode.put(PropertyConstants.PROPERTY_NAME_SESSION_ID, applyUserSessionId);
                            returnNode.put(PropertyConstants.PROPERTY_NAME_COMPUTER_ID, computerId);
                            returnNode.put(PropertyConstants.PROPERTY_NAME_LUG_SERVER_ID, lugServerId);
                            returnNode.put(PropertyConstants.PROPERTY_NAME_ALLOW_ALIAS, allowAlias);

                            resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                            resp.setStatus(HttpServletResponse.SC_OK);
                            resp.getWriter().write(mapper.writeValueAsString(returnNode));
                            resp.getWriter().flush();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to sign in for applied user.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        }
    }
}
