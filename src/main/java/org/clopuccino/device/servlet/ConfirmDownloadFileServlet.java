package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.FileDownloadDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.FileDownload;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <code>ConfirmDownloadFileServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "confirm-download-file", displayName = "confirm-download-file", description = "Confirm to the result of file download", urlPatterns = {"/directory/dcdownload"})
public class ConfirmDownloadFileServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ConfirmDownloadFileServlet.class.getSimpleName());

    private static final long serialVersionUID = 1592257642816217607L;

    private final DatabaseAccess dbAccess;

    private final ClientSessionService clientSessionService;

    public ConfirmDownloadFileServlet() {
        super();

        dbAccess = DatabaseUtility.createDatabaseAccess();

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

            JsonNode transferKeyNode = jsonNode.get("transferKey");
            JsonNode transferStatusNode = jsonNode.get("status");
            JsonNode fileSizeNode = jsonNode.get("fileSize");

            if (transferKeyNode == null || transferKeyNode.textValue() == null) {
                // transfer key not found or invalid
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "transfermation key");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (transferStatusNode == null || transferStatusNode.textValue() == null) {
                // status must not be empty
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "status");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (!transferStatusNode.textValue().equals(DatabaseConstants.TRANSFER_STATUS_SUCCESS) && !transferStatusNode.textValue().equals(DatabaseConstants.TRANSFER_STATUS_FAILURE)) {
                // illegal status value

                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "illegal.transfer.status", transferStatusNode.textValue());

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (fileSizeNode == null || fileSizeNode.longValue() < 0L) { // there's possible that file size is zero
                // fileSize must not be empty

                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "file size");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                // update status of the upload file to db and delete download tmp file if exists.

                final String transferKey = transferKeyNode.textValue();
                final String transferStatus = transferStatusNode.textValue();
                final long fileSize = fileSizeNode.longValue();

                FileDownloadDao fileDownloadDao = new FileDownloadDao(dbAccess);

                FileDownload fileDownload = fileDownloadDao.findFileDownloadForDownloadKey(transferKey);

                if (fileDownload != null) {
                    try {
                        fileDownloadDao.updateFileDownloadStatus(transferKey, transferStatus, System.currentTimeMillis(), fileSize);

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write(transferKey);
                        resp.getWriter().flush();
                    } finally {
                        Utility.deleteDownloadTmpFileAndUpdateRecord(fileDownload.getTmpFile(), fileDownloadDao, transferKey);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on confirming download file.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
