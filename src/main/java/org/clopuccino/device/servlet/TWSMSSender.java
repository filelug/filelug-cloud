package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.SMSNotificationDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.SMSNotification;
import org.clopuccino.domain.User;
import org.clopuccino.service.BaseService;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <code>TWSMSSender</code> sends verification SMS in country TW after user registered.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class TWSMSSender extends SMSSender {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("SMS-TW-SEND");

    private static final String HOSTNAME = "smexpress.mitake.com.tw";

    /**
     * http
     * --短簡訊Service Port:9600
     * --長簡訊或UNICODE簡訊Service Port:7002
     * https
     * --短簡訊Service Port:9601
     * --長簡訊或UNICODE簡訊Service Port:7102
     */
    private static final String PORT = "7102";

    private static final String PATH = "SpSendUtf"; // DO NOT USE SpSendUtf16, it's not working.

    // Max length of value of DestName, which is used to pass the user id here.
//    private static final int MAX_LENGTH_DEST_NAME = 36;

    private static final String USERNAME = "54689413";

    private static final String PASSWORD = "WB971228";

    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT+08:00");

    private static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmss";

    private static DateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);

    // validate time period for one text message, 20 mins
    private static final long VALID_TIME_PERIOD_IN_MILLIS = 20 * 60 * 1000;

    private static final String ENCODING = "utf-8"; // remain lower case

    private static final String SMS_RESPONSE_PATH = "sms/tw";

    private static final String SMS_RESPONSE_DATA_DELIMITERS = "\r\n";

    private static String smsResponseURL;

    private UserDao userDao;

    private SMSNotificationDao smsNotificationDao;

    private String sentToPhoneNumber;

    public TWSMSSender(SMSSenderFactory.Purpose purpose, String userId, String userLocale) {
        super(purpose, userId, userLocale);

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        smsNotificationDao = new SMSNotificationDao(dbAccess);

        sentToPhoneNumber = null;
    }

    public TWSMSSender(SMSSenderFactory.Purpose purpose, String sentToPhoneNumber, String userId, String userLocale) {
        super(purpose, userId, userLocale);

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        smsNotificationDao = new SMSNotificationDao(dbAccess);

        this.sentToPhoneNumber = sentToPhoneNumber;
    }

    @Override
    public void send() throws SQLException {
        // Get user out with the same thread to prevent getting the data before previously user data updated.
        final User user = userDao.findUserById(userId);

        Utility.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                if (user != null) {
                    if (sentToPhoneNumber == null || sentToPhoneNumber.trim().length() < 1) {
                        sentToPhoneNumber = user.getPhoneNumber();
                    }

                    LOGGER.info("Sending SMS to user: " + userId + ", client locale: " + userLocale + ", purpose: " + purpose.name());

                    try {
                        // HTTPS -- 需要向三竹申請在SmGateway中開啟Http(s)發送權限
                        String baseURI = new BaseService().getBaseURIFrom(true, HOSTNAME, PORT, null);

                        // HTTP
//                        String baseURI = new BaseService().getBaseURIFrom(false, HOSTNAME, PORT, null);

                        BaseService baseService = new BaseService();

                        List<NameValuePair> parameters = new ArrayList<>();
                        parameters.add(new BasicNameValuePair("username", USERNAME));
                        parameters.add(new BasicNameValuePair("password", PASSWORD));
                        parameters.add(new BasicNameValuePair("dstaddr", sentToPhoneNumber));

                        // There is no parameter 'DestName' in the response message, so it's of no use to use this paramter.
//                        String userId = user.getAccount();
//
//                        if (userId.length() > MAX_LENGTH_DEST_NAME) {
//                            parameters.add(new BasicNameValuePair("DestName", userId.substring(0, MAX_LENGTH_DEST_NAME)));
//                        } else {
//                            parameters.add(new BasicNameValuePair("DestName", userId));
//                        }

                        dateFormat.setTimeZone(TIME_ZONE);
                        Date currentDate = new Date();

                        parameters.add(new BasicNameValuePair("dlvtime", dateFormat.format(currentDate)));
                        parameters.add(new BasicNameValuePair("vldtime", dateFormat.format(new Date(currentDate.getTime() + VALID_TIME_PERIOD_IN_MILLIS))));

                        if (purpose == SMSSenderFactory.Purpose.REGISTER) {
                            String bodyText = ClopuccinoMessages.localizedMessage(userLocale, "sms.body.for.register", user.getVerifyCode());
                            parameters.add(new BasicNameValuePair("smbody", bodyText));
                        } else if (purpose == SMSSenderFactory.Purpose.RESET_PASSWORD) {
                            /* DEBUG */
                            LOGGER.debug("Reset password security code to be sent: " + user.getResetPasswordSecurityCode());

                            String bodyText = ClopuccinoMessages.localizedMessage(userLocale, "sms.body.for.reset.password", user.getResetPasswordSecurityCode());
                            parameters.add(new BasicNameValuePair("smbody", bodyText));
                        } else if (purpose == SMSSenderFactory.Purpose.CHANGE_PHONE_NUMBER) {
                            /* DEBUG */
                            LOGGER.debug("Change phone number security code to be sent: " + user.getChangePhoneNumberSecurityCode());

                            String bodyText = ClopuccinoMessages.localizedMessage(userLocale, "sms.body.for.change.phone.number", user.getChangePhoneNumberSecurityCode());
                            parameters.add(new BasicNameValuePair("smbody", bodyText));
                        }

                        // receive response for SMS sending only if SSL supported
                        if (baseService.getRepositoryUseHttps()) {
                            if (smsResponseURL == null) {
                                smsResponseURL = baseService.getBaseURIFrom(BaseService.repositoryUseHttps, Constants.FILELUG_AA_SERVER_HOSTNAME, Constants.FILELUG_SECURE_PORT, Constants.REPOSITORY_CONTEXT_PATH) + SMS_RESPONSE_PATH;
                            }

                            parameters.add(new BasicNameValuePair("response", smsResponseURL));
                        }

                        parameters.add(new BasicNameValuePair("CharsetURL", ENCODING));

                        // Encode blank as %20, instead of '+' for requests from SMS API.
                        HttpResponse response = baseService.doTrustSelfSignedGet(baseURI, PATH, null, parameters, false, null);

                        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

                        LOGGER.debug("Response from sending SMS:\n" + responseString);

                        if (response.getStatusLine().getStatusCode() == 200) {
                            if (purpose == SMSSenderFactory.Purpose.REGISTER) {
                                userDao.updateVerifyCodeSentTimestamp(userId, System.currentTimeMillis());
                            } else if (purpose == SMSSenderFactory.Purpose.RESET_PASSWORD) {
                                userDao.updateResetPasswordSecurityCodeSentTimestamp(userId, System.currentTimeMillis());
                            } else if (purpose == SMSSenderFactory.Purpose.CHANGE_PHONE_NUMBER) {
                                userDao.updateChangePhoneNumberSecurityCodeSentTimestamp(userId, System.currentTimeMillis());
                            }

                            /* parsing: delimiter: \r\n
                             * [1]
                             * msgid=$0002400E2
                             * statuscode=1
                             * AccountPoint=11205
                             */
                            StringTokenizer tokenizer = new StringTokenizer(responseString, SMS_RESPONSE_DATA_DELIMITERS);

                            int tokenSize = tokenizer.countTokens();

                            if (tokenSize == 4 && tokenizer.nextToken().equals("[1]")) {
                                // remove "msgid="
                                String messageId = tokenizer.nextToken().substring("msgid=".length());

                                // remvoe "statuscode="
                                String messageStatusCode = tokenizer.nextToken().substring("statuscode=".length());

                                // remove "AccountPoint="
                                String remainMessageCount = tokenizer.nextToken().substring("AccountPoint=".length());

                                SMSNotification smsNotification = smsNotificationDao.findSMSNotificationById(messageId);

                                if (smsNotification != null) {
                                    // 被 sms/tw 搶先了。此為例外狀況，
                                    // 不更新 statuscode 因為這是比較早的狀態。

                                    smsNotification.setUserId(userId);

                                    smsNotificationDao.updateSMSNotification(smsNotification);
                                } else {
                                    Integer messageStatusCodeInteger;

                                    try {
                                        messageStatusCodeInteger = Integer.parseInt(messageStatusCode);
                                    } catch (Exception e) {
                                        messageStatusCodeInteger = -1;
                                    }

                                    smsNotification = new SMSNotification(messageId, sentToPhoneNumber, 0L, System.currentTimeMillis(), messageStatusCodeInteger, "", userId);

                                    SMSNotification newSMSNotification = smsNotificationDao.createSMSNotification(smsNotification);

                                    if (newSMSNotification != null) {
                                        LOGGER.debug("SMS notification created successfully.\n" + newSMSNotification);
                                    } else {
                                        LOGGER.error("SMS notification created failed.");
                                    }
                                }

                                // Check if remaining message count is lower than the threshhold
                                try {
                                    Integer smsMessageRemainCount = Integer.parseInt(remainMessageCount);

                                    if (smsMessageRemainCount < Constants.SMS_MESSAGE_REMAIN_COUNT_THRESHHOLD) {
                                        // notify admin with emails

                                        String messageBody = "[Filelug]SMS remains " + smsMessageRemainCount + " messages.\nYou need to purchase the sms quota.";

                                        MailSender sender = MailSenderFactory.createNotifyAdminMailSender(messageBody);

                                        sender.send();
                                    }
                                } catch (Exception e) {
                                    LOGGER.error("Error on checking sms notification remaining count threshhold", e);
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error on sending SMS for user: " + userId, e);
                    }
                }
            }
        });
    }
}
