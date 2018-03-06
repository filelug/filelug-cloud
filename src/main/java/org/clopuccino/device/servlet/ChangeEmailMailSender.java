package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserDao;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Date;

/**
 * <code>ChangeEmailMailSender</code> sends security code to the email for changing email
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ChangeEmailMailSender extends MailSender {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("MAIL-SENDER-CHANGE-EMAIL");

    private static final String DEFAULT_FROM_EMAIL = "no-reply@filelug.com";

    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final String DEFAULT_CONTENT_TYPE = "text/html; charset=" + DEFAULT_CHARSET;

    private static final String CONTENT_TEMPLATE = "<html><head>" +
                                                   "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" +
                                                   "<title>%s</title></head><body>" +
                                                   "<p>%s</p><br>" +
                                                   "<p>%s</p><p>%s</p><p>%s</p><p>%s</p><br>" +
                                                   "<blockquote>%s</blockquote>" +
                                                   "</body></html>";

    private String securityCode;

    private String userId;

    private String userLocale;

    private UserDao userDao;

    public ChangeEmailMailSender(MailSenderFactory.Purpose purpose, String securityCode, String toEmail, String userId, String userLocale) {
        super(purpose, toEmail);

        this.securityCode = securityCode;

        this.userId = userId;

        this.userLocale = userLocale;

        userDao = new UserDao(DatabaseUtility.createDatabaseAccess());
    }

    @Override
    public void send() throws SQLException {
        if (getPurpose() != null && getPurpose() == MailSenderFactory.Purpose.CHANGE_EMAIL) {
            Utility.getExecutorService().execute(new Runnable() {
                @Override
                public void run() {
                    String subject = ClopuccinoMessages.localizedMessage(userLocale, "mail.change.email.verification.title");

                    String content = String.format(CONTENT_TEMPLATE,
                                                   subject,
                                                   ClopuccinoMessages.localizedMessage(userLocale, "mail.change.email.security.code", securityCode),
                                                   ClopuccinoMessages.localizedMessage(userLocale, "mail.change.email.action.main"),
                                                   ClopuccinoMessages.localizedMessage(userLocale, "mail.change.email.action.exception"),
                                                   ClopuccinoMessages.localizedMessage(userLocale, "mail.support"),
                                                   ClopuccinoMessages.localizedMessage(userLocale, "mail.sincerely"),
                                                   ClopuccinoMessages.localizedMessage(userLocale, "mail.team"));

                    try {
                        sendWithTLS(subject, DEFAULT_CHARSET, DEFAULT_FROM_EMAIL, content, DEFAULT_CONTENT_TYPE);

                        LOGGER.debug("Successfully sent change-email security code mail to user: " + toMails);

                        /* update user security code letter sent timestamp */
                        userDao.updateChangeEmailNewMailAndSecurityCodeSentTimestamp(securityCode, toMails, userId, System.currentTimeMillis());
                    } catch (Exception e) {
                        String errorMessage = String.format("Error on sending change-email security code mail to user: %s using AWS SES.\nReceiver: %s\nSender: %s\nSubject: %s\nReason:\n%s", userId, toMails, DEFAULT_FROM_EMAIL, subject, e.getMessage());
                        LOGGER.error(errorMessage, e);
                    }
                }
            });
        } else {
            LOGGER.error("Mail sender purpose: " + getPurpose() + " not supported.");
        }
    }
}
