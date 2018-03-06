package org.clopuccino.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.clopuccino.Utility;
import org.clopuccino.domain.ComputerModel;
import org.clopuccino.domain.ConnectModel;
import org.clopuccino.domain.UserComputer;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;


/**
 * <code>ComputerResourceTest</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ComputerResourceTest extends AbstractResourceTest {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ComputerResourceTest.class.getSimpleName());

    // Do extends the super in this method
    @Before
    public void init() throws Exception {
        super.init();
    }

    @Test
    public void testCreateComputer() throws Exception {
        String userId = "9aaa3acb2bf8aa13533040cc4b24cec22089eba94b4d1c11f3a520656abdab5305b5b3f905b994ea10a24c16783fc340";
        String encryptedPassword = DigestUtils.sha256Hex(passwd);

        final ComputerModel connectModel = new ComputerModel();
        connectModel.setAccount(userId);
        connectModel.setPassword(encryptedPassword);
        connectModel.setNickname(nickname);
        connectModel.setVerification(Utility.generateVerification(userId, encryptedPassword, nickname));
        connectModel.setLocale(userLocale);
        connectModel.setGroupName(computerGroup);
        connectModel.setComputerName(computerName);

        ObjectMapper mapper = Utility.createObjectMapper();
        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(connectModel);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        String path = "computer/create";

        HttpResponse response = doPostJson(path, inputJson);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Response Message=\n" + responseString);

                break;
            case 400: // BAD_REQUEST, necessary parameters not provided
                LOGGER.info("Status 400. Message: " + responseString);

                break;
            case 401: // incorrect password
                LOGGER.info("Status 401. Message: " + responseString);

                break;
            case 403: // user not registered
                LOGGER.info("Status 403. Message: " + responseString);

                break;
            default:
                LOGGER.error("Status not expected, " + status);

                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    }

    @Test
    public void testFindAvailableComputers() throws Exception {
        String path = "computer/available";

        String userId = "9aaa3acb2bf8aa13533040cc4b24cec22089eba94b4d1c11f3a520656abdab5305b5b3f905b994ea10a24c16783fc340";

        String encryptedPassword = DigestUtils.sha256Hex(passwd);

        ConnectModel connectModel = new ConnectModel();
//        connectModel.setAccount(userId);
        connectModel.setCountryId(countryId);
        connectModel.setPhoneNumber(phoneNumber);
        connectModel.setPassword(encryptedPassword);
        connectModel.setNickname(nickname);
        connectModel.setVerification(Utility.generateVerification(countryId, phoneNumber, encryptedPassword, nickname));
        connectModel.setLocale(userLocale);

        ObjectMapper mapper = Utility.createObjectMapper();

        String requestJson = mapper.writeValueAsString(connectModel);

        HttpResponse response = doPostJson(path, requestJson);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Registered successfully! Message:\n" + responseString);

                break;
            default:
                LOGGER.error("Status not expected, " + status);

                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    }

    @Test
    public void testCheckComputerAvailable() throws Exception {
        String userId = "9aaa3acb2bf8aa13533040cc4b24cec22089eba94b4d1c11f3a520656abdab5305b5b3f905b994ea10a24c16783fc340";
        String encryptedPassword = DigestUtils.sha256Hex(passwd);

        final ComputerModel computerModel = new ComputerModel();
        computerModel.setAccount(userId);
        computerModel.setPassword(encryptedPassword);
        computerModel.setNickname(nickname);
        computerModel.setVerification(Utility.generateVerification(userId, encryptedPassword, nickname));
        computerModel.setLocale(userLocale);
        computerModel.setGroupName(computerGroup);
        computerModel.setComputerName(computerName);

        ObjectMapper mapper = Utility.createObjectMapper();
        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(computerModel);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        String path = "computer/check-available";

        HttpResponse response = doPostJson(path, inputJson);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Response Message=\n" + responseString);

                break;
            case 400: // BAD_REQUEST, necessary parameters not provided
                LOGGER.info("Status 400. Message: " + responseString);

                break;
            case 401: // incorrect password
                LOGGER.info("Status 401. Message: " + responseString);

                break;
            case 403: // user not registered
                LOGGER.info("Status 403. Message: " + responseString);

                break;
            default:
                LOGGER.error("Status not expected, " + status);

                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    }

    @Test
    public void testUpdateUserComputerProfiles() throws Exception {
        String sessionId = "287DF1E4C4614D50806F05C63DA0EFC9BC1827DA0827DFB65899897EB79F25EC1635A421DED6EC9A18056149A550EC21CEA1D37C1DDF3E53F4724D152000BFDF";

        /* {
    "upload-directory" : "C:\Users\Administrator\Documents",
    "upload-subdirectory-type" : 4,
    "upload-subdirectory-value" : "西藏之旅",
    "upload-description-type" : 3,
    "upload-description-value" : "西藏布達拉宮",
    "upload-notification-type" : 1,
    "download-directory" : "/Storage/Emulated/0/Download",
    "download-subdirectory-type" : 4,
    "download-subdirectory-value" : "西藏之旅2",
    "download-description-type" : 3,
    "download-description-value" : "西藏布達拉宮2",
    "download-notification-type" : 0
} */

        UserComputer userComputer =
                new UserComputer(null, null, null, null, null, null, null, null, null, null, null,
                                 "C:\\Users\\Administrator\\Downloads01", 0, "01西藏之旅", 0, "01西藏布達拉宮", 1,
                                 "/Storage/Emulated/0/Download01", 0, "01西藏之旅2", 0, "01西藏布達拉宮2", 1);

        ObjectMapper mapper = Utility.createObjectMapper();
        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(userComputer);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        String path = "computer/ucprofiles";

        HttpResponse response = doPostJson(path, inputJson, sessionId);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Response Message=\n" + responseString);

                break;
            case 400: // BAD_REQUEST, necessary parameters not provided
                LOGGER.info("Status 400. Message: " + responseString);

                break;
            case 401: // incorrect password
                LOGGER.info("Status 401. Message: " + responseString);

                break;
            case 403: // user not registered
                LOGGER.info("Status 403. Message: " + responseString);

                break;
            default:
                LOGGER.error("Status not expected. Status=" + status + ", Info=" + responseString);
        }
    }

    @Test
    public void testFindUserComputerProfiles() throws Exception {
        String sessionId = "287DF1E4C4614D50806F05C63DA0EFC9BC1827DA0827DFB65899897EB79F25EC1635A421DED6EC9A18056149A550EC21CEA1D37C1DDF3E53F4724D152000BFDF";

        /*

         {
            "names" : [
                        "upload-directory" ,
                        "upload-subdirectory-type",
                        "upload-subdirectory-value",
                        "upload-description-type",
                        "upload-description-value",
                        "upload-notification-type",
                        "download-directory",
                        "download-subdirectory-type",
                        "download-subdirectory-value",
                        "download-description-type",
                        "download-description-value",
                        "download-notification-type"
                        ]
          }

         */

        List<String> profileNames = new ArrayList<>();
        profileNames.add("upload-directory");
        profileNames.add("upload-subdirectory-type");
        profileNames.add("upload-subdirectory-value");
        profileNames.add("upload-description-type");
        profileNames.add("upload-description-value");
        profileNames.add("upload-notification-type");
        profileNames.add("download-directory");
        profileNames.add("download-subdirectory-type");
        profileNames.add("download-subdirectory-value");
        profileNames.add("download-description-type");
        profileNames.add("download-description-value");
        profileNames.add("download-notification-type");
//        profileNames.add("profile-name-not-found");

        Map<String, List<String>> inputMap = new HashMap<>();

        inputMap.put("names", profileNames);

        ObjectMapper mapper = Utility.createObjectMapper();

        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputMap);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        String path = "computer/profiles";

        HttpResponse response = doPostJson(path, inputJson, sessionId);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Response Message=\n" + responseString);

                break;
            case 400: // BAD_REQUEST, necessary parameters not provided
                LOGGER.info("Status 400. Message: " + responseString);

                break;
            case 401: // incorrect password
                LOGGER.info("Status 401. Message: " + responseString);

                break;
            case 403: // user not registered
                LOGGER.info("Status 403. Message: " + responseString);

                break;
            default:
                LOGGER.error("Status not expected. Status=" + status + ", Info=" + responseString);
        }
    }
}
