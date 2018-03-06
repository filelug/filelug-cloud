package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.ComputerDao;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.Computer;
import org.clopuccino.domain.UserComputer;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <code>RequestConnectServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "request-connect", displayName = "request-connect", description = "Request connection between repository and server", urlPatterns = {"/user/reconnect"})
public class RequestConnectServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(RequestConnectServlet.class.getSimpleName());

    private static final long serialVersionUID = -7442906642164049194L;

    private final UserComputerDao userComputerDao;

    private final ComputerDao computerDao;

    private final ClientSessionService clientSessionService;


    public RequestConnectServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userComputerDao = new UserComputerDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            final String userId = clientSession.getUserId();

            final Long computerId = clientSession.getComputerId();
            String clientLocale = clientSession.getLocale();

            Computer computer = computerDao.findComputerById(computerId);

            if (computer == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "computer.not.found");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(Constants.HTTP_STATUS_COMPUTER_NOT_FOUND);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

                Boolean reconnect = userComputerDao.findReconnectById(userComputerId);

                if (reconnect == null) {
                    String responseMessage = ClopuccinoMessages.localizedMessage(clientLocale, "computer.not.found");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(responseMessage);
                    resp.getWriter().flush();
                } else {
                        /* reconnect flag alrady set when repository found socket not connected on other services,
                         * so it is forbidden to set reconnect again to prevent newly connected socket be closed again
                         */
//                        if (!reconnect) {
//                            userComputerDao.updateReconnect(userComputerId, true);
//                        }

                    checkNewConnectSocketRecursive(userId, computerId, userComputerId, clientLocale, resp, 1);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on requesting connection.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }

    private void checkNewConnectSocketRecursive(String userId, Long computerId, String userComputerId, String clientLocale, HttpServletResponse resp, int nInvoke) throws Exception {
        LOGGER.debug(String.format("[%d]Wait %d millis to check new connection from desktop for user: %s and user computer id: %s", nInvoke, Constants.CHECK_CONNECT_INTERVAL_IN_MILLIS, userId, userComputerId));

//        describeInvokeTime(userId, computerGroup, computerName, nInvoke);

        try {
            Thread.sleep(Constants.CHECK_CONNECT_INTERVAL_IN_MILLIS);
        } catch (Exception e) {
            /* ignored */
        }

        LOGGER.debug("Start checking new connection from user: " + userId + ", user computer id: " + userComputerId);

        /* 確認 server 已經建立連線 */
        UserComputer userComputer = userComputerDao.findUserComputerById(userComputerId);

        Boolean socketConnected = userComputer.isSocketConnected();

        if (socketConnected != null && socketConnected) {
            LOGGER.debug("New connection found between repository and server for user: " + userId + ", user computer id: " + userComputerId);

            String lugServerId = userComputer.getLugServerId();

            UserComputer returnedUserComputer = new UserComputer(userComputerId, userId, computerId, userComputer.getComputerAdminId(), userComputer.getGroupName(), userComputer.getComputerName(), null, lugServerId, null, null, null,
                                                                 userComputer.getUploadDirectory(),
                                                                 userComputer.getUploadSubdirectoryType(), userComputer.getUploadSubdirectoryValue(), userComputer.getUploadDescriptionType(), userComputer.getUploadDescriptionValue(), userComputer.getUploadNotificationType(),
                                                                 userComputer.getDownloadDirectory(),
                                                                 userComputer.getDownloadSubdirectoryType(), userComputer.getDownloadSubdirectoryValue(), userComputer.getDownloadDescriptionType(), userComputer.getDownloadDescriptionValue(), userComputer.getDownloadNotificationType());

            ObjectMapper mapper = Utility.createObjectMapper();
            String responseString = mapper.writeValueAsString(returnedUserComputer);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(responseString);
            resp.getWriter().flush();
        } else {
            if (nInvoke < Constants.CHECK_CONNECT_TIMES) {
                LOGGER.debug("Connection not found between repository and server for user: " + userId  + ", user computer id: " + userComputerId);

                checkNewConnectSocketRecursive(userId, computerId, userComputerId, clientLocale, resp, ++nInvoke);
            } else {
                /* when nInvoke == Constants.CHECK_CONNECT_TIMES and still not found server session,
                 * returns SC_SERVICE_UNAVAILABLE
                 * after we set true to the proeprty needReconnect of the user computer
                 */

                LOGGER.debug("Give up trying to find new connection between repository and server for user: " + userId + ", user computer id: " + userComputerId);

                Boolean reconnect = userComputer.isNeedReconnect();

                if (reconnect == null || !reconnect) {
                    /* update reconnect flag to true */
                    userComputerDao.updateReconnectByUserComputerId(userComputerId, Boolean.TRUE);
                }

                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "server.not.connected");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            }
        }
    }

//    private void describeInvokeTime(String userId, String computerGroup, String computerName, int nInvoke) {
//        if (LOGGER.isDebugEnabled()) {
//            String nInvokeDesc;
//
//            switch (nInvoke) {
//                case 1:
//                    nInvokeDesc = "first time";
//                    break;
//                case 2:
//                    nInvokeDesc = "second time";
//                    break;
//                case 3:
//                    nInvokeDesc = "third time";
//                    break;
//                case 4:
//                    nInvokeDesc = "fourth time";
//                    break;
//                case 5:
//                    nInvokeDesc = "fifth time";
//                    break;
//                default:
//                    nInvokeDesc = "unknown time";
//            }
//
//            LOGGER.debug("Wait to check new connection from user: " + userId + ", computer group: " + computerGroup + ", computer name: " + computerName + " for " + nInvokeDesc);
//        }
//    }

}
