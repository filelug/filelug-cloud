package org.clopuccino.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.clopuccino.Utility;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <code>SystemProperties</code> converts properties to json.
 * Use <code>ConcurrentHashMap</code> to save system properties in order for thread-safe access.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class SystemProperties {
    public static final String KEY_SESSION_ID = "sessionId";
    public static final String KEY_SHOW_HIDDEN = "show.hidden";
    public static final String KEY_FILE_SEPARATOR = "file.separator";
    public static final String KEY_PATH_SEPARATOR = "path.separator";
    public static final String KEY_LINE_SEPARATOR = "line.separator";
    public static final String KEY_USER_COUNTRY = "user.country";
    public static final String KEY_USER_LANGUAGE = "user.language";
    public static final String KEY_USER_HOME = "user.home";
    public static final String KEY_USER_DIR = "user.dir";
    public static final String KEY_USER_TIMEZONE = "user.timezone";
    public static final String KEY_USER_NAME = "user.name";
    public static final String KEY_OS_ARCH = "os.arch";
    public static final String KEY_OS_VERSION = "os.version";
    public static final String KEY_OS_NAME = "os.name";
    public static final String KEY_JAVA_CLASS_VERSION = "java.class.version";
    public static final String KEY_JAVA_VM_VENDOR = "java.vm.vendor";
    public static final String KEY_JAVA_VENDOR = "java.vendor";
    public static final String KEY_JAVA_SPECIFICATION_VERSION = "java.specification.version";
    public static final String KEY_JAVA_HOME = "java.home";
    public static final String KEY_JAVA_VM_INFO = "java.vm.info";
    public static final String KEY_JAVA_VERSION = "java.version";
    public static final String KEY_JAVA_RUNTIME_VERSION = "java.runtime.version";
    public static final String KEY_JAVA_IO_TMPDIR = "java.io.tmpdir";
    public static final String KEY_SUN_IO_UNICODE_ENCODING = "sun.io.unicode.encoding";
    public static final String KEY_FILE_ENCODING = "file.encoding";
    public static final String KEY_SUN_JNU_ENCODING = "sun.jnu.encoding";

    private static List<String> filterKeys;

    private static Map<String, String> systemProperties;

    public SystemProperties() {
        if (filterKeys == null) {
            filterKeys = Collections.synchronizedList(new ArrayList<String>());

            filterKeys.add(KEY_SESSION_ID);
            filterKeys.add(KEY_FILE_SEPARATOR);
            filterKeys.add(KEY_PATH_SEPARATOR);
            filterKeys.add(KEY_LINE_SEPARATOR);
            filterKeys.add(KEY_USER_COUNTRY);
            filterKeys.add(KEY_USER_LANGUAGE);
            filterKeys.add(KEY_USER_HOME);
            filterKeys.add(KEY_USER_DIR);
            filterKeys.add(KEY_USER_TIMEZONE);
            filterKeys.add(KEY_USER_NAME);
            filterKeys.add(KEY_OS_ARCH);
            filterKeys.add(KEY_OS_VERSION);
            filterKeys.add(KEY_OS_NAME);
            filterKeys.add(KEY_JAVA_CLASS_VERSION);
            filterKeys.add(KEY_JAVA_VM_VENDOR);
            filterKeys.add(KEY_JAVA_VENDOR);
            filterKeys.add(KEY_JAVA_SPECIFICATION_VERSION);
            filterKeys.add(KEY_JAVA_HOME);
            filterKeys.add(KEY_JAVA_VM_INFO);
            filterKeys.add(KEY_JAVA_VERSION);
            filterKeys.add(KEY_JAVA_RUNTIME_VERSION);
            filterKeys.add(KEY_JAVA_IO_TMPDIR);
            filterKeys.add(KEY_SUN_IO_UNICODE_ENCODING);
            filterKeys.add(KEY_FILE_ENCODING);
            filterKeys.add(KEY_SUN_JNU_ENCODING);
        }

        if (systemProperties == null) {
            systemProperties = new ConcurrentHashMap<String, String>();

            for (String key : filterKeys) {
                String value = System.getProperty(key);
                systemProperties.put(key, value != null ? value : "");
            }

//            Properties properties = System.getProperties();
//
//            Set<Map.Entry<Object, Object>> entrySet = properties.entrySet();
//
//            for (Map.Entry<Object, Object> entry : entrySet) {
//                systemProperties.put(entry.getKey().toString(), entry.getValue() != null ? entry.getValue().toString() : "");
//            }
        }
    }

    public void createOrUpdateProperty(String key, String value) {
        systemProperties.put(key, value);
    }

    public String createOrUpdatePropertyAndConvert2Json(String key, String value) {
        ObjectMapper mapper = Utility.createObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        Set<Map.Entry<String, String>> entryMap = systemProperties.entrySet();
        for (Map.Entry<String, String> entry : entryMap) {
            rootNode.put(entry.getKey(), entry.getValue());
        }

        rootNode.put(key, value);

        return rootNode.toString();
    }

    public String createOrUpdatePropertiesAndConvert2Json(Map<String, String> newProperties) {
        ObjectMapper mapper = Utility.createObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        Set<Map.Entry<String, String>> entryMap = systemProperties.entrySet();
        for (Map.Entry<String, String> entry : entryMap) {
            rootNode.put(entry.getKey(), entry.getValue());
        }

        if (newProperties != null && newProperties.size() > 0) {
            Set<Map.Entry<String, String>> newEntryMap = newProperties.entrySet();

            for (Map.Entry<String, String> entry : newEntryMap) {
                rootNode.put(entry.getKey(), entry.getValue());
            }
        }

        return rootNode.toString();
    }

    public String convert2Json() throws Exception {
        ObjectMapper mapper = Utility.createObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        Set<Map.Entry<String, String>> entryMap = systemProperties.entrySet();
        for (Map.Entry<String, String> entry : entryMap) {
            rootNode.put(entry.getKey(), entry.getValue());
        }

        return rootNode.toString();
    }
}
