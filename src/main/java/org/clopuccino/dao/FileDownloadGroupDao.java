package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.FileDownloadGroup;
import org.clopuccino.domain.FileDownloadGroupDetail;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * <code>FileDownloadGroupDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class FileDownloadGroupDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FileDownloadGroupDao.class.getSimpleName());

    public FileDownloadGroupDao() {
        super();
    }

    public FileDownloadGroupDao(DatabaseAccess dbAccess) {
        super(dbAccess);
    }

    public boolean createMasterTableIfNotExists() {
        boolean success = true;

        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dbAccess.getConnection();

            DatabaseMetaData dbMetaData = conn.getMetaData();

            /* check if table exists */
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_FILE_DOWNLOAD_GROUP, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_FILE_DOWNLOAD_GROUP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOAD_GROUP_CREATED_TIMESTAMP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_FILE_DOWNLOAD_GROUP_AUTH_USER);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_FILE_DOWNLOAD_GROUP_COMPUTER_ID);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_FILE_DOWNLOAD_GROUP, e.getMessage()), e);
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

    public boolean createDetailTableIfNotExists() {
        boolean success = true;

        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dbAccess.getConnection();

            DatabaseMetaData dbMetaData = conn.getMetaData();

            /* check if table exists */
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_FILE_DOWNLOAD_GROUP_DETAIL, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_FILE_DOWNLOAD_GROUP_DETAIL);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_FILE_DOWNLOAD_GROUP_DETAIL_DOWNLOAD_GROUP_ID);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOAD_GROUP_DETAIL_GROUP_ID);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_DOWNLOAD_GROUP_DETAIL_DOWNLOAD_KEY);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_FILE_DOWNLOAD_GROUP_DETAIL, e.getMessage()), e);
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

    public String findFileDownloadGroupIdByDownloadKey(String downloadKey) {
        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        String fileDownloadGroupId = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_DOWNLOAD_GROUP_DETAIL_BY_DOWNLOAD_KEY);

            pStatement.setString(1, downloadKey);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                fileDownloadGroupId = resultSet.getString(DatabaseConstants.COLUMN_NAME_DOWNLOAD_GROUP_ID);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to find file download group id for download key: " + downloadKey, e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return fileDownloadGroupId;
    }

    public FileDownloadGroupDetail findFileDownloadGroupDetailByDownloadKey(String downloadKey) {
        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        FileDownloadGroupDetail fileDownloadGroupDetail = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_DOWNLOAD_GROUP_DETAIL_BY_DOWNLOAD_KEY);

            pStatement.setString(1, downloadKey);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                fileDownloadGroupDetail = new FileDownloadGroupDetail();

                fileDownloadGroupDetail.setDownloadGroupDetailId(resultSet.getString(DatabaseConstants.COLUMN_NAME_FILE_DOWNLOAD_GROUP_DETAIL_ID));
                fileDownloadGroupDetail.setDownloadGroupId(resultSet.getString(DatabaseConstants.COLUMN_NAME_DOWNLOAD_GROUP_ID));
                fileDownloadGroupDetail.setDownloadKey(resultSet.getString(DatabaseConstants.COLUMN_NAME_DOWNLOAD_KEY));
                fileDownloadGroupDetail.setFilePath(resultSet.getString(DatabaseConstants.COLUMN_NAME_FILE_PATH));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to find file download group detail for download key: " + downloadKey, e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return fileDownloadGroupDetail;
    }

    public boolean deleteFileDownloadGroupById(String downloadGroupId) {
        boolean success = false;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_FILE_DOWNLOAD_GROUP_BY_DOWNLOAD_GROUP_ID);
            pStatement.setString(1, downloadGroupId);

            pStatement.executeUpdate();

            success = true;

            LOGGER.debug(String.format("Deleted file download group detail for download group id: '%s'.", downloadGroupId));
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on deleting file download group detail for download group id: '%s'\nerror message:\n%s", downloadGroupId, errorMessage), e);
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

    public boolean createFileDownloadGroupWithDetail(FileDownloadGroup fileDownloadGroup) {
        String downloadGroupId = fileDownloadGroup.getDownloadGroupId();
        String downloadGroupDirectory = fileDownloadGroup.getDownloadGroupDirectory();
        Integer subdirectoryType = fileDownloadGroup.getSubdirectoryType() != null ? fileDownloadGroup.getSubdirectoryType() : FileDownloadGroup.DEFAULT_SUBDIRECTORY_TYPE;
        Integer descriptionType = fileDownloadGroup.getDescriptionType() != null ? fileDownloadGroup.getDescriptionType() : FileDownloadGroup.DEFAULT_DESCRIPTION_TYPE;
        Integer notificationType = fileDownloadGroup.getNotificationType() != null ? fileDownloadGroup.getNotificationType() : FileDownloadGroup.DEFAULT_NOTIFICATION_TYPE;
        String subdirectoryValue = fileDownloadGroup.getSubdirectoryValue() != null ? fileDownloadGroup.getSubdirectoryValue() : "";
        String descriptionValue = fileDownloadGroup.getDescriptionValue() != null ? fileDownloadGroup.getDescriptionValue() : "";
        String userId = fileDownloadGroup.getUserId();
        Long computerId = fileDownloadGroup.getComputerId();

        Map<String, String> downloadKeyAndPaths = fileDownloadGroup.getDownloadKeyAndPaths();

        boolean success = createFileDownloadGroup(downloadGroupId, downloadGroupDirectory, subdirectoryType, descriptionType, notificationType, subdirectoryValue, descriptionValue, userId, computerId);

        if (success && downloadKeyAndPaths != null && downloadKeyAndPaths.size() > 0) {
            success = createFileDownloadGroupDetail(downloadGroupId, downloadKeyAndPaths);
        }

        return success;
    }

    public boolean createFileDownloadGroup(String downloadGroupId,
                                           String downloadGroupDirectory,
                                           Integer subdirectoryType,
                                           Integer descriptionType,
                                           Integer notificationType,
                                           String subdirectoryValue,
                                           String descriptionValue,
                                           String userId,
                                           Long computerId) {
        boolean success = false;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_FILE_DOWNLOAD_GROUP);
            pStatement.setString(1, downloadGroupId);
            pStatement.setString(2, downloadGroupDirectory);
            pStatement.setInt(3, subdirectoryType);
            pStatement.setInt(4, descriptionType);
            pStatement.setInt(5, notificationType);
            pStatement.setString(6, subdirectoryValue);
            pStatement.setString(7, descriptionValue);
            pStatement.setLong(8, System.currentTimeMillis());
            pStatement.setString(9, userId);
            pStatement.setLong(10, computerId);

            pStatement.executeUpdate();

            success = true;

            LOGGER.debug(String.format("File download group created for user '%s'\ncomputerId=%d\ngroupId=%s\ndownloadDirectory='%s'\nsubdirectoryType=%d\ndescriptionType=%d\nnotificationType=%d\nsubdirectoryValue=%s\ndescriiptionValue=%s\n", userId, computerId, downloadGroupId, downloadGroupDirectory, subdirectoryType, descriptionType, notificationType, subdirectoryValue, descriptionValue));
        } catch (Exception e) {
            success = false;

            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on creating file download group for user '%s'\ncomputerId=%d\ngroupId=%s\ndownloadDirectory='%s'\nsubdirectoryType=%d\ndescriptionType=%d\nnotificationType=%d\nsubdirectoryValue=%s\ndescriiptionValue=%s\nError message:\n%s\n", userId, computerId, downloadGroupId, downloadGroupDirectory, subdirectoryType, descriptionType, notificationType, subdirectoryValue, descriptionValue, errorMessage), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    // ignored
                }
            }
        }

        return success;
    }

    public String generateDownloadGroupDetailId(String downloadGroupId, String downloadKey) {
        return downloadGroupId + "+" + downloadKey;
    }

    public boolean createFileDownloadGroupDetail(String downloadGroupId, Map<String, String> downloadKeyAndPaths) {
        boolean success = false;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            if (downloadKeyAndPaths != null && downloadKeyAndPaths.size() > 0) {
                Set<Map.Entry<String, String>> keyAndPathSet = downloadKeyAndPaths.entrySet();

                for (Map.Entry<String, String> entry : keyAndPathSet) {
                    String downloadKey = entry.getKey();
                    String filePath = entry.getValue();

                    String fileDownloadGroupDetailId = generateDownloadGroupDetailId(downloadGroupId, downloadKey);

                    pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_FILE_DOWNLOAD_GROUP_DETAIL);

                    pStatement.setString(1, fileDownloadGroupDetailId);
                    pStatement.setString(2, downloadGroupId);
                    pStatement.setString(3, downloadKey);
                    pStatement.setString(4, filePath);

                    pStatement.executeUpdate();

                    pStatement.close();
                }
            }

            success = true;
        } catch (Exception e) {
            success = false;

            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on creating file download group detail.\ngroupId=%s\ndownload key&path='%s'\nError message:\n%s\n", downloadGroupId, downloadKeyAndPaths.toString(), errorMessage), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    // ignored
                }
            }
        }

        return success;
    }

    public boolean createFileDownloadGroupDetail(String downloadGroupId, String downloadKey, String filePath) {
        boolean success = false;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            String fileDownloadGroupDetailId = generateDownloadGroupDetailId(downloadGroupId, downloadKey);

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_FILE_DOWNLOAD_GROUP_DETAIL);

            pStatement.setString(1, fileDownloadGroupDetailId);
            pStatement.setString(2, downloadGroupId);
            pStatement.setString(3, downloadKey);
            pStatement.setString(4, filePath);

            pStatement.executeUpdate();

            success = true;
        } catch (Exception e) {
            success = false;

            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on creating single file download group detail.\ngroupId=%s\ndownload key='%s'\nfile path='%s'\nError message:\n%s\n", downloadGroupId, downloadKey, filePath, errorMessage), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    // ignored
                }
            }
        }

        return success;
    }

    public boolean deleteFileDownloadGroupDetailById(String fileDownloadGroupDetailId) {
        boolean success = false;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_FILE_DOWNLOAD_GROUP_DETAIL_BY_ID);
            pStatement.setString(1, fileDownloadGroupDetailId);

            pStatement.executeUpdate();

            success = true;

            LOGGER.debug(String.format("Deleted file download group detail by id: '%s'.", fileDownloadGroupDetailId));
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on deleting file download group detail by id: '%s'\nerror message:\n%s", fileDownloadGroupDetailId, errorMessage), e);
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




//    public boolean existingFileDownloadGroupDetailForDownloadKey(String downloadKey) {
//        boolean found = false;
//
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//        ResultSet resultSet = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_IF_EXISTS_FILE_DOWNLOAD_GROUP_DETAIL_BY_DOWNLOAD_KEY);
//
//            pStatement.setString(1, downloadKey);
//
//            resultSet = pStatement.executeQuery();
//
//            found = resultSet.next();
//        } catch (Exception e) {
//            LOGGER.error("Failed to find if file download group detail exists for download key: " + downloadKey, e);
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
//        return found;
//    }
//
//    public boolean updateFileDownloadGroupCreatedInDesktopStatus(String downloadGroupId, long createdInDesktopTimestamp, String createdInDesktopStatus) {
//        int updateCount = 0;
//
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_DOWNLOAD_GROUP_CREATED_IN_DESKTOP_STATUS);
//            pStatement.setLong(1, createdInDesktopTimestamp);
//            pStatement.setString(2, createdInDesktopStatus);
//            pStatement.setString(3, downloadGroupId);
//
//            updateCount = pStatement.executeUpdate();
//
//            if (LOGGER.isDebugEnabled()) {
//                String createdTimestampString;
//
//                if (createdInDesktopTimestamp > 0) {
//                    try {
//                        SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
//
//                        createdTimestampString = format.format(new Date(createdInDesktopTimestamp));
//                    } catch (Exception e1) {
//                        createdTimestampString = String.valueOf(createdInDesktopTimestamp);
//                    }
//                } else {
//                    createdTimestampString = "(Not created in desktop)";
//                }
//
//                LOGGER.debug(String.format("File download group updated created-in-desktop status for groupId=%s\ncreated-in-desktop timestamp=%s\ncreated-in-desktop status=%s\n", downloadGroupId, createdTimestampString, createdInDesktopStatus));
//            }
//        } catch (Exception e) {
//            String errorMessage = e.getMessage();
//
//            String createdTimestampString;
//
//            if (createdInDesktopTimestamp > 0) {
//                try {
//                    SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
//
//                    createdTimestampString = format.format(new Date(createdInDesktopTimestamp));
//                } catch (Exception e1) {
//                    createdTimestampString = String.valueOf(createdInDesktopTimestamp);
//                }
//            } else {
//                createdTimestampString = "(Not created in desktop)";
//            }
//
//            LOGGER.error(String.format("Error on updating file download group created-in-desktop status for groupId=%s\ncreated-in-desktop timestamp=%s\ncreated-in-desktop status=%s\nError message:\n%s\n", downloadGroupId, createdTimestampString, createdInDesktopStatus, errorMessage), e);
//        } finally {
//            if (dbAccess != null) {
//                try {
//                    dbAccess.close(null, null, pStatement, conn);
//                } catch (Exception e) {
//                    /* ignored */
//                }
//            }
//        }
//
//        return updateCount > 0;
//    }
//
//    public List<String> findDownloadKeysByDownloadGroupId(String downloadGroupId) {
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//        ResultSet resultSet = null;
//
//        List<String> downloadKeys = new ArrayList<>();
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_DOWNLOAD_GROUP_DETAIL_BY_GROUP_ID);
//
//            pStatement.setString(1, downloadGroupId);
//
//            resultSet = pStatement.executeQuery();
//
//            for (; resultSet.next(); ) {
//                downloadKeys.add(resultSet.getString(DatabaseConstants.COLUMN_NAME_DOWNLOAD_KEY));
//            }
//        } catch (Exception e) {
//            LOGGER.error("Failed to find file download group detail for download group id: " + downloadGroupId, e);
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
//        return downloadKeys;
//    }
//
//    public FileDownloadGroup findFileDownloadGroupByDownloadGroupId(String downloadGroupId, boolean includingDownloadKeys) {
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//        ResultSet resultSet = null;
//
//        FileDownloadGroup fileDownloadGroup = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_DOWNLOAD_GROUP_BY_ID);
//
//            pStatement.setString(1, downloadGroupId);
//
//            resultSet = pStatement.executeQuery();
//
//            if (resultSet.next()) {
//                fileDownloadGroup = new FileDownloadGroup();
//
//                fileDownloadGroup.setDownloadGroupId(resultSet.getString(DatabaseConstants.COLUMN_NAME_DOWNLOAD_GROUP_ID));
//                fileDownloadGroup.setDownloadGroupDirectory(resultSet.getString(DatabaseConstants.COLUMN_NAME_DOWNLOAD_GROUP_DIRECTORY));
//                fileDownloadGroup.setSubdirectoryType(resultSet.getInt(DatabaseConstants.COLUMN_NAME_DOWNLOAD_SUBDIRECTORY_TYPE));
//                fileDownloadGroup.setDescriptionType(resultSet.getInt(DatabaseConstants.COLUMN_NAME_DOWNLOAD_DESCRIPTION_TYPE));
//                fileDownloadGroup.setNotificationType(resultSet.getInt(DatabaseConstants.COLUMN_NAME_DOWNLOAD_NOTIFICATION_TYPE));
//                fileDownloadGroup.setSubdirectoryValue(resultSet.getString(DatabaseConstants.COLUMN_NAME_DOWNLOAD_SUBDIRECTORY_VALUE));
//                fileDownloadGroup.setDescriptionValue(resultSet.getString(DatabaseConstants.COLUMN_NAME_DOWNLOAD_DESCRIPTION_VALUE));
//                fileDownloadGroup.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
//                fileDownloadGroup.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID));
//                fileDownloadGroup.setCreatedInDesktopTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_CREATED_IN_DESKTOP_TIMESTAMP));
//                fileDownloadGroup.setCreatedInDesktopStatus(resultSet.getString(DatabaseConstants.COLUMN_NAME_CREATED_IN_DESKTOP_STATUS));
//            }
//        } catch (Exception e) {
//            LOGGER.error("Failed to find file download group for download group id: " + downloadGroupId, e);
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
//        if (includingDownloadKeys && fileDownloadGroup != null) {
//            fileDownloadGroup.setDownloadKeys(findDownloadKeysByDownloadGroupId(fileDownloadGroup.getDownloadGroupId()));
//        }
//
//        return fileDownloadGroup;
//    }
//
//    public List<FileDownloadGroup> findNotCreatedFileDownloadGroupsByUserComputer(String userId, Long computerId, boolean includingDownloadKeys) {
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//        ResultSet resultSet = null;
//
//        List<FileDownloadGroup> fileDownloadGroups = new ArrayList<>();
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_NOT_CREATED_DOWNLOAD_GROUP_BY_USER_COMPUTER);
//
//            pStatement.setString(1, userId);
//            pStatement.setLong(2, computerId);
//
//            resultSet = pStatement.executeQuery();
//
//            for (; resultSet.next(); ) {
//                FileDownloadGroup fileDownloadGroup = new FileDownloadGroup();
//
//                fileDownloadGroup.setDownloadGroupId(resultSet.getString(DatabaseConstants.COLUMN_NAME_DOWNLOAD_GROUP_ID));
//                fileDownloadGroup.setDownloadGroupDirectory(resultSet.getString(DatabaseConstants.COLUMN_NAME_DOWNLOAD_GROUP_DIRECTORY));
//                fileDownloadGroup.setSubdirectoryType(resultSet.getInt(DatabaseConstants.COLUMN_NAME_DOWNLOAD_SUBDIRECTORY_TYPE));
//                fileDownloadGroup.setDescriptionType(resultSet.getInt(DatabaseConstants.COLUMN_NAME_DOWNLOAD_DESCRIPTION_TYPE));
//                fileDownloadGroup.setNotificationType(resultSet.getInt(DatabaseConstants.COLUMN_NAME_DOWNLOAD_NOTIFICATION_TYPE));
//                fileDownloadGroup.setSubdirectoryValue(resultSet.getString(DatabaseConstants.COLUMN_NAME_DOWNLOAD_SUBDIRECTORY_VALUE));
//                fileDownloadGroup.setDescriptionValue(resultSet.getString(DatabaseConstants.COLUMN_NAME_DOWNLOAD_DESCRIPTION_VALUE));
//                fileDownloadGroup.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
//                fileDownloadGroup.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID));
//                fileDownloadGroup.setCreatedInDesktopTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_CREATED_IN_DESKTOP_TIMESTAMP));
//                fileDownloadGroup.setCreatedInDesktopStatus(resultSet.getString(DatabaseConstants.COLUMN_NAME_CREATED_IN_DESKTOP_STATUS));
//
//                if (includingDownloadKeys) {
//                    List<String> downloadKeys = findDownloadKeysByDownloadGroupId(fileDownloadGroup.getDownloadGroupId());
//
//                    fileDownloadGroup.setDownloadKeys(downloadKeys);
//                }
//            }
//        } catch (Exception e) {
//            LOGGER.error(String.format("Failed to find non-created file download groups for user: '%s',\ncomputer id: %d\nError message:\n%s\n", userId, computerId, e.getMessage()), e);
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
//        return fileDownloadGroups;
//    }
//
//    public boolean existingFileDownloadGroupForDownloadGroupId(String downloadGroupId) {
//        boolean exists = false;
//
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//        ResultSet resultSet = null;
//
//        FileDownloadGroup fileDownloadGroup = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_IF_EXISTS_FILE_DOWNLOAD_GROUP_BY_ID);
//
//            pStatement.setString(1, downloadGroupId);
//
//            resultSet = pStatement.executeQuery();
//
//            exists = resultSet.next();
//        } catch (Exception e) {
//            LOGGER.error("Failed to find if download group exists for download group id: " + downloadGroupId, e);
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
//        return exists;
//    }
//

}
