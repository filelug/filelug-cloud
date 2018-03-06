package org.clopuccino.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * <code>Country</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class Country {

    private String countryId;

    private Integer countryCode;

    private String phoneSample;

    // key=locale name, value=country name for this locale
    private Map<String, String> countryNames = new HashMap<>();

    public Country() {
    }

    public Country(String countryId, Integer countryCode, Map<String, String> countryNames, String phoneSample) {
        this.countryId = countryId;
        this.countryCode = countryCode;
        this.countryNames = countryNames;
        this.phoneSample = phoneSample;
    }

    public Country(String countryId, Integer countryCode) {
        this.countryId = countryId;
        this.countryCode = countryCode;
    }

    public void addCountryNameWithLocale(String locale, String countryName) {
        countryNames.put(locale, countryName);
    }

    /**
     * @return May be null if the locale is not supported in this Country object.
     */
    public String getCountryNameWithLocale(String locale) {
        return countryNames.get(locale);
    }

    public String generateStringToInputToDatabase() {
        // Make sure the content of countryNames contains the following related locales
        
        /*
        # COLUMN_NAME_COUNTRY_ID
        # COLUMN_NAME_COUNTRY_CODE
        # COLUMN_NAME_COUNTRY_PHONE_NUMBER
        # COLUMN_NAME_COUNTRY_LOCALE_DEFAULT
        # COLUMN_NAME_COUNTRY_LOCALE_EN
        # COLUMN_NAME_COUNTRY_LOCALE_ZH
        # COLUMN_NAME_COUNTRY_LOCALE_ZH_TW
        # COLUMN_NAME_COUNTRY_LOCALE_ZH_HK
        # COLUMN_NAME_COUNTRY_LOCALE_ZH_CN
        # COLUMN_NAME_COUNTRY_LOCALE_JA_JP
        # ...(other country locales which may be added later as system required)
        # COLUMN_NAME_COUNTRY_AVAILABLE
        #
        # Ex.
        # TW@@886@@0975009*23@@Taiwan@@Taiwan@@台湾@@台灣@@台灣@@台湾@@台湾@@TRUE
        */

        String countryDefaultName = countryNames.get("en");
        String countrySimpliedChineseName = countryNames.get("zh");
        String countryTaiwanName = countryNames.get("zh_TW");
        String countryHongKongName = countryNames.get("zh_HK");
        String countryJapaneseName = countryNames.get("ja");

        return String.format("%s@@%d@@%s@@%s@@%s@@%s@@%s@@%s@@%s@@%s@@%s", countryId, countryCode, phoneSample, countryDefaultName, countryDefaultName, countrySimpliedChineseName, countryTaiwanName, countryHongKongName, countrySimpliedChineseName, countryJapaneseName, "FALSE");
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public Integer getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(Integer countryCode) {
        this.countryCode = countryCode;
    }

    public Map<String, String> getCountryNames() {
        return countryNames;
    }

    public void setCountryNames(Map<String, String> countryNames) {
        this.countryNames = countryNames;
    }

    public String getCountryByLocale(String locale) {
        return countryNames.get(locale);
    }

    public String getPhoneSample() {
        return phoneSample;
    }

    public void setPhoneSample(String phoneSample) {
        this.phoneSample = phoneSample;
    }
}
