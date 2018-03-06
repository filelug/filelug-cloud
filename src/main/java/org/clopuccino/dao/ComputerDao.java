package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.Computer;
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
public class ComputerDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ComputerDao.class.getSimpleName());

    private static final int RECOVERY_KEY_LENGTH = 12;


    public ComputerDao() {
        super();
    }

    public ComputerDao(DatabaseAccess dbAccess) {
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
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_COMPUTER, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_COMPUTER);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_COMPUTER_AUTH_USER);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_COMPUTER_AUTH_USER_ID);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_COMPUTER_COMPUTER_GROUP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_COMPUTER_COMPUTER_NAME);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_COMPUTER_RECOVERY_KEY);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_COMPUTER, e.getMessage()), e);
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

    public String findComputerNameByComputerId(Long computerId) throws Exception {
        String computerName = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_COMPUTER_NAME_BY_COMPUTER_ID);

            pStatement.setLong(1, computerId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                computerName = resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding computer name for id '%s'\nerror message:\n%s", computerId, e.getMessage()), e);

            throw new Exception(e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return computerName;
    }

    public Computer findComputerById(Long computerId) throws SQLException {
        Computer record = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_COMPUTER_BY_COMPUTER_ID);

            pStatement.setLong(1, computerId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                record = new Computer();

                record.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID));
                record.setGroupName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_GROUP));
                record.setComputerName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME));
                record.setRecoveryKey(resultSet.getString(DatabaseConstants.COLUMN_NAME_RECOVERY_KEY));
                record.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding computer for id '%d'\nerror message:\n%s", computerId, e.getMessage()), e);

            throw new SQLException(e);
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

    public String findComputerOwnerById(Long computerId) throws SQLException {
        String computerOwner = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_COMPUTER_OWNER_BY_COMPUTER_ID);

            pStatement.setLong(1, computerId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                computerOwner = resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding computer owner for id '%d'\nerror message:\n%s", computerId, e.getMessage()), e);

            throw new SQLException(e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return computerOwner;
    }

    public Computer findComputerByRecoveryKey(String recoveryKey) throws SQLException {
        Computer record = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_COMPUTER_BY_RECOVERY_KEY);

            pStatement.setString(1, recoveryKey);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                record = new Computer();

                record.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID));
                record.setGroupName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_GROUP));
                record.setComputerName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME));
                record.setRecoveryKey(resultSet.getString(DatabaseConstants.COLUMN_NAME_RECOVERY_KEY));
                record.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding computer by recovery key '%s'\nerror message:\n%s", recoveryKey, e.getMessage()), e);

            throw new SQLException(e);
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

    public Computer findComputerByName(String computerGroup, String computerName) throws SQLException {
        Computer record = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_COMPUTER_BY_GROUP_NAME_AND_COMPUTER_NAME);

            pStatement.setString(1, computerGroup);
            pStatement.setString(2, computerName);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                record = new Computer();

                record.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID));
                record.setGroupName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_GROUP));
                record.setComputerName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME));
                record.setRecoveryKey(resultSet.getString(DatabaseConstants.COLUMN_NAME_RECOVERY_KEY));
                record.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding computer by group '%s'\nname '%s'\nerror message:\n%s", computerGroup, computerName, e.getMessage()), e);

            throw new SQLException(e);
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

    public Computer findComputerByNameForUser(String userId, String computerGroup, String computerName) throws SQLException {
        Computer record = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_COMPUTER_BY_GROUP_NAME_AND_COMPUTER_NAME_FOR_USER);

            pStatement.setString(1, computerGroup);
            pStatement.setString(2, computerName);
            pStatement.setString(3, userId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                record = new Computer();

                record.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID));
                record.setGroupName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_GROUP));
                record.setComputerName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME));
                record.setRecoveryKey(resultSet.getString(DatabaseConstants.COLUMN_NAME_RECOVERY_KEY));
                record.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding computer by group '%s'\nname '%s' for user '%s'\nerror message:\n%s", computerGroup, computerName, userId, e.getMessage()), e);

            throw new SQLException(e);
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
     * Creates a computer.
     *
     * @param computer The content of the computer to be created.
     * @return The created computer
     */
    public Computer createComputer(Computer computer) throws SQLException {
        Connection conn = null;
        PreparedStatement pStatement = null;

        String computerGroup = computer.getGroupName();
        String computerName = computer.getComputerName();
        String creator = computer.getUserId();

        String recoveryKey = computer.getRecoveryKey();

        if (recoveryKey == null || recoveryKey.trim().length() < 1) {
            recoveryKey = DigestUtils.sha256Hex(RandomStringUtils.random(RECOVERY_KEY_LENGTH, false, true) + System.currentTimeMillis());
        }

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_COMPUTER);
            pStatement.setString(1, computerGroup);
            pStatement.setString(2, computerName);
            pStatement.setString(3, recoveryKey);
            pStatement.setString(4, creator);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on creating computer: '%s'\ncreator: '%s'\nerror message:\n%s", computer.toString(), creator, e.getMessage()), e);

            throw new SQLException(e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return findComputerByRecoveryKey(recoveryKey);
    }

    public Computer createComputer(String computerGroup, String computerName, String recoveryKey, String userId) throws SQLException {
        Computer computer = new Computer();

        computer.setGroupName(computerGroup);
        computer.setComputerName(computerName);

        computer.setRecoveryKey(recoveryKey);
        computer.setUserId(userId);

        computer = createComputer(computer);

        return computer;
    }

    public List<Computer> findAllComputers() {
        List<Computer> computers = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_ALL_COMPUTERS);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                Computer record = new Computer();

                record.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID));
                record.setGroupName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_GROUP));
                record.setComputerName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME));
                record.setRecoveryKey(resultSet.getString(DatabaseConstants.COLUMN_NAME_RECOVERY_KEY));
                record.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));

                computers.add(record);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding all computers\nerror message:\n%s", e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return computers;
    }

    public void deleteComputerById(Long computerId) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_COMPUTER_BY_ID_CASCADE);

            pStatement.setLong(1, computerId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on deleting computer for computer id '%d'\nerror message:\n%s", computerId, e.getMessage()), e);
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

    public void updateComputer(Computer computer) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_COMPUTER_BY_ID);
            pStatement.setString(1, computer.getGroupName());
            pStatement.setString(2, computer.getComputerName());
            pStatement.setString(3, computer.getUserId());
            pStatement.setLong(4, computer.getComputerId());

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on updating computer: '%s'\nerror message:\n%s", computer.toString(), e.getMessage()), e);
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

    public void updateComputerName(Computer computer) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_COMPUTER_NAME);
            pStatement.setString(1, computer.getGroupName());
            pStatement.setString(2, computer.getComputerName());
            pStatement.setLong(4, computer.getComputerId());

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on updating computer group to: '%s', computer name to: '%s' of computer id: '%d'\nerror message:\n%s", computer.getGroupName(), computer.getComputerName(), computer.getComputerId(), e.getMessage()), e);
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

    public List<Computer> findComputerByAdminId(String userId) {
        List<Computer> computers = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_COMPUTERS_BY_USER_ID);

            pStatement.setString(1, userId);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                Computer record = new Computer();

                record.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID));
                record.setGroupName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_GROUP));
                record.setComputerName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME));
                record.setRecoveryKey(resultSet.getString(DatabaseConstants.COLUMN_NAME_RECOVERY_KEY));
                record.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));

                computers.add(record);
            }
        } catch (Exception e) {
            computers.clear();

            LOGGER.error(String.format("Error on finding computer by user '%s'\nerror message:\n%s", userId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return computers;
    }

    public List<String> findComputerNamesByUserId(String userId, boolean toLowerCase) {
        List<String> computerNames = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            String sqlStatement = toLowerCase ? DatabaseConstants.SQL_FIND_ALL_COMPUTER_NAMES_WITH_LOWER_CASE_BY_USER_ID : DatabaseConstants.SQL_FIND_ALL_COMPUTER_NAMES_BY_USER_ID;

            pStatement = conn.prepareStatement(sqlStatement);

            pStatement.setString(1, userId);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                // Do not use column name as the argument because it could be 'lower(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME)'
                String computerName = resultSet.getString(1);
//                String computerName = resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME);

                computerNames.add(computerName);
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding all computers\nerror message:\n%s", e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return computerNames;
    }
}
