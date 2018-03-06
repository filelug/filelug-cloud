package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.SMSNotification;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>SMSNotificationDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class SMSNotificationDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SMSNotificationDao.class.getSimpleName());


    public SMSNotificationDao() {
        super();
    }

    public SMSNotificationDao(DatabaseAccess dbAccess) {
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
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_SMS_NOTIFICATION, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_SMS_NOTIFICATION);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_SMS_NOTIFICATION_DELIVER_TIMESTAMP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_SMS_NOTIFICATION_AUTH_USER);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_SMS_NOTIFICATION, e.getMessage()), e);
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

    public SMSNotification findSMSNotificationById(String smsNotificationId) {
        SMSNotification record = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SMS_NOTIFICATION_BY_ID);

            pStatement.setString(1, smsNotificationId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                record = new SMSNotification();

                record.setSmsId(resultSet.getString(DatabaseConstants.COLUMN_NAME_SMS_MESSAGE_ID));
                record.setPhoneNumber(resultSet.getString(DatabaseConstants.COLUMN_NAME_PHONE_NUMBER));
                record.setDeliverTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_DELIVER_TIMESTAMP));
                record.setStatusUpdateTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_STATUS_UPDATE_TIMESTAMP));
                record.setStatus(resultSet.getInt(DatabaseConstants.COLUMN_NAME_STATUS));
                record.setStatusMessage(resultSet.getString(DatabaseConstants.COLUMN_NAME_STATUS_MESSAGE));
                record.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
            }
        } catch (Exception e) {
            record = null;

            LOGGER.error(String.format("Error on finding sms notification for id '%s'\nerror message:\n%s", smsNotificationId, e.getMessage()), e);
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

    public List<SMSNotification> findSMSNotificationsByPhoneNumber(String userId, String phoneNumber) {
        List<SMSNotification> smsNotifications = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SMS_NOTIFICATION_BY_PHONE_NUMBER);

            pStatement.setString(1, userId);
            pStatement.setString(2, phoneNumber);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                SMSNotification record = new SMSNotification();

                record.setSmsId(resultSet.getString(DatabaseConstants.COLUMN_NAME_SMS_MESSAGE_ID));
                record.setPhoneNumber(resultSet.getString(DatabaseConstants.COLUMN_NAME_PHONE_NUMBER));
                record.setDeliverTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_DELIVER_TIMESTAMP));
                record.setStatusUpdateTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_STATUS_UPDATE_TIMESTAMP));
                record.setStatus(resultSet.getInt(DatabaseConstants.COLUMN_NAME_STATUS));
                record.setStatusMessage(resultSet.getString(DatabaseConstants.COLUMN_NAME_STATUS_MESSAGE));
                record.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));

                smsNotifications.add(record);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding sms notification for phone number '%s' and user '%s'\nerror message:\n%s", phoneNumber, userId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return smsNotifications;
    }

    public SMSNotification createSMSNotification(SMSNotification smsNotification) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        boolean success = true;

        String smsNotificationId = smsNotification.getSmsId();
        String userId = smsNotification.getUserId();
        String phoneNumber = smsNotification.getPhoneNumber();

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_SMS_NOTIFICATION);
            pStatement.setString(1, smsNotificationId);
            pStatement.setString(2, phoneNumber);
            pStatement.setLong(3, smsNotification.getDeliverTimestamp());
            pStatement.setLong(4, smsNotification.getStatusUpdateTimestamp());
            pStatement.setInt(5, smsNotification.getStatus());
            pStatement.setString(6, smsNotification.getStatusMessage());
            pStatement.setString(7, userId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on create sms notification\n'%s'\nerror message:\n%s", smsNotification.toString(), e.getMessage()), e);
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
            return findSMSNotificationById(smsNotificationId);
        } else {
            return null;
        }
    }

    public boolean updateSMSNotification(SMSNotification smsNotification) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        boolean success = false;

        String smsNotificationId = smsNotification.getSmsId();
        String phoneNumber = smsNotification.getPhoneNumber();

        try {
            conn = dbAccess.getConnection();

            /* DO NOT UPDATE user id -- it's the foreign key */
            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_SMS_NOTIFICATION_BY_ID);
            pStatement.setString(1, phoneNumber);
            pStatement.setLong(2, smsNotification.getDeliverTimestamp());
            pStatement.setLong(3, smsNotification.getStatusUpdateTimestamp());
            pStatement.setInt(4, smsNotification.getStatus());
            pStatement.setString(5, smsNotification.getStatusMessage());
            pStatement.setString(6, smsNotification.getUserId());
            pStatement.setString(7, smsNotificationId);

            int count = pStatement.executeUpdate();

            success = count > 0;
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on update sms notification\n'%s'\nerror message:\n%s", smsNotification.toString(), e.getMessage()), e);
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
