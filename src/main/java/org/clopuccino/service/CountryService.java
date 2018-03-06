package org.clopuccino.service;

import ch.qos.logback.classic.Logger;
import org.clopuccino.Constants;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.Country;
import org.clopuccino.domain.CountryModel;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <code>CountryService</code> provides services related to country id, country code, and localized country name
 *
 * @author masonhsieh
 * @version 1.0
 */
public class CountryService {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CountryService.class.getSimpleName());

    private static Map<String, Country> countryMap;

    public CountryService() {}

    public List<CountryModel> findAvailableCountriesByLocale(String locale) {
        List<CountryModel> countryModels = new ArrayList<>();

        prepareCountryMap(false);

        Locale properLocale = findProperLocaleFromLocale(locale);

        countryMap.forEach((countryId, country) -> {
            String localizedCountryName = country.getCountryNameWithLocale(properLocale.toString());

            if (localizedCountryName == null) {
                localizedCountryName = country.getCountryNameWithLocale(Constants.DEFAULT_LOCALE_FOR_SUPPORTED_LOCALES.toString());
            }

            countryModels.add(new CountryModel(countryId, country.getCountryCode(), localizedCountryName, country.getPhoneSample()));
        });

        return countryModels;
    }

    /**
     * @return The country id. Return null if not found.
     */
    public String findCountryIdByCountryCode(int countryCode, String phoneNumberWithCountryCode) {
        String countryId = null;

        final String bundleNamePrefix = "NationalNumberPrefix";
        String bundleName = null;
        int checkPrefixCount = 1;
        String defaultCountryId = null;

        switch (countryCode) {
            case 1:
                // NationalNumberPrefix1.properties

                bundleName = bundleNamePrefix + countryCode;
                checkPrefixCount = 4;
                defaultCountryId = "US";

                break;
            case 7:
                // NationalNumberPrefix7.properties

                bundleName = bundleNamePrefix + countryCode;
                checkPrefixCount = 3;
                defaultCountryId = "RU";

                break;
            case 44:
                // NationalNumberPrefix44.properties

                bundleName = bundleNamePrefix + countryCode;
                checkPrefixCount = 6;
                defaultCountryId = "GB";

                break;
            case 212:
                // NationalNumberPrefix212.properties

                bundleName = bundleNamePrefix + countryCode;
                checkPrefixCount = 5;
                defaultCountryId = "EH";

                break;
            case 262:
                // NationalNumberPrefix212.properties

                bundleName = bundleNamePrefix + countryCode;
                checkPrefixCount = 6;
                defaultCountryId = "RE";

                break;
            case 599:
                // NationalNumberPrefix212.properties

                bundleName = bundleNamePrefix + countryCode;
                checkPrefixCount = 4;
                defaultCountryId = "CW";

                break;
            default:
                // DO nothing.
        }

        if (bundleName != null) {
            ResourceBundle nationalNumberPrefixBundle = ResourceBundle.getBundle(bundleName);

            int startIndex = phoneNumberWithCountryCode.startsWith("+") ? 1 : 0;
            int endIndex =   startIndex + checkPrefixCount;

            String phoneNumberWithCountryCodePrefix = phoneNumberWithCountryCode.substring(startIndex, endIndex);

            try {
                countryId = nationalNumberPrefixBundle.getString(phoneNumberWithCountryCodePrefix);
            } catch (Exception e) {
                // not found, use default
                countryId = defaultCountryId;
            }
        } else {
            ResourceBundle bundle = ResourceBundle.getBundle("CountryIdAndCode");

            // key is country id and value is country code.
            // The case of one country code with multiple country ids has solved in the if-block above,
            // so one-country-code-with-one-country-id only here.
            Set<String> keys = bundle.keySet();

            for (String key : keys) {
                String value = bundle.getString(key);

                if (Integer.valueOf(value) == countryCode) {
                    countryId = key;

                    break;
                }
            }
        }

        return countryId;
    }

    /**
     * @return The country code. If the country code not found, return 0.
     */
    public int findCountryCodeByCountryId(String countryId) {
        ResourceBundle bundle = ResourceBundle.getBundle("CountryIdAndCode");

        int countryCode;

        try {
            Object countryCodeObject = bundle.getObject(countryId);

            countryCode = Integer.parseInt((String) countryCodeObject);
        } catch (Exception e) {
            countryCode = 0;
        }

        return countryCode;
    }

    /**
     * @return "+" && countryCode && phoneNumber(without '0' prefix)
     */
    public static String phoneWithCountryFrom(int countryCode, String phoneNumber) {
        String phoneNumberWithoutZeroPrefix;

        if (phoneNumber.startsWith("0")) {
            phoneNumberWithoutZeroPrefix = phoneNumber.substring(1);
        } else {
            phoneNumberWithoutZeroPrefix = phoneNumber;
        }

        return String.format("%s%d%s", "+", countryCode, phoneNumberWithoutZeroPrefix);
    }

    private void prepareCountryMap(boolean refresh) {
        if (refresh || countryMap == null) {
            Map<String, Country> newCountryMap = parseCountryIdAndCode();

            findAndSetPhoneSampleTo(newCountryMap);

            findAndSetCountryNameTo(newCountryMap);

            countryMap = newCountryMap;
        }
    }

    /**
     * Get property locale based on the supported locales
     */
    public Locale findProperLocaleFromLocale(String locale) {
        Locale properLocale = null;

        if (locale == null || locale.equals(DatabaseConstants.COLUMN_NAME_COUNTRY_LOCALE_COLUMN_DEFAULT_NAME) || locale.trim().length() < 1) {
            properLocale = Constants.DEFAULT_LOCALE_FOR_SUPPORTED_LOCALES;
        } else {
            boolean foundPropertLocale = false;

            Locale[] supportedLocales = Constants.SUPPORTED_LOCALES;

            // assume the given locale is equals, case-insensitive to the string of the supported locale
            for (Locale value : supportedLocales) {
                if (value.toString().toLowerCase().equals(locale.trim().toLowerCase())) {
                    properLocale = value;
                    foundPropertLocale = true;
                    break;
                }
            }

            // assume the given locale is longer than the string of the supported locale
            if (!foundPropertLocale) {
                for (Locale value : supportedLocales) {
                    if (value.toString().toLowerCase().startsWith(locale.trim().toLowerCase())) {
                        properLocale = value;
                        foundPropertLocale = true;
                        break;
                    }
                }
            }

            // assume the string of the supported locale is longer than the given locale
            if (!foundPropertLocale) {
                for (Locale value : supportedLocales) {
                    if (locale.trim().toLowerCase().startsWith(value.toString().toLowerCase())) {
                        properLocale = value;
                        foundPropertLocale = true;
                        break;
                    }
                }
            }

            if (!foundPropertLocale) {
                properLocale = Constants.DEFAULT_LOCALE_FOR_SUPPORTED_LOCALES;
            }
        }

        return properLocale;
    }

    private Map<String, Country> parseCountryIdAndCode() {
        Map<String, Country> countryMap = new HashMap<>();

        // Parse Country ID and CODE

        ResourceBundle bundle = ResourceBundle.getBundle("CountryIdAndCode");

        // key is the country id, value is the country code

        Set<String> keys = bundle.keySet();

        for (String key : keys) {
            String value = bundle.getString(key);

            countryMap.put(key, new Country(key, Integer.parseInt(value)));
        }

        return countryMap;
    }

    private void findAndSetPhoneSampleTo(Map<String, Country> countryMap) {
        for (Locale locale : Constants.SUPPORTED_LOCALES) {
            ResourceBundle bundle = ResourceBundle.getBundle("CountryIdAndPhoneSample", locale);

            // key is the country id, value is the phone sample with the specified locale

            Set<String> keys = bundle.keySet();

            for (String key : keys) {
                Country country = countryMap.get(key);

                String phoneSample = bundle.getString(key);

                if (country == null) {
                    LOGGER.warn("No Country found for country id: " + key + ". Skip to add phone sample: " + phoneSample);
                } else {
                    country.setPhoneSample(phoneSample);
                }
            }
        }
    }

    private void findAndSetCountryNameTo(Map<String, Country> countryMap) {
        // Parse Country ID and NAME

        for (Locale locale : Constants.SUPPORTED_LOCALES) {
            ResourceBundle bundle = ResourceBundle.getBundle("CountryName", locale);

            // key is the country id, value is the country name with the specified locale

            Set<String> keys = bundle.keySet();

            for (String key : keys) {
                Country country = countryMap.get(key);

                String countryName = bundle.getString(key);

                if (country == null) {
                    LOGGER.warn("No Country found for country id: " + key + ". Skip to add country name: " + countryName);
                } else {
                    country.addCountryNameWithLocale(locale.toString(), countryName);
                }
            }
        }
    }
}
