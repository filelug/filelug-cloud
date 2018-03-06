package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <code>DeleteUserServlet2</code> serves device with version 2.0.0 or later to delete account.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "delete-user2", displayName = "delete-user2", description = "Delete User(V2)", urlPatterns = {"/user/delete2"})
public class DeleteUserServlet2 extends AbstractDeleteUserServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DeleteUserServlet2.class.getSimpleName());

    private static final long serialVersionUID = -156608494698930857L;

    public DeleteUserServlet2() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp, false);
    }
}
