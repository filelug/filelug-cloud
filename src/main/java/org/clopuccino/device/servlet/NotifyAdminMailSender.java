package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * <code>NotifyAdminMailSender</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class NotifyAdminMailSender extends MailSender {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("MAIL-SENDER-NOTIFY-ADMIN");

    private static final String DEFAULT_FROM_EMAIL = "admin@filelug.com";

    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final String DEFAULT_CONTENT_TYPE = "text/html; charset=" + DEFAULT_CHARSET;

    private static final String CONTENT_TEMPLATE = "<html><head>" +
                                                   "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" +
                                                   "<title>%s</title></head><body>" +
                                                   "<p>%s</p><br>" +
                                                   "</body></html>";

    private String message;


    public NotifyAdminMailSender(MailSenderFactory.Purpose purpose, String message) {
        super(purpose, Constants.ADMINISTRATORS_EMAILS);

        this.message = message;
    }

    @Override
    public void send() throws SQLException {
        if (getPurpose() != null && getPurpose() == MailSenderFactory.Purpose.ADMINISTRATION) {
            Utility.getExecutorService().execute(new Runnable() {
                @Override
                public void run() {
                    String subject = "Filelug Administration Notification";

                    String content = String.format(CONTENT_TEMPLATE, subject, message);

                    try {
                        sendWithTLS(subject, DEFAULT_CHARSET, DEFAULT_FROM_EMAIL, content, DEFAULT_CONTENT_TYPE);

                        LOGGER.debug("Successfully sent email to administrators: " + toMails);
                    } catch (Exception e) {
                        String errorMessage = String.format("Error on sending email to administrators: %s.\nReason:\n%s", toMails, e.getMessage());

                        LOGGER.error(errorMessage, e);
                    }
                }
            });
        } else {
            LOGGER.error("Mail sender purpose: " + getPurpose() + " not supported.");
        }
    }
}
