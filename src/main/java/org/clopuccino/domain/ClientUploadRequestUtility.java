package org.clopuccino.domain;

import ch.qos.logback.classic.Logger;
import org.clopuccino.Utility;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <code>ClientUploadRequestUtility</code> handles client session with request and response for upload servlet
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ClientUploadRequestUtility {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("UP_REQ_RESP");

    // key=client session id + Utility.realUrlEncode(directory) + Utility.realUrlEncode(filename) + curent millis, value=request and response objects for upload servlet and CountDownLatch from the servlet receiving request from device.
    private static final ConcurrentHashMap<String, CloseLatchAndUploadRequest> clientUploadRequests = new ConcurrentHashMap<>();
//    private static final Hashtable<String, CloseLatchAndUploadRequest> clientUploadRequests = new Hashtable<>();

    public static void put(String key, CloseLatchAndUploadRequest request) {
        clientUploadRequests.put(key, request);
    }

    public static CloseLatchAndUploadRequest get(String key) {
        return clientUploadRequests.get(key);
    }

    public static void remove(String key) {
        clientUploadRequests.remove(key);
    }

    /**
     * @return null if error occurred.
     */
    public static String generateKey(String clientSessionId, String directory, String filename) {
        String uploadKey = null;

        try {
            uploadKey = Utility.encodeUsingBase64(clientSessionId + "+" + System.currentTimeMillis(), "UTF-8");

            if (clientUploadRequests.containsKey(uploadKey)) {
                uploadKey = generateKey(clientSessionId, directory, filename);
            }

//            uploadKey = Utility.encodeUsingBase64(clientSessionId + "+" + Utility.realUrlEncode(directory) + "+" + Utility.realUrlEncode(filename) + "+" + System.currentTimeMillis(), "UTF-8");
        } catch (Exception e) {
            LOGGER.error("Failed to generate upload key from client session id: " + clientSessionId + "\ndirectory: " + directory + "\nfilename: " + filename + "\nerror:\n" + e.getMessage());
        }

        return uploadKey;
    }

    public static boolean canBeUsed(HttpServletResponse response) {
        return response != null && !response.isCommitted();
    }

    public static ConcurrentHashMap<String, CloseLatchAndUploadRequest> getClientUploadRequests() {
        return clientUploadRequests;
    }
}
