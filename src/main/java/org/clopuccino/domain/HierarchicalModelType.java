package org.clopuccino.domain;

/**
 * <code>HierarchicalModelType</code> describes the type of class {#link HierarchicalModel}.
 *
 * @author masonhsieh
 * @version 1.0
 */
public enum HierarchicalModelType {

    FILE,
    DIRECTORY,
    WINDOWS_SHORTCUT_FILE,
    WINDOWS_SHORTCUT_DIRECTORY,
    UNIX_SYMBOLIC_LINK_FILE,
    UNIX_SYMBOLIC_LINK_DIRECTORY,
    MAC_ALIAS_FILE,
    MAC_ALIAS_DIRECTORY,
    USER_HOME,      // user home directory, used for root directory
    LOCAL_DISK,     // disk in local machine, used for root directory
    NETWORK_DISK,   // newwork disk, such as FTP, SMB, used for root directory
    EXTERNAL_DISK,  // USB or TUNDERBOLT disk, used for root directory
    DVD_PLAYER,     // DVD/VCD player, used for root directory
    TIME_MACHINE;   // Time Machine Backup Disk, for macOS only


    /**
     * Make sure type is not null. If type is null, return true.
     */
    public static boolean isShortcutOrLink(HierarchicalModelType type) {
        String typeName = type.name();

        return typeName.contains("SHORTCUT") || typeName.contains("SYMBOLIC_LINK") || typeName.contains("ALIAS");

//        return !(type == FILE || type == DIRECTORY);
    }

    /**
     *
     * @param typeValue
     * @return null if not a valid type.
     */
    public static HierarchicalModelType fromString(String typeValue) {
        HierarchicalModelType type = null;

        try {
            type = Enum.valueOf(HierarchicalModelType.class, typeValue);
        } catch (Exception e) {
            /* ignored */
        }

        return type;
    }
}
