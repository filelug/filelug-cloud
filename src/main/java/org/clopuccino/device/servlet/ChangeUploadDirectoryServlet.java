package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.UserComputer;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * <code>FindFileByPathServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "change-upload-directory", displayName = "change-upload-directory", description = "Change the default upload directory of the current user computer", urlPatterns = {"/computer/udir"})
public class ChangeUploadDirectoryServlet extends HttpServlet {

    private static final long serialVersionUID = 4127287245488446248L;

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ChangeUploadDirectoryServlet.class.getSimpleName());

    private final UserComputerDao userComputerDao;

    private final ClientSessionService clientSessionService;

    public ChangeUploadDirectoryServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userComputerDao = new UserComputerDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String clientLocale = clientSession.getLocale();

            // check json input
            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode uploadDirectoryNode = jsonNode.get("upload-directory");

            if (uploadDirectoryNode == null || uploadDirectoryNode.textValue() == null) {
                // upload directory not found or invalid
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "directory");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                // update the upload directory of the user-computer

                String userComputerId = clientSession.getUserComputerId();

                UserComputer userComputer = userComputerDao.findUserComputerById(userComputerId);

                if (userComputer == null) {
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "login.again.get.computer.data");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    userComputerDao.updateUploadDirectoryById(userComputer.getUserComputerId(), uploadDirectoryNode.textValue());

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("OK");
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on changing upload directory.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
