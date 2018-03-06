package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <code>FindAvailableComputersServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "find-available-computers", displayName = "find-available-computers", description = "Find available computers", urlPatterns = {"/computer/available"})
public class FindAvailableComputersServlet extends AbstractFindAvailableComputersServlet {

    private static final long serialVersionUID = -3214977922677185063L;

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FindAvailableComputersServlet.class.getSimpleName());

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    public FindAvailableComputersServlet() {
        super();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp, false);
    }
}
