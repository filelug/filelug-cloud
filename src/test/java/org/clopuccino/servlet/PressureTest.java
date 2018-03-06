package org.clopuccino.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.clopuccino.Utility;
import org.clopuccino.domain.Computer;
import org.clopuccino.domain.ConnectModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <code>PressureTest</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class PressureTest extends AbstractResourceTest {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(PressureTest.class.getSimpleName());

    private List<ConnectModel> connectModels = new ArrayList<>();

    private long intervalInMillis = 1000;

    private int downloadCountPerInterval = 1;

    private int uploadCountPerInterval = 1;

    private String downloadDirectory = "C:\\Users\\Administrator\\Pictures";

    private String uploadDirectory = "C:\\Users\\Administrator\\Downloads";

    private String fileSeparator = File.separator;

    @Before
    public void setUp() throws Exception {
        super.init();

        // reset connect models

        connectModels.clear();

        String userId = "TW-0975087557";
        String password = DigestUtils.sha256Hex("1234lug");
        String nickname = RandomStringUtils.random(12, true, true);
        String verification = Utility.generateVerification(userId, password, nickname);
        boolean showHidden = false;
        String clientLocale = "zh_TW";
        Long computerId = 1L;
        String computerGroup = Computer.Type.GENERAL.name();
        String computerName = "小威的 MBP";
        String deviceVersion = "1.2.0";
        String deviceBuild = "2014.12.4.1";

        addNewConnectModel(userId, password, nickname, verification, showHidden, clientLocale, computerId, computerGroup, computerName, deviceVersion, deviceBuild);

        userId = "TW-0975087557";
        password = DigestUtils.sha256Hex("1234lug");
        nickname = RandomStringUtils.random(12, true, true);
        verification = Utility.generateVerification(userId, password, nickname);
        showHidden = false;
        clientLocale = "zh_TW";
        computerId = 2L;
        computerGroup = Computer.Type.GENERAL.name();
        computerName = "小威的 Windows 7";
        deviceVersion = "1.2.0";
        deviceBuild = "2014.12.4.1";

        addNewConnectModel(userId, password, nickname, verification, showHidden, clientLocale, computerId, computerGroup, computerName, deviceVersion, deviceBuild);

        // reset interval and download/upload counts
    }

    private void addNewConnectModel(String userId, String password, String nickname, String verification, boolean showHidden, String clientLocale, Long computerId, String computerGroup, String computerName, String deviceVersion, String deviceBuild) {
        ConnectModel connectModel = new ConnectModel();

        connectModel.setAccount(userId);
        connectModel.setPassword(password);
        connectModel.setNickname(nickname);
        connectModel.setVerification(verification);
        connectModel.setShowHidden(showHidden);
        connectModel.setLocale(clientLocale);
        connectModel.setComputerId(computerId);
        connectModel.setGroupName(computerGroup);
        connectModel.setComputerName(computerName);
        connectModel.setDeviceVersion(deviceVersion);
        connectModel.setDeviceBuild(deviceBuild);

        connectModels.add(connectModel);
    }

    @After
    public void tearDown() throws Exception {
        // release and close file resources, if any
    }

    @Test
    public void testDownload() throws Exception {
        for (ConnectModel connectModel : connectModels) {
            ObjectMapper mapper = Utility.createObjectMapper();
            String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(connectModel);

            LOGGER.info("Login Input JSON:\n%s", inputJson);

            String path = "user/login";

            HttpResponse response = doPostJson(path, inputJson);

            int status = response.getStatusLine().getStatusCode();

            LOGGER.info("Login response status '%d', message: %s ", status, response.getStatusLine().getReasonPhrase());

            String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

            switch (status) {
                case 200: // OK
                    LOGGER.info("Login response message:\n%s", responseString);

                    Map<String, Object> sysProperties = mapper.readValue(responseString, new TypeReference<Map<String, Object>>() {
                    });

                    String lugServerId = (String) sysProperties.get("lug-server-id");

                    if (lugServerId == null) {
                        fail("No lug server found.");
                    }

                    LOGGER.info("Lug server: '%s'", lugServerId);

                    updateBaseURI(lugServerId);

                    String clientSessionId = (String) sysProperties.get("sessionId");

                    LOGGER.info("sessionId: '%s'", clientSessionId);

                    fileSeparator = (String) sysProperties.get("file.separator");

                    LOGGER.info("file separator: '%s'", fileSeparator);

                    List<String> fileNames = findDownloadFiles();

                    int count = 0;

                    for (String fileName : fileNames) {
                        final String currentFileName = fileName;

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    downloadFile(currentFileName);
                                } catch (Exception e) {
                                    String errorMessage = String.format("Error on downloading file: %s", currentFileName);

                                    LOGGER.error(errorMessage, e);
                                }
                            }
                        }).start();

                        count++;

                        if (count >= downloadCountPerInterval) {
                            count = 0;

                            // take a rest
                            Thread.sleep(intervalInMillis);
                        }
                    }

                    break;
                default:
                    String errorMessage = String.format("Status '%d' not expected. Info: %s", status, responseString);

                    LOGGER.error(errorMessage);

                    fail(errorMessage);
            }
        }
    }

    private void downloadFile(String fileName) throws Exception {
        Set<Header> headers = new HashSet<>();
        headers.add(new BasicHeader("Accept-Encoding", "gzip, deflate"));

        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/536.30.1 (KHTML, like Gecko) Version/6.0.5 Safari/536.30.1"));

        String fileAbsPath = new File(downloadDirectory + fileSeparator + fileName).getAbsolutePath();

        String jsonString = String.format("{ \"path\" : \"%s\"}", fileAbsPath);

        HttpResponse response = doPostJson("directory/download", jsonString, headers, sessionId);

        int status = response.getStatusLine().getStatusCode();

        Header[] responseHeaders = response.getAllHeaders();
        if (responseHeaders != null) {
            LOGGER.info("Response headers:");
            for (Header header : responseHeaders) {
                LOGGER.info(header.getName() + " : " + header.getValue());
            }
        }

        HttpEntity responseEntity = response.getEntity();

        String responseString;

        switch (status) {
            case 200: // OK
                LOGGER.info("Downloaded successfully.");

                BufferedOutputStream dest = null;
                String downloadedPath = null;

                try {
                    String extension = FilenameUtils.getExtension(fileAbsPath);

                    if (extension.length() > 0) {
                        // abcd.txt --> length == 8, txt --> index 5,
                        downloadedPath = fileAbsPath.substring(0, fileAbsPath.length() - extension.length() - 1) + System.currentTimeMillis() + "." + extension;
                    } else {
                        downloadedPath = fileAbsPath + System.currentTimeMillis();
                    }

                    LOGGER.info("Trying to save to file: " + downloadedPath);

                    dest = new BufferedOutputStream(new FileOutputStream(downloadedPath));
                    responseEntity.writeTo(dest);

                    LOGGER.info("File Downloaded to: " + downloadedPath);

                    break;
                } catch (Throwable t) {
                    LOGGER.error("Failed to save to file: " + downloadedPath + "\n" + t.getMessage());
                } finally {
                    if (dest != null) {
                        dest.close();
                    }
                }
            default:
                responseString = EntityUtils.toString(responseEntity, Charset.forName("UTF-8"));

                String errorMessage = String.format("Status '%d' not expected. response:\n%s", status, responseString);

                throw new IOException(errorMessage);
        }
    }

    private List<String> findDownloadFiles() throws Exception {
        List<String> files = new ArrayList<>();

        ObjectMapper mapper = Utility.createObjectMapper();

        ObjectNode inputRootNode = mapper.createObjectNode();
        inputRootNode.put("path", downloadDirectory);

        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputRootNode);

        LOGGER.info("[List]Input JSON:\n%s", inputJson);

        String urlPath = "directory/list";

        HttpResponse response = doPostJson(urlPath, inputJson, sessionId);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("[List]Status: '%d', message: %s" + status, response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("[List]Response message=\n%s", responseString);

                List<Map> hierarchicalModels = mapper.readValue(responseString, new TypeReference<List<Map>>(){});

                for (Map modelMap : hierarchicalModels) {
                    String type = (String) modelMap.get("type");

                    if (type != null && type.toLowerCase().endsWith("file")) {
                        String name = (String) modelMap.get("name");

                        files.add(name);

                        LOGGER.info("[List]File name '%s' added", name);
                    }
                }

                break;
            default:
                String errorMessage = String.format("[List]Status '%d' not expected, message: %s", status, responseString);

                LOGGER.error(errorMessage);

                fail(errorMessage);
        }

        return files;
    }
}
