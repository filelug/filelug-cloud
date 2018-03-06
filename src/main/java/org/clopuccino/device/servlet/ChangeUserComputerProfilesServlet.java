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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * <code>ChangeUserComputerProfilesServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "change-user-computer-profiles", displayName = "change-user-computer-profiles", description = "Change the default properties of the current user computer", urlPatterns = {"/computer/ucprofiles"})
public class ChangeUserComputerProfilesServlet extends HttpServlet {

    private static final long serialVersionUID = 4127287245488446248L;

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ChangeUserComputerProfilesServlet.class.getSimpleName());

    private final UserComputerDao userComputerDao;

    private final ClientSessionService clientSessionService;

    public ChangeUserComputerProfilesServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userComputerDao = new UserComputerDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // validate client session id

            String clientSessionId = req.getHeader(Constants.HTTP_HEADER_FILELUG_SESSION_ID_NAME);

            if (clientSessionId == null || clientSessionId.trim().length() < 1) {
                clientSessionId = req.getHeader(Constants.HTTP_HEADER_AUTHORIZATION_NAME);
            }

            if (clientSessionId == null || clientSessionId.trim().length() < 1) {
                /* no client session found */
                String errorMessage = ClopuccinoMessages.getMessage("user.not.login");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                ClientSession clientSession = clientSessionService.findValidClientSessionById(clientSessionId, true);

                if (clientSession != null) {
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
                        // types of value could be String or Integer
                        Map<String, Object> updatedColumnValueMap = new HashMap<>();

                        Iterator<Map.Entry<String, JsonNode>> jsonNodeIterator = jsonNode.fields();

                        while(jsonNodeIterator.hasNext()) {
                            Map.Entry<String, JsonNode> entry = jsonNodeIterator.next();

                            String nodeName = entry.getKey();
                            JsonNode nodeValue = entry.getValue();

                            if (nodeValue != null) {
                                try {
                                    UserComputerProfile profile = new UserComputerProfile(nodeName);

                                    String columnName = profile.getColumnName();

                                    if (columnName != null) {
                                        Class type = profile.getType();

                                        if (type.equals(String.class)) {
                                            updatedColumnValueMap.put(columnName, nodeValue.textValue());
                                        } else if (type.equals(Integer.class)) {
                                            updatedColumnValueMap.put(columnName, nodeValue.asInt());
                                        } else if (type.equals(Long.class)) {
                                            updatedColumnValueMap.put(columnName, nodeValue.asLong());
                                        }
                                    }
                                } catch (Exception e) {
                                    // skip illegal property name
                                }
                            }
                        }

//                        // update upload-directory
//
//                        JsonNode uploadDirectoryNode = jsonNode.get("upload-directory");
//
//                        if (uploadDirectoryNode != null && uploadDirectoryNode.textValue() != null) {
//                            updatedColumnValueMap.put(DatabaseConstants.COLUMN_NAME_UPLOAD_DIRECTORY, uploadDirectoryNode.textValue());
//                        }
//
//                        // update-subdirectory-type
//
//                        JsonNode uploadSubdirectoryTypeNode = jsonNode.get("upload-subdirectory-type");
//
//                        if (uploadSubdirectoryTypeNode != null && uploadSubdirectoryTypeNode.isInt()) {
//                            updatedColumnValueMap.put(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_UPLOAD_SUBDIRECTORY_TYPE, uploadSubdirectoryTypeNode.asInt());
//                        }
//
//                        // upload-subdirectory-value
//                        JsonNode uploadSubdirectoryValueNode = jsonNode.get("upload-subdirectory-value");
//
//                        if (uploadSubdirectoryValueNode != null && uploadSubdirectoryValueNode.textValue() != null) {
//                            updatedColumnValueMap.put(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_UPLOAD_SUBDIRECTORY_VALUE, uploadSubdirectoryValueNode.textValue());
//                        }
//
//                        // upload-description-type
//                        JsonNode uploadDescriptionTypeNode = jsonNode.get("upload-description-type");
//
//                        if (uploadDescriptionTypeNode != null && uploadDescriptionTypeNode.isInt()) {
//                            updatedColumnValueMap.put(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_UPLOAD_DESCRIPTION_TYPE, uploadDescriptionTypeNode.asInt());
//                        }
//
//                        // upload-description-value
//                        JsonNode uploadDescriptionValueNode = jsonNode.get("upload-description-value");
//
//                        if (uploadDescriptionValueNode != null && uploadDescriptionValueNode.textValue() != null) {
//                            updatedColumnValueMap.put(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_UPLOAD_DESCRIPTION_VALUE, uploadDescriptionValueNode.textValue());
//                        }
//
//                        // upload-notification-type
//                        JsonNode uploadNotificationTypeNode = jsonNode.get("upload-notification-type");
//
//                        if (uploadNotificationTypeNode != null && uploadNotificationTypeNode.isInt()) {
//                            updatedColumnValueMap.put(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_UPLOAD_NOTIFICATION_TYPE, uploadNotificationTypeNode.asInt());
//                        }
//
//                        // download-directory
//
//                        JsonNode downloadDirectoryNode = jsonNode.get("download-directory");
//
//                        if (downloadDirectoryNode != null && downloadDirectoryNode.textValue() != null) {
//                            updatedColumnValueMap.put(DatabaseConstants.COLUMN_NAME_DOWNLOAD_DIRECTORY, downloadDirectoryNode.textValue());
//                        }
//
//                        // download-subdirectory-type
//
//                        JsonNode downloadSubdirectoryTypeNode = jsonNode.get("download-subdirectory-type");
//
//                        if (downloadSubdirectoryTypeNode != null && downloadSubdirectoryTypeNode.isInt()) {
//                            updatedColumnValueMap.put(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_TYPE, downloadSubdirectoryTypeNode.asInt());
//                        }
//
//                        // download-subdirectory-value
//
//                        JsonNode downloadSubdirectoryValueNode = jsonNode.get("download-subdirectory-value");
//
//                        if (downloadSubdirectoryValueNode != null && downloadSubdirectoryValueNode.textValue() != null) {
//                            updatedColumnValueMap.put(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_VALUE, downloadSubdirectoryValueNode.textValue());
//                        }
//
//                        // download-description-type
//
//                        JsonNode downloadDescriptionTypeNode = jsonNode.get("download-description-type");
//
//                        if (downloadDescriptionTypeNode != null && downloadDescriptionTypeNode.isInt()) {
//                            updatedColumnValueMap.put(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_DOWNLOAD_DESCRIPTION_TYPE, downloadDescriptionTypeNode.asInt());
//                        }
//
//                        // download-description-value
//
//                        JsonNode downloadDescriptionValueNode = jsonNode.get("download-description-value");
//
//                        if (downloadDescriptionValueNode != null && downloadDescriptionValueNode.textValue() != null) {
//                            updatedColumnValueMap.put(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_DOWNLOAD_DESCRIPTION_VALUE, downloadDescriptionValueNode.textValue());
//                        }
//
//                        // download-notification-type
//
//                        JsonNode downloadNotificationTypeNode = jsonNode.get("download-notification-type");
//
//                        if (downloadNotificationTypeNode != null && downloadNotificationTypeNode.isInt()) {
//                            updatedColumnValueMap.put(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_DOWNLOAD_NOTIFICATION_TYPE, downloadNotificationTypeNode.asInt());
//                        }

                        if (updatedColumnValueMap.size() > 0) {
                            // At least one needs to be updated
                            // update values to db and return

                            userComputerDao.updateUserComputerProfilesById(userComputer.getUserComputerId(), updatedColumnValueMap);
                        }

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write("OK");
                        resp.getWriter().flush();
                    }
                } else {
                    /* invalid client session */
                    String errorMessage = ClopuccinoMessages.localizedMessage(ClopuccinoMessages.DEFAULT_LOCALE, "invalid.session");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on changing user computer profiles.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
