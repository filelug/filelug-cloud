package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * <code>UserComputerPropertiesDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class UserComputerPropertiesDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(UserComputerPropertiesDao.class.getSimpleName());


    public UserComputerPropertiesDao() {
        super();
    }

    public UserComputerPropertiesDao(DatabaseAccess dbAccess) {
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
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_USER_COMPUTER_PROPERTIES, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_USER_COMPUTER_PROPERTIES);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_USER_COMPUTER_PROPERTIES_USER_COMPUTER);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_USER_COMPUTER_PROPERTIES, e.getMessage()), e);
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

    public Properties findPropertiesByUserComputerId(String userComputerId) {
        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        Properties properties = new Properties();

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_USER_COMPUTER_PROPERTIES_BY_USER_COMPUTER_ID);

            pStatement.setString(1, userComputerId);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                properties.put(
                        resultSet.getString(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_PROPERTY_KEY),
                        resultSet.getString(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_PROPERTY_VALUE));
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding user computer properties for user computer id '%s'\nerror message:\n%s", userComputerId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return properties;
    }

    public void createUserComputerProperties(String userComputerId, Properties properties) {
        Connection conn = null;
        PreparedStatement pStatement = null;
        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_USER_COMPUTER_PROPERTIES);

            Iterator<Map.Entry<Object, Object>> iterator = properties.entrySet().iterator();

            for (; iterator.hasNext(); ) {
                Map.Entry<Object, Object> entry = iterator.next();

                String propertyKey = (String) entry.getKey();
                String propertyValue = (String) entry.getValue();

                pStatement.setString(1, userComputerId);
                pStatement.setString(2, propertyKey);
                pStatement.setString(3, propertyValue);

                pStatement.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on create user computer properties user computer id '%s'\nerror message:\n%s", userComputerId, e.getMessage()), e);
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

    public void deletePropertiesByUserComputerId(String userComputerId) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_USER_COMPUTER_PROPERTIES_BY_USER_COMPUTER_ID);

            pStatement.setString(1, userComputerId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on delete properties for user computer id '%s'\nerror message:\n%s", userComputerId, e.getMessage()), e);
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

    public String findPropertyValue(String userComputerId, String propertyName) {
        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        String propertyValue = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_USER_COMPUTER_PROPERTY_VALUE);

            pStatement.setString(1, userComputerId);
            pStatement.setString(2, propertyName);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                propertyValue = resultSet.getString(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_PROPERTY_VALUE);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding property value for user computer id '%s' and property name '%s'\nerror message:\n%s", userComputerId, propertyName, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return propertyValue;
    }

    public void deletePropertiesByUserId(String userId) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_USER_COMPUTER_PROPERTIES_BY_AUTH_USER_ID);

            pStatement.setString(1, userId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on deleting properties for user id '%s'\nerror message:\n%s", userId, e.getMessage()), e);
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
}
