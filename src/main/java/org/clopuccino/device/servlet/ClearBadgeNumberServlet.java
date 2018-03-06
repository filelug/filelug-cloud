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
@WebServlet(name = "clear-badge-number", displayName = "clear-badge-number", description = "Change badge number to zero by device token and account", urlPatterns = {"/user/clear-badge-number"})
public class ClearBadgeNumberServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ClearBadgeNumberServlet.class.getSimpleName());

    private static final long serialVersionUID = 7970624460486451460L;

    private final UserDao userDao;

    private final DeviceTokenDao deviceTokenDao;

    private final ClientSessionService clientSessionService;


    public ClearBadgeNumberServlet() {
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

            // check json input
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

                    if (deviceTokenString == null || deviceTokenString.trim().length() < 1) {
                        errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "device token");

                        LOGGER.error(String.format("Error on clearing badge number for device token is empty. User: '%s'.", account));
                    } else if (account == null || account.trim().length() < 1) {
                        errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "user account");

                        LOGGER.error(String.format("Error on clearing badge number for user is empty. Device token: '%s'.", deviceTokenString));
                    } else if (!userDao.findExistsById(account)) {
                        errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                        LOGGER.error(String.format("Error on clearing badge number for user '%s' not found. Device token: '%s'", account, deviceTokenString));
                    } else {
                        DeviceToken successDeviceToken = deviceTokenDao.clearBadgeNumberBy(deviceTokenString, account);

                        if (successDeviceToken != null) {
                            successUpdated.add(successDeviceToken);

                            LOGGER.debug(String.format("Badge number cleared successfully for device token: '%s', user: '%s' ", successDeviceToken, account));
                        } else {
                            LOGGER.debug(String.format("Failed to clear badge number for device token: '%s', user: '%s'", deviceToken, account));
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
                LOGGER.warn("Null device tokens to clear badge number. Sent by user: " + clientSession.getUserId() + "session id: " + clientSession.getSessionId());
            }
        } catch (IOException e) {
            String errorMessage = "Incorrect request parameter: " + e.getMessage();

            LOGGER.error(errorMessage, e);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        } catch (Exception e) {
            LOGGER.error("Error on clearing badge number.", e);
            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
