package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.DeviceToken;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.clopuccino.db.DatabaseConstants.DEFAULT_FILELUG_BUILD_VALUE;
import static org.clopuccino.db.DatabaseConstants.DEFAULT_FILELUG_VERSION_VALUE;

/**
 * <code>DeviceTokenDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class DeviceTokenDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DeviceTokenDao.class.getSimpleName());


    public DeviceTokenDao() {
        super();
    }

    public DeviceTokenDao(DatabaseAccess dbAccess) {
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

            /* check if table purchase exists */
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_DEVICE_TOKEN, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_DEVICE_TOKEN);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_DEVICE_TOKEN_AUTH_USER);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_DEVICE_TOKEN_DEVICE_TOKEN);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_DEVICE_TOKEN_NOTIFICATION_TYPE);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_DEVICE_TOKEN_DEVICE_TYPE);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_DEVICE_TOKEN_AUTH_USER_ID);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nError message:\n%s", DatabaseConstants.TABLE_NAME_DEVICE_TOKEN, e.getMessage()), e);
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

    public DeviceToken createOrUpdateDeviceToken(DeviceToken deviceToken) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        boolean success = true;

//        Long sequenceId = deviceToken.getSequenceId();
        String deviceTokenString = deviceToken.getDeviceToken();
        String notificationType = deviceToken.getNotificationType();
        String deviceType = deviceToken.getDeviceType();
        String deviceVersion = deviceToken.getDeviceVersion();
        String filelugVersion = deviceToken.getFilelugVersion();
        String filelugBuild = deviceToken.getFilelugBuild();
        Integer badgeNumber = deviceToken.getBadgeNumber();
        String account = deviceToken.getAccount();

        try {
            DeviceToken foundDeviceToken = findDeviceTokenByDeviceTokenAndAccount(deviceTokenString, account);

            if (foundDeviceToken == null) {
                // create

                conn = dbAccess.getConnection();

                pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_DEVICE_TOKEN);
                pStatement.setString(1, deviceTokenString);
                pStatement.setString(2, notificationType);
                pStatement.setString(3, deviceType);
                pStatement.setString(4, deviceVersion);
                pStatement.setString(5, filelugVersion != null ? filelugVersion : DEFAULT_FILELUG_VERSION_VALUE);
                pStatement.setString(6, filelugBuild != null ? filelugBuild : DEFAULT_FILELUG_BUILD_VALUE);
                pStatement.setInt(7, badgeNumber != null ? badgeNumber : 0);
                pStatement.setString(8, account);

                pStatement.executeUpdate();
            } else {
                // update

                if (notificationType == null) {
                    notificationType = foundDeviceToken.getNotificationType();
                }

                if (deviceType == null) {
                    deviceType = foundDeviceToken.getDeviceType();
                }

                if (deviceVersion == null) {
                    deviceVersion = foundDeviceToken.getDeviceVersion();
                }

                if (filelugVersion == null || filelugVersion.equals(DEFAULT_FILELUG_VERSION_VALUE)) {
                    filelugVersion = foundDeviceToken.getFilelugVersion();
                }

                if (filelugBuild == null || filelugBuild.equals(DEFAULT_FILELUG_BUILD_VALUE)) {
                    filelugBuild = foundDeviceToken.getFilelugBuild();
                }

                if (badgeNumber == null) {
                    badgeNumber = foundDeviceToken.getBadgeNumber();
                }

                conn = dbAccess.getConnection();

                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_DEVICE_TOKEN_BY_ID);

                pStatement.setString(1, deviceTokenString);
                pStatement.setString(2, notificationType);
                pStatement.setString(3, deviceType);
                pStatement.setString(4, deviceVersion);
                pStatement.setString(5, filelugVersion);
                pStatement.setString(6, filelugBuild);
                pStatement.setInt(7, badgeNumber);
                pStatement.setString(8, account);
                pStatement.setLong(9, foundDeviceToken.getSequenceId());

                pStatement.executeUpdate();
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on create or update deviceToken\n'%s'\nError message:\n%s", deviceToken.toString(), e.getMessage()), e);
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
            return findDeviceTokenByDeviceTokenAndAccount(deviceTokenString, account);
        } else {
            return null;
        }
    }

    public boolean findExistingDeviceTokenByTokenAndUser(String deviceToken, String account) {
        boolean exists = false;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_IF_EXISTS_DEVICE_TOKNE_BY_TOKEN_AND_AUTH_USER_ID);

            pStatement.setString(1, deviceToken);

            pStatement.setString(2, account);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                exists = true;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding if device token exists for device token '%s' and user '%s'\nError message:\n%s", deviceToken, account, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return exists;
    }

    public String findDeviceFilelugVersionByTokenAndUser(String deviceToken, String account) {
        String filelugVersion = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_DEVICE_FILELUG_VERSION_BY_TOKEN_AND_AUTH_USER_ID);

            pStatement.setString(1, deviceToken);

            pStatement.setString(2, account);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                filelugVersion = resultSet.getString(DatabaseConstants.COLUMN_NAME_FILELUG_VERSION);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding device filelug version for device token '%s' and user '%s'\nError message:\n%s", deviceToken, account, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    // ignored
                }
            }
        }

        return filelugVersion;
    }

    public DeviceToken findDeviceTokenByDSequenceId(long deviceTokenSequenceId) {
        DeviceToken record = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_DEVICE_TOKNE_BY_SEQUENCE_ID);

            pStatement.setLong(1, deviceTokenSequenceId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                record = new DeviceToken();

                record.setSequenceId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_DEVICE_TOKEN_SEQUENCE_ID));
                record.setDeviceToken(resultSet.getString(DatabaseConstants.COLUMN_NAME_DEVICE_TOKEN));
                record.setNotificationType(resultSet.getString(DatabaseConstants.COLUMN_NAME_NOTIFICATION_TYPE));
                record.setDeviceType(resultSet.getString(DatabaseConstants.COLUMN_NAME_DEVICE_TYPE));
                record.setDeviceVersion(resultSet.getString(DatabaseConstants.COLUMN_NAME_DEVICE_VERSION));
                record.setFilelugVersion(resultSet.getString(DatabaseConstants.COLUMN_NAME_FILELUG_VERSION));
                record.setFilelugBuild(resultSet.getString(DatabaseConstants.COLUMN_NAME_FILELUG_BUILD));
                record.setBadgeNumber(resultSet.getInt(DatabaseConstants.COLUMN_NAME_BADGE_NUMBER));
                record.setAccount(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
            }
        } catch (Exception e) {
            record = null;

            LOGGER.error(String.format("Error on finding DeviceToken for sequence id '%d'\nError message:\n%s", deviceTokenSequenceId, e.getMessage()), e);
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

    public DeviceToken findDeviceTokenByDeviceTokenAndAccount(String deviceToken, String account) {
        DeviceToken record = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_DEVICE_TOKNE_BY_TOKEN_AND_AUTH_USER_ID);

            pStatement.setString(1, deviceToken);

            pStatement.setString(2, account);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                record = new DeviceToken();

                record.setSequenceId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_DEVICE_TOKEN_SEQUENCE_ID));
                record.setDeviceToken(resultSet.getString(DatabaseConstants.COLUMN_NAME_DEVICE_TOKEN));
                record.setNotificationType(resultSet.getString(DatabaseConstants.COLUMN_NAME_NOTIFICATION_TYPE));
                record.setDeviceType(resultSet.getString(DatabaseConstants.COLUMN_NAME_DEVICE_TYPE));
                record.setDeviceVersion(resultSet.getString(DatabaseConstants.COLUMN_NAME_DEVICE_VERSION));
                record.setFilelugVersion(resultSet.getString(DatabaseConstants.COLUMN_NAME_FILELUG_VERSION));
                record.setFilelugBuild(resultSet.getString(DatabaseConstants.COLUMN_NAME_FILELUG_BUILD));
                record.setBadgeNumber(resultSet.getInt(DatabaseConstants.COLUMN_NAME_BADGE_NUMBER));
                record.setAccount(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
            }
        } catch (Exception e) {
            record = null;

            LOGGER.error(String.format("Error on finding DeviceToken for device token '%s' and user: '%s'\nError message:\n%s", deviceToken, account, e.getMessage()), e);
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

    /* ordered by deviceToken timestamp, DESC */
    public List<DeviceToken> findDeviceTokenssByAccount(String account) {
        List<DeviceToken> deviceTokens = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_DEVICE_TOKENS_BY_AUTH_USER_ID);

            pStatement.setString(1, account);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                DeviceToken record = new DeviceToken();

                record.setSequenceId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_DEVICE_TOKEN_SEQUENCE_ID));
                record.setDeviceToken(resultSet.getString(DatabaseConstants.COLUMN_NAME_DEVICE_TOKEN));
                record.setNotificationType(resultSet.getString(DatabaseConstants.COLUMN_NAME_NOTIFICATION_TYPE));
                record.setDeviceType(resultSet.getString(DatabaseConstants.COLUMN_NAME_DEVICE_TYPE));
                record.setDeviceVersion(resultSet.getString(DatabaseConstants.COLUMN_NAME_DEVICE_VERSION));
                record.setFilelugVersion(resultSet.getString(DatabaseConstants.COLUMN_NAME_FILELUG_VERSION));
                record.setFilelugBuild(resultSet.getString(DatabaseConstants.COLUMN_NAME_FILELUG_BUILD));
                record.setBadgeNumber(resultSet.getInt(DatabaseConstants.COLUMN_NAME_BADGE_NUMBER));
                record.setAccount(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));

                deviceTokens.add(record);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding deviceToken for user '%s'\nerror message:\n%s", account, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return deviceTokens;
    }

    public DeviceToken incrementBadgeNumberBy(long deviceTokenSequenceId, int incrementalBadgeNumber) {

        // FIX: Do not increase badge number until we know exactly how to make it right.

        return findDeviceTokenByDSequenceId(deviceTokenSequenceId);

//        Connection conn = null;
//        PreparedStatement pStatement = null;
//
//        boolean success = true;
//
//        try {
//            DeviceToken foundDeviceToken = findDeviceTokenByDSequenceId(deviceTokenSequenceId);
//
//            if (foundDeviceToken != null) {
//                // update badge number
//
//                int newBadgeNumber = foundDeviceToken.getBadgeNumber() + incrementalBadgeNumber;
//
//                conn = dbAccess.getConnection();
//
//                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_BADEG_NUMBER_BY_DEVICE_TOKEN_ID);
//
//                pStatement.setInt(1, (newBadgeNumber < 0 ? 0 : newBadgeNumber));
//                pStatement.setLong(2, foundDeviceToken.getSequenceId());
//
//                pStatement.executeUpdate();
//            } else {
//                success = false;
//
//                LOGGER.debug(String.format("Device token object not found. Sequence id: '%d'", deviceTokenSequenceId));
//            }
//        } catch (Exception e) {
//            success = false;
//
//            LOGGER.error(String.format("Error on increment/decrement badge number for deviceToken with sequence id: '%d', incremental badge number: '%d'\nError message:\n%s", deviceTokenSequenceId, incrementalBadgeNumber, e.getMessage()), e);
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
//            return findDeviceTokenByDSequenceId(deviceTokenSequenceId);
//        } else {
//            return null;
//        }
    }

    public DeviceToken incrementBadgeNumberBy(String deviceTokenString, String account, int incrementalBadgeNumber) {

        // FIX: Do not increase badge number until we know exactly how to make it right.

        return findDeviceTokenByDeviceTokenAndAccount(deviceTokenString, account);

//        Connection conn = null;
//        PreparedStatement pStatement = null;
//
//        boolean success = true;
//
//        try {
//            DeviceToken foundDeviceToken = findDeviceTokenByDeviceTokenAndAccount(deviceTokenString, account);
//
//            if (foundDeviceToken != null) {
//                // update badge number
//
//                int newBadgeNumber = foundDeviceToken.getBadgeNumber() + incrementalBadgeNumber;
//
//                conn = dbAccess.getConnection();
//
//                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_BADEG_NUMBER_BY_DEVICE_TOKEN_ID);
//
//                pStatement.setInt(1, (newBadgeNumber < 0 ? 0 : newBadgeNumber));
//                pStatement.setLong(2, foundDeviceToken.getSequenceId());
//
//                pStatement.executeUpdate();
//            } else {
//                success = false;
//
//                LOGGER.debug(String.format("Device token object not found. Token: '%s', user: '%s'", deviceTokenString, account));
//            }
//        } catch (Exception e) {
//            success = false;
//
//            LOGGER.error(String.format("Error on increment/decrement badge number for deviceToken: '%s', user: '%s', incremental badge number: '%d'\nError message:\n%s", deviceTokenString, account, incrementalBadgeNumber, e.getMessage()), e);
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
//            return findDeviceTokenByDeviceTokenAndAccount(deviceTokenString, account);
//        } else {
//            return null;
//        }
    }

    public DeviceToken clearBadgeNumberBy(String deviceTokenString, String account) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        boolean success = true;

        try {
            DeviceToken foundDeviceToken = findDeviceTokenByDeviceTokenAndAccount(deviceTokenString, account);

            if (foundDeviceToken != null) {
                // update badge number
                conn = dbAccess.getConnection();

                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_BADEG_NUMBER_BY_DEVICE_TOKEN_ID);

                pStatement.setInt(1, 0);
                pStatement.setLong(2, foundDeviceToken.getSequenceId());

                pStatement.executeUpdate();
            } else {
                success = false;

                LOGGER.debug(String.format("Device token object not found. Device token: '%s', user: '%s'", deviceTokenString, account));
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on clear badge number for device token: '%s', user: '%s'\nError message:\n%s", deviceTokenString, account, e.getMessage()), e);
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
            return findDeviceTokenByDeviceTokenAndAccount(deviceTokenString, account);
        } else {
            return null;
        }
    }
}
