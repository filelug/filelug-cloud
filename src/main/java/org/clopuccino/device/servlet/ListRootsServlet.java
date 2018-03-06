package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.dao.ComputerDao;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.dao.UserComputerPropertiesDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.PostgresqlDatabaseAccess;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.Computer;
import org.clopuccino.domain.RequestListRootsModel;
import org.clopuccino.domain.Version;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <code>ListRootsServlet</code> lists the data of roots and home directory of the specified computer
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "list-roots", displayName = "list-roots", description = "List All Bookmarks", urlPatterns = {"/directory/roots"})
public class ListRootsServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ListRootsServlet.class.getSimpleName());

    private static final long serialVersionUID = -5037933018157969989L;

    private final ClientSessionService clientSessionService;

    private final ComputerDao computerDao;

    private final UserComputerDao userComputerDao;

    private final UserComputerPropertiesDao userComputerPropertiesDao;


    public ListRootsServlet() {
        super();

        DatabaseAccess dbAccess = new PostgresqlDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        userComputerPropertiesDao = new UserComputerPropertiesDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            final String userId = clientSession.getUserId();

            final Long computerId = clientSession.getComputerId();

            String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

            String clientLocale = clientSession.getLocale();

            // validate desktop version

            String desktopVersion = userComputerPropertiesDao.findPropertyValue(userComputerId, org.clopuccino.PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);
//            // find the owner of the computer
//            String computerOwner = computerDao.findComputerOwnerById(computerId);
//
//            String ownerUserComputerId;
//
//            if (computerOwner != null && computerOwner.trim().length() > 0) {
//                ownerUserComputerId = Utility.generateUserComputerIdFrom(computerOwner, computerId);
//            } else {
//                ownerUserComputerId = userComputerId;
//            }
//
//            String desktopVersion = userComputerPropertiesDao.findPropertyValue(ownerUserComputerId, org.clopuccino.PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);

            if (desktopVersion != null && Version.valid(desktopVersion) && new Version(desktopVersion).compareTo(new Version(Constants.INITIAL_VERSION_TO_V2)) < 0) {
                // Incompatible with version of desktop that is lower than 2.0.0

                Computer computer = computerDao.findComputerById(computerId);

                String errorMessage;

                if (computer != null) {
                    errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "desktop.need.update", computer.getComputerName());
                } else {
                    errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "desktop.need.update3");
                }

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(Constants.HTTP_STATUS_DESKTOP_VERSION_TOO_OLD);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                // get socket by user and validate it
                ConnectSocket socket = ConnectSocket.getInstance(userComputerId);

                if (socket != null && socket.validate(true)) {
                    // send message to server

                    ObjectMapper mapper = Utility.createObjectMapper();

                    // Always show hidden or the drive C: will not return.
//                    JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));
//
//                    JsonNode showHiddenNode = jsonNode.get("showHidden");
//
//                    boolean showHidden;
//
//                    if (showHiddenNode != null && showHiddenNode.isBoolean()) {
//                        showHidden = showHiddenNode.asBoolean();
//                    } else {
//                        showHidden = clientSession.isShowHidden();
//                    }

                    RequestListRootsModel requestModel = new RequestListRootsModel(Sid.LIST_ALL_ROOT_DIRECTORIES_V2, userId, clientLocale, true);
                    String requestJson = mapper.writeValueAsString(requestModel);

                    socket.setHttpServletResponse(resp);
                    CountDownLatch closeLatch = new CountDownLatch(1);
                    socket.setCloseLatch(closeLatch);

                    Session socketSession = socket.getSession();

                    socketSession.getAsyncRemote().sendText(requestJson);

                    // current thread blocked until ConnectSocket using resp to response to the client
                    closeLatch.await(Constants.DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS, TimeUnit.SECONDS);

                    // normally, it is because of timeout to connect to desktop
                    if (!resp.isCommitted()) {
                        // update reconnect flag to true
                        userComputerDao.updateReconnectByUserComputerId(userComputerId, Boolean.TRUE);

                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "desktop.connect.timeout.try.again");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    }
                } else {
                    // update reconnect flag to true
                    userComputerDao.updateReconnectByUserComputerId(userComputerId, Boolean.TRUE);

                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "server.not.connected");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on listing directory roots.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
