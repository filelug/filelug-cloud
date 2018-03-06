package org.clopuccino.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.clopuccino.Utility;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

import static org.junit.Assert.assertTrue;

/**
 * <code>ProductResourceTest</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ProductResourceTest extends AbstractResourceTest {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ProductResourceTest.class.getSimpleName());

    // Do extends the super in this method
    @Before
    public void init() throws Exception {
        super.init();
    }


    @Test
    public void testFindProductById() throws Exception {
        final String vendor = "apple";
        
        final String urlPath = "product/findByVendor";

        ObjectMapper mapper = Utility.createObjectMapper();

        ObjectNode inputRootNode = mapper.createObjectNode();
        inputRootNode.put("vendor", vendor);

        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputRootNode);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        HttpResponse response = doPostJson(urlPath, inputJson, sessionId);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        if (status == 200) {
            LOGGER.info("Found product: " + responseString);
        } else {
            LOGGER.error("Error on finding product by vendor: " + vendor + "; Status=" + status + "; Message=" + responseString);

            assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    } // end testFindProductById()

    
}
