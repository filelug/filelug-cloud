package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
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

/**
 * <code>CreatePurchaseServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "create-purchase", displayName = "create-purchase", description = "create purchase", urlPatterns = {"/product/newPurchase"})
public class CreatePurchaseServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CreatePurchaseServlet.class.getSimpleName());

    private static final long serialVersionUID = 6697360836010238959L;

    private final PurchaseDao purchaseDao;

    private final ClientSessionService clientSessionService;


    public CreatePurchaseServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        purchaseDao = new PurchaseDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String clientLocale = clientSession.getLocale();

            // check json input
            ObjectMapper mapper = Utility.createObjectMapper();

            Purchase purchase = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), Purchase.class);

            String purchaseId = purchase.getPurchaseId();
            String productId = purchase.getProductId();
            String purchaseUserId = purchase.getUserId();
            Long quantity = purchase.getQuantity();

            if (purchaseId == null || purchaseId.trim().length() < 1) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "purchase identifier");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (productId == null || productId.trim().length() < 1) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "product identifier");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (purchaseUserId == null || purchaseUserId.trim().length() < 1) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "user identifier");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (quantity == null || quantity < 1) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "purchase quantity");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                if (purchaseDao.existingPurchaseById(purchaseId)) {
                    LOGGER.info("Purchase duplicated: " + purchaseId);

                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    resp.getWriter().write("purchase exists");
                    resp.getWriter().flush();
                } else {
                    if (purchase.getPurchaseTimestamp() == null) {
                        purchase.setPurchaseTimestamp(System.currentTimeMillis());
                    }

                    // ALSO increase available transfer bytes
                    Purchase newPurchase = purchaseDao.createPurchase(purchase, true);

                    if (newPurchase != null) {
                        LOGGER.info("Purchase created: " + newPurchase);

                        resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write(mapper.writeValueAsString(newPurchase));
                        resp.getWriter().flush();
                    } else {
                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().write(ClopuccinoMessages.localizedMessage(clientLocale, "failed.create.purchase"));
                        resp.getWriter().flush();
                    }
                }
            }
        } catch (IOException e) {
            String errorMessage = "Incorrect request parameter: " + e.getMessage();

            LOGGER.error(errorMessage, e);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        } catch (Exception e) {
            LOGGER.error("Error on creating purchase.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
