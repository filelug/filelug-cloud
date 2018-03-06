package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.dao.UserComputerPropertiesDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.*;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.service.DeviceTokenService;
import org.clopuccino.service.LoginService;
import org.clopuccino.service.PropertiesSerializer;
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
 * <code>ConnectToComputerServlet</code> connects to the specified computer and returns the profile and settings of the computer.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "connect-to-computer", displayName = "connect-to-computer", description = "Connects to the specified computer", urlPatterns = {"/computer/connect-to-computer"})
public class ConnectToComputerServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ConnectToComputerServlet.class.getSimpleName());

    private static final long serialVersionUID = -5790930148060977500L;

    private final UserDao userDao;

    private final ClientSessionService clientSessionService;

    private final UserComputerDao userComputerDao;

    private final UserComputerPropertiesDao userComputerPropertiesDao;

    private final LoginService loginService;

    private final DeviceTokenService deviceTokenService;

    public ConnectToComputerServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        userComputerPropertiesDao = new UserComputerPropertiesDao(dbAccess);

        loginService = new LoginService(dbAccess);

        deviceTokenService = new DeviceTokenService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = Utility.createObjectMapper();

        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String locale = clientSession.getLocale();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode computerIdNode = jsonNode.get("computer-id");

            JsonNode showHiddenNode = jsonNode.get("showHidden");

            if (showHiddenNode == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(ClopuccinoMessages.DEFAULT_LOCALE, "param.null.or.empty", "show hidden");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (computerIdNode == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(locale, "param.null.or.empty", "computer id");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                long computerId = computerIdNode.asLong(0);

                if (computerId < 1) {
                    String errorMessage = ClopuccinoMessages.localizedMessage(locale, "param.invalid", "computer id");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    if (!showHiddenNode.isBoolean()) {
                        String errorMessage = ClopuccinoMessages.localizedMessage(locale, "param.invalid", "show hidden");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else {
                        boolean showHidden = showHiddenNode.asBoolean(false);

                        String userId = clientSession.getUserId();

                        User user = userDao.findUserById(userId);

                        if (user != null) {
                            // User found

                            JsonNode deviceTokenObjectNode = jsonNode.get("device-token");

                            DeviceToken deviceTokenObject = null;

                            boolean keepGoing = true;

                            try {
                                deviceTokenObject = DeviceTokenService.prepareDeviceToken(deviceTokenObjectNode, locale);
                            } catch (IllegalArgumentException e) {
                                keepGoing = false;

                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                resp.getWriter().write(e.getMessage());
                                resp.getWriter().flush();
                            }

                            if (keepGoing) {
                                String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

                                UserComputer userComputer = userComputerDao.findUserComputerById(userComputerId);

                                if (userComputer != null) {
//                                    // DEBUG
//                                    LOGGER.info("UserComputer found on connecting with device.\n" + userComputer);

                                    // bind computer id, user computer id, showHidden and device token with ClientSession, the current value of ClientSession may be null
                                    // Update the session object before checking version of desktop.

                                    clientSession.setComputerId(computerId);
                                    clientSession.setUserComputerId(userComputerId);

                                    if (deviceTokenObject != null && deviceTokenObject.getDeviceToken() != null) {
                                        clientSession.setDeviceToken(deviceTokenObject.getDeviceToken());
                                    }

                                    clientSession.setShowHidden(showHidden);
                                    clientSession.setLocale(locale);

                                    clientSessionService.updateClientSession(clientSession);

//                                    // DEBUG
//                                    LOGGER.info("Client session updated:\n" + clientSession);

                                    // validate desktop version

                                    String desktopVersion = userComputerPropertiesDao.findPropertyValue(userComputerId, org.clopuccino.PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);
//                                    // find the owner of the computer
//                                    String computerOwner = computerDao.findComputerOwnerById(computerId);
//
//                                    String ownerUserComputerId;
//
//                                    if (computerOwner != null && computerOwner.trim().length() > 0) {
//                                        ownerUserComputerId = Utility.generateUserComputerIdFrom(computerOwner, computerId);
//                                    } else {
//                                        ownerUserComputerId = userComputerId;
//                                    }
//
//                                    String desktopVersion = userComputerPropertiesDao.findPropertyValue(ownerUserComputerId, org.clopuccino.PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);

                                    if (desktopVersion == null) {
                                        desktopVersion = Constants.DEFAULT_DESKTOP_VERSION;
                                    }

                                    if (Version.valid(desktopVersion)
                                        && new Version(desktopVersion).compareTo(new Version(Constants.INITIAL_VERSION_TO_V2)) < 0) {
                                        keepGoing = false;

                                        // Incompatible with version of desktop that is lower than 2.0.0

                                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                        resp.setStatus(Constants.HTTP_STATUS_DESKTOP_VERSION_TOO_OLD);
                                        resp.getWriter().write(ClopuccinoMessages.localizedMessage(locale, "desktop.need.update", userComputer.getComputerName()));
                                        resp.getWriter().flush();
                                    } else {
                                        if (deviceTokenObject != null) {
                                            // Compare the major part of the version of the desktop with the device

                                            String filelugVersion = deviceTokenObject.getFilelugVersion();
                                            String filelugBuild = deviceTokenObject.getFilelugBuild();

                                            int majorVersionCompared = loginService.compareMajorVersion(filelugVersion, filelugBuild, desktopVersion);

                                            if (majorVersionCompared == 0) {
                                                // create or update the device token

                                                // The device token object from computer contains no userId, need to add it here
                                                deviceTokenObject.setAccount(userId);

                                                try {
                                                    deviceTokenService.createOrUploadDeviceToken(deviceTokenObject, locale);
                                                } catch (Exception e) {
                                                    keepGoing = false;

                                                    String message = "Error on creating or updating device token: " + deviceTokenObject;
                                                    LOGGER.error(message, e);

                                                    throw new Exception(message);
                                                }
                                            } else if (majorVersionCompared > 0) {
                                                keepGoing = false;

                                                // device is newer

                                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                                resp.setStatus(Constants.HTTP_STATUS_DESKTOP_VERSION_TOO_OLD);
                                                resp.getWriter().write(ClopuccinoMessages.localizedMessage(locale, "desktop.need.update", userComputer.getComputerName()));
                                                resp.getWriter().flush();
                                            } else {
                                                keepGoing = false;

                                                // desktop is newer

                                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                                resp.setStatus(Constants.HTTP_STATUS_DEVICE_VERSION_TOO_OLD);
                                                resp.getWriter().write(ClopuccinoMessages.localizedMessage(locale, "device.need.update", userComputer.getComputerName()));
                                                resp.getWriter().flush();
                                            }
                                        }

                                        if (keepGoing) {
                                            String lugServerId;

                                            Boolean socketConnected = userComputer.isSocketConnected();

                                            try {
                                                // request connect desktop if not

                                                if (socketConnected == null || !socketConnected) {
                                                    lugServerId = null;

                                                    // update reconnect to true -- only for checkDesktopSocket

                                                    Boolean reconnect = userComputer.isNeedReconnect();

                                                    if (reconnect == null || !reconnect) {
                                                        // update reconnect flag to true
                                                        userComputerDao.updateReconnectByUserComputerId(userComputerId, Boolean.TRUE);

                                                        LOGGER.info(String.format("Request new connection to computer '%d' for user: '%s'(id: '%s')", computerId, user.getPhoneNumber(), userId));
                                                    }
                                                } else {
                                                    lugServerId = userComputer.getLugServerId();
                                                }

                                                PropertiesSerializer propertiesSerializer = new PropertiesSerializer();
                                                propertiesSerializer.addToObjectMapper(mapper);

                                                Properties responseProperties = loginService.prepareResponsePropertiesForConnectingToComputer(userComputer, socketConnected, lugServerId);

                                                resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                                resp.setStatus(HttpServletResponse.SC_OK);
                                                resp.getWriter().write(mapper.writeValueAsString(responseProperties));
                                                resp.getWriter().flush();
                                            } catch (Exception e) {
                                                String errorMessage = e.getClass().getName() + ": " + e.getMessage();

                                                LOGGER.error("Login failed. error: " + errorMessage, e);

                                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                                resp.getWriter().write(errorMessage);
                                                resp.getWriter().flush();
                                            }
                                        }
                                    }
                                } else {
                                    String errorMessage = ClopuccinoMessages.localizedMessage(locale, "server.not.setup");

                                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                    resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                                    resp.getWriter().write(errorMessage);
                                    resp.getWriter().flush();
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
            LOGGER.error("Error on connecting to computer.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        }
    }
}
