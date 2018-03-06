package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.DeviceTokenDao;
import org.clopuccino.dao.UserDao;
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
import java.util.ArrayList;
import java.util.List;

/**
 * <code>CreateMultipleTokensServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "increment-badge-number", displayName = "increment-badge-number", description = "Add or minus badge number by device token and account", urlPatterns = {"/user/badge-number"})
public class IncrementBadgeNumberServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(IncrementBadgeNumberServlet.class.getSimpleName());

    private static final long serialVersionUID = 2777188781894195338L;

    private final UserDao userDao;

    private final DeviceTokenDao deviceTokenDao;

    private final ClientSessionService clientSessionService;


    public IncrementBadgeNumberServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        deviceTokenDao = new DeviceTokenDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String clientLocale = clientSession.getLocale();

                    /* check json input */
            ObjectMapper mapper = Utility.createObjectMapper();

            List<DeviceToken> deviceTokens = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), new TypeReference<List<DeviceToken>>() {
            });

            if (deviceTokens != null && deviceTokens.size() > 0) {
                List<DeviceToken> successUpdated = new ArrayList<>();
                String errorMessage = null;

                for (DeviceToken deviceToken : deviceTokens) {
                    if (errorMessage != null) {
                        break;
                    }

                    String deviceTokenString = deviceToken.getDeviceToken();
                    String account = deviceToken.getAccount();
                    Integer incrementalBadgeNumber = deviceToken.getIncrementBadgeNumber();

                    if (deviceTokenString == null || deviceTokenString.trim().length() < 1) {
                        errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "device token");

                        LOGGER.error(String.format("Error on adding badge number for empty device token. User: '%s'", account));
                    } else if (incrementalBadgeNumber == null) {
                        errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "incremental badge number");

                        LOGGER.error(String.format("Error on adding badge number for empty incremen badge number. Device token: '%s', user: '%s'", deviceTokenString, account));
                    } else if (account == null || account.trim().length() < 1) {
                        errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "user account");

                        LOGGER.error(String.format("Error on adding badge number for empty user. Device token: '%s'.", deviceTokenString));
                    } else if (!userDao.findExistsById(account)) {
                        errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                        LOGGER.error(String.format("Error on adding badge number for user '%s' not found. Device token: '%s'", account, deviceTokenString));
                    } else {
                        DeviceToken successDeviceToken = deviceTokenDao.incrementBadgeNumberBy(deviceTokenString, account, incrementalBadgeNumber);

                        if (successDeviceToken != null) {
                            successUpdated.add(successDeviceToken);

                            LOGGER.debug("Badge number incremented/decremented for device token: " + successDeviceToken);
                        } else {
                            LOGGER.debug("Failed to increment/decrement badge number for device token: " + deviceToken);
                        }
                    }
                }

                if (errorMessage != null) {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    String successUpdatedString = mapper.writeValueAsString(successUpdated);

                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(successUpdatedString);
                    resp.getWriter().flush();
                }
            } else {
                LOGGER.warn("Null device tokens to increment/decrement badge number. Sent by user: " + clientSession.getUserId() + "session id: " + clientSession.getSessionId());
            }
        } catch (IOException e) {
            String errorMessage = "Incorrect request parameter: " + e.getMessage();

            LOGGER.error(errorMessage);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        } catch (Exception e) {
            LOGGER.error("Error on incrementing badge numbers.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
