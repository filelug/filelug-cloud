package org.clopuccino;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.clopuccino.dao.FileDownloadDao;
import org.clopuccino.dao.FileUploadDao;
import org.glassfish.jersey.uri.UriComponent;
import org.glassfish.jersey.uri.UriTemplate;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <code>Utility</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class Utility {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("UTILITY");

    private static final ExecutorService executor = Executors.newFixedThreadPool(Constants.N_FIXED_THREADS);

    private static final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(Constants.N_SCHEDULED_THREADS);

    private static int userVerifyCodeLength = Constants.SECURITY_CODE_LENGTH_MIN;

    private static String QR_CODE_PREFIX = "FILELUG_";


    public static ExecutorService getExecutorService() {
        return executor;
    }

    public static ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutor;
    }

    public static String realUrlEncode(final String value) {
        final String encodedValue;

        if (value != null) {
            String valueName = "_value_";

            String valueTemplate = "{" + valueName + "}";

            Map<String, String> templateMap = new HashMap<>();

            templateMap.put(valueName, value);

            encodedValue = UriTemplate.resolveTemplateValues(UriComponent.Type.PATH_SEGMENT, valueTemplate, true, templateMap);
        } else {
            encodedValue = null;
        }

        return encodedValue;
    }

    /**
     * 取得指定json中的sid。若找不到或者sid值不是整數，回傳null。
     *
     * @param json 指定json
     * @return 指定json中的sid
     */
    public static Integer findSidFromJson(String json) {
        Integer sid = null;

        try {
            ObjectMapper mapper = createObjectMapper();
            JsonNode jsonObject = mapper.readTree(json);
            JsonNode sidNode = jsonObject.findValue("sid");
            if (sidNode != null && sidNode.isNumber()) {
                sid = sidNode.intValue();
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Failed to find sid from JSON:%n%s", json), e);
        }


//        if (sid == null && json != null && json.trim().startsWith("\"") && json.trim().endsWith("\"")) {
//            // remove the double quotes that wraps the json string
//            // because ObjectMapper.readTree(String) failed to parse if the string is wrapped by double quotes.
//
//            // remove the double quotes wrapper
//            json = json.trim().substring(1, json.trim().length() - 1);
//
//            // replace the \" with "
//            json = StringUtils.replace(json, "\\\"", "\"");
//
//            sid = findSidFromJson(json);
//        }

        return sid;
    }

    public static String encodeUsingBase64(String rawString, String charset) throws UnsupportedEncodingException {
        return Base64.encodeBase64String(rawString.getBytes(charset));
    }

    public static String decodeBase64String(String base64String, String charset) throws UnsupportedEncodingException {
        return new String(Base64.decodeBase64(base64String), charset);
    }

    /**
     * String to long integer, possible zero or larger than zero.
     * @return -1 if string is not a number.
     */
    public static long positiveLongFromString(String string) {
        long value;
        try {
            value = Long.parseLong(string);
        } catch (Exception e) {
            value = -1;
        }

        return value;
    }

    public static String representationFileSizeFromBytes(final long value) {
        final long[] dividers = new long[]{Constants.T, Constants.G, Constants.M, Constants.K, 1};

        final String[] units = new String[]{"TB", "GB", "MB", "KB", "B"};

        if (value < 1) {
            return "";
        }

        String result = null;

        for (int i = 0; i < dividers.length; i++) {
            final long divider = dividers[i];
            if (value >= divider) {
                result = format(value, divider, units[i]);
                break;
            }
        }
        return result;
    }

    private static String format(final long value, final long divider, final String unit) {
        final double result = divider > 1 ? (double) value / (double) divider : (double) value;
        return String.format("%.2f %s", result, unit);
    }

    /**
     * All the <code>ObjectMapper</>s should be created from here.
     */
    public static ObjectMapper createObjectMapper() {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        jsonFactory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);

        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        
        // To ignore unknown properties when parsing from string to object
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }

    public static String generateDbPassword(String source) {
        return DigestUtils.md5Hex(DigestUtils.sha256Hex(source));
    }

    public static String generateVerification(String countryId, String phoneNumber, String password, String nickname) {
        return DigestUtils.sha256Hex(phoneNumber + "|" + password + ":" + countryId + "_" + nickname) + DigestUtils.md5Hex(nickname + "==" + password);
    }

    public static String generateVerificationForSecurityCode(String countryId, String phoneNumber) {
        String defaultUserId = "9413";

        return DigestUtils.sha256Hex(defaultUserId + "|" + countryId + ":" + phoneNumber) + DigestUtils.md5Hex(phoneNumber + "==" + countryId);
    }

    public static String generateVerificationForSecurityCode(String userId, String countryId, String phoneNumber) {
        return DigestUtils.sha256Hex(userId + "|" + countryId + ":" + phoneNumber) + DigestUtils.md5Hex(phoneNumber + "==" + countryId);
    }

//    public static String generateVerificationForChangeEmailSecurityCode(String userId, String countryId, String phoneNumber, String newEmail) {
//        return DigestUtils.sha256Hex(userId + "|" + countryId + ":" + phoneNumber) + DigestUtils.md5Hex(phoneNumber + "==" + newEmail);
//    }

    public static String generateVerificationForChangePhoneNumberSecurityCode(String userId, String countryId, String phoneNumber, String password) {
        return DigestUtils.sha256Hex(phoneNumber + "|" + password + ":" + countryId + "_" + userId) + DigestUtils.md5Hex(userId + "==" + password);
    }

    public static String generateVerification(String userId) {
        return DigestUtils.sha256Hex(userId + "|:_") + DigestUtils.md5Hex(userId + "==");
    }

    public static String generateResetPasswordVerification(String userId, String countryId, String phoneNumber, String password, String encryptedSecurityCode) {
        return generateVerificationWithSecurityCode(userId, countryId, phoneNumber, password, encryptedSecurityCode);
    }

    public static String generateChangePhoneNumberVerification(String userId, String countryId, String phoneNumber, String password, String encryptedSecurityCode) {
        return generateVerificationWithSecurityCode(userId, countryId, phoneNumber, password, encryptedSecurityCode);
    }

    public static String generateVerificationWithSecurityCode(String userId, String countryId, String phoneNumber, String password, String encryptedSecurityCode) {
        return DigestUtils.sha256Hex(phoneNumber + "|" + password + ":" + countryId + "_" + encryptedSecurityCode) + DigestUtils.md5Hex(userId + "==" + password);
    }

    public static String generateChangeComputerAdminVerification(String oldAdminUserId, String oldAdminPassword, String newAdminUserId, String newAdminPassword, Long computerId, String recoveryKey) {
        return DigestUtils.sha256Hex(newAdminUserId + "|" + newAdminPassword + ":" + oldAdminPassword + "_" + String.valueOf(computerId)) + DigestUtils.md5Hex(oldAdminUserId + "==" + recoveryKey);
    }

    public static String generateDeleteUserVerification(String userId, String password, String nickname, String sessionToken) {
        return DigestUtils.sha256Hex(userId + "|" + password + ":" + sessionToken) + DigestUtils.md5Hex(nickname + "==" + userId);
    }

    public static String generateDeleteUserVerification(String userId, String nickname, String sessionToken) {
        return DigestUtils.sha256Hex(userId + "|" + sessionToken + ":" + nickname) + DigestUtils.md5Hex(nickname + "==" + userId);
    }

    public static String generateVerification(String userId, String password, String nickname) {
        return DigestUtils.sha256Hex(userId + "|" + password + ":" + nickname) + DigestUtils.md5Hex(nickname + "==" + password);
    }

    public static String generateVerificationToLogin(String computerGroup, String computerName, String locale) {
        return DigestUtils.sha256Hex(computerName + "|" + computerGroup + ":" + locale + "_" + computerGroup);
    }

    public static String generateVerificationToLoginApplyUser(String adminId, String applyUserId, long computerId) {
        return DigestUtils.sha256Hex(adminId + "|" + applyUserId + ":" + computerId + "_" + adminId);
    }

    public static String generateUserComputerIdFrom(String userId, Long computerId) {
        return userId + Constants.COMPUTER_DELIMITERS + String.valueOf(computerId.longValue());
    }

    /**
     * Parses and return the user id, computer id from the user computer id string.
     * Ruturn null if value of the user computer id is not well-formed.
     *
     * @param userComputerId A string composed of user id and computer id.
     * @return An array of three strings, the first is the user id, the second is the computer id.
     */
    public static String[] decomposeUserComputerId(String userComputerId) {
        int indexOfDelimiters = userComputerId.indexOf(Constants.COMPUTER_DELIMITERS);

        /* index of delimiters must exist and can't be the first or the last char */
        if (indexOfDelimiters > 0 && indexOfDelimiters < userComputerId.length() - 1) {
            String userId = userComputerId.substring(0, indexOfDelimiters);
            String computerId = userComputerId.substring(indexOfDelimiters + 1, userComputerId.length());

            return new String[]{userId, computerId};
        }

        return null;
    }
//    public static String[] decomposeUserComputerId(String userComputerId) {
//        int indexOfDelimiters = userComputerId.indexOf(Constants.COMPUTER_DELIMITERS);
//
//        /* index of delimiters must exist and can't be the first or the last char */
//        if (indexOfDelimiters > 0 && indexOfDelimiters < userComputerId.length() - 1) {
//            String userId = userComputerId.substring(0, indexOfDelimiters);
//            String computerId = userComputerId.substring(indexOfDelimiters + 1, userComputerId.length());
//
//            String[] computerGroupAndName = decomposeComputerId(computerId);
//
//            if (computerGroupAndName != null) {
//                return new String[]{userId, computerGroupAndName[0], computerGroupAndName[1]};
//            }
//        }
//
//        return null;
//    }

    /**
     * Generates encrypted user computer key from user id and computer id.
     */
    public static String generateEncryptedUserComputerIdFrom(String userId, Long computerId) {
        return DigestUtils.sha256Hex(Constants.ENCRYPTED_USER_COMPUTER_ID_PREFIX
                                     + (userId + Constants.COMPUTER_DELIMITERS + String.valueOf(computerId)).toLowerCase()
                                     + Constants.ENCRYPTED_USER_COMPUTER_ID_SUFFIX);
    }

    /**
     * Generates encrypted user id from user id.
     */
    public static String generateEncryptedUserIdFrom(String userId) {
        return DigestUtils.sha256Hex(Constants.ENCRYPTED_USER_ID_PREFIX
                                     + userId.toUpperCase()
                                     + Constants.ENCRYPTED_USER_ID_SUFFIX);
    }

    public static String generateProductDetailIdFrom(String productId, String locale) {
        return productId + Constants.PRODUCT_DETAIL_ID_DELIMITERS + locale;
    }

    public static String generatePurchaseIdFrom(String productId, String userId) {
        return productId + Constants.PURCHASEL_ID_DELIMITERS + userId + Constants.PURCHASEL_ID_DELIMITERS + System.currentTimeMillis();
    }

//    public static Properties readHttpConfiguration() {
//        return readConfiguration("http.ini");
//    }

    public static Properties readHttpsConfiguration() {
        return readConfiguration("ssl.ini");
    }

    /**
     * Gets properties from files in directory start.d
     * If file not found, return null.
     * If file exists but failed to load properties, return empty properties.
     */
    private static Properties readConfiguration(String filename) {
        // DEBUG
//        System.out.println("jetty.base=" + System.getProperty("jetty.base"));

        Properties properties = null;

        String configurationDirectory = System.getProperty("jetty.base");

        File startDirectory = new File(configurationDirectory, "start.d");

        File configurationFile = new File(startDirectory, filename);

        if (configurationFile.exists() && configurationFile.isFile()) {
            properties = readConfiguration(startDirectory, filename);
        } else {
            configurationDirectory = System.getProperty("user.dir");

            startDirectory = new File(configurationDirectory, "start.d");

            configurationFile = new File(startDirectory, filename);

            if (configurationFile.exists() && configurationFile.isFile()) {
                properties = readConfiguration(startDirectory, filename);
            } else {
                LOGGER.debug("Configuration file not found: " + configurationFile.getAbsolutePath());
            }
        }

        return properties;
    }

    // Before using jetty.base instead of configuration.directory
//    private static Properties readConfiguration(String filename) {
//        Properties properties = null;
//
//        String configurationDirectory = System.getProperty("configuration.directory");
//
//        File startDirectory = new File(configurationDirectory, "start.d");
//
//        File configurationFile = new File(startDirectory, filename);
//
//        if (configurationFile.exists() && configurationFile.isFile()) {
//            properties = readConfiguration(startDirectory, filename);
//        } else {
//            configurationDirectory = System.getProperty("user.dir");
//
//            startDirectory = new File(configurationDirectory, "start.d");
//
//            configurationFile = new File(startDirectory, filename);
//
//            if (configurationFile.exists() && configurationFile.isFile()) {
//                properties = readConfiguration(startDirectory, filename);
//            } else {
//                LOGGER.debug("Configuration file not found: " + configurationFile.getAbsolutePath());
//            }
//        }
//
//        return properties;
//    }

    private static Properties readConfiguration(File directory, String filename) {
        File configurationFile = new File(directory, filename);

        LOGGER.debug("Reading configuration file: " + configurationFile.getAbsolutePath());

        Properties properties = new Properties();

        InputStreamReader reader = null;

        try {
            reader = new InputStreamReader(new FileInputStream(configurationFile), "UTF-8");

            properties.load(reader);
        } catch (Exception e) {
            properties.clear();

            LOGGER.error("Failed to load file: " + configurationFile, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                        /* ignored */
                }
            }
        }

        return properties;
    }

    public static void shutdownAndAwaitTermination(ExecutorService pool, long awaitTimeout, TimeUnit awaitTimeUnit) {
        pool.shutdown(); // Disable new tasks from being submitted

        try {
            /* Wait a while for existing tasks to terminate */
            if (!pool.awaitTermination(awaitTimeout, awaitTimeUnit)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
//                if (!pool.awaitTermination(awaitTimeout, awaitTimeUnit))
//                    System.err.println("Pool did not terminate");
            }
        } catch (Throwable e) {
            try {
                pool.shutdownNow();
            } catch (Throwable t) {
                // ignored
            }
        }
    }

    public static void shutdownAndAwaitTermination(ScheduledExecutorService pool, long awaitTimeout, TimeUnit awaitTimeUnit) {
        pool.shutdown(); // Disable new tasks from being submitted

        try {
            /* Wait a while for existing tasks to terminate */
            if (!pool.awaitTermination(awaitTimeout, awaitTimeUnit)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
//                if (!pool.awaitTermination(awaitTimeout, awaitTimeUnit))
//                    System.err.println("Pool did not terminate");
            }
        } catch (Throwable e) {
            try {
                pool.shutdownNow();
            } catch (Throwable t) {
                // ignored
            }
        }
    }

    public static long lastWeekTimestamp() {
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_WEEK, -7);

        return calendar.getTimeInMillis();
    }

    public static long lastMonthTimestamp() {
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.MONTH, -1);

        return calendar.getTimeInMillis();
    }

    /**
     * Prepares the desktop software name.
     *
     * @param version software version. Such as 1.0.0, 1.1.0, 2.0.0
     * @param os including windows, mac, and linux. All are lower-cases
     * @param arch including x86 or x64.
     *
     * @return The desktop software name.
     */
    public static String prepareDesktopSoftwareName(String version, String os, String arch) {
        StringBuilder builder = new StringBuilder("fdesktop-" + version + "-");

        String suffix;
        if (os.toLowerCase().contains("win")) {
            builder.append("windows-");
            suffix = ".setup.exe.zip";
        } else if (os.toLowerCase().contains("mac")) {
            builder.append("mac-");
            suffix = ".app.zip";
        } else {
            builder.append("linux-");
            suffix = "-self-extract.zip";
        }

        if (arch.toLowerCase().contains("64")) {
            return builder.append("x64").append(suffix).toString();
        } else {
            return builder.append("x86").append(suffix).toString();
        }
    }

    public static String generateSecurityCode() {
        final String verifyCode = RandomStringUtils.random(userVerifyCodeLength++, false, true);

        if (userVerifyCodeLength > Constants.SECURITY_CODE_LENGTH_MAX) {
            userVerifyCodeLength = Constants.SECURITY_CODE_LENGTH_MIN;
        }

        return verifyCode;
    }

    public static void prepareResponseHeader(HttpServletResponse resp, String uploadKey, String transferStatus, String errorMessage) {
        if (uploadKey != null && uploadKey.trim().length() > 0) {
            resp.setHeader(Constants.HTTP_HEADER_NAME_UPLOAD_KEY, uploadKey);
        }

        if (transferStatus != null && transferStatus.trim().length() > 0) {
            resp.setHeader(Constants.HTTP_HEADER_NAME_UPLOAD_RESULT_STATUS, transferStatus);
        }

        if (errorMessage != null && errorMessage.trim().length() > 0) {
            resp.setHeader(Constants.HTTP_HEADER_NAME_UPLOAD_RESULT_ERROR_MESSAGE, errorMessage);
        }
    }

    public static boolean isAAServer() {
        String value = System.getProperty("run.as.AA");

        return value != null && value.trim().toLowerCase().equals("true");
    }

    public static void deleteUploadTmpFileAndUpdateRecord(String tmpFileAbsolutePath, FileUploadDao fileUploadDao, String uploadKey) {
        if (tmpFileAbsolutePath != null) {
            File tmpFile = new File(tmpFileAbsolutePath);

            if (tmpFile.exists() && tmpFile.isFile()) {
                try {
                    if (tmpFile.delete()) {
                        LOGGER.debug("Deleted tmp file: " + tmpFileAbsolutePath);

                        if (fileUploadDao != null && uploadKey != null) {
                            fileUploadDao.updateFileUploadTmpFileDeletedTimestamp(uploadKey, System.currentTimeMillis());
                        }
                    }
                } catch (Exception e2) {
                    LOGGER.error("Failed to delete file: \"" + tmpFileAbsolutePath + "\". You need to delete it manually.", e2);
                }
            } else {
                LOGGER.debug("File: \"" + tmpFileAbsolutePath + "\" not exists or is a directory.");
            }
        }
    }

    public static void deleteDownloadTmpFileAndUpdateRecord(String tmpFileAbsolutePath, FileDownloadDao fileDownloadDao, String downloadKey) {
        if (tmpFileAbsolutePath != null) {
            File tmpFile = new File(tmpFileAbsolutePath);

            if (tmpFile.exists() && tmpFile.isFile()) {
                try {
                    if (tmpFile.delete()) {
                        LOGGER.debug("Deleted tmp file: " + tmpFileAbsolutePath);

                        if (fileDownloadDao != null && downloadKey != null) {
                            fileDownloadDao.updateFileDownloadTmpFileDeletedTimestamp(downloadKey, System.currentTimeMillis());
                        }
                    }
                } catch (Exception e2) {
                    LOGGER.error("Failed to delete file: \"" + tmpFileAbsolutePath + "\". You need to delete it manually.", e2);
                }
            } else {
                LOGGER.debug("File: \"" + tmpFileAbsolutePath + "\" not exists or is a directory.");
            }
        }
    }

    public static String genereateTmpFileSuffix(String filename) {
        return "_" + filename;
    }


    public static void closeResource(Closeable closeableResource) {
        if (closeableResource != null) {
            try {
                closeableResource.close();
            } catch (Exception e) {
                // ignored
            }
        }
    }

    public static String generateQRCode(String computerGroup, String computerName, String locale, long timestamp) {
        return QR_CODE_PREFIX + DigestUtils.sha256Hex(computerName + "|" + computerGroup + ":" + timestamp + "_" + locale);
    }

    public static String generateVerificationToDeleteComputer(String userId, Long computerId) {
        String hash = DigestUtils.md5Hex(userId + "==" + computerId);

        return DigestUtils.sha256Hex(userId + "|" + hash + ":" + computerId + "_" + hash);
    }

    public static String generateVerificationToCheckComputerExists(String userId, Long computerId, String recoveryKey) {
        return DigestUtils.sha256Hex(recoveryKey + "|" + userId + ":" + computerId + "_" + userId);
    }

    public static String generateVerificationForExchangeAccessToken(String authorizationCode, String locale) {
        String authorizationCodeUpperCase = authorizationCode.toUpperCase();

        String authorizationCodeLowerCase = authorizationCode.toLowerCase();

        String hash = DigestUtils.md5Hex(authorizationCodeUpperCase + "==" + locale);

        return DigestUtils.sha256Hex(authorizationCodeLowerCase + "|" + hash + ":" + locale + "_" + hash);
    }

    public static String generateRemoveAdminVerification(String userId, Long computerId, String sessionId) {
        return DigestUtils.sha256Hex(sessionId + "|" + userId + ":" + computerId + "_" + userId);
    }
}
