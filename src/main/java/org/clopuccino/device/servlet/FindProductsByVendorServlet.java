package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.ProductDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.Product;
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
 * <code>FindProductsByVendorServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "find-products-information-by-vendor", displayName = "find-products-information-by-vendor", description = "find product information by vendor", urlPatterns = {"/product/findByVendor"})
public class FindProductsByVendorServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FindProductsByVendorServlet.class.getSimpleName());

    private static final long serialVersionUID = -4639234550347828532L;

    private final ProductDao productDao;

    private final ClientSessionService clientSessionService;


    public FindProductsByVendorServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        productDao = new ProductDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String clientLocale = clientSession.getLocale();

            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode vendorNode = jsonNode.get("vendor");

            if (vendorNode == null || vendorNode.textValue() == null || vendorNode.textValue().trim().length() < 1) {
                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("PRODUCT NOT FOUND");
                resp.getWriter().flush();
            } else {
                if (clientLocale == null || clientLocale.trim().length() < 1 || clientLocale.trim().toLowerCase().startsWith("en_")) {
                    clientLocale = "en";
                }

                String serverLocale = ClopuccinoMessages.getLocale(clientLocale);

                List<Product> products = productDao.findProductssByVendor(vendorNode.textValue(), serverLocale);

                String responseJsonString;
                if (products != null && products.size() > 0) {
                    responseJsonString = mapper.writeValueAsString(products);
                } else {
                    responseJsonString = "";
                }

                resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(responseJsonString);
                resp.getWriter().flush();
            }
        } catch (Exception e) {
            LOGGER.error("Error on finding products by vendor.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
