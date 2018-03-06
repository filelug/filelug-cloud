package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * <code>FilelugPropertiesDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class FilelugPropertiesDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FilelugPropertiesDao.class.getSimpleName());

    public FilelugPropertiesDao() {
        super();
    }

    public FilelugPropertiesDao(DatabaseAccess dbAccess) {
        super(dbAccess);
    }
    
    /**
     * Checks if filelugProperties table exists.
     *
     * @return true if filelugProperties table exists.
     */
    public boolean isFilelugPropertiesTableExists() {
        boolean exists = true;

        Connection conn = null;
        ResultSet rs = null;

        try {
            conn = dbAccess.getConnection();

            DatabaseMetaData dbMetaData = conn.getMetaData();

            /* check if table exists */
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_FILELUG_PROPERTIES, new String[]{"TABLE"});

            exists = rs.next();
        } catch (Exception e) {
            exists = false;

            LOGGER.error(String.format("Error on checking if table '%s' exists.\nerror message:\n%s", DatabaseConstants.TABLE_NAME_FILELUG_PROPERTIES, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(rs, null, null, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return exists;
    }

    /**
     * Create table filelugProperties if not exists.
     *
     * @return true if created success or already exists; otherwise false.
     */
    public boolean createTableIfNotExists() {
        boolean success = true;

        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dbAccess.getConnection();

            DatabaseMetaData dbMetaData = conn.getMetaData();

            /* check if table exists */
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_FILELUG_PROPERTIES, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_FILELUG_PROPERTIES);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_FILELUG_PROPERTIES, e.getMessage()), e);
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

    public boolean recreateDefaultFilelugProperties() {
        boolean success = true;

        Connection conn = null;
        Statement statement = null;
        PreparedStatement pStatement = null;

        try {
            ClassLoader classLoader = getClass().getClassLoader();

            InputStream propertiesInputStream = classLoader.getResourceAsStream("/" + DatabaseConstants.FILE_NAME_DEFAULT_FILELUG_PROPERTIES);

            if (propertiesInputStream == null) {
                propertiesInputStream = classLoader.getResourceAsStream(DatabaseConstants.FILE_NAME_DEFAULT_FILELUG_PROPERTIES);
            }

            if (propertiesInputStream == null) {
                throw new FileNotFoundException("Filelug default properties file not found: " + DatabaseConstants.FILE_NAME_DEFAULT_FILELUG_PROPERTIES);
            }

            conn = dbAccess.getConnection();

            statement = conn.createStatement();

            /* delete all existing */
            statement.executeUpdate(DatabaseConstants.SQL_TRUNCATE_TABLE_FILELUG_PROPERTIES);

            LOGGER.debug("Truncate table filelug_properties successfully before re-creating data.");

            /* create defaults */
            Properties defaultProperties = new Properties();
            defaultProperties.load(propertiesInputStream);

            Set<Map.Entry<Object, Object>> entries = defaultProperties.entrySet();

            if (entries != null && entries.size() > 0) {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_FILELUG_PROPERTIES);

                for (Map.Entry<Object, Object> entry : entries) {
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();

                    pStatement.setString(1, key);
                    pStatement.setString(2, value);

                    pStatement.executeUpdate();
                }

                pStatement.close();
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating or updating filelug default properties data.\nerror message:\n%s", e.getMessage()), e);
        } finally {
            try {
                dbAccess.close(null, statement, pStatement, conn);
            } catch (Exception e) {
                /* ignored */
            }
        }

        return success;
    }

    public String findValueByKey(String key) {
        String value = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILELUG_PROPERTIES_BY_KEY);

            pStatement.setString(1, key);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                value = resultSet.getString(DatabaseConstants.COLUMN_NAME_FILELUG_PROPERTY_VALUE);
            }
        } catch (Exception e) {
            value = null;

            LOGGER.error(String.format("Error on finding filelug property value for key '%s'\nerror message:\n%s", key, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return value;
    }
}
