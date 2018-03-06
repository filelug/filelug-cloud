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
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.Computer;
import org.clopuccino.domain.UserComputerConnectionStatus;
import org.clopuccino.service.ClientSessionService;
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
 * <code>CheckReconnectServlet3</code> checks multiple user computer at the same time.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "check-reconnect3", displayName = "check-reconnect3", description = "Check if the user computers need reconnect(V3)", urlPatterns = {"/ping3"})
public class CheckReconnectServlet3 extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CheckReconnectServlet3.class.getSimpleName());

    private static final long serialVersionUID = -7282194164900818951L;

    private UserComputerDao userComputerDao;

    private ComputerDao computerDao;

    private final ClientSessionService clientSessionService;

    public CheckReconnectServlet3() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userComputerDao = new UserComputerDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userIdFromSession = null;
        Long computerIdFromSession = null;

        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            userIdFromSession = clientSession.getUserId();

            computerIdFromSession = clientSession.getComputerId();

            ObjectMapper mapper = Utility.createObjectMapper();

            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
            List <String> encryptedUserComputerIds = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), type);

            if (encryptedUserComputerIds == null || encryptedUserComputerIds.size() < 1) {
                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("ERROR");
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

                    // Check computer
                    if (!theSameComputer) {
                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write("ERROR");
                        resp.getWriter().flush();
                    } else {
                        // Check if the admin user of the computer is the user of the session

                        Computer computer = computerDao.findComputerById(computerId);

                        if (computer == null || !computer.getUserId().equals(userIdFromSession)) {
                            // the admin user of the computer is NOT the user of the session

                            LOGGER.warn(String.format("[Hack Warning]Computer not found or the admin user of this computer is not the user of the session.\n Computer id: '%d'\nAdmin user of the computer: '%s'\nUser of the session: '%s'\nReconnect check from: %s", computerId, (computer != null ? computer.getUserId() : "null"), userIdFromSession, req.getRemoteAddr()));

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            resp.getWriter().write("ERROR");
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
        } catch (Exception e) {
            String errorMessage = "Failed to check reconnect.";

            LOGGER.error(String.format("%s user: '%s', computer: '%d'", errorMessage, userIdFromSession, computerIdFromSession), e);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        }
    }
}
