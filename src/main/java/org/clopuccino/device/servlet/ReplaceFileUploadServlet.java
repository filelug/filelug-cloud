package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.FileUploadDao;
import org.clopuccino.dao.FileUploadGroupDao;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.*;
import org.clopuccino.server.servlet.websocket.ConnectSocket;
import org.clopuccino.server.servlet.Sid;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <code>ReplaceFileUploadServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "replace-file-upload-data", displayName = "replace-file-upload-data", description = "Replace old file upload data with the new one.", urlPatterns = {"/directory/replace-upload"})
public class ReplaceFileUploadServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ReplaceFileUploadServlet.class.getSimpleName());

    private static final long serialVersionUID = 1826708814934895049L;

    private final ClientSessionService clientSessionService;

    private final FileUploadDao fileUploadDao;

    private final FileUploadGroupDao fileUploadGroupDao;

    private final UserComputerDao userComputerDao;

    public ReplaceFileUploadServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);

        fileUploadDao = new FileUploadDao(dbAccess);

        fileUploadGroupDao = new FileUploadGroupDao(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            final String clientSessionId = clientSession.getSessionId();

            final String userId = clientSession.getUserId();

            final Long computerId = clientSession.getComputerId();

            final String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

            final String clientLocale = clientSession.getLocale();

            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode oldTransferKeyNode = jsonNode.get("old-transferKey");

            JsonNode newTransferKeyNode = jsonNode.get("new-transferKey");

            if (oldTransferKeyNode == null || oldTransferKeyNode.textValue() == null) {
                // transfer key not found or invalid
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "old transfer key");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (newTransferKeyNode == null || newTransferKeyNode.textValue() == null) {
                // transfer key not found or invalid
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "new transfer key");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            }  else {
                // update status of the upload file to db

                final String oldTransferKey = oldTransferKeyNode.textValue();

                // Check if the upload key exists in file_upload_group_detail,
                // instead of file_uploaded, because upload key not exists in
                // file_uploaded unless all bytes of the file uploaded successfully.
                boolean found = fileUploadGroupDao.existingFileUploadGroupDetailForUploadKey(oldTransferKey);

                if (!found) {
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "failed.find.uploaded.files");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    ConnectSocket socket = ConnectSocket.getInstance(userComputerId);

                    if (socket != null && socket.validate(true)) {
                        // If fileUploadGroupId found in file_upload_group_detail by old record,
                        // create new record in file_upload_group_detail.

                        String fileUploadGroupId = fileUploadGroupDao.findFileUploadGroupIdByUploadKey(oldTransferKey);

                        if (fileUploadGroupId != null && fileUploadGroupId.trim().length() > 0) {
                            final String newTransferKey = newTransferKeyNode.textValue();

                            fileUploadGroupDao.createFileUploadGroupDetailIfNotExists(fileUploadGroupId, newTransferKey);

                            // delete old record in file_uploaded and file_upload_group_detail
                            // it's possible that old record in file_upload_group_detail but not in file_uploaded
                            // because last time the file failed to upload from device to server.

                            fileUploadGroupDao.deleteFileUploadGroupDetailForUploadKey(oldTransferKey);
                            fileUploadDao.deleteFileUploadedForUploadKey(oldTransferKey);
                        }

                        // Request to delete data in desktop

                        RequestDeleteFileUploadModel requestModel = new RequestDeleteFileUploadModel(Sid.DELETE_UPLOAD_FILE, userId, oldTransferKey, clientSessionId, clientLocale);

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
            }
        } catch (Exception e) {
            LOGGER.error("Error on replacing file upload.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
