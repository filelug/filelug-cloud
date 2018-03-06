package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.Country;
import org.clopuccino.domain.CountryModel;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class CountryDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CountryDao.class.getSimpleName());

    /* static to keep values until re-fetch
     * key=column name; value=column locale name, substring from column name
     */
    private static Map<String, String> columnLocaleNames;

    // static to keep values until re-fetch
    private static List<Country> availableCountries;

    public CountryDao() {
        super();
    }

    public CountryDao(DatabaseAccess dbAccess) {
        super(dbAccess);
    }

    /**
     * Checks if country table exists.
     *
     * @return true if country table exists.
     */
    public boolean isTableExists() {
        boolean exists = true;

        Connection conn = null;
        ResultSet rs = null;

        try {
            conn = dbAccess.getConnection();

            DatabaseMetaData dbMetaData = conn.getMetaData();

            /* check if table exists */
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_COUNTRY, new String[]{"TABLE"});

            exists = rs.next();
        } catch (Exception e) {
            exists = false;

            LOGGER.error(String.format("Error on checking if table '%s' exists.\nerror message:\n%s", DatabaseConstants.TABLE_NAME_COUNTRY, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(rs, null, null, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return exists;
    }

    /**
     * Create table country if not exists.
     *
     * @return true if created success or already exists; otherwise false.
     */
    public boolean createTableIfNotExists() {
        boolean success = true;

        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dbAccess.getConnection();

            DatabaseMetaData dbMetaData = conn.getMetaData();

            /* check if table exists */
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_COUNTRY, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_COUNTRY);
                statement.executeUpdate(DatabaseConstants.SQL_CREATE_INDEX_COUNTRY_COUNTRY_CODE);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_COUNTRY, e.getMessage()), e);
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

    /**
     * Deletes all existsing countries and create default ones.
     *
     * @return true if all created successfully; otherwise false.
     */
    public boolean recreateDefaultCountries() {
        boolean success = true;

        Connection conn = null;
        Statement statement = null;
        PreparedStatement pStatement = null;

        BufferedReader reader = null;

        try {
            ClassLoader classLoader = getClass().getClassLoader();

            InputStream regionInputStream = classLoader.getResourceAsStream("/" + DatabaseConstants.FILE_NAME_DEFAULT_COUNTRIES);

            if (regionInputStream == null) {
                regionInputStream = classLoader.getResourceAsStream(DatabaseConstants.FILE_NAME_DEFAULT_COUNTRIES);
            }

            if (regionInputStream == null) {
                throw new FileNotFoundException("Country file not found: " + DatabaseConstants.FILE_NAME_DEFAULT_COUNTRIES);
            }

//            File regionFile = new File(System.getProperty("user.dir"), DatabaseConstants.FILE_NAME_DEFAULT_COUNTRIES);
//
//            if (!regionFile.exists()) {
//                throw new FileNotFoundException("Country file not found: " + regionFile.getAbsolutePath());
//            }

            conn = dbAccess.getConnection();

            statement = conn.createStatement();

            /* delete all regions */
            statement.executeUpdate(DatabaseConstants.SQL_TRUNCATE_TABLE_COUNTRIES);

            LOGGER.debug("Truncate table " + DatabaseConstants.TABLE_NAME_COUNTRY + " successfully before re-creating data.");

            /* create defaults */
            reader = new BufferedReader(new InputStreamReader(regionInputStream, "UTF-8"));
//            reader = new BufferedReader(new InputStreamReader(new FileInputStream(regionFile), "UTF-8"));

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.trim().length() > 0 && !line.trim().startsWith(DatabaseConstants.DEFAULT_PLAIN_TEXT_FILE_COMMENT_CHARACTER)) {
                    /* tab delimiters */
                    StringTokenizer tokenizer = new StringTokenizer(line, DatabaseConstants.FILE_DEFAULT_COUNTRIES_DELIMITERS);

                    pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_COUNTRY);

                    /*
                     * Given the format of each line:
                     * COLUMN_NAME_COUNTRY_ID
                     * COLUMN_NAME_COUNTRY_CODE
                     * COLUMN_NAME_COUNTRY_PHONE_SAMPLE
                     * COLUMN_NAME_COUNTRY_LOCALE_DEFAULT
                     * COLUMN_NAME_COUNTRY_LOCALE_EN
                     * COLUMN_NAME_COUNTRY_LOCALE_ZH
                     * COLUMN_NAME_COUNTRY_LOCALE_ZH_TW
                     * COLUMN_NAME_COUNTRY_LOCALE_ZH_HK
                     * COLUMN_NAME_COUNTRY_LOCALE_ZH_CN
                     * COLUMN_NAME_COUNTRY_LOCALE_JA_JP
                     * ...(other country locales which may be added later as system required)
                     * COLUMN_NAME_COUNTRY_AVAILABLE
                     */
                    int parameterIndex = 0;
                    String countryId = null;

                    do {
                        String token = tokenizer.nextToken().trim();

                        if (++parameterIndex == 1) {
                            /* country id */
                            countryId = token;
                            pStatement.setString(parameterIndex, countryId);
                        } else if (parameterIndex == 2) {
                            /* country code */
                            pStatement.setInt(parameterIndex, Integer.valueOf(token));
                        } else {
                            if (token.equalsIgnoreCase("true")) {
                                pStatement.setBoolean(parameterIndex, Boolean.TRUE);
                            } else if (token.equalsIgnoreCase("false")) {
                                pStatement.setBoolean(parameterIndex, Boolean.FALSE);
                            } else {
                                pStatement.setString(parameterIndex, token);
                            }
                        }
                    } while (tokenizer.hasMoreTokens());

                    if (pStatement.executeUpdate() > 0) {
                        LOGGER.debug("Country: " + countryId + " created successfully.");
                    } else {
                        LOGGER.warn("Failure to create country: " + countryId);
                    }

                    pStatement.close();
                }
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating or updating default countries data.\nerror message:\n%s", e.getMessage()), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    /* ignored */
                }
            }

            if (dbAccess != null) {
                try {
                    dbAccess.close(null, statement, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return success;
    }

    /**
     * key=column name;
     * value=column locale name, substring from column name
     *
     * @return A map with locale-related column name(key) and the locale name, substring from column name(value)
     */
    private Map<String, String> findColumnLocalNames() {
        Map<String, String> columnLocaleNames = new HashMap<>();

        Connection conn = null;
        ResultSet rs = null;

        try {
            conn = dbAccess.getConnection();

            DatabaseMetaData dbMetaData = conn.getMetaData();

            rs = dbMetaData.getColumns(null, null, DatabaseConstants.TABLE_NAME_COUNTRY, null);

            for (; rs.next(); ) {
                String columnName = rs.getString("COLUMN_NAME");

                if (columnName.startsWith(DatabaseConstants.COLUMN_NAME_COUNTRY_LOCALE_COLUMN_PREFIX)) {
                    columnLocaleNames.put(columnName, columnName.substring(2, columnName.length()));
                }
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error on finding locale-related column name.\nerror message:\n%s", e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(rs, null, null, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return columnLocaleNames;
    }

    private List<Country> findAvailableCountries(Map<String, String> columnLocaleNames) {
        List<Country> regions = Collections.synchronizedList(new ArrayList<Country>());

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_AVAILABLE_COUNTRIES);

            resultSet = pStatement.executeQuery();

            /*
             * Given the format of each line:
             * COLUMN_NAME_COUNTRY_ID
             * COLUMN_NAME_COUNTRY_CODE
             * COLUMN_NAME_COUNTRY_PHONE_NUMBER
             * \tCOLUMN_NAME_COUNTRY_LOCALE_DEFAULT
             * \tCOLUMN_NAME_COUNTRY_LOCALE_EN
             * \tCOLUMN_NAME_COUNTRY_LOCALE_ZH
             * \tCOLUMN_NAME_COUNTRY_LOCALE_ZH_TW
             * \tCOLUMN_NAME_COUNTRY_LOCALE_ZH_HK
             * \tCOLUMN_NAME_COUNTRY_LOCALE_ZH_CN
             * \tCOLUMN_NAME_COUNTRY_LOCALE_JA_JP
             * \t...(other region locales which may be added later as system required)
             * \tCOLUMN_NAME_COUNTRY_AVAILABLE
             */
            for (; resultSet.next(); ) {
                Country country = new Country();

                country.setCountryId(resultSet.getString(DatabaseConstants.COLUMN_NAME_COUNTRY_ID));

                country.setCountryCode(resultSet.getInt(DatabaseConstants.COLUMN_NAME_COUNTRY_CODE));

                country.setPhoneSample(resultSet.getString(DatabaseConstants.COLUMN_NAME_COUNTRY_PHONE_SAMPLE));

                Map<String, String> countries = new HashMap<>();

                // all locale-related columns
                for (Map.Entry<String, String> columnLocaleNameEntry : columnLocaleNames.entrySet()) {
                    countries.put(columnLocaleNameEntry.getValue(), resultSet.getString(columnLocaleNameEntry.getKey()));
                }

                country.setCountryNames(countries);

                regions.add(country);
            }
        } catch (Exception e) {
            regions = null;

            LOGGER.error(String.format("Error on finding available countries.\nerror message:\n%s", e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return regions;
    }

    public List<CountryModel> findAvailableCountriesByLocale(String locale) {
        List<CountryModel> countryModels = new ArrayList<>();

        if (availableCountries == null) {
            if (columnLocaleNames == null) {
                columnLocaleNames = findColumnLocalNames();
            }

            availableCountries = findAvailableCountries(columnLocaleNames);
        }

        /* find property locale of values in columnLocaleNames */
        String properLocale = findProperLocaleFromLocale(locale);

        for (Country country : availableCountries) {
            CountryModel countryModel = new CountryModel();

            countryModel.setCountryId(country.getCountryId());
            countryModel.setCountryCode(country.getCountryCode());
            countryModel.setPhoneSample(country.getPhoneSample());

            for (Map.Entry<String, String> localeCountryName : country.getCountryNames().entrySet()) {
                if (properLocale.toLowerCase().equals(localeCountryName.getKey().toLowerCase())) {
                    countryModel.setCountryName(localeCountryName.getValue());

                    break;
                }
            }

            countryModels.add(countryModel);
        }

        return countryModels;
    }

    /**
     * Clear the static variable availableCountries and columnLocaleNames.
     */
    private synchronized void updateAvailableCountries() {
        columnLocaleNames = findColumnLocalNames();

        availableCountries = findAvailableCountries(columnLocaleNames);
    }

    private String findProperLocaleFromLocale(String locale) {
        String properLocale = null;

        if (locale == null || locale.trim().length() < 1) {
            properLocale = DatabaseConstants.COLUMN_NAME_COUNTRY_LOCALE_COLUMN_DEFAULT_NAME;
        } else {
            assert columnLocaleNames != null;

            Collection<String> values = columnLocaleNames.values();

            boolean foundPropertLocale = false;

            // assume locale is equals, case-insensitive to the key
            for (String value : values) {
                if (value.toLowerCase().equals(locale.trim().toLowerCase())) {
                    properLocale = value;
                    foundPropertLocale = true;
                    break;
                }
            }

            // assume locale is longer than key
            if (!foundPropertLocale) {
                for (String value : values) {
                    if (value.toLowerCase().startsWith(locale.trim().toLowerCase())) {
                        properLocale = value;
                        foundPropertLocale = true;
                        break;
                    }
                }
            }

            // assume key is longer than locale
            if (!foundPropertLocale) {
                for (String value : values) {
                    if (locale.trim().toLowerCase().startsWith(value.toLowerCase())) {
                        properLocale = value;
                        foundPropertLocale = true;
                        break;
                    }
                }
            }

            if (!foundPropertLocale) {
                properLocale = DatabaseConstants.COLUMN_NAME_COUNTRY_LOCALE_COLUMN_DEFAULT_NAME;
            }
        }

        return properLocale;
    }

    // Replaced by CountryService.findCountryIdByCountryCode(int, String)
    // because one country code may map to multiple country id.
//    public String findCountryIdByCountryCode(int countryCode) {
//        String countryId;
//
//        Connection conn = null;
//        PreparedStatement pStatement = null;
//        ResultSet resultSet = null;
//
//        try {
//            conn = dbAccess.getConnection();
//
//            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_COUNTRY_ID_BY_COUNTRY_CODE);
//
//            pStatement.setInt(1, countryCode);
//
//            resultSet = pStatement.executeQuery();
//
//            if (resultSet.next()) {
//                countryId = resultSet.getString(DatabaseConstants.COLUMN_NAME_COUNTRY_ID);
//            } else {
//                countryId = null;
//            }
//        } catch (Exception e) {
//            countryId = null;
//
//            LOGGER.error(String.format("Error on finding country id by country code '%d'.\nerror message:\n%s", countryCode, e.getMessage()), e);
//        } finally {
//            if (dbAccess != null) {
//                try {
//                    dbAccess.close(resultSet, null, pStatement, conn);
//                } catch (Exception e) {
//                    /* ignored */
//                }
//            }
//        }
//
//        return countryId;
//    }
}
