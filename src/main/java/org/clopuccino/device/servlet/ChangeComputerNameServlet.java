package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.*;
import org.clopuccino.PropertyConstants;
import org.clopuccino.dao.*;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.*;
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
 * <code>ChangeComputerNameServlet</code> changes computer name
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "change-computer-name", displayName = "change-computer-name", description = "Change computer name", urlPatterns = {"/computer/name"})
public class ChangeComputerNameServlet extends HttpServlet {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ChangeComputerNameServlet.class.getSimpleName());

    private static final long serialVersionUID = -2789982280295259804L;

    private final UserDao userDao;

    private final ComputerDao computerDao;

    private final UserComputerDao userComputerDao;

    private final FileUploadDao fileUploadDao;

    private final FileDownloadDao fileDownloadDao;

    private final ClientSessionService clientSessionService;

    public ChangeComputerNameServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        fileUploadDao = new FileUploadDao(dbAccess);

        fileDownloadDao = new FileDownloadDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String userId = clientSession.getUserId();

            String clientLocale = clientSession.getLocale();

            // check json input
            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode computerIdNode = jsonNode.get(PropertyConstants.PROPERTY_NAME_COMPUTER_ID);

            JsonNode newComputerGroupNode = jsonNode.get("new-computer-group");

            JsonNode newComputerNameNode = jsonNode.get("new-computer-name");

            JsonNode localeNode = jsonNode.get(PropertyConstants.PROPERTY_NAME_LOCALE);

            if (localeNode != null && localeNode.textValue() != null) {
                clientLocale = localeNode.textValue();
            }

            if (computerIdNode == null || computerIdNode.asLong(-1) < 0
                || newComputerGroupNode == null || newComputerGroupNode.textValue() == null
                || newComputerNameNode == null || newComputerNameNode.textValue() == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                long computerId = computerIdNode.asLong(-1);

                final String newComputerGroup = newComputerGroupNode.textValue();

                final String newComputerName = newComputerNameNode.textValue();

                String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

                Computer computer = computerDao.findComputerById(computerId);

                UserComputer userComputer = userComputerDao.findUserComputerById(userComputerId);

                if (computer != null && userComputer != null) {
                    if (Computer.suppoertedGroupName(newComputerGroup)) {
                        // Use findComputerByNameForUser(userId, computerGroup, computerName) to check if computer name duplicated for the same user.

                        Computer newComputer = computerDao.findComputerByNameForUser(userId, newComputerGroup, newComputerName);

                        if (newComputer != null) {
                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "computer.duplicated");

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_CONFLICT);
                            resp.getWriter().write(errorMessage);
                            resp.getWriter().flush();
                        } else {
                            // make sure the user is the admin of the computer

                            if (!userId.equals(computer.getUserId())) {
                                User user = userDao.findUserById(userId);

                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.computer.admin", computer.getComputerName(), user.getNickname());

                                resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                resp.setStatus(Constants.HTTP_STATUS_USER_NOT_ADMIN);
                                resp.getWriter().write(errorMessage);
                                resp.getWriter().flush();
                            } else {
                                // update computer with group and name

                                computer.setGroupName(newComputerGroup);
                                computer.setComputerName(newComputerName);

                                computerDao.updateComputer(computer);

                                /* Encrypted value is composed from userId and computerId, and they all remain the same after the computer name changed,
                                 * so it is not necessary to update the value
                                 */
//                                final String encryptedUserComputerId = Utility.generateEncryptedUserComputerIdFrom(userId, computerId);
//                                userComputerDao.updateEncryptedUserComputerIdByComputerId(computerId, encryptedUserComputerId);

                                Utility.getExecutorService().execute(() -> {
                                    fileUploadDao.updateFileUploadComputerName(computerId, newComputerGroup, newComputerName);
                                    fileDownloadDao.updateFileDownloadComputerName(computerId, newComputerGroup, newComputerName);
                                });

                                // change computer name in desktop if socket connected

                                final String finalLocale = clientLocale;

                                Utility.getExecutorService().execute(() -> {
                                    try {
                                        ConnectSocket socket = ConnectSocket.getInstance(userComputerId);

                                        if (socket != null && socket.validate(true)) {
                                            // send message to server
                                            // don't wait for the response here

                                            RequestChangeComputerNameModel requestModel = new RequestChangeComputerNameModel(Sid.CHANGE_COMPUTER_NAME_V2, userId, finalLocale, newComputerGroup, newComputerName);
                                            String requestJson = mapper.writeValueAsString(requestModel);

                                            Session socketSession = socket.getSession();

                                            socketSession.getAsyncRemote().sendText(requestJson);
                                        } else {
                                            String message = String.format("ConnectSocket for computer '%d' not reachable for user: '%s', but the server will still change the name of the computer to: '%s' ", computerId, userId, newComputerName);
                                            LOGGER.info(message);
                                        }
                                    } catch (Exception e) {
                                        String errorMessage = String.format("Error on sending message to computer '%d' to change the name of the computer for user '%s'.\nBut the server will still change the name of the computer to '%s'.", computerId, userId, newComputerName);
                                        LOGGER.error(errorMessage, e);
                                    }
                                });

                                String responseString = mapper.writeValueAsString(computer);

                                resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                resp.setStatus(HttpServletResponse.SC_OK);
                                resp.getWriter().write(responseString);
                                resp.getWriter().flush();
                            }
                        }
                    } else {
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    }
                } else {
                    // user computer not found

                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "computer.not.found");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                }
            }

        } catch (Exception e) {
            LOGGER.error("Error on update computer information.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
