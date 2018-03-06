package org.clopuccino.device.servlet;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class MailSenderFactory {

    public enum Purpose {
        GENERAL,
        ADMINISTRATION,
        CHANGE_EMAIL,
        RESET_PASSWORD
    }

    public static MailSender createChangeEmailMailSender(String securityCode, String newEmail, String userId, String userLocale) {
        // If multiple choices of mail senders, get the one using the given parameters.

        return new ChangeEmailMailSender(Purpose.CHANGE_EMAIL, securityCode, newEmail, userId, userLocale);
    }

    public static MailSender createResetPasswordMailSender(String securityCode, String sentToEmail, String userId, String userLocale) {
        // If multiple choices of mail senders, get the one using the given parameters.

        return new ResetPasswordMailSender(Purpose.RESET_PASSWORD, securityCode, sentToEmail, userId, userLocale);
    }

    public static MailSender createNotifyAdminMailSender(String message) {
        return new NotifyAdminMailSender(Purpose.ADMINISTRATION, message);
    }
}
