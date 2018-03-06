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
public class ComputerDataDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ComputerDataDao.class.getSimpleName());


    public ComputerDataDao() {
        super();
    }

    public ComputerDataDao(DatabaseAccess dbAccess) {
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
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_COMPUTER_DATA, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_COMPUTER_DATA);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_COMPUTER_DATA_CREATED_TIMESTAMP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_COMPUTER_DATA_COMPUTER_DATA_KEY);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_COMPUTER_DATA, e.getMessage()), e);
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

    public void createComputerProperties(Properties properties) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        long currentTimestamp = System.currentTimeMillis();

        try {

            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_COMPUTER_DATA);

            Iterator<Map.Entry<Object, Object>> iterator = properties.entrySet().iterator();

            for (; iterator.hasNext(); ) {
                Map.Entry<Object, Object> entry = iterator.next();

                String propertyKey = (String) entry.getKey();
                String propertyValue = (String) entry.getValue();

                pStatement.setLong(1, currentTimestamp);
                pStatement.setString(2, propertyKey);
                pStatement.setString(3, propertyValue);

                pStatement.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on creating computer data at '%d'\nerror message:\n%s", currentTimestamp, e.getMessage()), e);
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
