package org.clopuccino;

import ch.qos.logback.classic.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class SSLTestCase {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SSLTestCase.class.getSimpleName());

    @Test
    public void testSSLConnection() throws Exception {
        String url = "https://developer.apple.com/app-store/marketing/guidelines/zh-tw/#messaging";
//        String url = "https://www.verisign.com/";
//        String url = "http://www.oracle.com/technetwork/java/javase/downloads/index.html";

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(url);

            System.out.println("Executing request " + httpget.getRequestLine());

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
        } finally {
            httpclient.close();
        }
    }

}
