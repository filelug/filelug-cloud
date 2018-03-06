package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.clopuccino.Constants;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.FileUploadGroup;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <code>FileUploadGroupDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class FileUploadGroupDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FileUploadGroupDao.class.getSimpleName());


    public FileUploadGroupDao() {
        super();
    }

    public FileUploadGroupDao(DatabaseAccess dbAccess) {
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
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_FILE_UPLOAD_GROUP, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_FILE_UPLOAD_GROUP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_UPLOAD_GROUP_CREATED_IN_DESKTOP_TIMESTAMP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_UPLOAD_GROUP_CREATED_TIMESTAMP);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_FILE_UPLOAD_GROUP_AUTH_USER);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_FILE_UPLOAD_GROUP_COMPUTER_ID);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_FILE_UPLOAD_GROUP, e.getMessage()), e);
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
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_FILE_UPLOAD_GROUP_DETAIL, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_FILE_UPLOAD_GROUP_DETAIL);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_FOREIGN_KEY_FILE_UPLOAD_GROUP_DETAIL_UPLOAD_GROUP_ID);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_UPLOAD_GROUP_DETAIL_GROUP_ID);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_FILE_UPLOAD_GROUP_DETAIL_UPLOAD_KEY);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_FILE_UPLOAD_GROUP_DETAIL, e.getMessage()), e);
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

    public boolean createFileUploadGroupWithDetail(FileUploadGroup fileUploadGroup) {
        String uploadGroupId = fileUploadGroup.getUploadGroupId();
        String uploadGroupDirectory = fileUploadGroup.getUploadGroupDirectory();
        Integer subdirectoryType = fileUploadGroup.getSubdirectoryType() != null ? fileUploadGroup.getSubdirectoryType() : FileUploadGroup.DEFAULT_SUBDIRECTORY_TYPE;
        Integer descriptionType = fileUploadGroup.getDescriptionType() != null ? fileUploadGroup.getDescriptionType() : FileUploadGroup.DEFAULT_DESCRIPTION_TYPE;
        Integer notificationType = fileUploadGroup.getNotificationType() != null ? fileUploadGroup.getNotificationType() : FileUploadGroup.DEFAULT_NOTIFICATION_TYPE;
        String subdirectoryValue = fileUploadGroup.getSubdirectoryValue() != null ? fileUploadGroup.getSubdirectoryValue() : "";
        String descriptionValue = fileUploadGroup.getDescriptionValue() != null ? fileUploadGroup.getDescriptionValue() : "";
        String userId = fileUploadGroup.getUserId();
        Long computerId = fileUploadGroup.getComputerId();
        Long createdInDesktopTimestamp = fileUploadGroup.getCreatedInDesktopTimestamp() != null ? fileUploadGroup.getCreatedInDesktopTimestamp() : 0;
        String createInDesktopStatus = fileUploadGroup.getCreatedInDesktopStatus() != null ? fileUploadGroup.getCreatedInDesktopStatus() : "";

        List<String> uploadKeys = fileUploadGroup.getUploadKeys();

        boolean success = createFileUploadGroup(uploadGroupId, uploadGroupDirectory, subdirectoryType, descriptionType, notificationType, subdirectoryValue, descriptionValue, userId, computerId, createdInDesktopTimestamp, createInDesktopStatus);

        if (success && uploadKeys != null && uploadKeys.size() > 0) {
            success = createFileUploadGroupDetail(uploadGroupId, uploadKeys);
        }

        return success;
    }

    public boolean createFileUploadGroup(String uploadGroupId,
                                         String uploadGroupDirectory,
                                         Integer subdirectoryType,
                                         Integer descriptionType,
                                         Integer notificationType,
                                         String subdirectoryValue,
                                         String descriptionValue,
                                         String userId,
                                         Long computerId,
                                         long createdInDesktopTimestamp,
                                         String createdInDesktopStatus) {
        boolean success = false;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_FILE_UPLOAD_GROUP);
            pStatement.setString(1, uploadGroupId);
            pStatement.setString(2, uploadGroupDirectory);
            pStatement.setInt(3, subdirectoryType);
            pStatement.setInt(4, descriptionType);
            pStatement.setInt(5, notificationType);
            pStatement.setString(6, subdirectoryValue);
            pStatement.setString(7, descriptionValue);
            pStatement.setLong(8, System.currentTimeMillis());
            pStatement.setString(9, userId);
            pStatement.setLong(10, computerId);
            pStatement.setLong(11, createdInDesktopTimestamp);
            pStatement.setString(12, createdInDesktopStatus);

            pStatement.executeUpdate();

            success = true;

            if (LOGGER.isDebugEnabled()) {
                String createdTimestampString;

                if (createdInDesktopTimestamp > 0) {
                    try {
                        SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);

                        createdTimestampString = format.format(new Date(createdInDesktopTimestamp));
                    } catch (Exception e) {
                        createdTimestampString = String.valueOf(createdInDesktopTimestamp);
                    }
                } else {
                    createdTimestampString = "(Not created in desktop)";
                }

                LOGGER.debug(String.format("File upload group created for user '%s'\ncomputerId=%d\ngroupId=%s\nuploadDirectory='%s'\nsubdirectoryType=%d\ndescriptionType=%d\nnotificationType=%d\nsubdirectoryValue=%s\ndescriiptionValue=%s\ncreated-in-desktop timestamp=%s\n", userId, computerId, uploadGroupId, uploadGroupDirectory, subdirectoryType, descriptionType, notificationType, subdirectoryValue, descriptionValue, createdTimestampString));
            }
        } catch (Exception e) {
            success = false;

            String errorMessage = e.getMessage();

            String createdTimestampString;

            if (createdInDesktopTimestamp > 0) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);

                    createdTimestampString = format.format(new Date(createdInDesktopTimestamp));
                } catch (Exception e1) {
                    createdTimestampString = String.valueOf(createdInDesktopTimestamp);
                }
            } else {
                createdTimestampString = "(Not created in desktop)";
            }

            LOGGER.error(String.format("Error on creating file upload group for user '%s'\ncomputerId=%d\ngroupId=%s\nuploadDirectory='%s'\nsubdirectoryType=%d\ndescriptionType=%d\nnotificationType=%d\nsubdirectoryValue=%s\ndescriiptionValue=%s\ncreated-in-desktop timestamp=%s\nError message:\n%s\n", userId, computerId, uploadGroupId, uploadGroupDirectory, subdirectoryType, descriptionType, notificationType, subdirectoryValue, descriptionValue, createdTimestampString, errorMessage), e);
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

    public boolean createFileUploadGroupDetail(String uploadGroupId, List<String> uploadKeys) {
        boolean success = false;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            for (String uploadKey : uploadKeys) {
                String fileUploadGroupDetailId = generateUploadGroupDetailId(uploadGroupId, uploadKey);

                pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_FILE_UPLOAD_GROUP_DETAIL);

                pStatement.setString(1, fileUploadGroupDetailId);
                pStatement.setString(2, uploadGroupId);
                pStatement.setString(3, uploadKey);

                pStatement.executeUpdate();

                pStatement.close();
            }

            success = true;
        } catch (Exception e) {
            success = false;

            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on creating file upload group detail.\ngroupId=%s\nupload key='%s'\nError message:\n%s\n", uploadGroupId, Arrays.toString(uploadKeys.toArray(new String[0])), errorMessage), e);
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

    public boolean updateFileUploadGroupCreatedInDesktopStatus(String uploadGroupId, long createdInDesktopTimestamp, String createdInDesktopStatus) {
        int updateCount = 0;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_FILE_UPLOAD_GROUP_CREATED_IN_DESKTOP_STATUS);
            pStatement.setLong(1, createdInDesktopTimestamp);
            pStatement.setString(2, createdInDesktopStatus);
            pStatement.setString(3, uploadGroupId);

            updateCount = pStatement.executeUpdate();

            if (LOGGER.isDebugEnabled()) {
                String createdTimestampString;

                if (createdInDesktopTimestamp > 0) {
                    try {
                        SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);

                        createdTimestampString = format.format(new Date(createdInDesktopTimestamp));
                    } catch (Exception e1) {
                        createdTimestampString = String.valueOf(createdInDesktopTimestamp);
                    }
                } else {
                    createdTimestampString = "(Not created in desktop)";
                }

                LOGGER.debug(String.format("File upload group updated created-in-desktop status for groupId=%s\ncreated-in-desktop timestamp=%s\ncreated-in-desktop status=%s\n", uploadGroupId, createdTimestampString, createdInDesktopStatus));
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            String createdTimestampString;

            if (createdInDesktopTimestamp > 0) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);

                    createdTimestampString = format.format(new Date(createdInDesktopTimestamp));
                } catch (Exception e1) {
                    createdTimestampString = String.valueOf(createdInDesktopTimestamp);
                }
            } else {
                createdTimestampString = "(Not created in desktop)";
            }

            LOGGER.error(String.format("Error on updating file upload group created-in-desktop status for groupId=%s\ncreated-in-desktop timestamp=%s\ncreated-in-desktop status=%s\nError message:\n%s\n", uploadGroupId, createdTimestampString, createdInDesktopStatus, errorMessage), e);
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

    public FileUploadGroup findFileUploadGroupByUploadGroupId(String uploadGroupId, boolean includingUploadKeys) {
        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        FileUploadGroup fileUploadGroup = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_UPLOAD_GROUP_BY_ID);

            pStatement.setString(1, uploadGroupId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                fileUploadGroup = new FileUploadGroup();

                fileUploadGroup.setUploadGroupId(resultSet.getString(DatabaseConstants.COLUMN_NAME_UPLOAD_GROUP_ID));
                fileUploadGroup.setUploadGroupDirectory(resultSet.getString(DatabaseConstants.COLUMN_NAME_UPLOAD_GROUP_DIRECTORY));
                fileUploadGroup.setSubdirectoryType(resultSet.getInt(DatabaseConstants.COLUMN_NAME_UPLOAD_SUBDIRECTORY_TYPE));
                fileUploadGroup.setDescriptionType(resultSet.getInt(DatabaseConstants.COLUMN_NAME_UPLOAD_DESCRIPTION_TYPE));
                fileUploadGroup.setNotificationType(resultSet.getInt(DatabaseConstants.COLUMN_NAME_UPLOAD_NOTIFICATION_TYPE));
                fileUploadGroup.setSubdirectoryValue(resultSet.getString(DatabaseConstants.COLUMN_NAME_UPLOAD_SUBDIRECTORY_VALUE));
                fileUploadGroup.setDescriptionValue(resultSet.getString(DatabaseConstants.COLUMN_NAME_UPLOAD_DESCRIPTION_VALUE));
                fileUploadGroup.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
                fileUploadGroup.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID));
                fileUploadGroup.setCreatedInDesktopTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_CREATED_IN_DESKTOP_TIMESTAMP));
                fileUploadGroup.setCreatedInDesktopStatus(resultSet.getString(DatabaseConstants.COLUMN_NAME_CREATED_IN_DESKTOP_STATUS));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to find file upload group for upload group id: " + uploadGroupId, e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        if (includingUploadKeys && fileUploadGroup != null) {
            fileUploadGroup.setUploadKeys(findUploadKeysByUploadGroupId(fileUploadGroup.getUploadGroupId()));
        }

        return fileUploadGroup;
    }

    public List<String> findUploadKeysByUploadGroupId(String uploadGroupId) {
        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        List<String> uploadKeys = new ArrayList<>();

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_UPLOAD_GROUP_DETAIL_BY_GROUP_ID);

            pStatement.setString(1, uploadGroupId);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                uploadKeys.add(resultSet.getString(DatabaseConstants.COLUMN_NAME_UPLOAD_KEY));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to find file upload group detail for upload group id: " + uploadGroupId, e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return uploadKeys;
    }

    public List<FileUploadGroup> findNotCreatedFileUploadGroupsByUserComputer(String userId, Long computerId, boolean includingUploadKeys) {
        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        List<FileUploadGroup> fileUploadGroups = new ArrayList<>();

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_NOT_CREATED_UPLOAD_GROUP_BY_USER_COMPUTER);

            pStatement.setString(1, userId);
            pStatement.setLong(2, computerId);

            resultSet = pStatement.executeQuery();

            for (; resultSet.next(); ) {
                FileUploadGroup fileUploadGroup = new FileUploadGroup();

                fileUploadGroup.setUploadGroupId(resultSet.getString(DatabaseConstants.COLUMN_NAME_UPLOAD_GROUP_ID));
                fileUploadGroup.setUploadGroupDirectory(resultSet.getString(DatabaseConstants.COLUMN_NAME_UPLOAD_GROUP_DIRECTORY));
                fileUploadGroup.setSubdirectoryType(resultSet.getInt(DatabaseConstants.COLUMN_NAME_UPLOAD_SUBDIRECTORY_TYPE));
                fileUploadGroup.setDescriptionType(resultSet.getInt(DatabaseConstants.COLUMN_NAME_UPLOAD_DESCRIPTION_TYPE));
                fileUploadGroup.setNotificationType(resultSet.getInt(DatabaseConstants.COLUMN_NAME_UPLOAD_NOTIFICATION_TYPE));
                fileUploadGroup.setSubdirectoryValue(resultSet.getString(DatabaseConstants.COLUMN_NAME_UPLOAD_SUBDIRECTORY_VALUE));
                fileUploadGroup.setDescriptionValue(resultSet.getString(DatabaseConstants.COLUMN_NAME_UPLOAD_DESCRIPTION_VALUE));
                fileUploadGroup.setUserId(resultSet.getString(DatabaseConstants.COLUMN_NAME_AUTH_USER_ID));
                fileUploadGroup.setComputerId(resultSet.getLong(DatabaseConstants.COLUMN_NAME_COMPUTER_ID));
                fileUploadGroup.setCreatedInDesktopTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_CREATED_IN_DESKTOP_TIMESTAMP));
                fileUploadGroup.setCreatedInDesktopStatus(resultSet.getString(DatabaseConstants.COLUMN_NAME_CREATED_IN_DESKTOP_STATUS));

                if (includingUploadKeys) {
                    List<String> uploadKeys = findUploadKeysByUploadGroupId(fileUploadGroup.getUploadGroupId());

                    fileUploadGroup.setUploadKeys(uploadKeys);
                }
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Failed to find non-created file upload groups for user: '%s',\ncomputer id: %d\nError message:\n%s\n", userId, computerId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return fileUploadGroups;
    }

    public boolean existingFileUploadGroupForUploadGroupId(String uploadGroupId) {
        boolean exists = false;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        FileUploadGroup fileUploadGroup = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_IF_EXISTS_FILE_UPLOAD_GROUP_BY_ID);

            pStatement.setString(1, uploadGroupId);

            resultSet = pStatement.executeQuery();

            exists = resultSet.next();
        } catch (Exception e) {
            LOGGER.error("Failed to find if upload group exists for upload group id: " + uploadGroupId, e);
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

    // Return true if and only if the upload key not exists in table group-detail and created successfully
    // If the record with the same upload group id and upload key already exists, return false.
    public boolean createFileUploadGroupDetailIfNotExists(String uploadGroupId, String uploadKey) {
        boolean success = false;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            String fileUploadGroupDetailId = generateUploadGroupDetailId(uploadGroupId, uploadKey);

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_UPLOAD_GROUP_DETAIL_BY_GROUP_DETAIL_ID);

            pStatement.setString(1, fileUploadGroupDetailId);

            resultSet = pStatement.executeQuery();

            if (!resultSet.next()) {
                // Not exists, so create it.

                pStatement.close();

                pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_FILE_UPLOAD_GROUP_DETAIL);

                pStatement.setString(1, fileUploadGroupDetailId);
                pStatement.setString(2, uploadGroupId);
                pStatement.setString(3, uploadKey);

                pStatement.executeUpdate();

                pStatement.close();

                success = true;
            }
        } catch (Exception e) {
            success = false;

            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on creating single file upload group detail.\ngroupId=%s\nupload key='%s'\nError message:\n%s\n", uploadGroupId, uploadKey, errorMessage), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    // ignored
                }
            }
        }

        return success;
    }

    public boolean deleteFileUploadGroupDetailForUploadKey(String uploadKey) {
        boolean success = false;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_FILE_UPLOAD_GROUP_DETAIL_BY_UPLOAD_KEY);
            pStatement.setString(1, uploadKey);

            pStatement.executeUpdate();

            success = true;

            LOGGER.debug(String.format("Deleted file upload group detail for upload key: '%s'.", uploadKey));
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on deleting file upload group detail for upload key: '%s'\nerror message:\n%s", uploadKey, errorMessage), e);
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

    public String findFileUploadGroupIdByUploadKey(String uploadKey) {
        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        String fileUploadGroupId = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_FILE_UPLOAD_GROUP_DETAIL_BY_UPLOAD_KEY);

            pStatement.setString(1, uploadKey);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                fileUploadGroupId = resultSet.getString(DatabaseConstants.COLUMN_NAME_UPLOAD_GROUP_ID);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to find file upload group id for upload key: " + uploadKey, e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return fileUploadGroupId;
    }

    public String generateUploadGroupDetailId(String uploadGroupId, String uploadKey) {
        return uploadGroupId + "+" + uploadKey;
    }

    public boolean deleteFileUploadGroupById(String uploadGroupId) {
        boolean success = false;

        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_DELETE_FILE_UPLOAD_GROUP_BY_UPLOAD_GROUP_ID);
            pStatement.setString(1, uploadGroupId);

            pStatement.executeUpdate();

            success = true;

            LOGGER.debug(String.format("Deleted file upload group detail for upload group id: '%s'.", uploadGroupId));
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            LOGGER.error(String.format("Error on deleting file upload group detail for upload group id: '%s'\nerror message:\n%s", uploadGroupId, errorMessage), e);
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

    public boolean existingFileUploadGroupDetailForUploadKey(String uploadKey) {
        boolean found = false;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_IF_EXISTS_FILE_UPLOAD_GROUP_DETAIL_BY_UPLOAD_KEY);

            pStatement.setString(1, uploadKey);

            resultSet = pStatement.executeQuery();

            found = resultSet.next();
        } catch (Exception e) {
            LOGGER.error("Failed to find if file upload group exists for upload key: " + uploadKey, e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return found;
    }
}
