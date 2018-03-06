package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import org.apache.http.HttpHeaders;
import org.clopuccino.Constants;
import org.clopuccino.domain.ClientUploadRequestUtility;
import org.clopuccino.domain.CloseLatchAndUploadRequest;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

/**
 * <code>DownloadFileServlet</code> called by the server to download file from the file upload request of device to server
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "cancel-server-download-file", displayName = "cancel-server-download-file", description = "Cancel currently running file-downloading requested by server", urlPatterns = {"/directory/sdcancel"})
public class CancelServerDownloadFileServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CancelServerDownloadFileServlet.class.getSimpleName());

    private static final long serialVersionUID = -6048454060793763726L;


    public CancelServerDownloadFileServlet() {
        super();
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
                String respMessage = "success";

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

                    copyStreams(clientRequest.getInputStream(), resp.getOutputStream(), Constants.DEFAULT_TRANSFER_FILE_BUFFER_SIZE_IN_BYTES);

                    try {
                        resp.getOutputStream().flush();
                    } catch (Exception e) {
                        /* ignored */
                    }
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

    private void copyStreams(final InputStream in, final OutputStream out, final int buffersize) throws IOException {
        // create a 4kbyte buffer to read the file
        final byte[] bytes = new byte[buffersize];

        // the input stream does not supply accurate available() data
        // the zip entry does not know the size of the data
        int bytesRead = in.read(bytes);
        while (bytesRead > -1) {
            out.write(bytes, 0, bytesRead);
            bytesRead = in.read(bytes);
        }
    }
}
