package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.*;
import org.clopuccino.dao.ComputerDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.Computer;
import org.clopuccino.domain.RequestDeleteComputerModel;
import org.clopuccino.server.servlet.Sid;
import org.clopuccino.server.servlet.websocket.ConnectSocket;
import org.clopuccino.service.ClientSessionService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <code>DeleteComputerFromDeviceServlet</code> deletes computer from device
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "delete-computer-from-device", displayName = "delete-computer-from-device", description = "Delete computer from device", urlPatterns = {"/computer/delete2"})
public class DeleteComputerFromDeviceServlet extends HttpServlet {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DeleteComputerFromDeviceServlet.class.getSimpleName());

    private static final long serialVersionUID = 1102093666018266730L;

    private final ComputerDao computerDao;

    private final ClientSessionService clientSessionService;


    public DeleteComputerFromDeviceServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        computerDao = new ComputerDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            final String userId = clientSession.getUserId();

            String clientLocale = clientSession.getLocale();

            /*
                {
                    "computer-id" : 3837763637383939,
                    "verification" : "bf94b4d1c5305b5b3f96abdab89eba05b994ea10a249aaa3acb28aa13533040cc4b24cec2783fc3402011f3a52065c16",
                    "locale" : "zh_TW"
                }
             */

            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode computerIdNode = jsonNode.get(PropertyConstants.PROPERTY_NAME_COMPUTER_ID);
            JsonNode verificationNode = jsonNode.get(PropertyConstants.PROPERTY_NAME_VERIFICATION);
            JsonNode localeNode = jsonNode.get(PropertyConstants.PROPERTY_NAME_LOCALE);

            // Use locale in input params if not null

            if (localeNode != null && localeNode.textValue() != null) {
                clientLocale = localeNode.textValue();
            }

            if (computerIdNode == null || computerIdNode.asLong(-1) < 0
                || verificationNode == null || verificationNode.textValue() == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                // Make sure the owner of the computer is the user

                long computerId = computerIdNode.asLong(-1);

                String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

                Computer computer = computerDao.findComputerById(computerId);

                if (computer == null || !computer.getUserId().equals(userId)) {
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "permission.denied.delete.computer");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    String expectedVerification = Utility.generateVerificationToDeleteComputer(userId, computerId);

                    if (!verificationNode.textValue().equals(expectedVerification)) {
                        LOGGER.warn("Be careful that user with session: '" + clientSession.getSessionId() + "' is trying to hack the verification code for DeleteComputerFromDevice.");

                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else {
                        // let computer delete itself if connected

                        final String finalLocale = clientLocale;

                        Utility.getExecutorService().execute(() -> {
                            try {
                                ConnectSocket socket = ConnectSocket.getInstance(userComputerId);

                                if (socket != null && socket.validate(true)) {
                                    // send message to server
                                    // don't wait for the response here

                                    RequestDeleteComputerModel requestModel = new RequestDeleteComputerModel(Sid.DELETE_COMPUTER_V2, userId, finalLocale, expectedVerification);
                                    String requestJson = mapper.writeValueAsString(requestModel);

                                    Session socketSession = socket.getSession();

                                    socketSession.getAsyncRemote().sendText(requestJson);
                                } else {
                                    String message = String.format("ConnectSocket for computer '%d' not reachable for user: '%s', but the server will still delete the related data for this computer.", computerId, userId);
                                    LOGGER.info(message);
                                }
                            } catch (Exception e) {
                                String errorMessage = String.format("Error on sending message to computer '%d' to delete itself for user: '%s'\nBut the server will still delete the related data for this computer.", computerId, userId);
                                LOGGER.error(errorMessage, e);
                            }
                        });

                        // DO NOT delete related client sessions of devices because the computer id of this session
                        // can be updated when the device connects to another computer.
//                        clientSessionService.removeClientSessionsByComputer(computerId);

                        // delete the old computer name, cascading for related UserComputers
                        computerDao.deleteComputerById(computerId);

                        resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write("Computer Deleted");
                        resp.getWriter().flush();
                    }
                }
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error("Error on deleting computer.", e);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
