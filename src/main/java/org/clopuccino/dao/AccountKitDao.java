package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.AccountKit;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * <code>AccountKitDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class AccountKitDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(AccountKitDao.class.getSimpleName());


    public AccountKitDao() {
        super();
    }

    public AccountKitDao(DatabaseAccess dbAccess) {
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
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_ACCOUNT_KIT, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_ACCOUNT_KIT);

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_ACCOUNT_KIT_AUTH_USER);

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_ACCOUNT_KIT_CREATED_TIMESTAMP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_ACCOUNT_KIT_USER_ACCESS_TOKEN);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_ACCOUNT_KIT_AUTHORIZATION_CODE);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_ACCOUNT_KIT_AUTHORIZATION_CODE_ENCRYPTED);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_ACCOUNT_KIT_AUTH_USER_ID);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_ACCOUNT_KIT, e.getMessage()), e);
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

    public AccountKit findAccountKitByAuthorizationCode(String authorizationCode) throws Exception {
        AccountKit record = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_ACCOUNT_KIT_BY_AUTHORIZATION_CODE);

            pStatement.setString(1, authorizationCode);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                record = new AccountKit();

                record.setAccountKitId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_ACCOUNT_KIT_ID));
                record.setCreatedTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_ACCOUNT_KIT_CREATED_TIMESTAMP));
                record.setUserAccessToken(resultSet.getString(DatabaseConstants.COLUMN_NAME_USER_ACCESS_TOKEN));
                record.setAuthorizationCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTHORIZATION_CODE));
                record.setAuthorizationCodeEncrypted(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTHORIZATION_CODE_ENCRYPTED));
                record.setAccountKitUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_ACCOUNT_KIT_USER_ID));
                record.setCountryPhoneNumber(resultSet.getString(DatabaseConstants.COLUMN_NAME_COUNTRY_PHONE_NUMBER));
                record.setCountryPrefix(resultSet.getString(DatabaseConstants.COLUMN_NAME_COUNTRY_PREFIX));
                record.setNationalPhoneNumber(resultSet.getString(DatabaseConstants.COLUMN_NAME_NATIONAL_PHONE_NUMBER));
                record.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding AccountKit by authorization code '%s'\nerror message:\n%s", authorizationCode, e.getMessage()), e);

            throw e;
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return record;
    }

    public AccountKit findAccountKitByEncryptedAuthorizationCode(String encryptedAuthorizationCode) throws Exception {
        AccountKit record = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_ACCOUNT_KIT_BY_AUTHORIZATION_CODE_ENCRYPTED);

            pStatement.setString(1, encryptedAuthorizationCode);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                record = new AccountKit();

                record.setAccountKitId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_ACCOUNT_KIT_ID));
                record.setCreatedTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_ACCOUNT_KIT_CREATED_TIMESTAMP));
                record.setUserAccessToken(resultSet.getString(DatabaseConstants.COLUMN_NAME_USER_ACCESS_TOKEN));
                record.setAuthorizationCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTHORIZATION_CODE));
                record.setAuthorizationCodeEncrypted(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTHORIZATION_CODE_ENCRYPTED));
                record.setAccountKitUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_ACCOUNT_KIT_USER_ID));
                record.setCountryPhoneNumber(resultSet.getString(DatabaseConstants.COLUMN_NAME_COUNTRY_PHONE_NUMBER));
                record.setCountryPrefix(resultSet.getString(DatabaseConstants.COLUMN_NAME_COUNTRY_PREFIX));
                record.setNationalPhoneNumber(resultSet.getString(DatabaseConstants.COLUMN_NAME_NATIONAL_PHONE_NUMBER));
                record.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding AccountKit by encrypted authorization code '%s'\nerror message:\n%s", encryptedAuthorizationCode, e.getMessage()), e);

            throw e;
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return record;
    }

    public AccountKit findAccountKitByUserAccessToken(String userAccessToken) throws Exception {
        AccountKit record = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_ACCOUNT_KIT_BY_USER_ACCESS_TOKEN);

            pStatement.setString(1, userAccessToken);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                record = new AccountKit();

                record.setAccountKitId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_ACCOUNT_KIT_ID));
                record.setCreatedTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_ACCOUNT_KIT_CREATED_TIMESTAMP));
                record.setUserAccessToken(resultSet.getString(DatabaseConstants.COLUMN_NAME_USER_ACCESS_TOKEN));
                record.setAuthorizationCode(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTHORIZATION_CODE));
                record.setAuthorizationCodeEncrypted(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTHORIZATION_CODE_ENCRYPTED));
                record.setAccountKitUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_ACCOUNT_KIT_USER_ID));
                record.setCountryPhoneNumber(resultSet.getString(DatabaseConstants.COLUMN_NAME_COUNTRY_PHONE_NUMBER));
                record.setCountryPrefix(resultSet.getString(DatabaseConstants.COLUMN_NAME_COUNTRY_PREFIX));
                record.setNationalPhoneNumber(resultSet.getString(DatabaseConstants.COLUMN_NAME_NATIONAL_PHONE_NUMBER));
                record.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding AccountKit by user access token '%s'\nerror message:\n%s", userAccessToken, e.getMessage()), e);

            throw e;
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return record;
    }

    /**
     * Creates an AccountKit.
     *
     * @param accountKit The content of the AccountKit to be created.
     * @return true if created successfully; otherwise return false.
     */
    public AccountKit createAccountKit(AccountKit accountKit) throws Exception {
        Connection conn = null;
        PreparedStatement pStatement = null;

        String userAccessToken = accountKit.getUserAccessToken();
        String authorizationCode = accountKit.getAuthorizationCode();
        String authorizationCodeEncrypted = DigestUtils.sha256Hex(authorizationCode);
        String accountKitUserId = accountKit.getAccountKitUserId();
        String countryPhoneNumber = accountKit.getCountryPhoneNumber();
        String countryPrefix = accountKit.getCountryPrefix();
        String nationalPhoneNumber = accountKit.getNationalPhoneNumber();
        String authUserId = accountKit.getUserId();

        boolean created = false;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_ACCOUNT_KIT);

            pStatement.setLong(1, System.currentTimeMillis());
            pStatement.setString(2, userAccessToken);
            pStatement.setString(3, authorizationCode);
            pStatement.setString(4, authorizationCodeEncrypted);

            if (accountKitUserId != null) {
                pStatement.setString(5, accountKitUserId);
            } else {
                pStatement.setNull(5, Types.VARCHAR);
            }

            if (countryPhoneNumber != null) {
                pStatement.setString(6, countryPhoneNumber);
            } else {
                pStatement.setNull(6, Types.VARCHAR);
            }

            if (countryPrefix != null) {
                pStatement.setString(7, countryPrefix);
            } else {
                pStatement.setNull(7, Types.VARCHAR);
            }

            if (nationalPhoneNumber != null) {
                pStatement.setString(8, nationalPhoneNumber);
            } else {
                pStatement.setNull(8, Types.VARCHAR);
            }

            if (authUserId != null) {
                pStatement.setString(9, authUserId);
            } else {
                pStatement.setNull(9, Types.VARCHAR);
            }

            pStatement.executeUpdate();

            created = true;
        } catch (Exception e) {
            LOGGER.error(String.format("Error on creating AccountKit: '%s'\nerror message:\n%s", accountKit, e.getMessage()), e);

            throw e;
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        if (created) {
            return findAccountKitByUserAccessToken(userAccessToken);
        } else {
            return null;
        }
    }

    public boolean updateAccountKit(AccountKit accountKit) throws Exception {
        Connection conn = null;
        PreparedStatement pStatement = null;

        boolean success = false;

        long accountKitId = accountKit.getAccountKitId();
        String userAccessToken = accountKit.getUserAccessToken();
        String authorizationCode = accountKit.getAuthorizationCode();
        String authorizationCodeEncrypted = DigestUtils.sha256Hex(authorizationCode);
//        String authorizationCodeEncrypted = accountKit.getAuthorizationCodeEncrypted();
        String accountKitUserId = accountKit.getAccountKitUserId();
        String countryPhoneNumber = accountKit.getCountryPhoneNumber();
        String countryPrefix = accountKit.getCountryPrefix();
        String nationalPhoneNumber = accountKit.getNationalPhoneNumber();
        String authUserId = accountKit.getUserId();

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_ACCOUNT_KIT);


            pStatement.setString(1, userAccessToken);

            pStatement.setString(2, authorizationCode);

            pStatement.setString(3, authorizationCodeEncrypted);

            if (accountKitUserId != null) {
                pStatement.setString(4, accountKitUserId);
            } else {
                pStatement.setNull(4, Types.VARCHAR);
            }

            if (countryPhoneNumber != null) {
                pStatement.setString(5, countryPhoneNumber);
            } else {
                pStatement.setNull(5, Types.VARCHAR);
            }

            if (countryPrefix != null) {
                pStatement.setString(6, countryPrefix);
            } else {
                pStatement.setNull(6, Types.VARCHAR);
            }

            if (nationalPhoneNumber != null) {
                pStatement.setString(7, nationalPhoneNumber);
            } else {
                pStatement.setNull(7, Types.VARCHAR);
            }

            if (authUserId != null) {
                pStatement.setString(8, authUserId);
            } else {
                pStatement.setNull(8, Types.VARCHAR);
            }

            pStatement.setLong(9, accountKitId);

            int updateCount = pStatement.executeUpdate();

            success = updateCount > 0;
        } catch (Exception e) {
            LOGGER.error(String.format("Error on updating AccountKit: '%s'\nerror message:\n%s", accountKit, e.getMessage()), e);

            throw e;
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

    public boolean updateAuthUserId(long accountKitId, String authUserId) throws Exception {
        Connection conn = null;
        PreparedStatement pStatement = null;

        boolean success = false;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_ACCOUNT_KIT_AUTH_USER_ID);
            pStatement.setString(1, authUserId);
            pStatement.setLong(2, accountKitId);

            int updateCount = pStatement.executeUpdate();

            success = updateCount > 0;
        } catch (Exception e) {
            LOGGER.error(String.format("Error on updating auth user id: '%s' with account kit id: '%d'\nerror message:\n%s", authUserId, accountKitId, e.getMessage()), e);

            throw e;
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
}
