package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.User;
import org.clopuccino.domain.UserComputer;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>FindAvailableComputersServlet3</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "find-available-computers3", displayName = "find-available-computers3", description = "Find available computers(v3)", urlPatterns = {"/computer/available3"})
public class FindAvailableComputersServlet3 extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FindAvailableComputersServlet3.class.getSimpleName());

    private static final long serialVersionUID = -2223399506138310190L;

    private UserDao userDao;

    private UserComputerDao userComputerDao;

    private final ClientSessionService clientSessionService;

    public FindAvailableComputersServlet3() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String clientLocale = clientSession.getLocale();

            String userId = clientSession.getUserId();

            User user = userDao.findUserById(userId);

            if (user == null) {
                // user not found

                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                // 使用者曾經連線computer清單
                List<UserComputer> userComputers = userComputerDao.findUserComputersByUserId(userId);

                // response user id if no computers found.

                if (userComputers == null || userComputers.size() < 1) {
                    UserComputer userComputer = new UserComputer();

                    userComputer.setUserId(userId);

                    userComputers = new ArrayList<>();
                    userComputers.add(userComputer);
                }

                ObjectMapper mapper = Utility.createObjectMapper();

                String responseString = mapper.writeValueAsString(userComputers);

                resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(responseString);
                resp.getWriter().flush();
            }
        } catch (Exception e) {
            LOGGER.error("Error on finding available computer(V3)", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
