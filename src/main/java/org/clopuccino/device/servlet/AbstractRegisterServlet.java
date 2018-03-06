package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.RegisterModel;
import org.clopuccino.domain.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <code>AbstractRegisterServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public abstract class AbstractRegisterServlet extends HttpServlet {

    private static final long serialVersionUID = -734778016065895339L;

    protected UserDao userDao;

    public AbstractRegisterServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);
    }

    abstract protected Logger getLogger();

    protected RegisterModel receiveAndCheckPostInput(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = Utility.createObjectMapper();

        RegisterModel registerModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), RegisterModel.class);

        String countryId = registerModel.getCountryId();
        String phoneNumber = registerModel.getPhoneNumber();
        String password = registerModel.getPassword();
        String nickname = registerModel.getNickname();
        String verification = registerModel.getVerification();
        Boolean ignoreVerifiedUser = registerModel.getIgnorePasswordCheck();
        String clientLocale = registerModel.getLocale();

        if (countryId == null || phoneNumber == null || password == null || nickname == null || verification == null
            || countryId.trim().length() < 1 || phoneNumber.trim().length() < 1 || password.trim().length() < 1 || nickname.trim().length() < 1 || verification.trim().length() < 1
            || password.equals(DigestUtils.sha256Hex(""))) {
            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();

            return null;
        } else if (!verification.equals(Utility.generateVerification(countryId, phoneNumber, password, nickname))) {
            getLogger().warn("Be careful that user: (" + countryId + ")" + phoneNumber + " is trying to hack the verification code for registration");

            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(String.valueOf(errorMessage));
            resp.getWriter().flush();

            return null;
        } else {
            if (ignoreVerifiedUser == null) {
                registerModel.setIgnorePasswordCheck(Boolean.FALSE);
            }

            return registerModel;
        }
    }

    protected void createNewUserAndNotify(HttpServletRequest req, HttpServletResponse resp, RegisterModel registerModel) throws ServletException, IOException {
        try {
            String countryId = registerModel.getCountryId();
            String phoneNumber = registerModel.getPhoneNumber();
            String userEmail = registerModel.getEmail();
            String clientLocale = registerModel.getLocale();

            User user = new User();

            String userId = User.generateUniqueUserId(countryId, phoneNumber);

            user.setAccount(userId);
            user.setCountryId(countryId);
            user.setPhoneNumber(phoneNumber);
            user.setNickname(registerModel.getNickname());
            user.setPasswd(registerModel.getPassword());
            user.setVerified(false);
            user.setShowHidden(false);
            user.setVerifyCode(Utility.generateSecurityCode());
            user.setUserEmail(userEmail);

            User newUser = userDao.createUser(user);

            if (newUser != null) {
                SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
                Date registeredDate = new Date();
                String dateString = format.format(registeredDate);

                getLogger().info(String.format("User '%s' registered at: %s", userId, dateString));

                SMSSender sender = SMSSenderFactory.createRegisterationSmsSender(countryId, userId, clientLocale);

                if (sender != null) {
                    sender.send();

                    RegisterModel newRegisterModel = new RegisterModel();
                    newRegisterModel.setAccount(newUser.getAccount());
                    newRegisterModel.setTimestamp(registeredDate.getTime());

                    String responseJsonString = Utility.createObjectMapper().writeValueAsString(newRegisterModel);

                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                    resp.getWriter().write(responseJsonString);
                    resp.getWriter().flush();
                } else {
                    // country not supported

                    resp.setStatus(Constants.HTTP_STATUS_COUNTRY_NOT_SUPPORTED);
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "country.not.support"));
                    resp.getWriter().flush();
                }
            } else {
                String message = ClopuccinoMessages.localizedMessage(clientLocale, "need.register.again");

                getLogger().error(message + "\nuserId: " + userId + "\nregister model: " + registerModel);

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(message);
                resp.getWriter().flush();
            }
        } catch (SQLException e) {
            getLogger().error("Error on user registration.",e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
