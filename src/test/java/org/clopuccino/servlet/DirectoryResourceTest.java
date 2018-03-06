package org.clopuccino.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * <code>DirectoryResourceTest</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class DirectoryResourceTest extends AbstractResourceTest {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DirectoryResourceTest.class.getSimpleName());

    // Do extends the super in this method
    @Before
    public void init() throws Exception {
        super.init();
    }

    @Test
    public void testListRoots() throws Exception {
        String path = "directory/roots";
        HttpResponse response = doGet(path, sessionId);

        int status = response.getStatusLine().getStatusCode();

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        if (status == 200) {
            LOGGER.info("Response from service 'listRoots':----------\n" + responseString + "\n----------");
        } else {
            LOGGER.error("Error on list roots! Status=" + status + "; Info=" + responseString);
        }

    } // end testFindBookmarkById()

    @Test
    public void testList() throws Exception {
//        final String path = "/Users/masonhsieh/Google Drive/book/Development/C & C++";
        final String path = "/Users/masonhsieh/Documents";
//        final String path = "/Users/masonhsieh/Pictures";
//        final String path = "/";
//        final String path = "/Users/masonhsieh/Google Drive";
//        final String path = "/Users/masonhsieh/Music/iTunes/iTunes Media/Music/初登場 Live 江惠";

        ObjectMapper mapper = Utility.createObjectMapper();

        ObjectNode inputRootNode = mapper.createObjectNode();
        inputRootNode.put("path", path);

        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputRootNode);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        String urlPath = "directory/list";

        HttpResponse response = doPostJson(urlPath, inputJson, sessionId);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Response Message=\n" + responseString);

                List<Map> hierarchicalModels = mapper.readValue(responseString, new TypeReference<List<Map>>(){});

                for (Map childModelMap : hierarchicalModels) {

                    HierarchicalModel childModel = HierarchicalFactory.createHierarchical(childModelMap);

                    LOGGER.info(childModel.toString());
                }

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
    } // end testList()

    @Test
    public void testFindByPath() throws Exception {
        /* test file */
//        final String pathName = "appdynamics"; // "appdynamics2.png";
//        final String path = "/Users/masonhsieh/Downloads/" + pathName;

        /* test file */
//        final String pathName = "Cookbook.pdf";
//        final String path = "C:\\Documents and Settings\\Administrator\\My Documents\\My Pictures\\" + pathName;

        /* test file */
//        final String pathName = "Cookbook.pdf";
//        final String path = "/Users/clopuccino/課程資料/" + pathName;

        /* test directory 1 */
//        final String pathName = "Google Drive";
//        final String path = "/Users/masonhsieh/" + pathName;

        /* test directory 2 */
        final String pathName = "初登場 Live";
        final String path = "/Users/masonhsieh/Music/iTunes/iTunes Media/Music/江惠/" + pathName;

        Boolean calculateSize = Boolean.TRUE;

        ObjectMapper mapper = Utility.createObjectMapper();

        ObjectNode inputRootNode = mapper.createObjectNode();
        inputRootNode.put("path", path);
        inputRootNode.put("calculateSize", calculateSize);

        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputRootNode);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        String urlPath = "directory/find";

        HttpResponse response = doPostJson(urlPath, inputJson, sessionId);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Response Message=\n" + responseString);

                HierarchicalModel resultModel = mapper.readValue(responseString, HierarchicalModel.class);

                LOGGER.info(resultModel.toString());

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
    } // end testFindByPath()

    @Test
    public void testFileRename() throws Exception {
        /* test file */
        final String oldFilename = "Beginning Linux Programming 4th Edition 22.pdf"; //"第一本 Linux 程式設計書 第四版.pdf";
        final String newFilename = "unchecked_checkbox-64.png";
        final String parent = "/Users/masonhsieh/Downloads/temp";

        /* test file - for 403 */
//        final String oldFilename = "test";
//        final String newFilename = "test2";
//        final String parent = "/bin";

        final String oldPath = new File(parent, oldFilename).getAbsolutePath();
        final String newPath = new File(parent, newFilename).getAbsolutePath();

        ObjectMapper mapper = Utility.createObjectMapper();

        ObjectNode inputRootNode = mapper.createObjectNode();
        inputRootNode.put("path", oldPath);
        inputRootNode.put("filename", newFilename);

        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputRootNode);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        String urlPath = "directory/rename";

        HttpResponse response = doPostJson(urlPath, inputJson, sessionId);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Response Message=\n" + responseString);

                FileRenameModel resultModel = mapper.readValue(responseString, FileRenameModel.class);

                LOGGER.info(resultModel.toString());

                assertEquals("File new path not expected!", newPath, resultModel.getNewPath());

                break;
            case 400: // BAD_REQUEST, necessary parameters not provided
                LOGGER.info("Status 400. Message: " + responseString);

                break;
            case 401: // incorrect password
                LOGGER.info("Status 401. Message: " + responseString);

                break;
            case 403: // incorrect password
                LOGGER.info("Status 403. Message: " + responseString);

                break;
            case 409: // incorrect password
                LOGGER.info("Status 409. Message: " + responseString);

                break;
            default:
                LOGGER.error("Status not expected, " + status);

                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    } // end testFileRename()

    @Test
    public void testDownloadFile() throws Exception {
        /* test file */
//        final String pathName = "RWCookbook.pdf";
//        final String path = "/Users/masonhsieh/Downloads/" + pathName;

        final String pathName = "週工作日誌 20130225-20130322.pptx";
        final String path = "/Users/masonhsieh/Documents/mpower/一週工作日誌/" + pathName;

        /* test file */
//        final String pathName = "Cookbook.pdf";
//        final String path = "C:\\Documents and Settings\\Administrator\\My Documents\\My Pictures\\" + pathName;

        /* test file */
//        final String pathName = "Cookbook.pdf";
//        final String path = "/Users/clopuccino/課程資料/" + pathName;

        /* test directory 2 */
//        final String pathName = "初登場 Live 江惠";
//        final String path = "/Users/masonhsieh/Music/iTunes/iTunes Media/Music/" + pathName;

        String encodedPath = Utility.realUrlEncode(path);

        Set<Header> requestHeaders = new HashSet<Header>();
        requestHeaders.add(new BasicHeader("Accept-Encoding", "gzip, deflate"));

//        requestHeaders.add(new BasicHeader("User-Agent", "clopuccino/20130419 CFNetwork/609.1.4 Darwin/13.0.0"));
        requestHeaders.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/536.30.1 (KHTML, like Gecko) Version/6.0.5 Safari/536.30.1"));

        String jsonString = String.format("{ \"path\" : \"%s\"}", new File(path).getAbsolutePath());

        HttpResponse response = doPostJson("directory/download", jsonString, sessionId);

        int status = response.getStatusLine().getStatusCode();

        Header[] headers = response.getAllHeaders();
        if (headers != null) {
            LOGGER.info("Response headers:");
            for (Header header : headers) {
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
                    String extension = FilenameUtils.getExtension(path);

                    if (extension.length() > 0) {
                        // abcd.txt --> length == 8, txt --> index 5,
                        downloadedPath = path.substring(0, path.length() - extension.length() - 1) + System.currentTimeMillis() + "." + extension;
                    } else {
                        downloadedPath = path + System.currentTimeMillis();
                    }

                    LOGGER.info("Trying to save to file: " + downloadedPath);

                    dest = new BufferedOutputStream(new FileOutputStream(downloadedPath));
                    responseEntity.writeTo(dest);

                    LOGGER.info("Downloaded file at: " + downloadedPath);

                    break;
                } catch (Throwable t) {
                    LOGGER.error("Failed to save to file: " + downloadedPath + "\n" + t.getMessage());
                } finally {
                    if (dest != null) {
                        dest.close();
                    }
                }
            case 400: // BAD_REQUEST, path not exists or not a file
                responseString = EntityUtils.toString(responseEntity, Charset.forName("UTF-8"));

                LOGGER.info("Status 400. Message: " + responseString);

                break;
            default:
                LOGGER.error("Status not expected, " + status);

                responseString = EntityUtils.toString(responseEntity, Charset.forName("UTF-8"));

                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    } // end testDownloadFile()

    @Test
    public void testCreateDirectory() throws Exception {
//        String parent = "/Users/masonhsieh/Downloads";
//        String name = "TEST2";
        String parent = "/Users/masonhsieh/Downloads/temp/每日五蔬果";
        String name = "癌症遠離我";
        Boolean readable = true;
        Boolean writable = true;
        Boolean executable = true;

        final DirectoryModel directory = new DirectoryModel();
        directory.setParent(parent);
        directory.setName(name);
        directory.setReadable(readable);
        directory.setWritable(writable);
        directory.setExecutable(executable);

        ObjectMapper mapper = Utility.createObjectMapper();
        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(directory);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        String path = "directory";

        HttpResponse response = doPostJson(path, inputJson, sessionId);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Created Directory:\n" + responseString);
                break;
            case 400: // BAD_REQUEST, path not exists or necessary parameters not provided
                LOGGER.info("Status 400. Message: " + responseString);

                break;
            case 409: // CONFLICT, directory duplicated
                LOGGER.info("Status 409. Message: " + responseString);

                break;
            default:
                LOGGER.error("Status not expected, " + status);

                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    }

    @Test
    public void testUploadFile() throws Exception {
        String sourceParent = "/Users/masonhsieh/Movies/iMovie Export";
        String sourceName = "2010-09 擦乳液.m4v";

//        String destParent = "";
        String destParent = "/Users/masonhsieh/Downloads/temp";
//        String destName = "";
        String destName = "2010-09 擦乳液 2.m4v";

        String encodedDestParent = Utility.encodeUsingBase64(destParent, Constants.BASE64_CONVERSION_CHARSET);
        String encodedDestName = Utility.encodeUsingBase64(destName, Constants.BASE64_CONVERSION_CHARSET);

        Set<Header> newHeaders = new HashSet<>();
        newHeaders.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate"));
        newHeaders.add(new BasicHeader(Constants.HTTP_HEADER_NAME_UPLOAD_DIRECTORY, encodedDestParent));
        newHeaders.add(new BasicHeader(Constants.HTTP_HEADER_NAME_UPLOAD_FILE_NAME, encodedDestName));
        newHeaders.add(new BasicHeader(HttpHeaders.AUTHORIZATION, sessionId));

        String path = "directory/dupload";

        HttpResponse response = doUploadFile(path, newHeaders, new File(sourceParent, sourceName), sessionId);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("File uploaded successfully.\n" + responseString);
                break;
            case 400: // BAD_REQUEST, parent not exists or necessary parameters not provided
                LOGGER.info("Status 400. Message: " + responseString);

                break;
            case 409: // CONFLICT, file name duplicated
                LOGGER.info("Status 409. Message: " + responseString);

                break;
            default:
                LOGGER.error("Status not expected, " + status);

                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    }

    @Test
    public void testGenerateUploadKey() throws Exception {
        String clientSessionId = "91CF5179090DAD4BE6AEFDA6C84E5285EBB839A994BC99CBF0274317884DA817";
        String directory = "C:\\Documents and Settings\\Administrator\\My Documents\\My Pictures";
        String filename = "上傳檔案測試.PNG";

        String uploadKey = ClientUploadRequestUtility.generateKey(clientSessionId, directory, filename);

        System.out.println("Upload Key: " + uploadKey);
    }

    @Test
    public void testCurrentTimeMillis() {
        System.out.println(System.currentTimeMillis());
    }

//    @Test
//    public void testUpdateDirectory() throws Exception {
//        String parent = "/Users/masonhsieh/Downloads/每日五蔬果";
//        String name = "癌症遠離我5";
//
//        Boolean readable = true;
//        Boolean writable = false;
//        Boolean executable = true;
//
//        final DirectoryModel directory = new DirectoryModel();
//        directory.setParent(parent);
//        directory.setName(name);
//        directory.setReadable(readable);
//        directory.setWritable(writable);
//        directory.setExecutable(executable);
//
//        ObjectMapper mapper = Utility.createObjectMapper();
//        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(directory);
//
//        /* DEBUG */
//        LOGGER.info("Input JSON:\n" + inputJson);
//
//        String path = "directory";
//
//        HttpResponse response = doPostJson(path, inputJson, sessionId);
//
//        int status = response.getStatusLine().getStatusCode();
//
//        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());
//
//        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
//
//        switch (status) {
//            case 200: // OK
//                LOGGER.info("Update Directory: " + responseString);
//
//                DirectoryModel updatedModel = mapper.readValue(responseString, DirectoryModel.class);
//
//                LOGGER.info("To DirectoryModel class: " + updatedModel.toString());
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
//    } // end testUpdateDirectory()
//
//    @Test
//    public void testMoveDirectory() throws Exception {
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
//        String path = "directory/move";
//
//        HttpResponse response = doPostJson(path, inputJson, sessionId);
//
//        int status = response.getStatusLine().getStatusCode();
//
//        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());
//
//        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
//
//        if (status == 200) {
//            LOGGER.info("Move Directory: " + responseString);
//
//            DirectoryModel newDirectory = mapper.readValue(responseString, DirectoryModel.class);
//
//            LOGGER.info("Moved directory: " + newDirectory.toString());
//
//            assertEquals("Path of the moved directory not expected", target, new File(newDirectory.getParent(), newDirectory.getName()).getAbsolutePath());
//            assertFalse("Source still not moved!", new File(source).exists());
//        } else {
//            LOGGER.error("Directory moved failed. Status=" + status + "; Info=" + responseString);
//        }
//    } // end testMoveDirectory()
//
//    @Test
//    public void testCopyDirectory() throws Exception {
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
//        String path = "directory/copy";
//
//        HttpResponse response = doPostJson(path, inputJson, sessionId);
//
//        int status = response.getStatusLine().getStatusCode();
//
//        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());
//
//        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
//
//        switch (status) {
//            case 200:
//                LOGGER.info("Copied Directory: " + responseString);
//
//                DirectoryModel newDirectory = mapper.readValue(responseString, DirectoryModel.class);
//
//                LOGGER.info("Copied directory: " + newDirectory.toString());
//
//                assertEquals("Path of the copied directory not expected", target, new File(newDirectory.getParent(), newDirectory.getName()).getAbsolutePath());
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
//                LOGGER.error("Directory copied failed. Status=" + status + "; Info=" + responseString);
//        }
//    } // end testCopyDirectory()
//
//    @Test
//    public void testDeleteDirectory() throws Exception {
//        String parent = "/Users/masonhsieh/Downloads";
//        String name = "temp2";
////        String parent = "/Users/masonhsieh/Downloads/每日五蔬果";
////        String name = "癌症遠離我2";
//        String path = new File(parent, name).getAbsolutePath();
//
//        Boolean forever = Boolean.FALSE;
//
//        String encodedPath = Utility.realUrlEncode(path);
//
//        HttpResponse response = doDelete("directory/delete/" + forever.toString() + "/" + encodedPath, sessionId);
//
//        int status = response.getStatusLine().getStatusCode();
//
//        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());
//
//        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
//
//        switch (status) {
//            case 200:
//                LOGGER.info("Delete Directory: " + responseString);
//
//                ObjectMapper mapper = Utility.createObjectMapper();
//
//                DirectoryModel deletedDirectory = mapper.readValue(responseString, DirectoryModel.class);
//
//                LOGGER.info("Deleted directory: " + deletedDirectory.toString());
//
//                assertEquals("Path of the deleted directory not expected", path, new File(deletedDirectory.getParent(), deletedDirectory.getName()).getAbsolutePath());
//                assertFalse("Directory still exists!", new File(path).exists());
//
//                break;
//            case 400: // BAD_REQUEST, path not exists, not a directory, or trash not supported
//                LOGGER.info("Status 400. Message: " + responseString);
//
//                break;
//            case 403: // FORBIDDEN, path cannot write
//                LOGGER.info("Status 403. Message: " + responseString);
//
//                break;
//            default:
//                LOGGER.error("Directory deleted failed. Status=" + status + "; Info=" + responseString);
//        }
//    } // end testDeleteDirectory()
//
//    @Test
//    public void testDownloadFile() throws Exception {
//        /* test file */
////        final String pathName = "RWCookbook.pdf";
////        final String path = "/Users/masonhsieh/Downloads/" + pathName;
//
//        final String pathName = "週工作日誌 20130415-20130419.pptx";
//        final String path = "/Users/masonhsieh/Documents/mpower/一週工作日誌/" + pathName;
//
//        /* test file */
////        final String pathName = "Cookbook.pdf";
////        final String path = "C:\\Documents and Settings\\Administrator\\My Documents\\My Pictures\\" + pathName;
//
//        /* test file */
////        final String pathName = "Cookbook.pdf";
////        final String path = "/Users/clopuccino/課程資料/" + pathName;
//
//        /* test directory 2 */
////        final String pathName = "初登場 Live 江惠";
////        final String path = "/Users/masonhsieh/Music/iTunes/iTunes Media/Music/" + pathName;
//
//        String encodedPath = Utility.realUrlEncode(path);
//
//        Set<Header> requestHeaders = new HashSet<Header>();
//        requestHeaders.add(new BasicHeader("Accept-Encoding", "gzip, deflate"));
//
////        requestHeaders.add(new BasicHeader("User-Agent", "clopuccino/20130419 CFNetwork/609.1.4 Darwin/13.0.0"));
//        requestHeaders.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/536.30.1 (KHTML, like Gecko) Version/6.0.5 Safari/536.30.1"));
//
//        HttpResponse response = doGet("directory/download/" + encodedPath, requestHeaders, null, sessionId);
//
//        int status = response.getStatusLine().getStatusCode();
//
//        Header[] headers = response.getAllHeaders();
//        if (headers != null) {
//            LOGGER.info("Response headers:");
//            for (Header header : headers) {
//                LOGGER.info(header.getName() + " : " + header.getValue());
//            }
//        }
//
//        HttpEntity responseEntity = response.getEntity();
//
//        String responseString;
//
//        switch (status) {
//            case 200: // OK
//                LOGGER.info("Downloaded successfully.");
//
//                BufferedOutputStream dest = null;
//                String downloadedPath = null;
//
//                try {
//                    String extension = FilenameUtils.getExtension(path);
//
//                    if (extension.length() > 0) {
//                        // abcd.txt --> length == 8, txt --> index 5,
//                        downloadedPath = path.substring(0, path.length() - extension.length() - 1) + System.currentTimeMillis() + "." + extension;
//                    } else {
//                        downloadedPath = path + System.currentTimeMillis();
//                    }
//
//                    LOGGER.info("Trying to save to file: " + downloadedPath);
//
//                    dest = new BufferedOutputStream(new FileOutputStream(downloadedPath));
//                    responseEntity.writeTo(dest);
//
//                    LOGGER.info("Downloaded file at: " + downloadedPath);
//
//                    break;
//                } catch (Throwable t) {
//                    LOGGER.error("Failed to save to file: " + downloadedPath + "\n" + t.getMessage());
//                } finally {
//                    if (dest != null) {
//                        dest.close();
//                    }
//                }
//            case 400: // BAD_REQUEST, path not exists or not a file
//                responseString = EntityUtils.toString(responseEntity, Charset.forName("UTF-8"));
//
//                LOGGER.info("Status 400. Message: " + responseString);
//
//                break;
//            default:
//                LOGGER.error("Status not expected, " + status);
//
//                responseString = EntityUtils.toString(responseEntity, Charset.forName("UTF-8"));
//
//                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
//        }
//    } // end testDownloadFile()
//
//    @Test
//    public void testAsyncDownloadFile() throws Exception {
//        /* test file */
//        final String pathName = "RWCookbook.pdf";
//        final String path = "/Users/masonhsieh/Downloads/" + pathName;
//
////        final String pathName = "週工作日誌 20130415-20130419.pptx";
////        final String path = "/Users/masonhsieh/Documents/mpower/一週工作日誌/" + pathName;
//
//        /* test file */
////        final String pathName = "Cookbook.pdf";
////        final String path = "C:\\Documents and Settings\\Administrator\\My Documents\\My Pictures\\" + pathName;
//
//        /* test file */
////        final String pathName = "Cookbook.pdf";
////        final String path = "/Users/clopuccino/課程資料/" + pathName;
//
//        /* test directory 2 */
////        final String pathName = "初登場 Live 江惠";
////        final String path = "/Users/masonhsieh/Music/iTunes/iTunes Media/Music/" + pathName;
//
//        String encodedPath = Utility.realUrlEncode(path);
//
//        HttpResponse response = doAsyncGet("directory/download/" + encodedPath, sessionId);
//
//        int status = response.getStatusLine().getStatusCode();
//
//        Header[] headers = response.getAllHeaders();
//        if (headers != null) {
//            LOGGER.info("Response headers:");
//            for (Header header : headers) {
//                LOGGER.info(header.getName() + " : " + header.getValue());
//            }
//        }
//
//        HttpEntity responseEntity = response.getEntity();
//
//        String responseString;
//
//        switch (status) {
//            case 200: // OK
//                LOGGER.info("Downloaded successfully.");
//
//                BufferedOutputStream dest = null;
//                String downloadedPath = null;
//
//                try {
//                    String extension = FilenameUtils.getExtension(path);
//
//                    if (extension.length() > 0) {
//                        // abcd.txt --> length == 8, txt --> index 5,
//                        downloadedPath = path.substring(0, path.length() - extension.length() - 1) + System.currentTimeMillis() + "." + extension;
//                    } else {
//                        downloadedPath = path + System.currentTimeMillis();
//                    }
//
//                    LOGGER.info("Trying to save to file: " + downloadedPath);
//
//                    dest = new BufferedOutputStream(new FileOutputStream(downloadedPath));
//                    responseEntity.writeTo(dest);
//
//                    LOGGER.info("Downloaded file at: " + downloadedPath);
//
//                    break;
//                } catch (Throwable t) {
//                    LOGGER.error("Failed to save to file: " + downloadedPath + "\n" + t.getMessage());
//                } finally {
//                    if (dest != null) {
//                        dest.close();
//                    }
//                }
//            case 400: // BAD_REQUEST, path not exists or not a file
//                responseString = EntityUtils.toString(responseEntity, Charset.forName("UTF-8"));
//
//                LOGGER.info("Status 400. Message: " + responseString);
//
//                break;
//            default:
//                LOGGER.error("Status not expected, " + status);
//
//                responseString = EntityUtils.toString(responseEntity, Charset.forName("UTF-8"));
//
//                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
//        }
//    } // end testAsyncDownloadFile()
}
