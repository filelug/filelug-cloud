package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.CountryModel;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.service.CountryService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

/**
 * <code>FindAvailableCountriesByLocaleServlet</code> is the second version of service <code>FindCountryCodeByLocaleServlet</code>.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "find-available-countries-by-locale", displayName = "find-available-countries-by-locale", description = "Find Available Countries By Locale", urlPatterns = {"/country/available"})
public class FindAvailableCountriesByLocaleServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FindAvailableCountriesByLocaleServlet.class.getSimpleName());

    private static final long serialVersionUID = -6101824797248325438L;

    private final ClientSessionService clientSessionService;

    private final CountryService countryService;

    public FindAvailableCountriesByLocaleServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);

        countryService = new CountryService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String clientLocale = clientSession.getLocale();

            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode localeNode = jsonNode.get("ccode-locale");

            if (localeNode == null || localeNode.textValue() == null) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "Locale of Country Name");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                String localeText = localeNode.textValue();

                Locale properLocale = countryService.findProperLocaleFromLocale(ClopuccinoMessages.getLocale(localeText));

                List<CountryModel> countryModels = countryService.findAvailableCountriesByLocale(properLocale.toString());

                String responseJsonString = mapper.writeValueAsString(countryModels);

                resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(responseJsonString);
                resp.getWriter().flush();
            }
        } catch (Exception e) {
            LOGGER.error("Can't find country code by locale!", e);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("ERROR");
            resp.getWriter().flush();
        }
    }
}
