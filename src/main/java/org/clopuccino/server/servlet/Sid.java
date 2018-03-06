package org.clopuccino.server.servlet;

/**
 * <code>Sid</code> lists ID of services between repository and server.
 *
 * @author masonhsieh
 * @version 1.0
 */
public interface Sid {

    /* ----------------------- VERSION 1 ----------------------- */

    int CONNECT = 1001;
    int CHECK_RECONNECT = 1003;
    int CHANGE_PASSWORD = 1004;
    int CHANGE_NICKNAME = 1005;
    int REGISTER = 1999;

    int LIST_ALL_BOOKMARKS = 2001;
    int FIND_BOOKMARK_BY_ID = 2003;
    int CREATE_BOOKMARK = 2004;
    int UPDATE_BOOKMARK = 2005;
    int DELETE_BOOKMARK_BY_ID = 2006;

    int SYNCHRONIZE_BOOKMARKS = 2605;

    int LIST_CHILDREN = 3002;
    int FIND_BY_PATH = 3003;
    int DOWNLOAD_FILE = 3004;
    int FILE_RENAME = 3005;
    int UPLOAD_FILE = 3006;
    int ALLOW_UPLOAD_FILE = 3007;
    int CONFIRM_UPLOAD_FILE = 3008;
    int ALLOW_DOWNLOAD_FILE = 3009;
    int CONFIRM_DOWNLOAD_FILE = 3010;
    int UPLOAD_FILE2 = 3011;
    int UPLOAD_FILE_GROUP = 3012;
    int DELETE_UPLOAD_FILE = 3013;
    int DOWNLOAD_FILE_GROUP = 3014;
    int DOWNLOAD_FILE2 = 3015;

    int LIST_ALL_ROOT_DIRECTORIES = 4001;

    int PING = 9001;
    int UPDATE_SOFTWARE = 9002;
    int NEW_SOFTWARE_NOTIFY = 9003;
    int UNSUPPORTED = 9999;


    /* ----------------------- VERSION 2 ----------------------- */

    int CONNECT_V2 = 21001;
    int CHECK_RECONNECT_V2 = 21003;
    int CHANGE_PASSWORD_V2 = 21004;
    int CHANGE_NICKNAME_V2 = 21005;
    int DELETE_COMPUTER_V2 = 21006;
    int CHANGE_COMPUTER_NAME_V2 = 21007;
    int REGISTER_V2 = 21999;

    int GET_QR_CODE_V2 = 21101;
    int LOGIN_BY_QR_CODE_V2 = 21102;

    int SYNCHRONIZE_BOOKMARKS_V2 = 22605;

//    int LIST_CHILDREN_V2 = 23002;
//    int FIND_BY_PATH_V2 = 23003;
//    int DOWNLOAD_FILE_V2 = 23004;
    int FILE_RENAME_V2 = 23005;
    int UPLOAD_FILE_V2 = 23006;
    int ALLOW_UPLOAD_FILE_V2 = 23007;
    int CONFIRM_UPLOAD_FILE_V2 = 23008;
    int ALLOW_DOWNLOAD_FILE_V2 = 23009;
    int CONFIRM_DOWNLOAD_FILE_V2 = 23010;
    int UPLOAD_FILE2_V2 = 23011;
    int UPLOAD_FILE_GROUP_V2 = 23012;
    int DELETE_UPLOAD_FILE_V2 = 23013;
    int DOWNLOAD_FILE_GROUP_V2 = 23014;
    int DOWNLOAD_FILE2_V2 = 23015;

    int LIST_ALL_ROOT_DIRECTORIES_V2 = 24001;

    int PING_V2 = 29001;
    int UPDATE_SOFTWARE_V2 = 29002;
    int NEW_SOFTWARE_NOTIFY_V2 = 29003;
    int UNSUPPORTED_V2 = 29999;
}
