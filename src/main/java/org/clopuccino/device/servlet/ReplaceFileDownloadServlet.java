package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.FileDownloadDao;
import org.clopuccino.dao.FileDownloadGroupDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.FileDownloadGroupDetail;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <code>ReplaceFileDownloadServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "replace-file-download-data", displayName = "replace-file-download-data", description = "Replace old file download data with the new one.", urlPatterns = {"/directory/replace-download"})
public class ReplaceFileDownloadServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ReplaceFileDownloadServlet.class.getSimpleName());

    private static final long serialVersionUID = 5972104613310064864L;

    private final ClientSessionService clientSessionService;

    private final FileDownloadDao fileDownloadDao;

    private final FileDownloadGroupDao fileDownloadGroupDao;

    public ReplaceFileDownloadServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);

        fileDownloadDao = new FileDownloadDao(dbAccess);

        fileDownloadGroupDao = new FileDownloadGroupDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            final String clientLocale = clientSession.getLocale();

            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode oldTransferKeyNode = jsonNode.get("old-transferKey");

            JsonNode newTransferKeyNode = jsonNode.get("new-transferKey");

            if (oldTransferKeyNode == null || oldTransferKeyNode.textValue() == null) {
                        /* old transfer key not found or invalid */

                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "old transfer key");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (newTransferKeyNode == null || newTransferKeyNode.textValue() == null) {
                        /* new transfer key not found or invalid */

                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "new transfer key");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            }  else {
                // update status of the download file to db

                final String oldTransferKey = oldTransferKeyNode.textValue();

                // Check if the download key exists in file_download_group_detail,
                // instead of file_downloaded, because download key not exists in
                // file_downloaded unless all bytes of the file downloaded successfully.
                FileDownloadGroupDetail fileDownloadGroupDetail = fileDownloadGroupDao.findFileDownloadGroupDetailByDownloadKey(oldTransferKey);

                if (fileDownloadGroupDetail == null) {
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "failed.find.downloaded.files");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    final String fileDownloadGroupDetailId = fileDownloadGroupDetail.getDownloadGroupDetailId();
                    final String fileDownloadGroupId = fileDownloadGroupDetail.getDownloadGroupId();
                    final String newTransferKey = newTransferKeyNode.textValue();
                    final String newFilePath = fileDownloadGroupDetail.getFilePath();

                    fileDownloadGroupDao.createFileDownloadGroupDetail(fileDownloadGroupId, newTransferKey, newFilePath);

                    // delete old record in file_downloaded and file_download_group_detail
                    // it's possible that old record in file_download_group_detail but not in file_downloaded
                    // because last time the file failed to download from device to server.

                    fileDownloadGroupDao.deleteFileDownloadGroupDetailById(fileDownloadGroupDetailId);
                    fileDownloadDao.deleteFileDownloadedForDownloadKey(oldTransferKey);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("success");
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on replacing file download.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
