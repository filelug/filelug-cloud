package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.clopuccino.Constants;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.User;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>UserDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class UserDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(UserDao.class.getSimpleName());


    public UserDao() {
        super();
    }

    public UserDao(DatabaseAccess dbAccess) {
        super(dbAccess);
    }

    public boolean createTableIfNotExists() {
        boolean success = true;

        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dbAccess.getConnection();

            DatabaseMetaData dbMetaData = conn.getMetaData();

            /* check if table exists */
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_AUTH_USER, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_AUTH_USER);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_AUTH_USER_COUNTRY_ID);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_AUTH_USER_PHONE_NUMBER);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_AUTH_USER_PHONE_NUMBER_SHOULD_UPDATE);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_AUTH_USER, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(rs, statement, null, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return success;
    }

    /**
     * Finds users by phone number and if checking the value of shouldUpdatePhoneNumber of the user.
     * If the value of skipShouldUpdatePhoneNumber is true, it's possible to have two users returned
     * with the same phone number; if the value of skipShouldUpdatePhoneNumber is set to false, it's
     * possible that more than 2 users may be returned.
     *
     * @param countryId                   The country id
     * @param phoneNumber                 The phone number
     * @param skipShouldUpdatePhoneNumber If true, check the value of shouldUpdatePhoneNumber of the user
     *
     * @return The users found with the phone number and if checking the value of shouldUpdatePhoneNumber of the user.
     */
    public List<User> findUsersByPhone(String countryId, String phoneNumber, boolean skipShouldUpdatePhoneNumber) {
        List<User> users = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            if (skipShouldUpdatePhoneNumber) {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_AUTH_USER_BY_PHONE_WITH_SHOULD_UPDATE_PHONE_NUMBER);

                pStatement.setString(1, countryId);

                pStatement.setString(2, phoneNumber);

                pStatement.setBoolean(3, false);
            } else {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_AUTH_USER_BY_PHONE);

                pStatement.setString(1, countryId);

                pStatement.setString(2, phoneNumber);
            }

            resultSet = pStatement.executeQuery();

            for (;resultSet.next(); ) {
                User user = new User();

                user.setAccount(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
                user.setCountryId(resultSet.getString(DatabaseConstants.COLUMN_NAME_COUNTRY_ID));
                user.setPhoneNumber(resultSet.getString(DatabaseConstants.COLUMN_NAME_PHONE_NUMBER));
                user.setPasswd(resultSet.getString(DatabaseConstants.COLUMN_NAME_PASSWD));
                user.setNickname(resultSet.getString(DatabaseConstants.COLUMN_NAME_NICKNAME));
                user.setShowHidden(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_SHOW_HIDDEN));
                user.setVerified(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_AUTH_USER_VERIFIED));
                user.setVerifyCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_VERIFY_CODE));
                user.setVerifyLetterSentTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP));
                user.setResetPasswordSecurityCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_RESET_PASSWORD_CODE));
                user.setResetPasswordSecurityCodeSentTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_RESET_PASSWORD_CODE_SENT_TIMESTAMP));
                user.setUnverifiedUserEmail(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_EMAIL_NOT_VERIFIED));
                user.setUserEmail(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_EMAIL));
                user.setChangeEmailSecurityCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE));
                user.setChangeEmailSecurityCodeSentTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE_SENT_TIMESTAMP));
                user.setUnverifiedPhoneNumber(resultSet.getString(DatabaseConstants.COLUMN_NAME_PHONE_NUMBER_NOT_VERIFIED));
                user.setChangePhoneNumberSecurityCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE));
                user.setChangePhoneNumberSecurityCodeSentTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP));
                user.setShouldUpdatePhoneNumber(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE));
                user.setAvailableTransferBytes(resultSet.getLong(DatabaseConstants.COLUMN_NAME_AVAILABLE_TRANSFER_BYTES));
                user.setUnlimitedTransfer(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_IS_UNLIMITED_TRANSFER));
                user.setDownloadFileSizeLimitInBytes(resultSet.getLong(DatabaseConstants.COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES));
                user.setUploadFileSizeLimitInBytes(resultSet.getLong(DatabaseConstants.COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES));
                user.setDeletable(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_IS_DELETABLE));

                users.add(user);
            }
        } catch (Exception e) {
            users.clear();

            LOGGER.error(String.format("Error on finding user by country-id '%s' and phone '%s'\nerror message:\n%s", countryId, phoneNumber, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return users;
    }

    /**
     * Same with findUserByPhone(String,String,boolean) but adding a param testPhoneNumberWithZeroPrefix.
     * When testPhoneNumberWithZeroPrefix is set to true, test the phone number, and additional test the phone number prefix with a zero.
     *
     * @param testPhoneNumberWithZeroPrefix If set to true, both the phoneNumber and the one prefix with a zero('0') are used as the condition to find the user.
     */
    public List<User> findUsersByPhone(String countryId, String phoneNumber, boolean skipShouldUpdatePhoneNumber, boolean testPhoneNumberWithZeroPrefix) {
        List<User> users = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            if (skipShouldUpdatePhoneNumber) {
                if (testPhoneNumberWithZeroPrefix) {
                    pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_AUTH_USER_BY_PHONE_WITH_SHOULD_UPDATE_PHONE_NUMBER_AND_ZERO_PREFIX_PHONE_NUMBER);

                    pStatement.setString(1, countryId);

                    pStatement.setString(2, phoneNumber);

                    pStatement.setString(3, String.format("%d%s", 0, phoneNumber));

                    pStatement.setBoolean(4, false);
                } else {
                    pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_AUTH_USER_BY_PHONE_WITH_SHOULD_UPDATE_PHONE_NUMBER);

                    pStatement.setString(1, countryId);

                    pStatement.setString(2, phoneNumber);

                    pStatement.setBoolean(3, false);
                }
            } else {
                if (testPhoneNumberWithZeroPrefix) {
                    pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_AUTH_USER_BY_PHONE_AND_ZERO_PREFIX_PHONE_NUMBER);

                    pStatement.setString(1, countryId);

                    pStatement.setString(2, phoneNumber);

                    pStatement.setString(3, String.format("%d%s", 0, phoneNumber));
                } else {
                    pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_AUTH_USER_BY_PHONE);

                    pStatement.setString(1, countryId);

                    pStatement.setString(2, phoneNumber);
                }
            }

            resultSet = pStatement.executeQuery();

            for (;resultSet.next(); ) {
                User user = new User();

                user.setAccount(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
                user.setCountryId(resultSet.getString(DatabaseConstants.COLUMN_NAME_COUNTRY_ID));
                user.setPhoneNumber(resultSet.getString(DatabaseConstants.COLUMN_NAME_PHONE_NUMBER));
                user.setPasswd(resultSet.getString(DatabaseConstants.COLUMN_NAME_PASSWD));
                user.setNickname(resultSet.getString(DatabaseConstants.COLUMN_NAME_NICKNAME));
                user.setShowHidden(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_SHOW_HIDDEN));
                user.setVerified(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_AUTH_USER_VERIFIED));
                user.setVerifyCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_VERIFY_CODE));
                user.setVerifyLetterSentTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP));
                user.setResetPasswordSecurityCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_RESET_PASSWORD_CODE));
                user.setResetPasswordSecurityCodeSentTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_RESET_PASSWORD_CODE_SENT_TIMESTAMP));
                user.setUnverifiedUserEmail(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_EMAIL_NOT_VERIFIED));
                user.setUserEmail(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_EMAIL));
                user.setChangeEmailSecurityCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE));
                user.setChangeEmailSecurityCodeSentTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE_SENT_TIMESTAMP));
                user.setUnverifiedPhoneNumber(resultSet.getString(DatabaseConstants.COLUMN_NAME_PHONE_NUMBER_NOT_VERIFIED));
                user.setChangePhoneNumberSecurityCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE));
                user.setChangePhoneNumberSecurityCodeSentTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP));
                user.setShouldUpdatePhoneNumber(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE));
                user.setAvailableTransferBytes(resultSet.getLong(DatabaseConstants.COLUMN_NAME_AVAILABLE_TRANSFER_BYTES));
                user.setUnlimitedTransfer(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_IS_UNLIMITED_TRANSFER));
                user.setDownloadFileSizeLimitInBytes(resultSet.getLong(DatabaseConstants.COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES));
                user.setUploadFileSizeLimitInBytes(resultSet.getLong(DatabaseConstants.COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES));
                user.setDeletable(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_IS_DELETABLE));

                users.add(user);
            }
        } catch (Exception e) {
            users.clear();

            LOGGER.error(String.format("Error on finding user by country-id '%s' and phone '%s'\nerror message:\n%s", countryId, phoneNumber, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return users;
    }

    /**
     * Finds users by phone number and if checking the value of shouldUpdatePhoneNumber of the user.
     * The users must have registered and verified.
     * If the value of skipShouldUpdatePhoneNumber is true, it's possible to have two users returned
     * with the same phone number; if the value of skipShouldUpdatePhoneNumber is set to false, it's
     * possible that more than 2 users may be returned.
     *
     * @param countryId                   The country id
     * @param phoneNumber                 The phone number
     * @param skipShouldUpdatePhoneNumber If true, check the value of shouldUpdatePhoneNumber of the user
     *
     * @return The registered and verified users found with the phone number and if checking the value of shouldUpdatePhoneNumber of the user.
     */
    public List<User> findVerifiedUsersByPhone(String countryId, String phoneNumber, boolean skipShouldUpdatePhoneNumber) {
        List<User> users = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            if (skipShouldUpdatePhoneNumber) {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_VERIFIED_AUTH_USER_BY_PHONE_WITH_SHOULD_UPDATE_PHONE_NUMBER);

                pStatement.setString(1, countryId);

                pStatement.setString(2, phoneNumber);

                pStatement.setBoolean(3, false);
            } else {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_VERIFIED_AUTH_USER_BY_PHONE);

                pStatement.setString(1, countryId);

                pStatement.setString(2, phoneNumber);
            }

            resultSet = pStatement.executeQuery();

            for (;resultSet.next(); ) {
                User user = new User();

                user.setAccount(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
                user.setCountryId(resultSet.getString(DatabaseConstants.COLUMN_NAME_COUNTRY_ID));
                user.setPhoneNumber(resultSet.getString(DatabaseConstants.COLUMN_NAME_PHONE_NUMBER));
                user.setPasswd(resultSet.getString(DatabaseConstants.COLUMN_NAME_PASSWD));
                user.setNickname(resultSet.getString(DatabaseConstants.COLUMN_NAME_NICKNAME));
                user.setShowHidden(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_SHOW_HIDDEN));
                user.setVerified(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_AUTH_USER_VERIFIED));
                user.setVerifyCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_VERIFY_CODE));
                user.setVerifyLetterSentTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP));
                user.setResetPasswordSecurityCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_RESET_PASSWORD_CODE));
                user.setResetPasswordSecurityCodeSentTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_RESET_PASSWORD_CODE_SENT_TIMESTAMP));
                user.setUnverifiedUserEmail(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_EMAIL_NOT_VERIFIED));
                user.setUserEmail(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_EMAIL));
                user.setChangeEmailSecurityCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE));
                user.setChangeEmailSecurityCodeSentTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE_SENT_TIMESTAMP));
                user.setUnverifiedPhoneNumber(resultSet.getString(DatabaseConstants.COLUMN_NAME_PHONE_NUMBER_NOT_VERIFIED));
                user.setChangePhoneNumberSecurityCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE));
                user.setChangePhoneNumberSecurityCodeSentTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP));
                user.setShouldUpdatePhoneNumber(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE));
                user.setAvailableTransferBytes(resultSet.getLong(DatabaseConstants.COLUMN_NAME_AVAILABLE_TRANSFER_BYTES));
                user.setUnlimitedTransfer(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_IS_UNLIMITED_TRANSFER));
                user.setDownloadFileSizeLimitInBytes(resultSet.getLong(DatabaseConstants.COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES));
                user.setUploadFileSizeLimitInBytes(resultSet.getLong(DatabaseConstants.COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES));
                user.setDeletable(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_IS_DELETABLE));

                users.add(user);
            }
        } catch (Exception e) {
            users.clear();

            LOGGER.error(String.format("Error on finding user by country-id '%s' and phone '%s'\nerror message:\n%s", countryId, phoneNumber, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return users;
    }

    public List<String> findUserIdsByPhone(String countryId, String phoneNumber, boolean skipShouldUpdatePhoneNumber) {
        List<String> userIds = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            if (skipShouldUpdatePhoneNumber) {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_AUTH_USER_ID_BY_PHONE_WITH_SHOULD_UPDATE_PHONE_NUMBER);

                pStatement.setString(1, countryId);

                pStatement.setString(2, phoneNumber);

                pStatement.setBoolean(3, false);
            } else {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_AUTH_USER_ID_BY_PHONE);

                pStatement.setString(1, countryId);

                pStatement.setString(2, phoneNumber);
            }

            resultSet = pStatement.executeQuery();

            for (;resultSet.next(); ) {
                userIds.add(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
            }
        } catch (Exception e) {
            userIds.clear();

            LOGGER.error(String.format("Error on finding user id by country-id '%s' and phone '%s'\nerror message:\n%s", countryId, phoneNumber, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return userIds;
    }

    public User findUserById(String userId) throws SQLException {
        User user = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_AUTH_USER_BY_ID);

            pStatement.setString(1, userId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                user = new User();

                user.setAccount(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
                user.setCountryId(resultSet.getString(DatabaseConstants.COLUMN_NAME_COUNTRY_ID));
                user.setPhoneNumber(resultSet.getString(DatabaseConstants.COLUMN_NAME_PHONE_NUMBER));
                user.setPasswd(resultSet.getString(DatabaseConstants.COLUMN_NAME_PASSWD));
                user.setNickname(resultSet.getString(DatabaseConstants.COLUMN_NAME_NICKNAME));
                user.setShowHidden(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_SHOW_HIDDEN));
                user.setVerified(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_AUTH_USER_VERIFIED));
                user.setVerifyCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_VERIFY_CODE));
                user.setVerifyLetterSentTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP));
                user.setResetPasswordSecurityCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_RESET_PASSWORD_CODE));
                user.setResetPasswordSecurityCodeSentTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_RESET_PASSWORD_CODE_SENT_TIMESTAMP));
                user.setUnverifiedUserEmail(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_EMAIL_NOT_VERIFIED));
                user.setUserEmail(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_EMAIL));
                user.setChangeEmailSecurityCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE));
                user.setChangeEmailSecurityCodeSentTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_CHANGE_EMAIL_SECURITY_CODE_SENT_TIMESTAMP));
                user.setUnverifiedPhoneNumber(resultSet.getString(DatabaseConstants.COLUMN_NAME_PHONE_NUMBER_NOT_VERIFIED));
                user.setChangePhoneNumberSecurityCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE));
                user.setChangePhoneNumberSecurityCodeSentTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP));
                user.setShouldUpdatePhoneNumber(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_PHONE_NUMBER_SHOULD_UPDATE));
                user.setAvailableTransferBytes(resultSet.getLong(DatabaseConstants.COLUMN_NAME_AVAILABLE_TRANSFER_BYTES));
                user.setUnlimitedTransfer(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_IS_UNLIMITED_TRANSFER));
                user.setDownloadFileSizeLimitInBytes(resultSet.getLong(DatabaseConstants.COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES));
                user.setUploadFileSizeLimitInBytes(resultSet.getLong(DatabaseConstants.COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES));
                user.setDeletable(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_IS_DELETABLE));
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding user '%s'\nerror message:\n%s", userId, e.getMessage()), e);

            throw new SQLException(e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return user;
    }

    public String findNicknameById(String userId) throws SQLException {
        String nickname = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_NICKNAME_BY_USER_ID);

            pStatement.setString(1, userId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                nickname = resultSet.getString(DatabaseConstants.COLUMN_NAME_NICKNAME);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding the nickname of the user '%s'\nerror message:\n%s", userId, e.getMessage()), e);

            throw new SQLException(e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return nickname;
    }

    public boolean findExistsById(String userId) {
        boolean userExists = false;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_IF_EXISTS_AUTH_USER_BY_ID);

            pStatement.setString(1, userId);

            resultSet = pStatement.executeQuery();

            userExists = resultSet.next();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding if user exists by user id '%s'.\nerror message:\n%s", userId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return userExists;
    }

//    public boolean findExistsByPhone(String countryId, String phoneNumber) {
//        boolean userExists = false;
//
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//        ResultSet resultSet = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_AUTH_USER_EXISTS_BY_PHONE);
//
//            pStatement.setString(1, countryId);
//            pStatement.setString(2, phoneNumber);
//
//            resultSet = pStatement.executeQuery();
//
//            userExists = resultSet.next();
//        } catch (Exception e) {
//            LOGGER.error(String.format("Error on finding if user exists by country: '%s' and phone number: '%s'.\nerror message:\n%s", countryId, phoneNumber, e.getMessage()), e);
//        } finally {
//            if (dbAccess != null) {
//                try {
//                    dbAccess.close(resultSet, null, pStatement, conn);
//                } catch (Exception e) {
//                    /* ignored */
//                }
//            }
//        }
//
//        return userExists;
//    }

    public User createUser(User user) throws SQLException {
        Connection conn = null;
        PreparedStatement pStatement = null;

        boolean success = true;

        String userId = user.getAccount();

        try {
            conn = dbAccess.getConnection();

            final String encryptedUserId = DigestUtils.sha256Hex(userId);

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_AUTH_USER);

            pStatement.setString(1, userId);
            pStatement.setString(2, encryptedUserId);
            pStatement.setString(3, user.getCountryId());
            pStatement.setString(4, user.getPhoneNumber());

            pStatement.setString(5, user.getPasswd());
            pStatement.setString(6, user.getNickname());
            pStatement.setBoolean(7, user.getShowHidden());
            pStatement.setBoolean(8, user.getVerified());
            pStatement.setString(9, user.getVerifyCode());
            pStatement.setString(10, user.getUserEmail());
            pStatement.setLong(11, System.currentTimeMillis());

            pStatement.executeUpdate();
        } catch (Exception e) {
            success = false;
            LOGGER.error(String.format("Error on creating user: '%s'\nerror message:\n%s", userId, e.getMessage()), e);

            throw new SQLException(e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        if (success) {
            return findUserById(userId);
        } else {
            return null;
        }
    }

//    public User createUser(User user) {
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//
//        boolean success = true;
//
//        String userId = user.getAccount();
//
//        try {
//            conn = dbAccess.getConnection();
//
//            final String encryptedUserId = DigestUtils.sha256Hex(userId);
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_AUTH_USER);
//            pStatement.setString(1, userId);
//            pStatement.setString(2, encryptedUserId);
//            pStatement.setString(3, user.getCountryId());
//            pStatement.setString(4, user.getPhoneNumber());
//            pStatement.setString(5, user.getPasswd());
//            pStatement.setString(6, user.getNickname());
//            pStatement.setBoolean(7, false);
//            pStatement.setBoolean(8, false);
//            pStatement.setString(9, user.getVerifyCode());
//            // FIX: Be careful that the email may not be verified.
//            pStatement.setString(10, user.getUserEmail());
//            pStatement.executeUpdate();
//        } catch (Exception e) {
//            success = false;
//            LOGGER.error(String.format("Error on creating user: '%s'\nerror message:\n%s", userId, e.getMessage()), e);
//        } finally {
//            if (dbAccess != null) {
//                try {
//                    dbAccess.close(null, null, pStatement, conn);
//                } catch (Exception e) {
//                    /* ignored */
//                }
//            }
//        }
//
//        if (success) {
//            return findUserById(userId);
//        } else {
//            return null;
//        }
//    }

//    public boolean isUnlimitedTransferForUser(String userId) {
//        boolean unlimited = false;
//
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//        ResultSet resultSet = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_IF_UNLIMITED_TRANSFER_BY_USER);
//
//            pStatement.setString(1, userId);
//
//            resultSet = pStatement.executeQuery();
//
//            if (resultSet.next()) {
//                unlimited = resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_IS_UNLIMITED_TRANSFER);
//            }
//        } catch (Exception e) {
//            LOGGER.error(String.format("Error on finding if unlimited transfer for user '%s'\nerror message:\n%s", userId, e.getMessage()), e);
//        } finally {
//            if (dbAccess != null) {
//                try {
//                    dbAccess.close(resultSet, null, pStatement, conn);
//                } catch (Exception e) {
//                    /* ignored */
//                }
//            }
//        }
//
//        return unlimited;
//    }

    public long findAvailableTransferBytesForUser(String userId) {
        long volumes = 0;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_AVAILABLE_TRANSFER_BYTES_BY_USER);

            pStatement.setString(1, userId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                BigDecimal tmpBytes = resultSet.getBigDecimal(DatabaseConstants.COLUMN_NAME_AVAILABLE_TRANSFER_BYTES);

                volumes = tmpBytes.longValue();
//                volumes = resultSet.getLong(DatabaseConstants.COLUMN_NAME_AVAILABLE_TRANSFER_BYTES);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding available transfer bytes for user '%s'\nerror message:\n%s", userId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return volumes;
    }

//    public boolean updateAvailableTransferBytesForUser(String userId, long newAvailableTransferBytes) {
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//
//        boolean success = true;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_AVAILABLE_TRANSFER_BYTES_BY_USER);
//
//            pStatement.setBigDecimal(1, BigDecimal.valueOf(newAvailableTransferBytes));
//            pStatement.setString(2, userId);
//
//            int count = pStatement.executeUpdate();
//
//            if (count > 0) {
//                LOGGER.info(String.format("Update available transfer bytes to %s for user: '%s'", String.valueOf(newAvailableTransferBytes), userId));
//            }
//        } catch (Exception e) {
//            success = false;
//
//            LOGGER.error(String.format("Error on updating available transfer bytes to %s for user: '%s'\nerror message:\n%s", String.valueOf(newAvailableTransferBytes), userId, e.getMessage()), e);
//        } finally {
//            if (dbAccess != null) {
//                try {
//                    dbAccess.close(null, null, pStatement, conn);
//                } catch (Exception e) {
//                    /* ignored */
//                }
//            }
//        }
//
//        return success;
//    }

    public boolean updateUserVerifiedById(String userId, boolean verified) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        boolean success = true;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_AUTH_USER_VERIFIED_BY_ID);

            pStatement.setBoolean(1, verified);
            pStatement.setString(2, userId);

            int count = pStatement.executeUpdate();

            if (count > 0) {
                LOGGER.info(String.format("Update verified to %s for user: '%s'", (verified ? "true" : "false"), userId));
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on updating verified to %s for user: '%s'\nerror message:\n%s", (verified ? "true" : "false"), userId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return success;
    }

    public List<String> findTimeoutUnverifiedUsers() {
        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        List<String> userIds = new ArrayList<>();

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_USERS_BY_VERIFIED_NO_NOT);

            pStatement.setBoolean(1, false);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                long emailSentTimestamp = resultSet.getLong(DatabaseConstants.COLUMN_NAME_VERIFY_CODE_SENT_TIMESTAMP);

                /* check if timeout-unverified */
                if (System.currentTimeMillis() > emailSentTimestamp + (Constants.TIMEOUT_UNVERIFIED_USER_IN_SECONDS * 1000)) {
                    userIds.add(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
                }
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding timeout-unverified users: %s", e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return userIds;
    }

    /**
     * The specified user must be unverified.
     */
    public void deleteUnverifiedUserByPhone(String countryId, String phoneNumber) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_USER_BY_PHONE_NO_CASCADE);

            pStatement.setString(1, countryId);
            pStatement.setString(2, phoneNumber);
            pStatement.setBoolean(3, false);

            int deleted = pStatement.executeUpdate();

            if (deleted > 0) {
                LOGGER.info("Deleting unverified user for country: '" + countryId + "' and phone number: '" + phoneNumber + "'");
            } else {
                LOGGER.warn("Failed to delete unverified user for country: '" + countryId + "' and phone number: '" + phoneNumber + "'");
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on deleting unverified user for country: '%s' and phone number: '%s'\nerror message:\n%s", countryId, phoneNumber, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }
    }

    /**
     * The specified user must be unverified.
     */
    public void deleteUnverifiedUserById(String userId) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_USER_BY_ID_AND_VERIFIED);

            pStatement.setString(1, userId);
            pStatement.setBoolean(2, false);

            int deleted = pStatement.executeUpdate();

            if (deleted > 0) {
                LOGGER.info("Deleted unverified user: '" + userId + "'");
            } else {
                LOGGER.warn("Failed to delete unverified user: '" + userId + "'");
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on deleting unverified user: '%s'\nerror message:\n%s", userId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }
    }

//    public void updateReconnect(String userId, boolean reconnect) {
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_RECONNECT_BY_ID);
//            pStatement.setBoolean(1, reconnect);
//            pStatement.setString(2, userId);
//            pStatement.executeUpdate();
//
//            LOGGER.info("Mark reconnect as " + reconnect + " for user: " + userId);
//        } catch (Exception e) {
//            LOGGER.error("Failed to mark reconnect as " + reconnect + " for user: " + userId, e);
//        } finally {
//            if (dbAccess != null) {
//                try {
//                    dbAccess.close(null, null, pStatement, conn);
//                } catch (Exception e) {
//                        /* ignored */
//                }
//            }
//        }
//    } // updateReconnect(String, Boolean)
//
//    public void asyncMarkReconnect(final String userId, final Boolean reconnect) {
//        Utility.getExecutorService().execute(new Runnable() {
//            @Override
//            public void run() {
//                updateReconnect(userId, reconnect);
//            }
//        });
//    } // end asyncMarkReconnect()

    /**
     *
     * @param userId                        The user id
     * @param newPassword                   The new password
     * @param clearSecurityCodeAndTimestamp Set to true if you want to clear the value of reset_password_code and reset_code_sent_timestamp
     */
    public void updatePassword(String userId, String newPassword, boolean clearSecurityCodeAndTimestamp) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            if (clearSecurityCodeAndTimestamp) {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_AUTH_USER_PASSWORD_AND_CLEAR_RESET_PASSWORD_DATA);
            } else {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_AUTH_USER_PASSWORD);
            }

            pStatement.setString(1, newPassword);
            pStatement.setString(2, userId);
            pStatement.executeUpdate();

            LOGGER.info("Password changed for user: " + userId);
        } catch (Exception e) {
            LOGGER.error("Failed to change password for user: " + userId, e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }
    }

    public void updateNickname(String userId, String nickname) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_AUTH_USER_NICKNAME);
            pStatement.setString(1, nickname);
            pStatement.setString(2, userId);
            pStatement.executeUpdate();

            LOGGER.info("Nickname changed for user: " + userId);
        } catch (Exception e) {
            LOGGER.error("Failed to change nickname for user: " + userId, e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }
    }

    /**
     *
     * @param userId                        The user id
     * @param email                         The new email address
     * @param clearSecurityCodeAndTimestamp Set to true if you want clear the values of auth_user_email_not_verified, change_email_code and change_email_code_sent_timestamp
     */
    public void updateEmail(String userId, String email, boolean clearSecurityCodeAndTimestamp) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            if (clearSecurityCodeAndTimestamp) {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_AUTH_USER_EMAIL_AND_CHANGE_EMAIL_DATA);
            } else {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_AUTH_USER_EMAIL);
            }

            pStatement.setString(1, email);
            pStatement.setString(2, userId);
            pStatement.executeUpdate();

            LOGGER.info("Email changed for user: " + userId);
        } catch (Exception e) {
            LOGGER.error("Failed to change email for user: " + userId, e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }
    }

    public void createOrUpdateUserProfileWithUnverifiedEmail(String userId, String email, String nickname) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_AUTH_USER_UNVERIFIED_EMAIL_AND_NICKNAME);

            pStatement.setString(1, email);
            pStatement.setString(2, nickname);
            pStatement.setString(3, userId);
            pStatement.executeUpdate();

            LOGGER.debug(String.format("Changed unverified email '%s' and nickname '%s' for user '%s' ", email, nickname, userId));
        } catch (Exception e) {
            LOGGER.error(String.format("Error on changing unverified email '%s' and nickname '%s' for user '%s' ", email, nickname, userId), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }
    }

    public void updateVerifyCodeSentTimestamp(String userId, long sentTimestamp) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_VERIFY_CODE_SENT_TIMESTAMP_BY_ID);
            pStatement.setLong(1, sentTimestamp);
            pStatement.setString(2, userId);
            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Error on update verify code sent timestamp. User: " + userId + ", Sent timestamp: " + sentTimestamp, e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                        /* ignored */
                }
            }
        }
    }

    public void updateChangeEmailNewMailAndSecurityCodeSentTimestamp(String securityCode, String newEmail, String userId, long sentTimestamp) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_CHANGE_EMAIL_NEW_MAIL_AND_SECURITY_CODE_SENT_TIMESTAMP_BY_ID);
            pStatement.setString(1, securityCode);
            pStatement.setString(2, newEmail);
            pStatement.setLong(3, sentTimestamp);
            pStatement.setString(4, userId);
            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Error on update change-email security code sent timestamp. Un-verified new email: " + newEmail + ", user: " + userId + ", Sent timestamp: " + sentTimestamp, e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }
    }

    public void updateResetPasswordSecurityCodeSentTimestamp(String userId, long sentTimestamp) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_RESET_PASSWORD_SECURITY_CODE_SENT_TIMESTAMP_BY_ID);
            pStatement.setLong(1, sentTimestamp);
            pStatement.setString(2, userId);
            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Error on update reset-password-security-code sent timestamp. User: " + userId + ", Sent timestamp: " + sentTimestamp, e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                        /* ignored */
                }
            }
        }
    }

    public void updateChangePhoneNumberSecurityCodeSentTimestamp(String userId, long sentTimestamp) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_CHANGE_PHONE_NUMBER_SECURITY_CODE_SENT_TIMESTAMP_BY_ID);
            pStatement.setLong(1, sentTimestamp);
            pStatement.setString(2, userId);
            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Error on update change-phone-number-security-code sent timestamp. User: " + userId + ", Sent timestamp: " + sentTimestamp, e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                        /* ignored */
                }
            }
        }
    }

    public boolean updateResetSecurityCode(String userId, String securityCode) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        boolean success = true;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_AUTH_USER_RESET_SECURITY_CODE_BY_ID);

            pStatement.setString(1, securityCode);
            pStatement.setString(2, userId);

            int count = pStatement.executeUpdate();

            if (count > 0) {
                LOGGER.info(String.format("Update reset security code: to '%s' for user: '%s'", securityCode, userId));
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on updating reset security code to %s for user: '%s'\nerror message:\n%s", securityCode, userId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return success;
    }

    public boolean updateChangePhoneNumberSecurityCode(String userId, String unverifiedPhoneNumber, String securityCode) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        boolean success = true;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_AUTH_USER_CHANGE_PHONE_NUMBER_SECURITY_CODE_BY_ID);

            pStatement.setString(1, unverifiedPhoneNumber);
            pStatement.setString(2, securityCode);
            pStatement.setString(3, userId);

            int count = pStatement.executeUpdate();

            if (count > 0) {
                LOGGER.info(String.format("Update change phone number security code: to '%s' for user: '%s', unverified phone number: '%s'", securityCode, userId, unverifiedPhoneNumber));
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on updating phone number security code to %s for user: '%s', unverified phone number: '%s'\nerror message:\n%s", securityCode, userId, unverifiedPhoneNumber, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return success;
    }

    public void updateShouldUpdatePhoneNumber(String userId, boolean shouldUpdatePhoneNumber) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_AUTH_USER_PHONE_NUMBER_SHOULD_UPDATE);

            pStatement.setBoolean(1, shouldUpdatePhoneNumber);
            pStatement.setString(2, userId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on updating phone_number_should_update to '%s' for user: '%s'\nerror message:\n%s", String.valueOf(shouldUpdatePhoneNumber), userId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }
    }

    /**
     *
     * @param userId                        The user id
     * @param newCountryId                  The new country id
     * @param newPhoneNumber                The new phone number
     * @param clearSecurityCodeAndTimestamp Set to true if you want to clear the values of change_phone_number,
     *                                      change_phone_number_code_sent_timestamp, phone_number_not_verified
     *                                      and set phone_number_should_update to false.
     */
    public void updatePhoneNumberById(String userId, String newCountryId, String newPhoneNumber, boolean clearSecurityCodeAndTimestamp) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            if (clearSecurityCodeAndTimestamp) {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_AUTH_USER_PHONE_NUMBER_AND_CHANGE_PHONE_NUMBER_DATA);
            } else {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_AUTH_USER_PHONE_NUMBER);
            }

            pStatement.setString(1, newCountryId);
            pStatement.setString(2, newPhoneNumber);
            pStatement.setString(3, userId);
            pStatement.executeUpdate();

            LOGGER.info("Country id and phone number changed for user: " + userId);
        } catch (Exception e) {
            LOGGER.error("Failed to change country id and phone number for user: " + userId, e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }
    }

    /**
     * Deletes the user and the related table data, but excludes non-fk-tables such as
     * user_computer_properties and apply_connection
     *
     * @param userId The user id.
     */
    public void deleteUser(String userId) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_USER_BY_ID_AND_VERIFIED);

            pStatement.setString(1, userId);
            pStatement.setBoolean(2, true);

            int deleted = pStatement.executeUpdate();

            if (deleted > 0) {
                LOGGER.info("Deleted user: '" + userId + "'");
            } else {
                LOGGER.warn("Failed to delete user: '" + userId + "'");
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on deleting unverified user: '%s'\nerror message:\n%s", userId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }
    }

    public long findDownloadFileSizeLimitInBytes(String userId) {
        long limit = 0;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES_BY_USER);

            pStatement.setString(1, userId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                limit = resultSet.getLong(DatabaseConstants.COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES);
            } else {
                limit = DatabaseConstants.DEFAULT_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES;
            }
        } catch (Exception e) {
            limit = DatabaseConstants.DEFAULT_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES;

            LOGGER.error(String.format("Error on finding download file size limit in bytes for user '%s'\nerror message:\n%s", userId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return limit;
    }

    public long findUploadFileSizeLimitInBytes(String userId) {
        long limit = 0;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES_BY_USER);

            pStatement.setString(1, userId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                limit = resultSet.getLong(DatabaseConstants.COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES);
            } else {
                limit = DatabaseConstants.DEFAULT_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES;
            }
        } catch (Exception e) {
            limit = DatabaseConstants.DEFAULT_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES;

            LOGGER.error(String.format("Error on finding upload file size limit in bytes for user '%s'\nerror message:\n%s", userId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return limit;
    }

    /**
     * Find download and upload file size limits.
     * @param userId the user id
     * @return The first element is download file size limit and the second is upload file size limit.
     */
    public long[] findDownloadAndUploadFileSizeLimitInBytes(String userId) {
        long[] limits = new long[2];

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_DOWNLOAD_AND_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES_BY_USER);

            pStatement.setString(1, userId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                limits[0] = resultSet.getLong(DatabaseConstants.COLUMN_NAME_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES);
                limits[1] = resultSet.getLong(DatabaseConstants.COLUMN_NAME_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES);
            } else {
                limits[0] = DatabaseConstants.DEFAULT_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES;
                limits[1] = DatabaseConstants.DEFAULT_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES;
            }
        } catch (Exception e) {
            limits[0] = DatabaseConstants.DEFAULT_DOWNLOAD_FILE_SIZE_LIMIT_IN_BYTES;
            limits[1] = DatabaseConstants.DEFAULT_UPLOAD_FILE_SIZE_LIMIT_IN_BYTES;

            LOGGER.error(String.format("Error on finding download and upload file size limits in bytes for user '%s'\nerror message:\n%s", userId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return limits;
    }

//    public boolean updateChangeUserEmailSecurityCode(String userId, String email, String securityCode) {
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//
//        boolean success = true;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_AUTH_USER_CHANGE_EMAIL_SECURITY_CODE_BY_ID);
//
//            pStatement.setString(1, email);
//            pStatement.setString(2, securityCode);
//            pStatement.setLong(3, System.currentTimeMillis());
//            pStatement.setString(4, userId);
//
//            int count = pStatement.executeUpdate();
//
//            if (count > 0) {
//                LOGGER.info(String.format("Update change-user-email security code: to '%s' for email: '%s', user: '%s'", securityCode, email, userId));
//            }
//        } catch (Exception e) {
//            success = false;
//
//            LOGGER.error(String.format("Error on updating change-user-email security code to %s for email: '%s', user: '%s'\nerror message:\n%s", securityCode, email, userId, e.getMessage()), e);
//        } finally {
//            if (dbAccess != null) {
//                try {
//                    dbAccess.close(null, null, pStatement, conn);
//                } catch (Exception e) {
//                    /* ignored */
//                }
//            }
//        }
//
//        return success;
//    }
}
