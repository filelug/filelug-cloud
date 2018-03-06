package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.DeviceTokenDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.DeviceToken;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * <code>CreateMultipleTokensServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "create-or-update-device-tokens", displayName = "create-or-update-device-tokens", description = "create or update multiple device tokens", urlPatterns = {"/user/device-token"})
public class CreateOrUpdateDeviceTokensServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CreateOrUpdateDeviceTokensServlet.class.getSimpleName());

    private static final long serialVersionUID = 8739606787513489884L;

    private final DeviceTokenDao deviceTokenDao;

    private final ClientSessionService clientSessionService;


    public CreateOrUpdateDeviceTokensServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        deviceTokenDao = new DeviceTokenDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String clientLocale = clientSession.getLocale();

            // check json input
            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode readRootNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode sessionsNode = readRootNode.get("sessions");

            if (sessionsNode == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "sessions");

                LOGGER.error("Failed to create/update device token.\n" + errorMessage);

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                Iterator<JsonNode> iterator = sessionsNode.elements();

                Set<String> appliedSessions = new HashSet<>();

                for (; iterator.hasNext(); ) {
                    appliedSessions.add(iterator.next().textValue());
                }

                if (appliedSessions.size() < 1) {
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "sessions");

                    LOGGER.error("Failed to create/update device token.\n" + errorMessage);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    // make sure session exists and not eliminate duplication

                    Set<ClientSession> validClientSessions = new HashSet<>();

                    for (String appliedSession : appliedSessions) {
                        ClientSession appliedClientSession = clientSessionService.findValidClientSessionById(appliedSession, false);

                        if (appliedClientSession != null) {
                            validClientSessions.add(appliedClientSession);
                        }
                    }

                    if (validClientSessions.size() > 0) {
                        // Check device token

                        JsonNode deviceTokenObjectNode = readRootNode.findValue("device-token");

                        JsonNode deviceTokenStringNode = deviceTokenObjectNode.findValue("device-token");
                        JsonNode notificationTypeNode = deviceTokenObjectNode.findValue("notification-type");
                        JsonNode deviceTypeNode = deviceTokenObjectNode.findValue("device-type");
                        JsonNode deviceVersionNode = deviceTokenObjectNode.findValue("device-version");

                        // optional ones

                        JsonNode filelugVersionNode = deviceTokenObjectNode.findValue("filelug-version");
                        JsonNode filelugBuildNode = deviceTokenObjectNode.findValue("filelug-build");
                        JsonNode badgeNumberNode = deviceTokenObjectNode.findValue("badge-number");

                        if (deviceTokenStringNode == null || deviceTokenStringNode.textValue() == null || deviceTokenStringNode.textValue().trim().length() < 1) {
                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "device token");

                            LOGGER.error("Skipped creating/update device token.\n" + errorMessage);
                        } else if (notificationTypeNode == null || notificationTypeNode.textValue() == null || notificationTypeNode.textValue().trim().length() < 1) {
                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "notification type");

                            LOGGER.error("Skipped creating/update device token.\n" + errorMessage);
                        } else if (deviceTypeNode == null || deviceTypeNode.textValue() == null || deviceTypeNode.textValue().trim().length() < 1) {
                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "device type");

                            LOGGER.error("Skipped creating/update device token.\n" + errorMessage);
                        } else if (deviceVersionNode == null || deviceVersionNode.textValue() == null || deviceVersionNode.textValue().trim().length() < 1) {
                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "device version");

                            LOGGER.error("Skipped creating/update device token.\n" + errorMessage);
                        } else {
                            String deviceTokenString = deviceTokenStringNode.textValue();

                            // 1/2. Update to client sessions

                            for (ClientSession validClientSession : validClientSessions) {
                                String validClientSessionId = validClientSession.getSessionId();

                                clientSessionService.updateClientSessionDeviceToken(validClientSessionId, deviceTokenString);

                                LOGGER.debug("Updated device token: '" + deviceTokenString + "' to client session: " + validClientSessionId);
                            }

                            String notificationType = notificationTypeNode.textValue();
                            String deviceType = deviceTypeNode.textValue();
                            String deviceVersion = deviceVersionNode.textValue();

                            DeviceToken deviceToken = new DeviceToken();
                            deviceToken.setDeviceToken(deviceTokenString);
                            deviceToken.setNotificationType(notificationType);
                            deviceToken.setDeviceType(deviceType);
                            deviceToken.setDeviceVersion(deviceVersion);

                            // deal with optional nodes

                            if (filelugVersionNode != null && filelugVersionNode.textValue() != null && filelugVersionNode.textValue().trim().length() > 0) {
                                deviceToken.setFilelugVersion(filelugVersionNode.textValue());
                            }

                            if (filelugBuildNode != null && filelugBuildNode.textValue() != null && filelugBuildNode.textValue().trim().length() > 0) {
                                deviceToken.setFilelugBuild(filelugBuildNode.textValue());
                            }

                            if (badgeNumberNode != null && badgeNumberNode.isIntegralNumber()) {
                                deviceToken.setBadgeNumber(badgeNumberNode.asInt());
                            } else {
                                deviceToken.setBadgeNumber(0);
                            }

                            // 2/2. Eliminate duplicated user id and create/update to table device-token.

                            Set<String> appliedUsers = new HashSet<>();

                            for (ClientSession validClientSession : validClientSessions) {
                                appliedUsers.add(validClientSession.getUserId());
                            }

                            for (String appliedUserId : appliedUsers) {
                                deviceToken.setAccount(appliedUserId);

                                DeviceToken successDeviceToken = deviceTokenDao.createOrUpdateDeviceToken(deviceToken);

                                if (successDeviceToken != null) {
                                    LOGGER.debug("Device token created/updated: " + successDeviceToken);
                                } else {
                                    LOGGER.debug("Failed to create/update device token: " + deviceToken);
                                }
                            }

                            resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                            resp.setStatus(HttpServletResponse.SC_OK);
                            resp.getWriter().write("OK");
                            resp.getWriter().flush();
                        }
                    }
                }
            }
        } catch (IOException e) {
            String errorMessage = "Incorrect request parameter: " + e.getMessage();

            LOGGER.error(errorMessage, e);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        } catch (Exception e) {
            LOGGER.error("Error on creating or updating multiple device tokens", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
