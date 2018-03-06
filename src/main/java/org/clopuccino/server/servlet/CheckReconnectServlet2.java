package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.ComputerDao;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.Computer;
import org.clopuccino.domain.UserComputerConnectionStatus;
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
 * <code>CheckReconnectServlet2</code> checks multiple user computer at the same time.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "check-reconnect2", displayName = "check-reconnect2", description = "Check if the user computers need reconnect(V2)", urlPatterns = {"/ping2"})
public class CheckReconnectServlet2 extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CheckReconnectServlet2.class.getSimpleName());

    private static final long serialVersionUID = -992035290968309600L;

    private UserComputerDao userComputerDao;

    private ComputerDao computerDao;

    public CheckReconnectServlet2() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userComputerDao = new UserComputerDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // validate client session id
        String encryptedAdminId = req.getHeader(Constants.HTTP_HEADER_AUTHORIZATION_NAME);

        try {
            if (encryptedAdminId == null || encryptedAdminId.trim().length() < 1) {
                // no encrypted user found
                LOGGER.warn("[Hack Warning]Empty encrypted admin id. Reconnect check from " + req.getRemoteAddr());

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("");
                resp.getWriter().flush();
            } else {
                ObjectMapper mapper = Utility.createObjectMapper();

                JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
                List <String> encryptedUserComputerIds = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), type);

                if (encryptedUserComputerIds == null || encryptedUserComputerIds.size() < 1) {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("");
                    resp.getWriter().flush();
                } else {
                    List<UserComputerConnectionStatus> connectionStatuses = userComputerDao.findConnectionStatusesByEncryptedUserComputerId(encryptedUserComputerIds);

                    if (connectionStatuses != null && connectionStatuses.size() > 0) {
                        // test if all the user computers are from the same computer,
                        // and the admin id of this computer is the value of encryptedAdminId

                        boolean theSameComputer = true;
                        Long computerId = null;
                        for (UserComputerConnectionStatus connectionStatuse: connectionStatuses) {
                            Long currentComputerId = connectionStatuse.getComputerId();

                            if (computerId == null) {
                                computerId = currentComputerId;
                            } else if (!computerId.equals(currentComputerId)) {
                                theSameComputer = false;

                                break;
                            }
                        }

                        if (!theSameComputer) {
                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            resp.getWriter().write("");
                            resp.getWriter().flush();
                        } else {
                            Computer computer = computerDao.findComputerById(computerId);

                            if (computer == null || !Utility.generateEncryptedUserIdFrom(computer.getUserId()).equals(encryptedAdminId)) {
                                // Incorrect encrypted user found
                                LOGGER.warn("[Hack Warning]Computer not found or the provided admin id is not the admin user of the computer: " + computerId + ". Reconnect check from " + req.getRemoteAddr());

                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                resp.getWriter().write("");
                                resp.getWriter().flush();
                            } else {
                                String responseString = mapper.writeValueAsString(connectionStatuses);

                                resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                resp.setStatus(HttpServletResponse.SC_OK);
                                resp.getWriter().write(responseString);
                                resp.getWriter().flush();
                            }
                        }
                    } else {
                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write("");
                        resp.getWriter().flush();
                    }
                }
            }
        } catch (Exception e) {
            String errorMessage = "Failed to check reconnect.";

            LOGGER.error(errorMessage + " Encrypted admin id: " + encryptedAdminId, e);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        }
    }
}
