package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
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
 * <code>Register2Servlet</code> registers new user without checking if the phone number has been used to register before.
 *
 * @author masonhsieh
 * @version 1.0
 */
//@WebServlet(name = "register2", displayName = "register2", description = "Registers new user without checking if the phone number has been used to register before", urlPatterns = {"/register2"})
public class Register2Servlet extends AbstractRegisterServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("REGISTER2");


    public Register2Servlet() {
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

            // do not ge the user that should update phone number
            List<User> users = userDao.findUsersByPhone(countryId, phoneNumber, true);

            if (users != null && users.size() > 0) {
                for (User user : users) {
                    if (!user.getVerified()) {
                        // delete the un-registered one.
                        userDao.deleteUnverifiedUserByPhone(countryId, phoneNumber);
                    }
                }

                createNewUserAndNotify(req, resp, registerModel);
            }
        }
    }
}
