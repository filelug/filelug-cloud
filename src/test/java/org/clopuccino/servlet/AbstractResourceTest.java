package org.clopuccino.servlet;


import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.clopuccino.Constants;
import org.clopuccino.domain.Computer;
import org.clopuccino.service.BaseService;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <code>AbstractResourceTest</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class AbstractResourceTest {

//    protected String scheme = "https://";
    protected String scheme = "http://";

    protected String lugServerId = "repo2";

    protected String aaHostAddress = "repo.filelug.com";

//    protected String hostAddress = lugServerId + ".filelug.com";
    protected String hostAddress = "127.0.0.1";

//    protected String port = "443";
    protected String port = "8080";


    protected String contextPath = "crepo";

    protected String aaBaseURI;

    protected String baseURI;

    protected String sessionId = "EE302F02B40108F2958EEC5B50FDC30080C7CA9E8E3F5BE77ADA93BC5B3228FD3128717BCB0C75C7DEF999F8DF980366C22130197079D31CDCB4E67DD89CB2D3";

    protected String countryId = "TW";

    protected String phoneNumber = "0975700152";

    protected String operatorId = "7D727CF2F3E278110D7CD7CF20F0FFE0AF983A60009C02B78AEC2C78CDBDA601D4CF692C1434152E09EBF9EC00E3ED4FD5CD65D0DED0BEDE268B37E8613D859F";

    protected String verification = "8728";

    protected String passwd = "1234lug";

    protected String nickname = "小威";

    protected String userLocale = "zh-Hant";

    protected Long computerId = 5L;

    protected String computerGroup = Computer.Type.GENERAL.name();

    protected String computerName = "小威測試的電腦";
//    protected String computerName = "Filelug Demo";

    protected String recoveryKey = "041823369577";

    protected void init() throws Exception {
        System.setProperty("configuration.directory", "/Users/masonhsieh/projects/Servers/jetty-9.1.5-repo");

        aaBaseURI = new StringBuilder().append(scheme).append(aaHostAddress).append(":").append(port).append("/").append(contextPath).append("/").toString();

        baseURI = new StringBuilder().append(scheme).append(hostAddress).append(":").append(port).append("/").append(contextPath).append("/").toString();
    }

    protected void updateBaseURI(String subDomain) {
        lugServerId = subDomain;

        baseURI = new StringBuilder().append(scheme).append(hostAddress).append(":").append(port).append("/").append(contextPath).append("/").toString();
    }

    protected HttpResponse doGet(String path) throws Exception {
        return doGet(path, null, null, null);
    }

    protected HttpResponse doGet(String path, String sessionId) throws Exception {
        Set<Header> headers = null;

        if (sessionId != null && sessionId.trim().length() > 0) {
            headers = new HashSet<>();
            headers.add(new BasicHeader(HttpHeaders.AUTHORIZATION, sessionId));
        }

        return doGet(path, headers, null, null);
    }

    protected HttpResponse doGet(String path, Set<Header> headers, List<NameValuePair> params, String sessionId) throws Exception {
        return new BaseService().doGet(baseURI, path, headers, params, sessionId);
    }

    protected HttpResponse doPostJson(String path, String jsonString) throws Exception {
        return doPostJson(path, jsonString, null);
    }

    protected HttpResponse doPostJson(String path, String jsonString, String sessionId) throws Exception {

        return new BaseService().doPostJson(baseURI, path, jsonString, sessionId);
    }

    protected HttpResponse doPostJson(String path, String jsonString, Set<Header> headers, String sessionId) throws Exception {

        return new BaseService().doPostJson(baseURI, path, headers, jsonString, sessionId);
    }

    protected HttpResponse doPostJson(boolean connectAAServer, String path, String jsonString, String sessionId) throws Exception {
        String uri = connectAAServer ? aaBaseURI : baseURI;

        return new BaseService().doPostJson(uri, path, jsonString, sessionId);
    } // end doPostJson(String, String)

    protected HttpResponse doPost(String path, Set<Header> headers, List<NameValuePair> params, String sessionId) throws Exception {

        return new BaseService().doPost(baseURI, path, headers, params, sessionId);
    } // end doPost(String, Set<Header>, List<NameValuePair>, String)

    protected HttpResponse doUploadFile(String path, Set<Header> headers, File uploadFile, String sessionId) throws Exception {
        // 1 mins
        int timeoutInMiliis = 60 * 1000;
        int connectInMiliis = 60 * 1000;

        return new BaseService(timeoutInMiliis, connectInMiliis).doUploadFile(baseURI, path, headers, uploadFile, sessionId);
    } // end doUploadFile(String, Set<Header>, File, String)

//    protected HttpResponse doDelete(String path) throws Exception {
//        return doDelete(path, null);
//    } // end doDelete(String)

//    protected HttpResponse doDelete(String path, String sessionId) throws Exception {
//
//        return new BaseService().doDelete(baseURI, path, sessionId);
//    } // end doDelete(String)
}
