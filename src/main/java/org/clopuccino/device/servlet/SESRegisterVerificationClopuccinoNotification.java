package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserDao;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.sql.SQLException;
import java.util.Properties;

/**
 * <code>SESRegisterVerificationMailSender</code> sends verification mail after user registered.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class SESRegisterVerificationClopuccinoNotification implements ClopuccinoNotification {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("VERIFYMAIL");

    private static final String SMTP_USERNAME = "AKIAJEYJ3EK74MIDE55A";

    private static final String SMTP_PASSWORD = "AigtJD5zmv3W/aQLfUAVtjkO/No6eBVGRlWxgC58W4Th";

    private static final String SMTP_HOSTNAME = "email-smtp.us-east-1.amazonaws.com";

    private static final String DEFAULT_FROM_EMAIL = "verify@filelug.com";

    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final String DEFAULT_CONTENT_TYPE = "text/html; charset=" + DEFAULT_CHARSET;

    /* Port we will connect to on the Amazon SES SMTP endpoint. We are choosing port 25
     * because we will use STARTTLS to encrypt the connection.
     */
    private static final Integer SMTP_TLS_PORT = 587; // 25

    private static final String CONTENT_TEMPLATE = "<html>" +
                                                   "<head>" +
                                                   "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" +
                                                   "<title>%s</title>\n" +
                                                   "</head>" +
                                                   "<body>" +
                                                   "%s<br>" +
                                                   "<blockquote>%s<br></blockquote>" +
                                                   "<br>" +
                                                   "%s %s<br>" +
                                                   "<br>" +
                                                   "%s<br>" +
                                                   "<blockquote><a href=\"%s\">%s</a><br></blockquote>" +
                                                   "%s<br>" +
                                                   "<blockquote>%s<br></blockquote>" +
                                                   "</body>" +
                                                   "</html>";

    private String fromEmail;

    private String toEmail;

    private String nickname;

    private String subject;

    private String content;

    private String contentType;

    private String encryptedUser;

    private String verifiedCode;

    private String userLocale;

    public SESRegisterVerificationClopuccinoNotification(String toEmail, String encryptedUser, String verifiedCode, String userLocale) {
        this.toEmail = toEmail;
        this.encryptedUser = encryptedUser;
        this.verifiedCode = verifiedCode;
        this.userLocale = userLocale;
    }

    public SESRegisterVerificationClopuccinoNotification(String nickname, String toEmail, String encryptedUser, String verifiedCode, String userLocale) {
        this.nickname = nickname;
        this.toEmail = toEmail;
        this.encryptedUser = encryptedUser;
        this.verifiedCode = verifiedCode;
        this.userLocale = userLocale;
    }

    @Override
    public void send() throws SQLException {
        Utility.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                String verifyUrl = String.format("http://filelug.com/crepo/verify?eu=%s&vc=%s&locale=%s", encryptedUser, verifiedCode, userLocale);

                subject = ClopuccinoMessages.localizedMessage(userLocale, "mail.verify.Subject");

                fromEmail = DEFAULT_FROM_EMAIL;

                contentType = DEFAULT_CONTENT_TYPE;

                content = String.format(CONTENT_TEMPLATE,
                                        subject,
                                        ClopuccinoMessages.localizedMessage(userLocale, "mail.verify.ContentHello", nickname),
                                        ClopuccinoMessages.localizedMessage(userLocale, "mail.verify.ThanksUsing"),
                                        ClopuccinoMessages.localizedMessage(userLocale, "mail.verify.HaveRegistered"),
                                        toEmail,
                                        ClopuccinoMessages.localizedMessage(userLocale, "mail.verify.Click"),
                                        verifyUrl,
                                        verifyUrl,
                                        ClopuccinoMessages.localizedMessage(userLocale, "mail.verify.Sincerely"),
                                        ClopuccinoMessages.localizedMessage(userLocale, "mail.verify.Team"));

                sendWithSesTLS();
            }
        });
    }

    private void sendWithSesTLS() {
        Transport transport = null;

        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", SMTP_TLS_PORT);

        // Set properties indicating that we want to use STARTTLS to encrypt the connection.
        // The SMTP session will begin on an unencrypted connection, and then the client
        // will issue a STARTTLS command to upgrade to an encrypted connection.
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        // Create a Session object to represent a mail session with the specified properties.
        Session session = Session.getDefaultInstance(props);

        /* for some mail client that do not support double-bytes characters */
        String encodedSubject;

        try {
            encodedSubject = MimeUtility.encodeText(subject, DEFAULT_CHARSET, "B");
        } catch (Exception e) {
            encodedSubject = subject;
        }

        try {
            // Create a message with the specified information.
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            msg.setSubject(encodedSubject);
            msg.setContent(content, contentType);

            transport = session.getTransport();

            /* TODO: Comment connect and sendMessage when in local test */
            // Connect to Amazon SES using the SMTP username and password you specified above.
            transport.connect(SMTP_HOSTNAME, SMTP_USERNAME, SMTP_PASSWORD);

            // Send the email.
            transport.sendMessage(msg, msg.getAllRecipients());

            LOGGER.info("Verification mail sent to: " + toEmail);

            /* update user verification letter sent timestamp */
            new UserDao().updateVerifyCodeSentTimestamp(toEmail, System.currentTimeMillis());
        } catch (MessagingException e) {
            String errorMessage = String.format("Error on sending verification mail to user: %s using AWS SES.\nReceiver: %s\nSender: %s\nSubject: %s\nReason:\n%s", toEmail, toEmail, fromEmail, subject, e.getMessage());
            LOGGER.error(errorMessage, e);
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }
    }
}
