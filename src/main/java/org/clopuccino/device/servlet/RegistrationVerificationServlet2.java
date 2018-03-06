package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <code>RegistrationVerificationServlet2</code>
 * logins user if registered account verified successfully and add user information to the response string.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "registration-verification2", displayName = "registration-verification2", description = "Verify registered account and login user if registered successfully", urlPatterns = {"/verify2"})
public class RegistrationVerificationServlet2 extends AbstractRegistrationVerificationServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(RegistrationVerificationServlet2.class.getSimpleName());

    private static final long serialVersionUID = 7500622033904823040L;


    public RegistrationVerificationServlet2() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp, true);
    }
}
