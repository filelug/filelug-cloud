package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.ApplyConnection;
import org.clopuccino.domain.User;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>ComputerDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ApplyConnectionDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ApplyConnectionDao.class.getSimpleName());


    public ApplyConnectionDao() {
        super();
    }

    public ApplyConnectionDao(DatabaseAccess dbAccess) {
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
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_APPLY_CONNECTION, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_APPLY_CONNECTION);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_APPLY_CONNECTION_AUTH_USER);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_APPLY_CONNECTION_APPROVED_AUTH_USER);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_APPLY_CONNECTION_COMPUTER);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_APPLY_CONNECTION_APPLY_TIMESTAMPE);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_APPLY_CONNECTION_APPROVED);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_APPLY_CONNECTION_APPROVED_AUTH_USER_ID);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_APPLY_CONNECTION_APPROVED_TIMESTAMP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_APPLY_CONNECTION_AUTH_USER_ID);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_APPLY_CONNECTION_COMPUTER_ID);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_APPLY_CONNECTION, e.getMessage()), e);
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

    public List<User> findApprovedUsersByComputerId(Long computerId) throws SQLException {
        List<User> users = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_APPROVED_APPLY_CONNECTION_USERS_BY_COMPUTER_ID);

            pStatement.setLong(1, computerId);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
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
                user.setAvailableTransferBytes(resultSet.getLong(DatabaseConstants.COLUMN_NAME_AVAILABLE_TRANSFER_BYTES));
                user.setUnlimitedTransfer(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_IS_UNLIMITED_TRANSFER));

                users.add(user);
            }
        } catch (Exception e) {
            users.clear();

            String errorMessage = String.format("Error on finding users of approved apply-connection for computer id '%d'\nerror message:\n%s", computerId, e.getMessage());

            LOGGER.error(errorMessage, e);

            throw new SQLException(errorMessage);
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
     * Creates a apply-connection if not any one with the same apply user id and computer id;
     * if exists, update with the apply-timestamp, approved, approved user id, and approved timestamp.
     *
     * @param applyConnection The content of the apply-connection to be created or updated.
     * @return The created or updated apply-connection
     */
    public ApplyConnection createOrUpdateApplyConnection(ApplyConnection applyConnection) {
        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        boolean success = true;

        Long applyTimestamp = applyConnection.getApplyTimestamp();
        String applyUserId = applyConnection.getApplyUser();
        Long computerId = applyConnection.getComputerId();
        Boolean approved = applyConnection.getApproved();
        String approvedUserId = applyConnection.getApprovedUser();
        Long approvedTimestamp = applyConnection.getApprovedTimestamp();

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_APPLY_CONNECTION_BY_APPLY_USER_AND_COMPUTER_ID);

            pStatement.setString(1, applyUserId);
            pStatement.setLong(2, computerId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                // exists -> update

                Long applyConnectionId = resultSet.getLong(DatabaseConstants.COLUMN_NAME_APPLY_CONNECTION_ID);

                pStatement.close();

                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_APPLY_CONNECTION);

                pStatement.setLong(1, applyTimestamp);
                pStatement.setBoolean(2, approved);
                pStatement.setString(3, approvedUserId);
                pStatement.setLong(4, approvedTimestamp);
                pStatement.setLong(5, applyConnectionId);

                pStatement.executeUpdate();
            } else {
                // not exists -> create

                pStatement.close();

                pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_APPLY_CONNECTION);

                pStatement.setLong(1, applyTimestamp);
                pStatement.setString(2, applyUserId);
                pStatement.setLong(3, computerId);
                pStatement.setBoolean(4, approved);
                pStatement.setString(5, approvedUserId);
                pStatement.setLong(6, approvedTimestamp);

                pStatement.executeUpdate();
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on create apply-connection: '%s'\nerror message:\n%s", applyConnection.toString(), e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        if (success) {
            return findApplyConnectionByApplyUserAndComputerId(applyUserId, computerId);
        } else {
            return null;
        }
    }

    public ApplyConnection findApplyConnectionByApplyUserAndComputerId(String applyUserId, Long computerId) {
        ApplyConnection applyConnection = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_APPLY_CONNECTION_BY_APPLY_USER_AND_COMPUTER_ID);

            pStatement.setString(1, applyUserId);
            pStatement.setLong(2, computerId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                applyConnection = new ApplyConnection();

                applyConnection.setApplyConnectionId(resultSet.getString(DatabaseConstants.COLUMN_NAME_APPLY_CONNECTION_ID));
                applyConnection.setApplyTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_LATEST_APPLY_TIMESTAMP));
                applyConnection.setApplyUser(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
                applyConnection.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID));
                applyConnection.setApproved(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_APPROVED));
                applyConnection.setApprovedUser(resultSet.getString(DatabaseConstants.COLUMN_NAME_APPROVED_AUTH_USER_ID));
                applyConnection.setApprovedTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_LATEST_APPLY_TIMESTAMP));
                // DO NOT ADD COMPUTER_NAME to prevent error on desktop.
//                applyConnection.setComputerName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME));
            }
        } catch (Exception e) {
            applyConnection = null;

            LOGGER.error(String.format("Error on finding apply-connection for apply user id '%s'\ncomputer id '%d'\nerror message:\n%s", applyUserId, computerId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return applyConnection;
    }

    /**
     *
     * @param adminUserId          The administrator user id
     * @param excludeSelfApproved  If set to true, exclude the data that administrator user id is the same with approved user id.
     * @return                     The <code>ApplyConnection</code>s that meet the criteria.
     */
    public List<ApplyConnection> findByAdminUserId(String adminUserId, boolean excludeSelfApproved) {
        List<ApplyConnection> applyConnections = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            if (excludeSelfApproved) {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_APPLY_CONNECTIONS_BY_ADMIN_USER_ID_EXCLUDE_SELF_APPROVED);
            } else {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_APPLY_CONNECTIONS_BY_ADMIN_USER_ID);
            }

            pStatement.setString(1, adminUserId);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                ApplyConnection applyConnection = new ApplyConnection();

                applyConnection.setApplyConnectionId(resultSet.getString(DatabaseConstants.COLUMN_NAME_APPLY_CONNECTION_ID));
                applyConnection.setApplyTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_LATEST_APPLY_TIMESTAMP));
                applyConnection.setApplyUser(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
                applyConnection.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID));
                applyConnection.setApproved(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_APPROVED));
                applyConnection.setApprovedUser(resultSet.getString(DatabaseConstants.COLUMN_NAME_APPROVED_AUTH_USER_ID));
                applyConnection.setApprovedTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_LATEST_APPLY_TIMESTAMP));
                applyConnection.setComputerName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME));

                applyConnections.add(applyConnection);
            }
        } catch (Exception e) {
            applyConnections.clear();

            LOGGER.error(String.format("Error on finding apply-connections for admin user id '%s'\nerror message:\n%s", adminUserId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return applyConnections;
    }

    public void deleteByUserWithAdminOrApproved(String userId) throws Exception {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_APPLY_CONNECTION_BY_AUTH_USER_ID_OR_APPROVED_AUTH_USER_ID);

            pStatement.setString(1, userId);
            pStatement.setString(2, userId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on deleting apply-connections that auth user is '%s' or approved user id is '%s'\nerror message:\n%s", userId, userId, e.getMessage()), e);

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
    }

    public void deleteApprovedUsersByComputerId(Long computerId) throws Exception {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_APPLY_CONNECTION_BY_COMPUTER_ID);

            pStatement.setLong(1, computerId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on deleting apply-connections by computer id '%d'\nerror message:\n%s", computerId, e.getMessage()), e);

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
    }
}
