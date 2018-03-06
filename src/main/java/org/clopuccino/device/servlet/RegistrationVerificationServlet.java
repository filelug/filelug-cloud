package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <code>RegistrationVerificationServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "registration-verification", displayName = "registration-verification", description = "Verify registered account", urlPatterns = {"/verify"})
public class RegistrationVerificationServlet extends AbstractRegistrationVerificationServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(RegistrationVerificationServlet.class.getSimpleName());

    private static final long serialVersionUID = -8160164257911711741L;

    public RegistrationVerificationServlet() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp, false);
    }
}
