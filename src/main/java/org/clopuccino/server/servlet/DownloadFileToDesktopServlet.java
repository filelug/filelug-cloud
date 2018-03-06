package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import com.google.common.io.ByteStreams;
import org.apache.http.HttpHeaders;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ClientUploadRequestUtility;
import org.clopuccino.domain.CloseLatchAndUploadRequest;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * <code>DownloadFileServlet</code> called by the desktop to download file from the file upload request of device to server.
 * CloseLatch will be count down even if file is transfer to the desktop successfully.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "download-file-to-server", displayName = "download-file-to-server", description = "Download file to server", urlPatterns = {"/directory/sdownload"})
public class DownloadFileToDesktopServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DownloadFileToDesktopServlet.class.getSimpleName());

    private static final long serialVersionUID = 6139877802376076721L;

    public DownloadFileToDesktopServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /* check authorizaiton header value */
        String transferKey = req.getHeader(Constants.HTTP_HEADER_AUTHORIZATION_NAME);

        if (transferKey == null || ClientUploadRequestUtility.get(transferKey) == null) {
            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Invalid client session!");
            resp.getWriter().flush();
        } else {
            CloseLatchAndUploadRequest wrapper = ClientUploadRequestUtility.get(transferKey);

            HttpServletResponse clientResponse = wrapper.getResponse();

            HttpServletRequest clientRequest = wrapper.getRequest();

            if (!ClientUploadRequestUtility.canBeUsed(clientResponse)) {
                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("Invalid client response!");
                resp.getWriter().flush();
            } else if (clientRequest == null) {
                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("Invalid client request!");
                resp.getWriter().flush();
            } else {
                int status = HttpServletResponse.SC_OK;

                // return transfer key on success, e.g. response status: 200
                String respMessage = transferKey;

                try {
                    /* content length */
                    String contentLength = clientRequest.getHeader(HttpHeaders.CONTENT_LENGTH);

                    /* DEBUG */
                    LOGGER.debug(HttpHeaders.CONTENT_LENGTH + "=" + contentLength);

                    if (contentLength != null) {
                        resp.setHeader(HttpHeaders.CONTENT_LENGTH, contentLength);
                    }

                    /* content type */
                    String contentType = clientRequest.getHeader(HttpHeaders.CONTENT_TYPE);

                    /* DEBUG */
                    LOGGER.debug(HttpHeaders.CONTENT_TYPE, contentType);

                    if (contentType != null) {
                        resp.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
                    } else {
                        /* TODO: make sure it is correct */
                        resp.setHeader(HttpHeaders.CONTENT_TYPE, Constants.CONTENT_TYPE_DEFAULT_UPLOAD_FILE_TO_SERVER);
                    }

                    /* status */
                    resp.setStatus(HttpServletResponse.SC_OK);

                    ByteStreams.copy(clientRequest.getInputStream(), resp.getOutputStream());

                    resp.getOutputStream().flush();
                } catch (IOException e) {
                    status = Constants.HTTP_STATUS_CLIENT_CLOSE_REQUEST;
                    respMessage = "Request closed by user";
                } catch (Exception e) {
                    status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    respMessage = e.getMessage() != null ? e.getMessage() : "";
                } finally {
                    clientResponse.setStatus(status);
                    clientResponse.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    clientResponse.getWriter().write(respMessage);
                    clientResponse.getWriter().flush();

                    CountDownLatch closeLatch = wrapper.getCloseLatch();

                    if (closeLatch != null) {
                        closeLatch.countDown();
                    }
                }
            }

            /* remove key-value pair from upload map */
            ClientUploadRequestUtility.remove(transferKey);
        }
    }
}
