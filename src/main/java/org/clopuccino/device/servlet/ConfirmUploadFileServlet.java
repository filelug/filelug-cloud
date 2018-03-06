package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.FileUploadDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
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
 * <code>ConfirmUploadFileServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "confirm-upload-file", displayName = "confirm-upload-file", description = "Confirm to the result of file uploading", urlPatterns = {"/directory/dcupload"})
public class ConfirmUploadFileServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ConfirmUploadFileServlet.class.getSimpleName());

    private static final long serialVersionUID = 4912691164277567381L;

    private final DatabaseAccess dbAccess;

    private final ClientSessionService clientSessionService;

    public ConfirmUploadFileServlet() {
        super();

        dbAccess = DatabaseUtility.createDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String clientLocale = clientSession.getLocale();

            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode transferKeyNode = jsonNode.get("transferKey");
            JsonNode transferStatusNode = jsonNode.get("status");

            if (transferKeyNode == null || transferKeyNode.textValue() == null) {
                        /* transfer key not found or invalid */
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "transfermation key");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (transferStatusNode == null || transferStatusNode.textValue() == null) {
                        /* status must not be empty */
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "status");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (!transferStatusNode.textValue().equals(DatabaseConstants.TRANSFER_STATUS_SUCCESS) && !transferStatusNode.textValue().equals(DatabaseConstants.TRANSFER_STATUS_FAILURE)) {
                        /* illegal status value */
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "illegal.transfer.status", transferStatusNode.textValue());

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                        /* update status of the upload file to db */
                final String transferKey = transferKeyNode.textValue();
                final String transferStatus = transferStatusNode.textValue();

                final FileUploadDao fileUploadDao = new FileUploadDao(dbAccess);

                fileUploadDao.updateFileUploadStatus(transferKey, transferStatus, System.currentTimeMillis());

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(transferKey);
                resp.getWriter().flush();
            }
        } catch (Exception e) {
            LOGGER.error("Error on confirming upload file(V1).", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
