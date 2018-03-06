package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import org.apache.http.HttpHeaders;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.dao.DeviceTokenDao;
import org.clopuccino.dao.FileDownloadDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ClientDownloadResponseUtility;
import org.clopuccino.domain.CloseLatchAndDownloadResponse;
import org.clopuccino.service.NetworkService;
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
 * <code>UploadFileServlet</code> (V2) is the service for desktop to upload file to server.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "upload-file-from-desktop2", displayName = "upload-file-from-desktop2", description = "Upload file from desktop (V2)", urlPatterns = {"/directory/supload2"})
public class UploadFileServlet2 extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(String.valueOf(Sid.DOWNLOAD_FILE2));

    private static final long serialVersionUID = 328053229515502603L;

    private final FileDownloadDao fileDownloadDao;

    private final DeviceTokenDao deviceTokenDao;

    private final NetworkService networkService;

    public UploadFileServlet2() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        fileDownloadDao = new FileDownloadDao(dbAccess);

        deviceTokenDao = new DeviceTokenDao(dbAccess);

        networkService = new NetworkService();
    }

    // without saving to tmp file first and using stream pipeline instead.
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /* check authorizaiton header value */
        String downloadKey = req.getHeader(Constants.HTTP_HEADER_AUTHORIZATION_NAME);

        if (downloadKey == null || ClientDownloadResponseUtility.get(downloadKey) == null) {
            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Invalid client session!");
            resp.getWriter().flush();
        } else {
            try {
                CloseLatchAndDownloadResponse wrapper = ClientDownloadResponseUtility.get(downloadKey);

                HttpServletResponse clientResponse = wrapper.getResponse();

                if (!ClientDownloadResponseUtility.canBeUsed(clientResponse)) {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("Invalid client response!");
                    resp.getWriter().flush();
                } else {
                    String contentType = null;
                    int status = 0;
                    String message = null;

                    // Get device token sequence id from client response in order to increment badge number by 1.
                    Long deviceTokenSequenceId = null;

                    String deviceTokenSequenceIdString = clientResponse.getHeader("device-token-id");
                    if (deviceTokenSequenceIdString != null) {
                        try {
                            deviceTokenSequenceId = Long.parseLong(deviceTokenSequenceIdString);
                        } catch (Exception e) {
                            deviceTokenSequenceId = null;
                        }
                    }

                    try {
                        contentType = req.getContentType();
                        status = HttpServletResponse.SC_OK;
                        message = "success";

                        clientResponse.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

                        String contentLength = req.getHeader(HttpHeaders.CONTENT_LENGTH);

                        if (contentLength != null) {
                            clientResponse.setHeader(HttpHeaders.CONTENT_LENGTH, contentLength);
                        }

                        String lastModifiedInMillis = req.getHeader(Constants.HTTP_HEADER_NAME_FILE_LAST_MODIFIED);

                        if (lastModifiedInMillis != null) {
                            try {
                                Long lastModified = Long.parseLong(lastModifiedInMillis, 10);

                                clientResponse.setDateHeader(HttpHeaders.LAST_MODIFIED, lastModified);
                            } catch (NumberFormatException e) {
                                LOGGER.error("last modified is not a number: " + lastModifiedInMillis, e);
                            }
                        }

                        String contentRange = req.getHeader(Constants.HTTP_HEADER_NAME_FILE_CONTENT_RANGE);

                        if (contentRange != null) {
                            clientResponse.setHeader(HttpHeaders.CONTENT_RANGE, contentRange);

                            status = HttpServletResponse.SC_PARTIAL_CONTENT;

                            LOGGER.debug("Received partial content from desktop.\ndownload key: "
                                         + downloadKey + "\n"
                                         + HttpHeaders.CONTENT_RANGE + ": " + contentRange + "\n"
                                         + HttpHeaders.CONTENT_LENGTH + ": " + contentLength + "\n"
                                         + HttpHeaders.LAST_MODIFIED + ": " + lastModifiedInMillis
                                        );
                        } else {
                            LOGGER.debug("Received non-partial content from desktop.\ndownload key: "
                                         + downloadKey + "\n"
                                         + HttpHeaders.CONTENT_LENGTH + ": " + contentLength + "\n"
                                         + HttpHeaders.LAST_MODIFIED + ": " + lastModifiedInMillis
                                        );
                        }

                        String fromIp = networkService.getClientIpAddress(req);

                        String fromHost = networkService.getClientHostname(req);

                        fileDownloadDao.updateFileDownloadFromIpHost(downloadKey, fromIp, fromHost);

                        clientResponse.setStatus(status);
                        copyStreams(req.getInputStream(), clientResponse.getOutputStream(), Constants.DEFAULT_TRANSFER_FILE_BUFFER_SIZE_IN_BYTES);

                        try {
                            clientResponse.getOutputStream().flush();

                            fileDownloadDao.updateStatusProcessingToDesktopUploadedButUnconfirmed(downloadKey);
                        } catch (Exception e) {
                            /* ignored */
                        }
                    } catch (IOException e) {
                        contentType = Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8;
                        status = Constants.HTTP_STATUS_CLIENT_CLOSE_REQUEST;
                        message = "Request closed by user";
                    } catch (Exception e) {
                        contentType = Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8;
                        status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                        message = "Error on downloading file: " + e.getMessage();

                        LOGGER.error(message, e);

                        try {
                            clientResponse.getOutputStream().flush();
                        } catch (Exception e1) {
                            /* ignored */
                        }
                    } finally {
                        // increment badge number on matter device download file successfully or failed.
                        if (deviceTokenSequenceId != null) {
                            deviceTokenDao.incrementBadgeNumberBy(deviceTokenSequenceId, 1);
                        }

                        if (contentType != null) {
                            resp.setContentType(contentType);
                        }

                        resp.setStatus(status);

                        try {
                            resp.getWriter().write(message != null ? message : "");
                        } catch (Exception e) {
                            // ignored
                        }

                        try {
                            resp.getWriter().flush();
                        } catch (Exception e) {
                            // ignored
                        }

                        CountDownLatch closeLatch = wrapper.getCloseLatch();

                        if (closeLatch != null) {
                            closeLatch.countDown();
                        }
                    }
                }
            } finally {
                /* remove key-value pair from download map */
                ClientDownloadResponseUtility.remove(downloadKey);
            }
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
