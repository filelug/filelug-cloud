package org.clopuccino.db;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public interface DatabaseConstantsUpperCase {

    /* FIX: change all values to lower-case for postgresql default settings */

    /* TABLE: REGION related */

    String TABLE_NAME_COUNTRY = "COUNTRY";

    String COLUMN_NAME_COUNTRY_CODE = "C_CODE";

    String COLUMN_NAME_COUNTRY_PHONE_SAMPLE = "C_PHONE_SAMPLE";

    String COLUMN_NAME_COUNTRY_ID = "C_ID";

    String COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX = "L_";

    String COLUMN_NAME_COUNTRY_LOCALE_COLUMN_DEFAULT_NAME = "DEFAULT";

    String COLUMN_NAME_COUNTRY_LOCALE_DEFAULT = COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX + COLUMN_NAME_COUNTRY_LOCALE_COLUMN_DEFAULT_NAME;

    String COLUMN_NAME_COUNTRY_LOCALE_EN = COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX + "EN";

    String COLUMN_NAME_COUNTRY_LOCALE_ZH = COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX + "ZH";

    String COLUMN_NAME_COUNTRY_LOCALE_ZH_TW = COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX + "ZH_TW";

    String COLUMN_NAME_COUNTRY_LOCALE_ZH_HK = COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX + "ZH_HK";

    String COLUMN_NAME_COUNTRY_LOCALE_ZH_CN = COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX + "ZH_CN";

    String COLUMN_NAME_COUNTRY_LOCALE_JA_JP = COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX + "JA_JP";

    // Add other region locales here, the value MUST start with "L_" and then the locale with CASE-INSENSETIVE

    String COLUMN_NAME_COUNTRY_AVAILABLE = "C_AVAILABLE";

    String FILE_NAME_DEFAULT_COUNTRIES = "default-countries.txt";

    String FILE_DEFAULT_COUNTRIES_DELIMITERS = "\t";

    String FILE_DEFAULT_COUNTRIES_COMMENT_CHARACTER = "#";

    /* TABLE: USER related */

    String TABLE_NAME_AUTH_USER = "AUTH_USER";

    String COLUMN_NAME_AUTH_USER_ID = "AUTH_USER_ID";

    String COLUMN_NAME_PHONE_NUMBER = "PHONE_NUMBER";

    String COLUMN_NAME_PASSWD = "PASSWD";

    String COLUMN_NAME_NICKNAME = "NICKNAME";

    String COLUMN_NAME_SHOW_HIDDEN = "SHOW_HIDDEN";

    String COLUMN_NAME_AUTH_USER_VERIFIED = "AUTH_USER_VERIFIED";

//    String COLUMN_NAME_AUTH_USER_ID_ENCRYPTED = "AUTH_USER_ENCRYPTED";

    String COLUMN_NAME_VERIFY_CODE = "VERIFY_CODE";

    String COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP = "SENT_TIMESTAMP";

    String COLUMN_NAME_AVAILABLE_TRANSFER_BYTES = "AT_BYTES";

    /* initial available transfer bytes -- 2 GB */
    long DEFAULT_INITIAL_USER_AVAILABLE_TRANSFER_BYTES = 2147483648L;

//    String COLUMN_NAME_RECONNECT = "RECONNECT";

    /* DDL - TABLE: COUNTRY */
    String SQL_CREATE_TABLE_COUNTRY = "CREATE TABLE " + TABLE_NAME_COUNTRY + "("
                                      + COLUMN_NAME_COUNTRY_ID + " VARCHAR(8) PRIMARY KEY, "
                                      + COLUMN_NAME_COUNTRY_CODE + " INTEGER NOT NULL, "
                                      + COLUMN_NAME_COUNTRY_PHONE_SAMPLE + " VARCHAR(24) NOT NULL, "
                                      + COLUMN_NAME_COUNTRY_LOCALE_DEFAULT + " VARCHAR(1024) NOT NULL, "
                                      + COLUMN_NAME_COUNTRY_LOCALE_EN + " VARCHAR(1024) NOT NULL, "
                                      + COLUMN_NAME_COUNTRY_LOCALE_ZH + " VARCHAR(1024) NOT NULL, "
                                      + COLUMN_NAME_COUNTRY_LOCALE_ZH_TW + " VARCHAR(1024) NOT NULL, "
                                      + COLUMN_NAME_COUNTRY_LOCALE_ZH_HK + " VARCHAR(1024) NOT NULL, "
                                      + COLUMN_NAME_COUNTRY_LOCALE_ZH_CN + " VARCHAR(1024) NOT NULL, "
                                      + COLUMN_NAME_COUNTRY_LOCALE_JA_JP + " VARCHAR(1024) NOT NULL, "
                                      + COLUMN_NAME_COUNTRY_AVAILABLE + " BOOLEAN DEFAULT false)";

    String SQL_TRUNCATE_TABLE_COUNTRIES = "TRUNCATE TABLE " + TABLE_NAME_COUNTRY;

    String SQL_CREATE_COUNTRY = "INSERT INTO " + TABLE_NAME_COUNTRY + "("
                                + COLUMN_NAME_COUNTRY_ID + ", "
                                + COLUMN_NAME_COUNTRY_CODE + ", "
                                + COLUMN_NAME_COUNTRY_PHONE_SAMPLE + ", "
                                + COLUMN_NAME_COUNTRY_LOCALE_DEFAULT + ", "
                                + COLUMN_NAME_COUNTRY_LOCALE_EN + ", "
                                + COLUMN_NAME_COUNTRY_LOCALE_ZH + ", "
                                + COLUMN_NAME_COUNTRY_LOCALE_ZH_TW + ", "
                                + COLUMN_NAME_COUNTRY_LOCALE_ZH_HK + ", "
                                + COLUMN_NAME_COUNTRY_LOCALE_ZH_CN + ", "
                                + COLUMN_NAME_COUNTRY_LOCALE_JA_JP + ", "
                                + COLUMN_NAME_COUNTRY_AVAILABLE + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String SQL_FIND_AVAILABLE_COUNTRIES = "SELECT "
                                          + COLUMN_NAME_COUNTRY_ID + ", "
                                          + COLUMN_NAME_COUNTRY_CODE + ", "
                                          + COLUMN_NAME_COUNTRY_PHONE_SAMPLE + ", "
                                          + COLUMN_NAME_COUNTRY_LOCALE_DEFAULT + ", "
                                          + COLUMN_NAME_COUNTRY_LOCALE_EN + ", "
                                          + COLUMN_NAME_COUNTRY_LOCALE_ZH + ", "
                                          + COLUMN_NAME_COUNTRY_LOCALE_ZH_TW + ", "
                                          + COLUMN_NAME_COUNTRY_LOCALE_ZH_HK + ", "
                                          + COLUMN_NAME_COUNTRY_LOCALE_ZH_CN + ", "
                                          + COLUMN_NAME_COUNTRY_LOCALE_JA_JP + ", "
                                          + COLUMN_NAME_COUNTRY_AVAILABLE
                                          + " FROM "
                                          + TABLE_NAME_COUNTRY
                                          + " WHERE "
                                          + COLUMN_NAME_COUNTRY_AVAILABLE + " = true";

    /* DDL - TABLE: AUTH_USER */

    /* MAKE SURE false is supported for non-sql db */
    String SQL_CREATE_TABLE_AUTH_USER = "CREATE TABLE " + TABLE_NAME_AUTH_USER + "("
                                        + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) PRIMARY KEY, "
                                        + COLUMN_NAME_COUNTRY_ID + " VARCHAR(8) NOT NULL, "
                                        + COLUMN_NAME_PHONE_NUMBER + " VARCHAR(24) NOT NULL, "
                                        + COLUMN_NAME_PASSWD + " VARCHAR(1024) NOT NULL, "
                                        + COLUMN_NAME_NICKNAME + " VARCHAR(1024) NOT NULL, "
                                        + COLUMN_NAME_SHOW_HIDDEN + " BOOLEAN DEFAULT false, "
                                        + COLUMN_NAME_AUTH_USER_VERIFIED + " BOOLEAN DEFAULT false, "
                                        + COLUMN_NAME_VERIFY_CODE + " VARCHAR(1024) NOT NULL, "
                                        + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP + " BIGINT, "
                                        + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES + " BIGINT DEFAULT " + DEFAULT_INITIAL_USER_AVAILABLE_TRANSFER_BYTES + ")";

    /* find user count */
//    String SQL_FIND_AUTH_USER_COUNT = "SELECT COUNT(*) FROM " + TABLE_NAME_AUTH_USER;

    /* find user by id */
    /* TODO: columns added */
    String SQL_FIND_AUTH_USER_BY_ID = "SELECT "
                                      + COLUMN_NAME_AUTH_USER_ID + ", "
                                      + COLUMN_NAME_COUNTRY_ID + ", "
                                      + COLUMN_NAME_PHONE_NUMBER + ", "
                                      + COLUMN_NAME_PASSWD + ", "
                                      + COLUMN_NAME_NICKNAME + ", "
                                      + COLUMN_NAME_SHOW_HIDDEN + ", "
//                                      + COLUMN_NAME_AUTH_USER_ID_ENCRYPTED + ", "
//                                      + COLUMN_NAME_RECONNECT + ", "
                                      + COLUMN_NAME_AUTH_USER_VERIFIED + ", "
                                      + COLUMN_NAME_VERIFY_CODE + ", "
                                      + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP + ", "
                                      + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES
                                      + " FROM "
                                      + TABLE_NAME_AUTH_USER
                                      + " WHERE "
                                      + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* create user */
    /* TODO: columns added */
    String SQL_CREATE_AUTH_USER = "INSERT INTO " + TABLE_NAME_AUTH_USER + "("
                                  + COLUMN_NAME_AUTH_USER_ID + ", "
                                  + COLUMN_NAME_COUNTRY_ID + ", "
                                  + COLUMN_NAME_PHONE_NUMBER + ", "
                                  + COLUMN_NAME_PASSWD + ", "
                                  + COLUMN_NAME_NICKNAME + ", "
                                  + COLUMN_NAME_SHOW_HIDDEN + ", "
//                                  + COLUMN_NAME_AUTH_USER_ID_ENCRYPTED + ", "
//                                  + COLUMN_NAME_RECONNECT + ", "
                                  + COLUMN_NAME_AUTH_USER_VERIFIED + ", "
                                  + COLUMN_NAME_VERIFY_CODE + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    /* clear all data in table user */
//    String SQL_TRUNCATE_AUTH_USER = "TRUNCATE TABLE " + TABLE_NAME_AUTH_USER;

    /* update show hidden by user id */
//    String SQL_UPDATE_SHOW_HIDDEN_BY_ID = "UPDATE " + TABLE_NAME_AUTH_USER + " SET " + COLUMN_NAME_SHOW_HIDDEN + " = ? WHERE " + COLUMN_NAME_AUTH_USER_ID + " = ?";

//    /* find reconnect by user id-encrypted */
//    String SQL_FIND_RECONNECT_BY_ID_ENCRYPTED = "SELECT " + COLUMN_NAME_RECONNECT + " FROM " + TABLE_NAME_AUTH_USER + " WHERE " + COLUMN_NAME_AUTH_USER_ID_ENCRYPTED + "  = ?";

//    /* find reconnect by user id */
//    String SQL_FIND_RECONNECT_BY_ID = "SELECT " + COLUMN_NAME_RECONNECT + " FROM " + TABLE_NAME_AUTH_USER + " WHERE " + COLUMN_NAME_AUTH_USER_ID + "  = ?";

    /* find reconnect by user id */
    String SQL_FIND_AUTH_USER_EXISTS_BY_ID = "SELECT " + COLUMN_NAME_AUTH_USER_ID + " FROM " + TABLE_NAME_AUTH_USER + " WHERE " + COLUMN_NAME_AUTH_USER_ID + "  = ?";

    /* find user by user id and verify code */
    String SQL_FIND_AUTH_USER_BY_VERIFY_CODE = "SELECT "
                                               + COLUMN_NAME_AUTH_USER_ID + ", "
                                               + COLUMN_NAME_COUNTRY_ID + ", "
                                               + COLUMN_NAME_PHONE_NUMBER + ", "
                                               + COLUMN_NAME_PASSWD + ", "
                                               + COLUMN_NAME_NICKNAME + ", "
                                               + COLUMN_NAME_SHOW_HIDDEN + ", "
                                               + COLUMN_NAME_AUTH_USER_VERIFIED + ", "
                                               + COLUMN_NAME_VERIFY_CODE + ", "
                                               + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP + ", "
                                               + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES
                                               + " FROM "
                                               + TABLE_NAME_AUTH_USER
                                               + " WHERE "
                                               + COLUMN_NAME_AUTH_USER_ID + " = ? AND "
                                               + COLUMN_NAME_VERIFY_CODE + " = ?";

    /* find user by registration verification conditions */
//    String SQL_FIND_AUTH_USER_BY_VERIFY_CONDITIONS = "SELECT "
//                                                     + COLUMN_NAME_AUTH_USER_ID + ", "
//                                                     + COLUMN_NAME_COUNTRY_ID + ", "
//                                                     + COLUMN_NAME_PHONE_NUMBER + ", "
//                                                     + COLUMN_NAME_PASSWD + ", "
//                                                     + COLUMN_NAME_NICKNAME + ", "
//                                                     + COLUMN_NAME_SHOW_HIDDEN + ", "
////                                                     + COLUMN_NAME_AUTH_USER_ID_ENCRYPTED + ", "
////                                                     + COLUMN_NAME_RECONNECT + ", "
//                                                     + COLUMN_NAME_AUTH_USER_VERIFIED + ", "
//                                                     + COLUMN_NAME_VERIFY_CODE + ", "
//                                                     + COLUMN_NAME_VERIFY_LETTER_SENT_TIMESTAMP + ", "
//                                                     + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES
//                                                     + " FROM "
//                                                     + TABLE_NAME_AUTH_USER
//                                                     + " WHERE "
//                                                     + COLUMN_NAME_AUTH_USER_ID_ENCRYPTED + " = ? AND "
//                                                     + COLUMN_NAME_VERIFY_CODE + " = ?";

    /* update reconnect by auth user id-encrypted */
//    String SQL_UPDATE_RECONNECT_BY_ID_ENCRYPTED = "UPDATE " + TABLE_NAME_AUTH_USER + " SET " + COLUMN_NAME_RECONNECT + " = ? WHERE " + COLUMN_NAME_AUTH_USER_ID_ENCRYPTED + " = ?";

//    /* update reconnect by auth user id */
//    String SQL_UPDATE_RECONNECT_BY_ID = "UPDATE " + TABLE_NAME_AUTH_USER + " SET " + COLUMN_NAME_RECONNECT + " = ? WHERE " + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update verified by auth user id */
    String SQL_UPDATE_AUTH_USER_VERIFIED_BY_ID = "UPDATE " + TABLE_NAME_AUTH_USER + " SET " + COLUMN_NAME_AUTH_USER_VERIFIED + " = ? WHERE " + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update verify code sent timestamp by auth user id */
    String SQL_UPDATE_VERIFY_CODE_SENT_TIMESTAMP_BY_ID = "UPDATE " + TABLE_NAME_AUTH_USER + " SET " + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP + " = ? WHERE " + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update available transfer bytes by user id */
    String SQL_UPDATE_AVAILABLE_TRANSFER_BYTES_BY_USER = "UPDATE " + TABLE_NAME_AUTH_USER + " SET " + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES + " = ? WHERE " + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* find available transfer bytes by user id */
    String SQL_FIND_AVAILABLE_TRANSFER_BYTES_BY_USER =  "SELECT " + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES + " FROM " + TABLE_NAME_AUTH_USER + " WHERE " + COLUMN_NAME_AUTH_USER_ID + "  = ?";

    /* find timeout-unverified-users, need to specified the timeout timestamp in milliseconds */
    /* MAKE SURE false is supported for non-sql db */
    String SQL_FIND_USERS_BY_VERIFIED_NO_NOT = "SELECT " + COLUMN_NAME_AUTH_USER_ID + ", " + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP + " FROM " + TABLE_NAME_AUTH_USER + " WHERE " + COLUMN_NAME_AUTH_USER_VERIFIED + " = ?";

    /* delete user by id, it must be not verified and so there will be no cascading issues */
    /* MAKE SURE false is supported for non-sql db */
    String SQL_DELETE_USER_BY_ID_NO_CASCADE = "DELETE FROM " + TABLE_NAME_AUTH_USER + " WHERE " + COLUMN_NAME_AUTH_USER_ID + " = ? AND " + COLUMN_NAME_AUTH_USER_VERIFIED + " = ?";

    /* update password by auth user id */
    String SQL_UPDATE_AUTH_USER_PASSWORD = "UPDATE " + TABLE_NAME_AUTH_USER + " SET " + COLUMN_NAME_PASSWD + " = ? WHERE " + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update password by auth user id */
    String SQL_UPDATE_AUTH_USER_NICKNAME = "UPDATE " + TABLE_NAME_AUTH_USER + " SET " + COLUMN_NAME_NICKNAME + " = ? WHERE " + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* TABLE: FILE_UPLOADED related */

    String TABLE_NAME_FILE_UPLOADED = "FILE_UPLOADED";

    String COLUMN_NAME_UPLOAD_KEY = "UPLOAD_KEY";

    String COLUMN_NAME_FILENAME = "FILENAME";

    String COLUMN_NAME_DIRECTORY = "DIRECTORY";

    String COLUMN_NAME_FILE_SIZE = "FILE_SIZE";

    String COLUMN_NAME_START_TIMESTAMP = "START_TIMESTAMP";

    String COLUMN_NAME_END_TIMESTAMP = "END_TIMESTAMP";

    String COLUMN_NAME_STATUS = "STATUS";

    String INDEX_NAME_FILE_UPLOADED_END_TIMESTAMP = "INDEX_D_FILE_UPLOADED_END_TIMESTAMP";

    String TRANSFER_STATUS_PROCESSING = "processing";

    String TRANSFER_STATUS_SUCCESS = "success";

    String TRANSFER_STATUS_FAILURE = "failure";


    /* DDL - TABLE: FILE_UPLOADED */

    String SQL_CREATE_TABLE_FILE_UPLOADED = "CREATE TABLE " + TABLE_NAME_FILE_UPLOADED + "("
                                            + COLUMN_NAME_UPLOAD_KEY + " VARCHAR(1024) PRIMARY KEY, "
                                            + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) NOT NULL, "
                                            + COLUMN_NAME_FILENAME + " VARCHAR(1024) NOT NULL, "
                                            + COLUMN_NAME_DIRECTORY + " VARCHAR(1024) NOT NULL, "
                                            + COLUMN_NAME_FILE_SIZE + " BIGINT DEFAULT 0, "
                                            + COLUMN_NAME_START_TIMESTAMP + " BIGINT DEFAULT 0, "
                                            + COLUMN_NAME_END_TIMESTAMP + " BIGINT DEFAULT 0, "
                                            + COLUMN_NAME_STATUS + " VARCHAR(1024))";

    String SQL_CREATE_INDEX_FILE_UPLOADED_END_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_FILE_UPLOADED_END_TIMESTAMP + " ON " + TABLE_NAME_FILE_UPLOADED + "(" + COLUMN_NAME_END_TIMESTAMP + ")";

    String SQL_CREATE_FOREIGN_KEY_FILE_UPLOADED_USER = "ALTER TABLE " + TABLE_NAME_FILE_UPLOADED + " ADD FOREIGN KEY (" + COLUMN_NAME_AUTH_USER_ID + ") REFERENCES " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_AUTH_USER_ID + ") ON DELETE CASCADE";

    /* count a uploaded-file by uploadKey */
    String SQL_COUNT_FILE_UPLOADED_BY_UPLOAD_KEY = "SELECT count(*) FROM " + TABLE_NAME_FILE_UPLOADED + " WHERE " + COLUMN_NAME_UPLOAD_KEY + " = ?";

    /* find a uploaded-file by uploadKey */
    String SQL_FIND_FILE_UPLOADED_BY_UPLOAD_KEY = "SELECT " + COLUMN_NAME_UPLOAD_KEY + ", " + COLUMN_NAME_AUTH_USER_ID + ", " + COLUMN_NAME_FILENAME + ", " + COLUMN_NAME_DIRECTORY + ", " + COLUMN_NAME_FILE_SIZE + ", " + COLUMN_NAME_START_TIMESTAMP + ", " + COLUMN_NAME_END_TIMESTAMP + ", " + COLUMN_NAME_STATUS + " FROM " + TABLE_NAME_FILE_UPLOADED + " WHERE " + COLUMN_NAME_UPLOAD_KEY + " = ?";

    /* create FILE_UPLOADED */
    String SQL_CREATE_FILE_UPLOADED = "INSERT INTO " + TABLE_NAME_FILE_UPLOADED + "("
                                      + COLUMN_NAME_UPLOAD_KEY + ", "
                                      + COLUMN_NAME_AUTH_USER_ID + ", "
                                      + COLUMN_NAME_FILENAME + ", "
                                      + COLUMN_NAME_DIRECTORY + ", "
                                      + COLUMN_NAME_FILE_SIZE + ", "
                                      + COLUMN_NAME_START_TIMESTAMP + ", "
                                      + COLUMN_NAME_END_TIMESTAMP + ", "
                                      + COLUMN_NAME_STATUS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    /* update file upload by uploadKey for column end timestamp, file size and status */
//    String SQL_UPDATE_FILE_UPLOADED = "UPDATE " + TABLE_NAME_FILE_UPLOADED + " SET " + COLUMN_NAME_STATUS + " = ?, " + COLUMN_NAME_FILE_SIZE + " = ?, " + COLUMN_NAME_END_TIMESTAMP + " = ? WHERE " + COLUMN_NAME_UPLOAD_KEY + " = ?";

    /* update file upload by uploadKey for column end timestamp and status */
    String SQL_UPDATE_FILE_UPLOADED_WITHOUT_FILE_SIZE = "UPDATE " + TABLE_NAME_FILE_UPLOADED + " SET " + COLUMN_NAME_STATUS + " = ?, " + COLUMN_NAME_END_TIMESTAMP + " = ? WHERE " + COLUMN_NAME_UPLOAD_KEY + " = ?";

    /* update file upload by uploadKey for column file size */
    String SQL_UPDATE_FILE_UPLOADED_SIZE = "UPDATE " + TABLE_NAME_FILE_UPLOADED + " SET " + COLUMN_NAME_FILE_SIZE + " = ? WHERE " + COLUMN_NAME_UPLOAD_KEY + " = ?";

    /* update downloaded-files status from processing to failure by timeout startTimestamp */
    String SQL_UPDATE_FILE_UPLOADED_STATUS_PROCESSING_TO_FAILURE_FOR_TIMEOUT_START_TIMESTAMP = "UPDATE " + TABLE_NAME_FILE_UPLOADED + " SET " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_FAILURE + "' WHERE " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_PROCESSING + "' AND " + COLUMN_NAME_START_TIMESTAMP + " < ?";

    /* find upload-files by user, order by start timestamp desc */
    String SQL_FIND_ALL_FILE_UPLOADED_BY_USER = "SELECT " + COLUMN_NAME_UPLOAD_KEY + ", " + COLUMN_NAME_AUTH_USER_ID + ", " + COLUMN_NAME_FILENAME + ", " + COLUMN_NAME_DIRECTORY + ", " + COLUMN_NAME_FILE_SIZE + ", " + COLUMN_NAME_START_TIMESTAMP + ", " + COLUMN_NAME_END_TIMESTAMP + ", " + COLUMN_NAME_STATUS + " FROM " + TABLE_NAME_FILE_UPLOADED + " WHERE " + COLUMN_NAME_AUTH_USER_ID + " = ? ORDER BY " + COLUMN_NAME_START_TIMESTAMP + " DESC";

    String SQL_FIND_SUCCESS_FILE_UPLOADED_BY_USER = "SELECT " + COLUMN_NAME_UPLOAD_KEY + ", " + COLUMN_NAME_AUTH_USER_ID + ", " + COLUMN_NAME_FILENAME + ", " + COLUMN_NAME_DIRECTORY + ", " + COLUMN_NAME_FILE_SIZE + ", " + COLUMN_NAME_START_TIMESTAMP + ", " + COLUMN_NAME_END_TIMESTAMP + ", " + COLUMN_NAME_STATUS + " FROM " + TABLE_NAME_FILE_UPLOADED + " WHERE " + COLUMN_NAME_AUTH_USER_ID + " = ? AND " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_SUCCESS + "' ORDER BY " + COLUMN_NAME_END_TIMESTAMP + " DESC";

    /* find downloaded-files downloadKey by timeout startTimestamp */
    String SQL_FIND_PROCESSING_FILE_UPLOADED_BY_TIMEOUT_START_TIMESTAMP = "SELECT " + COLUMN_NAME_UPLOAD_KEY + ", " + COLUMN_NAME_AUTH_USER_ID + ", " + COLUMN_NAME_FILENAME + ", " + COLUMN_NAME_DIRECTORY + ", " + COLUMN_NAME_FILE_SIZE + ", " + COLUMN_NAME_START_TIMESTAMP + ", " + COLUMN_NAME_END_TIMESTAMP + ", " + COLUMN_NAME_STATUS + " FROM " + TABLE_NAME_FILE_UPLOADED + " WHERE " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_PROCESSING + "' AND " + COLUMN_NAME_START_TIMESTAMP + " < ?";

    /* find file size of the upload-file by upload key */
    String SQL_FIND_FILE_UPLOADED_SIZE_BY_UPLOAD_KEY = "SELECT " + COLUMN_NAME_FILE_SIZE + " FROM " + TABLE_NAME_FILE_UPLOADED + " WHERE " + COLUMN_NAME_UPLOAD_KEY + " = ?";

    /* find file size of the upload-file by upload key */
    String SQL_FIND_SUM_UPLOADING_FILE_SIZE_BY_USER = "SELECT SUM(" + COLUMN_NAME_FILE_SIZE + ") FROM " + TABLE_NAME_FILE_UPLOADED + " WHERE " + COLUMN_NAME_AUTH_USER_ID + " = ? AND " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_PROCESSING + "'";

    /* TABLE: FILE_DOWNLOADED related */

    String TABLE_NAME_FILE_DOWNLOADED = "FILE_DOWNLOADED";

    String COLUMN_NAME_DOWNLOAD_KEY = "DOWNLOAD_KEY";

    String COLUMN_NAME_FILE_PATH = "FILE_PATH";

    String INDEX_NAME_FILE_DOWNLOADED_END_TIMESTAMP = "INDEX_D_FILE_DOWNLOADED_END_TIMESTAMP";

    /* DDL - TABLE: FILE_DOWNLOADED */

    String SQL_CREATE_TABLE_FILE_DOWNLOADED = "CREATE TABLE " + TABLE_NAME_FILE_DOWNLOADED + "("
                                            + COLUMN_NAME_DOWNLOAD_KEY + " VARCHAR(1024) PRIMARY KEY, "
                                            + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) NOT NULL, "
                                            + COLUMN_NAME_FILE_PATH + " VARCHAR(1024) NOT NULL, "
                                            + COLUMN_NAME_FILE_SIZE + " BIGINT DEFAULT 0, "
                                            + COLUMN_NAME_START_TIMESTAMP + " BIGINT DEFAULT 0, "
                                            + COLUMN_NAME_END_TIMESTAMP + " BIGINT DEFAULT 0, "
                                            + COLUMN_NAME_STATUS + " VARCHAR(1024))";

    String SQL_CREATE_INDEX_FILE_DOWNLOADED_END_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_FILE_DOWNLOADED_END_TIMESTAMP + " ON " + TABLE_NAME_FILE_DOWNLOADED + "(" + COLUMN_NAME_END_TIMESTAMP + ")";

    String SQL_CREATE_FOREIGN_KEY_FILE_DOWNLOADED_USER = "ALTER TABLE " + TABLE_NAME_FILE_DOWNLOADED + " ADD FOREIGN KEY (" + COLUMN_NAME_AUTH_USER_ID + ") REFERENCES " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_AUTH_USER_ID + ") ON DELETE CASCADE";

    /* count a download-file by downloadKey */
    String SQL_COUNT_FILE_DOWNLOADED_BY_DOWNLOAD_KEY = "SELECT count(*) FROM " + TABLE_NAME_FILE_DOWNLOADED + " WHERE " + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    /* find a downloaded-file by downloadKey */
    String SQL_FIND_FILE_DOWNLOADED_BY_DOWNLOAD_KEY = "SELECT " + COLUMN_NAME_DOWNLOAD_KEY + ", " + COLUMN_NAME_AUTH_USER_ID + ", " + COLUMN_NAME_FILE_PATH + ", " + COLUMN_NAME_FILE_SIZE + ", " + COLUMN_NAME_START_TIMESTAMP + ", " + COLUMN_NAME_END_TIMESTAMP + ", " + COLUMN_NAME_STATUS + " FROM " + TABLE_NAME_FILE_DOWNLOADED + " WHERE " + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    /* create FILE_DOWNLOADED */
    String SQL_CREATE_FILE_DOWNLOADED = "INSERT INTO " + TABLE_NAME_FILE_DOWNLOADED + "("
                                      + COLUMN_NAME_DOWNLOAD_KEY + ", "
                                      + COLUMN_NAME_AUTH_USER_ID + ", "
                                      + COLUMN_NAME_FILE_PATH + ", "
                                      + COLUMN_NAME_START_TIMESTAMP + ", "
                                      + COLUMN_NAME_END_TIMESTAMP + ", "
                                      + COLUMN_NAME_STATUS + ") VALUES (?, ?, ?, ?, ?, ?)";

    /* update downloaded-files by downloadKey for column end timestamp and status */
    String SQL_UPDATE_FILE_DOWNLOADED = "UPDATE " + TABLE_NAME_FILE_DOWNLOADED + " SET " + COLUMN_NAME_STATUS + " = ?, " + COLUMN_NAME_END_TIMESTAMP + " = ?, " + COLUMN_NAME_FILE_SIZE + " = ? WHERE " + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    /* update downloaded-files by downloadKey for column end timestamp and status */
    String SQL_UPDATE_FILE_DOWNLOADED_WITHOUT_FILE_SIZE = "UPDATE " + TABLE_NAME_FILE_DOWNLOADED + " SET " + COLUMN_NAME_STATUS + " = ?, " + COLUMN_NAME_END_TIMESTAMP + " = ? WHERE " + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    /* update downloaded-files status from processing to failure by timeout startTimestamp */
    String SQL_UPDATE_FILE_DOWNLOADED_STATUS_PROCESSING_TO_FAILURE_FOR_TIMEOUT_START_TIMESTAMP = "UPDATE " + TABLE_NAME_FILE_DOWNLOADED + " SET " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_FAILURE + "' WHERE " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_PROCESSING + "' AND " + COLUMN_NAME_START_TIMESTAMP + " < ?";

    /* update downloaded-files by downloadKey for column end timestamp and status */
    String SQL_UPDATE_FILE_DOWNLOADED_SIZE = "UPDATE " + TABLE_NAME_FILE_DOWNLOADED + " SET " + COLUMN_NAME_FILE_SIZE + " = ? WHERE " + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    /* find download-files by user, order by start timestamp desc */
    String SQL_FIND_ALL_FILE_DOWNLOADED_BY_USER = "SELECT " + COLUMN_NAME_DOWNLOAD_KEY + ", " + COLUMN_NAME_AUTH_USER_ID + ", " + COLUMN_NAME_FILE_PATH + ", " + COLUMN_NAME_FILE_SIZE + ", " + COLUMN_NAME_START_TIMESTAMP + ", " + COLUMN_NAME_END_TIMESTAMP + ", " + COLUMN_NAME_STATUS + " FROM " + TABLE_NAME_FILE_DOWNLOADED + " WHERE " + COLUMN_NAME_AUTH_USER_ID + " = ? ORDER BY " + COLUMN_NAME_START_TIMESTAMP + " DESC";

    /* find download-files by user, order by start timestamp desc */
    String SQL_FIND_SUCCESS_FILE_DOWNLOADED_BY_USER = "SELECT " + COLUMN_NAME_DOWNLOAD_KEY + ", " + COLUMN_NAME_AUTH_USER_ID + ", " + COLUMN_NAME_FILE_PATH + ", " + COLUMN_NAME_FILE_SIZE + ", " + COLUMN_NAME_START_TIMESTAMP + ", " + COLUMN_NAME_END_TIMESTAMP + ", " + COLUMN_NAME_STATUS + " FROM " + TABLE_NAME_FILE_DOWNLOADED + " WHERE " + COLUMN_NAME_AUTH_USER_ID + " = ? AND " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_SUCCESS + "' ORDER BY " + COLUMN_NAME_END_TIMESTAMP + " DESC";

    /* find downloaded-files downloadKey by timeout startTimestamp */
    String SQL_FIND_PROCESSING_FILE_DOWNLOADED_BY_TIMEOUT_START_TIMESTAMP = "SELECT " + COLUMN_NAME_DOWNLOAD_KEY + ", " + COLUMN_NAME_AUTH_USER_ID + ", " + COLUMN_NAME_FILE_PATH + ", " + COLUMN_NAME_FILE_SIZE + ", " + COLUMN_NAME_START_TIMESTAMP + ", " + COLUMN_NAME_END_TIMESTAMP + ", " + COLUMN_NAME_STATUS + " FROM " + TABLE_NAME_FILE_DOWNLOADED + " WHERE " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_PROCESSING + "' AND " + COLUMN_NAME_START_TIMESTAMP + " < ?";

    /* find file size of the upload-file by upload key */
    String SQL_FIND_SUM_DOWNLOADING_FILE_SIZE_BY_USER = "SELECT SUM(" + COLUMN_NAME_FILE_SIZE + ") FROM " + TABLE_NAME_FILE_DOWNLOADED + " WHERE " + COLUMN_NAME_AUTH_USER_ID + " = ? AND " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_PROCESSING + "'";

    /* TABLE: SMS_NOTIFICATION related */
    String TABLE_NAME_SMS_NOTIFICATION = "SMS_NOTIFICATION";

    String COLUMN_NAME_SMS_MESSAGE_ID = "SMS_ID";

    String COLUMN_NAME_DELIVER_TIMESTAMP = "DELIVER_TIMESTAMP";

    String COLUMN_NAME_STATUS_UPDATE_TIMESTAMP = "STATUS_UPDATE_TIMESTAMP";

    String COLUMN_NAME_STATUS_MESSAGE = "STATUS_MESSAGE";

    String INDEX_NAME_SMS_NOTIFICATION_DELIVER_TIMESTAMP = "INDEX_S_SMS_NOTIFICATION_DELIVER_TIMESTAMP";

    /* DDL - TABLE: SMS_NOTIFICATION */

    String SQL_CREATE_TABLE_SMS_NOTIFICATION = "CREATE TABLE " + TABLE_NAME_SMS_NOTIFICATION + "("
                                               + COLUMN_NAME_SMS_MESSAGE_ID + " VARCHAR(1024) PRIMARY KEY, "
                                               + COLUMN_NAME_PHONE_NUMBER + " VARCHAR(24) NOT NULL, "
                                               + COLUMN_NAME_DELIVER_TIMESTAMP + " BIGINT DEFAULT 0, "
                                               + COLUMN_NAME_STATUS_UPDATE_TIMESTAMP + " BIGINT DEFAULT 0, "
                                               + COLUMN_NAME_STATUS + " INTEGER DEFAULT -1, "
                                               + COLUMN_NAME_STATUS_MESSAGE + " VARCHAR(1024), "
                                               + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) NOT NULL)";

    String SQL_CREATE_INDEX_SMS_NOTIFICATION_DELIVER_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_SMS_NOTIFICATION_DELIVER_TIMESTAMP + " ON " + TABLE_NAME_SMS_NOTIFICATION + "(" + COLUMN_NAME_DELIVER_TIMESTAMP + ")";

    String SQL_CREATE_FOREIGN_KEY_SMS_NOTIFICATION_USER = "ALTER TABLE " + TABLE_NAME_SMS_NOTIFICATION + " ADD FOREIGN KEY (" + COLUMN_NAME_AUTH_USER_ID + ") REFERENCES " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_AUTH_USER_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_SMS_NOTIFICATION = "INSERT INTO " + TABLE_NAME_SMS_NOTIFICATION + "("
                                         + COLUMN_NAME_SMS_MESSAGE_ID + ", "
                                         + COLUMN_NAME_PHONE_NUMBER + ", "
                                         + COLUMN_NAME_DELIVER_TIMESTAMP + ", "
                                         + COLUMN_NAME_STATUS_UPDATE_TIMESTAMP + ", "
                                         + COLUMN_NAME_STATUS + ", "
                                         + COLUMN_NAME_STATUS_MESSAGE + ", "
                                         + COLUMN_NAME_AUTH_USER_ID + ") VALUES (?, ?, ?, ?, ?, ?, ?)";

    /* return zero or one record */
    String SQL_FIND_SMS_NOTIFICATION_BY_ID = "SELECT "
                                             + COLUMN_NAME_SMS_MESSAGE_ID + ", "
                                             + COLUMN_NAME_PHONE_NUMBER + ", "
                                             + COLUMN_NAME_DELIVER_TIMESTAMP + ", "
                                             + COLUMN_NAME_STATUS_UPDATE_TIMESTAMP + ", "
                                             + COLUMN_NAME_STATUS + ", "
                                             + COLUMN_NAME_STATUS_MESSAGE + ", "
                                             + COLUMN_NAME_AUTH_USER_ID
                                             + " FROM "
                                             + TABLE_NAME_SMS_NOTIFICATION
                                             + " WHERE "
                                             + COLUMN_NAME_SMS_MESSAGE_ID + " = ?";

    /* may return multiple records */
    String SQL_FIND_SMS_NOTIFICATION_BY_PHONE_NUMBER = "SELECT "
                                                       + COLUMN_NAME_SMS_MESSAGE_ID + ", "
                                                       + COLUMN_NAME_PHONE_NUMBER + ", "
                                                       + COLUMN_NAME_DELIVER_TIMESTAMP + ", "
                                                       + COLUMN_NAME_STATUS_UPDATE_TIMESTAMP + ", "
                                                       + COLUMN_NAME_STATUS + ", "
                                                       + COLUMN_NAME_STATUS_MESSAGE + ", "
                                                       + COLUMN_NAME_AUTH_USER_ID
                                                       + " FROM "
                                                       + TABLE_NAME_SMS_NOTIFICATION
                                                       + " WHERE "
                                                       + COLUMN_NAME_AUTH_USER_ID + " = ? AND "
                                                       + COLUMN_NAME_PHONE_NUMBER + " = ?"
                                                       + " ORDER BY "
                                                       + COLUMN_NAME_DELIVER_TIMESTAMP + " DESC";

    String SQL_UPDATE_SMS_NOTIFICATION_BY_ID = "UPDATE " + TABLE_NAME_SMS_NOTIFICATION + " SET "
                                               + COLUMN_NAME_PHONE_NUMBER + " = ?, "
                                               + COLUMN_NAME_DELIVER_TIMESTAMP + " = ?, "
                                               + COLUMN_NAME_STATUS_UPDATE_TIMESTAMP + " = ?, "
                                               + COLUMN_NAME_STATUS + " = ?, "
                                               + COLUMN_NAME_STATUS_MESSAGE + " = ? WHERE "
                                               + COLUMN_NAME_SMS_MESSAGE_ID + " = ?";

    /* TABLE: COMPUTER related */

    String TABLE_NAME_COMPUTER = "CL_COMPUTER";

    String COLUMN_NAME_COMPUTER_ID = "COMPUTER_ID";

    String COLUMN_NAME_GROUP_NAME = "GROUP_NAME";

    String COLUMN_NAME_COMPUTER_NAME = "COMPUTER_NAME";

    String COLUMN_NAME_RECOVERY_KEY = "RECOVERY_KEY";

    String INDEX_NAME_COMPUTER_NAME = "INDEX_C_COMPUTER_NAME";

    String INDEX_NAME_COMPUTER_GROUP_NAME = "INDEX_C_GROUP_NAME";

    /* DDL - TABLE: CL_COMPUTER */

    String SQL_CREATE_TABLE_COMPUTER = "CREATE TABLE " + TABLE_NAME_COMPUTER + "("
                                       + COLUMN_NAME_COMPUTER_ID + " VARCHAR(1024) PRIMARY KEY, "
                                       + COLUMN_NAME_GROUP_NAME + " VARCHAR(1024) NOT NULL, "
                                       + COLUMN_NAME_COMPUTER_NAME + " VARCHAR(1024) NOT NULL, "
                                       + COLUMN_NAME_RECOVERY_KEY + " VARCHAR(1024) NOT NULL, "
                                       + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) NOT NULL)";

    /* ----- DO NOT CREATE FOREIGN KEY TO AUTH USER ID. The colum is a referenced value only. ----- */

    String SQL_CREATE_INDEX_COMPUTER_NAME = "CREATE INDEX " + INDEX_NAME_COMPUTER_NAME + " ON " + TABLE_NAME_COMPUTER + "(" + COLUMN_NAME_COMPUTER_NAME + ")";

    String SQL_CREATE_INDEX_COMPUTER_GROUP_NAME = "CREATE INDEX " + INDEX_NAME_COMPUTER_GROUP_NAME + " ON " + TABLE_NAME_COMPUTER + "(" + COLUMN_NAME_GROUP_NAME + ")";

    String SQL_CREATE_COMPUTER = "INSERT INTO " + TABLE_NAME_COMPUTER + "("
                                 + COLUMN_NAME_COMPUTER_ID + ", "
                                 + COLUMN_NAME_GROUP_NAME + ", "
                                 + COLUMN_NAME_COMPUTER_NAME + ", "
                                 + COLUMN_NAME_RECOVERY_KEY + ", "
                                 + COLUMN_NAME_AUTH_USER_ID + ") VALUES (?, ?, ?, ?, ?)";

    String SQL_FIND_COMPUTER_BY_COMPUTER_ID = "SELECT "
                                              + COLUMN_NAME_COMPUTER_ID + ", "
                                              + COLUMN_NAME_GROUP_NAME + ", "
                                              + COLUMN_NAME_COMPUTER_NAME + ", "
                                              + COLUMN_NAME_RECOVERY_KEY
                                              + " FROM "
                                              + TABLE_NAME_COMPUTER
                                              + " WHERE lower("
                                              + COLUMN_NAME_COMPUTER_ID + ") = lower(?)";

    String SQL_FIND_ALL_COMPUTERS = "SELECT "
                                    + COLUMN_NAME_COMPUTER_ID + ", "
                                    + COLUMN_NAME_GROUP_NAME + ", "
                                    + COLUMN_NAME_COMPUTER_NAME + ", "
                                    + COLUMN_NAME_RECOVERY_KEY
                                    + " FROM "
                                    + TABLE_NAME_COMPUTER
                                    + " ORDER BY "
                                    + COLUMN_NAME_GROUP_NAME + " ASC, "
                                    + COLUMN_NAME_COMPUTER_NAME + " ASC";

    String SQL_DELETE_COMPUTER_BY_ID_CASCADE = "DELETE FROM " + TABLE_NAME_COMPUTER + " WHERE lower(" + COLUMN_NAME_COMPUTER_ID + ") = lower(?)";

     /* TABLE: USER_COMPUTER related */

    String TABLE_NAME_USER_COMPUTER = "USER_COMPUTER";

    String COLUMN_NAME_USER_COMPUTER_ID = "UC_ID";

    String COLUMN_NAME_RECONNECT = "RECONNECT";

    String COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED = "USER_COMPUTER_ID_ENCRYPTED";

    String INDEX_NAME_USER_COMPUTER_ENCRYPTED = "INDEX_UC_ID_ENCRYPTED";

    /* DDL - TABLE: USER_COMPUTER */

    String SQL_CREATE_TABLE_USER_COMPUTER = "CREATE TABLE " + TABLE_NAME_USER_COMPUTER + "("
                                            + COLUMN_NAME_USER_COMPUTER_ID + " VARCHAR(1024) PRIMARY KEY, "
                                            + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) NOT NULL, "
                                            + COLUMN_NAME_COMPUTER_ID + " VARCHAR(1024) NOT NULL, "
                                            + COLUMN_NAME_GROUP_NAME + " VARCHAR(1024) NOT NULL, "
                                            + COLUMN_NAME_COMPUTER_NAME + " VARCHAR(1024) NOT NULL, "
                                            + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED + " VARCHAR(4096) NOT NULL, "
                                            + COLUMN_NAME_RECONNECT + " BOOLEAN DEFAULT false)";

    String SQL_CREATE_FOREIGN_KEY_USER_COMPUTER_USER = "ALTER TABLE " + TABLE_NAME_USER_COMPUTER + " ADD FOREIGN KEY (" + COLUMN_NAME_AUTH_USER_ID + ") REFERENCES " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_AUTH_USER_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_FOREIGN_KEY_USER_COMPUTER_COMPUTER = "ALTER TABLE " + TABLE_NAME_USER_COMPUTER + " ADD FOREIGN KEY (" + COLUMN_NAME_COMPUTER_ID + ") REFERENCES " + TABLE_NAME_COMPUTER + "(" + COLUMN_NAME_COMPUTER_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_INDEX_USER_COMPUTER_ENCRYPTED = "CREATE INDEX " + INDEX_NAME_USER_COMPUTER_ENCRYPTED + " ON " + TABLE_NAME_USER_COMPUTER + "(" + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED + ")";

    String SQL_CREATE_USER_COMPUTER = "INSERT INTO " + TABLE_NAME_USER_COMPUTER + "("
                                      + COLUMN_NAME_USER_COMPUTER_ID + ", "
                                      + COLUMN_NAME_AUTH_USER_ID + ", "
                                      + COLUMN_NAME_COMPUTER_ID + ", "
                                      + COLUMN_NAME_GROUP_NAME + ", "
                                      + COLUMN_NAME_COMPUTER_NAME + ", "
                                      + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED + ", "
                                      + COLUMN_NAME_RECONNECT + ") VALUES (?, ?, ?, ?, ?, ?, ?)";

    String SQL_FIND_USER_COMPUTERS_BY_ID = "SELECT "
                                           + COLUMN_NAME_USER_COMPUTER_ID + ", "
                                           + COLUMN_NAME_AUTH_USER_ID + ", "
                                           + COLUMN_NAME_COMPUTER_ID + ", "
                                           + COLUMN_NAME_GROUP_NAME + ", "
                                           + COLUMN_NAME_COMPUTER_NAME + ", "
                                           + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED + ", "
                                           + COLUMN_NAME_RECONNECT
                                           + " FROM "
                                           + TABLE_NAME_USER_COMPUTER
                                           + " WHERE lower("
                                           + COLUMN_NAME_USER_COMPUTER_ID + ") = lower(?)";

    String SQL_FIND_USER_COMPUTERS_BY_USER_ID = "SELECT "
                                                + COLUMN_NAME_USER_COMPUTER_ID + ", "
                                                + COLUMN_NAME_AUTH_USER_ID + ", "
                                                + COLUMN_NAME_COMPUTER_ID + ", "
                                                + COLUMN_NAME_GROUP_NAME + ", "
                                                + COLUMN_NAME_COMPUTER_NAME + ", "
                                                + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED + ", "
                                                + COLUMN_NAME_RECONNECT
                                                + " FROM "
                                                + TABLE_NAME_USER_COMPUTER
                                                + " WHERE "
                                                + COLUMN_NAME_AUTH_USER_ID + " = ? ORDER BY "
                                                + COLUMN_NAME_GROUP_NAME + " ASC, "
                                                + COLUMN_NAME_COMPUTER_NAME + " ASC";


    /* find reconnect by encrypted user computer id */
    String SQL_FIND_RECONNECT_BY_ID_ENCRYPTED = "SELECT " + COLUMN_NAME_RECONNECT + " FROM " + TABLE_NAME_USER_COMPUTER + " WHERE " + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED + "  = ?";

    /* find reconnect by user computer id */
    String SQL_FIND_RECONNECT_BY_ID = "SELECT " + COLUMN_NAME_RECONNECT + " FROM " + TABLE_NAME_USER_COMPUTER + " WHERE " + COLUMN_NAME_USER_COMPUTER_ID + "  = ?";

    /* update reconnect by user computer id */
    String SQL_UPDATE_RECONNECT_BY_ID = "UPDATE " + TABLE_NAME_USER_COMPUTER + " SET " + COLUMN_NAME_RECONNECT + " = ? WHERE " + COLUMN_NAME_USER_COMPUTER_ID + " = ?";

}
