package org.clopuccino.device.servlet;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.util.Properties;

/**
 * <code>TWSMSSender</code> sends verification SMS in country TW after user registered.
 *
 * @author masonhsieh
 * @version 1.0
 */
public abstract class MailSender implements ClopuccinoNotification {

    protected final String SMTP_USERNAME = "AKIAIGOJYSF32IRJ4CYA";

    protected final String SMTP_PASSWORD = "AkOR8f44QnskVfN+bInqsnQi7YCEiPbkIR1vU/4aZW69";

    protected final String SMTP_HOSTNAME = "email-smtp.us-west-2.amazonaws.com";

    /* Port we will connect to on the Amazon SES SMTP endpoint. We are choosing port 25
     * because we will use STARTTLS to encrypt the connection.
     */
    protected final Integer SMTP_TLS_PORT = 587; // 25, 465 or 587

    protected final MailSenderFactory.Purpose purpose;

    // one or more mail addresses allowed, separated by one whitespace
    protected String toMails;


    /**
     * Constructor with sending purpose and sending destinations
     *
     * @param purpose Sending purpose
     * @param toMails sending destinations, each email separated with one whitespace.
     */
    public MailSender(MailSenderFactory.Purpose purpose, String toMails) {
        if (purpose == null) {
            this.purpose = MailSenderFactory.Purpose.GENERAL;
        } else {
            this.purpose = purpose;
        }

        this.toMails = toMails;
    }

    public MailSenderFactory.Purpose getPurpose() {
        return purpose;
    }

    public String getToMails() {
        return toMails;
    }

    public void setToMails(String toMails) {
        this.toMails = toMails;
    }

    protected void sendWithTLS(String subject, String subjectCharset, String fromEmail, String content, String contentType) throws Exception {
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
            encodedSubject = MimeUtility.encodeText(subject, subjectCharset, "B");
        } catch (Exception e) {
            encodedSubject = subject;
        }

        try {
            // Create a message with the specified information.
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail));

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toMails, false));

            msg.setSubject(encodedSubject);
            msg.setContent(content, contentType);

            transport = session.getTransport();

            // Connect to Amazon SES using the SMTP username and password you specified above.
            transport.connect(SMTP_HOSTNAME, SMTP_USERNAME, SMTP_PASSWORD);

            // Send the email.
            transport.sendMessage(msg, msg.getAllRecipients());
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
