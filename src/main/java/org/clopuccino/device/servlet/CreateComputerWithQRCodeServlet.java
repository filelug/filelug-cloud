package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.*;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.*;
import org.clopuccino.server.servlet.websocket.LoginWithQRCodeEndpoint;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.service.ConnectionDispatchService;
import org.clopuccino.service.DeviceTokenService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * <code>CreateComputerWithQRCodeServlet</code> finds the endpoint connected to the desktop with the specified QR code.
 * Get the information from the endpoint to creates a new computer, a user computer object, device token object describing the desktop,
 * and save the system properties of the desktop. At last, it responses with the computer data and the lug server id.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "create-computer-with-qrcode", displayName = "create-computer-with-qrcode", description = "Create new computer with QR code", urlPatterns = {"/computer/create-with-qrcode"})
public class CreateComputerWithQRCodeServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CreateComputerWithQRCodeServlet.class.getSimpleName());

    private static final long serialVersionUID = -1773777258815934071L;

    private final UserDao userDao;

    private final ComputerDao computerDao;

    private final ClientSessionService clientSessionService;

    private final UserComputerDao userComputerDao;

    private final UserComputerPropertiesDao userComputerPropertiesDao;

    private final DeviceTokenService deviceTokenService;

    private final ApplyConnectionDao applyConnectionDao;

    public CreateComputerWithQRCodeServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        userComputerPropertiesDao = new UserComputerPropertiesDao(dbAccess);

        deviceTokenService = new DeviceTokenService(dbAccess);

        applyConnectionDao = new ApplyConnectionDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = Utility.createObjectMapper();

        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String locale = clientSession.getLocale();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode qrCodeNode = jsonNode.get("qr-code");

            if (qrCodeNode == null || qrCodeNode.textValue() == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(locale, "param.null.or.empty", "QR code");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                String qrCode = qrCodeNode.textValue();

                String userId = clientSession.getUserId();

                User user = userDao.findUserById(userId);

                if (user != null) {
                    // Get endpoint to the computer

                    LoginWithQRCodeEndpoint computerEndpoint = LoginWithQRCodeEndpoint.getInstance(qrCode);

                    if (computerEndpoint == null || !computerEndpoint.isOpen()) {
                        String errorMessage = ClopuccinoMessages.localizedMessage(locale, "desktop.session.not.open");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else {
                        DeviceToken deviceTokenObject = computerEndpoint.getDeviceToken();

                        Properties desktopProperties = computerEndpoint.getSystemProperties();

                        // computer

                        Long computerId = computerEndpoint.getComputerId();
                        String recoveryKey = computerEndpoint.getRecoveryKey();
                        String computerGroup = computerEndpoint.getComputerGroup();
                        String computerName = computerEndpoint.getComputerName();

                        if (computerId != null && recoveryKey != null && recoveryKey.trim().length() > 0) {
                            // find computer by id and assign computer group and name
                            Computer computer = computerDao.findComputerById(computerId);

                            if (computer != null) {
                                computerGroup = computer.getGroupName();
                                computerName = computer.getComputerName();

                                // The service could be invoked after all users of the computer removed from accessing this computer.
                                // The creator of the computer may be different than the current one, so the user must be updated as
                                // the new administrator of the computer

                                String oldUserId = computer.getUserId();

                                if (!userId.equals(oldUserId)) {
                                    computer.setUserId(userId);
                                    computerDao.updateComputer(computer);
                                    
                                    LOGGER.warn(String.format("Change the administrator of the computer from '%s' to '%s'", oldUserId, userId));
                                }
                            }
                        } else {
                            // create computer with user id, computer group and computer name

                            // If the computer name already exists for the same user, replaced by a new, unique one
                            computerName = prepareUniqueComputerNameForUser(userId, computerName);

                            Computer newComputer = computerDao.createComputer(computerGroup, computerName, null, userId);

                            // create user computer

                            computerId = newComputer.getComputerId();
                            recoveryKey = newComputer.getRecoveryKey();
                        }

                        // user computer

                        String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

                        UserComputer userComputer = userComputerDao.findUserComputerById(userComputerId);

                        String lugServerId = ConnectionDispatchService.dispatchConnectionBy(userId, computerId);

                        if (userComputer == null) {
                            // not exists, create

                            String encryptedUserComputerId = Utility.generateEncryptedUserComputerIdFrom(userId, computerId);

                            // for admin user, the default value to the allow-alias is true.
                            userComputer = new UserComputer(userComputerId, userId, computerId, userId, computerGroup, computerName, encryptedUserComputerId, lugServerId, false, false, true);

                            userComputer = userComputerDao.createUserComputer(userComputer);
                        } else {
                            // already exists, update

                            userComputer.setLugServerId(lugServerId);
                            userComputer.setSocketConnected(false);
                            userComputer.setNeedReconnect(false);

                            userComputerDao.updateUserComputerConnectionStatus(userComputer);
                        }

                        // Apply connection, don't update the approved timestamp and the approved status if it already exists.

                        if (applyConnectionDao.findApplyConnectionByApplyUserAndComputerId(userId, computerId) == null) {
                            // create connection, always approve connection when connected by admin

                            long currentTimestamp = System.currentTimeMillis();

                            ApplyConnection applyConnection = new ApplyConnection(null, currentTimestamp, userId, computerId, true, userId, currentTimestamp);

                            applyConnectionDao.createOrUpdateApplyConnection(applyConnection);
                        }

                        // save device token

                        String deviceTokenString = null;

                        if (deviceTokenObject != null) {
                            deviceTokenString = deviceTokenObject.getDeviceToken();

                            // The device token object from computer contains no userId, need to add it here
                            deviceTokenObject.setAccount(userId);

                            try {
                                deviceTokenService.createOrUploadDeviceToken(deviceTokenObject, locale);
                            } catch (Exception e) {
                                String message = "Error on creating or updating device token: " + deviceTokenObject;
                                LOGGER.error(message, e);

                                throw new Exception(message);
                            }
                        }

                        // save system properties

                        if (desktopProperties != null) {
                            userComputerPropertiesDao.createUserComputerProperties(userComputerId, desktopProperties);
                        }

                        // login user, get session id for computer and send result to computer

                        computerEndpoint.loginUserAndSendResultMessageToComputer(userId, computerId, lugServerId, deviceTokenString);

                        // response with computer id, group, name and lug server id

                        /*
                            {
                                "computer-id" : 9987765,
                                "user-id" : "9aaa3acb2bf8aa13533040cc4b24cec22089eba94b4d1c11f3a520656abdab5305b5b3f905b994ea10a24c16783fc340",
                                "user-computer-id" : "9aaa3acb2bf8aa13533040cc4b24cec22089eba94b4d1c11f3a520656abdab5305b5b3f905b994ea10a24c16783fc340|9987765",
                                "computer-admin-id" : "9aaa3acb2bf8aa13533040cc4b24cec22089eba94b4d1c11f3a520656abdab5305b5b3f905b994ea10a24c16783fc340"
                                "computer-group" : "GENERAL",
                                "computer-name" : "ALBERT'S WORKSTATION",
                                "lug-server-id":"r1"
                            }
                         */

                        ObjectNode returnNode = mapper.createObjectNode();

                        returnNode.put(PropertyConstants.PROPERTY_NAME_COMPUTER_ID, computerId);
                        returnNode.put(PropertyConstants.PROPERTY_NAME_USER_ID, userId);
                        returnNode.put(PropertyConstants.PROPERTY_NAME_USER_COMPUTER_ID, userComputerId);
                        returnNode.put(PropertyConstants.PROPERTY_NAME_COMPUTER_ADMIN_ID, userId);
                        returnNode.put(PropertyConstants.PROPERTY_NAME_COMPUTER_GROUP, computerGroup);
                        returnNode.put(PropertyConstants.PROPERTY_NAME_COMPUTER_NAME, computerName);
                        returnNode.put(PropertyConstants.PROPERTY_NAME_LUG_SERVER_ID, lugServerId);
                        returnNode.put(PropertyConstants.PROPERTY_NAME_RECOVERY_KEY, recoveryKey);

                        String returnJsonString = mapper.writeValueAsString(returnNode);

                        resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write(returnJsonString);
                        resp.getWriter().flush();
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
        } catch (Exception e) {
            LOGGER.error("Error on connecting to computer.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        }
    }

    private String prepareUniqueComputerNameForUser(String userId, String computerName) {
        final String UNIQUE_COMPUTER_NAME_REG_EXP = ".*_\\d";

        String uniqueComputerName;

        try {
            // find all computer names for this user
            List<String> existingComputerNamesInLowerCase = computerDao.findComputerNamesByUserId(userId, true);

            if (existingComputerNamesInLowerCase.contains(computerName.toLowerCase())) {
                String computerNamePrefix;
                int currentSuffixNumber;

                if (Pattern.compile(UNIQUE_COMPUTER_NAME_REG_EXP).matcher(computerName).matches()) {
                    // find the suffix number
                    int lastIndexOfUnderscore = computerName.lastIndexOf("_");

                    computerNamePrefix = computerName.substring(0, lastIndexOfUnderscore);

                    currentSuffixNumber = Integer.valueOf(computerName.substring(lastIndexOfUnderscore + 1));
                } else {
                    computerNamePrefix = computerName;

                    currentSuffixNumber = 1;
                }

                String tmpComputerName = String.format("%s_%d", computerNamePrefix, currentSuffixNumber + 1);

                // loop to check again if the new computer name exists
                uniqueComputerName = prepareUniqueComputerNameForUser(userId, tmpComputerName);
            } else {
                uniqueComputerName = computerName;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on preparing unique computer name for user: '%s', original computer name: '%s'\nThe original computer name returned.", userId, computerName));

            uniqueComputerName = computerName;
        }

        return uniqueComputerName;
    }
}
