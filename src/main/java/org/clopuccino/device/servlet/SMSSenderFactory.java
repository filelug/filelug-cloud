package org.clopuccino.device.servlet;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class SMSSenderFactory {

    public enum Purpose {
        GENERAL,
        REGISTER,
        RESET_PASSWORD,
        CHANGE_PHONE_NUMBER
    }

    public static SMSSender createRegisterationSmsSender(String countryId, String userId, String userLocale) {
        SMSSender sender = null;

        /* TODO: create sms sender object based on the country id */
        if (countryId.toLowerCase().startsWith("tw")) {
            sender = new TWSMSSender(Purpose.REGISTER, userId, userLocale);
        }

        return sender;
    }

    public static SMSSender createResetPasswordSmsSender(String countryId, String userId, String userLocale) {
        SMSSender sender = null;

        /* create sms sender object based on the country id */
        if (countryId.toLowerCase().startsWith("tw")) {
            sender = new TWSMSSender(Purpose.RESET_PASSWORD, userId, userLocale);
        }

        return sender;
    }

    public static SMSSender createChangePhoneNumberSmsSender(String newCountryId, String newPhoneNumber, String userId, String userLocale) {
        SMSSender sender = null;

        /* create sms sender object based on the country id */
        if (newCountryId.toLowerCase().startsWith("tw")) {
            sender = new TWSMSSender(Purpose.CHANGE_PHONE_NUMBER, newPhoneNumber, userId, userLocale);
        }

        return sender;
    }
}
