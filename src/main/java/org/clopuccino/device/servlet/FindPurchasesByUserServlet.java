package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.PurchaseDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.Purchase;
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
 * <code>FindPurchasesByUserServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "find-purchases-by-user", displayName = "find-purchases-by-user", description = "find purchases information for the specified user", urlPatterns = {"/product/findPurchasesByUser"})
public class FindPurchasesByUserServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FindPurchasesByUserServlet.class.getSimpleName());

    private static final long serialVersionUID = -6322482302755213850L;

    private final PurchaseDao purchaseDao;

    private final ClientSessionService clientSessionService;


    public FindPurchasesByUserServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        purchaseDao = new PurchaseDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            // Can only find purchanses of his/her own.
            String userId = clientSession.getUserId();

            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode requestUserNode = jsonNode.get("user-id");

            if (requestUserNode == null || requestUserNode.textValue() == null || requestUserNode.textValue().trim().length() < 1 || !requestUserNode.textValue().equals(userId)) {
                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("USER NOT FOUND");
                resp.getWriter().flush();
            } else {
                List<Purchase> purchases = purchaseDao.findPurchasessByUserId(userId);

                String responseJsonString;
                if (purchases != null && purchases.size() > 0) {
                    responseJsonString = mapper.writeValueAsString(purchases);
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
            resp.getWriter().write(errorMessage != null ? e.getMessage() : "ERROR");
            resp.getWriter().flush();
        }
    }
}
