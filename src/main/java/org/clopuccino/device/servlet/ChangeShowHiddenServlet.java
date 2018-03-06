package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
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
import java.io.InputStreamReader;

/**
 * <code>ChangeShowHiddenServlet</code> updates the showHidden value of the current ClientSession.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "change-show-hidden", displayName = "change-show-hidden", description = "Change User Email", urlPatterns = {"/computer/showHidden"})
public class ChangeShowHiddenServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ChangeShowHiddenServlet.class.getSimpleName());

    private static final long serialVersionUID = -4963504471041001081L;

    private final ClientSessionService clientSessionService;

    public ChangeShowHiddenServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = Utility.createObjectMapper();

        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String locale = clientSession.getLocale();

            Long sessionComputerId = clientSession.getComputerId();

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

                if (computerId < 1 || sessionComputerId == null || computerId != sessionComputerId) {
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

                        // update the session object with showHidden

                        clientSession.setShowHidden(showHidden);

                        clientSessionService.updateClientSession(clientSession);

                        resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write("OK");
                        resp.getWriter().flush();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on updating the value of show hidden", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        }
    }
}
