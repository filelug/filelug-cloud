package org.clopuccino;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * <code>ClopuccinoMessagesTestCase</code>
 * <p/>
 * User: masonhsieh
 * Date: 9/28/12
 * Time: 10:03
 */
public class ClopuccinoMessagesTestCase {

    @BeforeClass
    public static void beforeClass() throws Exception {
    }

    @AfterClass
    public static void afterClass() throws Exception {
    }

    @Test
    public void testLocalizedMessage() throws Exception {

        String locale = "zh_TW";

        String message = ClopuccinoMessages.localizedMessage(locale, "server.not.connected");

        System.out.printf("Message of locale '%s' is: '%s'", locale, message);

//        assertTrue(CollectionUtils.isEqualCollection(col1.entrySet(), col2.entrySet()));

    }

    @Test
    public void testAllAvailableLocales() throws Exception {
        Locale[] allLocales = Locale.getAvailableLocales();
        System.out.println(allLocales.length + " available locales:");
        for (Locale locale : allLocales) {
            String javaLocaleString = ClopuccinoMessages.getJavaLocaleString(locale);
            System.out.println(javaLocaleString + "=" + locale);
        }
    }

    @Test
    public void testLocaleMapping() throws Exception {
        ClopuccinoMessages.loadLocales();

        System.out.println(ClopuccinoMessages.listLocales());

        assertEquals("Locale mapping not expected", "zh_TW", ClopuccinoMessages.getLocale("zh-Hant"));

        String newClientLocale = "Wahaha";
        String newServerLocale = "WhatTheFuck";
        ClopuccinoMessages.putLocale(newClientLocale, newServerLocale);

        assertEquals("Locale mapping not expected for customized locale", newServerLocale, ClopuccinoMessages.getLocale(newClientLocale));
    }
}
