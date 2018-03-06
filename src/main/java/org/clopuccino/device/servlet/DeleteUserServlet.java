package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <code>DeleteUserServlet</code> serves devices with version 1.x to delete account.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "delete-user", displayName = "delete-user", description = "Delete User", urlPatterns = {"/user/delete"})
public class DeleteUserServlet extends AbstractDeleteUserServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DeleteUserServlet.class.getSimpleName());

    private static final long serialVersionUID = 2162293323038267518L;

    public DeleteUserServlet() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp, true);
    }
}