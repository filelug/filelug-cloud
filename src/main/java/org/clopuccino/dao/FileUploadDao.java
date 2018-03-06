package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.apache.commons.io.FilenameUtils;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.ConfirmTransferModel;
import org.clopuccino.domain.FileUpload;
import org.clopuccino.domain.TransferHistoryModel;
import org.clopuccino.domain.TransferHistoryTimeType;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <code>FileUploadDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class FileUploadDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FileUploadDao.class.getSimpleName());


    public FileUploadDao() {
        super();
    }

    public FileUploadDao(DatabaseAccess dbAccess) {
        super(dbAccess);
    }

    // The method must be invoked after table file_upload_group created
    public boolean createTableIfNotExists() {
        boolean success = true;

        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dbAccess.getConnection();

            DatabaseMetaData dbMetaData = conn.getMetaData();

            /* check if table exists */
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_FILE_UPLOADED, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_FILE_UPLOADED);

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_UPLOADED_UPLOAD_GROUP_ID);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_UPLOADED_END_TIMESTAMP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_UPLOADED_TMP_FILE_CREATED_TIMESTAMP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_UPLOADED_TMP_FILE_DELETED_TIMESTAMP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_UPLOADED_FROM_IP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_UPLOADED_FROM_HOST);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_UPLOADED_TO_IP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_UPLOADED_TO_HOST);

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_FILE_UPLOADED_COMPUTER_ID);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_FILE_UPLOADED_AUTH_USER);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_FILE_UPLOADED_UPLOAD_GROUP_ID);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_FILE_UPLOADED, e.getMessage()), e);
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

    public void createFileUploaded(String uploadKey, String userId, Long computerId, String computerGroup, String computerName, String directory, String filename, long fileSize, String uploadGroupId, long startTimestamp, String status, String tmpFile, long tmpFileCreatedTimestamp, long transferredByteIndex, long sourceFileLastModifiedTimestamp, String fromIp, String fromHost) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_FILE_UPLOADED);
            pStatement.setString(1, uploadKey);
            pStatement.setString(2, userId);
            pStatement.setLong(3, computerId);
            pStatement.setString(4, computerGroup);
            pStatement.setString(5, computerName);
            pStatement.setString(6, filename);
            pStatement.setString(7, directory);
            pStatement.setLong(8, fileSize);
            pStatement.setString(9, uploadGroupId);
            pStatement.setLong(10, startTimestamp);
            pStatement.setLong(11, 0);
            pStatement.setString(12, status);
            pStatement.setString(13, tmpFile);
            pStatement.setLong(14, tmpFileCreatedTimestamp);
            pStatement.setLong(15, transferredByteIndex);
            pStatement.setLong(16, sourceFileLastModifiedTimestamp);
            pStatement.setString(17, fromIp);
            pStatement.setString(18, fromHost);

            pStatement.executeUpdate();

            LOGGER.debug(String.format("File upload data created for user '%s'" +
                                       "\ncomputerId=%d" +
                                       "\ncomputerGroup=%s" +
                                       "\ncomputerName=%s" +
                                       "\nuploadKey=%s" +
                                       "\ndirectory=%s" +
                                       "\nfilename=%s" +
                                       "\nstart timestamp=%s" +
                                       "\nstatus=%s" +
                                       "\ntmp file=%s" +
                                       "\ntmp file created timestamp=%s" +
                                       "\ntransferred byte index=%s" +
                                       "\nsource file last modified=%s\n",
                                       userId,
                                       computerId,
                                       computerGroup,
                                       computerName,
                                       uploadKey,
                                       directory,
                                       filename,
                                       String.valueOf(startTimestamp),
                                       status,
                                       tmpFile,
                                       String.valueOf(tmpFileCreatedTimestamp),
                                       String.valueOf(transferredByteIndex),
                                       String.valueOf(sourceFileLastModifiedTimestamp)));
        } catch (Exception e) {
            LOGGER.error(String.format("Error on creating file upload data for user '%s'" +
                                       "\ncomputerId=%d" +
                                       "\ncomputerGroup=%s" +
                                       "\ncomputerName=%s" +
                                       "\nuploadKey=%s" +
                                       "\ndirectory=%s" +
                                       "\nfilename=%s" +
                                       "\nstart timestamp=%s" +
                                       "\nstatus=%s" +
                                       "\ntmp file=%s" +
                                       "\ntmp file created timestamp=%s" +
                                       "\ntransferred byte index=%s" +
                                       "\nsource file last modified=%s\n",
                                       userId,
                                       computerId,
                                       computerGroup,
                                       computerName,
                                       uploadKey,
                                       directory,
                                       filename,
                                       String.valueOf(startTimestamp),
                                       status,
                                       tmpFile,
                                       String.valueOf(tmpFileCreatedTimestamp),
                                       String.valueOf(transferredByteIndex),
                                       String.valueOf(sourceFileLastModifiedTimestamp)), e);
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

    // Before adding status as one of the arguments
//    public void createFileUploaded(String uploadKey, String userId, Long computerId, String computerGroup, String computerName, String directory, String filename, long fileSize, String uploadGroupId, long startTimestamp, String tmpFile, long tmpFileCreatedTimestamp, long transferredByteIndex, long sourceFileLastModifiedTimestamp, String fromIp, String fromHost) {
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_FILE_UPLOADED);
//            pStatement.setString(1, uploadKey);
//            pStatement.setString(2, userId);
//            pStatement.setLong(3, computerId);
//            pStatement.setString(4, computerGroup);
//            pStatement.setString(5, computerName);
//            pStatement.setString(6, filename);
//            pStatement.setString(7, directory);
//            pStatement.setLong(8, fileSize);
//            pStatement.setString(9, uploadGroupId);
//            pStatement.setLong(10, startTimestamp);
//            pStatement.setLong(11, 0);
//            pStatement.setString(12, DatabaseConstants.TRANSFER_STATUS_PROCESSING);
//            pStatement.setString(13, tmpFile);
//            pStatement.setLong(14, tmpFileCreatedTimestamp);
//            pStatement.setLong(15, transferredByteIndex);
//            pStatement.setLong(16, sourceFileLastModifiedTimestamp);
//            pStatement.setString(17, fromIp);
//            pStatement.setString(18, fromHost);
//
//            pStatement.executeUpdate();
//
//            LOGGER.debug(String.format("File upload data created for user '%s'" +
//                                       "\ncomputerId=%d" +
//                                       "\ncomputerGroup=%s" +
//                                       "\ncomputerName=%s" +
//                                       "\nuploadKey=%s" +
//                                       "\ndirectory=%s" +
//                                       "\nfilename=%s" +
//                                       "\nstart timestamp=%s" +
//                                       "\ntmp file=%s" +
//                                       "\ntmp file created timestamp=%s" +
//                                       "\ntransferred byte index=%s" +
//                                       "\nsource file last modified=%s\n",
//                                       userId,
//                                       computerId,
//                                       computerGroup,
//                                       computerName,
//                                       uploadKey,
//                                       directory,
//                                       filename,
//                                       String.valueOf(startTimestamp),
//                                       tmpFile,
//                                       String.valueOf(tmpFileCreatedTimestamp),
//                                       String.valueOf(transferredByteIndex),
//                                       String.valueOf(sourceFileLastModifiedTimestamp)));
//        } catch (Exception e) {
//            LOGGER.error(String.format("Error on creating file upload data for user '%s'" +
//                                       "\ncomputerId=%d" +
//                                       "\ncomputerGroup=%s" +
//                                       "\ncomputerName=%s" +
//                                       "\nuploadKey=%s" +
//                                       "\ndirectory=%s" +
//                                       "\nfilename=%s" +
//                                       "\nstart timestamp=%s" +
//                                       "\ntmp file=%s" +
//                                       "\ntmp file created timestamp=%s" +
//                                       "\ntransferred byte index=%s" +
//                                       "\nsource file last modified=%s\n",
//                                       userId,
//                                       computerId,
//                                       computerGroup,
//                                       computerName,
//                                       uploadKey,
//                                       directory,
//                                       filename,
//                                       String.valueOf(startTimestamp),
//                                       tmpFile,
//                                       String.valueOf(tmpFileCreatedTimestamp),
//                                       String.valueOf(transferredByteIndex),
//                                       String.valueOf(sourceFileLastModifiedTimestamp)), e);
//        } finally {
//            if (dbAccess != null) {
//                try {
//                    dbAccess.close(null, null, pStatement, conn);
//                } catch (Exception e) {
//                        /* ignored */
//                }
//            }
//        }
//    }

    public boolean updateFileUploadStatus(String transferKey, String status, long endTimestamp) {
        int updateCount = 0;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_UPLOADED_WITHOUT_FILE_SIZE);
            pStatement.setString(1, status);
            pStatement.setLong(2, endTimestamp);
            pStatement.setString(3, transferKey);

            updateCount = pStatement.executeUpdate();

            if (LOGGER.isDebugEnabled()) {
                SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
                String dateString = format.format(new Date(endTimestamp));

                LOGGER.debug(String.format("Updated file upload status to '%s'\nupload key: '%s' at: %s.", status, transferKey, dateString));
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            if (LOGGER.isDebugEnabled()) {
                SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
                String dateString = format.format(new Date(endTimestamp));

                LOGGER.error(String.format("Error on updating file upload status: '%s'\nupload key: '%s' at: %s\nerror message:\n%s", status, transferKey, dateString, errorMessage), e);
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

    public boolean updateFileUploadTmpFileDeletedTimestamp(String transferKey, long tmpFileDeletedTimestamp) {
        int updateCount = 0;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_UPLOADED_TMP_FILE_DELETED_TIMESTAMP);
            pStatement.setLong(1, tmpFileDeletedTimestamp);
            pStatement.setString(2, transferKey);

            updateCount = pStatement.executeUpdate();

            if (LOGGER.isDebugEnabled()) {
                SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
                String dateString = format.format(new Date(tmpFileDeletedTimestamp));

                LOGGER.debug(String.format("Updated file upload tmp file deleted timestamp to '%s' for upload key: '%s'.", dateString, transferKey));
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            if (LOGGER.isDebugEnabled()) {
                SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
                String dateString = format.format(new Date(tmpFileDeletedTimestamp));

                LOGGER.error(String.format("Error on updating file upload tmp file deleted timestamp to '%s' for upload key: '%s'\nerror message:\n%s", dateString, transferKey, errorMessage), e);
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

//    public void updateFileUploadSize(String transferKey, long fileSize) {
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_UPLOADED_SIZE);
//            pStatement.setLong(1, fileSize);
//            pStatement.setString(2, transferKey);
//
//            pStatement.executeUpdate();
//
//            LOGGER.info(String.format("Updating file uploading size: %s.\nuploadKey=%s", String.valueOf(fileSize), transferKey));
//        } catch (Exception e) {
//            String errorMessage = e.getMessage();
//
//            LOGGER.error(String.format("Updating file uploading size: %s.\nuploadKey=%s\nerror message:\n%s", String.valueOf(fileSize), transferKey, errorMessage), e);
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

    public String findFileUploadFilenameForUploadKey(String uploadKey) {
        String filename = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_UPLOADED_BY_UPLOAD_KEY);

            pStatement.setString(1, uploadKey);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                filename = resultSet.getString(DatabaseConstants.COLUMN_NAME_FILENAME);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to find filename for upload key: " + uploadKey, e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return filename;
    }

    public boolean existingFileUploadForUploadKey(String uploadKey) {
        boolean exists = false;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_IF_EXISTS_FILE_UPLOADED_BY_UPLOAD_KEY);

            pStatement.setString(1, uploadKey);

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

    public FileUpload findFileUploadForUploadKey(String uploadKey) {
        FileUpload fileUpload = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_UPLOADED_BY_UPLOAD_KEY);

            pStatement.setString(1, uploadKey);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                fileUpload = new FileUpload();

                fileUpload.setTransferKey(uploadKey);
                fileUpload.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
                fileUpload.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID));
                fileUpload.setGroupName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_GROUP));
                fileUpload.setComputerName(resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME));
                fileUpload.setFilename(resultSet.getString(DatabaseConstants.COLUMN_NAME_FILENAME));
                fileUpload.setDirectory(resultSet.getString(DatabaseConstants.COLUMN_NAME_DIRECTORY));
                fileUpload.setFileSize(resultSet.getLong(DatabaseConstants.COLUMN_NAME_FILE_SIZE));
                fileUpload.setUploadGroupId(resultSet.getString(DatabaseConstants.COLUMN_NAME_UPLOAD_GROUP_ID));
                fileUpload.setStartTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_START_TIMESTAMP));
                fileUpload.setEndTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_END_TIMESTAMP));
                fileUpload.setStatus(resultSet.getString(DatabaseConstants.COLUMN_NAME_STATUS));
                fileUpload.setTmpFile(resultSet.getString(DatabaseConstants.COLUMN_NAME_TMP_FILE));
                fileUpload.setTransferredByteIndex(resultSet.getLong(DatabaseConstants.COLUMN_NAME_TRANSFERRED_BYTE_INDEX));
                fileUpload.setSourceFileLastModifiedTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_SOURCE_FILE_LAST_MODIFIED));
            }
        } catch (Exception e) {
            fileUpload = null;

            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on finding file upload for upload key: %s\nerror message:\n%s", uploadKey, errorMessage), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return fileUpload;
    }



    public String findFileUploadTmpFileForUploadKey(String uploadKey) {
        String tmpFile = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_UPLOADED_TMP_FILE_BY_UPLOAD_KEY);

            pStatement.setString(1, uploadKey);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                tmpFile = resultSet.getString(DatabaseConstants.COLUMN_NAME_TMP_FILE);
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on finding file upload tmp file for upload key: %s\nerror message:\n%s", uploadKey, errorMessage), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return tmpFile;
    }

    public List<TransferHistoryModel> findFileUploadedForUser(String userId, boolean successOnly, TransferHistoryTimeType type) throws Exception {
        List<TransferHistoryModel> histories = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            if (successOnly) {
                switch (type) {
                    case TRANSFER_HISTORY_TYPE_LATEST_WEEK:
                        pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SUCCESS_FILE_UPLOADED_WITH_TIME_RANGE_BY_USER);
                        pStatement.setString(1, userId);
                        pStatement.setLong(2, Utility.lastWeekTimestamp());
                        pStatement.setLong(3, System.currentTimeMillis());

                        break;
                    case TRANSFER_HISTORY_TYPE_LATEST_MONTH:
                        pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SUCCESS_FILE_UPLOADED_WITH_TIME_RANGE_BY_USER);
                        pStatement.setString(1, userId);
                        pStatement.setLong(2, Utility.lastMonthTimestamp());
                        pStatement.setLong(3, System.currentTimeMillis());

                        break;
                    case TRANSFER_HISTORY_TYPE_ALL:
                        pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SUCCESS_FILE_UPLOADED_BY_USER);
                        pStatement.setString(1, userId);

                        break;
                    default:
                        // TRANSFER_HISTORY_TYPE_LATEST_20
                        pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SUCCESS_FILE_UPLOADED_LATEST_20_BY_USER);
                        pStatement.setString(1, userId);
                }
            } else {
                switch (type) {
                    case TRANSFER_HISTORY_TYPE_LATEST_WEEK:
                        pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_UPLOADED_WITH_TIME_RANGE_BY_USER);
                        pStatement.setString(1, userId);
                        pStatement.setLong(2, Utility.lastWeekTimestamp());
                        pStatement.setLong(3, System.currentTimeMillis());

                        break;
                    case TRANSFER_HISTORY_TYPE_LATEST_MONTH:
                        pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_UPLOADED_WITH_TIME_RANGE_BY_USER);
                        pStatement.setString(1, userId);
                        pStatement.setLong(2, Utility.lastMonthTimestamp());
                        pStatement.setLong(3, System.currentTimeMillis());

                        break;
                    case TRANSFER_HISTORY_TYPE_ALL:
                        pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_ALL_FILE_UPLOADED_BY_USER);
                        pStatement.setString(1, userId);

                        break;
                    default:
                        // TRANSFER_HISTORY_TYPE_LATEST_20
                        pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_UPLOADED_LATEST_20_BY_USER);
                        pStatement.setString(1, userId);
                }
            }

            resultSet = pStatement.executeQuery();

            for (; resultSet.next();) {
                String computerGroup = resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_GROUP);
                String computerName = resultSet.getString(DatabaseConstants.COLUMN_NAME_COMPUTER_NAME);
                long fileSize = resultSet.getLong(DatabaseConstants.COLUMN_NAME_FILE_SIZE);
                String filename = FilenameUtils.getName(resultSet.getString(DatabaseConstants.COLUMN_NAME_FILENAME));
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

//    public List<TransferHistoryModel> findAllFileUploadedForUser(String userId, boolean successOnly) throws Exception {
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
//                pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SUCCESS_FILE_UPLOADED_BY_USER);
//            } else {
//                pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_ALL_FILE_UPLOADED_BY_USER);
//            }
//
//            pStatement.setString(1, userId);
//
//            resultSet = pStatement.executeQuery();
//
//            for (; resultSet.next();) {
//                long fileSize = resultSet.getLong(DatabaseConstants.COLUMN_NAME_FILE_SIZE);
//                String filename = FilenameUtils.getName(resultSet.getString(DatabaseConstants.COLUMN_NAME_FILENAME));
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

//    public void updateStatusProcessingToFailureForTimeoutStartTimestamp(long minimumTimestamp) {
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//        ResultSet resultSet = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_PROCESSING_FILE_UPLOADED_BY_TIMEOUT_START_TIMESTAMP);
//            pStatement.setLong(1, minimumTimestamp);
//
//            boolean found = false;
//
//            resultSet = pStatement.executeQuery();
//
//            for (; resultSet.next(); ) {
//                found = true;
//
//                String uploadKey = resultSet.getString(DatabaseConstants.COLUMN_NAME_UPLOAD_KEY);
//                String userId = resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID);
//                String filename = resultSet.getString(DatabaseConstants.COLUMN_NAME_FILENAME);
//                String directory = resultSet.getString(DatabaseConstants.COLUMN_NAME_DIRECTORY);
//                long fileSize = resultSet.getLong(DatabaseConstants.COLUMN_NAME_FILE_SIZE);
//                long startTimestamp = resultSet.getLong(DatabaseConstants.COLUMN_NAME_START_TIMESTAMP);
//                String status = resultSet.getString(DatabaseConstants.COLUMN_NAME_STATUS);
//
//                String message = String.format("Timeout file-upload found.\nupload key: %s\nuser: %s\nfilename: %s\ndirectory: %s\nfile size in bytes: %s\nstart timestamp: %s\nstatus: %s\n", uploadKey, userId, filename, directory, String.valueOf(fileSize), String.valueOf(startTimestamp), status);
//                LOGGER.info(message);
//            }
//
//            if (found) {
//                LOGGER.info("Start updating upload status to failure for timeout start-timestamp");
//
//                resultSet.close();
//                pStatement.close();
//
//                pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_UPLOADED_STATUS_PROCESSING_TO_FAILURE_FOR_TIMEOUT_START_TIMESTAMP);
//                pStatement.setLong(1, minimumTimestamp);
//
//                pStatement.executeUpdate();
//            }
//        } catch (Exception e) {
//            String errorMessage = e.getMessage();
//
//            LOGGER.error(String.format("Error on updating file upload status to failure for timeout transfer: %s\nerror message:\n%s", String.valueOf(minimumTimestamp), errorMessage), e);
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

    /**
     * @return 0 if not found for the uploadKey or any error occurred
     */
    public long findFileUploadSizeForUploadKey(String uploadKey) {
        long fileSize = 0;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_UPLOADED_SIZE_BY_UPLOAD_KEY);

            pStatement.setString(1, uploadKey);

            resultSet = pStatement.executeQuery();

            if(resultSet.next()) {
                fileSize = resultSet.getLong(DatabaseConstants.COLUMN_NAME_FILE_SIZE);
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on finding file size of the uploaded file for upload key: %s\nerror message:\n%s", uploadKey, errorMessage), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return fileSize;
    }

    public long sumUploadingFileSizeForUser(String userId) {
        long fileSizeSum = 0;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_SUM_UPLOADING_FILE_SIZE_BY_USER);

            pStatement.setString(1, userId);

            resultSet = pStatement.executeQuery();

            if(resultSet.next()) {
                Object tmpValue = resultSet.getObject(1);

                if (tmpValue != null && Number.class.isInstance(tmpValue)) {
                    fileSizeSum = ((Number) tmpValue).longValue();
//                } else {
//                    LOGGER.error("Data type for sum of column: " + DatabaseConstants.TABLE_NAME_FILE_UPLOADED + "." + DatabaseConstants.COLUMN_NAME_FILE_SIZE + " is not type of Number, but " + tmpValue.getClass().getName());
                }

//                BigDecimal tmp = resultSet.getBigDecimal(1);
//
//                fileSizeSum = tmp.longValue();
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on finding sum-up file size of the uploading files for user: %s\nerror message:\n%s", userId, errorMessage), e);
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

    public void updateStatusProcessingToDeviceUploadedButUnconfirmed(String transferKey) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            /* for file status is still processing, not success or failed */

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_UPLOADED_STATUS_PROCESSING_TO_DEVICE_UPLOADED_BUT_UNCONFIRMED);

            pStatement.setString(1, transferKey);
            pStatement.executeUpdate();

//            LOGGER.info(String.format("File upload status updated from %s to %s for upload key=%s", DatabaseConstants.TRANSFER_STATUS_PROCESSING, DatabaseConstants.TRANSFER_STATUS_DEVICE_UPLOADED_BUT_UNCONFIRMED, transferKey));
        } catch (Exception e) {
            LOGGER.error(String.format("Error on updating file upload status from %s to %s for upload key=%s.", DatabaseConstants.TRANSFER_STATUS_PROCESSING, DatabaseConstants.TRANSFER_STATUS_DEVICE_UPLOADED_BUT_UNCONFIRMED, transferKey), e);
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

    public void updateFileUploadComputerName(Long computerId, String computerGroup, String computerName) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_UPLOADED_COMPUTER_NAME);
            pStatement.setString(1, computerGroup);
            pStatement.setString(2, computerName);
            pStatement.setLong(3, computerId);

            pStatement.executeUpdate();

            LOGGER.debug(String.format("Updated file upload computer group to '%s', computer name to '%s' with computer id '%d'.", computerGroup, computerName, computerId));
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on updating file upload computer group to '%s', computer name to '%s' with computer id '%d'\nerror message:\n%s", computerGroup, computerName, computerId, errorMessage), e);
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

    public List<ConfirmTransferModel> findFileUploadStatusForUploadKeys(List<String> uploadKeys) {
        List<ConfirmTransferModel> confirmTransferModels = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            String queryString = DatabaseConstants.SQL_FIND_FILE_UPLOADED_STATUS_BY_UPLOAD_KEY_ARRAY_PREFIX + convertToInClauseStringFrom(uploadKeys) + DatabaseConstants.SQL_FIND_FILE_UPLOADED_STATUS_BY_UPLOAD_KEY_ARRAY_SUFFIX;

            pStatement = conn.prepareStatement(queryString);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                ConfirmTransferModel confirmTransferModel = new ConfirmTransferModel();

                confirmTransferModel.setTransferKey(resultSet.getString(DatabaseConstants.COLUMN_NAME_UPLOAD_KEY));
                confirmTransferModel.setStatus(resultSet.getString(DatabaseConstants.COLUMN_NAME_STATUS));

                confirmTransferModels.add(confirmTransferModel);
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on finding upload status of the file upload for upload keys: %s\nerror message:\n%s", Arrays.toString(uploadKeys.toArray()), errorMessage), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return confirmTransferModels;
    }

    public String findFileUploadStatusForUploadKey(String transferKey) {
        String status = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_UPLOADED_STATUS_BY_UPLOAD_KEY);

            pStatement.setString(1, transferKey);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                status = resultSet.getString(DatabaseConstants.COLUMN_NAME_STATUS);
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on finding upload status of the file upload for upload key: %s\nerror message:\n%s", transferKey, errorMessage), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return status;
    }

    public boolean deleteFileUploadedForUploadKey(String uploadKey) {
        boolean success = false;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_FILE_UPLOADED_BY_UPLOAD_KEY);
            pStatement.setString(1, uploadKey);

            pStatement.executeUpdate();

            success = true;

            LOGGER.debug(String.format("Deleted file uploaded for upload key: '%s'.", uploadKey));
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on deleting file uploaded for upload key: '%s'\nerror message:\n%s", uploadKey, errorMessage), e);
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

    public boolean existingFileUploadForUploadGroupId(String uploadGroupId) {
        boolean exists = false;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_IF_EXISTS_FILE_UPLOADED_BY_UPLOAD_GROUP_ID);

            pStatement.setString(1, uploadGroupId);

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

    public void updateFileUploadToIpHost(String transferKey, String uploadToIp, String uploadToHost) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_UPLOADED_TO_IP_HOST);

            pStatement.setString(1, uploadToIp);
            pStatement.setString(2, uploadToHost);
            pStatement.setString(3, transferKey);

            pStatement.executeUpdate();
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on updating file upload with transfer key '%s', from ip '%s', from host '%s'.\nerror message:\n%s", transferKey, uploadToIp, uploadToHost, errorMessage), e);
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

    public boolean updateWithNewTmpFile(String transferKey, String tmpFilePath, long uploadFileSize, long tmpFileCreatedTimestamp, String status, long sourceFileLastModifiedTimestamp, String fromIp, String fromHost) {
        boolean success;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_UPLOADED_WITH_NEW_TMP_FILE);
            pStatement.setString(1, tmpFilePath);
            pStatement.setLong(2, uploadFileSize);
            pStatement.setLong(3, tmpFileCreatedTimestamp);
            pStatement.setLong(4, 0);
            pStatement.setString(5, status);
            pStatement.setLong(6, -1);
            pStatement.setLong(7, sourceFileLastModifiedTimestamp);
            pStatement.setString(8, fromIp);
            pStatement.setString(9, fromHost);
            pStatement.setString(10, null);
            pStatement.setString(11, null);
            pStatement.setString(12, transferKey);

            pStatement.executeUpdate();

            success = true;

            LOGGER.debug(String.format("Updated file upload with new tmp file '%s', upload key='%s'" +
                                       "\nupload file size=%s" +
                                       "\nsource file last modified=%s" +
                                       "\nfrom IP=%s" +
                                       "\nfrom Host=%s", tmpFilePath, transferKey, String.valueOf(uploadFileSize), String.valueOf(sourceFileLastModifiedTimestamp), fromIp, fromHost));
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on updating file upload with new tmp file '%s', upload key='%s'" +
                                       "\nupload file size=%s" +
                                       "\nsource file last modified=%s" +
                                       "\nfrom IP=%s" +
                                       "\nfrom Host=%s", tmpFilePath, transferKey, String.valueOf(uploadFileSize), String.valueOf(sourceFileLastModifiedTimestamp), fromIp, fromHost), e);
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

    public boolean updateWithExistingTmpFile(String uploadKey, long startTimestamp, String status, String fromIp, String fromHost) {
        boolean success;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_UPLOADED_WITH_EXISTING_TMP_FILE);
            pStatement.setLong(1, startTimestamp);
            pStatement.setLong(2, 0);
            pStatement.setString(3, status);
            pStatement.setString(4, fromIp);
            pStatement.setString(5, fromHost);
            pStatement.setString(6, null);
            pStatement.setString(7, null);
            pStatement.setString(8, uploadKey);

            pStatement.executeUpdate();

            success = true;

            LOGGER.debug(String.format("Updated file upload with existing tmp file. Upload key='%s'" +
                                       "\nfrom IP=%s" +
                                       "\nfrom Host=%s", uploadKey, fromIp, fromHost));
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on updating file upload with existing tmp file. Upload key='%s'" +
                                       "\nfrom IP=%s" +
                                       "\nfrom Host=%s", uploadKey, fromIp, fromHost), e);
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

    public boolean updateTmpFileWrittenResult(String transferKey, String status, long transferredByteIndex) {
        boolean success;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_UPLOADED_WITH_TMP_FILE_WRITTEN_RESULT);
            pStatement.setString(1, status);
            pStatement.setLong(2, transferredByteIndex);
            pStatement.setString(3, transferKey);

            pStatement.executeUpdate();

            success = true;

            LOGGER.debug(String.format("Updated file upload tmp file written result with upload key='%s'" +
                                       "\nstatus=%s" +
                                       "\ntransferred byte index=%s", transferKey, status, String.valueOf(transferredByteIndex)));
        } catch (Exception e) {
            success = false;;

            LOGGER.error(String.format("Error on updating file upload tmp file written result with upload key='%s'" +
                                       "\nstatus=%s" +
                                       "\ntransferred byte index=%s", transferKey, status, String.valueOf(transferredByteIndex)), e);
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
