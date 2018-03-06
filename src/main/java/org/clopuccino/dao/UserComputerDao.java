package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.UserComputer;
import org.clopuccino.domain.UserComputerConnectionStatus;
import org.clopuccino.domain.UserComputerProfile;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * <code>ComputerDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class UserComputerDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(UserComputerDao.class.getSimpleName());


    public UserComputerDao() {
        super();
    }

    public UserComputerDao(DatabaseAccess dbAccess) {
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
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_USER_COMPUTER, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_USER_COMPUTER);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_USER_COMPUTER_AUTH_USER);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_USER_COMPUTER_COMPUTER);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_USER_COMPUTER_ENCRYPTED);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_USER_COMPUTER_LUG_SERVER_ID);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_USER_COMPUTER, e.getMessage()), e);
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

    public UserComputer findUserComputerById(String userComputerId) {
        UserComputer record = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_USER_COMPUTER_BY_ID);

            pStatement.setString(1, userComputerId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                record = prepareUserComputerFromResultSet(resultSet);

            }
        } catch (Exception e) {
            record = null;

            LOGGER.error(String.format("Error on finding user computers for id '%s'\nerror message:\n%s", userComputerId, e.getMessage()), e);
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

    public UserComputer findComputerNameByUserComputerId(String userComputerId) {
        UserComputer record = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_USER_COMPUTER_BY_ID);

            pStatement.setString(1, userComputerId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                record = prepareUserComputerFromResultSet(resultSet);

            }
        } catch (Exception e) {
            record = null;

            LOGGER.error(String.format("Error on finding user computers for id '%s'\nerror message:\n%s", userComputerId, e.getMessage()), e);
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

    private UserComputer prepareUserComputerFromResultSet(ResultSet resultSet) throws SQLException {
        UserComputer record = new UserComputer();

        record.setUserComputerId(resultSet.getString(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_ID));
        record.setUserId(resultSet.getString(DatabaseConstants.COLUMN_ALIAS_USER_COMPUTER_AUTH_USER_ID));
        record.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_ALIAS_USER_COMPUTER_COMPUTER_ID));
        record.setComputerAdminId(resultSet.getString(DatabaseConstants.COLUMN_ALIAS_COMPUTER_AUTH_USER_ID));
        record.setGroupName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_GROUP));
        record.setComputerName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME));
        record.setEncryptedUserComputerId(resultSet.getString(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED));
        record.setLugServerId(resultSet.getString(DatabaseConstants.COLUMN_NAME_LUG_SERVER_ID));
        record.setSocketConnected(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_SOCKET_CONNECTED));
        record.setNeedReconnect(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_RECONNECT));
        record.setAllowAlias(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_ALLOW_ALIAS));

        record.setUploadDirectory(resultSet.getString(DatabaseConstants.COLUMN_NAME_UPLOAD_DIRECTORY));

        Integer uploadSubdirectoryType = resultSet.getInt(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_UPLOAD_SUBDIRECTORY_TYPE);

        // Test if the value just got is null. The value is set to 0 for ResultSet to get null value for a column type of Integer
        if (!resultSet.wasNull()) {
            record.setUploadSubdirectoryType(uploadSubdirectoryType);
        }

        record.setUploadSubdirectoryValue(resultSet.getString(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_UPLOAD_SUBDIRECTORY_VALUE));

        Integer uploadDescriptionType = resultSet.getInt(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_UPLOAD_DESCRIPTION_TYPE);

        // Test if the value just got is null. The value is set to 0 for ResultSet to get null value for a column type of Integer
        if (!resultSet.wasNull()) {
            record.setUploadDescriptionType(uploadDescriptionType);
        }

        record.setUploadDescriptionValue(resultSet.getString(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_UPLOAD_DESCRIPTION_VALUE));

        Integer uploadNotificationType = resultSet.getInt(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_UPLOAD_NOTIFICATION_TYPE);

        // Test if the value just got is null. The value is set to 0 for ResultSet to get null value for a column type of Integer
        if (!resultSet.wasNull()) {
            record.setUploadNotificationType(uploadNotificationType);
        }

        record.setDownloadDirectory(resultSet.getString(DatabaseConstants.COLUMN_NAME_DOWNLOAD_DIRECTORY));

        Integer downloadSubdirectoryType = resultSet.getInt(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_TYPE);

        // Test if the value just got is null. The value is set to 0 for ResultSet to get null value for a column type of Integer
        if (!resultSet.wasNull()) {
            record.setDownloadSubdirectoryType(downloadSubdirectoryType);
        }

        record.setDownloadSubdirectoryValue(resultSet.getString(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_VALUE));

        Integer downloadDescriptionType = resultSet.getInt(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_DOWNLOAD_DESCRIPTION_TYPE);

        // Test if the value just got is null. The value is set to 0 for ResultSet to get null value for a column type of Integer
        if (!resultSet.wasNull()) {
            record.setDownloadDescriptionType(downloadDescriptionType);
        }

        record.setDownloadDescriptionValue(resultSet.getString(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_DOWNLOAD_DESCRIPTION_VALUE));

        Integer downloadNotificationType = resultSet.getInt(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_DOWNLOAD_NOTIFICATION_TYPE);

        // Test if the value just got is null. The value is set to 0 for ResultSet to get null value for a column type of Integer
        if (!resultSet.wasNull()) {
            record.setDownloadNotificationType(downloadNotificationType);
        }

        return record;
    }

    public List<UserComputer> findUserComputersByUserId(String userId) {
        List<UserComputer> userComputers = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_USER_COMPUTERS_BY_USER_ID);

            pStatement.setString(1, userId);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                UserComputer record = new UserComputer();

                record.setUserComputerId(resultSet.getString(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_ID));

                record.setUserId(resultSet.getString(DatabaseConstants.COLUMN_ALIAS_USER_COMPUTER_AUTH_USER_ID));
                record.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_ALIAS_USER_COMPUTER_COMPUTER_ID));
                record.setComputerAdminId(resultSet.getString(DatabaseConstants.COLUMN_ALIAS_COMPUTER_AUTH_USER_ID));
                record.setGroupName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_GROUP));
                record.setComputerName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME));
                record.setEncryptedUserComputerId(resultSet.getString(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED));
                record.setLugServerId(resultSet.getString(DatabaseConstants.COLUMN_NAME_LUG_SERVER_ID));
                record.setSocketConnected(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_SOCKET_CONNECTED));
                record.setNeedReconnect(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_RECONNECT));
                record.setAllowAlias(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_ALLOW_ALIAS));

                userComputers.add(record);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding user computers for user id '%s'\nerror message:\n%s", userId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return userComputers;
    }

    public UserComputer createUserComputer(UserComputer userComputer) throws SQLException {
        Connection conn = null;
        PreparedStatement pStatement = null;

        boolean success = true;

        String userComputerId = userComputer.getUserComputerId();
        String userId = userComputer.getUserId();
        Long computerId = userComputer.getComputerId();
        String encrypedUserComputerId = userComputer.getEncryptedUserComputerId();
        String lugServerId = userComputer.getLugServerId();
        Boolean socketConnected = userComputer.isSocketConnected();
        Boolean needReconnect = userComputer.isNeedReconnect();
        Boolean allowAlias = userComputer.isAllowAlias();

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_USER_COMPUTER);
            pStatement.setString(1, userComputerId);
            pStatement.setString(2, userId);
            pStatement.setLong(3, computerId);
            pStatement.setString(4, encrypedUserComputerId);
            pStatement.setString(5, lugServerId);
            pStatement.setBoolean(6, socketConnected != null ? socketConnected : Boolean.FALSE);
            pStatement.setBoolean(7, needReconnect == null ? Boolean.FALSE : needReconnect);
            pStatement.setBoolean(8, allowAlias == null ? Boolean.TRUE : allowAlias);

            // do not add profiles to user_computer when creating because they are all values of null

            pStatement.executeUpdate();
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on create user computer\n'%s'\nerror message:\n%s", userComputer.toString(), e.getMessage()), e);
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
            return findUserComputerById(userComputerId);
        } else {
            return null;
        }
    }

    /**
     * Find reconnect flag and socket_connected for the specified encrypted user computer id.
     *
     * @param encryptedUserComputerId the specified encrypted user computer id
     *
     * @return A boolean object array. The first value is reconnect and the second is socket-connected.
     */
    public Boolean[] findReconnectAndSocketConnectedByEncryptedId(String encryptedUserComputerId) {
        Boolean[] foundBooleans = new Boolean[2];

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_RECONNECT_AND_SOCKET_CONNECTED_BY_ID_ENCRYPTED);
            pStatement.setString(1, encryptedUserComputerId);
            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                foundBooleans[0] = resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_RECONNECT);
                foundBooleans[1] = resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_SOCKET_CONNECTED);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding reconnect and socket-connected for encrypted user computer id '%s'\nerror message:\n%s", encryptedUserComputerId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return foundBooleans;
    }

    /**
     * Find reconnect flag for the specified encrypted user computer id.
     *
     * @param encryptedUserComputerId the specified encrypted user computer id
     *
     * @return true of false of the result. Return null if not found for the specified encrypted user computer id.
     */
    public Boolean findReconnectByEncryptedId(String encryptedUserComputerId) {
        Boolean reconnect = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_RECONNECT_BY_ID_ENCRYPTED);
            pStatement.setString(1, encryptedUserComputerId);
            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                /* found reconnect flag */
                reconnect = resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_RECONNECT);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding reconnect for encrypted user computer id '%s'\nerror message:\n%s", encryptedUserComputerId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return reconnect;
    }

    /**
     * Find reconnect flag for the specified user computer id.
     *
     * @param userComputerId the specified user computer id
     *
     * @return true of false of the result. Return null if not found for the specified user computer id.
     */
    public Boolean findReconnectById(String userComputerId) {
        Boolean reconnect = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_RECONNECT_BY_ID);
            pStatement.setString(1, userComputerId);
            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                /* found reconnect flag */
                reconnect = resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_RECONNECT);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding reconnect for user computer id '%s'\nerror message:\n%s", userComputerId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return reconnect;
    }

    /**
     * Update the reconnect flag for the specified user computer id.
     * If the reconnect is set to true, which means the ConnectSocket for the user computer does not exist,
     * the value of the column socket-connected will be set to false at the same time.
     */
    public void updateReconnectByUserComputerId(String userComputerId, boolean reconnect) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            if (reconnect) {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_RECONNECT_AND_SOCKET_CONNECTED_BY_ID);
                pStatement.setBoolean(1, Boolean.TRUE);
                pStatement.setBoolean(2, Boolean.FALSE);
                pStatement.setString(3, userComputerId);
                pStatement.executeUpdate();

                LOGGER.info("Mark reconnect as true and socket-connected as false for user computer id: " + userComputerId);
            } else {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_RECONNECT_BY_ID);
                pStatement.setBoolean(1, Boolean.FALSE);
                pStatement.setString(2, userComputerId);
                pStatement.executeUpdate();

                LOGGER.info("Mark reconnect as false for user computer id: " + userComputerId);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to mark reconnect as " + reconnect + " for user computer id: " + userComputerId, e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }
    } // updateReconnect(String, Boolean)

    public void updateReconnectByUserId(String userId, Boolean reconnect) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            if (reconnect) {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_RECONNECT_AND_SOCKET_CONNECTED_BY_USER_ID);
                pStatement.setBoolean(1, Boolean.TRUE);
                pStatement.setBoolean(2, Boolean.FALSE);
                pStatement.setString(3, userId);
                pStatement.executeUpdate();

                LOGGER.info("Mark reconnect as true and socket-connected as false for user id: " + userId);
            } else {
                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_RECONNECT_BY_USER_ID);
                pStatement.setBoolean(1, Boolean.FALSE);
                pStatement.setString(2, userId);
                pStatement.executeUpdate();

                LOGGER.info("Mark reconnect as false for user id: " + userId);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to mark reconnect as " + reconnect + " for user id: " + userId, e);
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

//    public void asyncMarkReconnect(final String userComputerId, final Boolean reconnect) {
//        Utility.getExecutorService().execute(new Runnable() {
//            @Override
//            public void run() {
//                updateReconnect(userComputerId, reconnect);
//            }
//        });
//    }

    public void updateLugServerById(String userComputerId, String newLugServerId) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_LUG_SERVER_BY_ID);

            pStatement.setString(1, newLugServerId);
            pStatement.setString(2, userComputerId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on update lug server id '%s' for user computer id '%s'\nerror message:\n%s", newLugServerId, userComputerId, e.getMessage()), e);
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
     * Find if socket from the desktop already connected and the which lug server is connected with the desktop.
     *
     * @param userComputerId the specified user computer id.
     * @return An object array with 2 element:<ol>
     *     <li>If socket connect. Type of Boolean. true if socket already connected.</li>
     *     <li>ID of the lug server. Type of String. null if not found.</li>
     * </ol>
     */
    public Object[] findSocketConnectedAndLugServerIdByUserComputerId(String userComputerId) {
        boolean socketConnected = false;
        String lugServerId = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SOCKET_CONNECTED_AND_LUG_SERVER_ID_BY_ID);

            pStatement.setString(1, userComputerId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                socketConnected = resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_SOCKET_CONNECTED);
                lugServerId = resultSet.getString(DatabaseConstants.COLUMN_NAME_LUG_SERVER_ID);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding socket connected for user computer id '%s'\nerror message:\n%s", userComputerId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return new Object[] {socketConnected, lugServerId};
    }

    public void updateSocketConnectedById(String userComputerId, boolean socketConnected) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_SOCKET_CONNECTED_BY_ID);

            pStatement.setBoolean(1, socketConnected);
            pStatement.setString(2, userComputerId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on update socket connected '%s' for user computer id '%s'\nerror message:\n%s", Boolean.toString(socketConnected), userComputerId, e.getMessage()), e);
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

    // Update connection-related columns:
    // * encrypted user computer id
    // * lug server id
    // * socket connected
    // * need reconnect
    public void updateUserComputerConnectionStatus(UserComputer userComputer) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_USER_COMPUTER_BY_ID);

            pStatement.setString(1, userComputer.getEncryptedUserComputerId());
            pStatement.setString(2, userComputer.getLugServerId());
            pStatement.setBoolean(3, userComputer.isSocketConnected());
            pStatement.setBoolean(4, userComputer.isNeedReconnect());
            pStatement.setString(5, userComputer.getUserComputerId());

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on update user computer connection status for user computer id '%s'\nerror message:\n%s", userComputer.getUserComputerId(), e.getMessage()), e);
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

    public void updateEncryptedUserComputerIdByComputerId(Long computerId, String encryptedUserComputerId) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_USER_COMPUTER_ENCRYPTED_BY_COMPUTER_ID);

            pStatement.setString(1, encryptedUserComputerId);
            pStatement.setLong(2, computerId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on update encrypted user computer id for computer id '%d'\nerror message:\n%s", computerId, e.getMessage()), e);
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

    public boolean findAllowAliasById(String userComputerId) {
        Boolean allowAlias = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_ALLOW_ALIAS_BY_ID);
            pStatement.setString(1, userComputerId);
            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                allowAlias = resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_ALLOW_ALIAS);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding allow-alias for user computer id '%s'\nerror message:\n%s", userComputerId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return allowAlias != null ? allowAlias : false;
    }

    public void updateUploadDirectoryById(String userComputerId, String uploadDirectory) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_UPLOAD_DIRECTORY_BY_ID);

            pStatement.setString(1, uploadDirectory);
            pStatement.setString(2, userComputerId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on update upload directory '%s' for user computer id '%s'\nerror message:\n%s", uploadDirectory, userComputerId, e.getMessage()), e);
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

    public UserComputer findUserComputerByEncryptedId(String encryptedUserComputerId) {
        UserComputer record = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_USER_COMPUTER_BY_ENCRYPTED_ID);

            pStatement.setString(1, encryptedUserComputerId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                record = prepareUserComputerFromResultSet(resultSet);
//                record = new UserComputer();
//
//                record.setUserComputerId(resultSet.getString(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_ID));
//                record.setUserId(resultSet.getString(DatabaseConstants.COLUMN_ALIAS_USER_COMPUTER_AUTH_USER_ID));
//                record.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_ALIAS_USER_COMPUTER_COMPUTER_ID));
//                record.setComputerAdminId(resultSet.getString(DatabaseConstants.COLUMN_ALIAS_COMPUTER_AUTH_USER_ID));
//                record.setGroupName(resultSet.getString(DatabaseConstants.COLUMN_NAME_GROUP_NAME));
//                record.setComputerName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME));
//                record.setEncryptedUserComputerId(resultSet.getString(DatabaseConstants.COLUMN_NAME_USER_COMPUTER_ID_ENCRYPTED));
//                record.setLugServerId(resultSet.getString(DatabaseConstants.COLUMN_NAME_LUG_SERVER_ID));
//                record.setSocketConnected(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_SOCKET_CONNECTED));
//                record.setNeedReconnect(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_RECONNECT));
//                record.setAllowAlias(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_ALLOW_ALIAS));
//                record.setUploadDirectory(resultSet.getString(DatabaseConstants.COLUMN_NAME_UPLOAD_DIRECTORY));
            }
        } catch (Exception e) {
            record = null;

            LOGGER.error(String.format("Error on finding user computers for encrypted id '%s'\nerror message:\n%s", encryptedUserComputerId, e.getMessage()), e);
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

    public List<UserComputerConnectionStatus> findConnectionStatusesByEncryptedUserComputerId(List<String> encryptedUserComputerIds) {
        List<UserComputerConnectionStatus> connectionStatuses = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            String sqlToQuery = DatabaseConstants.SQL_FIND_USER_COMPUTER_CONNECTION_STATUS_BY_ENCRYPTED_IDS_PREFIX + convertToInClauseStringFrom(encryptedUserComputerIds) + DatabaseConstants.SQL_FIND_USER_COMPUTER_CONNECTION_STATUS_BY_ENCRYPTED_IDS_SUFFIX;

            // DEBUG
//            LOGGER.info("Ping3 Qurey:\n" + sqlToQuery);

            pStatement = conn.prepareStatement(sqlToQuery);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                UserComputerConnectionStatus connectionStatus = new UserComputerConnectionStatus();

                connectionStatus.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
                connectionStatus.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID));
                connectionStatus.setSocketConnected(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_SOCKET_CONNECTED));
                connectionStatus.setNeedReconnect(resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_RECONNECT));

                connectionStatuses.add(connectionStatus);
            }
        } catch (Exception e) {
            connectionStatuses = null;

            LOGGER.error(String.format("Error on finding user computers for encrypted ids [%s]\nerror message:\n%s", convertToInClauseStringFrom(encryptedUserComputerIds), e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return connectionStatuses;
    }

    public void updateUserComputerProfilesById(String userComputerId, Map<String, Object> columnAndValueMap) {
        if (columnAndValueMap != null && columnAndValueMap.size() > 0) {
            Connection conn = null;
            PreparedStatement pStatement = null;

            // compose the prepared statement

            StringBuffer sqlStatementBuffer = new StringBuffer(DatabaseConstants.SQL_UPDATE_USER_COMPUTER_PROFILES_BY_ID_1);

            Set<Map.Entry<String, Object>> entrySet = columnAndValueMap.entrySet();

            for (Map.Entry<String, Object> entry : entrySet) {
                Object entryValue = entry.getValue();

                if (Integer.class.isInstance(entryValue)) {
                    sqlStatementBuffer.append(entry.getKey() + " = " + String.valueOf((Integer) entryValue) + ", ");
                } else {
                    sqlStatementBuffer.append(entry.getKey() + " = '" + entryValue.toString() + "', ");
                }
            }

            // remove the last ", " --> length of 2
            int length = sqlStatementBuffer.length();

            sqlStatementBuffer.delete(length - 2, length);

            sqlStatementBuffer.append(DatabaseConstants.SQL_UPDATE_USER_COMPUTER_PROFILES_BY_ID_2);

            String sqlStatement = sqlStatementBuffer.toString();

            // DEBUG
//            LOGGER.info(String.format("SQL to update user computer profiles:\n%s", sqlStatement));

            try {
                conn = dbAccess.getConnection();

                pStatement = conn.prepareStatement(sqlStatement);

                pStatement.setString(1, userComputerId);

                pStatement.executeUpdate();
            } catch (Exception e) {
                LOGGER.error(String.format("Error on update user computer profiles for user computer id '%s'\nstatement:\n%s\nerror message:\n%s", userComputerId, sqlStatement, e.getMessage()), e);
            } finally {
                if (dbAccess != null) {
                    try {
                        dbAccess.close(null, null, pStatement, conn);
                    } catch (Exception e) {
                        //* ignored */
                    }
                }
            }
        }
    }

    public Map<String, Object> findUserComputerProfilesById(String userComputerId, List<UserComputerProfile> userComputerProfiles) {
        Map<String, Object> nameAndValues = new HashMap<>();

        if (userComputerProfiles != null && userComputerProfiles.size() > 0) {
            Connection conn = null;
            PreparedStatement pStatement = null;
            ResultSet resultSet = null;

            try {
                conn = dbAccess.getConnection();

                pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_USER_COMPUTER_BY_ID);

                pStatement.setString(1, userComputerId);

                resultSet = pStatement.executeQuery();

                if (resultSet.next()) {
                    for (UserComputerProfile userComputerProfile : userComputerProfiles) {
                        String propertyName = userComputerProfile.getPropertyName();
                        String columnName = userComputerProfile.getColumnName();
                        Class valueType = userComputerProfile.getType();

                        if (valueType != null) {
                            if (valueType.equals(String.class)) {
                                String columnValue = resultSet.getString(columnName);

                                nameAndValues.put(propertyName, columnValue);
                            } else if (valueType.equals(Integer.class)) {
                                Integer columnValue = resultSet.getInt(columnName);

                                if (!resultSet.wasNull()) {
                                    nameAndValues.put(propertyName, columnValue);
                                }
                            } else if (valueType.equals(Long.class)) {
                                Long columnValue = resultSet.getLong(columnName);

                                if (!resultSet.wasNull()) {
                                    nameAndValues.put(propertyName, columnValue);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(String.format("Error on finding user computers for id '%s'\nerror message:\n%s", userComputerId, e.getMessage()), e);
            } finally {
                if (dbAccess != null) {
                    try {
                        dbAccess.close(resultSet, null, pStatement, conn);
                    } catch (Exception e) {
                    /* ignored */
                    }
                }
            }
        }

        return nameAndValues;
    }

    public void deleteUserComputerById(String userComputerId) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_USER_COMPUTER_BY_ID);

            pStatement.setString(1, userComputerId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on deleting user computer with user computer id '%s'%nerror message:%n%s", userComputerId, e.getMessage()), e);
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

    public void deleteUserComputerByComputerId(long computerId) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_USER_COMPUTER_BY_COMPUTER_ID);

            pStatement.setLong(1, computerId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on deleting user computer with computer id '%d'%nerror message:%n%s", computerId, e.getMessage()), e);
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
