package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.CountryDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.CountryModel;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * <code>FindCountryCodeByLocaleServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "find-country-information-by-locale", displayName = "find-country-information-by-locale", description = "find country code by locale", urlPatterns = {"/system/country"})
public class FindCountryByLocaleServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FindCountryByLocaleServlet.class.getSimpleName());

    private static final long serialVersionUID = -2056958212340441663L;

    private final CountryDao countryDao;


    public FindCountryByLocaleServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        countryDao = new CountryDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode localeNode = jsonNode.get("ccode-locale");

            if (localeNode == null) {
                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("PAGE NOT FOUND");
                resp.getWriter().flush();
            } else {
                String localeText = localeNode.textValue();

                if (localeText == null || localeText.trim().length() < 1) {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("PAGE NOT FOUND");
                    resp.getWriter().flush();
                } else {
                    String locale = ClopuccinoMessages.getLocale(localeText);

                    if (locale == null) {
                        locale = DatabaseConstants.COLUMN_NAME_COUNTRY_LOCALE_COLUMN_DEFAULT_NAME;
                    }

                    List<CountryModel> countryModels = countryDao.findAvailableCountriesByLocale(locale);

                    String responseJsonString = mapper.writeValueAsString(countryModels);

                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(responseJsonString);
                    resp.getWriter().flush();
                }
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
