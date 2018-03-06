package org.clopuccino.db;

/**
 * <code>DatabaseConstants</code>
 * <p/>
 * All values to lower-case for postgresql default settings.
 *
 * @author masonhsieh
 * @version 1.0
 */
public interface DatabaseConstants {

    /* TABLE: REGION related */

    String TABLE_NAME_COUNTRY = "country";

    String COLUMN_NAME_COUNTRY_CODE = "c_code";

    String COLUMN_NAME_COUNTRY_PHONE_SAMPLE = "c_phone_sample";

    String COLUMN_NAME_COUNTRY_ID = "c_id";

    String COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX = "l_";

    String COLUMN_NAME_COUNTRY_LOCALE_COLUMN_DEFAULT_NAME = "default";

    String COLUMN_NAME_COUNTRY_LOCALE_DEFAULT = COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX + COLUMN_NAME_COUNTRY_LOCALE_COLUMN_DEFAULT_NAME;

    String COLUMN_NAME_COUNTRY_LOCALE_EN = COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX + "en";

    String COLUMN_NAME_COUNTRY_LOCALE_ZH = COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX + "zh";

    String COLUMN_NAME_COUNTRY_LOCALE_ZH_TW = COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX + "zh_tw";

    String COLUMN_NAME_COUNTRY_LOCALE_ZH_HK = COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX + "zh_hk";

    String COLUMN_NAME_COUNTRY_LOCALE_ZH_CN = COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX + "zh_cn";

    String COLUMN_NAME_COUNTRY_LOCALE_JA_JP = COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX + "ja_jp";

    // Add other region locales here, the value MUST start with "L_" and then the locale with CASE-INSENSETIVE

    String COLUMN_NAME_COUNTRY_AVAILABLE = "c_available";

    String FILE_NAME_DEFAULT_COUNTRIES = "default-countries.txt";

    String FILE_DEFAULT_COUNTRIES_DELIMITERS = "@@";

    String DEFAULT_PLAIN_TEXT_FILE_COMMENT_CHARACTER = "#";

    /* TABLE: USER related */

    String TABLE_NAME_AUTH_USER = "auth_user";

    String COLUMN_NAME_AUTH_USER_ID = "auth_user_id";

    String COLUMN_NAME_PHONE_NUMBER = "phone_number";

    String COLUMN_NAME_PASSWD = "passwd";

    String COLUMN_NAME_NICKNAME = "nickname";

    String COLUMN_NAME_SHOW_HIDDEN = "show_hidden";

    String COLUMN_NAME_AUTH_USER_VERIFIED = "auth_user_verified";

    String COLUMN_NAME_AUTH_USER_ID_ENCRYPTED = "auth_user_id_encrypted";

    String COLUMN_NAME_VERIFY_CODE = "verify_code";

    String COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP = "verify_code_sent_timestamp";

    String COLUMN_NAME_RESET_PASSWORD_CODE = "reset_passwd_code";

    String COLUMN_NAME_RESET_PASSWORD_CODE_SENT_TIMESTAMP = "reset_code_sent_timestamp";

    String COLUMN_NAME_AUTH_USER_EMAIL = "auth_user_email";

    String COLUMN_NAME_AUTH_USER_EMAIL_NOT_VERIFIED = "auth_user_email_not_verified";

    String COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE = "change_email_code";

    String COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE_SENT_TIMESTAMP = "change_email_code_sent_timestamp";

    String COLUMN_NAME_PHONE_NUMBER_NOT_VERIFIED = "phone_number_not_verified";

    String COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE = "change_phone_number_code";

    String COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP = "change_phone_number_code_sent_timestamp";

    String COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE = "phone_number_should_update";

    String COLUMN_NAME_AVAILABLE_TRANSFER_BYTES = "at_bytes";

    String COLUMN_NAME_IS_UNLIMITED_TRANSFER = "unlimited_transfer";

    String COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES = "download_size_limit_bytes";

    String COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES = "upload_size_limit_bytes";

    long DEFAULT_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES = 209715200L; // 200 * 1024 *1024L; 200 MB

    long DEFAULT_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES = 209715200L; // 200 * 1024 *1024L; 200 MB

//    long DEFAULT_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES = 524288000L; // 200 * 1024 *1024L; 500 MB
//
//    long DEFAULT_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES = 524288000L; // 200 * 1024 *1024L; 500 MB

    String COLUMN_NAME_IS_DELETABLE = "is_deletable";

    /* initial available transfer bytes -- 2 GB */
    long DEFAULT_INITIAL_USER_AVAILABLE_TRANSFER_BYTES = 2147483648L;

    /* purchase capacity bytes per purchase quantity -- 2 GB */
    long DEFAULT_PURCHASE_CAPACITY_BYTES_PER_QUANTITY = 2147483648L;

    String INDEX_NAME_AUTH_USER_COUNTRY_ID = "index_auth_user_country_id";

    String INDEX_NAME_AUTH_USER_PHONE_NUMBER = "index_auth_user_phone_number";

    String INDEX_NAME_AUTH_USER_PHONE_NUMBER_SHOULD_UPDATE = "index_auth_user_phone_number_should_update";

    String INDEX_NAME_COUNTRY_COUNTRY_CODE = "index_country_c_code";

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

    String SQL_CREATE_INDEX_COUNTRY_COUNTRY_CODE = "CREATE INDEX " + INDEX_NAME_COUNTRY_COUNTRY_CODE + " ON " + TABLE_NAME_COUNTRY + "(" + COLUMN_NAME_COUNTRY_CODE + ")";

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

    String SQL_FIND_COUNTRY_ID_BY_COUNTRY_CODE = "SELECT "
                                                 + COLUMN_NAME_COUNTRY_ID
                                                 + " FROM "
                                                 + TABLE_NAME_COUNTRY
                                                 + " WHERE "
                                                 + COLUMN_NAME_COUNTRY_CODE + " = ?";

    /* DDL - TABLE: AUTH_USER */

    /* MAKE SURE false is supported for non-sql db */
    String SQL_CREATE_TABLE_AUTH_USER = "CREATE TABLE " + TABLE_NAME_AUTH_USER + "("
                                        + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) PRIMARY KEY, "
                                        + COLUMN_NAME_AUTH_USER_ID_ENCRYPTED + " VARCHAR(4096) NOT NULL, "
                                        + COLUMN_NAME_COUNTRY_ID + " VARCHAR(8) NOT NULL, "
                                        + COLUMN_NAME_PHONE_NUMBER + " VARCHAR(24) NOT NULL, "
                                        + COLUMN_NAME_PASSWD + " VARCHAR(1024) NULL, "
                                        + COLUMN_NAME_NICKNAME + " VARCHAR(1024) NULL, "
                                        + COLUMN_NAME_SHOW_HIDDEN + " BOOLEAN DEFAULT false, "
                                        + COLUMN_NAME_AUTH_USER_VERIFIED + " BOOLEAN DEFAULT false, "
                                        + COLUMN_NAME_VERIFY_CODE + " VARCHAR(1024) NULL, "
                                        + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP + " BIGINT, "
                                        + COLUMN_NAME_RESET_PASSWORD_CODE + " VARCHAR(1024), "
                                        + COLUMN_NAME_RESET_PASSWORD_CODE_SENT_TIMESTAMP + " BIGINT, "
                                        + COLUMN_NAME_AUTH_USER_EMAIL_NOT_VERIFIED + " VARCHAR(1024), "
                                        + COLUMN_NAME_AUTH_USER_EMAIL + " VARCHAR(1024), "
                                        + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE + " VARCHAR(1024), "
                                        + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE_SENT_TIMESTAMP + " BIGINT, "
                                        + COLUMN_NAME_PHONE_NUMBER_NOT_VERIFIED + " VARCHAR(24), "
                                        + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE + " VARCHAR(1024), "
                                        + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP + " BIGINT, "
                                        + COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE + " BOOLEAN DEFAULT false, "
                                        + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES + " NUMERIC(15, 0) DEFAULT " + DEFAULT_INITIAL_USER_AVAILABLE_TRANSFER_BYTES + ", "  // NUMERIC(15, 0) ~ 900 TB Capacity at most
                                        + COLUMN_NAME_IS_UNLIMITED_TRANSFER + " BOOLEAN DEFAULT true, "
                                        + COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES + " BIGINT NOT NULL DEFAULT " + DEFAULT_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                        + COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES + " BIGINT NOT NULL DEFAULT " + DEFAULT_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                        + COLUMN_NAME_IS_DELETABLE + " BOOLEAN DEFAULT true)";

    String SQL_CREATE_INDEX_AUTH_USER_COUNTRY_ID = "CREATE INDEX " + INDEX_NAME_AUTH_USER_COUNTRY_ID + " ON " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_COUNTRY_ID + ")";

    String SQL_CREATE_INDEX_AUTH_USER_PHONE_NUMBER = "CREATE INDEX " + INDEX_NAME_AUTH_USER_PHONE_NUMBER + " ON " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_PHONE_NUMBER + ")";

    String SQL_CREATE_INDEX_AUTH_USER_PHONE_NUMBER_SHOULD_UPDATE = "CREATE INDEX " + INDEX_NAME_AUTH_USER_PHONE_NUMBER_SHOULD_UPDATE + " ON " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE + ")";

    // find user by id
    String SQL_FIND_AUTH_USER_BY_ID = "SELECT "
                                      + COLUMN_NAME_AUTH_USER_ID + ", "
                                      + COLUMN_NAME_COUNTRY_ID + ", "
                                      + COLUMN_NAME_PHONE_NUMBER + ", "
                                      + COLUMN_NAME_PASSWD + ", "
                                      + COLUMN_NAME_NICKNAME + ", "
                                      + COLUMN_NAME_SHOW_HIDDEN + ", "
                                      + COLUMN_NAME_AUTH_USER_VERIFIED + ", "
                                      + COLUMN_NAME_VERIFY_CODE + ", "
                                      + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP + ", "
                                      + COLUMN_NAME_RESET_PASSWORD_CODE + ", "
                                      + COLUMN_NAME_RESET_PASSWORD_CODE_SENT_TIMESTAMP + ", "
                                      + COLUMN_NAME_AUTH_USER_EMAIL_NOT_VERIFIED + ", "
                                      + COLUMN_NAME_AUTH_USER_EMAIL + ", "
                                      + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE + ", "
                                      + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE_SENT_TIMESTAMP + ", "
                                      + COLUMN_NAME_PHONE_NUMBER_NOT_VERIFIED + ", "
                                      + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE + ", "
                                      + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP + ", "
                                      + COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE + ", "
                                      + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES + ", "
                                      + COLUMN_NAME_IS_UNLIMITED_TRANSFER + ", "
                                      + COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                      + COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                      + COLUMN_NAME_IS_DELETABLE
                                      + " FROM "
                                      + TABLE_NAME_AUTH_USER
                                      + " WHERE "
                                      + COLUMN_NAME_AUTH_USER_ID + " = ?";
    // find nickname by user id
    String SQL_FIND_NICKNAME_BY_USER_ID = "SELECT "
                                          + COLUMN_NAME_NICKNAME
                                          + " FROM "
                                          + TABLE_NAME_AUTH_USER
                                          + " WHERE "
                                          + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* find user by country id and phone number */
    String SQL_FIND_AUTH_USER_BY_PHONE = "SELECT "
                                      + COLUMN_NAME_AUTH_USER_ID + ", "
                                      + COLUMN_NAME_COUNTRY_ID + ", "
                                      + COLUMN_NAME_PHONE_NUMBER + ", "
                                      + COLUMN_NAME_PASSWD + ", "
                                      + COLUMN_NAME_NICKNAME + ", "
                                      + COLUMN_NAME_SHOW_HIDDEN + ", "
                                      + COLUMN_NAME_AUTH_USER_VERIFIED + ", "
                                      + COLUMN_NAME_VERIFY_CODE + ", "
                                      + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP + ", "
                                      + COLUMN_NAME_RESET_PASSWORD_CODE + ", "
                                      + COLUMN_NAME_RESET_PASSWORD_CODE_SENT_TIMESTAMP + ", "
                                      + COLUMN_NAME_AUTH_USER_EMAIL_NOT_VERIFIED + ", "
                                      + COLUMN_NAME_AUTH_USER_EMAIL + ", "
                                      + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE + ", "
                                      + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE_SENT_TIMESTAMP + ", "
                                      + COLUMN_NAME_PHONE_NUMBER_NOT_VERIFIED + ", "
                                      + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE + ", "
                                      + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP + ", "
                                      + COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE + ", "
                                      + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES + ", "
                                      + COLUMN_NAME_IS_UNLIMITED_TRANSFER + ", "
                                      + COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                      + COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                      + COLUMN_NAME_IS_DELETABLE
                                      + " FROM "
                                      + TABLE_NAME_AUTH_USER
                                      + " WHERE "
                                      + COLUMN_NAME_COUNTRY_ID
                                      + " = ? AND "
                                      + COLUMN_NAME_PHONE_NUMBER + " = ?";

    String SQL_FIND_AUTH_USER_BY_PHONE_AND_ZERO_PREFIX_PHONE_NUMBER = "SELECT "
                                                                      + COLUMN_NAME_AUTH_USER_ID + ", "
                                                                      + COLUMN_NAME_COUNTRY_ID + ", "
                                                                      + COLUMN_NAME_PHONE_NUMBER + ", "
                                                                      + COLUMN_NAME_PASSWD + ", "
                                                                      + COLUMN_NAME_NICKNAME + ", "
                                                                      + COLUMN_NAME_SHOW_HIDDEN + ", "
                                                                      + COLUMN_NAME_AUTH_USER_VERIFIED + ", "
                                                                      + COLUMN_NAME_VERIFY_CODE + ", "
                                                                      + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP + ", "
                                                                      + COLUMN_NAME_RESET_PASSWORD_CODE + ", "
                                                                      + COLUMN_NAME_RESET_PASSWORD_CODE_SENT_TIMESTAMP + ", "
                                                                      + COLUMN_NAME_AUTH_USER_EMAIL_NOT_VERIFIED + ", "
                                                                      + COLUMN_NAME_AUTH_USER_EMAIL + ", "
                                                                      + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE + ", "
                                                                      + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE_SENT_TIMESTAMP + ", "
                                                                      + COLUMN_NAME_PHONE_NUMBER_NOT_VERIFIED + ", "
                                                                      + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE + ", "
                                                                      + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP + ", "
                                                                      + COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE + ", "
                                                                      + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES + ", "
                                                                      + COLUMN_NAME_IS_UNLIMITED_TRANSFER + ", "
                                                                      + COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                                                      + COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                                                      + COLUMN_NAME_IS_DELETABLE
                                                                      + " FROM "
                                                                      + TABLE_NAME_AUTH_USER
                                                                      + " WHERE "
                                                                      + COLUMN_NAME_COUNTRY_ID
                                                                      + " = ? AND "
                                                                      + COLUMN_NAME_PHONE_NUMBER + " IN (?, ?)";

    /* find user by country id and phone number */
    String SQL_FIND_VERIFIED_AUTH_USER_BY_PHONE = "SELECT "
                                                  + COLUMN_NAME_AUTH_USER_ID + ", "
                                                  + COLUMN_NAME_COUNTRY_ID + ", "
                                                  + COLUMN_NAME_PHONE_NUMBER + ", "
                                                  + COLUMN_NAME_PASSWD + ", "
                                                  + COLUMN_NAME_NICKNAME + ", "
                                                  + COLUMN_NAME_SHOW_HIDDEN + ", "
                                                  + COLUMN_NAME_AUTH_USER_VERIFIED + ", "
                                                  + COLUMN_NAME_VERIFY_CODE + ", "
                                                  + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP + ", "
                                                  + COLUMN_NAME_RESET_PASSWORD_CODE + ", "
                                                  + COLUMN_NAME_RESET_PASSWORD_CODE_SENT_TIMESTAMP + ", "
                                                  + COLUMN_NAME_AUTH_USER_EMAIL_NOT_VERIFIED + ", "
                                                  + COLUMN_NAME_AUTH_USER_EMAIL + ", "
                                                  + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE + ", "
                                                  + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE_SENT_TIMESTAMP + ", "
                                                  + COLUMN_NAME_PHONE_NUMBER_NOT_VERIFIED + ", "
                                                  + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE + ", "
                                                  + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP + ", "
                                                  + COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE + ", "
                                                  + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES + ", "
                                                  + COLUMN_NAME_IS_UNLIMITED_TRANSFER + ", "
                                                  + COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                                  + COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                                  + COLUMN_NAME_IS_DELETABLE
                                                  + " FROM "
                                                  + TABLE_NAME_AUTH_USER
                                                  + " WHERE "
                                                  + COLUMN_NAME_COUNTRY_ID
                                                  + " = ? AND "
                                                  + COLUMN_NAME_PHONE_NUMBER
                                                  + " = ? AND "
                                                  + COLUMN_NAME_AUTH_USER_VERIFIED
                                                  + " = true";

    /* Same with SQL_FIND_AUTH_USER_BY_PHONE, but select user id only */
    String SQL_FIND_AUTH_USER_ID_BY_PHONE = "SELECT "
                                         + COLUMN_NAME_AUTH_USER_ID
                                         + " FROM "
                                         + TABLE_NAME_AUTH_USER
                                         + " WHERE "
                                         + COLUMN_NAME_COUNTRY_ID
                                         + " = ? AND "
                                         + COLUMN_NAME_PHONE_NUMBER + " = ?";

    /* find user by country id and phone number with condition if should update phone number */
    String SQL_FIND_AUTH_USER_BY_PHONE_WITH_SHOULD_UPDATE_PHONE_NUMBER = "SELECT "
                                                                         + COLUMN_NAME_AUTH_USER_ID + ", "
                                                                         + COLUMN_NAME_COUNTRY_ID + ", "
                                                                         + COLUMN_NAME_PHONE_NUMBER + ", "
                                                                         + COLUMN_NAME_PASSWD + ", "
                                                                         + COLUMN_NAME_NICKNAME + ", "
                                                                         + COLUMN_NAME_SHOW_HIDDEN + ", "
                                                                         + COLUMN_NAME_AUTH_USER_VERIFIED + ", "
                                                                         + COLUMN_NAME_VERIFY_CODE + ", "
                                                                         + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP + ", "
                                                                         + COLUMN_NAME_RESET_PASSWORD_CODE + ", "
                                                                         + COLUMN_NAME_RESET_PASSWORD_CODE_SENT_TIMESTAMP + ", "
                                                                         + COLUMN_NAME_AUTH_USER_EMAIL_NOT_VERIFIED + ", "
                                                                         + COLUMN_NAME_AUTH_USER_EMAIL + ", "
                                                                         + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE + ", "
                                                                         + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE_SENT_TIMESTAMP + ", "
                                                                         + COLUMN_NAME_PHONE_NUMBER_NOT_VERIFIED + ", "
                                                                         + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE + ", "
                                                                         + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP + ", "
                                                                         + COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE + ", "
                                                                         + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES + ", "
                                                                         + COLUMN_NAME_IS_UNLIMITED_TRANSFER + ", "
                                                                         + COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                                                         + COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                                                         + COLUMN_NAME_IS_DELETABLE
                                                                         + " FROM "
                                                                         + TABLE_NAME_AUTH_USER
                                                                         + " WHERE "
                                                                         + COLUMN_NAME_COUNTRY_ID
                                                                         + " = ? AND "
                                                                         + COLUMN_NAME_PHONE_NUMBER
                                                                         + " = ? AND "
                                                                         + COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE + " = ?";

    /* find user by country id and phone number with condition if should update phone number */
    String SQL_FIND_AUTH_USER_BY_PHONE_WITH_SHOULD_UPDATE_PHONE_NUMBER_AND_ZERO_PREFIX_PHONE_NUMBER = "SELECT "
                                                                                                      + COLUMN_NAME_AUTH_USER_ID + ", "
                                                                                                      + COLUMN_NAME_COUNTRY_ID + ", "
                                                                                                      + COLUMN_NAME_PHONE_NUMBER + ", "
                                                                                                      + COLUMN_NAME_PASSWD + ", "
                                                                                                      + COLUMN_NAME_NICKNAME + ", "
                                                                                                      + COLUMN_NAME_SHOW_HIDDEN + ", "
                                                                                                      + COLUMN_NAME_AUTH_USER_VERIFIED + ", "
                                                                                                      + COLUMN_NAME_VERIFY_CODE + ", "
                                                                                                      + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP + ", "
                                                                                                      + COLUMN_NAME_RESET_PASSWORD_CODE + ", "
                                                                                                      + COLUMN_NAME_RESET_PASSWORD_CODE_SENT_TIMESTAMP + ", "
                                                                                                      + COLUMN_NAME_AUTH_USER_EMAIL_NOT_VERIFIED + ", "
                                                                                                      + COLUMN_NAME_AUTH_USER_EMAIL + ", "
                                                                                                      + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE + ", "
                                                                                                      + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE_SENT_TIMESTAMP + ", "
                                                                                                      + COLUMN_NAME_PHONE_NUMBER_NOT_VERIFIED + ", "
                                                                                                      + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE + ", "
                                                                                                      + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP + ", "
                                                                                                      + COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE + ", "
                                                                                                      + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES + ", "
                                                                                                      + COLUMN_NAME_IS_UNLIMITED_TRANSFER + ", "
                                                                                                      + COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                                                                                      + COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                                                                                      + COLUMN_NAME_IS_DELETABLE
                                                                                                      + " FROM "
                                                                                                      + TABLE_NAME_AUTH_USER
                                                                                                      + " WHERE "
                                                                                                      + COLUMN_NAME_COUNTRY_ID
                                                                                                      + " = ? AND "
                                                                                                      + COLUMN_NAME_PHONE_NUMBER
                                                                                                      + " IN (?, ?) AND "
                                                                                                      + COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE + " = ?";

    /* find registered and verified user by country id and phone number with condition if should update phone number */
    String SQL_FIND_VERIFIED_AUTH_USER_BY_PHONE_WITH_SHOULD_UPDATE_PHONE_NUMBER = "SELECT "
                                                                                  + COLUMN_NAME_AUTH_USER_ID + ", "
                                                                                  + COLUMN_NAME_COUNTRY_ID + ", "
                                                                                  + COLUMN_NAME_PHONE_NUMBER + ", "
                                                                                  + COLUMN_NAME_PASSWD + ", "
                                                                                  + COLUMN_NAME_NICKNAME + ", "
                                                                                  + COLUMN_NAME_SHOW_HIDDEN + ", "
                                                                                  + COLUMN_NAME_AUTH_USER_VERIFIED + ", "
                                                                                  + COLUMN_NAME_VERIFY_CODE + ", "
                                                                                  + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP + ", "
                                                                                  + COLUMN_NAME_RESET_PASSWORD_CODE + ", "
                                                                                  + COLUMN_NAME_RESET_PASSWORD_CODE_SENT_TIMESTAMP + ", "
                                                                                  + COLUMN_NAME_AUTH_USER_EMAIL_NOT_VERIFIED + ", "
                                                                                  + COLUMN_NAME_AUTH_USER_EMAIL + ", "
                                                                                  + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE + ", "
                                                                                  + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE_SENT_TIMESTAMP + ", "
                                                                                  + COLUMN_NAME_PHONE_NUMBER_NOT_VERIFIED + ", "
                                                                                  + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE + ", "
                                                                                  + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP + ", "
                                                                                  + COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE + ", "
                                                                                  + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES + ", "
                                                                                  + COLUMN_NAME_IS_UNLIMITED_TRANSFER + ", "
                                                                                  + COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                                                                  + COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                                                                  + COLUMN_NAME_IS_DELETABLE
                                                                                  + " FROM "
                                                                                  + TABLE_NAME_AUTH_USER
                                                                                  + " WHERE "
                                                                                  + COLUMN_NAME_COUNTRY_ID
                                                                                  + " = ? AND "
                                                                                  + COLUMN_NAME_PHONE_NUMBER
                                                                                  + " = ? AND "
                                                                                  + COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE
                                                                                  + " = ? AND "
                                                                                  + COLUMN_NAME_AUTH_USER_VERIFIED
                                                                                  + " = true";

    /* Same with SQL_FIND_AUTH_USER_BY_PHONE_WITH_SHOULD_UPDATE_PHONE_NUMBER, but select user id only */
    String SQL_FIND_AUTH_USER_ID_BY_PHONE_WITH_SHOULD_UPDATE_PHONE_NUMBER = "SELECT "
                                                                         + COLUMN_NAME_AUTH_USER_ID
                                                                         + " FROM "
                                                                         + TABLE_NAME_AUTH_USER
                                                                         + " WHERE "
                                                                         + COLUMN_NAME_COUNTRY_ID
                                                                         + " = ? AND "
                                                                         + COLUMN_NAME_PHONE_NUMBER
                                                                         + " = ? AND "
                                                                         + COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE + " = ?";

    /* create user */
    String SQL_CREATE_AUTH_USER = "INSERT INTO " + TABLE_NAME_AUTH_USER + "("
                                  + COLUMN_NAME_AUTH_USER_ID + ", "
                                  + COLUMN_NAME_AUTH_USER_ID_ENCRYPTED + ", "
                                  + COLUMN_NAME_COUNTRY_ID + ", "
                                  + COLUMN_NAME_PHONE_NUMBER + ", "
                                  + COLUMN_NAME_PASSWD + ", "
                                  + COLUMN_NAME_NICKNAME + ", "
                                  + COLUMN_NAME_SHOW_HIDDEN + ", "
                                  + COLUMN_NAME_AUTH_USER_VERIFIED + ", "
                                  + COLUMN_NAME_VERIFY_CODE + ", "
                                  + COLUMN_NAME_AUTH_USER_EMAIL + ", "
                                  + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /* clear all data in table user */
//    String SQL_TRUNCATE_AUTH_USER = "TRUNCATE TABLE " + TABLE_NAME_AUTH_USER;

    /* update show hidden by user id */
//    String SQL_UPDATE_SHOW_HIDDEN_BY_ID = "UPDATE " + TABLE_NAME_AUTH_USER + " SET " + COLUMN_NAME_SHOW_HIDDEN + " = ? WHERE " + COLUMN_NAME_AUTH_USER_ID + " = ?";

//    /* find reconnect by user id-encrypted */
//    String SQL_FIND_RECONNECT_BY_ID_ENCRYPTED = "SELECT " + COLUMN_NAME_RECONNECT + " FROM " + TABLE_NAME_AUTH_USER + " WHERE " + COLUMN_NAME_AUTH_USER_ID_ENCRYPTED + "  = ?";

//    /* find reconnect by user id */
//    String SQL_FIND_RECONNECT_BY_ID = "SELECT " + COLUMN_NAME_RECONNECT + " FROM " + TABLE_NAME_AUTH_USER + " WHERE " + COLUMN_NAME_AUTH_USER_ID + "  = ?";

    /* check if user exists by user id */
    String SQL_FIND_IF_EXISTS_AUTH_USER_BY_ID = "SELECT "
                                                + COLUMN_NAME_AUTH_USER_ID
                                                + " FROM "
                                                + TABLE_NAME_AUTH_USER
                                                + " WHERE "
                                                + COLUMN_NAME_AUTH_USER_ID + "  = ?";


    /* check if user exists by country id and phone number */
    String SQL_FIND_IF_EXISTS_AUTH_USER_BY_PHONE = "SELECT "
                                                   + COLUMN_NAME_AUTH_USER_ID
                                                   + " FROM "
                                                   + TABLE_NAME_AUTH_USER
                                                   + " WHERE "
                                                   + COLUMN_NAME_COUNTRY_ID
                                                   + "  = ? AND "
                                                   + COLUMN_NAME_PHONE_NUMBER + " = ?";

    /* find user by user id and verify code */
//    String SQL_FIND_AUTH_USER_BY_VERIFY_CODE = "SELECT "
//                                               + COLUMN_NAME_AUTH_USER_ID + ", "
//                                               + COLUMN_NAME_COUNTRY_ID + ", "
//                                               + COLUMN_NAME_PHONE_NUMBER + ", "
//                                               + COLUMN_NAME_PASSWD + ", "
//                                               + COLUMN_NAME_NICKNAME + ", "
//                                               + COLUMN_NAME_SHOW_HIDDEN + ", "
//                                               + COLUMN_NAME_AUTH_USER_VERIFIED + ", "
//                                               + COLUMN_NAME_VERIFY_CODE + ", "
//                                               + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP + ", "
//                                               + COLUMN_NAME_AUTH_USER_EMAIL + ", "
//                                               + COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE + ", "
//                                               + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES + ", "
//                                               + COLUMN_NAME_IS_UNLIMITED_TRANSFER + ", "
//                                               + COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
//                                               + COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
//                                               + COLUMN_NAME_IS_DELETABLE
//                                               + " FROM "
//                                               + TABLE_NAME_AUTH_USER
//                                               + " WHERE "
//                                               + COLUMN_NAME_AUTH_USER_ID + " = ? AND "
//                                               + COLUMN_NAME_VERIFY_CODE + " = ?";

    /* update verified by auth user id */
    String SQL_UPDATE_AUTH_USER_VERIFIED_BY_ID = "UPDATE "
                                                 + TABLE_NAME_AUTH_USER
                                                 + " SET "
                                                 + COLUMN_NAME_AUTH_USER_VERIFIED
                                                 + " = ? WHERE "
                                                 + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update verify code sent timestamp by auth user id */
    String SQL_UPDATE_VERIFY_CODE_SENT_TIMESTAMP_BY_ID = "UPDATE "
                                                         + TABLE_NAME_AUTH_USER
                                                         + " SET "
                                                         + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP
                                                         + " = ? WHERE "
                                                         + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update change-email new email and security code sent timestamp by auth user id */
    String SQL_UPDATE_CHANGE_EMAIL_NEW_MAIL_AND_SECURITY_CODE_SENT_TIMESTAMP_BY_ID = "UPDATE "
                                                                        + TABLE_NAME_AUTH_USER
                                                                        + " SET "
                                                                        + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE + " = ?, "
                                                                        + COLUMN_NAME_AUTH_USER_EMAIL_NOT_VERIFIED + " = ?, "
                                                                        + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE_SENT_TIMESTAMP
                                                                        + " = ? WHERE "
                                                                        + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update reset password security code sent timestamp by auth user id */
    String SQL_UPDATE_RESET_PASSWORD_SECURITY_CODE_SENT_TIMESTAMP_BY_ID = "UPDATE "
                                                                          + TABLE_NAME_AUTH_USER
                                                                          + " SET "
                                                                          + COLUMN_NAME_RESET_PASSWORD_CODE_SENT_TIMESTAMP
                                                                          + " = ? WHERE "
                                                                          + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update change phone number security code sent timestamp by auth user id */
    String SQL_UPDATE_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP_BY_ID = "UPDATE "
                                                                          + TABLE_NAME_AUTH_USER
                                                                          + " SET "
                                                                          + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP
                                                                          + " = ? WHERE "
                                                                          + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update reset password security code by auth user id */
    String SQL_UPDATE_AUTH_USER_RESET_SECURITY_CODE_BY_ID = "UPDATE "
                                                            + TABLE_NAME_AUTH_USER
                                                            + " SET "
                                                            + COLUMN_NAME_RESET_PASSWORD_CODE
                                                            + " = ? WHERE "
                                                            + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update change phone number security code by auth user id */
    String SQL_UPDATE_AUTH_USER_CHANGE_PHONE_NUMBER_SECURITY_CODE_BY_ID = "UPDATE "
                                                                          + TABLE_NAME_AUTH_USER
                                                                          + " SET "
                                                                          + COLUMN_NAME_PHONE_NUMBER_NOT_VERIFIED + " = ?, "
                                                                          + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE
                                                                          + " = ? WHERE "
                                                                          + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update shouldUpdatePhoneNumber to true */
    String SQL_UPDATE_AUTH_USER_PHONE_NUMBER_SHOULD_UPDATE = "UPDATE "
                                                            + TABLE_NAME_AUTH_USER
                                                            + " SET "
                                                            + COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE
                                                            + " = ? WHERE "
                                                            + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update change-user-email security code by auth user id */
//    String SQL_UPDATE_AUTH_USER_CHANGE_EMAIL_SECURITY_CODE_BY_ID = "UPDATE "
//                                                                   + TABLE_NAME_AUTH_USER
//                                                                   + " SET "
//                                                                   + COLUMN_NAME_AUTH_USER_EMAIL + " = ?, "
//                                                                   + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE + " = ?,"
//                                                                   + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE_SENT_TIMESTAMP
//                                                                   + " = ? WHERE "
//                                                                   + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update available transfer bytes by user id */
    String SQL_UPDATE_AVAILABLE_TRANSFER_BYTES_BY_USER = "UPDATE "
                                                         + TABLE_NAME_AUTH_USER
                                                         + " SET "
                                                         + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES
                                                         + " = ? WHERE "
                                                         + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* find available transfer bytes by user id */
    String SQL_FIND_IF_UNLIMITED_TRANSFER_BY_USER = "SELECT "
                                                    + COLUMN_NAME_IS_UNLIMITED_TRANSFER
                                                    + " FROM "
                                                    + TABLE_NAME_AUTH_USER
                                                    + " WHERE "
                                                    + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* find available transfer bytes by user id */
    String SQL_FIND_AVAILABLE_TRANSFER_BYTES_BY_USER = "SELECT "
                                                       + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES
                                                       + " FROM "
                                                       + TABLE_NAME_AUTH_USER
                                                       + " WHERE "
                                                       + COLUMN_NAME_AUTH_USER_ID + " = ?";

    // find download file size limit in bytes by user id
    String SQL_FIND_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES_BY_USER = "SELECT "
                                                                + COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES
                                                                + " FROM "
                                                                + TABLE_NAME_AUTH_USER
                                                                + " WHERE "
                                                                + COLUMN_NAME_AUTH_USER_ID + " = ?";

    // find upload file size limit in bytes by user id
    String SQL_FIND_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES_BY_USER = "SELECT "
                                                              + COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES
                                                              + " FROM "
                                                              + TABLE_NAME_AUTH_USER
                                                              + " WHERE "
                                                              + COLUMN_NAME_AUTH_USER_ID + " = ?";

    // find upload file size limit in bytes by user id
    String SQL_FIND_DOWNLOAD_AND_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES_BY_USER = "SELECT "
                                                                           + COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES + ", "
                                                                           + COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES
                                                                           + " FROM "
                                                                           + TABLE_NAME_AUTH_USER
                                                                           + " WHERE "
                                                                           + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* find timeout-unverified-users, need to specified the timeout timestamp in milliseconds */
    /* MAKE SURE false is supported for non-sql db */
    String SQL_FIND_USERS_BY_VERIFIED_NO_NOT = "SELECT "
                                               + COLUMN_NAME_AUTH_USER_ID + ", "
                                               + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP
                                               + " FROM "
                                               + TABLE_NAME_AUTH_USER
                                               + " WHERE "
                                               + COLUMN_NAME_AUTH_USER_VERIFIED + " = ?";

    /* delete user by id */
    String SQL_DELETE_USER_BY_ID_AND_VERIFIED = "DELETE FROM "
                                              + TABLE_NAME_AUTH_USER
                                              + " WHERE "
                                              + COLUMN_NAME_AUTH_USER_ID
                                              + " = ? AND "
                                              + COLUMN_NAME_AUTH_USER_VERIFIED + " = ?";

    /* delete user by country id and phone number, it must be not verified and so there will be no cascading issues */
    String SQL_DELETE_USER_BY_PHONE_NO_CASCADE = "DELETE FROM "
                                              + TABLE_NAME_AUTH_USER
                                              + " WHERE "
                                              + COLUMN_NAME_COUNTRY_ID
                                              + " = ? AND "
                                              + COLUMN_NAME_PHONE_NUMBER
                                              + " = ? AND "
                                              + COLUMN_NAME_AUTH_USER_VERIFIED + " = ?";

    /* update password by auth user id */
    String SQL_UPDATE_AUTH_USER_PASSWORD = "UPDATE "
                                           + TABLE_NAME_AUTH_USER
                                           + " SET "
                                           + COLUMN_NAME_PASSWD
                                           + " = ? WHERE "
                                           + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update password and clear reset_password_code and reset_code_sent_timestamp by auth user id */
    String SQL_UPDATE_AUTH_USER_PASSWORD_AND_CLEAR_RESET_PASSWORD_DATA = "UPDATE "
                                                                         + TABLE_NAME_AUTH_USER
                                                                         + " SET "
                                                                         + COLUMN_NAME_RESET_PASSWORD_CODE + " = null, "
                                                                         + COLUMN_NAME_RESET_PASSWORD_CODE_SENT_TIMESTAMP + " = 0, "
                                                                         + COLUMN_NAME_PASSWD
                                                                         + " = ? WHERE "
                                                                         + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update password by auth user id */
    String SQL_UPDATE_AUTH_USER_NICKNAME = "UPDATE "
                                           + TABLE_NAME_AUTH_USER
                                           + " SET "
                                           + COLUMN_NAME_NICKNAME
                                           + " = ? WHERE "
                                           + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update email by auth user id */
    String SQL_UPDATE_AUTH_USER_EMAIL = "UPDATE "
                                           + TABLE_NAME_AUTH_USER
                                           + " SET "
                                           + COLUMN_NAME_AUTH_USER_EMAIL
                                           + " = ? WHERE "
                                           + COLUMN_NAME_AUTH_USER_ID + " = ?";

    String SQL_UPDATE_AUTH_USER_UNVERIFIED_EMAIL_AND_NICKNAME = "UPDATE "
                                                                + TABLE_NAME_AUTH_USER
                                                                + " SET "
                                                                + COLUMN_NAME_AUTH_USER_EMAIL_NOT_VERIFIED + " = ?, "
                                                                + COLUMN_NAME_NICKNAME
                                                                + " = ? WHERE "
                                                                + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* update email and clear the values of
     * auth_user_email_not_verified,
     * change_email_code and
     * change_email_code_sent_timestamp by auth user id
     */
    String SQL_UPDATE_AUTH_USER_EMAIL_AND_CHANGE_EMAIL_DATA = "UPDATE "
                                                              + TABLE_NAME_AUTH_USER
                                                              + " SET "
                                                              + COLUMN_NAME_AUTH_USER_EMAIL_NOT_VERIFIED + " = null, "
                                                              + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE + " = null, "
                                                              + COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE_SENT_TIMESTAMP + " = 0, "
                                                              + COLUMN_NAME_AUTH_USER_EMAIL
                                                              + " = ? WHERE "
                                                              + COLUMN_NAME_AUTH_USER_ID + " = ?";

    String SQL_UPDATE_AUTH_USER_PHONE_NUMBER = "UPDATE "
                                               + TABLE_NAME_AUTH_USER
                                               + " SET "
                                               + COLUMN_NAME_COUNTRY_ID + " = ?, "
                                               + COLUMN_NAME_PHONE_NUMBER
                                               + " = ? WHERE "
                                               + COLUMN_NAME_AUTH_USER_ID + " = ?";

    String SQL_UPDATE_AUTH_USER_PHONE_NUMBER_AND_CHANGE_PHONE_NUMBER_DATA = "UPDATE "
                                                                            + TABLE_NAME_AUTH_USER
                                                                            + " SET "
                                                                            + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE + " = null, "
                                                                            + COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP + " = 0, "
                                                                            + COLUMN_NAME_PHONE_NUMBER_NOT_VERIFIED + " = null, "
                                                                            + COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE + " = false, "
                                                                            + COLUMN_NAME_COUNTRY_ID + " = ?, "
                                                                            + COLUMN_NAME_PHONE_NUMBER
                                                                            + " = ? WHERE "
                                                                            + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* TABLE: sns_application related */

    String TABLE_NAME_SNS_APPLICATION = "sns_application";

    String COLUMN_NAME_SNS_PLATFORM = "sns_platform";

    String COLUMN_NAME_SNS_APPLICATION_ARN = "application_arn";

    String COLUMN_NAME_SNS_APPLICATION_LAST_MODIFIED_DATE = "application_last_modified_date"; // milliseconds

    String INDEX_NAME_SNS_APPLICATION_ARN = "index_sns_application_arn";

    /* DDL - TABLE: CL_COMPUTER */

    String SQL_CREATE_TABLE_SNS_APPLICATION = "CREATE TABLE " + TABLE_NAME_SNS_APPLICATION + "("
                                              + COLUMN_NAME_SNS_PLATFORM + " VARCHAR(24) PRIMARY KEY, "
                                              + COLUMN_NAME_SNS_APPLICATION_ARN + " VARCHAR(1024) NOT NULL, "
                                              + COLUMN_NAME_SNS_APPLICATION_LAST_MODIFIED_DATE + " BIGINT DEFAULT 0)";

    String SQL_CREATE_INDEX_SNS_APPLICATION_ARN = "CREATE INDEX " + INDEX_NAME_SNS_APPLICATION_ARN + " ON " + TABLE_NAME_SNS_APPLICATION + "(" + COLUMN_NAME_SNS_APPLICATION_ARN + ")";

    String SQL_CREATE_SNS_APPLICATION = "INSERT INTO " + TABLE_NAME_SNS_APPLICATION + "("
                                        + COLUMN_NAME_SNS_PLATFORM + ", "
                                        + COLUMN_NAME_SNS_APPLICATION_ARN + ", "
                                        + COLUMN_NAME_SNS_APPLICATION_LAST_MODIFIED_DATE + ") VALUES (?, ?, ?)";

    String SQL_FIND_SNS_APPLICATION_BY_PLATFORM = "SELECT "
                                                  + COLUMN_NAME_SNS_PLATFORM + ", "
                                                  + COLUMN_NAME_SNS_APPLICATION_ARN + ", "
                                                  + COLUMN_NAME_SNS_APPLICATION_LAST_MODIFIED_DATE
                                                  + " FROM "
                                                  + TABLE_NAME_SNS_APPLICATION
                                                  + " WHERE "
                                                  + COLUMN_NAME_SNS_PLATFORM + " = ?";

    String SQL_FIND_SNS_APPLICATION_ARN_BY_PLATFORM = "SELECT "
                                                      + COLUMN_NAME_SNS_APPLICATION_ARN
                                                      + " FROM "
                                                      + TABLE_NAME_SNS_APPLICATION
                                                      + " WHERE "
                                                      + COLUMN_NAME_SNS_PLATFORM + " = ?";

    String SQL_FIND_ALL_SNS_APPLICATIONS = "SELECT "
                                           + COLUMN_NAME_SNS_PLATFORM + ", "
                                           + COLUMN_NAME_SNS_APPLICATION_ARN + ", "
                                           + COLUMN_NAME_SNS_APPLICATION_LAST_MODIFIED_DATE
                                           + " FROM "
                                           + TABLE_NAME_SNS_APPLICATION;

    String SQL_DELETE_SNS_APPLICATION_BY_PLATFORM = "DELETE FROM "
                                                    + TABLE_NAME_SNS_APPLICATION
                                                    + " WHERE "
                                                    + COLUMN_NAME_SNS_PLATFORM + " = ?";

    String SQL_UPDATE_SNS_APPLICATION_BY_PLATFORM = "UPDATE "
                                                    + TABLE_NAME_SNS_APPLICATION
                                                    + " SET "
                                                    + COLUMN_NAME_SNS_APPLICATION_ARN + " = ?, "
                                                    + COLUMN_NAME_SNS_APPLICATION_LAST_MODIFIED_DATE
                                                    + " = ? WHERE "
                                                    + COLUMN_NAME_SNS_PLATFORM + " = ?";

//    String TABLE_NAME_SNS_APPLICATION = "sns_application";
//
//    String COLUMN_NAME_SNS_PLATFORM = "sns_platform";
//
//    String COLUMN_NAME_SNS_APPLICATION_ARN = "application_arn";
//
//    String COLUMN_NAME_SNS_APPLICATION_ENABLED = "application_enabled";
//
//    String COLUMN_NAME_SNS_APPLICATION_EXPIRATION_DATE = "application_expiration_date"; // milliseconds
//
//    String COLUMN_NAME_SNS_APPLICATION_LAST_MODIFIED_DATE = "application_last_modified_date"; // milliseconds
//
//    String INDEX_NAME_SNS_APPLICATION_ARN = "index_sns_application_arn";
//
//    /* DDL - TABLE: CL_COMPUTER */
//
//    String SQL_CREATE_TABLE_SNS_APPLICATION = "CREATE TABLE " + TABLE_NAME_SNS_APPLICATION + "("
//                                              + COLUMN_NAME_SNS_PLATFORM + " VARCHAR(24) PRIMARY KEY, "
//                                              + COLUMN_NAME_SNS_APPLICATION_ARN + " VARCHAR(1024) NOT NULL, "
//                                              + COLUMN_NAME_SNS_APPLICATION_ENABLED + " BOOLEAN DEFAULT true, "
//                                              + COLUMN_NAME_SNS_APPLICATION_EXPIRATION_DATE + " BIGINT DEFAULT 0, "
//                                              + COLUMN_NAME_SNS_APPLICATION_LAST_MODIFIED_DATE + " BIGINT DEFAULT 0)";
//
//    String SQL_CREATE_INDEX_SNS_APPLICATION_ARN = "CREATE INDEX " + INDEX_NAME_SNS_APPLICATION_ARN + " ON " + TABLE_NAME_SNS_APPLICATION + "(" + COLUMN_NAME_SNS_APPLICATION_ARN + ")";
//
//    String SQL_CREATE_SNS_APPLICATION = "INSERT INTO " + TABLE_NAME_SNS_APPLICATION + "("
//                                        + COLUMN_NAME_SNS_PLATFORM + ", "
//                                        + COLUMN_NAME_SNS_APPLICATION_ARN + ", "
//                                        + COLUMN_NAME_SNS_APPLICATION_ENABLED + ", "
//                                        + COLUMN_NAME_SNS_APPLICATION_EXPIRATION_DATE + ", "
//                                        + COLUMN_NAME_SNS_APPLICATION_LAST_MODIFIED_DATE + ") VALUES (?, ?, ?, ?)";
//
//    String SQL_FIND_SNS_APPLICATION_BY_PLATFORM = "SELECT "
//                                                  + COLUMN_NAME_SNS_PLATFORM + ", "
//                                                  + COLUMN_NAME_SNS_APPLICATION_ARN + ", "
//                                                  + COLUMN_NAME_SNS_APPLICATION_ENABLED + ", "
//                                                  + COLUMN_NAME_SNS_APPLICATION_EXPIRATION_DATE + ", "
//                                                  + COLUMN_NAME_SNS_APPLICATION_LAST_MODIFIED_DATE
//                                                  + " FROM "
//                                                  + TABLE_NAME_SNS_APPLICATION
//                                                  + " WHERE "
//                                                  + COLUMN_NAME_SNS_PLATFORM + " = ?";
//
//    String SQL_FIND_SNS_APPLICATION_ARN_BY_PLATFORM = "SELECT "
//                                                      + COLUMN_NAME_SNS_APPLICATION_ARN
//                                                      + " FROM "
//                                                      + TABLE_NAME_SNS_APPLICATION
//                                                      + " WHERE "
//                                                      + COLUMN_NAME_SNS_PLATFORM + " = ?";
//
//    String SQL_FIND_ALL_SNS_APPLICATIONS = "SELECT "
//                                           + COLUMN_NAME_SNS_PLATFORM + ", "
//                                           + COLUMN_NAME_SNS_APPLICATION_ARN + ", "
//                                           + COLUMN_NAME_SNS_APPLICATION_ENABLED + ", "
//                                           + COLUMN_NAME_SNS_APPLICATION_EXPIRATION_DATE + ", "
//                                           + COLUMN_NAME_SNS_APPLICATION_LAST_MODIFIED_DATE
//                                           + " FROM "
//                                           + TABLE_NAME_SNS_APPLICATION;
//
//    String SQL_DELETE_SNS_APPLICATION_BY_PLATFORM = "DELETE FROM "
//                                                    + TABLE_NAME_SNS_APPLICATION
//                                                    + " WHERE "
//                                                    + COLUMN_NAME_SNS_PLATFORM + " = ?";
//
//    String SQL_UPDATE_SNS_APPLICATION_BY_PLATFORM = "UPDATE "
//                                                    + TABLE_NAME_SNS_APPLICATION
//                                                    + " SET "
//                                                    + COLUMN_NAME_SNS_APPLICATION_ARN + " = ?, "
//                                                    + COLUMN_NAME_SNS_APPLICATION_ENABLED + " = ?, "
//                                                    + COLUMN_NAME_SNS_APPLICATION_EXPIRATION_DATE + " = ?, "
//                                                    + COLUMN_NAME_SNS_APPLICATION_LAST_MODIFIED_DATE
//                                                    + " = ? WHERE "
//                                                    + COLUMN_NAME_SNS_PLATFORM + " = ?";

    /* TABLE: COMPUTER related */

    String TABLE_NAME_COMPUTER = "cl_computer";

    String COLUMN_NAME_COMPUTER_ID = "computer_id";

    String COLUMN_NAME_COMPUTER_GROUP = "group_name";

    String COLUMN_NAME_COMPUTER_NAME = "computer_name";

    String COLUMN_NAME_RECOVERY_KEY = "recovery_key";

    String INDEX_NAME_COMPUTER_COMPUTER_NAME = "index_c_computer_name";

    String INDEX_NAME_COMPUTER_AUTH_USER_ID = "index_c_computer_auth_user_id";

    String INDEX_NAME_COMPUTER_COMPUTER_GROUP = "index_c_group_name";

    String INDEX_NAME_COMPUTER_RECOVERY_KEY = "index_c_recovery_key";

    /* DDL - TABLE: CL_COMPUTER */

    String SQL_CREATE_TABLE_COMPUTER = "CREATE TABLE " + TABLE_NAME_COMPUTER + "("
                                       + COLUMN_NAME_COMPUTER_ID + " BIGSERIAL PRIMARY KEY, "
                                       + COLUMN_NAME_COMPUTER_GROUP + " VARCHAR(1024) NOT NULL, "
                                       + COLUMN_NAME_COMPUTER_NAME + " VARCHAR(1024) NOT NULL, "
                                       + COLUMN_NAME_RECOVERY_KEY + " VARCHAR(1024) NOT NULL, "
                                       + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) NOT NULL)";

    String SQL_CREATE_FOREIGN_KEY_COMPUTER_AUTH_USER = "ALTER TABLE " + TABLE_NAME_COMPUTER + " ADD FOREIGN KEY (" + COLUMN_NAME_AUTH_USER_ID + ") REFERENCES " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_AUTH_USER_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_INDEX_COMPUTER_AUTH_USER_ID = "CREATE INDEX " + INDEX_NAME_COMPUTER_AUTH_USER_ID + " ON " + TABLE_NAME_COMPUTER + "(" + COLUMN_NAME_AUTH_USER_ID + ")";

    String SQL_CREATE_INDEX_COMPUTER_COMPUTER_GROUP = "CREATE INDEX " + INDEX_NAME_COMPUTER_COMPUTER_GROUP + " ON " + TABLE_NAME_COMPUTER + "(" + COLUMN_NAME_COMPUTER_GROUP + ")";

    String SQL_CREATE_INDEX_COMPUTER_COMPUTER_NAME = "CREATE INDEX " + INDEX_NAME_COMPUTER_COMPUTER_NAME + " ON " + TABLE_NAME_COMPUTER + "(" + COLUMN_NAME_COMPUTER_NAME + ")";

    String SQL_CREATE_INDEX_COMPUTER_RECOVERY_KEY = "CREATE UNIQUE INDEX " + INDEX_NAME_COMPUTER_RECOVERY_KEY + " ON " + TABLE_NAME_COMPUTER + "(" + COLUMN_NAME_RECOVERY_KEY + ")";

    String SQL_CREATE_COMPUTER = "INSERT INTO " + TABLE_NAME_COMPUTER + "("
                                 + COLUMN_NAME_COMPUTER_GROUP + ", "
                                 + COLUMN_NAME_COMPUTER_NAME + ", "
                                 + COLUMN_NAME_RECOVERY_KEY + ", "
                                 + COLUMN_NAME_AUTH_USER_ID + ") VALUES (?, ?, ?, ?)";

    String SQL_FIND_COMPUTER_BY_COMPUTER_ID = "SELECT "
                                              + COLUMN_NAME_COMPUTER_ID + ", "
                                              + COLUMN_NAME_COMPUTER_GROUP + ", "
                                              + COLUMN_NAME_COMPUTER_NAME + ", "
                                              + COLUMN_NAME_RECOVERY_KEY + ", "
                                              + COLUMN_NAME_AUTH_USER_ID
                                              + " FROM "
                                              + TABLE_NAME_COMPUTER
                                              + " WHERE "
                                              + COLUMN_NAME_COMPUTER_ID + " = ?";

    String SQL_FIND_COMPUTER_OWNER_BY_COMPUTER_ID = "SELECT "
                                                    + COLUMN_NAME_AUTH_USER_ID
                                                    + " FROM "
                                                    + TABLE_NAME_COMPUTER
                                                    + " WHERE "
                                                    + COLUMN_NAME_COMPUTER_ID + " = ?";

    String SQL_FIND_COMPUTER_BY_RECOVERY_KEY = "SELECT "
                                               + COLUMN_NAME_COMPUTER_ID + ", "
                                               + COLUMN_NAME_COMPUTER_GROUP + ", "
                                               + COLUMN_NAME_COMPUTER_NAME + ", "
                                               + COLUMN_NAME_RECOVERY_KEY + ", "
                                               + COLUMN_NAME_AUTH_USER_ID
                                               + " FROM "
                                               + TABLE_NAME_COMPUTER
                                               + " WHERE "
                                               + COLUMN_NAME_RECOVERY_KEY + " = ?";

    String SQL_FIND_COMPUTER_NAME_BY_COMPUTER_ID = "SELECT "
                                                   + COLUMN_NAME_COMPUTER_NAME
                                                   + " FROM "
                                                   + TABLE_NAME_COMPUTER
                                                   + " WHERE "
                                                   + COLUMN_NAME_COMPUTER_ID + " = ?";

    String SQL_FIND_COMPUTERS_BY_USER_ID = "SELECT "
                                           + COLUMN_NAME_COMPUTER_ID + ", "
                                           + COLUMN_NAME_COMPUTER_GROUP + ", "
                                           + COLUMN_NAME_COMPUTER_NAME + ", "
                                           + COLUMN_NAME_RECOVERY_KEY + ", "
                                           + COLUMN_NAME_AUTH_USER_ID
                                           + " FROM "
                                           + TABLE_NAME_COMPUTER
                                           + " WHERE "
                                           + COLUMN_NAME_AUTH_USER_ID + " = ?";

    String SQL_FIND_COMPUTER_BY_GROUP_NAME_AND_COMPUTER_NAME = "SELECT "
                                                               + COLUMN_NAME_COMPUTER_ID + ", "
                                                               + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                               + COLUMN_NAME_COMPUTER_NAME + ", "
                                                               + COLUMN_NAME_RECOVERY_KEY + ", "
                                                               + COLUMN_NAME_AUTH_USER_ID
                                                               + " FROM "
                                                               + TABLE_NAME_COMPUTER
                                                               + " WHERE lower("
                                                               + COLUMN_NAME_COMPUTER_GROUP
                                                               + ") = lower(?) AND lower("
                                                               + COLUMN_NAME_COMPUTER_NAME + ") = lower(?)";

    String SQL_FIND_COMPUTER_BY_GROUP_NAME_AND_COMPUTER_NAME_FOR_USER = "SELECT "
                                                                        + COLUMN_NAME_COMPUTER_ID + ", "
                                                                        + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                                        + COLUMN_NAME_COMPUTER_NAME + ", "
                                                                        + COLUMN_NAME_RECOVERY_KEY + ", "
                                                                        + COLUMN_NAME_AUTH_USER_ID
                                                                        + " FROM "
                                                                        + TABLE_NAME_COMPUTER
                                                                        + " WHERE lower("
                                                                        + COLUMN_NAME_COMPUTER_GROUP
                                                                        + ") = lower(?) AND lower("
                                                                        + COLUMN_NAME_COMPUTER_NAME
                                                                        + ") = lower(?) AND "
                                                                        + COLUMN_NAME_AUTH_USER_ID + " = ?";

    String SQL_FIND_ALL_COMPUTERS = "SELECT "
                                    + COLUMN_NAME_COMPUTER_ID + ", "
                                    + COLUMN_NAME_COMPUTER_GROUP + ", "
                                    + COLUMN_NAME_COMPUTER_NAME + ", "
                                    + COLUMN_NAME_RECOVERY_KEY + ", "
                                    + COLUMN_NAME_AUTH_USER_ID
                                    + " FROM "
                                    + TABLE_NAME_COMPUTER
                                    + " ORDER BY "
                                    + COLUMN_NAME_COMPUTER_GROUP + " ASC, "
                                    + COLUMN_NAME_COMPUTER_NAME + " ASC";

    String SQL_FIND_ALL_COMPUTER_NAMES_BY_USER_ID = "SELECT "
                                                    + COLUMN_NAME_COMPUTER_NAME
                                                    + " FROM "
                                                    + TABLE_NAME_COMPUTER
                                                    + " WHERE "
                                                    + COLUMN_NAME_AUTH_USER_ID
                                                    + " = ? ORDER BY "
                                                    + COLUMN_NAME_COMPUTER_NAME + " ASC";

    String SQL_FIND_ALL_COMPUTER_NAMES_WITH_LOWER_CASE_BY_USER_ID = "SELECT lower("
                                                                    + COLUMN_NAME_COMPUTER_NAME
                                                                    + ") FROM "
                                                                    + TABLE_NAME_COMPUTER
                                                                    + " WHERE "
                                                                    + COLUMN_NAME_AUTH_USER_ID
                                                                    + " = ? ORDER BY "
                                                                    + COLUMN_NAME_COMPUTER_NAME + " ASC";

    String SQL_DELETE_COMPUTER_BY_ID_CASCADE = "DELETE FROM "
                                               + TABLE_NAME_COMPUTER
                                               + " WHERE "
                                               + COLUMN_NAME_COMPUTER_ID + " = ?";

    String SQL_UPDATE_COMPUTER_BY_ID = "UPDATE "
                                       + TABLE_NAME_COMPUTER
                                       + " SET "
                                       + COLUMN_NAME_COMPUTER_GROUP + " = ?, "
                                       + COLUMN_NAME_COMPUTER_NAME + " = ?, "
                                       + COLUMN_NAME_AUTH_USER_ID
                                       + " = ? WHERE "
                                       + COLUMN_NAME_COMPUTER_ID + " = ?";


    String SQL_UPDATE_COMPUTER_NAME = "UPDATE "
                                      + TABLE_NAME_COMPUTER
                                      + " SET "
                                      + COLUMN_NAME_COMPUTER_GROUP + " = ?, "
                                      + COLUMN_NAME_COMPUTER_NAME
                                      + " = ? WHERE "
                                      + COLUMN_NAME_COMPUTER_ID + " = ?";

    // TABLE: FILE_UPLOAD_GROUP

    String TABLE_NAME_FILE_UPLOAD_GROUP = "file_upload_group";

    String COLUMN_NAME_UPLOAD_GROUP_ID = "upload_group_id";

    String COLUMN_NAME_UPLOAD_GROUP_DIRECTORY = "upload_group_directory";

    String COLUMN_NAME_UPLOAD_SUBDIRECTORY_TYPE = "subdirectory_type";

    String COLUMN_NAME_UPLOAD_SUBDIRECTORY_VALUE = "subdirectory_name";

    String COLUMN_NAME_UPLOAD_DESCRIPTION_TYPE = "description_type";

    String COLUMN_NAME_UPLOAD_DESCRIPTION_VALUE = "description_value";

    String COLUMN_NAME_UPLOAD_NOTIFICATION_TYPE = "notification_type";

    String COLUMN_NAME_CREATED_TIMESTAMP = "created_timestamp";

    String COLUMN_NAME_CREATED_IN_DESKTOP_TIMESTAMP = "created_in_desktop_timestamp";

    String COLUMN_NAME_CREATED_IN_DESKTOP_STATUS = "created_in_desktop_status";

    String CREATED_IN_DESKTOP_STATUS_SUCCESS = "success";

    String CREATED_IN_DESKTOP_STATUS_FAILURE = "failure";

    String INDEX_NAME_FILE_UPLOAD_GROUP_CREATED_IN_DESKTOP_TIMESTAMP = "index_ug_created_in_desktop_timestamp";

    String INDEX_NAME_FILE_UPLOAD_GROUP_CREATED_TIMESTAMP = "index_ug_created_timestamp";

    // DDL - TABLE: FILE_UPLOAD_GROUP

    String SQL_CREATE_TABLE_FILE_UPLOAD_GROUP = "CREATE TABLE " + TABLE_NAME_FILE_UPLOAD_GROUP + "("
                                                + COLUMN_NAME_UPLOAD_GROUP_ID + " VARCHAR(1024) PRIMARY KEY, "
                                                + COLUMN_NAME_UPLOAD_GROUP_DIRECTORY + " VARCHAR(4096) NOT NULL, "
                                                + COLUMN_NAME_UPLOAD_SUBDIRECTORY_TYPE + " INTEGER NOT NULL, "
                                                + COLUMN_NAME_UPLOAD_DESCRIPTION_TYPE + " INTEGER NOT NULL, "
                                                + COLUMN_NAME_UPLOAD_NOTIFICATION_TYPE + " INTEGER NOT NULL, "
                                                + COLUMN_NAME_UPLOAD_SUBDIRECTORY_VALUE + " VARCHAR(1024) NULL, "
                                                + COLUMN_NAME_UPLOAD_DESCRIPTION_VALUE + " TEXT NULL, "
                                                + COLUMN_NAME_CREATED_TIMESTAMP + " BIGINT DEFAULT 0, "
                                                + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) NOT NULL, " // When computer deleted, the value will set to null
                                                + COLUMN_NAME_COMPUTER_ID + " BIGINT NULL, "
                                                + COLUMN_NAME_CREATED_IN_DESKTOP_TIMESTAMP + " BIGINT DEFAULT 0, "
                                                + COLUMN_NAME_CREATED_IN_DESKTOP_STATUS + " VARCHAR(1024) NULL)";

    String SQL_CREATE_FOREIGN_KEY_FILE_UPLOAD_GROUP_COMPUTER_ID = "ALTER TABLE " + TABLE_NAME_FILE_UPLOAD_GROUP + " ADD FOREIGN KEY (" + COLUMN_NAME_COMPUTER_ID + ") REFERENCES " + TABLE_NAME_COMPUTER + "(" + COLUMN_NAME_COMPUTER_ID + ") ON DELETE SET NULL";

    String SQL_CREATE_FOREIGN_KEY_FILE_UPLOAD_GROUP_AUTH_USER = "ALTER TABLE " + TABLE_NAME_FILE_UPLOAD_GROUP + " ADD FOREIGN KEY (" + COLUMN_NAME_AUTH_USER_ID + ") REFERENCES " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_AUTH_USER_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_INDEX_FILE_UPLOAD_GROUP_CREATED_IN_DESKTOP_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_FILE_UPLOAD_GROUP_CREATED_IN_DESKTOP_TIMESTAMP + " ON " + TABLE_NAME_FILE_UPLOAD_GROUP + "(" + COLUMN_NAME_CREATED_IN_DESKTOP_TIMESTAMP + ")";

    String SQL_CREATE_INDEX_FILE_UPLOAD_GROUP_CREATED_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_FILE_UPLOAD_GROUP_CREATED_TIMESTAMP + " ON " + TABLE_NAME_FILE_UPLOAD_GROUP + "(" + COLUMN_NAME_CREATED_TIMESTAMP + ")";

    String SQL_CREATE_FILE_UPLOAD_GROUP = "INSERT INTO " + TABLE_NAME_FILE_UPLOAD_GROUP + "("
                                          + COLUMN_NAME_UPLOAD_GROUP_ID + ", "
                                          + COLUMN_NAME_UPLOAD_GROUP_DIRECTORY + ", "
                                          + COLUMN_NAME_UPLOAD_SUBDIRECTORY_TYPE + ", "
                                          + COLUMN_NAME_UPLOAD_DESCRIPTION_TYPE + ", "
                                          + COLUMN_NAME_UPLOAD_NOTIFICATION_TYPE + ", "
                                          + COLUMN_NAME_UPLOAD_SUBDIRECTORY_VALUE + ", "
                                          + COLUMN_NAME_UPLOAD_DESCRIPTION_VALUE + ", "
                                          + COLUMN_NAME_CREATED_TIMESTAMP + ", "
                                          + COLUMN_NAME_AUTH_USER_ID + ", "
                                          + COLUMN_NAME_COMPUTER_ID + ", "
                                          + COLUMN_NAME_CREATED_IN_DESKTOP_TIMESTAMP + ", "
                                          + COLUMN_NAME_CREATED_IN_DESKTOP_STATUS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // Find by group-id
    String SQL_FIND_FILE_UPLOAD_GROUP_BY_ID = "SELECT "
                                              + COLUMN_NAME_UPLOAD_GROUP_ID + ", "
                                              + COLUMN_NAME_UPLOAD_GROUP_DIRECTORY + ", "
                                              + COLUMN_NAME_UPLOAD_SUBDIRECTORY_TYPE + ", "
                                              + COLUMN_NAME_UPLOAD_DESCRIPTION_TYPE + ", "
                                              + COLUMN_NAME_UPLOAD_NOTIFICATION_TYPE + ", "
                                              + COLUMN_NAME_UPLOAD_SUBDIRECTORY_VALUE + ", "
                                              + COLUMN_NAME_UPLOAD_DESCRIPTION_VALUE + ", "
                                              + COLUMN_NAME_AUTH_USER_ID + ", "
                                              + COLUMN_NAME_COMPUTER_ID + ", "
                                              + COLUMN_NAME_CREATED_IN_DESKTOP_TIMESTAMP + ", "
                                              + COLUMN_NAME_CREATED_IN_DESKTOP_STATUS
                                              + " FROM " + TABLE_NAME_FILE_UPLOAD_GROUP
                                              + " WHERE " + COLUMN_NAME_UPLOAD_GROUP_ID + " = ?";

    // Find if exists by group-id
    String SQL_FIND_IF_EXISTS_FILE_UPLOAD_GROUP_BY_ID = "SELECT "
                                                        + COLUMN_NAME_UPLOAD_GROUP_ID
                                                        + " FROM " + TABLE_NAME_FILE_UPLOAD_GROUP
                                                        + " WHERE " + COLUMN_NAME_UPLOAD_GROUP_ID + " = ?";

    // Find not-created upload group by user, computer-id
    String SQL_FIND_FILE_NOT_CREATED_UPLOAD_GROUP_BY_USER_COMPUTER = "SELECT "
                                                                     + COLUMN_NAME_UPLOAD_GROUP_ID + ", "
                                                                     + COLUMN_NAME_UPLOAD_GROUP_DIRECTORY + ", "
                                                                     + COLUMN_NAME_UPLOAD_SUBDIRECTORY_TYPE + ", "
                                                                     + COLUMN_NAME_UPLOAD_DESCRIPTION_TYPE + ", "
                                                                     + COLUMN_NAME_UPLOAD_NOTIFICATION_TYPE + ", "
                                                                     + COLUMN_NAME_UPLOAD_SUBDIRECTORY_VALUE + ", "
                                                                     + COLUMN_NAME_UPLOAD_DESCRIPTION_VALUE + ", "
                                                                     + COLUMN_NAME_AUTH_USER_ID + ", "
                                                                     + COLUMN_NAME_COMPUTER_ID + ", "
                                                                     + COLUMN_NAME_CREATED_IN_DESKTOP_TIMESTAMP + ", "
                                                                     + COLUMN_NAME_CREATED_IN_DESKTOP_STATUS
                                                                     + " FROM " + TABLE_NAME_FILE_UPLOAD_GROUP
                                                                     + " WHERE " + COLUMN_NAME_AUTH_USER_ID
                                                                     + " = ? AND " + COLUMN_NAME_COMPUTER_ID
                                                                     + " = ? AND " + COLUMN_NAME_CREATED_IN_DESKTOP_TIMESTAMP + " < 1";

    // Update created-in-desktop timestamp
    String SQL_UPDATE_FILE_UPLOAD_GROUP_CREATED_IN_DESKTOP_STATUS = "UPDATE "
                                                                    + TABLE_NAME_FILE_UPLOAD_GROUP
                                                                    + " SET "
                                                                    + COLUMN_NAME_CREATED_IN_DESKTOP_TIMESTAMP + " = ?, "
                                                                    + COLUMN_NAME_CREATED_IN_DESKTOP_STATUS
                                                                    + " = ? WHERE "
                                                                    + COLUMN_NAME_UPLOAD_GROUP_ID + " = ?";

    // Delete by upload-group-id
    String SQL_DELETE_FILE_UPLOAD_GROUP_BY_UPLOAD_GROUP_ID = "DELETE FROM "
                                                             + TABLE_NAME_FILE_UPLOAD_GROUP
                                                             + " WHERE "
                                                             + COLUMN_NAME_UPLOAD_GROUP_ID + " = ?";


    // TABLE: FILE_UPLOAD_GROUP_DETAIL: relation with upload transfer keys (one-to-many)

    String TABLE_NAME_FILE_UPLOAD_GROUP_DETAIL = "file_upload_group_detail";

    String COLUMN_NAME_FILE_UPLOAD_GROUP_DETAIL_ID = "file_upload_group_detail_id";

    String COLUMN_NAME_UPLOAD_KEY = "upload_key";

    String INDEX_NAME_FILE_UPLOAD_GROUP_DETAIL_GROUP_ID = "index_ugd_group_id";

    String INDEX_NAME_FILE_UPLOAD_GROUP_DETAIL_UPLOAD_KEY = "index_ugd_upload_key";

    // DO NOT set FK to transfer key because when creating the records, the upload file record is not created.

    // DDL - TABLE: FILE_UPLOAD_GROUP_DETAIL

    String SQL_CREATE_TABLE_FILE_UPLOAD_GROUP_DETAIL = "CREATE TABLE " + TABLE_NAME_FILE_UPLOAD_GROUP_DETAIL + "("
                                                       + COLUMN_NAME_FILE_UPLOAD_GROUP_DETAIL_ID + " VARCHAR(2049) PRIMARY KEY, " // 2049: group_id+transfer_key
                                                       + COLUMN_NAME_UPLOAD_GROUP_ID + " VARCHAR(1024) NOT NULL, "
                                                       + COLUMN_NAME_UPLOAD_KEY + " VARCHAR(1024) NOT NULL)";

    String SQL_CREATE_FOREIGN_KEY_FILE_UPLOAD_GROUP_DETAIL_UPLOAD_GROUP_ID = "ALTER TABLE " + TABLE_NAME_FILE_UPLOAD_GROUP_DETAIL + " ADD FOREIGN KEY (" + COLUMN_NAME_UPLOAD_GROUP_ID + ") REFERENCES " + TABLE_NAME_FILE_UPLOAD_GROUP + "(" + COLUMN_NAME_UPLOAD_GROUP_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_INDEX_FILE_UPLOAD_GROUP_DETAIL_GROUP_ID = "CREATE INDEX " + INDEX_NAME_FILE_UPLOAD_GROUP_DETAIL_GROUP_ID + " ON " + TABLE_NAME_FILE_UPLOAD_GROUP_DETAIL + "(" + COLUMN_NAME_FILE_UPLOAD_GROUP_DETAIL_ID + ")";

    String SQL_CREATE_INDEX_FILE_UPLOAD_GROUP_DETAIL_UPLOAD_KEY = "CREATE INDEX " + INDEX_NAME_FILE_UPLOAD_GROUP_DETAIL_UPLOAD_KEY + " ON " + TABLE_NAME_FILE_UPLOAD_GROUP_DETAIL + "(" + COLUMN_NAME_UPLOAD_KEY + ")";

    String SQL_CREATE_FILE_UPLOAD_GROUP_DETAIL = "INSERT INTO " + TABLE_NAME_FILE_UPLOAD_GROUP_DETAIL + "("
                                                 + COLUMN_NAME_FILE_UPLOAD_GROUP_DETAIL_ID + ", "
                                                 + COLUMN_NAME_UPLOAD_GROUP_ID + ", "
                                                 + COLUMN_NAME_UPLOAD_KEY + ") VALUES (?, ?, ?)";

    // Find by upload-group
    String SQL_FIND_FILE_UPLOAD_GROUP_DETAIL_BY_GROUP_ID = "SELECT "
                                                           + COLUMN_NAME_FILE_UPLOAD_GROUP_DETAIL_ID + ", "
                                                           + COLUMN_NAME_UPLOAD_GROUP_ID + ", "
                                                           + COLUMN_NAME_UPLOAD_KEY
                                                           + " FROM " + TABLE_NAME_FILE_UPLOAD_GROUP_DETAIL
                                                           + " WHERE " + COLUMN_NAME_UPLOAD_GROUP_ID + " = ?";

    // Find by upload KEY
    String SQL_FIND_FILE_UPLOAD_GROUP_DETAIL_BY_UPLOAD_KEY = "SELECT "
                                                           + COLUMN_NAME_FILE_UPLOAD_GROUP_DETAIL_ID + ", "
                                                           + COLUMN_NAME_UPLOAD_GROUP_ID + ", "
                                                           + COLUMN_NAME_UPLOAD_KEY
                                                           + " FROM " + TABLE_NAME_FILE_UPLOAD_GROUP_DETAIL
                                                           + " WHERE " + COLUMN_NAME_UPLOAD_KEY + " = ?";

    // Find if exists by upload KEY
    String SQL_FIND_IF_EXISTS_FILE_UPLOAD_GROUP_DETAIL_BY_UPLOAD_KEY = "SELECT "
                                                                       + COLUMN_NAME_FILE_UPLOAD_GROUP_DETAIL_ID
                                                                       + " FROM " + TABLE_NAME_FILE_UPLOAD_GROUP_DETAIL
                                                                       + " WHERE " + COLUMN_NAME_UPLOAD_KEY + " = ?";

    // Find by upload-group-detail-id
    String SQL_FIND_FILE_UPLOAD_GROUP_DETAIL_BY_GROUP_DETAIL_ID = "SELECT "
                                                                  + COLUMN_NAME_FILE_UPLOAD_GROUP_DETAIL_ID + ", "
                                                                  + COLUMN_NAME_UPLOAD_GROUP_ID + ", "
                                                                  + COLUMN_NAME_UPLOAD_KEY
                                                                  + " FROM " + TABLE_NAME_FILE_UPLOAD_GROUP_DETAIL
                                                                  + " WHERE " + COLUMN_NAME_FILE_UPLOAD_GROUP_DETAIL_ID + " = ?";

    // Delete by upload-key
    String SQL_DELETE_FILE_UPLOAD_GROUP_DETAIL_BY_UPLOAD_KEY = "DELETE FROM "
                                                               + TABLE_NAME_FILE_UPLOAD_GROUP_DETAIL
                                                               + " WHERE "
                                                               + COLUMN_NAME_UPLOAD_KEY + " = ?";


    /* TABLE: FILE_UPLOADED related */

    String TABLE_NAME_FILE_UPLOADED = "file_uploaded";

    String COLUMN_NAME_FILENAME = "filename";

    String COLUMN_NAME_DIRECTORY = "directory";

    String COLUMN_NAME_FILE_SIZE = "file_size";

    String COLUMN_NAME_START_TIMESTAMP = "start_timestamp";

    String COLUMN_NAME_END_TIMESTAMP = "end_timestamp";

    String COLUMN_NAME_STATUS = "status";

    String COLUMN_NAME_FROM_IP = "from_ip";

    String COLUMN_NAME_FROM_HOST = "from_host";

    String COLUMN_NAME_TO_IP = "to_ip";

    String COLUMN_NAME_TO_HOST = "to_host";

    // path of the temp file
    String COLUMN_NAME_TMP_FILE = "tmp_file";

    String COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP = "tmp_file_created_timestamp";

    String COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP = "tmp_file_deleted_timestamp";

    String COLUMN_NAME_TRANSFERRED_BYTE_INDEX = "transferred_byte_index";

    String COLUMN_NAME_SOURCE_FILE_LAST_MODIFIED = "source_file_last_modified";

    String INDEX_NAME_FILE_UPLOADED_END_TIMESTAMP = "index_d_file_uploaded_end_timestamp";

    String INDEX_NAME_FILE_UPLOADED_UPLOAD_GROUP_ID = "index_d_file_uploaded_upload_group_id";

    String INDEX_NAME_FILE_UPLOADED_TMP_FILE_CREATED_TIMESTAMP = "index_d_file_uplaoded_tmp_file_created_timestamp";

    String INDEX_NAME_FILE_UPLOADED_TMP_FILE_DELETED_TIMESTAMP = "index_d_file_uplaoded_tmp_file_deleted_timestamp";

    String INDEX_NAME_FILE_UPLOADED_FROM_IP = "index_d_file_uploaded_from_ip";

    String INDEX_NAME_FILE_UPLOADED_FROM_HOST = "index_d_file_uploaded_from_host";

    String INDEX_NAME_FILE_UPLOADED_TO_IP = "index_d_file_uploaded_to_ip";

    String INDEX_NAME_FILE_UPLOADED_TO_HOST = "index_d_file_uploaded_to_host";

    /* The desktop app has already uploaded the file to the server
     * but the device does not confirm this yet.
     */
    String TRANSFER_STATUS_DESKTOP_UPLOADED_BUT_UNCONFIRMED = "desktop_uploaded_but_unconfirmed";

    /* The device app has already uploaded the file to the server
     * but the desktop does not confirm this yet.
     */
    String TRANSFER_STATUS_DEVICE_UPLOADED_BUT_UNCONFIRMED = "device_uploaded_but_unconfirmed";

    String TRANSFER_STATUS_PROCESSING = "processing";

    String TRANSFER_STATUS_SUCCESS = "success";

    String TRANSFER_STATUS_FAILURE = "failure";

    String TRANSFER_STATUS_NOT_FOUND = "not_found";


    /* DDL - TABLE: FILE_UPLOADED */

    String SQL_CREATE_TABLE_FILE_UPLOADED = "CREATE TABLE " + TABLE_NAME_FILE_UPLOADED + "("
                                            + COLUMN_NAME_UPLOAD_KEY + " VARCHAR(1024) PRIMARY KEY, "
                                            + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) NOT NULL, "
                                            + COLUMN_NAME_COMPUTER_ID + " BIGINT NULL, "              // When computer deleted, the value will set to null
                                            + COLUMN_NAME_COMPUTER_GROUP + " VARCHAR(1024) NOT NULL, "
                                            + COLUMN_NAME_COMPUTER_NAME + " VARCHAR(1024) NOT NULL, "
                                            + COLUMN_NAME_FILENAME + " VARCHAR(1024) NOT NULL, "
                                            + COLUMN_NAME_DIRECTORY + " VARCHAR(1024) NOT NULL, "
                                            + COLUMN_NAME_FILE_SIZE + " NUMERIC(15, 0) DEFAULT 0, "   // NUMERIC(15, 0) ~ 900 TB Capacity at most
                                            + COLUMN_NAME_UPLOAD_GROUP_ID + " VARCHAR(1024) NULL, "   // When upload group deleted, the value will set to null
                                            + COLUMN_NAME_START_TIMESTAMP + " BIGINT DEFAULT 0, "
                                            + COLUMN_NAME_END_TIMESTAMP + " BIGINT DEFAULT 0, "
                                            + COLUMN_NAME_STATUS + " VARCHAR(1024), "
                                            + COLUMN_NAME_TMP_FILE + " VARCHAR(1024) NULL, "
                                            + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP + " BIGINT DEFAULT 0, "
                                            + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP + " BIGINT DEFAULT 0, "
                                            + COLUMN_NAME_TRANSFERRED_BYTE_INDEX + " BIGINT NULL DEFAULT -1, "
                                            + COLUMN_NAME_SOURCE_FILE_LAST_MODIFIED + " BIGINT NULL DEFAULT 0, "
                                            + COLUMN_NAME_FROM_IP + " VARCHAR(1024) NULL, "
                                            + COLUMN_NAME_FROM_HOST + " VARCHAR(1024) NULL, "
                                            + COLUMN_NAME_TO_IP + " VARCHAR(1024) NULL, "
                                            + COLUMN_NAME_TO_HOST + " VARCHAR(1024) NULL)";

    String SQL_CREATE_INDEX_FILE_UPLOADED_END_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_FILE_UPLOADED_END_TIMESTAMP + " ON " + TABLE_NAME_FILE_UPLOADED + "(" + COLUMN_NAME_END_TIMESTAMP + ")";

    String SQL_CREATE_FOREIGN_KEY_FILE_UPLOADED_COMPUTER_ID = "ALTER TABLE " + TABLE_NAME_FILE_UPLOADED + " ADD FOREIGN KEY (" + COLUMN_NAME_COMPUTER_ID + ") REFERENCES " + TABLE_NAME_COMPUTER + "(" + COLUMN_NAME_COMPUTER_ID + ") ON DELETE SET NULL";

    String SQL_CREATE_FOREIGN_KEY_FILE_UPLOADED_AUTH_USER = "ALTER TABLE " + TABLE_NAME_FILE_UPLOADED + " ADD FOREIGN KEY (" + COLUMN_NAME_AUTH_USER_ID + ") REFERENCES " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_AUTH_USER_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_INDEX_FILE_UPLOADED_TMP_FILE_CREATED_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_FILE_UPLOADED_TMP_FILE_CREATED_TIMESTAMP + " ON " + TABLE_NAME_FILE_UPLOADED + "(" + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP + ")";

    String SQL_CREATE_INDEX_FILE_UPLOADED_TMP_FILE_DELETED_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_FILE_UPLOADED_TMP_FILE_DELETED_TIMESTAMP + " ON " + TABLE_NAME_FILE_UPLOADED + "(" + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP + ")";

    String SQL_CREATE_INDEX_FILE_UPLOADED_UPLOAD_GROUP_ID = "CREATE INDEX " + INDEX_NAME_FILE_UPLOADED_UPLOAD_GROUP_ID + " ON " + TABLE_NAME_FILE_UPLOADED + "(" + COLUMN_NAME_UPLOAD_GROUP_ID + ")";

    String SQL_CREATE_FOREIGN_KEY_FILE_UPLOADED_UPLOAD_GROUP_ID = "ALTER TABLE " + TABLE_NAME_FILE_UPLOADED + " ADD FOREIGN KEY (" + COLUMN_NAME_UPLOAD_GROUP_ID + ") REFERENCES " + TABLE_NAME_FILE_UPLOAD_GROUP + "(" + COLUMN_NAME_UPLOAD_GROUP_ID + ") ON DELETE SET NULL";

    String SQL_CREATE_INDEX_FILE_UPLOADED_FROM_IP = "CREATE INDEX " + INDEX_NAME_FILE_UPLOADED_FROM_IP + " ON " + TABLE_NAME_FILE_UPLOADED + "(" + COLUMN_NAME_FROM_IP + ")";

    String SQL_CREATE_INDEX_FILE_UPLOADED_FROM_HOST = "CREATE INDEX " + INDEX_NAME_FILE_UPLOADED_FROM_HOST + " ON " + TABLE_NAME_FILE_UPLOADED + "(" + COLUMN_NAME_FROM_HOST + ")";

    String SQL_CREATE_INDEX_FILE_UPLOADED_TO_IP = "CREATE INDEX " + INDEX_NAME_FILE_UPLOADED_TO_IP + " ON " + TABLE_NAME_FILE_UPLOADED + "(" + COLUMN_NAME_TO_IP + ")";

    String SQL_CREATE_INDEX_FILE_UPLOADED_TO_HOST = "CREATE INDEX " + INDEX_NAME_FILE_UPLOADED_TO_HOST + " ON " + TABLE_NAME_FILE_UPLOADED + "(" + COLUMN_NAME_TO_HOST + ")";


    /* find a uploaded-file by uploadKey */
    String SQL_FIND_FILE_UPLOADED_BY_UPLOAD_KEY = "SELECT "
                                                  + COLUMN_NAME_UPLOAD_KEY + ", "
                                                  + COLUMN_NAME_AUTH_USER_ID + ", "
                                                  + COLUMN_NAME_COMPUTER_ID + ", "
                                                  + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                  + COLUMN_NAME_COMPUTER_NAME + ", "
                                                  + COLUMN_NAME_FILENAME + ", "
                                                  + COLUMN_NAME_DIRECTORY + ", "
                                                  + COLUMN_NAME_FILE_SIZE + ", "
                                                  + COLUMN_NAME_UPLOAD_GROUP_ID + ", "
                                                  + COLUMN_NAME_START_TIMESTAMP + ", "
                                                  + COLUMN_NAME_END_TIMESTAMP + ", "
                                                  + COLUMN_NAME_STATUS + ", "
                                                  + COLUMN_NAME_TMP_FILE + ", "
                                                  + COLUMN_NAME_TRANSFERRED_BYTE_INDEX + ", "
                                                  + COLUMN_NAME_SOURCE_FILE_LAST_MODIFIED
                                                  + " FROM " + TABLE_NAME_FILE_UPLOADED
                                                  + " WHERE " + COLUMN_NAME_UPLOAD_KEY + " = ?";

    String SQL_FIND_FILE_UPLOADED_TMP_FILE_BY_UPLOAD_KEY = "SELECT "
                                                           + COLUMN_NAME_TMP_FILE
                                                           + " FROM " + TABLE_NAME_FILE_UPLOADED
                                                           + " WHERE " + COLUMN_NAME_UPLOAD_KEY + " = ?";

    // find if a uploaded-file exists by uploadKey
    String SQL_FIND_IF_EXISTS_FILE_UPLOADED_BY_UPLOAD_KEY = "SELECT "
                                                            + COLUMN_NAME_UPLOAD_KEY
                                                            + " FROM " + TABLE_NAME_FILE_UPLOADED
                                                            + " WHERE " + COLUMN_NAME_UPLOAD_KEY + " = ?";

    // find if a uploaded-file exists by upload group id
    String SQL_FIND_IF_EXISTS_FILE_UPLOADED_BY_UPLOAD_GROUP_ID = "SELECT "
                                                                 + COLUMN_NAME_UPLOAD_KEY
                                                                 + " FROM " + TABLE_NAME_FILE_UPLOADED
                                                                 + " WHERE " + COLUMN_NAME_UPLOAD_GROUP_ID + " = ?";

    /* create FILE_UPLOADED */
    String SQL_CREATE_FILE_UPLOADED = "INSERT INTO " + TABLE_NAME_FILE_UPLOADED + "("
                                      + COLUMN_NAME_UPLOAD_KEY + ", "
                                      + COLUMN_NAME_AUTH_USER_ID + ", "
                                      + COLUMN_NAME_COMPUTER_ID + ", "
                                      + COLUMN_NAME_COMPUTER_GROUP + ", "
                                      + COLUMN_NAME_COMPUTER_NAME + ", "
                                      + COLUMN_NAME_FILENAME + ", "
                                      + COLUMN_NAME_DIRECTORY + ", "
                                      + COLUMN_NAME_FILE_SIZE + ", "
                                      + COLUMN_NAME_UPLOAD_GROUP_ID + ", "
                                      + COLUMN_NAME_START_TIMESTAMP + ", "
                                      + COLUMN_NAME_END_TIMESTAMP + ", "
                                      + COLUMN_NAME_STATUS + ", "
                                      + COLUMN_NAME_TMP_FILE + ", "
                                      + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP + ", "
                                      + COLUMN_NAME_TRANSFERRED_BYTE_INDEX + ", "
                                      + COLUMN_NAME_SOURCE_FILE_LAST_MODIFIED + ", "
                                      + COLUMN_NAME_FROM_IP + ", "
                                      + COLUMN_NAME_FROM_HOST + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /* update file upload by uploadKey for column end timestamp and status */
    String SQL_UPDATE_FILE_UPLOADED_WITHOUT_FILE_SIZE = "UPDATE "
                                                        + TABLE_NAME_FILE_UPLOADED
                                                        + " SET "
                                                        + COLUMN_NAME_STATUS + " = ?, "
                                                        + COLUMN_NAME_END_TIMESTAMP
                                                        + " = ? WHERE "
                                                        + COLUMN_NAME_UPLOAD_KEY + " = ?";

    String SQL_UPDATE_FILE_UPLOADED_TMP_FILE_DELETED_TIMESTAMP = "UPDATE "
                                                                 + TABLE_NAME_FILE_UPLOADED
                                                                 + " SET "
                                                                 + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP
                                                                 + " = ? WHERE "
                                                                 + COLUMN_NAME_UPLOAD_KEY + " = ?";

    /* upload uploaded-file status from processing to device_success by upload key */
    String SQL_UPDATE_FILE_UPLOADED_STATUS_PROCESSING_TO_DEVICE_UPLOADED_BUT_UNCONFIRMED = "UPDATE "
                                                                                           + TABLE_NAME_FILE_UPLOADED
                                                                                           + " SET "
                                                                                           + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_DEVICE_UPLOADED_BUT_UNCONFIRMED
                                                                                           + "' WHERE "
                                                                                           + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_PROCESSING
                                                                                           + "' AND " + COLUMN_NAME_UPLOAD_KEY + " = ?";

    // update upload-files with to ip and hostname
    String SQL_UPDATE_FILE_UPLOADED_TO_IP_HOST = "UPDATE "
                                                 + TABLE_NAME_FILE_UPLOADED
                                                 + " SET "
                                                 + COLUMN_NAME_TO_IP + " = ?, "
                                                 + COLUMN_NAME_TO_HOST
                                                 + " = ? WHERE "
                                                 + COLUMN_NAME_UPLOAD_KEY + " = ?";

    /* find upload-files by user, order by start timestamp desc */
    String SQL_FIND_ALL_FILE_UPLOADED_BY_USER = "SELECT " + COLUMN_NAME_UPLOAD_KEY + ", "
                                                + COLUMN_NAME_AUTH_USER_ID + ", "
                                                + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                + COLUMN_NAME_COMPUTER_NAME + ", "
                                                + COLUMN_NAME_FILENAME + ", "
                                                + COLUMN_NAME_DIRECTORY + ", "
                                                + COLUMN_NAME_FILE_SIZE + ", "
                                                + COLUMN_NAME_UPLOAD_GROUP_ID + ", "
                                                + COLUMN_NAME_START_TIMESTAMP + ", "
                                                + COLUMN_NAME_END_TIMESTAMP + ", "
                                                + COLUMN_NAME_STATUS
                                                + " FROM " + TABLE_NAME_FILE_UPLOADED
                                                + " WHERE " + COLUMN_NAME_AUTH_USER_ID
                                                + " = ? ORDER BY " + COLUMN_NAME_START_TIMESTAMP + " DESC";

    /* find upload-files by user, order by start timestamp desc - latest 20 */
    String SQL_FIND_FILE_UPLOADED_LATEST_20_BY_USER = "SELECT " + COLUMN_NAME_UPLOAD_KEY + ", "
                                                      + COLUMN_NAME_AUTH_USER_ID + ", "
                                                      + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                      + COLUMN_NAME_COMPUTER_NAME + ", "
                                                      + COLUMN_NAME_FILENAME + ", "
                                                      + COLUMN_NAME_DIRECTORY + ", "
                                                      + COLUMN_NAME_FILE_SIZE + ", "
                                                      + COLUMN_NAME_UPLOAD_GROUP_ID + ", "
                                                      + COLUMN_NAME_START_TIMESTAMP + ", "
                                                      + COLUMN_NAME_END_TIMESTAMP + ", "
                                                      + COLUMN_NAME_STATUS
                                                      + " FROM " + TABLE_NAME_FILE_UPLOADED
                                                      + " WHERE " + COLUMN_NAME_AUTH_USER_ID
                                                      + " = ? ORDER BY " + COLUMN_NAME_START_TIMESTAMP + " DESC LIMIT 20";

    /* find upload-files by user, order by start timestamp desc - time range */
    String SQL_FIND_FILE_UPLOADED_WITH_TIME_RANGE_BY_USER = "SELECT " + COLUMN_NAME_UPLOAD_KEY + ", "
                                                            + COLUMN_NAME_AUTH_USER_ID + ", "
                                                            + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                            + COLUMN_NAME_COMPUTER_NAME + ", "
                                                            + COLUMN_NAME_FILENAME + ", "
                                                            + COLUMN_NAME_DIRECTORY + ", "
                                                            + COLUMN_NAME_FILE_SIZE + ", "
                                                            + COLUMN_NAME_UPLOAD_GROUP_ID + ", "
                                                            + COLUMN_NAME_START_TIMESTAMP + ", "
                                                            + COLUMN_NAME_END_TIMESTAMP + ", "
                                                            + COLUMN_NAME_STATUS
                                                            + " FROM " + TABLE_NAME_FILE_UPLOADED
                                                            + " WHERE " + COLUMN_NAME_AUTH_USER_ID
                                                            + " = ? AND " + COLUMN_NAME_END_TIMESTAMP + " BETWEEN ? AND ? "
                                                            + " ORDER BY " + COLUMN_NAME_START_TIMESTAMP + " DESC";

    /* find successful upload-files by user, order by start timestamp desc */
    String SQL_FIND_SUCCESS_FILE_UPLOADED_BY_USER = "SELECT " + COLUMN_NAME_UPLOAD_KEY + ", "
                                                    + COLUMN_NAME_AUTH_USER_ID + ", "
                                                    + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                    + COLUMN_NAME_COMPUTER_NAME + ", "
                                                    + COLUMN_NAME_FILENAME + ", "
                                                    + COLUMN_NAME_DIRECTORY + ", "
                                                    + COLUMN_NAME_FILE_SIZE + ", "
                                                    + COLUMN_NAME_UPLOAD_GROUP_ID + ", "
                                                    + COLUMN_NAME_START_TIMESTAMP + ", "
                                                    + COLUMN_NAME_END_TIMESTAMP + ", "
                                                    + COLUMN_NAME_STATUS
                                                    + " FROM " + TABLE_NAME_FILE_UPLOADED
                                                    + " WHERE " + COLUMN_NAME_AUTH_USER_ID
                                                    + " = ? AND " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_SUCCESS
                                                    + "' ORDER BY " + COLUMN_NAME_END_TIMESTAMP + " DESC";

    /* find successful upload-files by user, order by start timestamp desc - latest 20 */
    String SQL_FIND_SUCCESS_FILE_UPLOADED_LATEST_20_BY_USER = "SELECT " + COLUMN_NAME_UPLOAD_KEY + ", "
                                                              + COLUMN_NAME_AUTH_USER_ID + ", "
                                                              + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                              + COLUMN_NAME_COMPUTER_NAME + ", "
                                                              + COLUMN_NAME_FILENAME + ", "
                                                              + COLUMN_NAME_DIRECTORY + ", "
                                                              + COLUMN_NAME_FILE_SIZE + ", "
                                                              + COLUMN_NAME_UPLOAD_GROUP_ID + ", "
                                                              + COLUMN_NAME_START_TIMESTAMP + ", "
                                                              + COLUMN_NAME_END_TIMESTAMP + ", "
                                                              + COLUMN_NAME_STATUS
                                                              + " FROM " + TABLE_NAME_FILE_UPLOADED
                                                              + " WHERE " + COLUMN_NAME_AUTH_USER_ID
                                                              + " = ? AND " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_SUCCESS
                                                              + "' ORDER BY " + COLUMN_NAME_END_TIMESTAMP + " DESC LIMIT 20";


    /* find successful upload-files by user, order by start timestamp desc - time range */
    String SQL_FIND_SUCCESS_FILE_UPLOADED_WITH_TIME_RANGE_BY_USER = "SELECT " + COLUMN_NAME_UPLOAD_KEY + ", "
                                                                    + COLUMN_NAME_AUTH_USER_ID + ", "
                                                                    + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                                    + COLUMN_NAME_COMPUTER_NAME + ", "
                                                                    + COLUMN_NAME_FILENAME + ", "
                                                                    + COLUMN_NAME_DIRECTORY + ", "
                                                                    + COLUMN_NAME_FILE_SIZE + ", "
                                                                    + COLUMN_NAME_UPLOAD_GROUP_ID + ", "
                                                                    + COLUMN_NAME_START_TIMESTAMP + ", "
                                                                    + COLUMN_NAME_END_TIMESTAMP + ", "
                                                                    + COLUMN_NAME_STATUS
                                                                    + " FROM " + TABLE_NAME_FILE_UPLOADED
                                                                    + " WHERE " + COLUMN_NAME_AUTH_USER_ID
                                                                    + " = ? AND " + COLUMN_NAME_END_TIMESTAMP + " BETWEEN ? AND ? "
                                                                    + " AND " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_SUCCESS
                                                                    + "' ORDER BY " + COLUMN_NAME_END_TIMESTAMP + " DESC";

    /* find uploaded-files upload key by timeout startTimestamp */
//    String SQL_FIND_PROCESSING_FILE_UPLOADED_BY_TIMEOUT_START_TIMESTAMP = "SELECT " + COLUMN_NAME_UPLOAD_KEY + ", " + COLUMN_NAME_AUTH_USER_ID + ", " + COLUMN_NAME_FILENAME + ", " + COLUMN_NAME_DIRECTORY + ", " + COLUMN_NAME_FILE_SIZE + ", " + COLUMN_NAME_START_TIMESTAMP + ", " + COLUMN_NAME_END_TIMESTAMP + ", " + COLUMN_NAME_STATUS + " FROM " + TABLE_NAME_FILE_UPLOADED + " WHERE " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_PROCESSING + "' AND " + COLUMN_NAME_START_TIMESTAMP + " < ?";

    /* find file size of the upload-file by upload key */
    String SQL_FIND_FILE_UPLOADED_SIZE_BY_UPLOAD_KEY = "SELECT "
                                                       + COLUMN_NAME_FILE_SIZE
                                                       + " FROM "
                                                       + TABLE_NAME_FILE_UPLOADED
                                                       + " WHERE "
                                                       + COLUMN_NAME_UPLOAD_KEY + " = ?";

    /* find file size of the upload-file by upload key */
    String SQL_FIND_SUM_UPLOADING_FILE_SIZE_BY_USER = "SELECT SUM("
                                                      + COLUMN_NAME_FILE_SIZE
                                                      + ") FROM "
                                                      + TABLE_NAME_FILE_UPLOADED
                                                      + " WHERE "
                                                      + COLUMN_NAME_AUTH_USER_ID
                                                      + " = ? AND "
                                                      + COLUMN_NAME_STATUS
                                                      + " = '"
                                                      + TRANSFER_STATUS_PROCESSING + "'";

    String SQL_FIND_FILE_UPLOADED_STATUS_BY_UPLOAD_KEY_ARRAY_PREFIX = "SELECT "
                                                               + COLUMN_NAME_UPLOAD_KEY + ", "
                                                               + COLUMN_NAME_STATUS
                                                               + " FROM "
                                                               + TABLE_NAME_FILE_UPLOADED
                                                               + " WHERE "
                                                               + COLUMN_NAME_UPLOAD_KEY
                                                               + " IN ( ";

    String SQL_FIND_FILE_UPLOADED_STATUS_BY_UPLOAD_KEY_ARRAY_SUFFIX = " )";


    // Find status by transfer key
    String SQL_FIND_FILE_UPLOADED_STATUS_BY_UPLOAD_KEY = "SELECT "
                                                         + COLUMN_NAME_STATUS
                                                         + " FROM "
                                                         + TABLE_NAME_FILE_UPLOADED
                                                         + " WHERE "
                                                         + COLUMN_NAME_UPLOAD_KEY + " = ?";

    /* update computer group and computer name of the file upload by computer id */
    String SQL_UPDATE_FILE_UPLOADED_COMPUTER_NAME = "UPDATE "
                                                    + TABLE_NAME_FILE_UPLOADED
                                                    + " SET "
                                                    + COLUMN_NAME_COMPUTER_GROUP + " = ?, "
                                                    + COLUMN_NAME_COMPUTER_NAME
                                                    + " = ? WHERE "
                                                    + COLUMN_NAME_COMPUTER_ID + " = ?";

    // update the tmp file path and related columns to set a new tmp file
    String SQL_UPDATE_FILE_UPLOADED_WITH_NEW_TMP_FILE = "UPDATE "
                                                        + TABLE_NAME_FILE_UPLOADED
                                                        + " SET "
                                                        + COLUMN_NAME_TMP_FILE + " = ?, "
                                                        + COLUMN_NAME_FILE_SIZE + " = ?, "
                                                        + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP + " = ?, "
                                                        + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP + " = ?, "
                                                        + COLUMN_NAME_STATUS + " = ?, "
                                                        + COLUMN_NAME_TRANSFERRED_BYTE_INDEX + " = ?, "
                                                        + COLUMN_NAME_SOURCE_FILE_LAST_MODIFIED + " = ?, "
                                                        + COLUMN_NAME_FROM_IP + " = ?, "
                                                        + COLUMN_NAME_FROM_HOST + " = ?, "
                                                        + COLUMN_NAME_TO_IP + " = ?, "
                                                        + COLUMN_NAME_TO_HOST
                                                        + " = ? WHERE "
                                                        + COLUMN_NAME_UPLOAD_KEY + " = ?";

    // update the related columns without changing the tmp file path
    String SQL_UPDATE_FILE_UPLOADED_WITH_EXISTING_TMP_FILE = "UPDATE "
                                                             + TABLE_NAME_FILE_UPLOADED
                                                             + " SET "
                                                             + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP + " = ?, "
                                                             + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP + " = ?, "
                                                             + COLUMN_NAME_STATUS + " = ?, "
                                                             + COLUMN_NAME_FROM_IP + " = ?, "
                                                             + COLUMN_NAME_FROM_HOST + " = ?, "
                                                             + COLUMN_NAME_TO_IP + " = ?, "
                                                             + COLUMN_NAME_TO_HOST
                                                             + " = ? WHERE "
                                                             + COLUMN_NAME_UPLOAD_KEY + " = ?";

    // update the tmp file path and related columns to set a new tmp file
    String SQL_UPDATE_FILE_UPLOADED_WITH_TMP_FILE_WRITTEN_RESULT = "UPDATE "
                                                                   + TABLE_NAME_FILE_UPLOADED
                                                                   + " SET "
                                                                   + COLUMN_NAME_STATUS + " = ?, "
                                                                   + COLUMN_NAME_TRANSFERRED_BYTE_INDEX
                                                                   + " = ? WHERE "
                                                                   + COLUMN_NAME_UPLOAD_KEY + " = ?";

    String SQL_DELETE_FILE_UPLOADED_BY_UPLOAD_KEY = "DELETE FROM "
                                                    + TABLE_NAME_FILE_UPLOADED
                                                    + " WHERE "
                                                    + COLUMN_NAME_UPLOAD_KEY + " = ?";

    // TABLE: FILE_DOWNLOAD_GROUP

    String COLUMN_NAME_DOWNLOAD_KEY = "download_key";

    String COLUMN_NAME_FILE_PATH = "file_path";

    String TABLE_NAME_FILE_DOWNLOAD_GROUP = "file_download_group";

    String COLUMN_NAME_DOWNLOAD_GROUP_ID = "download_group_id";

    String COLUMN_NAME_DOWNLOAD_GROUP_DIRECTORY = "download_group_directory";

    String COLUMN_NAME_DOWNLOAD_SUBDIRECTORY_TYPE = "subdirectory_type";

    String COLUMN_NAME_DOWNLOAD_SUBDIRECTORY_VALUE = "subdirectory_name";

    String COLUMN_NAME_DOWNLOAD_DESCRIPTION_TYPE = "description_type";

    String COLUMN_NAME_DOWNLOAD_DESCRIPTION_VALUE = "description_value";

    String COLUMN_NAME_DOWNLOAD_NOTIFICATION_TYPE = "notification_type";

    String INDEX_NAME_FILE_DOWNLOAD_GROUP_CREATED_TIMESTAMP = "index_dg_created_timestamp";

    // DDL - TABLE: FILE_DOWNLOAD_GROUP

    String SQL_CREATE_TABLE_FILE_DOWNLOAD_GROUP = "CREATE TABLE " + TABLE_NAME_FILE_DOWNLOAD_GROUP + "("
                                                  + COLUMN_NAME_DOWNLOAD_GROUP_ID + " VARCHAR(1024) PRIMARY KEY, "
                                                  + COLUMN_NAME_DOWNLOAD_GROUP_DIRECTORY + " VARCHAR(4096) NULL, "
                                                  + COLUMN_NAME_DOWNLOAD_SUBDIRECTORY_TYPE + " INTEGER DEFAULT 0, "
                                                  + COLUMN_NAME_DOWNLOAD_DESCRIPTION_TYPE + " INTEGER DEFAULT 0, "
                                                  + COLUMN_NAME_DOWNLOAD_NOTIFICATION_TYPE + " INTEGER DEFAULT 2, "
                                                  + COLUMN_NAME_DOWNLOAD_SUBDIRECTORY_VALUE + " VARCHAR(1024) NULL, "
                                                  + COLUMN_NAME_DOWNLOAD_DESCRIPTION_VALUE + " TEXT NULL, "
                                                  + COLUMN_NAME_CREATED_TIMESTAMP + " BIGINT DEFAULT 0, "
                                                  + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) NOT NULL, " // When computer deleted, the value will set to null
                                                  + COLUMN_NAME_COMPUTER_ID + " BIGINT NULL)";

    String SQL_CREATE_FOREIGN_KEY_FILE_DOWNLOAD_GROUP_COMPUTER_ID = "ALTER TABLE " + TABLE_NAME_FILE_DOWNLOAD_GROUP + " ADD FOREIGN KEY (" + COLUMN_NAME_COMPUTER_ID + ") REFERENCES " + TABLE_NAME_COMPUTER + "(" + COLUMN_NAME_COMPUTER_ID + ") ON DELETE SET NULL";

    String SQL_CREATE_FOREIGN_KEY_FILE_DOWNLOAD_GROUP_AUTH_USER = "ALTER TABLE " + TABLE_NAME_FILE_DOWNLOAD_GROUP + " ADD FOREIGN KEY (" + COLUMN_NAME_AUTH_USER_ID + ") REFERENCES " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_AUTH_USER_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_INDEX_FILE_DOWNLOAD_GROUP_CREATED_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_FILE_DOWNLOAD_GROUP_CREATED_TIMESTAMP + " ON " + TABLE_NAME_FILE_DOWNLOAD_GROUP + "(" + COLUMN_NAME_CREATED_TIMESTAMP + ")";

//    // Manually add column COLUMN_NAME_DOWNLOAD_GROUP_ID if necessary:
//    // ALTER TABLE file_downloaded ADD download_group_id VARCHAR(1024) NULL;
//
//    // Called after file_download_group created and column COLUMN_NAME_DOWNLOAD_GROUP_ID created.
//    String SQL_CREATE_INDEX_FILE_DOWNLOADED_DOWNLOAD_GROUP_ID = "CREATE INDEX " + INDEX_NAME_FILE_DOWNLOADED_DOWNLOAD_GROUP_ID + " ON " + TABLE_NAME_FILE_DOWNLOADED + "(" + COLUMN_NAME_DOWNLOAD_GROUP_ID + ")";
//    String SQL_CREATE_FOREIGN_KEY_FILE_DOWNLOADED_DOWNLOAD_GROUP_ID = "ALTER TABLE " + TABLE_NAME_FILE_DOWNLOADED + " ADD FOREIGN KEY (" + COLUMN_NAME_DOWNLOAD_GROUP_ID + ") REFERENCES " + TABLE_NAME_FILE_DOWNLOAD_GROUP + "(" + COLUMN_NAME_DOWNLOAD_GROUP_ID + ") ON DELETE SET NULL";

    String SQL_CREATE_FILE_DOWNLOAD_GROUP = "INSERT INTO " + TABLE_NAME_FILE_DOWNLOAD_GROUP + "("
                                            + COLUMN_NAME_DOWNLOAD_GROUP_ID + ", "
                                            + COLUMN_NAME_DOWNLOAD_GROUP_DIRECTORY + ", "
                                            + COLUMN_NAME_DOWNLOAD_SUBDIRECTORY_TYPE + ", "
                                            + COLUMN_NAME_DOWNLOAD_DESCRIPTION_TYPE + ", "
                                            + COLUMN_NAME_DOWNLOAD_NOTIFICATION_TYPE + ", "
                                            + COLUMN_NAME_DOWNLOAD_SUBDIRECTORY_VALUE + ", "
                                            + COLUMN_NAME_DOWNLOAD_DESCRIPTION_VALUE + ", "
                                            + COLUMN_NAME_CREATED_TIMESTAMP + ", "
                                            + COLUMN_NAME_AUTH_USER_ID + ", "
                                            + COLUMN_NAME_COMPUTER_ID + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // Find by group-id
    String SQL_FIND_FILE_DOWNLOAD_GROUP_BY_ID = "SELECT "
                                                + COLUMN_NAME_DOWNLOAD_GROUP_ID + ", "
                                                + COLUMN_NAME_DOWNLOAD_GROUP_DIRECTORY + ", "
                                                + COLUMN_NAME_DOWNLOAD_SUBDIRECTORY_TYPE + ", "
                                                + COLUMN_NAME_DOWNLOAD_DESCRIPTION_TYPE + ", "
                                                + COLUMN_NAME_DOWNLOAD_NOTIFICATION_TYPE + ", "
                                                + COLUMN_NAME_DOWNLOAD_SUBDIRECTORY_VALUE + ", "
                                                + COLUMN_NAME_DOWNLOAD_DESCRIPTION_VALUE + ", "
                                                + COLUMN_NAME_AUTH_USER_ID + ", "
                                                + COLUMN_NAME_COMPUTER_ID
                                                + " FROM " + TABLE_NAME_FILE_DOWNLOAD_GROUP
                                                + " WHERE " + COLUMN_NAME_DOWNLOAD_GROUP_ID + " = ?";

    // Find if exists by group-id
    String SQL_FIND_IF_EXISTS_FILE_DOWNLOAD_GROUP_BY_ID = "SELECT "
                                                          + COLUMN_NAME_DOWNLOAD_GROUP_ID
                                                          + " FROM " + TABLE_NAME_FILE_DOWNLOAD_GROUP
                                                          + " WHERE " + COLUMN_NAME_DOWNLOAD_GROUP_ID + " = ?";

//    // Find not-created download group by user, computer-id
//    String SQL_FIND_FILE_NOT_CREATED_DOWNLOAD_GROUP_BY_USER_COMPUTER = "SELECT "
//                                                                     + COLUMN_NAME_DOWNLOAD_GROUP_ID + ", "
//                                                                     + COLUMN_NAME_DOWNLOAD_GROUP_DIRECTORY + ", "
//                                                                     + COLUMN_NAME_DOWNLOAD_SUBDIRECTORY_TYPE + ", "
//                                                                     + COLUMN_NAME_DOWNLOAD_DESCRIPTION_TYPE + ", "
//                                                                     + COLUMN_NAME_DOWNLOAD_NOTIFICATION_TYPE + ", "
//                                                                     + COLUMN_NAME_DOWNLOAD_SUBDIRECTORY_VALUE + ", "
//                                                                     + COLUMN_NAME_DOWNLOAD_DESCRIPTION_VALUE + ", "
//                                                                     + COLUMN_NAME_AUTH_USER_ID + ", "
//                                                                     + COLUMN_NAME_COMPUTER_ID + ", "
//                                                                     + COLUMN_NAME_CREATED_IN_DESKTOP_TIMESTAMP + ", "
//                                                                     + COLUMN_NAME_CREATED_IN_DESKTOP_STATUS
//                                                                     + " FROM " + TABLE_NAME_FILE_DOWNLOAD_GROUP
//                                                                     + " WHERE " + COLUMN_NAME_AUTH_USER_ID
//                                                                     + " = ? AND " + COLUMN_NAME_COMPUTER_ID
//                                                                     + " = ? AND " + COLUMN_NAME_CREATED_IN_DESKTOP_TIMESTAMP + " < 1";
//
//    // Update created-in-desktop timestamp
//    String SQL_UPDATE_FILE_DOWNLOAD_GROUP_CREATED_IN_DESKTOP_STATUS = "UPDATE "
//                                                                    + TABLE_NAME_FILE_DOWNLOAD_GROUP
//                                                                    + " SET "
//                                                                    + COLUMN_NAME_CREATED_IN_DESKTOP_TIMESTAMP + " = ?, "
//                                                                    + COLUMN_NAME_CREATED_IN_DESKTOP_STATUS
//                                                                    + " = ? WHERE "
//                                                                    + COLUMN_NAME_DOWNLOAD_GROUP_ID + " = ?";

    // Delete by download-group-id
    String SQL_DELETE_FILE_DOWNLOAD_GROUP_BY_DOWNLOAD_GROUP_ID = "DELETE FROM "
                                                                 + TABLE_NAME_FILE_DOWNLOAD_GROUP
                                                                 + " WHERE "
                                                                 + COLUMN_NAME_DOWNLOAD_GROUP_ID + " = ?";


    // TABLE: FILE_DOWNLOAD_GROUP_DETAIL: relation with download transfer keys (one-to-many)

    String TABLE_NAME_FILE_DOWNLOAD_GROUP_DETAIL = "file_download_group_detail";

    String COLUMN_NAME_FILE_DOWNLOAD_GROUP_DETAIL_ID = "file_download_group_detail_id";

//    String COLUMN_NAME_DOWNLOAD_KEY = "download_key";

    String INDEX_NAME_FILE_DOWNLOAD_GROUP_DETAIL_GROUP_ID = "index_dgd_group_id";

    String INDEX_NAME_FILE_DOWNLOAD_GROUP_DETAIL_DOWNLOAD_KEY = "index_dgd_download_key";

    // DO NOT set FK to transfer key because when creating the records, the download file record is not created.

    // DDL - TABLE: FILE_DOWNLOAD_GROUP_DETAIL

    String SQL_CREATE_TABLE_FILE_DOWNLOAD_GROUP_DETAIL = "CREATE TABLE " + TABLE_NAME_FILE_DOWNLOAD_GROUP_DETAIL + "("
                                                         + COLUMN_NAME_FILE_DOWNLOAD_GROUP_DETAIL_ID + " VARCHAR(2049) PRIMARY KEY, " // 2049: group_id+transfer_key
                                                         + COLUMN_NAME_DOWNLOAD_GROUP_ID + " VARCHAR(1024) NOT NULL, "
                                                         + COLUMN_NAME_DOWNLOAD_KEY + " VARCHAR(1024) NOT NULL, "
                                                         + COLUMN_NAME_FILE_PATH + " VARCHAR(1024) NOT NULL)";

    String SQL_CREATE_FOREIGN_KEY_FILE_DOWNLOAD_GROUP_DETAIL_DOWNLOAD_GROUP_ID = "ALTER TABLE " + TABLE_NAME_FILE_DOWNLOAD_GROUP_DETAIL + " ADD FOREIGN KEY (" + COLUMN_NAME_DOWNLOAD_GROUP_ID + ") REFERENCES " + TABLE_NAME_FILE_DOWNLOAD_GROUP + "(" + COLUMN_NAME_DOWNLOAD_GROUP_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_INDEX_FILE_DOWNLOAD_GROUP_DETAIL_GROUP_ID = "CREATE INDEX " + INDEX_NAME_FILE_DOWNLOAD_GROUP_DETAIL_GROUP_ID + " ON " + TABLE_NAME_FILE_DOWNLOAD_GROUP_DETAIL + "(" + COLUMN_NAME_FILE_DOWNLOAD_GROUP_DETAIL_ID + ")";

    String SQL_CREATE_INDEX_FILE_DOWNLOAD_GROUP_DETAIL_DOWNLOAD_KEY = "CREATE INDEX " + INDEX_NAME_FILE_DOWNLOAD_GROUP_DETAIL_DOWNLOAD_KEY + " ON " + TABLE_NAME_FILE_DOWNLOAD_GROUP_DETAIL + "(" + COLUMN_NAME_DOWNLOAD_KEY + ")";

    String SQL_CREATE_FILE_DOWNLOAD_GROUP_DETAIL = "INSERT INTO " + TABLE_NAME_FILE_DOWNLOAD_GROUP_DETAIL + "("
                                                   + COLUMN_NAME_FILE_DOWNLOAD_GROUP_DETAIL_ID + ", "
                                                   + COLUMN_NAME_DOWNLOAD_GROUP_ID + ", "
                                                   + COLUMN_NAME_DOWNLOAD_KEY + ", "
                                                   + COLUMN_NAME_FILE_PATH + ") VALUES (?, ?, ?, ?)";

    // Find by download-group
    String SQL_FIND_FILE_DOWNLOAD_GROUP_DETAIL_BY_GROUP_ID = "SELECT "
                                                             + COLUMN_NAME_FILE_DOWNLOAD_GROUP_DETAIL_ID + ", "
                                                             + COLUMN_NAME_DOWNLOAD_GROUP_ID + ", "
                                                             + COLUMN_NAME_DOWNLOAD_KEY + ", "
                                                             + COLUMN_NAME_FILE_PATH
                                                             + " FROM " + TABLE_NAME_FILE_DOWNLOAD_GROUP_DETAIL
                                                             + " WHERE " + COLUMN_NAME_DOWNLOAD_GROUP_ID + " = ?";

    // Find by download KEY
    String SQL_FIND_FILE_DOWNLOAD_GROUP_DETAIL_BY_DOWNLOAD_KEY = "SELECT "
                                                                 + COLUMN_NAME_FILE_DOWNLOAD_GROUP_DETAIL_ID + ", "
                                                                 + COLUMN_NAME_DOWNLOAD_GROUP_ID + ", "
                                                                 + COLUMN_NAME_DOWNLOAD_KEY + ", "
                                                                 + COLUMN_NAME_FILE_PATH
                                                                 + " FROM " + TABLE_NAME_FILE_DOWNLOAD_GROUP_DETAIL
                                                                 + " WHERE " + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    // Find if exists by download KEY
    String SQL_FIND_IF_EXISTS_FILE_DOWNLOAD_GROUP_DETAIL_BY_DOWNLOAD_KEY = "SELECT "
                                                                           + COLUMN_NAME_FILE_DOWNLOAD_GROUP_DETAIL_ID
                                                                           + " FROM " + TABLE_NAME_FILE_DOWNLOAD_GROUP_DETAIL
                                                                           + " WHERE " + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    // Find by download-group-detail-id
    String SQL_FIND_FILE_DOWNLOAD_GROUP_DETAIL_BY_GROUP_DETAIL_ID = "SELECT "
                                                                    + COLUMN_NAME_FILE_DOWNLOAD_GROUP_DETAIL_ID + ", "
                                                                    + COLUMN_NAME_DOWNLOAD_GROUP_ID + ", "
                                                                    + COLUMN_NAME_DOWNLOAD_KEY + ", "
                                                                    + COLUMN_NAME_FILE_PATH
                                                                    + " FROM " + TABLE_NAME_FILE_DOWNLOAD_GROUP_DETAIL
                                                                    + " WHERE " + COLUMN_NAME_FILE_DOWNLOAD_GROUP_DETAIL_ID + " = ?";

    // Delete group detail by pk
    String SQL_DELETE_FILE_DOWNLOAD_GROUP_DETAIL_BY_ID = "DELETE FROM "
                                                         + TABLE_NAME_FILE_DOWNLOAD_GROUP_DETAIL
                                                         + " WHERE "
                                                         + COLUMN_NAME_FILE_DOWNLOAD_GROUP_DETAIL_ID + " = ?";

    /* TABLE: FILE_DOWNLOADED related */

    String TABLE_NAME_FILE_DOWNLOADED = "file_downloaded";

//    String COLUMN_NAME_DOWNLOAD_KEY = "download_key";

//    String COLUMN_NAME_FILE_PATH = "file_path";

//    String COLUMN_NAME_DOWNLOAD_GROUP_ID = "download_group_id";

    String INDEX_NAME_FILE_DOWNLOADED_END_TIMESTAMP = "index_d_file_downloaded_end_timestamp";

    String INDEX_NAME_FILE_DOWNLOADED_DOWNLOAD_GROUP_ID = "index_d_file_downloaded_download_group_id";

    String INDEX_NAME_FILE_DOWNLOADED_TMP_FILE_CREATED_TIMESTAMP = "index_d_file_downloaded_tmp_file_created_timestamp";

    String INDEX_NAME_FILE_DOWNLOADED_TMP_FILE_DELETED_TIMESTAMP = "index_d_file_downloaded_tmp_file_deleted_timestamp";

    String INDEX_NAME_FILE_DOWNLOADED_FROM_IP = "index_d_file_downloaded_from_ip";

    String INDEX_NAME_FILE_DOWNLOADED_FROM_HOST = "index_d_file_downloaded_from_host";

    String INDEX_NAME_FILE_DOWNLOADED_TO_IP = "index_d_file_downloaded_to_ip";

    String INDEX_NAME_FILE_DOWNLOADED_TO_HOST = "index_d_file_downloaded_to_host";

    /* DDL - TABLE: FILE_DOWNLOADED */

    String SQL_CREATE_TABLE_FILE_DOWNLOADED = "CREATE TABLE " + TABLE_NAME_FILE_DOWNLOADED + "("
                                              + COLUMN_NAME_DOWNLOAD_KEY + " VARCHAR(1024) PRIMARY KEY, "
                                              + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) NOT NULL, "
                                              + COLUMN_NAME_COMPUTER_ID + " BIGINT NULL, "              // When computer deleted, the value will set to null
                                              + COLUMN_NAME_COMPUTER_GROUP + " VARCHAR(1024) NOT NULL, "
                                              + COLUMN_NAME_COMPUTER_NAME + " VARCHAR(1024) NOT NULL, "
                                              + COLUMN_NAME_FILE_PATH + " VARCHAR(1024) NOT NULL, "
                                              + COLUMN_NAME_FILE_SIZE + " NUMERIC(15, 0) DEFAULT 0, "   // NUMERIC(15, 0) ~ 900 TB Capacity at most
                                              + COLUMN_NAME_DOWNLOAD_GROUP_ID + " VARCHAR(1024) NULL, "
                                              + COLUMN_NAME_START_TIMESTAMP + " BIGINT DEFAULT 0, "
                                              + COLUMN_NAME_END_TIMESTAMP + " BIGINT DEFAULT 0, "
                                              + COLUMN_NAME_STATUS + " VARCHAR(1024), "
                                              + COLUMN_NAME_TMP_FILE + " VARCHAR(1024) NULL, "
                                              + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP + " BIGINT DEFAULT 0, "
                                              + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP + " BIGINT DEFAULT 0, "
                                              + COLUMN_NAME_FROM_IP + " VARCHAR(1024) NULL, "
                                              + COLUMN_NAME_FROM_HOST + " VARCHAR(1024) NULL, "
                                              + COLUMN_NAME_TO_IP + " VARCHAR(1024) NULL, "
                                              + COLUMN_NAME_TO_HOST + " VARCHAR(1024) NULL)";

    String SQL_CREATE_INDEX_FILE_DOWNLOADED_END_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_FILE_DOWNLOADED_END_TIMESTAMP + " ON " + TABLE_NAME_FILE_DOWNLOADED + "(" + COLUMN_NAME_END_TIMESTAMP + ")";

    String SQL_CREATE_FOREIGN_KEY_FILE_DOWNLOADED_COMPUTER_ID = "ALTER TABLE " + TABLE_NAME_FILE_DOWNLOADED + " ADD FOREIGN KEY (" + COLUMN_NAME_COMPUTER_ID + ") REFERENCES " + TABLE_NAME_COMPUTER + "(" + COLUMN_NAME_COMPUTER_ID + ") ON DELETE SET NULL";

    String SQL_CREATE_FOREIGN_KEY_FILE_DOWNLOADED_AUTH_USER = "ALTER TABLE " + TABLE_NAME_FILE_DOWNLOADED + " ADD FOREIGN KEY (" + COLUMN_NAME_AUTH_USER_ID + ") REFERENCES " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_AUTH_USER_ID + ") ON DELETE CASCADE";

//    String SQL_UPDATE_TABLE_FILE_DOWNLOADED_ADD_COLUMN_TMP_FILE = "ALTER TABLE " + TABLE_NAME_FILE_DOWNLOADED + " ADD " + COLUMN_NAME_TMP_FILE + " VARCHAR(1024) NULL";
//
//    String SQL_UPDATE_TABLE_FILE_DOWNLOADED_ADD_COLUMN_TMP_FILE_CREATED_TIMESTAMP = "ALTER TABLE " + TABLE_NAME_FILE_DOWNLOADED + " ADD " + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP + " BIGINT DEFAULT 0";
//
//    String SQL_UPDATE_TABLE_FILE_DOWNLOADED_ADD_COLUMN_TMP_FILE_DELETED_TIMESTAMP = "ALTER TABLE " + TABLE_NAME_FILE_DOWNLOADED + " ADD " + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP + " BIGINT DEFAULT 0";

    String SQL_CREATE_INDEX_FILE_DOWNLOADED_TMP_FILE_CREATED_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_FILE_DOWNLOADED_TMP_FILE_CREATED_TIMESTAMP + " ON " + TABLE_NAME_FILE_DOWNLOADED + "(" + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP + ")";

    String SQL_CREATE_INDEX_FILE_DOWNLOADED_TMP_FILE_DELETED_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_FILE_DOWNLOADED_TMP_FILE_DELETED_TIMESTAMP + " ON " + TABLE_NAME_FILE_DOWNLOADED + "(" + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP + ")";

//    String SQL_UPDATE_TABLE_FILE_DOWNLOADED_ADD_COLUMN_FROM_IP = "ALTER TABLE " + TABLE_NAME_FILE_DOWNLOADED + " ADD " + COLUMN_NAME_FROM_IP + " VARCHAR(1024) NULL";
//
//    String SQL_UPDATE_TABLE_FILE_DOWNLOADED_ADD_COLUMN_FROM_HOST = "ALTER TABLE " + TABLE_NAME_FILE_DOWNLOADED + " ADD " + COLUMN_NAME_FROM_HOST + " VARCHAR(1024) NULL";
//
//    String SQL_UPDATE_TABLE_FILE_DOWNLOADED_ADD_COLUMN_TO_IP = "ALTER TABLE " + TABLE_NAME_FILE_DOWNLOADED + " ADD " + COLUMN_NAME_TO_IP + " VARCHAR(1024) NULL";
//
//    String SQL_UPDATE_TABLE_FILE_DOWNLOADED_ADD_COLUMN_TO_HOST = "ALTER TABLE " + TABLE_NAME_FILE_DOWNLOADED + " ADD " + COLUMN_NAME_TO_HOST + " VARCHAR(1024) NULL";

    String SQL_CREATE_INDEX_FILE_DOWNLOADED_FROM_IP = "CREATE INDEX " + INDEX_NAME_FILE_DOWNLOADED_FROM_IP + " ON " + TABLE_NAME_FILE_DOWNLOADED + "(" + COLUMN_NAME_FROM_IP + ")";

    String SQL_CREATE_INDEX_FILE_DOWNLOADED_FROM_HOST = "CREATE INDEX " + INDEX_NAME_FILE_DOWNLOADED_FROM_HOST + " ON " + TABLE_NAME_FILE_DOWNLOADED + "(" + COLUMN_NAME_FROM_HOST + ")";

    String SQL_CREATE_INDEX_FILE_DOWNLOADED_TO_IP = "CREATE INDEX " + INDEX_NAME_FILE_DOWNLOADED_TO_IP + " ON " + TABLE_NAME_FILE_DOWNLOADED + "(" + COLUMN_NAME_TO_IP + ")";

    String SQL_CREATE_INDEX_FILE_DOWNLOADED_TO_HOST = "CREATE INDEX " + INDEX_NAME_FILE_DOWNLOADED_TO_HOST + " ON " + TABLE_NAME_FILE_DOWNLOADED + "(" + COLUMN_NAME_TO_HOST + ")";

    String SQL_CREATE_INDEX_FILE_DOWNLOADED_DOWNLOAD_GROUP_ID = "CREATE INDEX " + INDEX_NAME_FILE_DOWNLOADED_DOWNLOAD_GROUP_ID + " ON " + TABLE_NAME_FILE_DOWNLOADED + "(" + COLUMN_NAME_DOWNLOAD_GROUP_ID + ")";

    String SQL_CREATE_FOREIGN_KEY_FILE_DOWNLOADED_DOWNLOAD_GROUP_ID = "ALTER TABLE " + TABLE_NAME_FILE_DOWNLOADED + " ADD FOREIGN KEY (" + COLUMN_NAME_DOWNLOAD_GROUP_ID + ") REFERENCES " + TABLE_NAME_FILE_DOWNLOAD_GROUP + "(" + COLUMN_NAME_DOWNLOAD_GROUP_ID + ") ON DELETE SET NULL";

    // count a download-file by downloadKey
//    String SQL_COUNT_FILE_DOWNLOADED_BY_DOWNLOAD_KEY = "SELECT count(*) FROM " + TABLE_NAME_FILE_DOWNLOADED + " WHERE " + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    // find a downloaded-file by downloadKey
    String SQL_FIND_FILE_DOWNLOADED_BY_DOWNLOAD_KEY = "SELECT "
                                                      + COLUMN_NAME_DOWNLOAD_KEY + ", "
                                                      + COLUMN_NAME_AUTH_USER_ID + ", "
                                                      + COLUMN_NAME_COMPUTER_ID + ", "
                                                      + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                      + COLUMN_NAME_COMPUTER_NAME + ", "
                                                      + COLUMN_NAME_FILE_PATH + ", "
                                                      + COLUMN_NAME_FILE_SIZE + ", "
                                                      + COLUMN_NAME_DOWNLOAD_GROUP_ID + ", "
                                                      + COLUMN_NAME_START_TIMESTAMP + ", "
                                                      + COLUMN_NAME_END_TIMESTAMP + ", "
                                                      + COLUMN_NAME_STATUS + ", "
                                                      + COLUMN_NAME_TMP_FILE + ", "
                                                      + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP + ", "
                                                      + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP
                                                      + " FROM " + TABLE_NAME_FILE_DOWNLOADED
                                                      + " WHERE " + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    String SQL_FIND_IF_EXISTS_FILE_DOWNLOADED_BY_DOWNLOAD_KEY = "SELECT "
                                                                + COLUMN_NAME_DOWNLOAD_KEY
                                                                + " FROM " + TABLE_NAME_FILE_DOWNLOADED
                                                                + " WHERE " + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    String SQL_FIND_FILE_DOWNLOADED_TMP_FILE_BY_DOWNLOAD_KEY = "SELECT "
                                                               + COLUMN_NAME_TMP_FILE
                                                               + " FROM " + TABLE_NAME_FILE_DOWNLOADED
                                                               + " WHERE " + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    // find if a downloaded-file exists by download group id
    String SQL_FIND_IF_EXISTS_FILE_DOWNLOADED_BY_DOWNLOAD_GROUP_ID = "SELECT "
                                                                     + COLUMN_NAME_DOWNLOAD_KEY
                                                                     + " FROM " + TABLE_NAME_FILE_DOWNLOADED
                                                                     + " WHERE " + COLUMN_NAME_DOWNLOAD_GROUP_ID + " = ?";

    /* create FILE_DOWNLOADED */
    String SQL_CREATE_FILE_DOWNLOADED = "INSERT INTO " + TABLE_NAME_FILE_DOWNLOADED + "("
                                        + COLUMN_NAME_DOWNLOAD_KEY + ", "
                                        + COLUMN_NAME_AUTH_USER_ID + ", "
                                        + COLUMN_NAME_COMPUTER_ID + ", "
                                        + COLUMN_NAME_COMPUTER_GROUP + ", "
                                        + COLUMN_NAME_COMPUTER_NAME + ", "
                                        + COLUMN_NAME_FILE_PATH + ", "
                                        + COLUMN_NAME_DOWNLOAD_GROUP_ID + ", "
                                        + COLUMN_NAME_START_TIMESTAMP + ", "
                                        + COLUMN_NAME_END_TIMESTAMP + ", "
                                        + COLUMN_NAME_STATUS + ", "
                                        + COLUMN_NAME_TO_IP + ", "
                                        + COLUMN_NAME_TO_HOST + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String SQL_UPDATE_FILE_DOWNLOADED_TMP_FILE_CREATED = "UPDATE "
                                                         + TABLE_NAME_FILE_DOWNLOADED
                                                         + " SET "
                                                         + COLUMN_NAME_STATUS + " = ?, "
                                                         + COLUMN_NAME_TMP_FILE + " = ?, "
                                                         + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP
                                                         + " = ? WHERE "
                                                         + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    String SQL_UPDATE_FILE_DOWNLOADED_TMP_FILE_DELETED_TIMESTAMP = "UPDATE "
                                                                   + TABLE_NAME_FILE_DOWNLOADED
                                                                   + " SET "
                                                                   + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP
                                                                   + " = ? WHERE "
                                                                   + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    // update downloaded-files by downloadKey
    String SQL_UPDATE_FILE_DOWNLOADED = "UPDATE "
                                        + TABLE_NAME_FILE_DOWNLOADED
                                        + " SET "
                                        + COLUMN_NAME_FILE_PATH + " = ?, "
                                        + COLUMN_NAME_FILE_SIZE + " = ?, "
                                        + COLUMN_NAME_START_TIMESTAMP + " = ?, "
                                        + COLUMN_NAME_END_TIMESTAMP + " = ?, "
                                        + COLUMN_NAME_STATUS + " = ?, "
                                        + COLUMN_NAME_TMP_FILE + " = ?, "
                                        + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP + " = ?, "
                                        + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP + " = ?, "
                                        + COLUMN_NAME_FROM_IP + " = ?, "
                                        + COLUMN_NAME_FROM_HOST + " = ?, "
                                        + COLUMN_NAME_TO_IP + " = ?, "
                                        + COLUMN_NAME_TO_HOST
                                        + " = ? WHERE "
                                        + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    /* update downloaded-files by downloadKey for column end timestamp and status */
    String SQL_UPDATE_FILE_DOWNLOADED_STATUS = "UPDATE "
                                               + TABLE_NAME_FILE_DOWNLOADED
                                               + " SET " + COLUMN_NAME_STATUS + " = ?, "
                                               + COLUMN_NAME_END_TIMESTAMP + " = ?, "
                                               + COLUMN_NAME_FILE_SIZE
                                               + " = ? WHERE "
                                               + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    /* update downloaded-files by downloadKey for column end timestamp and status */
    String SQL_UPDATE_FILE_DOWNLOADED_WITHOUT_FILE_SIZE = "UPDATE "
                                                          + TABLE_NAME_FILE_DOWNLOADED
                                                          + " SET "
                                                          + COLUMN_NAME_STATUS
                                                          + " = ?, "
                                                          + COLUMN_NAME_END_TIMESTAMP
                                                          + " = ? WHERE "
                                                          + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    /* update downloaded-files status from processing to failure by timeout startTimestamp */
//    String SQL_UPDATE_FILE_DOWNLOADED_STATUS_PROCESSING_TO_FAILURE_FOR_TIMEOUT_START_TIMESTAMP = "UPDATE "
//                                                                                                 + TABLE_NAME_FILE_DOWNLOADED
//                                                                                                 + " SET "
//                                                                                                 + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_FAILURE
//                                                                                                 + "' WHERE "
//                                                                                                 + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_PROCESSING
//                                                                                                 + "' AND "
//                                                                                                 + COLUMN_NAME_START_TIMESTAMP + " < ?";

    // update downloaded-files with from ip and hostname
    String SQL_UPDATE_FILE_DOWNLOADED_FROM_IP_HOST = "UPDATE "
                                                     + TABLE_NAME_FILE_DOWNLOADED
                                                     + " SET "
                                                     + COLUMN_NAME_FROM_IP + " = ?, "
                                                     + COLUMN_NAME_FROM_HOST
                                                     + " = ? WHERE "
                                                     + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    /* update downloaded-files status from processing to desktop_success by transfer key */
    String SQL_UPDATE_FILE_DOWNLOADED_STATUS_FROM_PROCESSING_TO_DESKTOP_UPLOADED_BUT_UNCONFIRMED = "UPDATE "
                                                                                                   + TABLE_NAME_FILE_DOWNLOADED
                                                                                                   + " SET "
                                                                                                   + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_DESKTOP_UPLOADED_BUT_UNCONFIRMED
                                                                                                   + "' WHERE "
                                                                                                   + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_PROCESSING
                                                                                                   + "' AND " + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    /* update downloaded-files by downloadKey for column end timestamp and status */
    String SQL_UPDATE_FILE_DOWNLOADED_SIZE = "UPDATE "
                                             + TABLE_NAME_FILE_DOWNLOADED
                                             + " SET "
                                             + COLUMN_NAME_FILE_SIZE
                                             + " = ? WHERE "
                                             + COLUMN_NAME_DOWNLOAD_KEY + " = ?";

    /* find download-files by user, order by start timestamp desc */
    String SQL_FIND_ALL_FILE_DOWNLOADED_BY_USER = "SELECT " + COLUMN_NAME_DOWNLOAD_KEY + ", "
                                                  + COLUMN_NAME_AUTH_USER_ID + ", "
                                                  + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                  + COLUMN_NAME_COMPUTER_NAME + ", "
                                                  + COLUMN_NAME_FILE_PATH + ", "
                                                  + COLUMN_NAME_FILE_SIZE + ", "
                                                  + COLUMN_NAME_START_TIMESTAMP + ", "
                                                  + COLUMN_NAME_END_TIMESTAMP + ", "
                                                  + COLUMN_NAME_STATUS + ", "
                                                  + COLUMN_NAME_DOWNLOAD_GROUP_ID + ", "
                                                  + COLUMN_NAME_TMP_FILE + ", "
                                                  + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP + ", "
                                                  + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP
                                                  + " FROM " + TABLE_NAME_FILE_DOWNLOADED
                                                  + " WHERE " + COLUMN_NAME_AUTH_USER_ID
                                                  + " = ? ORDER BY " + COLUMN_NAME_START_TIMESTAMP + " DESC";

    /* find download-files by user, order by start timestamp desc */
    String SQL_FIND_SUCCESS_FILE_DOWNLOADED_BY_USER = "SELECT " + COLUMN_NAME_DOWNLOAD_KEY + ", "
                                                      + COLUMN_NAME_AUTH_USER_ID + ", "
                                                      + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                      + COLUMN_NAME_COMPUTER_NAME + ", "
                                                      + COLUMN_NAME_FILE_PATH + ", "
                                                      + COLUMN_NAME_FILE_SIZE + ", "
                                                      + COLUMN_NAME_START_TIMESTAMP + ", "
                                                      + COLUMN_NAME_END_TIMESTAMP + ", "
                                                      + COLUMN_NAME_STATUS + ", "
                                                      + COLUMN_NAME_DOWNLOAD_GROUP_ID + ", "
                                                      + COLUMN_NAME_TMP_FILE + ", "
                                                      + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP + ", "
                                                      + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP
                                                      + " FROM " + TABLE_NAME_FILE_DOWNLOADED
                                                      + " WHERE " + COLUMN_NAME_AUTH_USER_ID
                                                      + " = ? AND " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_SUCCESS
                                                      + "' ORDER BY " + COLUMN_NAME_END_TIMESTAMP + " DESC";

    /* find successful download-files by user, order by start timestamp desc, latest 20 */
    String SQL_FIND_SUCCESS_FILE_DOWNLOADED_LATEST_20_BY_USER = "SELECT " + COLUMN_NAME_DOWNLOAD_KEY + ", "
                                                                + COLUMN_NAME_AUTH_USER_ID + ", "
                                                                + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                                + COLUMN_NAME_COMPUTER_NAME + ", "
                                                                + COLUMN_NAME_FILE_PATH + ", "
                                                                + COLUMN_NAME_FILE_SIZE + ", "
                                                                + COLUMN_NAME_START_TIMESTAMP + ", "
                                                                + COLUMN_NAME_END_TIMESTAMP + ", "
                                                                + COLUMN_NAME_STATUS + ", "
                                                                + COLUMN_NAME_DOWNLOAD_GROUP_ID + ", "
                                                                + COLUMN_NAME_TMP_FILE + ", "
                                                                + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP + ", "
                                                                + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP
                                                                + " FROM " + TABLE_NAME_FILE_DOWNLOADED
                                                                + " WHERE " + COLUMN_NAME_AUTH_USER_ID
                                                                + " = ? AND " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_SUCCESS
                                                                + "' ORDER BY " + COLUMN_NAME_END_TIMESTAMP + " DESC LIMIT 20";

    /* find all download-files by user, order by start timestamp desc, latest 20 */
    String SQL_FIND_FILE_DOWNLOADED_LATEST_20_BY_USER = "SELECT " + COLUMN_NAME_DOWNLOAD_KEY + ", "
                                                        + COLUMN_NAME_AUTH_USER_ID + ", "
                                                        + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                        + COLUMN_NAME_COMPUTER_NAME + ", "
                                                        + COLUMN_NAME_FILE_PATH + ", "
                                                        + COLUMN_NAME_FILE_SIZE + ", "
                                                        + COLUMN_NAME_START_TIMESTAMP + ", "
                                                        + COLUMN_NAME_END_TIMESTAMP + ", "
                                                        + COLUMN_NAME_STATUS + ", "
                                                        + COLUMN_NAME_DOWNLOAD_GROUP_ID + ", "
                                                        + COLUMN_NAME_TMP_FILE + ", "
                                                        + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP + ", "
                                                        + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP
                                                        + " FROM " + TABLE_NAME_FILE_DOWNLOADED
                                                        + " WHERE " + COLUMN_NAME_AUTH_USER_ID
                                                        + " = ? ORDER BY " + COLUMN_NAME_END_TIMESTAMP + " DESC LIMIT 20";

    /* find successful download-files by user, order by start timestamp desc, with time range */
    String SQL_FIND_SUCCESS_FILE_DOWNLOADED_WITH_TIME_RANGE_BY_USER = "SELECT " + COLUMN_NAME_DOWNLOAD_KEY + ", "
                                                                      + COLUMN_NAME_AUTH_USER_ID + ", "
                                                                      + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                                      + COLUMN_NAME_COMPUTER_NAME + ", "
                                                                      + COLUMN_NAME_FILE_PATH + ", "
                                                                      + COLUMN_NAME_FILE_SIZE + ", "
                                                                      + COLUMN_NAME_START_TIMESTAMP + ", "
                                                                      + COLUMN_NAME_END_TIMESTAMP + ", "
                                                                      + COLUMN_NAME_STATUS + ", "
                                                                      + COLUMN_NAME_DOWNLOAD_GROUP_ID + ", "
                                                                      + COLUMN_NAME_TMP_FILE + ", "
                                                                      + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP + ", "
                                                                      + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP
                                                                      + " FROM " + TABLE_NAME_FILE_DOWNLOADED
                                                                      + " WHERE " + COLUMN_NAME_AUTH_USER_ID
                                                                      + " = ? AND " + COLUMN_NAME_END_TIMESTAMP + " BETWEEN ? AND ? "
                                                                      + " AND " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_SUCCESS
                                                                      + "' ORDER BY " + COLUMN_NAME_END_TIMESTAMP + " DESC";

    /* find all download-files by user, order by start timestamp desc, with time range */
    String SQL_FIND_FILE_DOWNLOADED_WITH_TIME_RANGE_BY_USER = "SELECT " + COLUMN_NAME_DOWNLOAD_KEY + ", "
                                                              + COLUMN_NAME_AUTH_USER_ID + ", "
                                                              + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                              + COLUMN_NAME_COMPUTER_NAME + ", "
                                                              + COLUMN_NAME_FILE_PATH + ", "
                                                              + COLUMN_NAME_FILE_SIZE + ", "
                                                              + COLUMN_NAME_START_TIMESTAMP + ", "
                                                              + COLUMN_NAME_END_TIMESTAMP + ", "
                                                              + COLUMN_NAME_STATUS + ", "
                                                              + COLUMN_NAME_DOWNLOAD_GROUP_ID + ", "
                                                              + COLUMN_NAME_TMP_FILE + ", "
                                                              + COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP + ", "
                                                              + COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP
                                                              + " FROM " + TABLE_NAME_FILE_DOWNLOADED
                                                              + " WHERE " + COLUMN_NAME_AUTH_USER_ID
                                                              + " = ? AND " + COLUMN_NAME_END_TIMESTAMP + " BETWEEN ? AND ? "
                                                              + " ORDER BY " + COLUMN_NAME_END_TIMESTAMP + " DESC";

    /* find downloaded-files downloadKey by timeout startTimestamp */
//    String SQL_FIND_PROCESSING_FILE_DOWNLOADED_BY_TIMEOUT_START_TIMESTAMP = "SELECT " + COLUMN_NAME_DOWNLOAD_KEY + ", " + COLUMN_NAME_AUTH_USER_ID + ", " + COLUMN_NAME_FILE_PATH + ", " + COLUMN_NAME_FILE_SIZE + ", " + COLUMN_NAME_START_TIMESTAMP + ", " + COLUMN_NAME_END_TIMESTAMP + ", " + COLUMN_NAME_STATUS + " FROM " + TABLE_NAME_FILE_DOWNLOADED + " WHERE " + COLUMN_NAME_STATUS + " = '" + TRANSFER_STATUS_PROCESSING + "' AND " + COLUMN_NAME_START_TIMESTAMP + " < ?";

    /* find file size of the upload-file by upload key */
    String SQL_FIND_SUM_DOWNLOADING_FILE_SIZE_BY_USER = "SELECT SUM("
                                                        + COLUMN_NAME_FILE_SIZE
                                                        + ") FROM "
                                                        + TABLE_NAME_FILE_DOWNLOADED
                                                        + " WHERE "
                                                        + COLUMN_NAME_AUTH_USER_ID
                                                        + " = ? AND "
                                                        + COLUMN_NAME_STATUS
                                                        + " = '"
                                                        + TRANSFER_STATUS_PROCESSING + "'";

    String SQL_DELETE_FILE_DOWNLOADED_BY_DOWNLOAD_KEY = "DELETE FROM "
                                                        + TABLE_NAME_FILE_DOWNLOADED
                                                        + " WHERE "
                                                        + COLUMN_NAME_DOWNLOAD_KEY + " = ?";


    /* TABLE: SMS_NOTIFICATION related */
    String TABLE_NAME_SMS_NOTIFICATION = "sms_notification";

    String COLUMN_NAME_SMS_MESSAGE_ID = "sms_id";

    String COLUMN_NAME_DELIVER_TIMESTAMP = "deliver_timestamp";

    String COLUMN_NAME_STATUS_UPDATE_TIMESTAMP = "status_update_timestamp";

    String COLUMN_NAME_STATUS_MESSAGE = "status_message";

    String INDEX_NAME_SMS_NOTIFICATION_DELIVER_TIMESTAMP = "index_s_sms_notification_deliver_timestamp";

    /* DDL - TABLE: SMS_NOTIFICATION */

    // COLUMN_NAME_AUTH_USER_ID can be null
    String SQL_CREATE_TABLE_SMS_NOTIFICATION = "CREATE TABLE " + TABLE_NAME_SMS_NOTIFICATION + "("
                                               + COLUMN_NAME_SMS_MESSAGE_ID + " VARCHAR(1024) PRIMARY KEY, "
                                               + COLUMN_NAME_PHONE_NUMBER + " VARCHAR(24) NOT NULL, "
                                               + COLUMN_NAME_DELIVER_TIMESTAMP + " BIGINT DEFAULT 0, "
                                               + COLUMN_NAME_STATUS_UPDATE_TIMESTAMP + " BIGINT DEFAULT 0, "
                                               + COLUMN_NAME_STATUS + " INTEGER DEFAULT -1, "
                                               + COLUMN_NAME_STATUS_MESSAGE + " VARCHAR(1024), "
                                               + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024))";

    String SQL_CREATE_INDEX_SMS_NOTIFICATION_DELIVER_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_SMS_NOTIFICATION_DELIVER_TIMESTAMP + " ON " + TABLE_NAME_SMS_NOTIFICATION + "(" + COLUMN_NAME_DELIVER_TIMESTAMP + ")";

    String SQL_CREATE_FOREIGN_KEY_SMS_NOTIFICATION_AUTH_USER = "ALTER TABLE " + TABLE_NAME_SMS_NOTIFICATION + " ADD FOREIGN KEY (" + COLUMN_NAME_AUTH_USER_ID + ") REFERENCES " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_AUTH_USER_ID + ") ON DELETE CASCADE";

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
                                               + COLUMN_NAME_STATUS_MESSAGE + " = ?, "
                                               + COLUMN_NAME_AUTH_USER_ID + " = ? WHERE "
                                               + COLUMN_NAME_SMS_MESSAGE_ID + " = ?";

    /* update computer group and computer name of downloaded files by computer id */
    String SQL_UPDATE_FILE_DOWNLOADED_COMPUTER_NAME = "UPDATE "
                                                      + TABLE_NAME_FILE_DOWNLOADED
                                                      + " SET "
                                                      + COLUMN_NAME_COMPUTER_GROUP
                                                      + " = ?, "
                                                      + COLUMN_NAME_COMPUTER_NAME
                                                      + " = ? WHERE "
                                                      + COLUMN_NAME_COMPUTER_ID + " = ?";

    /* TABLE: USER_COMPUTER related */

    String TABLE_NAME_USER_COMPUTER = "user_computer";

    String COLUMN_NAME_USER_COMPUTER_ID = "uc_id";

    String COLUMN_NAME_LUG_SERVER_ID = "lug_server_id";

    String COLUMN_NAME_SOCKET_CONNECTED = "socket_connected";

    String COLUMN_NAME_RECONNECT = "reconnect";

    String COLUMN_NAME_ALLOW_ALIAS = "allow_alias";

    String COLUMN_NAME_UPLOAD_DIRECTORY = "default_upload_directory";

    String COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED = "user_computer_id_encrypted";

    String INDEX_NAME_USER_COMPUTER_ENCRYPTED = "index_uc_id_encrypted";

    String INDEX_NAME_USER_COMPUTER_LUG_SERVER_ID = "index_uc_lug_server_id";

    String COLUMN_ALIAS_USER_COMPUTER_AUTH_USER_ID = "uc_user_id";

    String COLUMN_ALIAS_COMPUTER_AUTH_USER_ID = "cl_user_id";

    String COLUMN_ALIAS_USER_COMPUTER_COMPUTER_ID = "uc_cl_id";

    String COLUMN_NAME_USER_COMPUTER_UPLOAD_SUBDIRECTORY_TYPE = "upload_subdirectory_type";
    String COLUMN_NAME_USER_COMPUTER_UPLOAD_SUBDIRECTORY_VALUE = "upload_subdirectory_value";
    String COLUMN_NAME_USER_COMPUTER_UPLOAD_DESCRIPTION_TYPE = "upload_description_type";
    String COLUMN_NAME_USER_COMPUTER_UPLOAD_DESCRIPTION_VALUE = "upload_description_value";
    String COLUMN_NAME_USER_COMPUTER_UPLOAD_NOTIFICATION_TYPE = "upload_notification_type";

    String COLUMN_NAME_DOWNLOAD_DIRECTORY = "default_download_directory";
    String COLUMN_NAME_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_TYPE = "download_subdirectory_type";
    String COLUMN_NAME_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_VALUE = "download_subdirectory_value";
    String COLUMN_NAME_USER_COMPUTER_DOWNLOAD_DESCRIPTION_TYPE = "download_description_type";
    String COLUMN_NAME_USER_COMPUTER_DOWNLOAD_DESCRIPTION_VALUE = "download_description_value";
    String COLUMN_NAME_USER_COMPUTER_DOWNLOAD_NOTIFICATION_TYPE = "download_notification_type";

//    Integer DEFAULT_COLUMN_VALUE_USER_COMPUTER_UPLOAD_SUBDIRECTORY_TYPE = 0;
//    Integer DEFAULT_COLUMN_VALUE_USER_COMPUTER_UPLOAD_DESCRIPTION_TYPE = 0;
//    Integer DEFAULT_COLUMN_VALUE_USER_COMPUTER_UPLOAD_NOTIFICATION_TYPE = 2;
//    Integer DEFAULT_COLUMN_VALUE_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_TYPE = 0;
//    Integer DEFAULT_COLUMN_VALUE_USER_COMPUTER_DOWNLOAD_DESCRIPTION_TYPE = 0;
//    Integer DEFAULT_COLUMN_VALUE_USER_COMPUTER_DOWNLOAD_NOTIFICATION_TYPE = 2;

    /* DDL - TABLE: USER_COMPUTER */

    String SQL_CREATE_TABLE_USER_COMPUTER = "CREATE TABLE " + TABLE_NAME_USER_COMPUTER + "("
                                            + COLUMN_NAME_USER_COMPUTER_ID + " VARCHAR(1024) PRIMARY KEY, "
                                            + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) NOT NULL, "
                                            + COLUMN_NAME_COMPUTER_ID + " BIGINT NOT NULL, "
                                            + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED + " VARCHAR(4096) NOT NULL, "
                                            + COLUMN_NAME_LUG_SERVER_ID + " VARCHAR(1024) NULL, "
                                            + COLUMN_NAME_SOCKET_CONNECTED + " BOOLEAN DEFAULT false, "
                                            + COLUMN_NAME_RECONNECT + " BOOLEAN DEFAULT false, "
                                            + COLUMN_NAME_ALLOW_ALIAS + " BOOLEAN DEFAULT true, "
                                            + COLUMN_NAME_UPLOAD_DIRECTORY + " VARCHAR(4096) NULL, "
                                            + COLUMN_NAME_USER_COMPUTER_UPLOAD_SUBDIRECTORY_TYPE + " INTEGER NULL, "
                                            + COLUMN_NAME_USER_COMPUTER_UPLOAD_SUBDIRECTORY_VALUE + " VARCHAR(4096) NULL, "
                                            + COLUMN_NAME_USER_COMPUTER_UPLOAD_DESCRIPTION_TYPE + " INTEGER NULL, "
                                            + COLUMN_NAME_USER_COMPUTER_UPLOAD_DESCRIPTION_VALUE + " VARCHAR(4096) NULL, "
                                            + COLUMN_NAME_USER_COMPUTER_UPLOAD_NOTIFICATION_TYPE + " INTEGER NULL, "
                                            + COLUMN_NAME_DOWNLOAD_DIRECTORY + " VARCHAR(4096) NULL, "
                                            + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_TYPE + " INTEGER NULL, "
                                            + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_VALUE + " VARCHAR(4096) NULL, "
                                            + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_DESCRIPTION_TYPE + " INTEGER NULL, "
                                            + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_DESCRIPTION_VALUE + " VARCHAR(4096) NULL, "
                                            + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_NOTIFICATION_TYPE + " INTEGER NULL)";

    // Run the followings to db to add columns of user computer profiles
//    rollback;
//    alter table user_computer add upload_subdirectory_type INTEGER NULL;
//    alter table user_computer add upload_subdirectory_value VARCHAR(4096) NULL;
//    alter table user_computer add upload_description_type INTEGER NULL;
//    alter table user_computer add upload_description_value VARCHAR(4096) NULL;
//    alter table user_computer add upload_notification_type INTEGER NULL;
//    alter table user_computer add default_download_directory VARCHAR(4096) NULL;
//    alter table user_computer add download_subdirectory_type INTEGER NULL;
//    alter table user_computer add download_subdirectory_value VARCHAR(4096) NULL;
//    alter table user_computer add download_description_type INTEGER NULL
//    alter table user_computer add download_description_value VARCHAR(4096) NULL;
//    alter table user_computer add download_notification_type INTEGER NULL;
//    commit;

    String SQL_CREATE_FOREIGN_KEY_USER_COMPUTER_AUTH_USER = "ALTER TABLE " + TABLE_NAME_USER_COMPUTER + " ADD FOREIGN KEY (" + COLUMN_NAME_AUTH_USER_ID + ") REFERENCES " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_AUTH_USER_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_FOREIGN_KEY_USER_COMPUTER_COMPUTER = "ALTER TABLE " + TABLE_NAME_USER_COMPUTER + " ADD FOREIGN KEY (" + COLUMN_NAME_COMPUTER_ID + ") REFERENCES " + TABLE_NAME_COMPUTER + "(" + COLUMN_NAME_COMPUTER_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_INDEX_USER_COMPUTER_ENCRYPTED = "CREATE INDEX " + INDEX_NAME_USER_COMPUTER_ENCRYPTED + " ON " + TABLE_NAME_USER_COMPUTER + "(" + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED + ")";

    String SQL_CREATE_INDEX_USER_COMPUTER_LUG_SERVER_ID = "CREATE INDEX " + INDEX_NAME_USER_COMPUTER_LUG_SERVER_ID + " ON " + TABLE_NAME_USER_COMPUTER + "(" + COLUMN_NAME_LUG_SERVER_ID + ")";

    // do not add profiles when creating user_computer because the values are all nulls.
    String SQL_CREATE_USER_COMPUTER = "INSERT INTO " + TABLE_NAME_USER_COMPUTER + "("
                                      + COLUMN_NAME_USER_COMPUTER_ID + ", "
                                      + COLUMN_NAME_AUTH_USER_ID + ", "
                                      + COLUMN_NAME_COMPUTER_ID + ", "
                                      + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED + ", "
                                      + COLUMN_NAME_LUG_SERVER_ID + ", "
                                      + COLUMN_NAME_SOCKET_CONNECTED + ", "
                                      + COLUMN_NAME_RECONNECT + ", "
                                      + COLUMN_NAME_ALLOW_ALIAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    String SQL_FIND_USER_COMPUTER_BY_ID = "SELECT "
                                          + COLUMN_NAME_USER_COMPUTER_ID + ", "
                                          + TABLE_NAME_USER_COMPUTER + "." + COLUMN_NAME_AUTH_USER_ID + " AS " + COLUMN_ALIAS_USER_COMPUTER_AUTH_USER_ID + ", "  // computer user id
                                          + TABLE_NAME_COMPUTER + "." + COLUMN_NAME_AUTH_USER_ID + " AS " + COLUMN_ALIAS_COMPUTER_AUTH_USER_ID + ", "            // computer admin id
                                          + TABLE_NAME_USER_COMPUTER + "." + COLUMN_NAME_COMPUTER_ID + " AS " + COLUMN_ALIAS_USER_COMPUTER_COMPUTER_ID + ", "
                                          + COLUMN_NAME_COMPUTER_GROUP + ", "
                                          + COLUMN_NAME_COMPUTER_NAME + ", "
                                          + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED + ", "
                                          + COLUMN_NAME_LUG_SERVER_ID + ", "
                                          + COLUMN_NAME_SOCKET_CONNECTED + ", "
                                          + COLUMN_NAME_RECONNECT + ", "
                                          + COLUMN_NAME_ALLOW_ALIAS + ", "
                                          + COLUMN_NAME_UPLOAD_DIRECTORY + ", "
                                          + COLUMN_NAME_USER_COMPUTER_UPLOAD_SUBDIRECTORY_TYPE + ", "
                                          + COLUMN_NAME_USER_COMPUTER_UPLOAD_SUBDIRECTORY_VALUE + ", "
                                          + COLUMN_NAME_USER_COMPUTER_UPLOAD_DESCRIPTION_TYPE + ", "
                                          + COLUMN_NAME_USER_COMPUTER_UPLOAD_DESCRIPTION_VALUE + ", "
                                          + COLUMN_NAME_USER_COMPUTER_UPLOAD_NOTIFICATION_TYPE + ", "
                                          + COLUMN_NAME_DOWNLOAD_DIRECTORY + ", "
                                          + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_TYPE + ", "
                                          + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_VALUE + ", "
                                          + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_DESCRIPTION_TYPE + ", "
                                          + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_DESCRIPTION_VALUE + ", "
                                          + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_NOTIFICATION_TYPE
                                          + " FROM "
                                          + TABLE_NAME_USER_COMPUTER + ", "
                                          + TABLE_NAME_COMPUTER
                                          + " WHERE "
                                          + TABLE_NAME_USER_COMPUTER + "." + COLUMN_NAME_COMPUTER_ID
                                          + " = "
                                          + TABLE_NAME_COMPUTER + "." + COLUMN_NAME_COMPUTER_ID
                                          + " AND "
                                          + COLUMN_NAME_USER_COMPUTER_ID + " = ?";

    String SQL_FIND_USER_COMPUTER_BY_ENCRYPTED_ID = "SELECT "
                                                    + COLUMN_NAME_COMPUTER_ID + ", "
                                                    + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED + ", "
                                                    + COLUMN_NAME_LUG_SERVER_ID + ", "
                                                    + COLUMN_NAME_SOCKET_CONNECTED + ", "
                                                    + COLUMN_NAME_RECONNECT + ", "
                                                    + COLUMN_NAME_ALLOW_ALIAS + ", "
                                                    + COLUMN_NAME_UPLOAD_DIRECTORY + ", "
                                                    + COLUMN_NAME_USER_COMPUTER_UPLOAD_SUBDIRECTORY_TYPE + ", "
                                                    + COLUMN_NAME_USER_COMPUTER_UPLOAD_SUBDIRECTORY_VALUE + ", "
                                                    + COLUMN_NAME_USER_COMPUTER_UPLOAD_DESCRIPTION_TYPE + ", "
                                                    + COLUMN_NAME_USER_COMPUTER_UPLOAD_DESCRIPTION_VALUE + ", "
                                                    + COLUMN_NAME_USER_COMPUTER_UPLOAD_NOTIFICATION_TYPE + ", "
                                                    + COLUMN_NAME_DOWNLOAD_DIRECTORY + ", "
                                                    + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_TYPE + ", "
                                                    + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_VALUE + ", "
                                                    + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_DESCRIPTION_TYPE + ", "
                                                    + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_DESCRIPTION_VALUE + ", "
                                                    + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_NOTIFICATION_TYPE
                                                    + " FROM "
                                                    + TABLE_NAME_USER_COMPUTER + ", "
                                                    + TABLE_NAME_COMPUTER
                                                    + " WHERE "
                                                    + TABLE_NAME_USER_COMPUTER + "." + COLUMN_NAME_COMPUTER_ID
                                                    + " = "
                                                    + TABLE_NAME_COMPUTER + "." + COLUMN_NAME_COMPUTER_ID
                                                    + " AND "
                                                    + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED + " = ?";

    String SQL_FIND_USER_COMPUTER_CONNECTION_STATUS_BY_ENCRYPTED_IDS_PREFIX = "SELECT "
                                                                       + COLUMN_NAME_AUTH_USER_ID + ", "
                                                                       + COLUMN_NAME_COMPUTER_ID + ", "
                                                                       + COLUMN_NAME_SOCKET_CONNECTED + ", "
                                                                       + COLUMN_NAME_RECONNECT
                                                                       + " FROM "
                                                                       + TABLE_NAME_USER_COMPUTER
                                                                       + " WHERE "
                                                                       + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED
                                                                       + " IN ( ";

    String SQL_FIND_USER_COMPUTER_CONNECTION_STATUS_BY_ENCRYPTED_IDS_SUFFIX = " )";

    String SQL_FIND_USER_COMPUTERS_BY_USER_ID = "SELECT "
                                                + COLUMN_NAME_USER_COMPUTER_ID + ", "
                                                + TABLE_NAME_USER_COMPUTER + "." + COLUMN_NAME_AUTH_USER_ID + " AS " + COLUMN_ALIAS_USER_COMPUTER_AUTH_USER_ID + ", "  // computer user id
                                                + TABLE_NAME_COMPUTER + "." + COLUMN_NAME_AUTH_USER_ID + " AS " + COLUMN_ALIAS_COMPUTER_AUTH_USER_ID + ", "            // computer admin id
                                                + TABLE_NAME_USER_COMPUTER + "." + COLUMN_NAME_COMPUTER_ID + " AS " + COLUMN_ALIAS_USER_COMPUTER_COMPUTER_ID + ", "
                                                + COLUMN_NAME_COMPUTER_GROUP + ", "
                                                + COLUMN_NAME_COMPUTER_NAME + ", "
                                                + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED + ", "
                                                + COLUMN_NAME_LUG_SERVER_ID + ", "
                                                + COLUMN_NAME_SOCKET_CONNECTED + ", "
                                                + COLUMN_NAME_RECONNECT + ", "
                                                + COLUMN_NAME_ALLOW_ALIAS + ", "
                                                + COLUMN_NAME_UPLOAD_DIRECTORY + ", "
                                                + COLUMN_NAME_USER_COMPUTER_UPLOAD_SUBDIRECTORY_TYPE + ", "
                                                + COLUMN_NAME_USER_COMPUTER_UPLOAD_SUBDIRECTORY_VALUE + ", "
                                                + COLUMN_NAME_USER_COMPUTER_UPLOAD_DESCRIPTION_TYPE + ", "
                                                + COLUMN_NAME_USER_COMPUTER_UPLOAD_DESCRIPTION_VALUE + ", "
                                                + COLUMN_NAME_USER_COMPUTER_UPLOAD_NOTIFICATION_TYPE + ", "
                                                + COLUMN_NAME_DOWNLOAD_DIRECTORY + ", "
                                                + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_TYPE + ", "
                                                + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_VALUE + ", "
                                                + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_DESCRIPTION_TYPE + ", "
                                                + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_DESCRIPTION_VALUE + ", "
                                                + COLUMN_NAME_USER_COMPUTER_DOWNLOAD_NOTIFICATION_TYPE
                                                + " FROM "
                                                + TABLE_NAME_USER_COMPUTER + ", "
                                                + TABLE_NAME_COMPUTER
                                                + " WHERE "
                                                + TABLE_NAME_USER_COMPUTER + "." + COLUMN_NAME_COMPUTER_ID
                                                + " = "
                                                + TABLE_NAME_COMPUTER + "." + COLUMN_NAME_COMPUTER_ID
                                                + " AND "
                                                + TABLE_NAME_USER_COMPUTER + "." + COLUMN_NAME_AUTH_USER_ID + " = ? ORDER BY "
                                                + COLUMN_NAME_COMPUTER_GROUP + " ASC, "
                                                + COLUMN_NAME_COMPUTER_NAME + " ASC";

    /* find reconnect by encrypted user computer id */
    String SQL_FIND_RECONNECT_AND_SOCKET_CONNECTED_BY_ID_ENCRYPTED = "SELECT "
                                                                     + COLUMN_NAME_RECONNECT + ", "
                                                                     + COLUMN_NAME_SOCKET_CONNECTED
                                                                     + " FROM " + TABLE_NAME_USER_COMPUTER
                                                                     + " WHERE " + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED + "  = ?";

    /* find reconnect by encrypted user computer id */
    String SQL_FIND_RECONNECT_BY_ID_ENCRYPTED = "SELECT "
                                                + COLUMN_NAME_RECONNECT
                                                + " FROM "
                                                + TABLE_NAME_USER_COMPUTER
                                                + " WHERE "
                                                + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED + "  = ?";

    // find allow-alias by user computer id
    String SQL_FIND_ALLOW_ALIAS_BY_ID = "SELECT "
                                        + COLUMN_NAME_ALLOW_ALIAS
                                        + " FROM "
                                        + TABLE_NAME_USER_COMPUTER
                                        + " WHERE "
                                        + COLUMN_NAME_USER_COMPUTER_ID + "  = ?";

    /* find reconnect by user computer id */
    String SQL_FIND_RECONNECT_BY_ID = "SELECT "
                                      + COLUMN_NAME_RECONNECT
                                      + " FROM "
                                      + TABLE_NAME_USER_COMPUTER
                                      + " WHERE "
                                      + COLUMN_NAME_USER_COMPUTER_ID + " = ?";

    String SQL_FIND_SOCKET_CONNECTED_AND_LUG_SERVER_ID_BY_ID = "SELECT "
                                                               + COLUMN_NAME_SOCKET_CONNECTED + ", "
                                                               + COLUMN_NAME_LUG_SERVER_ID
                                                               + " FROM "
                                                               + TABLE_NAME_USER_COMPUTER
                                                               + " WHERE " + COLUMN_NAME_USER_COMPUTER_ID + " = ?";

    /* update reconnect by user computer id */
    String SQL_UPDATE_RECONNECT_BY_ID = "UPDATE "
                                        + TABLE_NAME_USER_COMPUTER
                                        + " SET "
                                        + COLUMN_NAME_RECONNECT
                                        + " = ? WHERE "
                                        + COLUMN_NAME_USER_COMPUTER_ID + " = ?";

    /* update reconnect by user id */
    String SQL_UPDATE_RECONNECT_BY_USER_ID = "UPDATE "
                                        + TABLE_NAME_USER_COMPUTER
                                        + " SET "
                                        + COLUMN_NAME_RECONNECT
                                        + " = ? WHERE "
                                        + COLUMN_NAME_AUTH_USER_ID + " = ?";

    String SQL_UPDATE_RECONNECT_AND_SOCKET_CONNECTED_BY_ID = "UPDATE "
                                                             + TABLE_NAME_USER_COMPUTER
                                                             + " SET "
                                                             + COLUMN_NAME_RECONNECT + " = ?, "
                                                             + COLUMN_NAME_SOCKET_CONNECTED
                                                             + " = ? WHERE "
                                                             + COLUMN_NAME_USER_COMPUTER_ID + " = ?";

    String SQL_UPDATE_RECONNECT_AND_SOCKET_CONNECTED_BY_USER_ID = "UPDATE "
                                                             + TABLE_NAME_USER_COMPUTER
                                                             + " SET "
                                                             + COLUMN_NAME_RECONNECT + " = ?, "
                                                             + COLUMN_NAME_SOCKET_CONNECTED
                                                             + " = ? WHERE "
                                                             + COLUMN_NAME_AUTH_USER_ID + " = ?";

    String SQL_UPDATE_LUG_SERVER_BY_ID = "UPDATE "
                                         + TABLE_NAME_USER_COMPUTER
                                         + " SET "
                                         + COLUMN_NAME_LUG_SERVER_ID
                                         + " = ? WHERE "
                                         + COLUMN_NAME_USER_COMPUTER_ID + " = ?";

    String SQL_UPDATE_SOCKET_CONNECTED_BY_ID = "UPDATE "
                                               + TABLE_NAME_USER_COMPUTER
                                               + " SET "
                                               + COLUMN_NAME_SOCKET_CONNECTED
                                               + " = ? WHERE "
                                               + COLUMN_NAME_USER_COMPUTER_ID + " = ?";

    String SQL_UPDATE_USER_COMPUTER_BY_ID = "UPDATE "
                                            + TABLE_NAME_USER_COMPUTER
                                            + " SET "
                                            + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED + " = ?, "
                                            + COLUMN_NAME_LUG_SERVER_ID + " = ?, "
                                            + COLUMN_NAME_SOCKET_CONNECTED + " = ?, "
                                            + COLUMN_NAME_RECONNECT
                                            + " = ? WHERE "
                                            + COLUMN_NAME_USER_COMPUTER_ID + " = ?";

    String SQL_UPDATE_USER_COMPUTER_ENCRYPTED_BY_COMPUTER_ID = "UPDATE "
                                                               + TABLE_NAME_USER_COMPUTER
                                                               + " SET "
                                                               + COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED
                                                               + " = ? WHERE "
                                                               + COLUMN_NAME_COMPUTER_ID + " = ?";

    String SQL_UPDATE_UPLOAD_DIRECTORY_BY_ID = "UPDATE "
                                         + TABLE_NAME_USER_COMPUTER
                                         + " SET "
                                         + COLUMN_NAME_UPLOAD_DIRECTORY
                                         + " = ? WHERE "
                                         + COLUMN_NAME_USER_COMPUTER_ID + " = ?";

    String SQL_UPDATE_USER_COMPUTER_PROFILES_BY_ID_1 = "UPDATE "
                                                     + TABLE_NAME_USER_COMPUTER
                                                     + " SET ";

    String SQL_UPDATE_USER_COMPUTER_PROFILES_BY_ID_2 = " WHERE "
                                                       + COLUMN_NAME_USER_COMPUTER_ID + " = ?";

    String SQL_DELETE_USER_COMPUTER_BY_ID = "DELETE FROM "
                                            + TABLE_NAME_USER_COMPUTER
                                            + " WHERE "
                                            + COLUMN_NAME_USER_COMPUTER_ID + " = ?";

    String SQL_DELETE_USER_COMPUTER_BY_COMPUTER_ID = "DELETE FROM "
                                                     + TABLE_NAME_USER_COMPUTER
                                                     + " WHERE "
                                                     + COLUMN_NAME_COMPUTER_ID + " = ?";

    /* TABLE: USER_COMPUTER_PROPERTIES related */

    String TABLE_NAME_USER_COMPUTER_PROPERTIES = "user_computer_properties";

    String COLUMN_NAME_USER_COMPUTER_PROPERTIES_ID = "ucp_id";

    String COLUMN_NAME_USER_COMPUTER_PROPERTY_KEY = "user_computer_property_key";

    String COLUMN_NAME_USER_COMPUTER_PROPERTY_VALUE = "user_computer_property_value";

    /* DDL - TABLE: USER_COMPUTER_PROPERTIES */

    String SQL_CREATE_TABLE_USER_COMPUTER_PROPERTIES = "CREATE TABLE "
                                                       + TABLE_NAME_USER_COMPUTER_PROPERTIES + "("
                                                       + COLUMN_NAME_USER_COMPUTER_PROPERTIES_ID + " BIGSERIAL PRIMARY KEY, "
                                                       + COLUMN_NAME_USER_COMPUTER_ID + " VARCHAR(4096) NOT NULL, "
                                                       + COLUMN_NAME_USER_COMPUTER_PROPERTY_KEY + " VARCHAR(1024) NOT NULL, "
                                                       + COLUMN_NAME_USER_COMPUTER_PROPERTY_VALUE + " VARCHAR(1024) NOT NULL)";

    String SQL_CREATE_FOREIGN_KEY_USER_COMPUTER_PROPERTIES_USER_COMPUTER = "ALTER TABLE " + TABLE_NAME_USER_COMPUTER_PROPERTIES + " ADD FOREIGN KEY (" + COLUMN_NAME_USER_COMPUTER_ID + ") REFERENCES " + TABLE_NAME_USER_COMPUTER + "(" + COLUMN_NAME_USER_COMPUTER_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_USER_COMPUTER_PROPERTIES = "INSERT INTO " + TABLE_NAME_USER_COMPUTER_PROPERTIES + "("
                                                 + COLUMN_NAME_USER_COMPUTER_ID + ", "
                                                 + COLUMN_NAME_USER_COMPUTER_PROPERTY_KEY + ", "
                                                 + COLUMN_NAME_USER_COMPUTER_PROPERTY_VALUE + ") VALUES (?, ?, ?)";

    String SQL_FIND_USER_COMPUTER_PROPERTIES_BY_USER_COMPUTER_ID = "SELECT "
                                                                   + COLUMN_NAME_USER_COMPUTER_ID + ", "
                                                                   + COLUMN_NAME_USER_COMPUTER_PROPERTY_KEY + ", "
                                                                   + COLUMN_NAME_USER_COMPUTER_PROPERTY_VALUE
                                                                   + " FROM "
                                                                   + TABLE_NAME_USER_COMPUTER_PROPERTIES
                                                                   + " WHERE "
                                                                   + COLUMN_NAME_USER_COMPUTER_ID + " = ?";

    String SQL_FIND_USER_COMPUTER_PROPERTY_VALUE = "SELECT "
                                                   + COLUMN_NAME_USER_COMPUTER_ID + ", "
                                                   + COLUMN_NAME_USER_COMPUTER_PROPERTY_KEY + ", "
                                                   + COLUMN_NAME_USER_COMPUTER_PROPERTY_VALUE
                                                   + " FROM "
                                                   + TABLE_NAME_USER_COMPUTER_PROPERTIES
                                                   + " WHERE "
                                                   + COLUMN_NAME_USER_COMPUTER_ID + " = ? AND "
                                                   + COLUMN_NAME_USER_COMPUTER_PROPERTY_KEY + " = ?";

    String SQL_DELETE_USER_COMPUTER_PROPERTIES_BY_USER_COMPUTER_ID = "DELETE FROM "
                                                                     + TABLE_NAME_USER_COMPUTER_PROPERTIES
                                                                     + " WHERE "
                                                                     + COLUMN_NAME_USER_COMPUTER_ID + " = ?";

    String SQL_DELETE_USER_COMPUTER_PROPERTIES_BY_AUTH_USER_ID = "DELETE FROM "
                                                                 + TABLE_NAME_USER_COMPUTER_PROPERTIES
                                                                 + " WHERE "
                                                                 + COLUMN_NAME_USER_COMPUTER_ID
                                                                 + " IN ( SELECT "
                                                                 + COLUMN_NAME_USER_COMPUTER_ID
                                                                 + " FROM "
                                                                 + TABLE_NAME_USER_COMPUTER
                                                                 + " WHERE "
                                                                 + COLUMN_NAME_AUTH_USER_ID + " = ? )";

    /* TABLE: PRODUCT related */

    String TABLE_NAME_PRODUCT = "product";

    String COLUMN_NAME_PRODUCT_ID = "product_id";

    String COLUMN_NAME_VENDOR = "product_vendor";

    String COLUMN_NAME_VENDOR_PRODUCT_TYPE = "vender_product_type";

    String COLUMN_NAME_TRANSFER_BYTES = "transfer_bytes";

    /* e.g. 2GB */
    String COLUMN_NAME_DISPLAYED_TRANSFER_CAPACITY = "displayed_transfer_capacity";

    String COLUMN_NAME_PRODUCT_IS_UNLIMITED = "product_unlimited";

    String DEFAULT_VENDOR_PRODUCT_TYPE = "n/a";

    String DEFAULT_VENDOR_TRANSACTION_ID = "n/a";

    String DEFAULT_VENDOR_USER_ID = "n/a";

    /* initial available transfer bytes -- 2 GB */
    long DEFAULT_TRANSFER_BYTES = 2147483648L;

    String INDEX_NAME_PRODUCT_VENDOR = "index_product_vendor";

    String FILE_NAME_DEFAULT_PRODUCTS = "default-products.txt";

    String FILE_DEFAULT_PRODUCTS_DELIMITERS = "@@";

    /* DDL - TABLE: PRODUCT */

    /* we don't support product for period subscription now */
    String SQL_CREATE_TABLE_PRODUCT = "CREATE TABLE " + TABLE_NAME_PRODUCT + "("
                                      + COLUMN_NAME_PRODUCT_ID + " VARCHAR(1024) PRIMARY KEY, "
                                      + COLUMN_NAME_VENDOR + " VARCHAR(1024) NOT NULL, "
                                      + COLUMN_NAME_VENDOR_PRODUCT_TYPE + " VARCHAR(1024) DEFAULT '" + DEFAULT_VENDOR_PRODUCT_TYPE + "', "
                                      + COLUMN_NAME_TRANSFER_BYTES + " BIGINT DEFAULT " + DEFAULT_TRANSFER_BYTES + ", "
                                      + COLUMN_NAME_DISPLAYED_TRANSFER_CAPACITY + " VARCHAR(1024) NOT NULL, "
                                      + COLUMN_NAME_PRODUCT_IS_UNLIMITED + " BOOLEAN DEFAULT false)";

    String SQL_CREATE_INDEX_PRODUCT_VENDOR = "CREATE INDEX " + INDEX_NAME_PRODUCT_VENDOR + " ON " + TABLE_NAME_PRODUCT + "(" + COLUMN_NAME_VENDOR + ")";

    String SQL_FIND_PRODUCT_BY_ID = "SELECT "
                                    + COLUMN_NAME_PRODUCT_ID + ", "
                                    + COLUMN_NAME_VENDOR + ", "
                                    + COLUMN_NAME_VENDOR_PRODUCT_TYPE + ", "
                                    + COLUMN_NAME_TRANSFER_BYTES + ", "
                                    + COLUMN_NAME_DISPLAYED_TRANSFER_CAPACITY + ", "
                                    + COLUMN_NAME_PRODUCT_IS_UNLIMITED
                                    + " FROM "
                                    + TABLE_NAME_PRODUCT
                                    + " WHERE "
                                    + COLUMN_NAME_PRODUCT_ID + " = ?";


    String SQL_CREATE_PRODUCT = "INSERT INTO " + TABLE_NAME_PRODUCT + "("
                                + COLUMN_NAME_PRODUCT_ID + ", "
                                + COLUMN_NAME_VENDOR + ", "
                                + COLUMN_NAME_VENDOR_PRODUCT_TYPE + ", "
                                + COLUMN_NAME_TRANSFER_BYTES + ", "
                                + COLUMN_NAME_DISPLAYED_TRANSFER_CAPACITY + ", "
                                + COLUMN_NAME_PRODUCT_IS_UNLIMITED + ") VALUES (?, ?, ?, ?, ?, ?)";

    String SQL_UPDATE_PRODUCT = "UPDATE " + TABLE_NAME_PRODUCT + " SET "
                                + COLUMN_NAME_VENDOR + " = ?, "
                                + COLUMN_NAME_VENDOR_PRODUCT_TYPE + " = ?, "
                                + COLUMN_NAME_TRANSFER_BYTES + " = ?, "
                                + COLUMN_NAME_DISPLAYED_TRANSFER_CAPACITY + " = ?, "
                                + COLUMN_NAME_PRODUCT_IS_UNLIMITED + " = ? WHERE "
                                + COLUMN_NAME_PRODUCT_ID + " = ?";


    /* TABLE: PRODUCT_DETAIL related */

    String TABLE_NAME_PRODUCT_DETAIL = "product_detail";

    String COLUMN_NAME_PRODUCT_DETAIL_ID = "product_detail_id";

    String COLUMN_NAME_PRODUCT_LOCALE = "product_locale";

    String COLUMN_NAME_PRODUCT_PRICE = "product_price";

    String COLUMN_NAME_PRODUCT_DISPLAYED_PRICE = "product_displayed_price";

    String COLUMN_NAME_PRODUCT_NAME = "product_name";

    String COLUMN_NAME_PRODUCT_DESCRIPTION = "product_description";

    String INDEX_NAME_PRODUCT_DETAIL_PRODUCT_ID = "index_product_detail_product_id";

    String INDEX_NAME_PRODUCT_DETAIL_PRODUCT_LOCALE = "index_product_detail_product_locale";

    /* DDL - TABLE: PRODUCT_DETAIL */

    String SQL_CREATE_TABLE_PRODUCT_DETAIL = "CREATE TABLE " + TABLE_NAME_PRODUCT_DETAIL + "("
                                             + COLUMN_NAME_PRODUCT_DETAIL_ID + " VARCHAR(1024) PRIMARY KEY, "
                                             + COLUMN_NAME_PRODUCT_ID + " VARCHAR(1024) NOT NULL, "
                                             + COLUMN_NAME_PRODUCT_LOCALE + " VARCHAR(16) NOT NULL, "
                                             + COLUMN_NAME_PRODUCT_NAME + " VARCHAR(1024) NOT NULL, "
                                             + COLUMN_NAME_PRODUCT_PRICE + " NUMERIC(10, 2) NOT NULL, "
                                             + COLUMN_NAME_PRODUCT_DISPLAYED_PRICE + " VARCHAR(16) NOT NULL, "
                                             + COLUMN_NAME_PRODUCT_DESCRIPTION + " VARCHAR(1024) NOT NULL)";

    String SQL_CREATE_INDEX_PRODUCT_DETAIL_PRODUCT_ID = "CREATE INDEX " + INDEX_NAME_PRODUCT_DETAIL_PRODUCT_ID + " ON " + TABLE_NAME_PRODUCT_DETAIL + "(" + COLUMN_NAME_PRODUCT_ID + ")";

    String SQL_CREATE_INDEX_PRODUCT_DETAIL_PRODUCT_LOCALE = "CREATE INDEX " + INDEX_NAME_PRODUCT_DETAIL_PRODUCT_LOCALE + " ON " + TABLE_NAME_PRODUCT_DETAIL + "(" + COLUMN_NAME_PRODUCT_LOCALE + ")";

    String SQL_FIND_PRODUCT_DETAIL_BY_ID = "SELECT "
                                           + COLUMN_NAME_PRODUCT_DETAIL_ID + ", "
                                           + COLUMN_NAME_PRODUCT_ID + ", "
                                           + COLUMN_NAME_PRODUCT_LOCALE + ", "
                                           + COLUMN_NAME_PRODUCT_NAME + ", "
                                           + COLUMN_NAME_PRODUCT_PRICE + ", "
                                           + COLUMN_NAME_PRODUCT_DISPLAYED_PRICE + ", "
                                           + COLUMN_NAME_PRODUCT_DESCRIPTION
                                           + " FROM "
                                           + TABLE_NAME_PRODUCT_DETAIL
                                           + " WHERE "
                                           + COLUMN_NAME_PRODUCT_DETAIL_ID + " = ?";

    String SQL_FIND_PRODUCTS_BY_VENDOR = "SELECT "
                                         + "p." + COLUMN_NAME_PRODUCT_ID + ", "
                                         + "p." + COLUMN_NAME_VENDOR + ", "
                                         + "p." + COLUMN_NAME_VENDOR_PRODUCT_TYPE + ", "
                                         + "p." + COLUMN_NAME_TRANSFER_BYTES + ", "
                                         + "p." + COLUMN_NAME_DISPLAYED_TRANSFER_CAPACITY + ", "
                                         + "p." + COLUMN_NAME_PRODUCT_IS_UNLIMITED + ", "
                                         + "d." + COLUMN_NAME_PRODUCT_LOCALE + ", "
                                         + "d." + COLUMN_NAME_PRODUCT_NAME + ", "
                                         + "d." + COLUMN_NAME_PRODUCT_PRICE + ", "
                                         + "d." + COLUMN_NAME_PRODUCT_DISPLAYED_PRICE + ", "
                                         + "d." + COLUMN_NAME_PRODUCT_DESCRIPTION
                                         + " FROM "
                                         + TABLE_NAME_PRODUCT + " p, "
                                         + TABLE_NAME_PRODUCT_DETAIL + " d"
                                         + " WHERE "
                                         + "p." + COLUMN_NAME_PRODUCT_ID + " = d." + COLUMN_NAME_PRODUCT_ID
                                         + " AND "
                                         + "lower(p." + COLUMN_NAME_VENDOR + ") = lower(?)"
                                         + " AND "
                                         + "lower(d." + COLUMN_NAME_PRODUCT_LOCALE + ") = lower(?) ORDER BY "
                                         + "p." + COLUMN_NAME_PRODUCT_ID + " ASC";

    String SQL_CREATE_PRODUCT_DETAIL = "INSERT INTO " + TABLE_NAME_PRODUCT_DETAIL + "("
                                       + COLUMN_NAME_PRODUCT_DETAIL_ID + ", "
                                       + COLUMN_NAME_PRODUCT_ID + ", "
                                       + COLUMN_NAME_PRODUCT_LOCALE + ", "
                                       + COLUMN_NAME_PRODUCT_NAME + ", "
                                       + COLUMN_NAME_PRODUCT_PRICE + ", "
                                       + COLUMN_NAME_PRODUCT_DISPLAYED_PRICE + ", "
                                       + COLUMN_NAME_PRODUCT_DESCRIPTION + ") VALUES (?, ?, ?, ?, ?, ?, ?)";

    String SQL_UPDATE_PRODUCT_DETAIL = "UPDATE " + TABLE_NAME_PRODUCT_DETAIL + " SET "
                                       + COLUMN_NAME_PRODUCT_ID + " = ?, "
                                       + COLUMN_NAME_PRODUCT_LOCALE + " = ?, "
                                       + COLUMN_NAME_PRODUCT_NAME + " = ?, "
                                       + COLUMN_NAME_PRODUCT_PRICE + " = ?, "
                                       + COLUMN_NAME_PRODUCT_DISPLAYED_PRICE + " = ?, "
                                       + COLUMN_NAME_PRODUCT_DESCRIPTION + " = ? WHERE "
                                       + COLUMN_NAME_PRODUCT_DETAIL_ID + " = ?";

    /* TABLE: PURCHASE related */

    String TABLE_NAME_PURCHASE = "purchase";

    String COLUMN_NAME_PURCHASE_ID = "purchase_id";

    String COLUMN_NAME_VENDOR_TRANSACTION_ID = "vendor_transaction_id";

    String COLUMN_NAME_VENDOR_USER_ID = "vendor_user_id";

    String COLUMN_NAME_PURCHASE_QUANTITY = "purchase_quantity";

    String COLUMN_NAME_PURCHASE_TIMESTAMP = "purchase_timestamp";

    String INDEX_NAME_PURCHASE_PRODUCT_ID = "index_purchase_product_id";

    String INDEX_NAME_PURCHASE_VENDOR_USER_ID = "index_purchase_vendor_userId";

    String INDEX_NAME_PURCHASE_AUTH_USER_ID = "index_purchase_auth_userId";

    /* DDL - TABLE: PURCHASE */

    String SQL_CREATE_TABLE_PURCHASE = "CREATE TABLE " + TABLE_NAME_PURCHASE + "("
                                       + COLUMN_NAME_PURCHASE_ID + " VARCHAR(1024) PRIMARY KEY, "
                                       + COLUMN_NAME_PRODUCT_ID + " VARCHAR(1024) NOT NULL, "
                                       + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) NOT NULL, "
                                       + COLUMN_NAME_PURCHASE_QUANTITY + " BIGINT NOT NULL, "
                                       + COLUMN_NAME_VENDOR_TRANSACTION_ID + " VARCHAR(1024) DEFAULT '" + DEFAULT_VENDOR_TRANSACTION_ID + "', "
                                       + COLUMN_NAME_VENDOR_USER_ID + " VARCHAR(1024) DEFAULT '" + DEFAULT_VENDOR_USER_ID + "', "
                                       + COLUMN_NAME_PURCHASE_TIMESTAMP + " BIGINT DEFAULT 0)";

    String SQL_CREATE_INDEX_PURCHASE_PRODUCT_ID = "CREATE INDEX " + INDEX_NAME_PURCHASE_PRODUCT_ID + " ON " + TABLE_NAME_PURCHASE + "(" + COLUMN_NAME_PRODUCT_ID + ")";

    String SQL_CREATE_INDEX_PURCHASE_VENDOR_USER_ID = "CREATE INDEX " + INDEX_NAME_PURCHASE_VENDOR_USER_ID + " ON " + TABLE_NAME_PURCHASE + "(" + COLUMN_NAME_VENDOR_USER_ID + ")";

    String SQL_CREATE_INDEX_PURCHASE_AUTH_USER_ID = "CREATE INDEX " + INDEX_NAME_PURCHASE_AUTH_USER_ID + " ON " + TABLE_NAME_PURCHASE + "(" + COLUMN_NAME_AUTH_USER_ID + ")";

    String SQL_FIND_PURCHASE_BY_ID = "SELECT "
                                     + COLUMN_NAME_PURCHASE_ID + ", "
                                     + COLUMN_NAME_PRODUCT_ID + ", "
                                     + COLUMN_NAME_AUTH_USER_ID + ", "
                                     + COLUMN_NAME_PURCHASE_QUANTITY + ", "
                                     + COLUMN_NAME_VENDOR_TRANSACTION_ID + ", "
                                     + COLUMN_NAME_VENDOR_USER_ID + ", "
                                     + COLUMN_NAME_PURCHASE_TIMESTAMP
                                     + " FROM "
                                     + TABLE_NAME_PURCHASE
                                     + " WHERE "
                                     + COLUMN_NAME_PURCHASE_ID + " = ?";

    String SQL_FIND_PURCHASES_BY_USER = "SELECT "
                                        + COLUMN_NAME_PURCHASE_ID + ", "
                                        + COLUMN_NAME_PRODUCT_ID + ", "
                                        + COLUMN_NAME_AUTH_USER_ID + ", "
                                        + COLUMN_NAME_PURCHASE_QUANTITY + ", "
                                        + COLUMN_NAME_VENDOR_TRANSACTION_ID + ", "
                                        + COLUMN_NAME_VENDOR_USER_ID + ", "
                                        + COLUMN_NAME_PURCHASE_TIMESTAMP
                                        + " FROM "
                                        + TABLE_NAME_PURCHASE
                                        + " WHERE "
                                        + COLUMN_NAME_AUTH_USER_ID + " = ?"
                                        + " ORDER BY "
                                        + COLUMN_NAME_PURCHASE_TIMESTAMP + " DESC";

    String SQL_CREATE_PURCHASE = "INSERT INTO " + TABLE_NAME_PURCHASE + "("
                                 + COLUMN_NAME_PURCHASE_ID + ", "
                                 + COLUMN_NAME_PRODUCT_ID + ", "
                                 + COLUMN_NAME_AUTH_USER_ID + ", "
                                 + COLUMN_NAME_PURCHASE_QUANTITY + ", "
                                 + COLUMN_NAME_VENDOR_TRANSACTION_ID + ", "
                                 + COLUMN_NAME_VENDOR_USER_ID + ", "
                                 + COLUMN_NAME_PURCHASE_TIMESTAMP + ") VALUES (?, ?, ?, ?, ?, ?, ?)";

    /* TABLE: CLIENT_SESSION related */

    String TABLE_NAME_CLIENT_SESSION = "client_session";

    String COLUMN_NAME_CLIENT_SESSION_ID = "client_session_id";

    String COLUMN_NAME_LAST_ACCESS_TIMESTAMP = "last_access_timestamp";

    String COLUMN_NAME_CLIENT_LOCALE = "client_locale";

    String COLUMN_NAME_DEVICE_TOKEN = "device_token";

    // the new session that replaced this old one
    String COLUMN_NAME_REPLACED_BY = "replaced_by";

    String INDEX_NAME_CLIENT_SESSION_AUTH_USER_ID = "index_client_session_user_id";

    String INDEX_NAME_CLIENT_SESSION_COMPUTER_ID = "index_client_session_computer_id";

    String INDEX_NAME_CLIENT_SESSION_LAST_ACCESS_TIMESTAMP = "index_client_session_last_access_timestamp";

    String INDEX_NAME_CLIENT_SESSION_REPLACED_BY = "index_client_session_replaced_by";

    String DEFAULT_CLIENT_LOCALE = "en";

    /* DDL - TABLE: CLIENT_SESSION */

    String SQL_CREATE_TABLE_CLIENT_SESSION = "CREATE TABLE " + TABLE_NAME_CLIENT_SESSION + "("
                                             + COLUMN_NAME_CLIENT_SESSION_ID + " VARCHAR(1024) PRIMARY KEY, "
                                             + COLUMN_NAME_COMPUTER_ID + " BIGINT NOT NULL, "
                                             + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) NOT NULL, "
                                             + COLUMN_NAME_DEVICE_TOKEN + " VARCHAR(1024) NULL, "
                                             + COLUMN_NAME_LAST_ACCESS_TIMESTAMP + " BIGINT NOT NULL, "
                                             + COLUMN_NAME_SHOW_HIDDEN + " BOOLEAN DEFAULT false, "
                                             + COLUMN_NAME_CLIENT_LOCALE + " VARCHAR(1024) DEFAULT '" + DEFAULT_CLIENT_LOCALE + "', "
                                             + COLUMN_NAME_REPLACED_BY + " VARCHAR(1024) NULL) ";

    String SQL_CREATE_FOREIGN_KEY_CLIENT_SESSION_AUTH_USER = "ALTER TABLE " + TABLE_NAME_CLIENT_SESSION + " ADD FOREIGN KEY (" + COLUMN_NAME_AUTH_USER_ID + ") REFERENCES " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_AUTH_USER_ID + ") ON DELETE CASCADE";

    // self references
    String SQL_CREATE_FOREIGN_KEY_CLIENT_SESSION_REPLACED_BY = "ALTER TABLE " + TABLE_NAME_CLIENT_SESSION + " ADD FOREIGN KEY (" + COLUMN_NAME_REPLACED_BY + ") REFERENCES " + TABLE_NAME_CLIENT_SESSION + "(" + COLUMN_NAME_CLIENT_SESSION_ID + ") ON DELETE CASCADE";

//    String SQL_ADD_COLUMN_CLIENT_SESSION_DEVICE_TOKEN = "ALTER TABLE " + TABLE_NAME_CLIENT_SESSION + " ADD " + COLUMN_NAME_DEVICE_TOKEN + " VARCHAR(1024) NULL";

    // Do not FK to computer id because pseudo computer id will break this rule
//    String SQL_CREATE_FOREIGN_KEY_CLIENT_SESSION_COMPUTER_ID = "ALTER TABLE " + TABLE_NAME_CLIENT_SESSION + " ADD FOREIGN KEY (" + COLUMN_NAME_COMPUTER_ID + ") REFERENCES " + TABLE_NAME_COMPUTER + "(" + COLUMN_NAME_COMPUTER_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_INDEX_CLIENT_SESSION_AUTH_USER_ID = "CREATE INDEX " + INDEX_NAME_CLIENT_SESSION_AUTH_USER_ID + " ON " + TABLE_NAME_CLIENT_SESSION + "(" + COLUMN_NAME_AUTH_USER_ID + ")";

    String SQL_CREATE_INDEX_CLIENT_SESSION_COMPUTER_ID = "CREATE INDEX " + INDEX_NAME_CLIENT_SESSION_COMPUTER_ID + " ON " + TABLE_NAME_CLIENT_SESSION + "(" + COLUMN_NAME_COMPUTER_ID + ")";

    String SQL_CREATE_INDEX_CLIENT_SESSION_LAST_ACCESS_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_CLIENT_SESSION_LAST_ACCESS_TIMESTAMP + " ON " + TABLE_NAME_CLIENT_SESSION + "(" + COLUMN_NAME_LAST_ACCESS_TIMESTAMP + ")";

    String SQL_CREATE_INDEX_CLIENT_SESSION_REPLACED_BY = "CREATE INDEX " + INDEX_NAME_CLIENT_SESSION_REPLACED_BY + " ON " + TABLE_NAME_CLIENT_SESSION + "(" + COLUMN_NAME_REPLACED_BY + ")";

    String SQL_CREATE_CLIENT_SESSION = "INSERT INTO " + TABLE_NAME_CLIENT_SESSION + "("
                                       + COLUMN_NAME_CLIENT_SESSION_ID + ", "
                                       + COLUMN_NAME_COMPUTER_ID + ", "
                                       + COLUMN_NAME_AUTH_USER_ID + ", "
                                       + COLUMN_NAME_DEVICE_TOKEN + ", "
                                       + COLUMN_NAME_LAST_ACCESS_TIMESTAMP + ", "
                                       + COLUMN_NAME_SHOW_HIDDEN + ", "
                                       + COLUMN_NAME_CLIENT_LOCALE + ") VALUES (?, ?, ?, ?, ?, ?, ?)";

    String SQL_FIND_CLIENT_SESSION_BY_ID = "SELECT "
                                           + COLUMN_NAME_CLIENT_SESSION_ID + ", "
                                           + COLUMN_NAME_COMPUTER_ID + ", "
                                           + COLUMN_NAME_AUTH_USER_ID + ", "
                                           + COLUMN_NAME_DEVICE_TOKEN + ", "
                                           + COLUMN_NAME_LAST_ACCESS_TIMESTAMP + ", "
                                           + COLUMN_NAME_SHOW_HIDDEN + ", "
                                           + COLUMN_NAME_CLIENT_LOCALE
                                           + " FROM "
                                           + TABLE_NAME_CLIENT_SESSION
                                           + " WHERE "
                                           + COLUMN_NAME_CLIENT_SESSION_ID + " = ?";

    String SQL_FIND_CLIENT_SESSION_ID_BY_REPLACED_BY = "SELECT "
                                                       + COLUMN_NAME_CLIENT_SESSION_ID
                                                       + " FROM "
                                                       + TABLE_NAME_CLIENT_SESSION
                                                       + " WHERE "
                                                       + COLUMN_NAME_REPLACED_BY + " = ?";

    String SQL_DELETE_CLIENT_SESSION_BY_ID = "DELETE FROM "
                                             + TABLE_NAME_CLIENT_SESSION
                                             + " WHERE "
                                             + COLUMN_NAME_CLIENT_SESSION_ID + " = ?";

    String SQL_FIND_CLIENT_SESSIONS_BY_USER_COMPUTER = "SELECT "
                                                       + COLUMN_NAME_CLIENT_SESSION_ID + ", "
                                                       + COLUMN_NAME_COMPUTER_ID + ", "
                                                       + COLUMN_NAME_AUTH_USER_ID + ", "
                                                       + COLUMN_NAME_DEVICE_TOKEN + ", "
                                                       + COLUMN_NAME_LAST_ACCESS_TIMESTAMP + ", "
                                                       + COLUMN_NAME_SHOW_HIDDEN + ", "
                                                       + COLUMN_NAME_CLIENT_LOCALE
                                                       + " FROM "
                                                       + TABLE_NAME_CLIENT_SESSION
                                                       + " WHERE "
                                                       + COLUMN_NAME_AUTH_USER_ID + " = ?"
                                                       + " AND "
                                                       + COLUMN_NAME_COMPUTER_ID
                                                       + " = ? ORDER BY "
                                                       + COLUMN_NAME_LAST_ACCESS_TIMESTAMP + " DESC";

    String SQL_FIND_CLIENT_SESSION_IDS_BY_USER_COMPUTER = "SELECT "
                                                          + COLUMN_NAME_CLIENT_SESSION_ID + ", "
                                                          + COLUMN_NAME_LAST_ACCESS_TIMESTAMP
                                                          + " FROM "
                                                          + TABLE_NAME_CLIENT_SESSION
                                                          + " WHERE "
                                                          + COLUMN_NAME_AUTH_USER_ID + " = ?"
                                                          + " AND "
                                                          + COLUMN_NAME_COMPUTER_ID
                                                          + " = ? ORDER BY "
                                                          + COLUMN_NAME_LAST_ACCESS_TIMESTAMP + " DESC";

//    String SQL_FIND_CLIENT_SESSIONS_BY_USER = "SELECT "
//                                              + COLUMN_NAME_CLIENT_SESSION_ID + ", "
//                                              + COLUMN_NAME_GROUP_NAME + ", "
//                                              + COLUMN_NAME_COMPUTER_NAME + ", "
//                                              + COLUMN_NAME_AUTH_USER_ID + ", "
//                                              + COLUMN_NAME_DEVICE_TOKEN + ", "
//                                              + COLUMN_NAME_LAST_ACCESS_TIMESTAMP + ", "
//                                              + COLUMN_NAME_SHOW_HIDDEN + ", "
//                                              + COLUMN_NAME_CLIENT_LOCALE
//                                              + " FROM "
//                                              + TABLE_NAME_CLIENT_SESSION
//                                              + " WHERE "
//                                              + COLUMN_NAME_AUTH_USER_ID + " = ?"
//                                              + " ORDER BY "
//                                              + COLUMN_NAME_LAST_ACCESS_TIMESTAMP + " DESC";

    String SQL_FIND_ALL_CLIENT_SESSIONS = "SELECT "
                                          + COLUMN_NAME_CLIENT_SESSION_ID + ", "
                                          + COLUMN_NAME_COMPUTER_ID + ", "
                                          + COLUMN_NAME_AUTH_USER_ID + ", "
                                          + COLUMN_NAME_DEVICE_TOKEN + ", "
                                          + COLUMN_NAME_LAST_ACCESS_TIMESTAMP + ", "
                                          + COLUMN_NAME_SHOW_HIDDEN + ", "
                                          + COLUMN_NAME_CLIENT_LOCALE
                                          + " FROM "
                                          + TABLE_NAME_CLIENT_SESSION
                                          + " ORDER BY "
                                          + COLUMN_NAME_LAST_ACCESS_TIMESTAMP + " DESC";

    String SQL_DELETE_CLIENT_SESSIONS_BY_USER_ID = "DELETE FROM "
                                                   + TABLE_NAME_CLIENT_SESSION
                                                   + " WHERE "
                                                   + COLUMN_NAME_AUTH_USER_ID + " = ?";

    String SQL_DELETE_CLIENT_SESSIONS_BY_COMPUTER = "DELETE FROM "
                                                    + TABLE_NAME_CLIENT_SESSION
                                                    + " WHERE "
                                                    + COLUMN_NAME_COMPUTER_ID + " = ?";

    String SQL_DELETE_CLIENT_SESSIONS_BY_ID = "DELETE FROM "
                                              + TABLE_NAME_CLIENT_SESSION
                                              + " WHERE "
                                              + COLUMN_NAME_CLIENT_SESSION_ID + " = ?";

    String SQL_DELETE_CLIENT_SESSIONS_BY_LAST_ACCESS_TIMESTAMP_SMALLER_THAN = "DELETE FROM "
                                                                              + TABLE_NAME_CLIENT_SESSION
                                                                              + " WHERE "
                                                                              + COLUMN_NAME_LAST_ACCESS_TIMESTAMP + " < ?";

    String SQL_UPDATE_CLIENT_SESSIONS_LAST_ACCESS_TIMESTAMP_BY_ID = "UPDATE "
                                                                    + TABLE_NAME_CLIENT_SESSION
                                                                    + " SET "
                                                                    + COLUMN_NAME_LAST_ACCESS_TIMESTAMP
                                                                    + " = ? WHERE "
                                                                    + COLUMN_NAME_CLIENT_SESSION_ID + " = ?";

    String SQL_UPDATE_CLIENT_SESSIONS_DEVICE_TOKEN_BY_ID = "UPDATE "
                                                           + TABLE_NAME_CLIENT_SESSION
                                                           + " SET "
                                                           + COLUMN_NAME_DEVICE_TOKEN
                                                           + " = ? WHERE "
                                                           + COLUMN_NAME_CLIENT_SESSION_ID + " = ?";

    String SQL_UPDATE_CLIENT_SESSION = "UPDATE "
                                       + TABLE_NAME_CLIENT_SESSION
                                       + " SET "
                                       + COLUMN_NAME_COMPUTER_ID + " = ?, "
                                       + COLUMN_NAME_DEVICE_TOKEN + " = ?, "
                                       + COLUMN_NAME_LAST_ACCESS_TIMESTAMP + " = ?, "
                                       + COLUMN_NAME_SHOW_HIDDEN + " = ?, "
                                       + COLUMN_NAME_CLIENT_LOCALE
                                       + " = ? WHERE "
                                       + COLUMN_NAME_CLIENT_SESSION_ID + " = ?";

    String SQL_UPDATE_CLIENT_SESSION_REPLACED_BY = "UPDATE "
                                                   + TABLE_NAME_CLIENT_SESSION
                                                   + " SET "
                                                   + COLUMN_NAME_REPLACED_BY
                                                   + " = ? WHERE "
                                                   + COLUMN_NAME_CLIENT_SESSION_ID + " = ?";

    String SQL_TRUNCATE_TABLE_CLIENT_SESSION = "TRUNCATE TABLE " + TABLE_NAME_CLIENT_SESSION;


    /* TABLE: FILELUG_PROPERTIES related -- Save filelug properties */

    String TABLE_NAME_FILELUG_PROPERTIES = "filelug_properties";

    String COLUMN_NAME_FILELUG_PROPERTY_KEY = "filelug_property_key";

    String COLUMN_NAME_FILELUG_PROPERTY_VALUE = "filelug_property_value";

    String FILE_NAME_DEFAULT_FILELUG_PROPERTIES = "default-filelug-properties.txt";

    /* DDL - TABLE: FILELUG_PROPERTIES */

    String SQL_CREATE_TABLE_FILELUG_PROPERTIES = "CREATE TABLE " + TABLE_NAME_FILELUG_PROPERTIES + "("
                                                 + COLUMN_NAME_FILELUG_PROPERTY_KEY + " VARCHAR(1024) PRIMARY KEY, "
                                                 + COLUMN_NAME_FILELUG_PROPERTY_VALUE + " VARCHAR(1024) NOT NULL)";

    String SQL_TRUNCATE_TABLE_FILELUG_PROPERTIES = "TRUNCATE TABLE " + TABLE_NAME_FILELUG_PROPERTIES;

    String SQL_CREATE_FILELUG_PROPERTIES = "INSERT INTO " + TABLE_NAME_FILELUG_PROPERTIES + "("
                                           + COLUMN_NAME_FILELUG_PROPERTY_KEY + ", "
                                           + COLUMN_NAME_FILELUG_PROPERTY_VALUE + ") VALUES (?, ?)";

    String SQL_FIND_FILELUG_PROPERTIES_BY_KEY = "SELECT "
                                                + COLUMN_NAME_FILELUG_PROPERTY_KEY + ", "
                                                + COLUMN_NAME_FILELUG_PROPERTY_VALUE
                                                + " FROM "
                                                + TABLE_NAME_FILELUG_PROPERTIES
                                                + " WHERE "
                                                + COLUMN_NAME_FILELUG_PROPERTY_KEY + " = ?";

    String SQL_UPDATE_FILELUG_PROPERTIES = "UPDATE " + TABLE_NAME_FILELUG_PROPERTIES
                                           + " SET "
                                           + COLUMN_NAME_FILELUG_PROPERTY_VALUE
                                           + " = ? WHERE "
                                           + COLUMN_NAME_FILELUG_PROPERTY_KEY + " = ?";


    /* TABLE: APPLY_CONNECTION related */

    String TABLE_NAME_APPLY_CONNECTION = "apply_connection";

    String COLUMN_NAME_APPLY_CONNECTION_ID = "apply_connection_id";

    String COLUMN_NAME_LATEST_APPLY_TIMESTAMP = "latest_apply_timestamp";

    String COLUMN_NAME_APPROVED = "approved";

    // admin user id
    String COLUMN_NAME_APPROVED_AUTH_USER_ID = "approved_auth_user_id";

    String COLUMN_NAME_LATEST_APPROVED_TIMESTAMP = "latest_approved_timestamp";

    String INDEX_NAME_APPLY_CONNECTION_APPLY_TIMESTAMPE = "index_c_ac_apply_timestamp";

    String INDEX_NAME_APPLY_CONNECTION_APPROVED = "index_c_ac_approved";

    String INDEX_NAME_APPLY_CONNECTION_APPROVED_AUTH_USER_ID = "index_c_ac_approved_auth_user_id";

    String INDEX_NAME_APPLY_CONNECTION_APPROVED_TIMESTAMPE = "index_c_ac_approved_timestamp";

    String INDEX_NAME_APPLY_CONNECTION_AUTH_USER_ID = "index_c_ac_auth_user_id";

    String INDEX_NAME_APPLY_CONNECTION_COMPUTER_ID = "index_c_ac_computer_id";

    /* DDL - TABLE: APPLY_CONNECTION */

    String SQL_CREATE_TABLE_APPLY_CONNECTION = "CREATE TABLE " + TABLE_NAME_APPLY_CONNECTION + "("
                                               + COLUMN_NAME_APPLY_CONNECTION_ID + " BIGSERIAL PRIMARY KEY, "
                                               + COLUMN_NAME_LATEST_APPLY_TIMESTAMP + " BIGINT NOT NULL, "
                                               + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) NOT NULL, "       // non-admin-user for this computer
                                               + COLUMN_NAME_COMPUTER_ID + " BIGINT NOT NULL, "
                                               + COLUMN_NAME_APPROVED + " BOOLEAN DEFAULT false, "
                                               + COLUMN_NAME_APPROVED_AUTH_USER_ID + " VARCHAR(1024), "       // admin-user for this computer
                                               + COLUMN_NAME_LATEST_APPROVED_TIMESTAMP + " BIGINT)";

    String SQL_CREATE_FOREIGN_KEY_APPLY_CONNECTION_AUTH_USER = "ALTER TABLE " + TABLE_NAME_APPLY_CONNECTION + " ADD FOREIGN KEY (" + COLUMN_NAME_AUTH_USER_ID + ") REFERENCES " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_AUTH_USER_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_FOREIGN_KEY_APPLY_CONNECTION_APPROVED_AUTH_USER = "ALTER TABLE " + TABLE_NAME_APPLY_CONNECTION + " ADD FOREIGN KEY (" + COLUMN_NAME_APPROVED_AUTH_USER_ID + ") REFERENCES " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_AUTH_USER_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_FOREIGN_KEY_APPLY_CONNECTION_COMPUTER = "ALTER TABLE " + TABLE_NAME_APPLY_CONNECTION + " ADD FOREIGN KEY (" + COLUMN_NAME_COMPUTER_ID + ") REFERENCES " + TABLE_NAME_COMPUTER + "(" + COLUMN_NAME_COMPUTER_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_INDEX_APPLY_CONNECTION_APPLY_TIMESTAMPE = "CREATE INDEX " + INDEX_NAME_APPLY_CONNECTION_APPLY_TIMESTAMPE + " ON " + TABLE_NAME_APPLY_CONNECTION + "(" + COLUMN_NAME_LATEST_APPLY_TIMESTAMP + ")";

    String SQL_CREATE_INDEX_APPLY_CONNECTION_APPROVED = "CREATE INDEX " + INDEX_NAME_APPLY_CONNECTION_APPROVED + " ON " + TABLE_NAME_APPLY_CONNECTION + "(" + COLUMN_NAME_APPROVED + ")";

    // postgres will not generate index for foreign key, so it must be created explicitly
    String SQL_CREATE_INDEX_APPLY_CONNECTION_APPROVED_AUTH_USER_ID = "CREATE INDEX " + INDEX_NAME_APPLY_CONNECTION_APPROVED_AUTH_USER_ID + " ON " + TABLE_NAME_APPLY_CONNECTION + "(" + COLUMN_NAME_APPROVED_AUTH_USER_ID + ")";

    // postgres will not generate index for foreign key, so it must be created explicitly, CREATE INDEX index_c_ac_auth_user_id ON apply_connection(auth_user_id)
    String SQL_CREATE_INDEX_APPLY_CONNECTION_AUTH_USER_ID = "CREATE INDEX " + INDEX_NAME_APPLY_CONNECTION_AUTH_USER_ID + " ON " + TABLE_NAME_APPLY_CONNECTION + "(" + COLUMN_NAME_AUTH_USER_ID + ")";

    // postgres will not generate index for foreign key, so it must be created explicitly, CREATE INDEX index_c_ac_computer_id ON apply_connection(computer_id)
    String SQL_CREATE_INDEX_APPLY_CONNECTION_COMPUTER_ID = "CREATE INDEX " + INDEX_NAME_APPLY_CONNECTION_COMPUTER_ID + " ON " + TABLE_NAME_APPLY_CONNECTION + "(" + COLUMN_NAME_COMPUTER_ID + ")";

    String SQL_CREATE_INDEX_APPLY_CONNECTION_APPROVED_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_APPLY_CONNECTION_APPROVED_TIMESTAMPE + " ON " + TABLE_NAME_APPLY_CONNECTION + "(" + COLUMN_NAME_LATEST_APPROVED_TIMESTAMP + ")";

    // Exclude the sequence id
    String SQL_CREATE_APPLY_CONNECTION = "INSERT INTO " + TABLE_NAME_APPLY_CONNECTION + "("
                                         + COLUMN_NAME_LATEST_APPLY_TIMESTAMP + ", "
                                         + COLUMN_NAME_AUTH_USER_ID + ", "
                                         + COLUMN_NAME_COMPUTER_ID + ", "
                                         + COLUMN_NAME_APPROVED + ", "
                                         + COLUMN_NAME_APPROVED_AUTH_USER_ID + ", "
                                         + COLUMN_NAME_LATEST_APPROVED_TIMESTAMP + ") VALUES (?, ?, ?, ?, ?, ?)";

    String SQL_FIND_APPLY_CONNECTION_BY_APPLY_USER_AND_COMPUTER_ID = "SELECT "
                                                                       + COLUMN_NAME_APPLY_CONNECTION_ID + ", "
                                                                       + COLUMN_NAME_LATEST_APPLY_TIMESTAMP + ", "
                                                                       + COLUMN_NAME_AUTH_USER_ID + ", "
                                                                       + COLUMN_NAME_COMPUTER_ID + ", "
                                                                       + COLUMN_NAME_APPROVED + ", "
                                                                       + COLUMN_NAME_APPROVED_AUTH_USER_ID + ", "
                                                                       + COLUMN_NAME_LATEST_APPROVED_TIMESTAMP
                                                                       + " FROM "
                                                                       + TABLE_NAME_APPLY_CONNECTION
                                                                       + " WHERE "
                                                                       + COLUMN_NAME_AUTH_USER_ID
                                                                       + " = ? AND "
                                                                       + COLUMN_NAME_COMPUTER_ID + " = ?";

    String SQL_FIND_APPLY_CONNECTIONS_BY_ADMIN_USER_ID = "SELECT "
                                                         + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_APPLY_CONNECTION_ID + ", "
                                                         + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_LATEST_APPLY_TIMESTAMP + ", "
                                                         + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_AUTH_USER_ID + ", "
                                                         + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_COMPUTER_ID + ", "
                                                         + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_APPROVED + ", "
                                                         + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_APPROVED_AUTH_USER_ID + ", "
                                                         + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_LATEST_APPROVED_TIMESTAMP + ", "
                                                         + TABLE_NAME_COMPUTER + "." + COLUMN_NAME_COMPUTER_NAME
                                                         + " FROM "
                                                         + TABLE_NAME_APPLY_CONNECTION + ", "
                                                         + TABLE_NAME_COMPUTER
                                                         + " WHERE "
                                                         + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_APPROVED_AUTH_USER_ID
                                                         + " = ? AND "
                                                         + TABLE_NAME_COMPUTER + "." + COLUMN_NAME_COMPUTER_ID
                                                         + " = "
                                                         + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_COMPUTER_ID;

    String SQL_FIND_APPLY_CONNECTIONS_BY_ADMIN_USER_ID_EXCLUDE_SELF_APPROVED = "SELECT "
                                                                               + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_APPLY_CONNECTION_ID + ", "
                                                                               + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_LATEST_APPLY_TIMESTAMP + ", "
                                                                               + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_AUTH_USER_ID + ", "
                                                                               + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_COMPUTER_ID + ", "
                                                                               + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_APPROVED + ", "
                                                                               + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_APPROVED_AUTH_USER_ID + ", "
                                                                               + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_LATEST_APPROVED_TIMESTAMP + ", "
                                                                               + TABLE_NAME_COMPUTER + "." + COLUMN_NAME_COMPUTER_NAME
                                                                               + " FROM "
                                                                               + TABLE_NAME_APPLY_CONNECTION + ", "
                                                                               + TABLE_NAME_COMPUTER
                                                                               + " WHERE "
                                                                               + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_APPROVED_AUTH_USER_ID
                                                                               + " = ? AND "
                                                                               + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_APPROVED_AUTH_USER_ID
                                                                               + " <> "
                                                                               + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_AUTH_USER_ID
                                                                               + " AND "
                                                                               + TABLE_NAME_COMPUTER + "." + COLUMN_NAME_COMPUTER_ID
                                                                               + " = "
                                                                               + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_COMPUTER_ID;

//    String SQL_FIND_APPLY_CONNECTIONS_BY_ADMIN_USER_ID = "SELECT "
//                                                         + COLUMN_NAME_APPLY_CONNECTION_ID + ", "
//                                                         + COLUMN_NAME_LATEST_APPLY_TIMESTAMP + ", "
//                                                         + COLUMN_NAME_AUTH_USER_ID + ", "
//                                                         + COLUMN_NAME_COMPUTER_ID + ", "
//                                                         + COLUMN_NAME_APPROVED + ", "
//                                                         + COLUMN_NAME_APPROVED_AUTH_USER_ID + ", "
//                                                         + COLUMN_NAME_LATEST_APPROVED_TIMESTAMP
//                                                         + " FROM "
//                                                         + TABLE_NAME_APPLY_CONNECTION
//                                                         + " WHERE "
//                                                         + COLUMN_NAME_APPROVED_AUTH_USER_ID + " = ?";

//    String SQL_FIND_APPLY_CONNECTIONS_BY_ADMIN_USER_ID_EXCLUDE_SELF_APPROVED = "SELECT "
//                                                                               + COLUMN_NAME_APPLY_CONNECTION_ID + ", "
//                                                                               + COLUMN_NAME_LATEST_APPLY_TIMESTAMP + ", "
//                                                                               + COLUMN_NAME_AUTH_USER_ID + ", "
//                                                                               + COLUMN_NAME_COMPUTER_ID + ", "
//                                                                               + COLUMN_NAME_APPROVED + ", "
//                                                                               + COLUMN_NAME_APPROVED_AUTH_USER_ID + ", "
//                                                                               + COLUMN_NAME_LATEST_APPROVED_TIMESTAMP
//                                                                               + " FROM "
//                                                                               + TABLE_NAME_APPLY_CONNECTION
//                                                                               + " WHERE "
//                                                                               + COLUMN_NAME_APPROVED_AUTH_USER_ID
//                                                                               + " = ? AND "
//                                                                               + COLUMN_NAME_APPROVED_AUTH_USER_ID
//                                                                               + " <> "
//                                                                               + COLUMN_NAME_AUTH_USER_ID;

    /* used to update the approved result
     * the user may apply connection to the same computer more than one time,
     * so use the term 'latest' to apply timestamp and approved timestamp
     */
    String SQL_UPDATE_APPLY_CONNECTION = "UPDATE " + TABLE_NAME_APPLY_CONNECTION + " SET "
                                         + COLUMN_NAME_LATEST_APPLY_TIMESTAMP + " = ?, "
                                         + COLUMN_NAME_APPROVED + " = ?, "
                                         + COLUMN_NAME_APPROVED_AUTH_USER_ID + " = ?, "
                                         + COLUMN_NAME_LATEST_APPROVED_TIMESTAMP + " = ? WHERE "
                                         + COLUMN_NAME_APPLY_CONNECTION_ID + " = ?";

    // join table: auth_user
    String SQL_FIND_APPROVED_APPLY_CONNECTION_USERS_BY_COMPUTER_ID = "SELECT "
                                                                     + TABLE_NAME_AUTH_USER + "." + COLUMN_NAME_AUTH_USER_ID + ", "
                                                                     + COLUMN_NAME_COUNTRY_ID + ", "
                                                                     + COLUMN_NAME_PHONE_NUMBER + ", "
                                                                     + COLUMN_NAME_PASSWD + ", "
                                                                     + COLUMN_NAME_NICKNAME + ", "
                                                                     + COLUMN_NAME_SHOW_HIDDEN + ", "
                                                                     + COLUMN_NAME_AUTH_USER_VERIFIED + ", "
                                                                     + COLUMN_NAME_VERIFY_CODE + ", "
                                                                     + COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP + ", "
                                                                     + COLUMN_NAME_AVAILABLE_TRANSFER_BYTES + ", "
                                                                     + COLUMN_NAME_IS_UNLIMITED_TRANSFER
                                                                     + " FROM "
                                                                     + TABLE_NAME_APPLY_CONNECTION + ", "
                                                                     + TABLE_NAME_AUTH_USER
                                                                     + " WHERE "
                                                                     + TABLE_NAME_AUTH_USER + "." + COLUMN_NAME_AUTH_USER_ID
                                                                     + " = "
                                                                     + TABLE_NAME_APPLY_CONNECTION + "." + COLUMN_NAME_AUTH_USER_ID
                                                                     + " AND "
                                                                     + COLUMN_NAME_COMPUTER_ID
                                                                     + " = ? AND "
                                                                     + COLUMN_NAME_APPROVED
                                                                     + " = true";

    /* delete by user id */
    String SQL_DELETE_APPLY_CONNECTION_BY_AUTH_USER_ID_OR_APPROVED_AUTH_USER_ID = "DELETE FROM "
                                                                                  + TABLE_NAME_APPLY_CONNECTION
                                                                                  + " WHERE "
                                                                                  + COLUMN_NAME_AUTH_USER_ID
                                                                                  + " = ? OR "
                                                                                  + COLUMN_NAME_APPROVED_AUTH_USER_ID + " = ?";

    /* delete by computer id */
    String SQL_DELETE_APPLY_CONNECTION_BY_COMPUTER_ID = "DELETE FROM "
                                                        + TABLE_NAME_APPLY_CONNECTION
                                                        + " WHERE "
                                                        + COLUMN_NAME_COMPUTER_ID
                                                        + " = ?";


    // TABLE: TASK related

    String FILE_NAME_DEFAULT_TASKS = "default-tasks.txt";

    String FILE_DEFAULT_TASKS_DELIMITERS = "@@";

    String TABLE_NAME_TASK = "c_task";

    String COLUMN_NAME_TASK_ID = "task_id";

    String COLUMN_NAME_TASK_INTERVAL = "task_interval";

    String COLUMN_NAME_TASK_INITIAL_DELAY = "task_init_delay";

    String COLUMN_NAME_TASK_LATEST_STATUS = "task_latest_status";

    String COLUMN_NAME_TASK_LATEST_START_TIMESTAMP = "task_latest_start_timestamp";

    String COLUMN_NAME_TASK_LATEST_END_TIMESTAMP = "task_latest_end_timestamp";

    String COLUMN_NAME_TASK_LATEST_ERROR_MESSAGE = "task_latest_err_msg";

    // DDL - TABLE: TASK

    String SQL_CREATE_TABLE_TASK = "CREATE TABLE " + TABLE_NAME_TASK + "("
                                   + COLUMN_NAME_TASK_ID + " VARCHAR(1024) PRIMARY KEY, "
                                   + COLUMN_NAME_TASK_INTERVAL + " BIGINT NOT NULL DEFAULT 3600, "
                                   + COLUMN_NAME_TASK_INITIAL_DELAY + " BIGINT NOT NULL DEFAULT 120, "
                                   + COLUMN_NAME_TASK_LATEST_STATUS + " VARCHAR(1024) NOT NULL DEFAULT 'none', "       // non-admin-user for this computer
                                   + COLUMN_NAME_TASK_LATEST_START_TIMESTAMP + " BIGINT NOT NULL DEFAULT 0, "
                                   + COLUMN_NAME_TASK_LATEST_END_TIMESTAMP + " BIGINT NOT NULL DEFAULT 0, "
                                   + COLUMN_NAME_TASK_LATEST_ERROR_MESSAGE + " TEXT)";

    String SQL_TRUNCATE_TABLE_TASK = "TRUNCATE TABLE " + TABLE_NAME_TASK;

    String SQL_CREATE_TASK = "INSERT INTO " + TABLE_NAME_TASK + "("
                             + COLUMN_NAME_TASK_ID + ", "
                             + COLUMN_NAME_TASK_INTERVAL + ", "
                             + COLUMN_NAME_TASK_INITIAL_DELAY + ", "
                             + COLUMN_NAME_TASK_LATEST_STATUS + ", "
                             + COLUMN_NAME_TASK_LATEST_START_TIMESTAMP + ", "
                             + COLUMN_NAME_TASK_LATEST_END_TIMESTAMP + ", "
                             + COLUMN_NAME_TASK_LATEST_ERROR_MESSAGE + ") VALUES (?, ?, ?, ?, ?, ?, ?)";

    String SQL_FIND_TASK_BY_ID = "SELECT "
                                 + COLUMN_NAME_TASK_ID + ", "
                                 + COLUMN_NAME_TASK_INTERVAL + ", "
                                 + COLUMN_NAME_TASK_INITIAL_DELAY + ", "
                                 + COLUMN_NAME_TASK_LATEST_STATUS + ", "
                                 + COLUMN_NAME_TASK_LATEST_START_TIMESTAMP + ", "
                                 + COLUMN_NAME_TASK_LATEST_END_TIMESTAMP + ", "
                                 + COLUMN_NAME_TASK_LATEST_ERROR_MESSAGE
                                 + " FROM "
                                 + TABLE_NAME_TASK
                                 + " WHERE "
                                 + COLUMN_NAME_TASK_ID + " = ?";

    String SQL_UPDATE_TASK = "UPDATE "
                             + TABLE_NAME_TASK
                             + " SET "
                             + COLUMN_NAME_TASK_LATEST_STATUS + " = ?, "
                             + COLUMN_NAME_TASK_LATEST_START_TIMESTAMP + " = ?, "
                             + COLUMN_NAME_TASK_LATEST_END_TIMESTAMP + " = ?, "
                             + COLUMN_NAME_TASK_LATEST_ERROR_MESSAGE
                             + " = ? WHERE "
                             + COLUMN_NAME_TASK_ID + " = ?";


    // TABLE: DEVICE_TOKEN related

    String TABLE_NAME_DEVICE_TOKEN = "device_token";

    String COLUMN_NAME_DEVICE_TOKEN_SEQUENCE_ID = "sequence_id";

    String COLUMN_NAME_NOTIFICATION_TYPE = "notification_type";

    String COLUMN_NAME_DEVICE_TYPE = "device_type";

    String COLUMN_NAME_DEVICE_VERSION = "device_ver";

    String COLUMN_NAME_FILELUG_VERSION = "filelug_ver";

    String COLUMN_NAME_FILELUG_BUILD = "filelug_build";

    String COLUMN_NAME_BADGE_NUMBER = "badge_num";

    String INDEX_NAME_DEVICE_TOKEN_DEVICE_TOKEN = "index_device_token_device_token";

    String INDEX_NAME_DEVICE_TOKEN_NOTIFICATION_TYPE = "index_device_token_notification_type";

    String INDEX_NAME_DEVICE_TOKEN_DEVICE_TYPE = "index_device_token_device_type";

    String INDEX_NAME_DEVICE_TOKEN_AUTH_USER_ID = "index_device_token_auth_user_id";

    String DEFAULT_FILELUG_VERSION_VALUE = "0";

    String DEFAULT_FILELUG_BUILD_VALUE = "0";

    // DDL - TABLE: DEVICE_TOKEN

    String SQL_CREATE_TABLE_DEVICE_TOKEN = "CREATE TABLE " + TABLE_NAME_DEVICE_TOKEN + "("
                                           + COLUMN_NAME_DEVICE_TOKEN_SEQUENCE_ID + " BIGSERIAL PRIMARY KEY, "
                                           + COLUMN_NAME_DEVICE_TOKEN + " VARCHAR(1024) NOT NULL, "
                                           + COLUMN_NAME_NOTIFICATION_TYPE + " VARCHAR(24) NOT NULL, "
                                           + COLUMN_NAME_DEVICE_TYPE + " VARCHAR(24) NOT NULL, "
                                           + COLUMN_NAME_DEVICE_VERSION + " VARCHAR(24) NOT NULL, "
                                           + COLUMN_NAME_FILELUG_VERSION + " VARCHAR(24) NOT NULL DEFAULT '0', "
                                           + COLUMN_NAME_FILELUG_BUILD + " VARCHAR(24) NOT NULL DEFAULT '0', "
                                           + COLUMN_NAME_BADGE_NUMBER + " INTEGER DEFAULT 0, "
                                           + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) NOT NULL)";

    String SQL_CREATE_FOREIGN_KEY_DEVICE_TOKEN_AUTH_USER = "ALTER TABLE " + TABLE_NAME_DEVICE_TOKEN + " ADD FOREIGN KEY (" + COLUMN_NAME_AUTH_USER_ID + ") REFERENCES " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_AUTH_USER_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_INDEX_DEVICE_TOKEN_DEVICE_TOKEN = "CREATE INDEX " + INDEX_NAME_DEVICE_TOKEN_DEVICE_TOKEN + " ON " + TABLE_NAME_DEVICE_TOKEN + "(" + COLUMN_NAME_DEVICE_TOKEN + ")";

    String SQL_CREATE_INDEX_DEVICE_TOKEN_NOTIFICATION_TYPE = "CREATE INDEX " + INDEX_NAME_DEVICE_TOKEN_NOTIFICATION_TYPE + " ON " + TABLE_NAME_DEVICE_TOKEN + "(" + COLUMN_NAME_NOTIFICATION_TYPE + ")";

    String SQL_CREATE_INDEX_DEVICE_TOKEN_DEVICE_TYPE = "CREATE INDEX " + INDEX_NAME_DEVICE_TOKEN_DEVICE_TYPE + " ON " + TABLE_NAME_DEVICE_TOKEN + "(" + COLUMN_NAME_DEVICE_TYPE + ")";

    String SQL_CREATE_INDEX_DEVICE_TOKEN_AUTH_USER_ID = "CREATE INDEX " + INDEX_NAME_DEVICE_TOKEN_AUTH_USER_ID + " ON " + TABLE_NAME_DEVICE_TOKEN + "(" + COLUMN_NAME_AUTH_USER_ID + ")";

    String SQL_CREATE_DEVICE_TOKEN = "INSERT INTO " + TABLE_NAME_DEVICE_TOKEN + "("
                                     + COLUMN_NAME_DEVICE_TOKEN + ", "
                                     + COLUMN_NAME_NOTIFICATION_TYPE + ", "
                                     + COLUMN_NAME_DEVICE_TYPE + ", "
                                     + COLUMN_NAME_DEVICE_VERSION + ", "
                                     + COLUMN_NAME_FILELUG_VERSION + ", "
                                     + COLUMN_NAME_FILELUG_BUILD + ", "
                                     + COLUMN_NAME_BADGE_NUMBER + ", "
                                     + COLUMN_NAME_AUTH_USER_ID + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    String SQL_UPDATE_DEVICE_TOKEN_BY_ID = "UPDATE "
                                           + TABLE_NAME_DEVICE_TOKEN
                                           + " SET "
                                           + COLUMN_NAME_DEVICE_TOKEN + " = ?, "
                                           + COLUMN_NAME_NOTIFICATION_TYPE + " = ?, "
                                           + COLUMN_NAME_DEVICE_TYPE + " = ?, "
                                           + COLUMN_NAME_DEVICE_VERSION + " = ?, "
                                           + COLUMN_NAME_FILELUG_VERSION + " = ?, "
                                           + COLUMN_NAME_FILELUG_BUILD + " = ?, "
                                           + COLUMN_NAME_BADGE_NUMBER + " = ?, "
                                           + COLUMN_NAME_AUTH_USER_ID
                                           + " = ? WHERE "
                                           + COLUMN_NAME_DEVICE_TOKEN_SEQUENCE_ID + " = ?";

    String SQL_UPDATE_BADEG_NUMBER_BY_DEVICE_TOKEN_ID = "UPDATE "
                                                        + TABLE_NAME_DEVICE_TOKEN
                                                        + " SET "
                                                        + COLUMN_NAME_BADGE_NUMBER
                                                        + " = ? WHERE "
                                                        + COLUMN_NAME_DEVICE_TOKEN_SEQUENCE_ID + " = ?";

    String SQL_FIND_IF_EXISTS_DEVICE_TOKNE_BY_TOKEN_AND_AUTH_USER_ID = "SELECT "
                                                                       + COLUMN_NAME_DEVICE_TOKEN_SEQUENCE_ID
                                                                       + " FROM "
                                                                       + TABLE_NAME_DEVICE_TOKEN
                                                                       + " WHERE "
                                                                       + COLUMN_NAME_DEVICE_TOKEN
                                                                       + " = ? AND "
                                                                       + COLUMN_NAME_AUTH_USER_ID + " = ?";

    String SQL_FIND_DEVICE_FILELUG_VERSION_BY_TOKEN_AND_AUTH_USER_ID = "SELECT "
                                                                       + COLUMN_NAME_FILELUG_VERSION
                                                                       + " FROM "
                                                                       + TABLE_NAME_DEVICE_TOKEN
                                                                       + " WHERE "
                                                                       + COLUMN_NAME_DEVICE_TOKEN
                                                                       + " = ? AND "
                                                                       + COLUMN_NAME_AUTH_USER_ID + " = ?";

    String SQL_FIND_DEVICE_TOKNE_BY_SEQUENCE_ID = "SELECT "
                                                  + COLUMN_NAME_DEVICE_TOKEN_SEQUENCE_ID + ", "
                                                  + COLUMN_NAME_DEVICE_TOKEN + ", "
                                                  + COLUMN_NAME_NOTIFICATION_TYPE + ", "
                                                  + COLUMN_NAME_DEVICE_TYPE + ", "
                                                  + COLUMN_NAME_DEVICE_VERSION + ", "
                                                  + COLUMN_NAME_FILELUG_VERSION + ", "
                                                  + COLUMN_NAME_FILELUG_BUILD + ", "
                                                  + COLUMN_NAME_BADGE_NUMBER + ", "
                                                  + COLUMN_NAME_AUTH_USER_ID
                                                  + " FROM "
                                                  + TABLE_NAME_DEVICE_TOKEN
                                                  + " WHERE "
                                                  + COLUMN_NAME_DEVICE_TOKEN_SEQUENCE_ID
                                                  + " = ?";

    String SQL_FIND_DEVICE_TOKNE_BY_TOKEN_AND_AUTH_USER_ID = "SELECT "
                                                             + COLUMN_NAME_DEVICE_TOKEN_SEQUENCE_ID + ", "
                                                             + COLUMN_NAME_DEVICE_TOKEN + ", "
                                                             + COLUMN_NAME_NOTIFICATION_TYPE + ", "
                                                             + COLUMN_NAME_DEVICE_TYPE + ", "
                                                             + COLUMN_NAME_DEVICE_VERSION + ", "
                                                             + COLUMN_NAME_FILELUG_VERSION + ", "
                                                             + COLUMN_NAME_FILELUG_BUILD + ", "
                                                             + COLUMN_NAME_BADGE_NUMBER + ", "
                                                             + COLUMN_NAME_AUTH_USER_ID
                                                             + " FROM "
                                                             + TABLE_NAME_DEVICE_TOKEN
                                                             + " WHERE "
                                                             + COLUMN_NAME_DEVICE_TOKEN
                                                             + " = ? AND "
                                                             + COLUMN_NAME_AUTH_USER_ID + " = ?";

    String SQL_FIND_DEVICE_TOKENS_BY_AUTH_USER_ID = "SELECT "
                                                    + COLUMN_NAME_DEVICE_TOKEN_SEQUENCE_ID + ", "
                                                    + COLUMN_NAME_DEVICE_TOKEN + ", "
                                                    + COLUMN_NAME_NOTIFICATION_TYPE + ", "
                                                    + COLUMN_NAME_DEVICE_TYPE + ", "
                                                    + COLUMN_NAME_DEVICE_VERSION + ", "
                                                    + COLUMN_NAME_FILELUG_VERSION + ", "
                                                    + COLUMN_NAME_FILELUG_BUILD + ", "
                                                    + COLUMN_NAME_BADGE_NUMBER + ", "
                                                    + COLUMN_NAME_AUTH_USER_ID
                                                    + " FROM "
                                                    + TABLE_NAME_DEVICE_TOKEN
                                                    + " WHERE "
                                                    + COLUMN_NAME_AUTH_USER_ID + " = ?";

    /* TABLE: ACCOUNT_KIT related */

    String TABLE_NAME_ACCOUNT_KIT = "account_kit";

    String COLUMN_NAME_ACCOUNT_KIT_ID = "ak_id";

    String COLUMN_NAME_ACCOUNT_KIT_CREATED_TIMESTAMP = "ak_created_timestamp";

    String COLUMN_NAME_USER_ACCESS_TOKEN = "user_access_token";

    String COLUMN_NAME_AUTHORIZATION_CODE = "authorization_code";

    String COLUMN_NAME_AUTHORIZATION_CODE_ENCRYPTED = "authorization_code_encrypted";

    String COLUMN_NAME_ACCOUNT_KIT_USER_ID = "ak_user_id";

    String COLUMN_NAME_COUNTRY_PHONE_NUMBER = "country_phone_number";

    String COLUMN_NAME_COUNTRY_PREFIX = "country_prefix";

    String COLUMN_NAME_NATIONAL_PHONE_NUMBER = "national_phone_number";

    // No more nedded
//    String COLUMN_NAME_NEED_VERIFY_NICKNAME_AND_EMAIL = "need_verify_nickname_and_email";

    String INDEX_NAME_ACCOUNT_KIT_AUTH_USER_ID = "index_ak_auth_user_id";

    String INDEX_NAME_ACCOUNT_KIT_USER_ACCESS_TOKEN = "index_ak_user_access_token";

    String INDEX_NAME_ACCOUNT_KIT_AUTHORIZATION_CODE = "index_ak_authorization_code";

    String INDEX_NAME_ACCOUNT_KIT_AUTHORIZATION_CODE_ENCRYPTED = "index_ak_authorization_code_encrypted";

    String INDEX_NAME_ACCOUNT_KIT_CREATED_TIMESTAMP = "index_ak_created_timestamp";

    /* DDL - TABLE: ACCOUNT_KIT */

    String SQL_CREATE_TABLE_ACCOUNT_KIT = "CREATE TABLE " + TABLE_NAME_ACCOUNT_KIT + "("
                                          + COLUMN_NAME_ACCOUNT_KIT_ID + " BIGSERIAL PRIMARY KEY, "
                                          + COLUMN_NAME_ACCOUNT_KIT_CREATED_TIMESTAMP + " BIGINT NOT NULL, "
                                          + COLUMN_NAME_USER_ACCESS_TOKEN + " VARCHAR(1024) NOT NULL, "
                                          + COLUMN_NAME_AUTHORIZATION_CODE + " VARCHAR(1024) NOT NULL, "
                                          + COLUMN_NAME_AUTHORIZATION_CODE_ENCRYPTED + " VARCHAR(1024) NOT NULL, "
                                          + COLUMN_NAME_ACCOUNT_KIT_USER_ID + " VARCHAR(1024) NULL, "
                                          + COLUMN_NAME_COUNTRY_PHONE_NUMBER + " VARCHAR(1024) NULL, "
                                          + COLUMN_NAME_COUNTRY_PREFIX + " VARCHAR(1024) NULL, "
                                          + COLUMN_NAME_NATIONAL_PHONE_NUMBER + " VARCHAR(1024) NULL, "
                                          + COLUMN_NAME_AUTH_USER_ID + " VARCHAR(1024) NULL)";

    String SQL_CREATE_FOREIGN_KEY_ACCOUNT_KIT_AUTH_USER = "ALTER TABLE " + TABLE_NAME_ACCOUNT_KIT + " ADD FOREIGN KEY (" + COLUMN_NAME_AUTH_USER_ID + ") REFERENCES " + TABLE_NAME_AUTH_USER + "(" + COLUMN_NAME_AUTH_USER_ID + ") ON DELETE CASCADE";

    String SQL_CREATE_INDEX_ACCOUNT_KIT_CREATED_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_ACCOUNT_KIT_CREATED_TIMESTAMP + " ON " + TABLE_NAME_ACCOUNT_KIT + "(" + COLUMN_NAME_ACCOUNT_KIT_CREATED_TIMESTAMP + ")";

    String SQL_CREATE_INDEX_ACCOUNT_KIT_USER_ACCESS_TOKEN = "CREATE INDEX " + INDEX_NAME_ACCOUNT_KIT_USER_ACCESS_TOKEN + " ON " + TABLE_NAME_ACCOUNT_KIT + "(" + COLUMN_NAME_USER_ACCESS_TOKEN + ")";

    String SQL_CREATE_INDEX_ACCOUNT_KIT_AUTHORIZATION_CODE = "CREATE INDEX " + INDEX_NAME_ACCOUNT_KIT_AUTHORIZATION_CODE + " ON " + TABLE_NAME_ACCOUNT_KIT + "(" + COLUMN_NAME_AUTHORIZATION_CODE + ")";

    String SQL_CREATE_INDEX_ACCOUNT_KIT_AUTHORIZATION_CODE_ENCRYPTED = "CREATE INDEX " + INDEX_NAME_ACCOUNT_KIT_AUTHORIZATION_CODE_ENCRYPTED + " ON " + TABLE_NAME_ACCOUNT_KIT + "(" + COLUMN_NAME_AUTHORIZATION_CODE_ENCRYPTED + ")";

    String SQL_CREATE_INDEX_ACCOUNT_KIT_AUTH_USER_ID = "CREATE INDEX " + INDEX_NAME_ACCOUNT_KIT_AUTH_USER_ID + " ON " + TABLE_NAME_ACCOUNT_KIT + "(" + COLUMN_NAME_AUTH_USER_ID + ")";

    String SQL_CREATE_ACCOUNT_KIT = "INSERT INTO " + TABLE_NAME_ACCOUNT_KIT + "("
                                    + COLUMN_NAME_ACCOUNT_KIT_CREATED_TIMESTAMP + ", "
                                    + COLUMN_NAME_USER_ACCESS_TOKEN + ", "
                                    + COLUMN_NAME_AUTHORIZATION_CODE + ", "
                                    + COLUMN_NAME_AUTHORIZATION_CODE_ENCRYPTED + ", "
                                    + COLUMN_NAME_ACCOUNT_KIT_USER_ID + ", "
                                    + COLUMN_NAME_COUNTRY_PHONE_NUMBER + ", "
                                    + COLUMN_NAME_COUNTRY_PREFIX + ", "
                                    + COLUMN_NAME_NATIONAL_PHONE_NUMBER + ", "
                                    + COLUMN_NAME_AUTH_USER_ID + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String SQL_FIND_ACCOUNT_KIT_BY_AUTHORIZATION_CODE = "SELECT "
                                                        + COLUMN_NAME_ACCOUNT_KIT_ID + ", "
                                                        + COLUMN_NAME_ACCOUNT_KIT_CREATED_TIMESTAMP + ", "
                                                        + COLUMN_NAME_USER_ACCESS_TOKEN + ", "
                                                        + COLUMN_NAME_AUTHORIZATION_CODE + ", "
                                                        + COLUMN_NAME_AUTHORIZATION_CODE_ENCRYPTED + ", "
                                                        + COLUMN_NAME_ACCOUNT_KIT_USER_ID + ", "
                                                        + COLUMN_NAME_COUNTRY_PHONE_NUMBER + ", "
                                                        + COLUMN_NAME_COUNTRY_PREFIX + ", "
                                                        + COLUMN_NAME_NATIONAL_PHONE_NUMBER + ", "
                                                        + COLUMN_NAME_AUTH_USER_ID
                                                        + " FROM "
                                                        + TABLE_NAME_ACCOUNT_KIT
                                                        + " WHERE "
                                                        + COLUMN_NAME_AUTHORIZATION_CODE + " = ?";

    String SQL_FIND_ACCOUNT_KIT_BY_AUTHORIZATION_CODE_ENCRYPTED = "SELECT "
                                                                  + COLUMN_NAME_ACCOUNT_KIT_ID + ", "
                                                                  + COLUMN_NAME_ACCOUNT_KIT_CREATED_TIMESTAMP + ", "
                                                                  + COLUMN_NAME_USER_ACCESS_TOKEN + ", "
                                                                  + COLUMN_NAME_AUTHORIZATION_CODE + ", "
                                                                  + COLUMN_NAME_AUTHORIZATION_CODE_ENCRYPTED + ", "
                                                                  + COLUMN_NAME_ACCOUNT_KIT_USER_ID + ", "
                                                                  + COLUMN_NAME_COUNTRY_PHONE_NUMBER + ", "
                                                                  + COLUMN_NAME_COUNTRY_PREFIX + ", "
                                                                  + COLUMN_NAME_NATIONAL_PHONE_NUMBER + ", "
                                                                  + COLUMN_NAME_AUTH_USER_ID
                                                                  + " FROM "
                                                                  + TABLE_NAME_ACCOUNT_KIT
                                                                  + " WHERE "
                                                                  + COLUMN_NAME_AUTHORIZATION_CODE_ENCRYPTED + " = ?";

    String SQL_FIND_ACCOUNT_KIT_BY_USER_ACCESS_TOKEN = "SELECT "
                                                       + COLUMN_NAME_ACCOUNT_KIT_ID + ", "
                                                       + COLUMN_NAME_ACCOUNT_KIT_CREATED_TIMESTAMP + ", "
                                                       + COLUMN_NAME_USER_ACCESS_TOKEN + ", "
                                                       + COLUMN_NAME_AUTHORIZATION_CODE + ", "
                                                       + COLUMN_NAME_AUTHORIZATION_CODE_ENCRYPTED + ", "
                                                       + COLUMN_NAME_ACCOUNT_KIT_USER_ID + ", "
                                                       + COLUMN_NAME_COUNTRY_PHONE_NUMBER + ", "
                                                       + COLUMN_NAME_COUNTRY_PREFIX + ", "
                                                       + COLUMN_NAME_NATIONAL_PHONE_NUMBER + ", "
                                                       + COLUMN_NAME_AUTH_USER_ID
                                                       + " FROM "
                                                       + TABLE_NAME_ACCOUNT_KIT
                                                       + " WHERE "
                                                       + COLUMN_NAME_USER_ACCESS_TOKEN + " = ?";

    String SQL_UPDATE_ACCOUNT_KIT_AUTH_USER_ID = "UPDATE "
                                                 + TABLE_NAME_ACCOUNT_KIT
                                                 + " SET "
                                                 + COLUMN_NAME_AUTH_USER_ID
                                                 + " = ? WHERE "
                                                 + COLUMN_NAME_ACCOUNT_KIT_ID + " = ?";

    String SQL_UPDATE_ACCOUNT_KIT = "UPDATE "
                                    + TABLE_NAME_ACCOUNT_KIT
                                    + " SET "
                                    + COLUMN_NAME_USER_ACCESS_TOKEN + " = ?, "
                                    + COLUMN_NAME_AUTHORIZATION_CODE + " = ?, "
                                    + COLUMN_NAME_AUTHORIZATION_CODE_ENCRYPTED + " = ?, "
                                    + COLUMN_NAME_ACCOUNT_KIT_USER_ID + " = ?, "
                                    + COLUMN_NAME_COUNTRY_PHONE_NUMBER + " = ?, "
                                    + COLUMN_NAME_COUNTRY_PREFIX + " = ?, "
                                    + COLUMN_NAME_NATIONAL_PHONE_NUMBER + " = ?, "
                                    + COLUMN_NAME_AUTH_USER_ID
                                    + " = ? WHERE "
                                    + COLUMN_NAME_ACCOUNT_KIT_ID + " = ?";

    // Find the pk of the account_kit by auth_user_id,
    // order by created timestamp desc
    String SQL_FIND_ACCOUNT_KIT_ID_BY_AUTH_USER_ID = "SELECT "
                                                     + COLUMN_NAME_ACCOUNT_KIT_ID
                                                     + " FROM "
                                                     + TABLE_NAME_ACCOUNT_KIT
                                                     + " WHERE "
                                                     + COLUMN_NAME_AUTH_USER_ID
                                                     + " = ? ORDER BY "
                                                     + COLUMN_NAME_ACCOUNT_KIT_CREATED_TIMESTAMP + " DESC";

    /* TABLE: COMPUTER_DATA related */

    String TABLE_NAME_COMPUTER_DATA = "computer_data";

    String COLUMN_NAME_COMPUTER_DATA_ID = "computer_data_id";

    String COLUMN_NAME_COMPUTER_DATA_KEY = "computer_data_key";

    String COLUMN_NAME_COMPUTER_DATA_VALUE = "computer_data_value";

    String INDEX_NAME_COMPUTER_DATA_CREATED_TIMESTAMP = "index_computer_data_created_timestamp";

    String INDEX_NAME_COMPUTER_DATA_COMPUTER_DATA_KEY = "index_computer_data_computer_data_key";

    /* DDL - TABLE: COMPUTER_DATA */

    String SQL_CREATE_TABLE_COMPUTER_DATA = "CREATE TABLE "
                                            + TABLE_NAME_COMPUTER_DATA + "("
                                            + COLUMN_NAME_COMPUTER_DATA_ID + " BIGSERIAL PRIMARY KEY, "
                                            + COLUMN_NAME_CREATED_TIMESTAMP + " BIGINT NOT NULL, "
                                            + COLUMN_NAME_COMPUTER_DATA_KEY + " VARCHAR(1024) NOT NULL, "
                                            + COLUMN_NAME_COMPUTER_DATA_VALUE + " VARCHAR(1024) NOT NULL)";

    String SQL_CREATE_INDEX_COMPUTER_DATA_CREATED_TIMESTAMP = "CREATE INDEX " + INDEX_NAME_COMPUTER_DATA_CREATED_TIMESTAMP + " ON " + TABLE_NAME_COMPUTER_DATA + "(" + COLUMN_NAME_CREATED_TIMESTAMP + ")";

    String SQL_CREATE_INDEX_COMPUTER_DATA_COMPUTER_DATA_KEY = "CREATE INDEX " + INDEX_NAME_COMPUTER_DATA_COMPUTER_DATA_KEY + " ON " + TABLE_NAME_COMPUTER_DATA + "(" + COLUMN_NAME_COMPUTER_DATA_KEY + ")";

    String SQL_CREATE_COMPUTER_DATA = "INSERT INTO "
                                      + TABLE_NAME_COMPUTER_DATA + "("
                                      + COLUMN_NAME_CREATED_TIMESTAMP + ", "
                                      + COLUMN_NAME_COMPUTER_DATA_KEY + ", "
                                      + COLUMN_NAME_COMPUTER_DATA_VALUE + ") VALUES (?, ?, ?)";
}
