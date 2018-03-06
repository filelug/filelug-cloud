package org.clopuccino.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.clopuccino.Utility;
import org.clopuccino.domain.ConnectModel;
import org.clopuccino.domain.RegisterModel;
import org.clopuccino.domain.RegistrationVerificationModel;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * <code>UserResourceTest</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class UserResourceTest extends AbstractResourceTest {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(UserResourceTest.class.getSimpleName());

    private String newUserId = "ECA9EB2FBF3DDA4F2C949221FF569BFAD7F6D353A2102F2F69F2E001748DB2FB410DF3555CEA158A69A1E3044F72FDE76A6C7F83C2C5A40D819D7A7268619403";

    // Do extends the super in this method
    @Before
    public void init() throws Exception {
        super.init();
    }

    @Test
    public void testEncryptPassword() throws Exception {
        String rawPassword = "filelug";
        String encryptedPassword = DigestUtils.sha256Hex(rawPassword);

        System.out.println("Encrypted password=" + encryptedPassword);
    }

    @Test
    public void testRegister() throws Exception {
        String path = "register";

        String encryptedPassword = DigestUtils.sha256Hex(passwd);

        RegisterModel registerModel = new RegisterModel();
        registerModel.setCountryId(countryId);
        registerModel.setPhoneNumber(phoneNumber);
        registerModel.setPassword(encryptedPassword);
        registerModel.setNickname(nickname);
        registerModel.setVerification(Utility.generateVerification(countryId, phoneNumber, encryptedPassword, nickname));
        registerModel.setLocale(userLocale);

        ObjectMapper mapper = Utility.createObjectMapper();

        String requestJson = mapper.writeValueAsString(registerModel);

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
    public void testRegistrationVerification() throws Exception {
        String path = "verify";

        RegistrationVerificationModel verificationModel = new RegistrationVerificationModel();

        String encryptedPassword = DigestUtils.sha256Hex(passwd);

        verificationModel.setAccount(newUserId);
//        verificationModel.setCountryId(countryId);
//        verificationModel.setPhoneNumber(phoneNumber);
        verificationModel.setPassword(encryptedPassword);
        verificationModel.setNickname(nickname);
        verificationModel.setVerification(Utility.generateVerification(newUserId, encryptedPassword, nickname));
        verificationModel.setLocale(userLocale);
        verificationModel.setVerifyCode(verification);

        ObjectMapper mapper = Utility.createObjectMapper();

        String requestJson = mapper.writeValueAsString(verificationModel);

        HttpResponse response = doPostJson(path, requestJson);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Registration verification successfully! Message:\n" + responseString);

                break;
            default:
                LOGGER.error("Status not expected, " + status);

                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    }

    @Test
    public void testCheckUserExisting() throws Exception {
        String userId = "ECA9EB2FBF3DDA4F2C949221FF569BFAD7F6D353A2102F2F69F2E001748DB2FB410DF3555CEA158A69A1E3044F72FDE76A6C7F83C2C5A40D819D7A7268619403";
        String encryptedPassword = DigestUtils.sha256Hex(passwd);

        final ConnectModel connectModel = new ConnectModel();
        connectModel.setAccount(userId);
        connectModel.setPassword(encryptedPassword);
        connectModel.setNickname(nickname);
        connectModel.setVerification(Utility.generateVerification(userId, encryptedPassword, nickname));
        connectModel.setLocale(userLocale);

        ObjectMapper mapper = Utility.createObjectMapper();
        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(connectModel);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        String path = "user/exist";

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
    public void testLogin() throws Exception {
        String userId = "7D727CF2F3E278110D7CD7CF20F0FFE0AF983A60009C02B78AEC2C78CDBDA601D4CF692C1434152E09EBF9EC00E3ED4FD5CD65D0DED0BEDE268B37E8613D859F";
        String encryptedPassword = DigestUtils.sha256Hex(passwd);

        final ConnectModel connectModel = new ConnectModel();
        connectModel.setAccount(userId);
        connectModel.setPassword(encryptedPassword);
        connectModel.setNickname(nickname);
        connectModel.setVerification(Utility.generateVerification(userId, encryptedPassword, nickname));
        connectModel.setShowHidden(false);
        connectModel.setLocale(userLocale);
        connectModel.setComputerId(computerId);
        connectModel.setGroupName(computerGroup);
        connectModel.setComputerName(computerName);

        ObjectMapper mapper = Utility.createObjectMapper();
        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(connectModel);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        String path = "user/login";

        HttpResponse response = doPostJson(path, inputJson);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Response Message=\n" + responseString);

                Map<String, Object> sysProperties = mapper.readValue(responseString, new TypeReference<Map<String, Object>>() {
                });

                assertEquals("showHidden value not expected!", connectModel.getShowHidden(), sysProperties.get("showHidden"));

                LOGGER.info("showHidden=" + sysProperties.get("showHidden"));

                String clientSessionId = (String) sysProperties.get("sessionId");
                assertTrue("No sessionId value!", clientSessionId != null && clientSessionId.trim().length() > 0);

                LOGGER.info("sessionId=" + clientSessionId);

                break;
            case 400: // BAD_REQUEST, necessary parameters not provided
                LOGGER.info("Status 400. Message: " + responseString);

                break;
            case 401: // incorrect password
                LOGGER.info("Status 401. Message: " + responseString);

                break;
            default:
                LOGGER.error("Status not expected, " + status);

                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    }

    @Test
    public void testLoginOnly() throws Exception {
        String userId = "7D727CF2F3E278110D7CD7CF20F0FFE0AF983A60009C02B78AEC2C78CDBDA601D4CF692C1434152E09EBF9EC00E3ED4FD5CD65D0DED0BEDE268B37E8613D859F";
        String encryptedPassword = DigestUtils.sha256Hex(passwd);

        final ConnectModel connectModel = new ConnectModel();
        connectModel.setAccount(userId);
        connectModel.setPassword(encryptedPassword);
        connectModel.setNickname(nickname);
        connectModel.setVerification(Utility.generateVerification(userId, encryptedPassword, nickname));
        connectModel.setShowHidden(false);
        connectModel.setLocale(userLocale);

        ObjectMapper mapper = Utility.createObjectMapper();
        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(connectModel);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        String path = "user/login";

        HttpResponse response = doPostJson(path, inputJson);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Response Message=\n" + responseString);

                Map<String, Object> sysProperties = mapper.readValue(responseString, new TypeReference<Map<String, Object>>() {
                });

                assertEquals("showHidden value not expected!", connectModel.getShowHidden(), sysProperties.get("showHidden"));

                LOGGER.info("showHidden=" + sysProperties.get("showHidden"));

                String clientSessionId = (String) sysProperties.get("sessionId");
                assertTrue("No sessionId value!", clientSessionId != null && clientSessionId.trim().length() > 0);

                LOGGER.info("sessionId=" + clientSessionId);

                break;
            case 400: // BAD_REQUEST, necessary parameters not provided
                LOGGER.info("Status 400. Message: " + responseString);

                break;
            case 401: // incorrect password
                LOGGER.info("Status 401. Message: " + responseString);

                break;
            default:
                LOGGER.error("Status not expected, Status=" + status + ", Info=" + responseString);
        }
    }


//    @Test
//    public void testLogout() throws Exception {
//        String id = "benius@gmail.com";
//        String sessionId = "042B2EB4C3A4607B75649E5E2D3A60E08BB55F75651222E59C3F238102258E4F";
//
//        final User clientUserModel = new User();
//        clientUserModel.setAccount(id);
//
//        ObjectMapper mapper = Utility.createObjectMapper();
//        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(clientUserModel);
//
//        /* DEBUG */
//        LOGGER.info("Input JSON:\n" + inputJson);
//
//        String path = "user/logout";
//
//        HttpResponse response = doPostJson(path, inputJson, sessionId);
//
//        int status = response.getStatusLine().getStatusCode();
//
//        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());
//
//        HttpEntity responseEntity = response.getEntity();
//        String responseString = responseEntity == null ? "" : EntityUtils.toString(responseEntity, Charset.forName("UTF-8"));
//
//        switch (status) {
//            case 200: // OK
//                LOGGER.info("Logged out session of user: " + responseString);
//                break;
//            case 400: // BAD_REQUEST, necessary parameters not provided
//                LOGGER.info("Status 400. Message: " + responseString);
//
//                break;
//            case 401: // not login yet
//                LOGGER.info("Status 401. Message: " + responseString);
//
//                break;
//            default:
//                LOGGER.error("Status not expected, " + status);
//
//                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
//        }
//    }

//    public void testSystemProperties() throws Exception {
//        Properties properties = System.getProperties();
//
//        Set<Map.Entry<Object, Object>> entrySet = properties.entrySet();
//
//        for (Map.Entry<Object, Object> entry : entrySet) {
//            System.out.println(entry.getKey() + ":" + entry.getValue());
//        }
//    }

//    @Test
//    public void testUpdateUser() throws Exception {
//        String parent = "/Users/masonhsieh/Downloads/每日五蔬果";
//        String name = "癌症遠離我4";
//
//        Boolean readable = true;
//        Boolean writable = false;
//        Boolean executable = true;
//
//        final UserModel auth = new UserModel();
//        auth.setParent(parent);
//        auth.setName(name);
//        auth.setReadable(readable);
//        auth.setWritable(writable);
//        auth.setExecutable(executable);
//
//        ObjectMapper mapper = Utility.createObjectMapper();
//        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(auth);
//
//        /* DEBUG */
//        LOGGER.info("Input JSON:\n" + inputJson);
//
//        String path = "auth";
//
//        HttpResponse response = doPostJson(path, inputJson);
//
//        int status = response.getStatusLine().getStatusCode();
//
//        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());
//
//        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
//
//        switch (status) {
//            case 200: // OK
//                LOGGER.info("Update User: " + responseString);
//
//                UserModel updatedModel = mapper.readValue(responseString, UserModel.class);
//
//                LOGGER.info("To UserModel class: " + updatedModel.toString());
//
//                break;
//            case 400: // BAD_REQUEST, path not exists or necessary parameters not provided
//                LOGGER.info("Status 400. Message: " + responseString);
//
//                break;
//            default:
//                LOGGER.error("Status not expected, " + status);
//
//                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
//        }
//    } // end testUpdateUser()
//
//    @Test
//    public void testMoveUser() throws Exception {
//        final String source = "/Users/masonhsieh/Downloads/temp";
//        final String target = "/Users/masonhsieh/Downloads/temp2";
//
//        MoveOrCopyBean bean = new MoveOrCopyBean(source, target);
//
//        ObjectMapper mapper = Utility.createObjectMapper();
//        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bean);
//
//        /* DEBUG */
//        LOGGER.info("Input JSON:\n" + inputJson);
//
//        String path = "auth/move";
//
//        HttpResponse response = doPostJson(path, inputJson);
//
//        int status = response.getStatusLine().getStatusCode();
//
//        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());
//
//        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
//
//        if (status == 200) {
//            LOGGER.info("Move User: " + responseString);
//
//            UserModel newUser = mapper.readValue(responseString, UserModel.class);
//
//            LOGGER.info("Moved auth: " + newUser.toString());
//
//            assertEquals("Path of the moved auth not expected", target, new File(newUser.getParent(), newUser.getName()).getAbsolutePath());
//            assertFalse("Source still not moved!", new File(source).exists());
//        } else {
//            LOGGER.error("User moved failed. Status=" + status + "; Info=" + responseString);
//        }
//    } // end testMoveUser()
//
//    @Test
//    public void testCopyUser() throws Exception {
//        final String source = "/Users/masonhsieh/Downloads/temp2";
////        final String source = null;
//
//        final String target = "/Users/masonhsieh/Downloads/temp";
////        final String target = null;
//
//        MoveOrCopyBean bean = new MoveOrCopyBean(source, target);
//
//        ObjectMapper mapper = Utility.createObjectMapper();
//        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bean);
//
//        /* DEBUG */
//        LOGGER.info("Input JSON:\n" + inputJson);
//
//        String path = "auth/copy";
//
//        HttpResponse response = doPostJson(path, inputJson);
//
//        int status = response.getStatusLine().getStatusCode();
//
//        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());
//
//        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
//
//        switch (status) {
//            case 200:
//                LOGGER.info("Copied User: " + responseString);
//
//                UserModel newUser = mapper.readValue(responseString, UserModel.class);
//
//                LOGGER.info("Copied auth: " + newUser.toString());
//
//                assertEquals("Path of the copied auth not expected", target, new File(newUser.getParent(), newUser.getName()).getAbsolutePath());
//                assertTrue("Target not exists!", new File(target).exists());
//
//                break;
//            case 400: // BAD_REQUEST, source path not exists
//                LOGGER.info("Status 400. Message: " + responseString);
//
//                break;
//            case 403: // FORBIDDEN, source cannot read
//                LOGGER.info("Status 403. Message: " + responseString);
//
//                break;
//            case 409: // CONFLICT, target path already exists
//                LOGGER.info("Status 409. Message: " + responseString);
//
//                break;
//            default:
//                LOGGER.error("User copied failed. Status=" + status + "; Info=" + responseString);
//        }
//    } // end testCopyUser()
//
//    @Test
//    public void testDeleteUser() throws Exception {
//        String parent = "/Users/masonhsieh/Downloads";
//        String name = "temp2";
////        String parent = "/Users/masonhsieh/Downloads/每日五蔬果";
////        String name = "癌症遠離我2";
//        String path = new File(parent, name).getAbsolutePath();
//
//        Boolean forever = Boolean.TRUE;
//
//        String encodedPath = Utility.realUrlEncode(path);
//
//        HttpResponse response = doDelete("auth/delete/" + forever.toString() + "/" + encodedPath);
//
//        int status = response.getStatusLine().getStatusCode();
//
//        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());
//
//        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
//
//        switch (status) {
//            case 200:
//                LOGGER.info("Delete User: " + responseString);
//
//                ObjectMapper mapper = Utility.createObjectMapper();
//
//                UserModel deletedUser = mapper.readValue(responseString, UserModel.class);
//
//                LOGGER.info("Deleted auth: " + deletedUser.toString());
//
//                assertEquals("Path of the deleted auth not expected", path, new File(deletedUser.getParent(), deletedUser.getName()).getAbsolutePath());
//                assertFalse("User still exists!", new File(path).exists());
//
//                break;
//            case 400: // BAD_REQUEST, path not exists, not a auth, or trash not supported
//                LOGGER.info("Status 400. Message: " + responseString);
//
//                break;
//            case 403: // FORBIDDEN, path cannot write
//                LOGGER.info("Status 403. Message: " + responseString);
//
//                break;
//            default:
//                LOGGER.error("User deleted failed. Status=" + status + "; Info=" + responseString);
//        }
//    } // end testDeleteUser()
}
