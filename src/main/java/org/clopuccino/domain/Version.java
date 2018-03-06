package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.clopuccino.Constants;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <code>Version</code> wraps of the version number,
 * format with major.minor.maintenance or major.minor.
 *
 * If there's no maintenance number, use 0 instead.
 * <ul>
 *     <li>The value 8.3 equals to value 8.3.0</li>
 *     <li>The value 8 equals to value 8.0 and 8.0.0</li>
 * </ul>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class Version implements Comparable {

    @JsonProperty("version")
    private String version;

    private int majorNumber;

    private int minorNumber;

    private int maintenanceNumber;



    public static boolean valid(String version) {
        return version != null && Pattern.compile(Constants.VERSION_REG_EXP).matcher(version).matches();
    }

    public static String extractVersionFrom(String aString) {
        String VERSION_REG_EXP_SEC_3 = "\\d+\\.\\d+\\.\\d+";
        String VERSION_REG_EXP_SEC_2 = "\\d+\\.\\d+";
        String VERSION_REG_EXP_SEC_1 = "\\d+";

        String foundVersion = matchVersion(VERSION_REG_EXP_SEC_3, aString);

        if (foundVersion != null) {
            return foundVersion;
        } else {
            foundVersion = matchVersion(VERSION_REG_EXP_SEC_2, aString);

            if (foundVersion != null) {
                return foundVersion;
            } else {
                return matchVersion(VERSION_REG_EXP_SEC_1, aString);
            }
        }
    }

    private static String matchVersion(String regExp, String version) {
        Matcher matcher = Pattern.compile(regExp).matcher(version);

        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    public Version(String version) throws IllegalArgumentException {
        if (valid(version)) {
            this.version = version;

            StringTokenizer tokenizer = new StringTokenizer(version, ".", false);

            majorNumber = Integer.parseInt(tokenizer.nextToken());

            if (tokenizer.hasMoreTokens()) {
                minorNumber = Integer.parseInt(tokenizer.nextToken());

                if (tokenizer.hasMoreTokens()) {
                    maintenanceNumber = Integer.parseInt(tokenizer.nextToken());
                } else {
                    maintenanceNumber = 0;
                }
            } else {
                minorNumber = 0;
                maintenanceNumber = 0;
            }
        } else {
            throw new IllegalArgumentException("Illegal version: " + version);
        }
    }

    public String getVersion() {
        return version;
    }

    public int getMajorNumber() {
        return majorNumber;
    }

    public int getMinorNumber() {
        return minorNumber;
    }

    public int getMaintenanceNumber() {
        return maintenanceNumber;
    }

    @Override
    public int compareTo(Object another) {
        final int SMALLER = -1;
        final int EQUAL = 0;
        final int LARGER = 1;

        if (another == null) {
            return LARGER;
        }

        if (this == another) {
            return EQUAL;
        }

        int majorNumber2 = ((Version) another).getMajorNumber();

        if (majorNumber > majorNumber2) {
            return LARGER;
        }

        if (majorNumber < majorNumber2) {
            return SMALLER;
        }

        int minorNumber2 = ((Version) another).getMinorNumber();

        if (minorNumber > minorNumber2) {
            return LARGER;
        }

        if (minorNumber < minorNumber2) {
            return SMALLER;
        }

        int maintenanceNumber2 = ((Version) another).getMaintenanceNumber();

        if (maintenanceNumber > maintenanceNumber2) {
            return LARGER;
        }

        if (maintenanceNumber < maintenanceNumber2) {
            return SMALLER;
        }

        return EQUAL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Version version = (Version) o;

        if (maintenanceNumber != version.maintenanceNumber)
            return false;
        if (majorNumber != version.majorNumber)
            return false;
        if (minorNumber != version.minorNumber)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = majorNumber;
        result = 31 * result + minorNumber;
        result = 31 * result + maintenanceNumber;
        return result;
    }
}
