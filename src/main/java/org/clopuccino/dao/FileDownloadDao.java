package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.apache.commons.io.FilenameUtils;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.FileDownload;
import org.clopuccino.domain.TransferHistoryModel;
import org.clopuccino.domain.TransferHistoryTimeType;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <code>FileDownloadDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class FileDownloadDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FileDownloadDao.class.getSimpleName());


    public FileDownloadDao() {
        super();
    }

    public FileDownloadDao(DatabaseAccess dbAccess) {
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
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_FILE_DOWNLOADED, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_FILE_DOWNLOADED);

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOADED_END_TIMESTAMP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOADED_TMP_FILE_CREATED_TIMESTAMP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOADED_TMP_FILE_DELETED_TIMESTAMP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOADED_FROM_IP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOADED_FROM_HOST);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOADED_TO_IP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOADED_TO_HOST);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOADED_DOWNLOAD_GROUP_ID);

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_FILE_DOWNLOADED_COMPUTER_ID);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_FILE_DOWNLOADED_AUTH_USER);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_FILE_DOWNLOADED_DOWNLOAD_GROUP_ID);
            }

//            // add column tmp_file, tmp_file_created_timestamp and tmp_file_deleted_timestamp to table TABLE_NAME_FILE_DOWNLOADED, if not exists
//
//            try {
//                dbAccess.close(rs, statement, null, null);
//            } catch (Exception e) {
//                /* ignored */
//            }
//
//            // add column tmp_file to table file_downloaded if not exists
//
//            rs = dbMetaData.getColumns(null, null, DatabaseConstants.TABLE_NAME_FILE_DOWNLOADED, DatabaseConstants.COLUMN_NAME_TMP_FILE);
//
//            if (!rs.next()) {
//                statement = conn.createStatement();
//
//                statement.executeUpdate(DatabaseConstants.SQL_UPDATE_TABLE_FILE_DOWNLOADED_ADD_COLUMN_TMP_FILE);
//                statement.executeUpdate(DatabaseConstants.SQL_UPDATE_TABLE_FILE_DOWNLOADED_ADD_COLUMN_TMP_FILE_CREATED_TIMESTAMP);
//                statement.executeUpdate(DatabaseConstants.SQL_UPDATE_TABLE_FILE_DOWNLOADED_ADD_COLUMN_TMP_FILE_DELETED_TIMESTAMP);
//
//                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOADED_TMP_FILE_CREATED_TIMESTAMP);
//                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOADED_TMP_FILE_DELETED_TIMESTAMP);
//            }
//
//            try {
//                dbAccess.close(rs, statement, null, null);
//            } catch (Exception e) {
//                /* ignored */
//            }
//
//            // add columns to/from ip and hostname if not exists
//
//            rs = dbMetaData.getColumns(null, null, DatabaseConstants.TABLE_NAME_FILE_DOWNLOADED, DatabaseConstants.COLUMN_NAME_FROM_IP);
//
//            if (!rs.next()) {
//                statement = conn.createStatement();
//
//                statement.executeUpdate(DatabaseConstants.SQL_UPDATE_TABLE_FILE_DOWNLOADED_ADD_COLUMN_FROM_IP);
//                statement.executeUpdate(DatabaseConstants.SQL_UPDATE_TABLE_FILE_DOWNLOADED_ADD_COLUMN_FROM_HOST);
//                statement.executeUpdate(DatabaseConstants.SQL_UPDATE_TABLE_FILE_DOWNLOADED_ADD_COLUMN_TO_IP);
//                statement.executeUpdate(DatabaseConstants.SQL_UPDATE_TABLE_FILE_DOWNLOADED_ADD_COLUMN_TO_HOST);
//
//                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOADED_FROM_IP);
//                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOADED_FROM_HOST);
//                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOADED_TO_IP);
//                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOADED_TO_HOST);
//
//                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOADED_DOWNLOAD_GROUP_ID);
//
//                // Make sure table file_downloaded is created.
//                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_FILE_DOWNLOADED_DOWNLOAD_GROUP_ID);
//            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_FILE_DOWNLOADED, e.getMessage()), e);
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

    public void createFileDownloaded(String downloadKey, String userId, Long computerId, String computerGroup, String computerName, String filePath, String downloadGroupId, long startTimestamp, String downloadToIp, String downloadToHost) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_FILE_DOWNLOADED);
            pStatement.setString(1, downloadKey);
            pStatement.setString(2, userId);
            pStatement.setLong(3, computerId);
            pStatement.setString(4, computerGroup);
            pStatement.setString(5, computerName);
            pStatement.setString(6, filePath);
            pStatement.setString(7, downloadGroupId);
            pStatement.setLong(8, startTimestamp);
            pStatement.setLong(9, 0);
            pStatement.setString(10, DatabaseConstants.TRANSFER_STATUS_PROCESSING);
            pStatement.setString(11, downloadToIp);
            pStatement.setString(12, downloadToHost);

            pStatement.executeUpdate();

            SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
            String dateString = format.format(new Date(startTimestamp));

            LOGGER.debug(String.format("File download created for user '%s' at: %s\ncomputerId=%d\ncomputerGroup=%s\ncomputerName=%s\ndownloadKey=%s\nfilePath=%s\ndownloadGroupId=%s\nstart timestamp=%s\nto ip=%s\nto host=%s\n", userId, dateString, computerId, computerGroup, computerName, downloadKey, filePath, downloadGroupId, String.valueOf(startTimestamp), downloadToIp, downloadToHost));
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
            String dateString = format.format(new Date(startTimestamp));

            LOGGER.error(String.format("Error on creating file download for user '%s' at: %s\ncomputerId=%d\ncomputerGroup=%s\ncomputerName=%s\ndownloadKey=%s\nfilePath=%s\nstart timestamp=%s\nto ip=%s\nto host=%s\nerror message:\n%s\n", userId, dateString, computerId, computerGroup, computerName, downloadKey, filePath, String.valueOf(startTimestamp), downloadToIp, downloadToHost, errorMessage), e);
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
     * To prevent null value to mis-replace original value, use findFileDownloadForDownloadKey(String) first to get the full FileDownload object,
     * replace values you want and then invoke this method to update data.
     *
     * @param fileDownload The FileDownload to be updated
     *
     * @return true if updated successfully
     */
    public boolean updateFileDownload(FileDownload fileDownload) {
        int updatedCount = 0;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_DOWNLOADED);
            pStatement.setString(1, fileDownload.getFilePath());
            pStatement.setLong(2, fileDownload.getFileSize());
            pStatement.setLong(3, fileDownload.getStartTimestamp());
            pStatement.setLong(4, fileDownload.getEndTimestamp());
            pStatement.setString(5, fileDownload.getStatus());
            pStatement.setString(6, fileDownload.getTmpFile());
            pStatement.setLong(7, fileDownload.getTmpFileCreatedTimestamp());
            pStatement.setLong(8, fileDownload.getTmpFileDeletedTimestamp());
            pStatement.setString(9, fileDownload.getFromIp());
            pStatement.setString(10, fileDownload.getFromHost());
            pStatement.setString(11, fileDownload.getToIp());
            pStatement.setString(12, fileDownload.getToHost());
            pStatement.setString(13, fileDownload.getTransferKey());

            updatedCount = pStatement.executeUpdate();

            LOGGER.debug(String.format("File download updated:\n%s", fileDownload.toString()));
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on updating file download:\n%s\nerror message:\n%s", fileDownload.toString(), errorMessage), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return updatedCount > 0;
    }

    public boolean updateFileDownloadStatus(String transferKey, String status, long endTimestamp, long fileSize) {
        int updatedCount = 0;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_DOWNLOADED_STATUS);
            pStatement.setString(1, status);
            pStatement.setLong(2, endTimestamp);
            pStatement.setLong(3, fileSize);
            pStatement.setString(4, transferKey);

            updatedCount = pStatement.executeUpdate();

            if (LOGGER.isDebugEnabled()) {
                SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
                String dateString = format.format(new Date(endTimestamp));

                LOGGER.debug(String.format("File download status updated at: %s.\ndownloadKey=%s\nstaus=%s\nfileSize=%s", dateString, transferKey, status, String.valueOf(fileSize)));
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
            String dateString = format.format(new Date(endTimestamp));

            LOGGER.error(String.format("Error on updating file download status at: %s\ndownloadKey=%s\nstatus=%s\nfileSize=%s\nerror message:\n%s", dateString, transferKey, status, String.valueOf(fileSize), errorMessage), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return updatedCount > 0;
    }

    public boolean updateFileDownloadTmpFileDeletedTimestamp(String transferKey, long tmpFileDeletedTimestamp) {
        int updateCount = 0;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_DOWNLOADED_TMP_FILE_DELETED_TIMESTAMP);
            pStatement.setLong(1, tmpFileDeletedTimestamp);
            pStatement.setString(2, transferKey);

            updateCount = pStatement.executeUpdate();

            if (LOGGER.isDebugEnabled()) {
                SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
                String dateString = format.format(new Date(tmpFileDeletedTimestamp));

                LOGGER.debug(String.format("Updated file download tmp file deleted timestamp to '%s' for download key: '%s'.", dateString, transferKey));
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            if (LOGGER.isDebugEnabled()) {
                SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
                String dateString = format.format(new Date(tmpFileDeletedTimestamp));

                LOGGER.error(String.format("Error on updating file download tmp file deleted timestamp to '%s' for download key: '%s'\nerror message:\n%s", dateString, transferKey, errorMessage), e);
            }
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return updateCount > 0;
    }

    public boolean updateFileDownloadStatus(String transferKey, String status, long endTimestamp) {
        int updatedCount = 0;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_DOWNLOADED_WITHOUT_FILE_SIZE);
            pStatement.setString(1, status);
            pStatement.setLong(2, endTimestamp);
            pStatement.setString(3, transferKey);

            updatedCount = pStatement.executeUpdate();

            if (LOGGER.isDebugEnabled()) {
                SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
                String dateString = format.format(new Date(endTimestamp));

                LOGGER.debug(String.format("File download status updated at: %s.\ndownloadKey=%s\nstaus=%s", dateString, transferKey, status));
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
            String dateString = format.format(new Date(endTimestamp));

            LOGGER.error(String.format("Error on updating file download status at: %s\ndownloadKey=%s\nstatus=%s\nerror message:\n%s", dateString, transferKey, status, errorMessage), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return updatedCount > 0;
    }

//    /**
//     * Updates the processing status to failure
//     * for the start timestamp of the downloads/uploads is smaller than the specified minimum timestamp
//     * and the processing status is still in processing.
//     *
//     * @param minimumTimestamp The minimum accepted timestamp that is not time-out.
//     */
//    public void updateStatusProcessingToFailureForTimeoutStartTimestamp(long minimumTimestamp) {
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            LOGGER.info("Start updating download status to failure for timeout start-timestamp");
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_DOWNLOADED_STATUS_PROCESSING_TO_FAILURE_FOR_TIMEOUT_START_TIMESTAMP);
//            pStatement.setLong(1, minimumTimestamp);
//
//            pStatement.executeUpdate();
//        } catch (Exception e) {
//            LOGGER.error(String.format("Error on updating file download status to failure for timeout transfer: %s\n", String.valueOf(minimumTimestamp)), e);
//        } finally {
//            if (dbAccess != null) {
//                try {
//                    dbAccess.close(null, null, pStatement, conn);
//                } catch (Exception e) {
//                    /* ignored */
//                }
//            }
//        }
//    }

//    public void updateStatusProcessingToFailureForTimeoutStartTimestamp(long minimumTimestamp) {
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//        ResultSet resultSet = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_PROCESSING_FILE_DOWNLOADED_BY_TIMEOUT_START_TIMESTAMP);
//            pStatement.setLong(1, minimumTimestamp);
//
//            boolean found = false;
//
//            resultSet = pStatement.executeQuery();
//
//            for (; resultSet.next(); ) {
//                found = true;
//
//                String downloadKey = resultSet.getString(DatabaseConstants.COLUMN_NAME_DOWNLOAD_KEY);
//                String userId = resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID);
//                String filePath = resultSet.getString(DatabaseConstants.COLUMN_NAME_FILE_PATH);
//                long fileSize = resultSet.getLong(DatabaseConstants.COLUMN_NAME_FILE_SIZE);
//                long startTimestamp = resultSet.getLong(DatabaseConstants.COLUMN_NAME_START_TIMESTAMP);
//                String status = resultSet.getString(DatabaseConstants.COLUMN_NAME_STATUS);
//
//                String message = String.format("Timeout file-download found.\ndownload key: %s\nuser: %s\nfile path: %s\nfile size in bytes: %s\nstart timestamp: %s\nstatus: %s\n", downloadKey, userId, filePath, String.valueOf(fileSize), String.valueOf(startTimestamp), status);
//                LOGGER.info(message);
//            }
//
//            if (found) {
//                LOGGER.info("Start updating download status to failure for timeout start-timestamp");
//
//                resultSet.close();
//                pStatement.close();
//
//                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_DOWNLOADED_STATUS_PROCESSING_TO_FAILURE_FOR_TIMEOUT_START_TIMESTAMP);
//                pStatement.setLong(1, minimumTimestamp);
//
//                pStatement.executeUpdate();
//            }
//        } catch (Exception e) {
//            String errorMessage = e.getMessage();
//
//            LOGGER.error(String.format("Error on updating file download status to failure for timeout transfer: %s\nerror message:\n%s", String.valueOf(minimumTimestamp), errorMessage), e);
//        } finally {
//            if (dbAccess != null) {
//                try {
//                    dbAccess.close(resultSet, null, pStatement, conn);
//                } catch (Exception e) {
//                    /* ignored */
//                }
//            }
//        }
//    }

    public List<TransferHistoryModel> findFileDownloadedForUser(String userId, boolean successOnly, TransferHistoryTimeType type) throws Exception {
        List<TransferHistoryModel> histories = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            if (successOnly) {
                switch (type) {
                    case TRANSFER_HISTORY_TYPE_LATEST_WEEK:
                        pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SUCCESS_FILE_DOWNLOADED_WITH_TIME_RANGE_BY_USER);
                        pStatement.setString(1, userId);
                        pStatement.setLong(2, Utility.lastWeekTimestamp());
                        pStatement.setLong(3, System.currentTimeMillis());

                        break;
                    case TRANSFER_HISTORY_TYPE_LATEST_MONTH:
                        pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SUCCESS_FILE_DOWNLOADED_WITH_TIME_RANGE_BY_USER);
                        pStatement.setString(1, userId);
                        pStatement.setLong(2, Utility.lastMonthTimestamp());
                        pStatement.setLong(3, System.currentTimeMillis());

                        break;
                    case TRANSFER_HISTORY_TYPE_ALL:
                        pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SUCCESS_FILE_DOWNLOADED_BY_USER);
                        pStatement.setString(1, userId);

                        break;
                    default:
                        // TRANSFER_HISTORY_TYPE_LATEST_20
                        pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SUCCESS_FILE_DOWNLOADED_LATEST_20_BY_USER);
                        pStatement.setString(1, userId);
                }
            } else {
                switch (type) {
                    case TRANSFER_HISTORY_TYPE_LATEST_WEEK:
                        pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_DOWNLOADED_WITH_TIME_RANGE_BY_USER);
                        pStatement.setString(1, userId);
                        pStatement.setLong(2, Utility.lastWeekTimestamp());
                        pStatement.setLong(3, System.currentTimeMillis());

                        break;
                    case TRANSFER_HISTORY_TYPE_LATEST_MONTH:
                        pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_DOWNLOADED_WITH_TIME_RANGE_BY_USER);
                        pStatement.setString(1, userId);
                        pStatement.setLong(2, Utility.lastMonthTimestamp());
                        pStatement.setLong(3, System.currentTimeMillis());

                        break;
                    case TRANSFER_HISTORY_TYPE_ALL:
                        pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_ALL_FILE_DOWNLOADED_BY_USER);
                        pStatement.setString(1, userId);

                        break;
                    default:
                        // TRANSFER_HISTORY_TYPE_LATEST_20
                        pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_DOWNLOADED_LATEST_20_BY_USER);
                        pStatement.setString(1, userId);
                }
            }

            resultSet = pStatement.executeQuery();

            for (; resultSet.next();) {
                String computerGroup = resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_GROUP);
                String computerName = resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME);
                long fileSize = resultSet.getLong(DatabaseConstants.COLUMN_NAME_FILE_SIZE);
                String filename = FilenameUtils.getName(resultSet.getString(DatabaseConstants.COLUMN_NAME_FILE_PATH));
                long endTimestamp = resultSet.getLong(DatabaseConstants.COLUMN_NAME_END_TIMESTAMP);

                histories.add(new TransferHistoryModel(computerGroup, computerName, fileSize, endTimestamp, filename));
            }
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return histories;
    }



//    public List<TransferHistoryModel> findAllFileDownloadedForUser(String userId, boolean successOnly) throws Exception {
//        List<TransferHistoryModel> histories = new ArrayList<>();
//
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//        ResultSet resultSet = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            if (successOnly) {
//                pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SUCCESS_FILE_DOWNLOADED_BY_USER);
//            } else {
//                pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_ALL_FILE_DOWNLOADED_BY_USER);
//            }
//
//            pStatement.setString(1, userId);
//
//            resultSet = pStatement.executeQuery();
//
//            for (; resultSet.next();) {
//                long fileSize = resultSet.getLong(DatabaseConstants.COLUMN_NAME_FILE_SIZE);
//                String filename = FilenameUtils.getName(resultSet.getString(DatabaseConstants.COLUMN_NAME_FILE_PATH));
//                long endTimestamp = resultSet.getLong(DatabaseConstants.COLUMN_NAME_END_TIMESTAMP);
//
//                histories.add(new TransferHistoryModel(fileSize, endTimestamp, filename));
//            }
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
//        return histories;
//    } // end findAllFileDownloadedForUser(String, boolean)

    public void updateFileDownloadSize(String transferKey, long fileSize) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_DOWNLOADED_SIZE);
            pStatement.setLong(1, fileSize);
            pStatement.setString(2, transferKey);

            pStatement.executeUpdate();

            LOGGER.debug(String.format("File download size updated to '%s' for downloadKey=%s", String.valueOf(fileSize), transferKey));
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on updating file download size updated to '%s' for downloadKey=%s\nerror message:\n%s", String.valueOf(fileSize), transferKey, errorMessage), e);
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

    public long sumDownloadingFileSizeForUser(String userId) {
        long fileSizeSum = 0;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SUM_DOWNLOADING_FILE_SIZE_BY_USER);

            pStatement.setString(1, userId);

            resultSet = pStatement.executeQuery();

            if(resultSet.next()) {
                Object tmpValue = resultSet.getObject(1);

                if (tmpValue != null && Number.class.isInstance(tmpValue)) {
                    fileSizeSum = ((Number) tmpValue).longValue();
//                } else {
//                    LOGGER.error("Data type for sum of column: " + DatabaseConstants.TABLE_NAME_FILE_DOWNLOADED + "." + DatabaseConstants.COLUMN_NAME_FILE_SIZE + " is not type of Number, but " + tmpValue.getClass().getName());
                }

//                BigDecimal tmp = resultSet.getBigDecimal(1);
//
//                fileSizeSum = tmp.longValue();
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on finding sum-up file size of the downloading files for user: %s\nerror message:\n%s", userId, errorMessage), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return fileSizeSum;
    }

    public boolean existingFileDownloadForDownloadKey(String downloadKey) {
        boolean exists = false;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_IF_EXISTS_FILE_DOWNLOADED_BY_DOWNLOAD_KEY);

            pStatement.setString(1, downloadKey);

            resultSet = pStatement.executeQuery();

            exists = resultSet.next();
        } catch (Exception e) {
            exists = false;
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

    public FileDownload findFileDownloadForDownloadKey(String downloadKey) {
        FileDownload fileDownload = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_DOWNLOADED_BY_DOWNLOAD_KEY);

            pStatement.setString(1, downloadKey);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                fileDownload = new FileDownload();

                fileDownload.setTransferKey(downloadKey);
                fileDownload.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
                fileDownload.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID));
                fileDownload.setGroupName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_GROUP));
                fileDownload.setComputerName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME));
                fileDownload.setFilePath(resultSet.getString(DatabaseConstants.COLUMN_NAME_FILE_PATH));
                fileDownload.setFileSize(resultSet.getLong(DatabaseConstants.COLUMN_NAME_FILE_SIZE));
                fileDownload.setDownloadGroupId(resultSet.getString(DatabaseConstants.COLUMN_NAME_DOWNLOAD_GROUP_ID));
                fileDownload.setStartTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_START_TIMESTAMP));
                fileDownload.setEndTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_END_TIMESTAMP));
                fileDownload.setStatus(resultSet.getString(DatabaseConstants.COLUMN_NAME_STATUS));
                fileDownload.setTmpFile(resultSet.getString(DatabaseConstants.COLUMN_NAME_TMP_FILE));
                fileDownload.setTmpFileCreatedTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_TMP_FILE_CREATED_TIMESTAMP));
                fileDownload.setTmpFileDeletedTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_TMP_FILE_DELETED_TIMESTAMP));
            }
        } catch (Exception e) {
            fileDownload = null;

            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on finding file download for download key: %s\nerror message:\n%s", downloadKey, errorMessage), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return fileDownload;
    }

    public boolean updateFileDownloadTmpFileCreated(String downloadKey, String tmpFile, long tmpFileCreatedTimestamp) {
        int updateCount = 0;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_DOWNLOADED_TMP_FILE_CREATED);

            pStatement.setString(1, DatabaseConstants.TRANSFER_STATUS_DESKTOP_UPLOADED_BUT_UNCONFIRMED);
            pStatement.setString(2, tmpFile);
            pStatement.setLong(3, tmpFileCreatedTimestamp);
            pStatement.setString(4, downloadKey);

            updateCount = pStatement.executeUpdate();

            LOGGER.debug(String.format("Updated file download tmp file to '%s', tmp file created timestamp to '%d', status to '%s' for download key '%s'.", tmpFile, tmpFileCreatedTimestamp, DatabaseConstants.TRANSFER_STATUS_DESKTOP_UPLOADED_BUT_UNCONFIRMED, downloadKey));
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on updating file download tmp file to '%s', tmp file created timestamp to '%d', status to '%s' for download key '%s'.\nerror message:\n%s", tmpFile, tmpFileCreatedTimestamp, DatabaseConstants.TRANSFER_STATUS_DESKTOP_UPLOADED_BUT_UNCONFIRMED, downloadKey, errorMessage), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return updateCount > 0;
    }

    public void updateFileDownloadFromIpHost(String transferKey, String downloadFromIp, String downloadFromHost) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_DOWNLOADED_FROM_IP_HOST);

            pStatement.setString(1, downloadFromIp);
            pStatement.setString(2, downloadFromHost);
            pStatement.setString(3, transferKey);

            pStatement.executeUpdate();

            LOGGER.debug(String.format("Successfully updating file download with transfer key '%s', from ip '%s', from host '%s'.", transferKey, downloadFromIp, downloadFromHost));
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on updating file download with transfer key '%s', from ip '%s', from host '%s'.\nerror message:\n%s", transferKey, downloadFromIp, downloadFromHost, errorMessage), e);
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

    public void updateStatusProcessingToDesktopUploadedButUnconfirmed(String transferKey) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_DOWNLOADED_STATUS_FROM_PROCESSING_TO_DESKTOP_UPLOADED_BUT_UNCONFIRMED);

            pStatement.setString(1, transferKey);

            pStatement.executeUpdate();

            LOGGER.debug(String.format("File download status updated from %s to %s for download key=%s", DatabaseConstants.TRANSFER_STATUS_PROCESSING, DatabaseConstants.TRANSFER_STATUS_DESKTOP_UPLOADED_BUT_UNCONFIRMED, transferKey));
        } catch (Exception e) {
            LOGGER.error(String.format("Error on updating file download status from %s to %s for download key=%s.", DatabaseConstants.TRANSFER_STATUS_PROCESSING, DatabaseConstants.TRANSFER_STATUS_DESKTOP_UPLOADED_BUT_UNCONFIRMED, transferKey), e);
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

    public void updateFileDownloadComputerName(Long computerId, String computerGroup, String computerName) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_DOWNLOADED_COMPUTER_NAME);
            pStatement.setString(1, computerGroup);
            pStatement.setString(2, computerName);
            pStatement.setLong(3, computerId);

            pStatement.executeUpdate();

            LOGGER.debug(String.format("Updated file download computer group to '%s', computer name to '%s' with computer id '%d'.", computerGroup, computerName, computerId));
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on updating file download computer group to '%s', computer name to '%s' with computer id '%d'\nerror message:\n%s", computerGroup, computerName, computerId, errorMessage), e);
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

    public boolean existingFileDownloadForDownloadGroupId(String downloadGroupId) {
        boolean exists = false;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_IF_EXISTS_FILE_DOWNLOADED_BY_DOWNLOAD_GROUP_ID);

            pStatement.setString(1, downloadGroupId);

            resultSet = pStatement.executeQuery();

            exists = resultSet.next();
        } catch (Exception e) {
            exists = false;
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

    public boolean deleteFileDownloadedForDownloadKey(String downloadKey) {
        boolean success = false;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_FILE_DOWNLOADED_BY_DOWNLOAD_KEY);
            pStatement.setString(1, downloadKey);

            pStatement.executeUpdate();

            success = true;

            LOGGER.debug(String.format("Deleted file downloaded for download key: '%s'.", downloadKey));
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on deleting file downloaded for download key: '%s'\nerror message:\n%s", downloadKey, errorMessage), e);
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
