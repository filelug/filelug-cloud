package org.clopuccino;

import ch.qos.logback.classic.Logger;
import org.glassfish.jersey.internal.l10n.LocalizableMessageFactory;
import org.glassfish.jersey.internal.l10n.Localizer;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <code>ClopuccinoMessages</code> handles i18n messages for clopuccino.
 *
 * @author masonhsieh
 * @version 1.0
 */
public final class ClopuccinoMessages {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("LOCALES");

    // Use Locale.ROOT leads to the value of DEFAULT_LOCALE_STRING is empty,
    // and for Desktop, it can't distinguish between ROOT("") locale and os default locale.
//    public static final Locale DEFAULT_LOCALE = Locale.ROOT;
//    public static final String DEFAULT_LOCALE_STRING = DEFAULT_LOCALE.getLanguage();

    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    public static final String DEFAULT_LOCALE_STRING = DEFAULT_LOCALE.getLanguage();

    /* client locale (key) to java locale (value) */
    private static final Hashtable<String, String> localeMap = new Hashtable<>();

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("Messages");

    private final static Localizer localizer = new Localizer();

    public static void loadLocales() {
        ResourceBundle bundle = ResourceBundle.getBundle("ClientServerLocale");
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            String value = bundle.getString(key);
            localeMap.put(key, value);
        }
    }

    /**
     * @param clientLocale
     * @param javaLocale
     * @return The previous value of the key or null if no previous one.
     */
    public static String putLocale(String clientLocale, String javaLocale) {
        String previousValue = null;

        try {
            previousValue = localeMap.put(clientLocale, javaLocale);
        } catch (Exception e) {
            String errorMessage = String.format("Error on adding or replacing locale. client locale=%s, server locale=%s.\nReason: %s", clientLocale, javaLocale, e.getMessage());
            LOGGER.error(errorMessage, e);
        }

        return previousValue;
    }

    /**
     * @param clientLocale
     * @return the value to which the key had been mapped in this hashtable, or <code>null</code> if the key did not have a mapping.
     */
    public static String removeLocale(String clientLocale) {
        return localeMap.remove(clientLocale);
    }

    /**
     * @param clientLocale
     * @return the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the key.
     */
    public static String getLocale(String clientLocale) {
        return localeMap.get(clientLocale);
    }

    /**
     * List all locales mapping with format of "key1=value1, key2=value2, ...".
     * If no locales mapping, return empty string.
     *
     * @return
     */
    public static String listLocales() {
        if (localeMap.size() > 0) {
            StringBuilder buffer = new StringBuilder();

            Set<Map.Entry<String, String>> entries = localeMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                buffer.append(entry.getKey());
                buffer.append("=");
                buffer.append(entry.getValue());
                buffer.append(", ");
            }

            int bufferSize = buffer.length();
            buffer.delete(bufferSize - 2, bufferSize);

            return buffer.toString();
        } else {
            return "";
        }
    }

    public static String getMessage(String key, Object... args) {
        return localizer.localize(messageFactory.getMessage(key, args));
    }

    public static String localizedMessage(String locale, String key, Object... args) {
        if (locale == null || locale.trim().length() < 1) {
            locale = DEFAULT_LOCALE_STRING;
        }

        String javaLocale = getLocale(locale);

        Locale foundLocale = javaLocale != null ? getLocaleFromJavaLocaleString(javaLocale) : DEFAULT_LOCALE;
        if (foundLocale == null) {
            foundLocale = DEFAULT_LOCALE;
        }

        return new Localizer(foundLocale).localize(messageFactory.getMessage(key, args));
    }

    public static String localizedMessage(Locale locale, String key, Object... args) {
        return new Localizer(locale != null ? locale : DEFAULT_LOCALE).localize(messageFactory.getMessage(key, args));
    }

    /**
     * Convert a string based testLocale into a Locale Object. Assumes the string
     * has form "{language}_{country}_{variant}". Examples: "en", "de_DE",
     * "_GB", "en_US_WIN", "de__POSIX", "fr_MAC"
     * <p/>
     * 若localeString為null、空值、或不合法的Locale，則回傳null
     *
     * @param locale The String
     * @return the Locale. 若localeString為null、空值、或不合法的Locale，則回傳null
     */
    public static Locale getLocaleFromJavaLocaleString(String locale) {
        if (locale == null || locale.trim().length() < 1) {
            return DEFAULT_LOCALE;
        }

        locale = locale.trim();

        // Extract language
        int languageIndex = locale.indexOf('_');
        String language = null;
        if (languageIndex == -1) {
            // No further "_" so is "{language}" only
            return new Locale(locale, "");
        } else {
            language = locale.substring(0, languageIndex);
        }

        // Extract country
        int countryIndex = locale.indexOf('_', languageIndex + 1);
        String country = null;
        if (countryIndex == -1) {
            // No further "_" so is "{language}_{country}"
            country = locale.substring(languageIndex + 1);
            return new Locale(language, country);
        } else {
            // Assume all remaining is the variant so is
            // "{language}_{country}_{variant}"
            country = locale.substring(languageIndex + 1, countryIndex);
            String variant = locale.substring(countryIndex + 1);
            return new Locale(language, country, variant);
        }
    } // end getLocaleFromJavaLocaleString(String)

    public static String getJavaLocaleString(Locale locale) {
        StringBuilder builder = new StringBuilder();

        if (locale != null) {
            String language = locale.getLanguage();

            builder.append(language);

            String country = locale.getCountry();

            if (country != null && country.length() > 0) {
                builder.append("_");
                builder.append(country);
            }

            String variant = locale.getVariant();

            if (variant != null && variant.length() > 0) {
                builder.append("_");
                builder.append(variant);
            }
        }

        return builder.toString();
    }
}
