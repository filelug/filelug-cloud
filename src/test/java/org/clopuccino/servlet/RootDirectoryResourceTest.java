package org.clopuccino.servlet;

import ch.qos.logback.classic.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * <code>RootDirectoryResourceTest</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class RootDirectoryResourceTest extends AbstractResourceTest {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(RootDirectoryResourceTest.class.getSimpleName());

    // Do extends the super in this method
    @Before
    public void init() throws Exception {
        super.init();
    }

    @Test
    public void testListAllRootDirectories() throws Exception {
        final String urlPath = "rootDirectories";

        HttpResponse response = doGet(urlPath, sessionId);

        int status = response.getStatusLine().getStatusCode();

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        if (status == 200) {
            LOGGER.info("Found root directory: " + responseString);
        } else {
            LOGGER.error("Error on finding all root directories. Status=" + status + "; Message=" + responseString);
        }
    }
}
