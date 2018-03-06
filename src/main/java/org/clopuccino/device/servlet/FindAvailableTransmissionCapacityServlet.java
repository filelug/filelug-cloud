package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ClientSession;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <code>FindAvailableTransmissionCapacityServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "find-available-transmission-capacity", displayName = "find-available-transmission-capacity", description = "Find available transmission capacity", urlPatterns = {"/user/tcapacity"})
public class FindAvailableTransmissionCapacityServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FindAvailableTransmissionCapacityServlet.class.getSimpleName());

    private static final long serialVersionUID = -1486865618439437728L;

    private final DatabaseAccess dbAccess;

    private final ClientSessionService clientSessionService;


    public FindAvailableTransmissionCapacityServlet() {
        super();

        dbAccess = DatabaseUtility.createDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String userId = clientSession.getUserId();

            UserDao userDao = new UserDao(dbAccess);

            long availableTransmissionBytes = userDao.findAvailableTransferBytesForUser(userId);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(String.valueOf(availableTransmissionBytes));
            resp.getWriter().flush();
        } catch (Exception e) {
            LOGGER.error("Error on finding available transmission capacity.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    } // end doPost(HttpServletRequest, HttpServletResponse)
}
