package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.*;
import org.clopuccino.dao.ComputerDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.Computer;
import org.clopuccino.service.ClientSessionService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <code>CheckComputerExistingServlet</code> checks if the computer exists.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "check-computer-exists", displayName = "check-computer-exists", description = "Check if computer exists", urlPatterns = {"/computer/exist"})
public class CheckComputerExistingServlet extends HttpServlet {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CheckComputerExistingServlet.class.getSimpleName());

    private static final long serialVersionUID = -83417902282240524L;

    private final ComputerDao computerDao;

    private ClientSessionService clientSessionService;

    public CheckComputerExistingServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        computerDao = new ComputerDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // make sure the client session id NOT FOUND

            String clientSessionId = req.getHeader(Constants.HTTP_HEADER_FILELUG_SESSION_ID_NAME);

            if (clientSessionId == null || clientSessionId.trim().length() < 1) {
                clientSessionId = req.getHeader(Constants.HTTP_HEADER_AUTHORIZATION_NAME);
            }

            if (clientSessionId == null || clientSessionId.trim().length() < 1) {
                String errorMessage = ClopuccinoMessages.localizedMessage(ClopuccinoMessages.DEFAULT_LOCALE, "session.not.provided");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();

                LOGGER.warn("Session not provided for service: " + req.getRequestURI());

            } else {
                ClientSession clientSession = clientSessionService.findClientSessionBySessionId(clientSessionId);

                if (clientSession != null) {
                    // session found --> return 400

                    String errorMessage = ClopuccinoMessages.getMessage("at.least.one.necessary.param.null");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();

                    LOGGER.warn(String.format("Session SHOULD NOT exist: '%s'", clientSessionId));
                } else {
                    // validate input

                    /*
                        {
                            "user-id" : "9aaa3acb2bf8aa13533040cc4b24cec22089eba94b4d1c11f3a520656abdab5305b5b3f905b994ea10a24c16783fc340",
                            "computer-id" : 3837763637383939,
                            "recoveryKey":"012336272652",
                            "computer-group" : "GENERAL",
                            "computer-name" : "ALBERT'S WORKSTATION",
                            "verification" : "bf94b4d1c5305b5b3f96abdab89eba05b994ea10a249aaa3acb28aa13533040cc4b24cec2783fc3402011f3a52065c16",
                            "locale" : "zh_TW",
                        }
                    */

                    ObjectMapper mapper = Utility.createObjectMapper();

                    JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

                    JsonNode userIdNode = jsonNode.get(PropertyConstants.PROPERTY_NAME_USER_ID);
                    JsonNode computerIdNode = jsonNode.get(PropertyConstants.PROPERTY_NAME_COMPUTER_ID);
                    JsonNode recoveryKeyNode = jsonNode.get(PropertyConstants.PROPERTY_NAME_RECOVERY_KEY);
                    JsonNode computerGroupNode = jsonNode.get(PropertyConstants.PROPERTY_NAME_COMPUTER_GROUP);
                    JsonNode computerNameNode = jsonNode.get(PropertyConstants.PROPERTY_NAME_COMPUTER_NAME);
                    JsonNode verificationNode = jsonNode.get(PropertyConstants.PROPERTY_NAME_VERIFICATION);
                    JsonNode localeNode = jsonNode.get(PropertyConstants.PROPERTY_NAME_LOCALE);

                    if (userIdNode == null || userIdNode.textValue() == null
                        || computerIdNode == null || computerIdNode.asLong(-1) < 0
                        || recoveryKeyNode == null || recoveryKeyNode.textValue() == null
                        || computerGroupNode == null || computerGroupNode.textValue() == null
                        || computerNameNode == null || computerNameNode.textValue() == null
                        || verificationNode == null || verificationNode.textValue() == null
                        || localeNode == null || localeNode.textValue() == null) {
                        String errorMessage = ClopuccinoMessages.getMessage("at.least.one.necessary.param.null");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else {
                        String userId = userIdNode.textValue();

                        Long computerId = userIdNode.asLong(-1);

                        String recoveryKey = recoveryKeyNode.textValue();

                        String locale = localeNode.textValue();

                        String expectedVerification = Utility.generateVerificationToCheckComputerExists(userId, computerId, recoveryKey);

                        if (!verificationNode.textValue().equals(expectedVerification)) {
                            String errorMessage = ClopuccinoMessages.localizedMessage(locale,"at.least.one.necessary.param.null");

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            resp.getWriter().write(errorMessage);
                            resp.getWriter().flush();
                        } else {
                            boolean keepGoing = true;
                            Computer computer = null;

                            try {
                                computer = computerDao.findComputerById(computerId);
                            } catch (Exception e) {
                                keepGoing = false;

                                String errorMessage = ClopuccinoMessages.localizedMessage(locale,"error.find.computer.data");

                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                resp.getWriter().write(errorMessage);
                                resp.getWriter().flush();
                            }

                            if (keepGoing) {
                                if (computer != null) {
                                    // computer found

                                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                    resp.setStatus(HttpServletResponse.SC_OK);
                                    resp.getWriter().write("Computer Found");
                                    resp.getWriter().flush();
                                } else {
                                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                    resp.setStatus(Constants.HTTP_STATUS_COMPUTER_NOT_FOUND);
                                    resp.getWriter().write("Computer Not Found");
                                    resp.getWriter().flush();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on check if computer exists.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
