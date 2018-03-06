package org.clopuccino.domain;

import org.clopuccino.db.DatabaseConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class UserComputerProfile {

    public static final String PROFILE_VALUES_KEY_COLUMN_NAME = "COLUMN";

    public static final String PROFILE_VALUES_KEY_TYPE = "TYPE";

    // key is the property name
    // value contains 2 elements:
    // (1) key: "column", value: the correspondent database column name, type of String
    // (2) key: "type", value: the class type of the property value, e.g., String.class, Integer.class and Long.class, type of Class
    private static final Map<String, Map<String, Object>> PROFILE_MAP = new HashMap<>();

    private String propertyName;

    private String columnName;

    private Class type;

    static {
        initPropertyNameAndDatabaseColumnMapping();
    }

    public static String getDatabaseColumNameFrom(String propertyName) {
        String columnName = null;

        if (propertyName != null) {
            Map valueMap = PROFILE_MAP.get(propertyName);

            if (valueMap != null) {
                columnName = (String) valueMap.get(PROFILE_VALUES_KEY_COLUMN_NAME);
            }
        }

        return columnName;
    }

    public static Class getProfileTypeFrom(String propertyName) {
        Class type = null;

        if (propertyName != null) {
            Map valueMap = PROFILE_MAP.get(propertyName);

            if (valueMap != null) {
                type = (Class) valueMap.get(PROFILE_VALUES_KEY_TYPE);
            }
        }

        return type;
    }

    public static Map<String, Map<String, Object>> getProfileMap() {
        return PROFILE_MAP;
    }

    public UserComputerProfile(String propertyName) {
        this.propertyName = propertyName;

        if (propertyName != null) {
            Map valueMap = PROFILE_MAP.get(propertyName);

            if (valueMap != null) {
                columnName = (String) valueMap.get(PROFILE_VALUES_KEY_COLUMN_NAME);

                if (columnName == null) {
                    throw new IllegalArgumentException("No such property");
                }

                type = (Class) valueMap.get(PROFILE_VALUES_KEY_TYPE);
            }
        } else {
            throw new IllegalArgumentException("property name can not be empty.");
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public Class getType() {
        return type;
    }

    public String getPropertyName() {
        return propertyName;
    }

    private static void initPropertyNameAndDatabaseColumnMapping() {
        PROFILE_MAP.clear();

        // update upload-directory

        Map<String, Object> uploadDirectoryMap = new HashMap<>();
        uploadDirectoryMap.put(PROFILE_VALUES_KEY_COLUMN_NAME, DatabaseConstants.COLUMN_NAME_UPLOAD_DIRECTORY);
        uploadDirectoryMap.put(PROFILE_VALUES_KEY_TYPE, String.class);

        PROFILE_MAP.put("upload-directory", uploadDirectoryMap);

        // update-subdirectory-type

        Map<String, Object> uploadSubdirectoryTypeMap = new HashMap<>();
        uploadSubdirectoryTypeMap.put(PROFILE_VALUES_KEY_COLUMN_NAME, DatabaseConstants.COLUMN_NAME_USER_COMPUTER_UPLOAD_SUBDIRECTORY_TYPE);
        uploadSubdirectoryTypeMap.put(PROFILE_VALUES_KEY_TYPE, Integer.class);

        PROFILE_MAP.put("upload-subdirectory-type", uploadSubdirectoryTypeMap);

        // upload-subdirectory-value

        Map<String, Object> uploadSubdirectoryValueMap = new HashMap<>();
        uploadSubdirectoryValueMap.put(PROFILE_VALUES_KEY_COLUMN_NAME, DatabaseConstants.COLUMN_NAME_USER_COMPUTER_UPLOAD_SUBDIRECTORY_VALUE);
        uploadSubdirectoryValueMap.put(PROFILE_VALUES_KEY_TYPE, String.class);

        PROFILE_MAP.put("upload-subdirectory-value", uploadSubdirectoryValueMap);

        // upload-description-type

        Map<String, Object> uploadDescriptionTypeMap = new HashMap<>();
        uploadDescriptionTypeMap.put(PROFILE_VALUES_KEY_COLUMN_NAME, DatabaseConstants.COLUMN_NAME_USER_COMPUTER_UPLOAD_DESCRIPTION_TYPE);
        uploadDescriptionTypeMap.put(PROFILE_VALUES_KEY_TYPE, Integer.class);

        PROFILE_MAP.put("upload-description-type", uploadDescriptionTypeMap);

        // upload-description-value

        Map<String, Object> uploadDescriptionValueMap = new HashMap<>();
        uploadDescriptionValueMap.put(PROFILE_VALUES_KEY_COLUMN_NAME, DatabaseConstants.COLUMN_NAME_USER_COMPUTER_UPLOAD_DESCRIPTION_VALUE);
        uploadDescriptionValueMap.put(PROFILE_VALUES_KEY_TYPE, String.class);

        PROFILE_MAP.put("upload-description-value", uploadDescriptionValueMap);

        // upload-notification-type

        Map<String, Object> uploadNotificationTypeMap = new HashMap<>();
        uploadNotificationTypeMap.put(PROFILE_VALUES_KEY_COLUMN_NAME, DatabaseConstants.COLUMN_NAME_USER_COMPUTER_UPLOAD_NOTIFICATION_TYPE);
        uploadNotificationTypeMap.put(PROFILE_VALUES_KEY_TYPE, Integer.class);

        PROFILE_MAP.put("upload-notification-type", uploadNotificationTypeMap);

        // download-directory

        Map<String, Object> downloadDirectoryMap = new HashMap<>();
        downloadDirectoryMap.put(PROFILE_VALUES_KEY_COLUMN_NAME, DatabaseConstants.COLUMN_NAME_DOWNLOAD_DIRECTORY);
        downloadDirectoryMap.put(PROFILE_VALUES_KEY_TYPE, String.class);

        PROFILE_MAP.put("download-directory", downloadDirectoryMap);

        // download-subdirectory-type

        Map<String, Object> downloadSubdirectoryTypeMap = new HashMap<>();
        downloadSubdirectoryTypeMap.put(PROFILE_VALUES_KEY_COLUMN_NAME, DatabaseConstants.COLUMN_NAME_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_TYPE);
        downloadSubdirectoryTypeMap.put(PROFILE_VALUES_KEY_TYPE, Integer.class);

        PROFILE_MAP.put("download-subdirectory-type", downloadSubdirectoryTypeMap);

        // download-subdirectory-value

        Map<String, Object> downloadSubdirectoryValueMap = new HashMap<>();
        downloadSubdirectoryValueMap.put(PROFILE_VALUES_KEY_COLUMN_NAME, DatabaseConstants.COLUMN_NAME_USER_COMPUTER_DOWNLOAD_SUBDIRECTORY_VALUE);
        downloadSubdirectoryValueMap.put(PROFILE_VALUES_KEY_TYPE, String.class);

        PROFILE_MAP.put("download-subdirectory-value", downloadSubdirectoryValueMap);

        // download-description-type

        Map<String, Object> downloadDescriptionTypeMap = new HashMap<>();
        downloadDescriptionTypeMap.put(PROFILE_VALUES_KEY_COLUMN_NAME, DatabaseConstants.COLUMN_NAME_USER_COMPUTER_DOWNLOAD_DESCRIPTION_TYPE);
        downloadDescriptionTypeMap.put(PROFILE_VALUES_KEY_TYPE, Integer.class);

        PROFILE_MAP.put("download-description-type", downloadDescriptionTypeMap);

        // download-description-value

        Map<String, Object> downloadDescriptionValueMap = new HashMap<>();
        downloadDescriptionValueMap.put(PROFILE_VALUES_KEY_COLUMN_NAME, DatabaseConstants.COLUMN_NAME_USER_COMPUTER_DOWNLOAD_DESCRIPTION_VALUE);
        downloadDescriptionValueMap.put(PROFILE_VALUES_KEY_TYPE, String.class);

        PROFILE_MAP.put("download-description-value", downloadDescriptionValueMap);

        // download-notification-type

        Map<String, Object> downloadNotificationTypeMap = new HashMap<>();
        downloadNotificationTypeMap.put(PROFILE_VALUES_KEY_COLUMN_NAME, DatabaseConstants.COLUMN_NAME_USER_COMPUTER_DOWNLOAD_NOTIFICATION_TYPE);
        downloadNotificationTypeMap.put(PROFILE_VALUES_KEY_TYPE, Integer.class);

        PROFILE_MAP.put("download-notification-type", downloadNotificationTypeMap);
    }


}
