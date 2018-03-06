package org.clopuccino;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for OS-related
 */
public class OSUtility {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("OS_UTIL");

    public enum OSType {
        WINDOWS, LINUX, OS_X, SOLARIS, HP_UX, BSD, OTHERS
    }

    private static final String lowerCaseOsName = System.getProperty("os.name").toLowerCase();

    public OSUtility() {
    }

    public static OSType getOSType() {
        LOGGER.debug("OS: " + lowerCaseOsName);

        OSType osType;

        if (isWindows()) {
            osType = OSType.WINDOWS;
        } else if (isLinux()) {
            osType = OSType.LINUX;
        } else if (isOSX()) {
            osType = OSType.OS_X;
        } else if (isSolaris()) {
            osType = OSType.SOLARIS;
        } else if (isHpUnix()) {
            osType = OSType.HP_UX;
        } else if (isBSD()) {
            osType = OSType.BSD;
        } else {
            osType = OSType.OTHERS;
        }

        return osType;
    }

    public static boolean isBSD() {
        return lowerCaseOsName.contains("bsd");
    }

    public static boolean isHpUnix() {
        return lowerCaseOsName.contains("hp-ux");
    }

    public static boolean isSolaris() {
        return lowerCaseOsName.contains("solaris");
    }

    public static boolean isOSX() {
        return lowerCaseOsName.contains("mac os x");
    }

    public static boolean isLinux() {
        return lowerCaseOsName.contains("linux");
    }

    public static boolean isWindows() {
        return lowerCaseOsName.contains("windows");
    }

//    public static void main(String[] args) {
//        OSUtility.getOSType();
//
//        Map<String, String> envs = System.getenv();
//
//        Set<Map.Entry<String, String>> entries = envs.entrySet();
//
//        for (Map.Entry<String, String> entry : entries) {
//            System.out.println(entry.getKey() + " : " + entry.getValue());
//        }
//
//    }
}
