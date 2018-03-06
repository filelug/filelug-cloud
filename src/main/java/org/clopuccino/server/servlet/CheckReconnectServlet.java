package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.db.DatabaseAccess;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <code>CheckReconnectServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "check-reconnect", displayName = "check-reconnect", description = "Check if the user computer need reconnect", urlPatterns = {"/ping"})
public class CheckReconnectServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("CHECK_RECONN");

    private static final long serialVersionUID = -992035290968309600L;

    private UserComputerDao userComputerDao;

    public CheckReconnectServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userComputerDao = new UserComputerDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String encryptedUserComputerId = null;

        try {
            /* validate client session id */
            encryptedUserComputerId = req.getHeader(Constants.HTTP_HEADER_AUTHORIZATION_NAME);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);

            if (encryptedUserComputerId == null || encryptedUserComputerId.trim().length() < 1) {
                /* no encrypted user found */
                LOGGER.warn("Empty encrypted user computer id for reconnect check from " + req.getRemoteAddr());

                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                Boolean[] founds = userComputerDao.findReconnectAndSocketConnectedByEncryptedId(encryptedUserComputerId);

                Boolean reconnect = founds[0];
                Boolean socketConnected = founds[1];

                // DEBUG
//                LOGGER.debug("reconnect=" + reconnect + ", socket_connected=" + socketConnected);

                if (reconnect == null) {
                    LOGGER.debug("UserComputer not found for encrypted user computer id: " + encryptedUserComputerId);

                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                } else {
                    if (reconnect) {
                        LOGGER.debug("Need to reconnect for encrypted user: " + encryptedUserComputerId);

                        resp.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        if (socketConnected != null && socketConnected) {
                            resp.setStatus(HttpServletResponse.SC_CONFLICT);
                        } else {
                            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                        }
                    }
                }
            }

            resp.getWriter().write("");
            resp.getWriter().flush();
        } catch (Exception e) {
            String errorMessage = "Failed to check reconnect.";

            LOGGER.error(errorMessage + ", encrypted user computer id: " + encryptedUserComputerId, e);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        }
    }
}
