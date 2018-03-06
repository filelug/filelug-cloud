package org.clopuccino;

import org.clopuccino.domain.AccountKit;
import org.clopuccino.service.CountryService;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class CountryCodeTest {

    @Test
    public void testCountryCodeWithNationalNumber() {

        // key: + && country code && phone number
        // value: expected country id
        Map<String, String> phoneNumberAndCountryIdMap = new HashMap<>();

        // 1
        phoneNumberAndCountryIdMap.put("+17211231234", "SX");
        phoneNumberAndCountryIdMap.put("13651231234", "CA");
        phoneNumberAndCountryIdMap.put("+13661231234", "US");
        // 7
        phoneNumberAndCountryIdMap.put("77011231234", "KZ");
        phoneNumberAndCountryIdMap.put("+79121231234", "RU");
        // 44
        phoneNumberAndCountryIdMap.put("441624123456", "IM");
        phoneNumberAndCountryIdMap.put("+441623123456", "GB");
        // 212
        phoneNumberAndCountryIdMap.put("+212611231234", "MA");
        phoneNumberAndCountryIdMap.put("212621231234", "EH");
        // 262
        phoneNumberAndCountryIdMap.put("+262269123123", "YT");
        phoneNumberAndCountryIdMap.put("262270123123", "RE");
        // 599
        phoneNumberAndCountryIdMap.put("59991231234", "CW");
        phoneNumberAndCountryIdMap.put("+5997", "BQ");
        // 886
        phoneNumberAndCountryIdMap.put("886975123123", "TW");
        phoneNumberAndCountryIdMap.put("+886958123123", "TW");

        CountryService countryService = new CountryService();

        phoneNumberAndCountryIdMap.forEach((phoneNumberWithCountryCode, expectedCountryId) -> {
            String foundCountryId = null;

            // Test 1
            if (phoneNumberWithCountryCode.startsWith("1") || phoneNumberWithCountryCode.startsWith("+1")) {
                foundCountryId = countryService.findCountryIdByCountryCode(1, phoneNumberWithCountryCode);

                // Test 7
            } else if (phoneNumberWithCountryCode.startsWith("7") || phoneNumberWithCountryCode.startsWith("+7")) {
                foundCountryId = countryService.findCountryIdByCountryCode(7, phoneNumberWithCountryCode);

                // Test 44
            } else if (phoneNumberWithCountryCode.startsWith("44") || phoneNumberWithCountryCode.startsWith("+44")) {
                foundCountryId = countryService.findCountryIdByCountryCode(44, phoneNumberWithCountryCode);

                // Test 212
            } else if (phoneNumberWithCountryCode.startsWith("212") || phoneNumberWithCountryCode.startsWith("+212")) {
                foundCountryId = countryService.findCountryIdByCountryCode(212, phoneNumberWithCountryCode);

                // Test 262
            } else if (phoneNumberWithCountryCode.startsWith("262") || phoneNumberWithCountryCode.startsWith("+262")) {
                foundCountryId = countryService.findCountryIdByCountryCode(262, phoneNumberWithCountryCode);

                // Test 599
            } else if (phoneNumberWithCountryCode.startsWith("599") || phoneNumberWithCountryCode.startsWith("+599")) {
                foundCountryId = countryService.findCountryIdByCountryCode(599, phoneNumberWithCountryCode);

                // Test 886
            } else if (phoneNumberWithCountryCode.startsWith("886") || phoneNumberWithCountryCode.startsWith("+886")) {
                foundCountryId = countryService.findCountryIdByCountryCode(886, phoneNumberWithCountryCode);
            } else {
                System.out.println("Not in the testing scope: (" + phoneNumberWithCountryCode + ", " + expectedCountryId + ")");
            }

            assertEquals("Country Id Not Expected!", expectedCountryId, foundCountryId);
        });

    }
}
