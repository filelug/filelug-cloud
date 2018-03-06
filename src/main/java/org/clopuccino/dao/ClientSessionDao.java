package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.ClientSession;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>ClientSessionDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ClientSessionDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ComputerDao.class.getSimpleName());

    public ClientSessionDao() {
        super();
    }

    public ClientSessionDao(DatabaseAccess dbAccess) {
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
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_CLIENT_SESSION, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_CLIENT_SESSION);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_CLIENT_SESSION_AUTH_USER);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_CLIENT_SESSION_REPLACED_BY);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_CLIENT_SESSION_AUTH_USER_ID);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_CLIENT_SESSION_COMPUTER_ID);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_CLIENT_SESSION_LAST_ACCESS_TIMESTAMP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_CLIENT_SESSION_REPLACED_BY);
//            } else {
//                // add column device_token if not exists
//                rs.close();
//
//                rs = dbMetaData.getColumns(null, null, DatabaseConstants.TABLE_NAME_CLIENT_SESSION, DatabaseConstants.COLUMN_NAME_DEVICE_TOKEN);
//
//                if (!rs.next()) {
//                    statement = conn.createStatement();
//
//                    statement.executeUpdate(DatabaseConstants.SQL_ADD_COLUMN_CLIENT_SESSION_DEVICE_TOKEN);
//                }
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_CLIENT_SESSION, e.getMessage()), e);
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

    public void createClientSession(ClientSession clientSession) throws Exception {
        Connection conn = null;
        PreparedStatement pStatement = null;

        String sessionId = clientSession.getSessionId();
        Long computerId = clientSession.getComputerId();
        String userId = clientSession.getUserId();
        String deviceToken = clientSession.getDeviceToken();
        Long lastAccessTimestamp = clientSession.getLastAccessTime();
        Boolean showHidden = clientSession.isShowHidden();
        String locale = clientSession.getLocale();

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_CLIENT_SESSION);
            pStatement.setString(1, sessionId);
            pStatement.setLong(2, computerId);
            pStatement.setString(3, userId);
            pStatement.setString(4, (deviceToken != null ? deviceToken : ""));
            pStatement.setLong(5, lastAccessTimestamp);
            pStatement.setBoolean(6, showHidden);
            pStatement.setString(7, locale != null ? locale : DatabaseConstants.DEFAULT_CLIENT_LOCALE);

            pStatement.executeUpdate();

            LOGGER.debug(String.format("Created client session: '%s'", clientSession.toString()));
        } catch (Exception e) {
            LOGGER.error(String.format("Error on create client session: '%s'\nerror message:\n%s", clientSession.toString(), e.getMessage()), e);

            throw new Exception(ClopuccinoMessages.localizedMessage(locale, "connection.failed.try.again"));
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

    public boolean existingClientSessionById(String sessionId) {
        boolean exists = false;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_CLIENT_SESSION_BY_ID);

            pStatement.setString(1, sessionId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                exists = true;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding if client session exists for id '%s'\nerror message:\n%s", sessionId, e.getMessage()), e);
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

    public void deleteClientSessionsByUser(String userId) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_CLIENT_SESSIONS_BY_USER_ID);

            pStatement.setString(1, userId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on deleting client session for user '%s'\nerror message:\n%s", userId, e.getMessage()), e);
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

    public void deleteClientSessionsByComputer(Long computerId) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_CLIENT_SESSIONS_BY_COMPUTER);

            pStatement.setLong(1, computerId);
//            pStatement.setString(1, computerGroup);
//            pStatement.setString(2, computerName);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on deleting client session for computer id '%d'\nerror message:\n%s", computerId, e.getMessage()), e);
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

//    public Set<ClientSession> findClientSessionsByUserComputer(String userId, Long computerId) {
//        Set<ClientSession> clientSessions = new HashSet<>();
//
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//        ResultSet resultSet = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_CLIENT_SESSIONS_BY_USER_COMPUTER);
//
//            pStatement.setString(1, userId);
//            pStatement.setLong(2, computerId);
////            pStatement.setString(2, computerGroup);
////            pStatement.setString(3, computerName);
//
//            resultSet = pStatement.executeQuery();
//
//            for (; resultSet.next(); ) {
//                String sessionId = resultSet.getString(DatabaseConstants.COLUMN_NAME_CLIENT_SESSION_ID);
////                String foundComputerGroup = resultSet.getString(DatabaseConstants.COLUMN_NAME_GROUP_NAME);
////                String foundComputerName = resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME);
//                String foundUserId = resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID);
//                Long lastAccessTimestamp = resultSet.getLong(DatabaseConstants.COLUMN_NAME_LAST_ACCESS_TIMESTAMP);
//                Boolean showHidden = resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_SHOW_HIDDEN);
//                String locale = resultSet.getString(DatabaseConstants.COLUMN_NAME_CLIENT_LOCALE);
//
//                clientSessions.add(new ClientSession(computerId, foundUserId, sessionId, lastAccessTimestamp, showHidden, locale));
//            }
//        } catch (Exception e) {
//            LOGGER.error(String.format("Error on finding client sessions for user id '%s', computer id '%d'\nerror message:\n%s", userId, computerId, e.getMessage()), e);
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
//        return clientSessions;
//    }

//    public Set<String> findClientSessionIdsByUserComputer(String userId, Long computerId) {
//        Set<String> sessionIds = new HashSet<>();
//
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//        ResultSet resultSet = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_CLIENT_SESSION_IDS_BY_USER_COMPUTER);
//
//            pStatement.setString(1, userId);
//            pStatement.setLong(2, computerId);
////            pStatement.setString(2, computerGroup);
////            pStatement.setString(3, computerName);
//
//            resultSet = pStatement.executeQuery();
//
//            for (; resultSet.next(); ) {
//                sessionIds.add(resultSet.getString(DatabaseConstants.COLUMN_NAME_CLIENT_SESSION_ID));
//            }
//        } catch (Exception e) {
//            LOGGER.error(String.format("Error on finding client session ids for user id '%s', computer id '%d'\nerror message:\n%s", userId, computerId, e.getMessage()), e);
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
//        return sessionIds;
//    }

    public void deleteClientSessionsByLastAccessTimeStampSmallerThan(long timestamp) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_CLIENT_SESSIONS_BY_LAST_ACCESS_TIMESTAMP_SMALLER_THAN);

            pStatement.setLong(1, timestamp);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on deleting client session for last access timestamp small than '%s'\nerror message:\n%s", String.valueOf(timestamp), e.getMessage()), e);
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

    public ClientSession findClientSessionById(String sessionId) throws Exception {
        ClientSession clientSession = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_CLIENT_SESSION_BY_ID);

            pStatement.setString(1, sessionId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                String foudnSessionId = resultSet.getString(DatabaseConstants.COLUMN_NAME_CLIENT_SESSION_ID);
                Long computerId = resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID);
                String foundUserId = resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID);
                String deviceToken = resultSet.getString(DatabaseConstants.COLUMN_NAME_DEVICE_TOKEN);
                Long lastAccessTimestamp = resultSet.getLong(DatabaseConstants.COLUMN_NAME_LAST_ACCESS_TIMESTAMP);
                Boolean showHidden = resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_SHOW_HIDDEN);
                String locale = resultSet.getString(DatabaseConstants.COLUMN_NAME_CLIENT_LOCALE);

                clientSession = new ClientSession(computerId, foundUserId, foudnSessionId, deviceToken, lastAccessTimestamp, showHidden, locale);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding client session for session id '%s'\nerror message:\n%s", sessionId, e.getMessage()), e);

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

        return clientSession;
    }

    public List<String> findClientSessionIdByReplacedBy(String replacedBySesionId) throws Exception {
        List<String> sessionIds = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_CLIENT_SESSION_ID_BY_REPLACED_BY);

            pStatement.setString(1, replacedBySesionId);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                sessionIds.add(resultSet.getString(DatabaseConstants.COLUMN_NAME_CLIENT_SESSION_ID));
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding client session for replaced-by-session-id '%s'\nerror message:\n%s", replacedBySesionId, e.getMessage()), e);

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

        return sessionIds;
    }

//    public Set<ClientSession> findAllClientSessions() {
//        Set<ClientSession> clientSessions = new HashSet<>();
//
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//        ResultSet resultSet = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_ALL_CLIENT_SESSIONS);
//
//            resultSet = pStatement.executeQuery();
//
//            for (; resultSet.next(); ) {
//                String sessionId = resultSet.getString(DatabaseConstants.COLUMN_NAME_CLIENT_SESSION_ID);
//                Long computerId = resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID);
////                String computerGroup = resultSet.getString(DatabaseConstants.COLUMN_NAME_GROUP_NAME);
////                String computerName = resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME);
//                String userId = resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID);
//                Long lastAccessTimestamp = resultSet.getLong(DatabaseConstants.COLUMN_NAME_LAST_ACCESS_TIMESTAMP);
//                Boolean showHidden = resultSet.getBoolean(DatabaseConstants.COLUMN_NAME_SHOW_HIDDEN);
//                String locale = resultSet.getString(DatabaseConstants.COLUMN_NAME_CLIENT_LOCALE);
//
//                clientSessions.add(new ClientSession(computerId, userId, sessionId, lastAccessTimestamp, showHidden, locale));
//            }
//        } catch (Exception e) {
//            LOGGER.error(String.format("Error on finding all client sessions.\nerror message:\n%s", e.getMessage()), e);
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
//        return clientSessions;
//    }

    public void updateClientSessionLastAccessTimestamp(String sessionId, long newLastAccessTimestamp) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_CLIENT_SESSIONS_LAST_ACCESS_TIMESTAMP_BY_ID);

            pStatement.setLong(1, newLastAccessTimestamp);
            pStatement.setString(2, sessionId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on update last access timestamp of the client session with session id '%s'\nerror message:\n%s", sessionId, e.getMessage()), e);
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

    public void updateClientSessionDeviceToken(String sessionId, String deviceToken) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_CLIENT_SESSIONS_DEVICE_TOKEN_BY_ID);

            pStatement.setString(1, deviceToken);
            pStatement.setString(2, sessionId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on update device token of the client session with session id '%s'\nerror message:\n%s", sessionId, e.getMessage()), e);
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

    public void updateClientSession(ClientSession clientSession) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_CLIENT_SESSION);

            pStatement.setLong(1, clientSession.getComputerId());
            pStatement.setString(2, clientSession.getDeviceToken());
            pStatement.setLong(3, clientSession.getLastAccessTime());
            pStatement.setBoolean(4, clientSession.isShowHidden());
            pStatement.setString(5, clientSession.getLocale());
            pStatement.setString(6, clientSession.getSessionId());

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on update client session '%s'\nerror message:\n%s", clientSession.toString(), e.getMessage()), e);
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

    public void truncateTable() {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_TRUNCATE_TABLE_CLIENT_SESSION);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on truncating table client session.\nerror message:\n%s", e.getMessage()), e);
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
     * It fails to delete the record if the session id is referenced by other records in column: replaced_by.
     * 
     * @param sessionId the id of the session table
     */
    public void deleteClientSessionsBySessionId(String sessionId) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_CLIENT_SESSIONS_BY_ID);

            pStatement.setString(1, sessionId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on deleting client session with session id '%s'%nerror message:%n%s", sessionId, e.getMessage()), e);
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

    public void updateClientSessionReplacedBy(String newSessionId, String oldSessionId) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_CLIENT_SESSION_REPLACED_BY);

            pStatement.setString(1, newSessionId);
            pStatement.setString(2, oldSessionId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on updating client session '%s' replaced by new session: '%s'\nerror message:\n%s", oldSessionId, newSessionId, e.getMessage()), e);
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
