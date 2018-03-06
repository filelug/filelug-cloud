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
import org.clopuccino.domain.UserComputerProfile;
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
import java.util.Map;


/**
 * <code>FindUserComputerProfilesServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "find-user-computer-profiles", displayName = "find-user-computer-profiles", description = "Find the default properties of the current user computer", urlPatterns = {"/computer/profiles"})
public class FindUserComputerProfilesServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FindUserComputerProfilesServlet.class.getSimpleName());

    private static final long serialVersionUID = -1612508497101409821L;

    private final UserComputerDao userComputerDao;

    private final ClientSessionService clientSessionService;

    public FindUserComputerProfilesServlet() {
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

            String userComputerId = clientSession.getUserComputerId();

            UserComputer userComputer = userComputerDao.findUserComputerById(userComputerId);

            if (userComputer == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "login.again.get.computer.data");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                List<UserComputerProfile> userComputerProfiles = new ArrayList<>();

                // Find correspondent database column name from property name

                JsonNode propertyNames = jsonNode.get("names");

                if (propertyNames != null && propertyNames.isArray()) {
                    for (final JsonNode propertyNameNode : propertyNames) {
                        String propertyName = propertyNameNode.textValue();

                        if (propertyName != null) {
                            try {
                                UserComputerProfile profile = new UserComputerProfile(propertyName);

                                userComputerProfiles.add(profile);
                            } catch (Exception e) {
                                // skip illegal property
                            }
                        }
                    }
                }

                Map<String, Object> propertyNameAndValues = userComputerDao.findUserComputerProfilesById(userComputer.getUserComputerId(), userComputerProfiles);


                // FIX: If it throws exception because of serialization error,
                // try to add PropertiesSerializer to this mapper
                String responseJson = mapper.writeValueAsString(propertyNameAndValues);

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(responseJson);
                resp.getWriter().flush();
            }
        } catch (Exception e) {
            LOGGER.error("Error on finding user computer profiles", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
