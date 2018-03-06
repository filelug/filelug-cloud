package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import org.apache.http.HttpHeaders;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.dao.FileUploadDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.FileUpload;
import org.clopuccino.domain.HierarchicalModel;
import org.clopuccino.service.NetworkService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * <code>DownloadFileServlet3</code> does not count down CloseLatch when file is transfered to the desktop successfully.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "download-file-to-desktop3", asyncSupported = true, displayName = "download-file-to-desktop3", description = "Download file to desktop(V3)", urlPatterns = {"/directory/sdownload3"})
public class DownloadFileToDesktopServlet3 extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DownloadFileToDesktopServlet3.class.getSimpleName());

    private static final long serialVersionUID = 6621001426051080017L;

    private final FileUploadDao fileUploadDao;

    private final NetworkService networkService;


    public DownloadFileToDesktopServlet3() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        fileUploadDao = new FileUploadDao(dbAccess);

        networkService = new NetworkService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /* check authorizaiton header value */
        final String transferKey = req.getParameter(Constants.HTTP_PARAM_NAME_TRANSFER_KEY);

        if (transferKey == null) {
            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Missing essential parameters.");
            resp.getWriter().flush();
        } else {
            try {
                FileUpload fileUpload = fileUploadDao.findFileUploadForUploadKey(transferKey);

                if (fileUpload == null) {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("No such upload record found.");
                    resp.getWriter().flush();
                } else {
                    String tmpFileAbsolutePath = fileUpload.getTmpFile();

                    boolean validTmpFile = false;

                    if ((tmpFileAbsolutePath != null && tmpFileAbsolutePath.trim().length() > 0)) {
                        File tmpFile = new File(tmpFileAbsolutePath);

                        if (tmpFile.exists() && tmpFile.isFile() && tmpFile.canRead()) {
                            validTmpFile = true;
                        }
                    }

                    if (!validTmpFile) {
                        LOGGER.error("Failed to download file \"" + tmpFileAbsolutePath + "\" from server to desktop because it does not exist or permission denied.");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write("File not exists or permission denied.");
                        resp.getWriter().flush();
                    } else {
                        File tmpFile = new File(tmpFileAbsolutePath);

                        // content length
                        String contentLength = String.valueOf(tmpFile.length());

                        resp.setHeader(HttpHeaders.CONTENT_LENGTH, contentLength);

                        LOGGER.debug(HttpHeaders.CONTENT_LENGTH + "=" + contentLength);

                        // content type
                        String contentType = HierarchicalModel.prepareContentType(tmpFile);

                        // content type - not working
//                        Path tmpFilePath = Paths.get(tmpFile.toURI());
//                        String contentType = Files.probeContentType(tmpFilePath);

                        resp.setHeader(HttpHeaders.CONTENT_TYPE, contentType);

                        LOGGER.debug(HttpHeaders.CONTENT_TYPE, contentType);

                        String fromIp = networkService.getClientIpAddress(req);

                        String fromHost = networkService.getClientHostname(req);

                        fileUploadDao.updateFileUploadToIpHost(transferKey, fromIp, fromHost);

                        ServletOutputStream servletOutputStream = resp.getOutputStream();

                        Files.copy(Paths.get(tmpFile.toURI()), servletOutputStream);

                        servletOutputStream.flush();

                        LOGGER.debug("File downloaded successfully from server to desktop. Transfer key: {}", transferKey);
                    }
                }
            } catch (Exception e) {
                int errorStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                String message = e.getMessage() != null ? e.getMessage() : "";

                LOGGER.error("Error on downloading file from server to desktop.\n" + message, e);

                try {
                    resp.sendError(errorStatus, message);
                } catch (Exception e2) {
                    LOGGER.error("Failed to response to desktop after failed to upload file from device.\n" + e2.getMessage(), e2);
                }

            }
        }
    }
}
