package org.clopuccino.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.clopuccino.Utility;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;


/**
 * <code>SystemResourceTest</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class SystemResourceTest extends AbstractResourceTest {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SystemResourceTest.class.getSimpleName());

    // Do extends the super in this method
    @Before
    public void init() throws Exception {
        super.init();
    }

    @Test
    public void testFileSeparator() throws Exception {
        final String urlPath = "system/separator/file";

        HttpResponse response = doGet(urlPath, sessionId);

        int status = response.getStatusLine().getStatusCode();

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        if (status == 200) {
            LOGGER.info("Found file separator: " + responseString);
        } else {
            LOGGER.error("Error on finding file separator. Status=" + status + "; Message=" + responseString);
        }
    } // end testFileSeparator()

    @Test
    public void testPathSeparator() throws Exception {
        final String urlPath = "system/separator/path";

        HttpResponse response = doGet(urlPath, sessionId);

        int status = response.getStatusLine().getStatusCode();

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        if (status == 200) {
            LOGGER.info("Found path separator: " + responseString);
        } else {
            LOGGER.error("Error on finding path separator. Status=" + status + "; Message=" + responseString);
        }
    } // end Test()

    @Test
    public void testPingDesktop() throws Exception {
        final String urlPath = "system/dping";

        HttpResponse response = doGet(urlPath);

        int status = response.getStatusLine().getStatusCode();

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        if (status == 200) {
            LOGGER.info("Desktop connection OK");
        } else {
            LOGGER.error("Error on ping desktop. Status=" + status + "; Message=" + responseString);
        }
    } // end Test()

    @Test
    public void testUpdateShowHiddenValue() throws Exception {
        final Boolean showHidden = false;
        final Boolean logout = true;

        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("showHidden", showHidden.toString()));
        params.add(new BasicNameValuePair("logout", logout.toString()));

        HttpResponse response = doPost("system/show/hidden", null, params, sessionId);

        int status = response.getStatusLine().getStatusCode();

        switch (status) {
            case 200: // OK
                LOGGER.info("showHidden value updated successfully.");

                /* status must not be 401(unauthorized) */
                new DirectoryResourceTest().testList();

                break;
            default: // Error
                LOGGER.error("Status not expected, " + status);

                String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    }

    @Test
    public void testFindCountryByLocale() throws Exception {
        ObjectMapper mapper = Utility.createObjectMapper();

        ObjectNode rootNode = mapper.createObjectNode();

//        rootNode.put("ccode-locale", "en_UK");
//        rootNode.put("ccode-locale", "en_US");
//        rootNode.put("ccode-locale", "ja_JP");
        rootNode.put("ccode-locale", "ja_JP_JP");
//        rootNode.put("ccode-locale", "zh_HK");
//        rootNode.put("ccode-locale", "zh");
//        rootNode.put("ccode-locale", "zh_Hant");
//        rootNode.put("ccode-locale", "zh_Hans");
//        rootNode.put("ccode-locale", "zh_SG");
//        rootNode.put("ccode-locale", "zh_CN");
//        rootNode.put("ccode-locale", "zh_TW");

        String inputJson = mapper.writeValueAsString(rootNode);

        LOGGER.info("Input JSON:\n" + inputJson);

        HttpResponse response = doPostJson("system/country", inputJson);

        int status = response.getStatusLine().getStatusCode();

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Find country codes successfully.");

                LOGGER.info("Response JSON:\n" + responseString);

                break;
            default: // Error
                LOGGER.error("Status not expected, " + status);

                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    }

    @Test
    public void testUpdateAvailableCountryCache() throws Exception {
        ObjectMapper mapper = Utility.createObjectMapper();

        ObjectNode rootNode = mapper.createObjectNode();

//        rootNode.put("ccode-locale", "en_UK");
//        rootNode.put("ccode-locale", "en_US");
//        rootNode.put("ccode-locale", "ja_JP");
        rootNode.put("ccode-locale", "ja_JP_JP");
//        rootNode.put("ccode-locale", "zh_HK");
//        rootNode.put("ccode-locale", "zh");
//        rootNode.put("ccode-locale", "zh_Hant");
//        rootNode.put("ccode-locale", "zh_Hans");
//        rootNode.put("ccode-locale", "zh_SG");
//        rootNode.put("ccode-locale", "zh_CN");
//        rootNode.put("ccode-locale", "zh_TW");

        String inputJson = mapper.writeValueAsString(rootNode);

        LOGGER.info("Input JSON:\n" + inputJson);

        HttpResponse response = doPostJson(true, "system/update-country", inputJson, sessionId);

        int status = response.getStatusLine().getStatusCode();

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Find country codes successfully.");

                LOGGER.info("Response JSON:\n" + responseString);

                break;
            default: // Error
                LOGGER.error("Status not expected, " + status);

                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    }

//    @Test
//    public void testMailServerAddress() throws Exception {
//        String mailTo = "benius@gmail.com";
//
//        final WebTarget webTarget = target().path("system/ip/mail");
//
//        Response response;
//
//        Form form = new Form();
//        form.param("mailTo", mailTo);
//
//        response = webTarget.request().post(Entity.form(form));
//
////        response = webTarget.request(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(Entity.text(mailTo));
//
//        int status = response.getStatus();
//
//        switch (status) {
//            case 200: // OK
//                ServerAddress serverAddress = response.readEntity(ServerAddress.class);
//                LOGGER.info("Server Addresses Information: " + serverAddress.toString());
//                break;
//            case 400: // BAD_REQUEST, incorrect mail to address
//                String message = response.readEntity(String.class);
//                LOGGER.info("Status 400. Message: " + message);
//
//                break;
//            default:
//                LOGGER.error("Status not expected, " + status);
//
//                assertTrue("Status=" + status + ", Info=" + response.readEntity(String.class), status == 200);
//        }
//    }
//
//    @Test
//    public void testIPAddresses() throws Exception {
//        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
//
//        for (; networkInterfaces.hasMoreElements();) {
//            NetworkInterface networkInterface = networkInterfaces.nextElement();
//
//            String displayName = networkInterface.getDisplayName();
//            String name = networkInterface.getName();
//
//            System.out.println("---------- " + displayName + "(" + name + ") ----------");
//
//            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
//
//            for (; inetAddresses.hasMoreElements(); ) {
//                InetAddress inetAddress = inetAddresses.nextElement();
//
//                String ipString;
//                if (Inet6Address.class.isInstance(inetAddress)) {
//                    String ip = inetAddress.getHostAddress(); // There could be some ipv6 address like 'fe80:0:0:0:ca2a:14ff:fe38:a67d%4', 'fe80:0:0:0:0:0:0:1%1' --> remove '%' and the latters.
//
//                    int indexOfPercentageSignature = ip.indexOf("%");
//                    if (indexOfPercentageSignature > -1) {
//                        ip = ip.substring(0, indexOfPercentageSignature);
//                    }
//
//                    ipString = ip + "(IPv6)";
//                } else {
//                    ipString = inetAddress.getHostAddress();
//                }
//
//                System.out.println(ipString);
//            }
//
//            System.out.println("\n");
//        }
//    }
}
