package org.clopuccino.service;

import ch.qos.logback.classic.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

/**
 * <code>BaseService</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class BaseService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(BaseService.class.getSimpleName());

    // works for 9.1 or later versions of jetty
//    private static final String JETTY_CONFIG_KEY_HTTP_PORT = "jetty.http.port";
    private static final String JETTY_CONFIG_KEY_HTTPS_PORT = "jetty.ssl.port";

    // works only for 9.0 or early versions of jetty
//    private static final String JETTY_CONFIG_KEY_HTTP_PORT = "jetty.port";
//    private static final String JETTY_CONFIG_KEY_HTTPS_PORT = "https.port";

    private static final String DEFAULT_HTTPS_PORT = "443";

    private static final String DEFAULT_HTTP_PORT = "8080";

    public static final String CONTENT_TYPE_UNKNOWN = "application/octet-stream";

    public static final ResourceBundle mimeTypeBundle = ResourceBundle.getBundle("MimeType");

    /**
     * Safe characters for x-www-form-urlencoded data, as per java.net.URLEncoder and browser behaviour,
     * i.e. alphanumeric plus {@code "-", "_", ".", "*"}
     */
    private static final BitSet URLENCODER = new BitSet(256);

    private static final char PARAMETER_SEPARATOR = '&';

    private static final String PARAMETER_NAME_VALUE_SEPARATOR = "=";

    public static Boolean repositoryUseHttps = null;

//    public static String repositoryPort = null;

    // Socket timeout is the timeout to receive data
    private int socketTimeout = Constants.SOCKET_TIMEOUT;

    private int connectTimeout = Constants.CONNECT_TIMEOUT;


    public BaseService() {
        if (repositoryUseHttps == null) {
            Properties PROPERTIES_HTTPS = Utility.readHttpsConfiguration();

            repositoryUseHttps = PROPERTIES_HTTPS != null && PROPERTIES_HTTPS.containsKey(JETTY_CONFIG_KEY_HTTPS_PORT);
        }
    }

    // Before considering it's possible that Utility.readHttpsConfiguration() or Utility.readHttpConfiguration() returns null
//    public BaseService() {
//        if (repositoryUseHttps == null) {
//            Properties PROPERTIES_HTTPS = Utility.readHttpsConfiguration();
//
//            repositoryUseHttps = PROPERTIES_HTTPS != null && PROPERTIES_HTTPS.containsKey(JETTY_CONFIG_KEY_HTTPS_PORT);
//
//            if (repositoryUseHttps) {
//                repositoryPort = (PROPERTIES_HTTPS.getProperty(JETTY_CONFIG_KEY_HTTPS_PORT) != null) ? PROPERTIES_HTTPS.getProperty(JETTY_CONFIG_KEY_HTTPS_PORT) : DEFAULT_HTTPS_PORT;
//            } else {
//                Properties PROPERTIES_HTTP = Utility.readHttpConfiguration();
//
//                repositoryPort = (PROPERTIES_HTTP != null && PROPERTIES_HTTP.getProperty(JETTY_CONFIG_KEY_HTTP_PORT) != null) ? PROPERTIES_HTTP.getProperty(JETTY_CONFIG_KEY_HTTP_PORT) : DEFAULT_HTTP_PORT;
//
//                LOGGER.warn("No SSL supported. Repository will run as a testing environment.");
//            }
//        }
//    }

    public BaseService(int socketTimeout, int connectTimeout) {
        this();

        this.socketTimeout = socketTimeout;
        this.connectTimeout = connectTimeout;
    }

    public Boolean getRepositoryUseHttps() {
        return repositoryUseHttps;
    }

    public String getBaseURIFrom(boolean isSecure, String hostname, String port, String contextPath) {
        StringBuilder uriBuilder = new StringBuilder();

        String uriScheme;
        if (isSecure) {
            uriScheme = "https";
        } else {
            uriScheme = "http";
        }

        uriBuilder.append(uriScheme).append("://").append(hostname);

        if (port != null && port.length() > 0) {
            uriBuilder.append(":").append(port);
        }

        uriBuilder.append("/");

        if (contextPath != null && contextPath.length() > 0) {
            uriBuilder.append(contextPath).append("/");
        }

        return uriBuilder.toString();
    }

    public HttpResponse doGet(String baseURI, String path, Set<Header> headers, List<NameValuePair> params, String sessionId) throws Exception {
        HttpResponse response;

        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build();

        HttpClientBuilder clientBuilder = HttpClientBuilder.create().setDefaultRequestConfig(config);

        Set<Header> newHeaders;

        if (headers != null && headers.size() > 0) {
            newHeaders = new HashSet<>(headers);
        } else {
            newHeaders = new HashSet<>();
        }

        if (sessionId != null && sessionId.trim().length() > 0) {
            newHeaders.add(new BasicHeader(HttpHeaders.AUTHORIZATION, sessionId));
        }

        if (newHeaders.size() > 0) {
            clientBuilder.setDefaultHeaders(newHeaders);
        }

        HttpClient httpClient = clientBuilder.build();

        String fullPath = baseURI + path;

        if (params != null && params.size() > 0) {
            if (!fullPath.endsWith("?")) {
                fullPath += "?";
            }

            String paramString = URLEncodedUtils.format(params, "UTF-8");

            fullPath += paramString;
        }

        HttpGet httpGet = new HttpGet(fullPath);

        LOGGER.debug("Execute request GET " + httpGet.getURI().toString());

        response = httpClient.execute(httpGet);

        return response;
    } // end doGet()

    public HttpResponse doTrustSelfSignedGet(String baseURI, String path, Set<Header> headers, List<NameValuePair> params, boolean parameterBlankAsPlus, String sessionId) throws Exception {
        HttpResponse response;

        SSLContextBuilder builder = new SSLContextBuilder();

        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());

        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build());

        HttpClientBuilder clientBuilder = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory);

        Set<Header> newHeaders;

        if (headers != null && headers.size() > 0) {
            newHeaders = new HashSet<>(headers);
        } else {
            newHeaders = new HashSet<>();
        }

        if (sessionId != null && sessionId.trim().length() > 0) {
            newHeaders.add(new BasicHeader(HttpHeaders.AUTHORIZATION, sessionId));
        }

        if (newHeaders.size() > 0) {
            clientBuilder.setDefaultHeaders(newHeaders);
        }

        CloseableHttpClient httpClient = clientBuilder.build();

        String fullPath = baseURI + path;

        if (params != null && params.size() > 0) {
            if (!fullPath.endsWith("?")) {
                fullPath += "?";
            }

            String paramString = formatParametersGet(params, "UTF-8", parameterBlankAsPlus);
//            String paramString = URLEncodedUtils.format(params, "UTF-8");

            fullPath += paramString;
        }

        HttpGet httpGet = new HttpGet(fullPath);

        LOGGER.debug("Execute request Trust Self-Signed GET " + httpGet.getURI().toString());

        response = httpClient.execute(httpGet);

        return response;
    } // end doGet()

    public HttpResponse doPutJson(String baseURI, String path, String jsonString, String sessionId) throws Exception {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build();

        HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        HttpPut httpPut = new HttpPut(baseURI + path);

        if (sessionId != null && sessionId.trim().length() > 0) {
            httpPut.setHeader(HttpHeaders.AUTHORIZATION, sessionId);
        }

        httpPut.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));

        LOGGER.debug("Execute request PUT " + httpPut.getURI().toString());

        return httpClient.execute(httpPut);
    } // end doPutJson(String, String)

    public HttpResponse doPostJson(String baseURI, String path, String jsonString, String sessionId) throws Exception {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build();

        HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        HttpPost httpPost = new HttpPost(baseURI + path);

        if (sessionId != null && sessionId.trim().length() > 0) {
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, sessionId);
        }

        httpPost.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));

        LOGGER.debug("Execute request POST " + httpPost.getURI().toString());

        return httpClient.execute(httpPost);
    } // end

    public HttpResponse doPostJson(String baseURI, String path, Set<Header> headers, String jsonString, String sessionId) throws Exception {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build();

        HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        HttpPost httpPost = new HttpPost(baseURI + path);

        if (sessionId != null && sessionId.trim().length() > 0) {
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, sessionId);
        }

        if (headers != null && headers.size() > 0) {
            for (Header header : headers) {
                httpPost.setHeader(header);
            }
        }

        httpPost.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));

        LOGGER.debug("Execute request POST " + httpPost.getURI().toString());

        return httpClient.execute(httpPost);
    } // end

    public HttpResponse doPost(String baseURI, String path, Set<Header> headers, List<NameValuePair> params, String sessionId) throws Exception {
        HttpResponse response;

        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build();
        HttpClientBuilder clientBuilder = HttpClientBuilder.create().setDefaultRequestConfig(config);

        Set<Header> newHeaders;

        if (headers != null && headers.size() > 0) {
            newHeaders = new HashSet<>(headers);
        } else {
            newHeaders = new HashSet<>();
        }

        if (sessionId != null && sessionId.trim().length() > 0) {
            newHeaders.add(new BasicHeader(HttpHeaders.AUTHORIZATION, sessionId));
        }

        if (newHeaders.size() > 0) {
            clientBuilder.setDefaultHeaders(newHeaders);
        }

        HttpClient httpClient = clientBuilder.build();

        String fullPath = baseURI + path;

        if (params != null && params.size() > 0) {
            if (!fullPath.endsWith("?")) {
                fullPath += "?";
            }

            String paramString = URLEncodedUtils.format(params, "UTF-8");

            fullPath += paramString;
        }

        HttpPost httpPost = new HttpPost(fullPath);

        LOGGER.debug("Execute request POST " + httpPost.getURI().toString());

        response = httpClient.execute(httpPost);

        return response;
    }

    public HttpResponse doDelete(String baseURI, String path, String sessionId) throws Exception {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build();

        HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        HttpDelete httpDelete = new HttpDelete(baseURI + path);

        if (sessionId != null && sessionId.trim().length() > 0) {
            httpDelete.setHeader(HttpHeaders.AUTHORIZATION, sessionId);
        }

        LOGGER.debug("Execute request DELETE " + httpDelete.getURI().toString());

        return httpClient.execute(httpDelete);
    }

    public HttpResponse doUploadFile(String baseURI, String path, Set<Header> headers, File uploadFile, String sessionId) throws Exception {
        HttpResponse response;

        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(Constants.CONNECT_TIMEOUT).build();

        HttpClientBuilder clientBuilder = HttpClientBuilder.create().setDefaultRequestConfig(config);

        Set<Header> newHeaders;

        if (headers != null && headers.size() > 0) {
            newHeaders = new HashSet<>(headers);
        } else {
            newHeaders = new HashSet<>();
        }

        if (sessionId != null && sessionId.trim().length() > 0) {
            newHeaders.add(new BasicHeader(HttpHeaders.AUTHORIZATION, sessionId));
        }

        /* content type */
        String mimeType;

        String fileExtension = FilenameUtils.getExtension(uploadFile.getName());

        if (fileExtension != null && fileExtension.trim().length() > 0 && mimeTypeBundle.containsKey(fileExtension)) {
            mimeType = mimeTypeBundle.getString(fileExtension);
        } else {
            mimeType = CONTENT_TYPE_UNKNOWN;
        }

        ContentType contentType = ContentType.create(mimeType);

        if (newHeaders.size() > 0) {
            clientBuilder.setDefaultHeaders(newHeaders);
        }

        HttpClient httpClient = clientBuilder.build();

        String fullPath = baseURI + path;

        HttpPost httpPost = new HttpPost(fullPath);

        FileEntity fileEntity = new FileEntity(uploadFile, contentType);
        fileEntity.setChunked(false);

        httpPost.setEntity(fileEntity);

        LOGGER.debug("Execute file upload: " + httpPost.getURI().toString());

        response = httpClient.execute(httpPost);

        return response;
    } // end doUploadFile()

    public boolean pingLugServer(String lugServerId) {
        boolean success;

        if (getRepositoryUseHttps()) {
            try {
                RequestConfig config = RequestConfig.custom().setSocketTimeout(Constants.SOCKET_TIMEOUT_TO_PING_LUG_SERVER).setConnectTimeout(Constants.CONNECT_TIMEOUT_TO_PING_LUG_SERVER).build();

                HttpClientBuilder clientBuilder = HttpClientBuilder.create().setDefaultRequestConfig(config);

                HttpClient httpClient = clientBuilder.build();

                String fullPath = "https://" + lugServerId + "." + Constants.DOMAIN_ZONE_NAME + ":" + Constants.FILELUG_SECURE_PORT + "/" + Constants.REPOSITORY_CONTEXT_PATH + "/" + Constants.LUG_SERVER_PING_PAGE;

                HttpGet httpGet = new HttpGet(fullPath);

                LOGGER.debug("ping lug server: " + httpGet.getURI().toString());

                HttpResponse response = httpClient.execute(httpGet);

                success = (response.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK);
            } catch (Exception e) {
                success = false;
                LOGGER.error("Error on ping lug server: " + lugServerId, e);
            }
        } else {
            success = Constants.AA_SERVER_ID_AS_LUG_SERVER.equals(lugServerId);
        }

        // TODO: record down and mail to administrator EVERY HOUR that the lug server is down
        // until the lug server starts up again

        return success;
    }

    // Copied and modified from #org.apache.http.client.utils.URLEncodedUtils
    public static String formatParametersGet(final List <? extends NameValuePair> parameters, final String charset, boolean blankAsPlus) {
        final StringBuilder result = new StringBuilder();

        Charset parameterCharset = charset != null ? Charset.forName(charset) : Charset.forName("UTF-8");

        for (final NameValuePair parameter : parameters) {
            final String encodedName = urlEncode(parameter.getName(), parameterCharset, URLENCODER, blankAsPlus);
            final String encodedValue = urlEncode(parameter.getValue(), parameterCharset, URLENCODER, blankAsPlus);

            if (result.length() > 0) {
                result.append(PARAMETER_SEPARATOR);
            }

            result.append(encodedName);

            if (encodedValue != null) {
                result.append(PARAMETER_NAME_VALUE_SEPARATOR);
                result.append(encodedValue);
            }
        }

        return result.toString();
    }

    // Copied and modified from #org.apache.http.client.utils.URLEncodedUtils
    private static String urlEncode(final String content, final Charset charset, final BitSet safechars, final boolean blankAsPlus) {
        if (content == null) {
            return null;
        }

        final StringBuilder buf = new StringBuilder();
        final ByteBuffer bb = charset.encode(content);
        while (bb.hasRemaining()) {
            final int b = bb.get() & 0xff;
            if (safechars.get(b)) {
                buf.append((char) b);
            } else if (blankAsPlus && b == ' ') {
                buf.append('+');
            } else {
                buf.append("%");
                final char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
                final char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
                buf.append(hex1);
                buf.append(hex2);
            }
        }

        return buf.toString();
    }
}
