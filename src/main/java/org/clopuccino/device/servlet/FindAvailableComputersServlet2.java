package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ConnectModel;
import org.clopuccino.domain.User;
import org.clopuccino.domain.UserComputer;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>FindAvailableComputersServlet2</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "find-available-computers2", displayName = "find-available-computers2", description = "Find available computers(v2)", urlPatterns = {"/computer/available2"})
public class FindAvailableComputersServlet2 extends AbstractFindAvailableComputersServlet {

    private static final long serialVersionUID = 6087982137049006301L;

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FindAvailableComputersServlet2.class.getSimpleName());

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    public FindAvailableComputersServlet2() {
        super();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp, true);
    }
}
