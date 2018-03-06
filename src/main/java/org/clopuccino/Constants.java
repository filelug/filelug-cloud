package org.clopuccino;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * <code>Constants</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public interface Constants {

    // TODO: Move url-related to DB

    String DOMAIN_ZONE_NAME = "filelug.com";

    String FILELUG_AA_SERVER_HOSTNAME = "repo." + DOMAIN_ZONE_NAME;
    String FILELUG_NON_SECURE_PORT = "8080";
    String FILELUG_SECURE_PORT = "443";
    String REPOSITORY_CONTEXT_PATH = "crepo";
    String LUG_SERVER_PING_PAGE = "index.jsp";

    /* initial delay in seconds to start deleting timeout client sessions - 70 mins
     * The value should be shorter then INIT_DELAY_DELETE_TIMEOUT_CLIENT_SESSION_IN_SECONDS
     * to prevent newly created client session with error code of 503
     */
    Integer INIT_DELAY_DELETE_TIMEOUT_CONNECT_SOCKET_IN_SECONDS = 4200;

    /* initial delay in seconds to start deleting timeout client sessions - 80 mins */
    Integer INIT_DELAY_DELETE_TIMEOUT_CLIENT_SESSION_IN_SECONDS = 4800;

    /* initial delay in seconds to start deleting timeout unverified users - 90 mins */
    Integer INIT_DELAY_DELETE_TIMEOUT_UNVERIFIED_USER_IN_SECONDS = 5400;

    /* initial delay in seconds to start deleting timeout file transfers - 100 mins */
    int INIT_DELAY_DELETE_TIMEOUT_FILE_TRANSFER_IN_SECONDS = 6000;


    // Connect timeout is the timeout until a connection with the server is established.
    int CONNECT_TIMEOUT = 60000; // in millisecond, 60 seconds

    /* SO_TIMEOUT is the timeout for waiting for data or, put differently,
     * a maximum period inactivity between two consecutive data packets
     */
    int SOCKET_TIMEOUT = 60000; // in millisecond, 60 seconds

    int SOCKET_TIMEOUT_TO_PING_LUG_SERVER = 2000; // in millisecond, 2 seconds

    int CONNECT_TIMEOUT_TO_PING_LUG_SERVER = 2000; // in millisecond, 2 seconds

    long ASYNC_CONTEXT_TIMEOUT_IN_MILLISECONDS_FOR_TRANSFER_FILE = 60 * 60 * 1000;

    // the time in seconds for a client session should be deleted from db - 7 days
    Integer CLIENT_SESSION_TO_DELETE_IN_SECONDS = 60 * 60 * 24 * 7;

    // client session idle timeout in seconds - 1 hour
    Integer DEFAULT_CLIENT_SESSION_IDLE_TIMEOUT_IN_SECONDS = 3600;

    /* connect socket: socket timeout in seconds */
    Integer IDLE_TIMEOUT_IN_SECONDS_TO_CONNECT_SOCKET = 3600; // 1 hour

    // max waiting for close latch in seconds

//    // DEBUG: Change back with the code below when in production
//    int DEFAULT_TRANSFER_FILE_CLOSE_LATCH_WAIT_IN_SECONDS = 60;
    // This is the production code. Replace the code above with this when in production
    int DEFAULT_TRANSFER_FILE_CLOSE_LATCH_WAIT_IN_SECONDS = 3600;

    // seconds
    int KEEP_ALIVE_TIMEOUT = 60;

    int MAX_KEEP_ALIVE_REQUESTS = 5;

    String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss 'GMT'Z";

    SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);

    SimpleDateFormat PSEUDO_COMPUTER_ID_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    // tiemout in seconds for a file download or upload - replaced by DEFAULT_TRANSFER_FILE_CLOSE_LATCH_WAIT_IN_SECONDS
    // a little less than 1 hour
//    int DEFAULT_TRANSFER_FILE_CONTENT_TIME_OUT_IN_SECONDS = 3590;

//    int FILE_TRANSFER_TIME_OUT_STATUS_UPDATE_TIME_INTERVAL_IN_SECONDS = (int) (DEFAULT_TRANSFER_FILE_CONTENT_TIME_OUT_IN_SECONDS * 1.1);

    /* connect socket timeout in seconds - 1 hour */
    Integer DEFAULT_CONNECT_SOCKET_IDLE_TIMEOUT_IN_SECONDS = 3600; // 1 hour

    // Reserved for max length of a file name is 255. 64 bytes means length of 128
    Integer DEFAULT_CLIENT_SESSION_ID_BYTE_COUNT = 64;

    // Reserved for max length of a file name is 255. 64 bytes means length of 128
    Integer DEFAULT_USER_ID_BYTE_COUNT = 64;

    /* interval to delete invalid client sessions, in seconds - 8 days */
    Integer DEFAULT_DELETE_INVALID_CLIENT_SESSION_INTERVAL = 60 * 60 * 24 * 8;
//    Integer DEFAULT_DELETE_INVALID_CLIENT_SESSION_INTERVAL = (int) (DEFAULT_CLIENT_SESSION_IDLE_TIMEOUT_IN_SECONDS * 1.5);

    /* interval to delete invalid connect socket, in seconds - 1 hour */
    Integer DEFAULT_DELETE_INVALID_CONNECT_SOCKET_INTERVAL = 3600;

    String HTTP_HEADER_FILELUG_SESSION_ID_NAME = "fsi";

    String HTTP_HEADER_AUTHORIZATION_NAME = "Authorization";

    String HTTP_HEADER_NAME_UPLOAD_KEY = "upkey";

    String HTTP_HEADER_NAME_UPLOAD_GROUP_ID = "upload-group-id";

    String HTTP_HEADER_NAME_UPLOAD_DIRECTORY = "updir";

    String HTTP_HEADER_NAME_UPLOAD_FILE_NAME = "upname";

    String HTTP_HEADER_NAME_UPLOAD_FILE_SIZE = "upsize";

    // either DatabaseConstants.TRANSFER_STATUS_SUCCESS or DatabaseConstants.TRANSFER_STATUS_FAILURE
    String HTTP_HEADER_NAME_UPLOAD_RESULT_STATUS = "upstatus";

    String HTTP_HEADER_NAME_UPLOAD_RESULT_ERROR_MESSAGE = "uperror";

    String HTTP_HEADER_NAME_CHANGE_TIMESTAMP = "Change-Timestamp";

    String HTTP_HEADER_NAME_FILE_CONTENT_RANGE = "File-Content-Range";

    String HTTP_HEADER_NAME_FILE_LAST_MODIFIED = "File-Last-Modified";

    String HTTP_HEADER_NAME_FILE_RANGE = "File-Range";

    String HTTP_HEADER_NAME_UPLOADED_BUT_UNCONFIRMED = "uploaded_but_uncomfirmed";

    String HTTP_PARAM_NAME_TRANSFER_KEY = "t";

    /* The value must be set larger than 45 sec (reconnect) and smaller than 60 sec (device socket timeout).
     * So we can distinguish desktop timeout from repository timeout.
     */
    int DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS = 55;
    int DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_MILLIS = DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS * 1000;

//    int DEFAULT_CONNECT_SOCKET_WAITING_TIME_OUT_IN_SECONDS = 60;

    // Set to the same value in {jetty.home}/start.init
//    int DEFAULT_TRANSFER_FILE_BUFFER_SIZE_IN_BYTES = 32768;
    int DEFAULT_TRANSFER_FILE_BUFFER_SIZE_IN_BYTES = 4096;

    String CONTENT_TYPE_JSON_UTF8 = "application/json;charset=UTF-8";

    String CONTENT_TYPE_TEXT_PLAIN_UTF8 = "text/plain; charset=utf-8";

    String CONTENT_TYPE_DEFAULT_UPLOAD_FILE_TO_SERVER = "application/octet-stream";

    String TMP_UPLOAD_FILE_PREFIX = "fupload_";

    String TMP_DOWNLOAD_FILE_PREFIX = "fdownload_";

    int N_FIXED_THREADS = 1000;

    int N_SCHEDULED_THREADS = 20;

    long CHECK_CONNECT_INTERVAL_IN_MILLIS = 5 * 1000;
//    long CHECK_CONNECT_INTERVAL_IN_MILLIS = 15 * 1000;

    int CHECK_CONNECT_TIMES = 9;

    String BASE64_CONVERSION_CHARSET = "UTF-8";

    /* Timeout unverified user - 1 days */
    Integer TIMEOUT_UNVERIFIED_USER_IN_SECONDS = 86400;

    /* interval to delete timeout unverified user, in seconds - 1 hour */
    Integer DEFAULT_DELETE_TIMEOUT_UNVERIFIED_USER_INTERVAL = 3600;

    long K = 1024;

    long M = K * K;

    long G = M * K;

    long T = G * K;

    String ENCRYPTED_USER_COMPUTER_ID_PREFIX = "@";

    String ENCRYPTED_USER_COMPUTER_ID_SUFFIX = "#";

    String ENCRYPTED_USER_ID_PREFIX = ")@";

    String ENCRYPTED_USER_ID_SUFFIX = "#(";

    String USER_ACCOUNT_DELIMITERS = "-";

    String COMPUTER_DELIMITERS = "|";

    int MIN_COMPUTER_NAME_LENGTH = 6;

    int MAX_COMPUTER_NAME_LENGTH = 20;

    String PRODUCT_DETAIL_ID_DELIMITERS = "|";

    String PURCHASEL_ID_DELIMITERS = "|";

    /* pretend 10 GB left for unlimited-transfer user */
    long FAKE_AVAILABLE_BYTES_FOR_UNLIMITED_TRANSFER_USER = 10737418240L; // 10 * 1024 * 1024 * 1024L;

    String AA_SERVER_ID_AS_LUG_SERVER = "aa";

    long DEFAULT_AWAIT_TERMINATION_TIMEOUT_IN_SECONDS = 10;

//    /* types of download/upload history time range */
//
//    int TRANSFER_HISTORY_TYPE_LATEST_20 = 0;
//
//    int TRANSFER_HISTORY_TYPE_LATEST_WEEK = 1;
//
//    int TRANSFER_HISTORY_TYPE_LATEST_MONTH = 2;
//
//    int TRANSFER_HISTORY_TYPE_ALL = 3;

//    String PROPERTY_KEY_DESKTOP_LOCALE = "desktop.locale";

    String PROPERTY_KEY_FILELUG_DESKTOP_VERSION = "desktop.version";

    String DEFAULT_DESKTOP_VERSION = "1.0.0";

    String INITIAL_VERSION_TO_V2 = "2.0.0";

    String VERSION_REG_EXP = "^\\d+(\\.\\d+)*";
//    String VERSION_REG_EXP = "^\\d+(\\.\\d+)+";

    String FILELUG_PROPERTY_KEY_DESKTOP_LATEST_VERSION = "desktop_latest_version";

    String FILELUG_PROPERTY_KEY_DESKTOP_DOWNLOAD_URL = "desktop_download_url";

    String FILELUG_PROPERTY_KEY_LUG_SERVER_DOMAIN_NAMES = "lug_server_domain_names";

    String FILELUG_PROPERTY_VALUE_LUG_SERVER_DOMAIN_NAMES_DELIMITERS = ",";

    String DEFAULT_DESKTOP_DOWNLOAD_URL = "http://www." + DOMAIN_ZONE_NAME + "/get/index.html";

    int SECURITY_CODE_LENGTH_MIN = 4;

    int SECURITY_CODE_LENGTH_MAX = 6;

    Integer SMS_MESSAGE_REMAIN_COUNT_THRESHHOLD = 500;

    
    // Separated by one whitespace
    String ADMINISTRATORS_EMAILS = "masonhsieh@filelug.com benius@gmail.com cys0001@gmail.com";


    // names of the tasks

    String TASK_NAME_UPDATE_AVAILABLE_COUNTRIES = "repo.each.update.available.countries";

    String TASK_NAME_UPDATE_AVAILABLE_LUG_SERVERS = "repo.each.update.available.lug.servers";

    String TASK_NAME_RELOAD_SNS_APPLICATIONS = "repo.each.reload.sns.applications";

    
    // supported locales

    Locale[] SUPPORTED_LOCALES = {
            Locale.ENGLISH,
            Locale.CHINESE,
            Locale.TAIWAN,
            new Locale("zh", "HK"),
//            Locale.SIMPLIFIED_CHINESE, Locale.JAPANESE
    };

    Locale DEFAULT_LOCALE_FOR_SUPPORTED_LOCALES = Locale.ENGLISH;

    // CUSTOMIZED HTTP RESPONSE STATUS

    /*
     * 430: User already registered
     */
    Integer HTTP_STATUS_USER_ALREADY_REGISTERED = 430;

    /*
     * 431: Incorrect security code for such as confirming registration or reset password.
     */
    Integer HTTP_STATUS_INCORRECT_SECURITY_CODE = 431;

    /*
     * 432: User not registered
     */
    Integer HTTP_STATUS_USER_NOT_REGISTERED = 432;

    /*
     *  460: computer not found
     */
    Integer HTTP_STATUS_COMPUTER_NOT_FOUND = 460;

    /*
     *  461: the specified user is not the administrator of the computer
     */
    Integer HTTP_STATUS_USER_NOT_ADMIN = 461;

    /*
     *  462: the specified user not apply connection to the computer yet
     */
    Integer HTTP_STATUS_USER_NOT_APPLY_CONNECTION_YET = 462;

    /*
     *  463: the specified user have't been approved to access the computer
     */
    Integer HTTP_STATUS_APPLY_CONNECTION_NOT_APPROVED_YET = 463;

    /*
     *  464: the specified country is not supported yet
     */
    Integer HTTP_STATUS_COUNTRY_NOT_SUPPORTED = 464;

    /*
     *  465: the version of the desktop is too old and the desktop application needs upgrade
     */
    Integer HTTP_STATUS_DESKTOP_VERSION_TOO_OLD = 465;

    /*
     *  466: the version of the device is too old and the device app needs upgrade
     */
    Integer HTTP_STATUS_DEVICE_VERSION_TOO_OLD = 466;

    /*
     *  467: the phone number has been taken and need to change a new one
     */
    Integer HTTP_STATUS_SHOULD_UPDATE_PHONE_NUMBER = 467;

    /*
     *  468: user's email address is empty.
     */
    Integer HTTP_STATUS_EMPTY_USER_EMAIL = 468;

    /*
     *  469: user is an administrator of a computer and
     *  there's at least one non-administrator user allowed to connect to this computer.
     */
    Integer HTTP_STATUS_USER_ALLOWED_NON_ADMIN_USERS = 469;

    /*
     *  470: file size is larger than the download/upload file size limit.
     */
    Integer HTTP_STATUS_FILE_SIZE_LIMIT_EXCEEDED = 470;

    /*
     *  471: Incompatible version. Software needs update.
     */
    Integer HTTP_STATUS_INCOMPATIBLE_VERSION = 471;

    /*
     * 499: request closed by user
     */
    Integer HTTP_STATUS_CLIENT_CLOSE_REQUEST = 499;


    // notification messages
    String NOTIFICATION_MESSAGE_KEY_TYPE = "fl-type";

    String NOTIFICATION_MESSAGE_TYPE_UPLOAD_FILE = "upload-file";

    String NOTIFICATION_MESSAGE_TYPE_ALL_FILES_UPLOADED_SUCCESSFULLY = "all-files-uploaded-successfully";

    String NOTIFICATION_MESSAGE_KEY_TRANSFER_KEY = "transfer-key";

    String NOTIFICATION_MESSAGE_KEY_TRANSFER_STATUS = "transfer-status";

    String NOTIFICATION_MESSAGE_KEY_UPLOAD_GROUP_ID = "upload-group-id";

    String NOTIFICATION_CATEGORY_FILE_UPLOAD = "file_upload";

    String NOTIFICATION_CATEGORY_APPLY_ACCEPTED= "apply_accepted";

    String NOTIFICATION_CATEGORY_APPLY_TO_ADMIN= "apply_to_admin";

    // Facebook Application-Related
    String FACEBOOK_FILELUG_APP_ID = "553710028164970";

    String FACEBOOK_ACCOUNT_KIT_API_VERSION = "v1.0";

    String FACEBOOK_ACCOUNT_KIT_APP_SECRETE = "15a942a78a6092730d925bc05a2c623c";
}
