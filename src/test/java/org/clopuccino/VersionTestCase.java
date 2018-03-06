package org.clopuccino;

import org.clopuccino.domain.Version;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * <code>VersionTestCase</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class VersionTestCase {

    @Test
    public void testVersion() throws Exception {
        Version version1 = new Version("123.24.7");
        Version version2 = new Version("89.134.987");
        Version version3 = new Version("123.9.987");

        assertTrue("Major number not expected!", version1.getMajorNumber() > version2.getMajorNumber());

        assertTrue("Minor number not expected!", version1.getMinorNumber() < version2.getMinorNumber());

        assertTrue("Maintenance number not expected!", version2.getMaintenanceNumber() == version3.getMaintenanceNumber());

    }

    @Test
    public void testVersionExtraction() throws Exception {
        String VERSION_REG_EXP_SEC_3 = "\\d+\\.\\d+\\.\\d+";
        String VERSION_REG_EXP_SEC_2 = "\\d+\\.\\d+";
        String VERSION_REG_EXP_SEC_1 = "\\d+";

        String[] samples = {"Ubuntu 16.04.1 LTS (xenial)", "4.4.0-47-generic"};

        for (String sample : samples) {
            String foundVersion = matchVersion(VERSION_REG_EXP_SEC_3, sample);

            if (foundVersion != null) {
                System.out.println("sample ==> " + foundVersion);
            } else {
                foundVersion = matchVersion(VERSION_REG_EXP_SEC_2, sample);

                if (foundVersion != null) {
                    System.out.println("sample ==> " + foundVersion);
                } else {
                    foundVersion = matchVersion(VERSION_REG_EXP_SEC_1, sample);

                    if (foundVersion != null) {
                        System.out.println("sample ==> " + foundVersion);
                    } else {
                        System.out.println("sample ==> " + "NOT FOUND");
                    }
                }
            }
        }
    }

    private String matchVersion(String regExp, String version) {
        Matcher matcher = Pattern.compile(regExp).matcher(version);

        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    @Test
    public void testDesktopSoftwareName() throws Exception {
        /* windows x64 */
        String version = "1.0.0";
        String os = "windows";
        String arch = "x64";

        assertEquals("Desktop software name not expected.", "fdesktop-"+ version + "-" + os + "-" + arch + ".setup.exe.zip", Utility.prepareDesktopSoftwareName(version, os, arch));

        /* windows x86 */
        version = "1.0.0";
        os = "windows";
        arch = "x86";

        assertEquals("Desktop software name not expected.", "fdesktop-" + version + "-" + os + "-" + arch + ".setup.exe.zip", Utility.prepareDesktopSoftwareName(version, os, arch));

        /* mac x64 */
        version = "1.0.0";
        os = "mac";
        arch = "x64";

        assertEquals("Desktop software name not expected.", "fdesktop-" + version + "-" + os + "-" + arch + ".app.zip", Utility.prepareDesktopSoftwareName(version, os, arch));

        /* linux x64 */
        version = "1.0.0";
        os = "linux";
        arch = "x64";

        assertEquals("Desktop software name not expected.", "fdesktop-"+ version + "-" + os + "-" + arch + "-self-extract.zip", Utility.prepareDesktopSoftwareName(version, os, arch));

        /* linux x86 */
        version = "1.0.0";
        os = "linux";
        arch = "x86";

        assertEquals("Desktop software name not expected.", "fdesktop-" + version + "-" + os + "-" + arch + "-self-extract.zip", Utility.prepareDesktopSoftwareName(version, os, arch));

    }
}
