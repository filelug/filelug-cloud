package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.domain.RegisterModel;
import org.clopuccino.domain.User;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <code>RegisterServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "register", displayName = "register", description = "Register account information", urlPatterns = {"/register"})
public class RegisterServlet extends AbstractRegisterServlet {

    private static final long serialVersionUID = -6495484143074897220L;

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("REGISTER");


    public RegisterServlet() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RegisterModel registerModel = receiveAndCheckPostInput(req, resp);

        if (registerModel != null) {
            String countryId = registerModel.getCountryId();
            String phoneNumber = registerModel.getPhoneNumber();
            String password = registerModel.getPassword();
            Boolean ignorePasswordCheck = registerModel.getIgnorePasswordCheck();

            // It's possible to have two users with the same phone number
            // even when skipShouldUpdatePhoneNumber set to true:
            // 1. The current user whose shouldUpdatePhoneNumber is false
            // 2. The user who has registered but one verified yet.

            List<User> users = userDao.findUsersByPhone(countryId, phoneNumber, true);

            boolean stopRegistration = false;
            int responseErrorStatus = 0; // Used only when stopRegistration is true.

            if (users != null && users.size() > 0) {
                if (ignorePasswordCheck != null && ignorePasswordCheck) {
                    // 電話號碼與密碼皆相同，才停止繼續註冊程序
                    // 若電話號碼相同，但是密碼不同，則繼續註冊程序。

                    for (User user : users) {
                        if (user.getVerified() && user.getPasswd().equals(password)) {
                            stopRegistration = true;
                            responseErrorStatus = HttpServletResponse.SC_FORBIDDEN;
                        } else {
                            // delete the un-registered one and register new account.
                            userDao.deleteUnverifiedUserByPhone(countryId, phoneNumber);
                        }
                    }
                } else {
                    // 只要電話號碼相同，就停止繼續註冊程序
                    // 若密碼也相同，回傳403；若密碼不同，回傳409。

                    for (User user : users) {
                        if (user.getVerified()) {
                            stopRegistration = true;

                            if (user.getPasswd().equals(password)) {
                                responseErrorStatus = HttpServletResponse.SC_FORBIDDEN;
                            } else {
                                responseErrorStatus = HttpServletResponse.SC_CONFLICT;
                            }
                        } else {
                            // delete the un-registered one and register new account.
                            userDao.deleteUnverifiedUserByPhone(countryId, phoneNumber);
                        }
                    }
                }
            }

            if (stopRegistration) {
                String errorMessage = ClopuccinoMessages.localizedMessage(registerModel.getLocale(), "user.already.registered.and.verified");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(responseErrorStatus);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                // situations:
                // 1. there's no verified user, only unverified one
                // 2. no verified, nor unverified ones
                createNewUserAndNotify(req, resp, registerModel);
            }
        }
    }
}
