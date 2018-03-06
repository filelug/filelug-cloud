package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.SnsApplication;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>SnsApplicationDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class SnsApplicationDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SnsApplicationDao.class.getSimpleName());

    public SnsApplicationDao() {
        super();
    }

    public SnsApplicationDao(DatabaseAccess dbAccess) {
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
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_SNS_APPLICATION, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_SNS_APPLICATION);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_SNS_APPLICATION_ARN);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_SNS_APPLICATION, e.getMessage()), e);
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

    public SnsApplication findApplicationByPlatform(String platform) {
        SnsApplication record = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SNS_APPLICATION_BY_PLATFORM);

            pStatement.setString(1, platform);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                record = new SnsApplication();

                record.setPlatform(resultSet.getString(DatabaseConstants.COLUMN_NAME_SNS_PLATFORM));
                record.setApplicationArn(resultSet.getString(DatabaseConstants.COLUMN_NAME_SNS_APPLICATION_ARN));
                record.setLastModifiedDate(resultSet.getLong(DatabaseConstants.COLUMN_NAME_SNS_APPLICATION_LAST_MODIFIED_DATE));
            }
        } catch (Exception e) {
            record = null;

            LOGGER.error(String.format("Error on finding SNS application by platform '%s'\nerror message:\n%s", platform, e.getMessage()), e);
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

    public boolean existingApplicationByPlatform(String platform) {
        boolean exists = false;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SNS_APPLICATION_ARN_BY_PLATFORM);

            pStatement.setString(1, platform);

            resultSet = pStatement.executeQuery();

            exists = resultSet.next();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding if SNS application exists by platform '%s'\nerror message:\n%s", platform, e.getMessage()), e);
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

    public List<SnsApplication> findAllSnsApplications() {
        List<SnsApplication> snsApplications = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_ALL_SNS_APPLICATIONS);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                SnsApplication record = new SnsApplication();

                record.setPlatform(resultSet.getString(DatabaseConstants.COLUMN_NAME_SNS_PLATFORM));
                record.setApplicationArn(resultSet.getString(DatabaseConstants.COLUMN_NAME_SNS_APPLICATION_ARN));
                record.setLastModifiedDate(resultSet.getLong(DatabaseConstants.COLUMN_NAME_SNS_APPLICATION_LAST_MODIFIED_DATE));

                snsApplications.add(record);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding all SNS applications\nerror message:\n%s", e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return snsApplications;
    }

    public void deleteSnsApplicationByPlatform(String platform) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_SNS_APPLICATION_BY_PLATFORM);

            pStatement.setString(1, platform);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on deleting SNS application by platform '%s'\nerror message:\n%s", platform, e.getMessage()), e);
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

    public void updateSnsApplication(SnsApplication snsApplication) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_SNS_APPLICATION_BY_PLATFORM);
            pStatement.setString(1, snsApplication.getApplicationArn());
            pStatement.setLong(2, System.currentTimeMillis());

            pStatement.setString(3, snsApplication.getPlatform());

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on updating SNS application: '%s'\nerror message:\n%s", snsApplication.toString(), e.getMessage()), e);
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
     * Creates a SNS application.
     *
     * @param snsApplication The content of the SNS application to be created.
     * @return The created SNS application
     */
    public SnsApplication createSnsApplication(SnsApplication snsApplication) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        String platform = snsApplication.getPlatform();

        boolean success = true;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_SNS_APPLICATION);
            pStatement.setString(1, platform);
            pStatement.setString(2, snsApplication.getApplicationArn());
            pStatement.setLong(3, System.currentTimeMillis());

            pStatement.executeUpdate();
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating SNS application: '%s'\nerror message:\n%s", snsApplication.toString(), e.getMessage()), e);
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
            return findApplicationByPlatform(platform);
        } else {
            return null;
        }
    }
}
