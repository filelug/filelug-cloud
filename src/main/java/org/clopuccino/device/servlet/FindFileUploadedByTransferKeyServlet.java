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
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.FileUpload;
import org.clopuccino.domain.FileUploadModel;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <code>FindFileUploadedByTransferKeyServlet</code> find the file upload model with the specified transfer key.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "find-file-upload-data", displayName = "find-file-upload-data", description = "Find file uploaded data by the transfer key.", urlPatterns = {"/directory/find-dupload"})
public class FindFileUploadedByTransferKeyServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FindFileUploadedByTransferKeyServlet.class.getSimpleName());

    private static final long serialVersionUID = -926864809288554240L;

    private final ClientSessionService clientSessionService;

    private final FileUploadDao fileUploadDao;

    public FindFileUploadedByTransferKeyServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);

        fileUploadDao = new FileUploadDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);
            final String clientLocale = clientSession.getLocale();

            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode transferKeyNode = jsonNode.get("transferKey");

            if (transferKeyNode == null || transferKeyNode.textValue() == null) {
                        /* transfer key not found or invalid */
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "transfer key");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                // update status of the upload file to db

                final String inputTransferKey = transferKeyNode.textValue();

                FileUpload fileUpload = fileUploadDao.findFileUploadForUploadKey(inputTransferKey);

                if (fileUpload == null) {
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "failed.find.uploaded.files");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    Long transferredSize;
                    Long fileSize = fileUpload.getFileSize();
                    Long fileLastModifiedDate = fileUpload.getSourceFileLastModifiedTimestamp();

                    String tmpFilePath = fileUpload.getTmpFile();

                    File tmpFile = new File(tmpFilePath);

                    if (!tmpFile.exists()) {
                        // physical file not found

                        transferredSize = 0L;
                    } else {
                        long fileLength = tmpFile.length();

                        Long transferredByteIndexInDB = fileUpload.getTransferredByteIndex();

                        if (transferredByteIndexInDB != null && transferredByteIndexInDB + 1 == fileLength) {
                            transferredSize = fileLength;
                        } else {
                            // inconsistent, mark as 0 so to ask to upload file from start.

                            transferredSize = 0L;
                        }
                    }

                    FileUploadModel fileUploadModel = new FileUploadModel(inputTransferKey, transferredSize, fileSize, fileLastModifiedDate);

                    String responseJsonString = mapper.writeValueAsString(fileUploadModel);

                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(responseJsonString);
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on finding file upload by transfer key.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
