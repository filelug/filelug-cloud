package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.ComputerDataDao;
import org.clopuccino.db.DatabaseAccess;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * <code>FindCountryCodeByLocaleServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "send-computer-data", displayName = "send-computer-data", description = "find country code by locale", urlPatterns = {"/computer/whoami"})
public class SendComputerDataServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SendComputerDataServlet.class.getSimpleName());

    private static final long serialVersionUID = -8095237000559862311L;

    private final ComputerDataDao computerDataDao;


    public SendComputerDataServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        computerDataDao = new ComputerDataDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            Properties properties = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), Properties.class);

            computerDataDao.createComputerProperties(properties);

            resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("OK");
            resp.getWriter().flush();
        } catch (Exception e) {
            LOGGER.error("Can't process computer data", e);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("ERROR");
            resp.getWriter().flush();
        }
    }
}
