package org.clopuccino.domain;

import ch.qos.logback.classic.Logger;
import org.clopuccino.Utility;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.Hashtable;

/**
 * <code>ClientDownloadResponseUtility</code> handles client session with response for download-servlet-response
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ClientDownloadResponseUtility {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("DOWN_RESP");

    // key=download key sent from device's request to DownloadFileServlet(path: /directory/ddownload),
    // value=response object for download servlet and CountDownLatch from the servlet receiving request from device.
    private static final Hashtable<String, CloseLatchAndDownloadResponse> clientDownloadResponses = new Hashtable<>();

    public static void put(String key, CloseLatchAndDownloadResponse response) {
        clientDownloadResponses.put(key, response);
    }

    public static CloseLatchAndDownloadResponse get(String key) {
        return clientDownloadResponses.get(key);
    }

    public static void remove(String key) {
        clientDownloadResponses.remove(key);
    }

    public static String generateKey(String clientSessionId, String filePath) {
        String downloadKey = null;

        try {
            downloadKey = Utility.encodeUsingBase64(clientSessionId + "+" + System.currentTimeMillis(), "UTF-8");

            if (clientDownloadResponses.containsKey(downloadKey)) {
                downloadKey = generateKey(clientSessionId, filePath);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to generate upload key from client session id: " + clientSessionId + "\nfilePath: " + filePath + "\nerror:\n" + e.getMessage());
        }

        return downloadKey;
    }

    public static boolean canBeUsed(HttpServletResponse response) {
        return response != null && !response.isCommitted();
    }

    public static Hashtable<String, CloseLatchAndDownloadResponse> getClientDownloadResponses() {
        return clientDownloadResponses;
    }
}
