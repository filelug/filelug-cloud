package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.dao.SMSNotificationDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.SMSNotification;
import org.clopuccino.domain.User;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * <code>TWSMSNotificationServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "tw-sms-notification", displayName = "tw-sms-notification", description = "Receive the results of SMS from Taiwan", urlPatterns = {"/sms/tw"})
public class TWSMSNotificationServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("SMS-TW-NOTIFY");

    private static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmss";

    private static final long serialVersionUID = -7115452483151386935L;

    private static DateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);

    private final UserDao userDao;

    private final SMSNotificationDao smsNotificationDao;

    public TWSMSNotificationServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        smsNotificationDao = new SMSNotificationDao(dbAccess);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // ?msgid=8091234567         簡訊序號。為SmGateway所編定的簡訊序號
        // &dstaddr=09001234567      受訊方手機號碼
        // &dlvtime=20060810125612   簡訊預約時間。格式為YYYYMMDDHHNNSS
        // &donetime=20060810165612  簡訊最新狀態時間。格式為YYYYMMDDHHNNSS
        // &statusstr=DELIVRD        簡訊狀態說明。請參考附錄四的說明
        // &statuscode=0             0或1代表發送成功。發送時statuscode<0的簡訊為發送失敗，不會有狀態回報
        // &StatusFlag=4             簡訊狀態。請參考附錄一的說明

        String smsId = req.getParameter("msgid");
        String phoneNumber = req.getParameter("dstaddr");
        String deliverTimeString = req.getParameter("dlvtime");
        String statusUpdateTimeString = req.getParameter("donetime");
        String statusString = req.getParameter("StatusFlag");
//        String destName = req.getParameter("DestName"); -- Never receive this message

        LOGGER.debug("SMS Notification received: message id: " + smsId
                     + ", phone number: " + phoneNumber
                     + ", deliver time: " + deliverTimeString
                     + ", status update time: " + statusUpdateTimeString
                     + ", status: " + statusString);

        // all parameters

//        Map<String, String[]> parameterMap = req.getParameterMap();
//
//        Set<Map.Entry<String, String[]>> entries = parameterMap.entrySet();
//
//        LOGGER.debug("All parameters:");
//        for (Map.Entry<String, String[]> entry : entries) {
//            LOGGER.debug(entry.getKey() + "=" + Arrays.toString(entry.getValue()));
//        }

        long deliverTimestamp = 0;

        try {
            deliverTimestamp = dateFormat.parse(deliverTimeString).getTime();
        } catch (Exception e) {
            LOGGER.error("Incorrect deliver time string format: " + deliverTimeString);
        }

        long statusUpdateTimestamp = 0;

        try {
            statusUpdateTimestamp = dateFormat.parse(statusUpdateTimeString).getTime();
        } catch (Exception e) {
            LOGGER.error("Incorrect status update time string format: " + statusUpdateTimeString);
        }

        int status = -1;

        try {
            status = Integer.valueOf(statusString, 10);
        } catch (Exception e) {
            LOGGER.error("Incorrect status string: " + statusString);
        }

        String countryId = "TW";

        String responseString = "magicid=sms_gateway_rpack\r\nmsgid=" + smsId + "\r\n";
        byte[] responsBytes =responseString.getBytes();

        List<User> users = userDao.findUsersByPhone(countryId, phoneNumber, true);

        // It's possible to have two users with the same phone number
        // even when skipShouldUpdatePhoneNumber set to true:
        // 1. The current user whose shouldUpdatePhoneNumber is false
        // 2. The user who has registered but one verified yet, or
        //    just verified and the verified timestamp, if it is verified, is later than the first one.

        if (users != null && users.size() > 0) {
            SMSNotification smsNotification = smsNotificationDao.findSMSNotificationById(smsId);

            User user = null;

            if (users.size() < 2) {
                // only one
                user = users.get(0);
            } else {
                if (smsNotification != null) {
                    for (User currentUser : users) {
                        if (currentUser.getAccount().equals(smsNotification.getUserId())) {
                            user = currentUser;
                        }
                    }
                }
            }

            if (user != null) {
                if (smsNotification != null) {
                    /* update */
                    smsNotification.setPhoneNumber(phoneNumber);
                    smsNotification.setDeliverTimestamp(deliverTimestamp);
                    smsNotification.setStatusUpdateTimestamp(statusUpdateTimestamp);
                    smsNotification.setStatus(status);
                    smsNotification.setUserId(user.getAccount());

                    boolean updateSuccess = smsNotificationDao.updateSMSNotification(smsNotification);

                    if (updateSuccess) {
                        LOGGER.debug("SMS notification updated successfully.\n" + smsNotification);

                        resp.setContentType("text/plain");
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getOutputStream().write(responsBytes);
                        resp.getOutputStream().flush();
                    } else {
                        LOGGER.error("SMS notification updated failed.\n" + smsNotification);

                        resp.setContentType("text/plain");
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getOutputStream().write(responsBytes);
                        resp.getOutputStream().flush();
                    }
                } else {
                    /* create */
                    smsNotification = new SMSNotification();

                    smsNotification.setSmsId(smsId);
                    smsNotification.setPhoneNumber(phoneNumber);
                    smsNotification.setDeliverTimestamp(deliverTimestamp);
                    smsNotification.setStatusUpdateTimestamp(statusUpdateTimestamp);
                    smsNotification.setStatus(status);
                    smsNotification.setStatusMessage("");
                    smsNotification.setUserId(user.getAccount());

                    SMSNotification newSMSNotification = smsNotificationDao.createSMSNotification(smsNotification);

                    if (newSMSNotification != null) {
                        LOGGER.debug("SMS notification created successfully.\n" + smsNotification);

                        resp.setContentType("text/plain");
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getOutputStream().write(responsBytes);
                        resp.getOutputStream().flush();
                    } else {
                        LOGGER.error("SMS notification created failed.\n" + smsNotification);

                        resp.setContentType("text/plain");
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getOutputStream().write(responsBytes);
                        resp.getOutputStream().flush();
                    }
                }
            } else {
                LOGGER.error(String.format("User(phone number '%s') with the same user id  not found for this SMS notification: '%s'. User id: %s", phoneNumber, smsId, (smsNotification != null ? smsNotification.getUserId() : "(Unknown user id)")));

                resp.setContentType("text/plain");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getOutputStream().write(responsBytes);
                resp.getOutputStream().flush();
            }
        } else {
            LOGGER.error(String.format("User with phone number '%s' not found for this SMS notification: '%s'", phoneNumber, smsId));

            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getOutputStream().write(responsBytes);
            resp.getOutputStream().flush();
        }
    }
}
