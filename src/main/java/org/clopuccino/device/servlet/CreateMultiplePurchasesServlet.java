package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.ArrayList;
import java.util.List;

/**
 * <code>CreateMultiplePurchasesServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "create-multiple-purchases", displayName = "create-multiple-purchases", description = "create multiple purchases", urlPatterns = {"/product/newMultiplePurchases"})
public class CreateMultiplePurchasesServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CreateMultiplePurchasesServlet.class.getSimpleName());

    private static final long serialVersionUID = 4687937519236105474L;

    private final PurchaseDao purchaseDao;

    private final ClientSessionService clientSessionService;


    public CreateMultiplePurchasesServlet() {
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

                    /* check json input */
            ObjectMapper mapper = Utility.createObjectMapper();

            List<Purchase> purchases = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), new TypeReference<List<Purchase>>() {
            });

            if (purchases != null && purchases.size() > 0) {
                List<Purchase> successCreated = new ArrayList<>();

                for (Purchase purchase : purchases) {
                    String purchaseId = purchase.getPurchaseId();
                    String productId = purchase.getProductId();
                    String purchaseUserId = purchase.getUserId();
                    Long quantity = purchase.getQuantity();

                    if (purchaseId == null || purchaseId.trim().length() < 1) {
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "purchase identifier");

                        LOGGER.error("Skipped creating purchase: " + purchase.toString() + "\n" + errorMessage);
                    } else if (productId == null || productId.trim().length() < 1) {
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "product identifier");

                        LOGGER.error("Skipped creating purchase: " + purchase.toString() + "\n" + errorMessage);
                    } else if (purchaseUserId == null || purchaseUserId.trim().length() < 1) {
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "user identifier");

                        LOGGER.error("Skipped creating purchase: " + purchase.toString() + "\n" + errorMessage);
                    } else if (quantity == null || quantity < 1) {
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "purchase quantity");

                        LOGGER.error("Skipped creating purchase: " + purchase.toString() + "\n" + errorMessage);
                    } else {
                        if (purchaseDao.existingPurchaseById(purchaseId)) {
                            LOGGER.error("Skipped creating purchase: " + purchase.toString() + "\nPurchase duplicaated.");
                        } else {
                            if (purchase.getPurchaseTimestamp() == null) {
                                purchase.setPurchaseTimestamp(System.currentTimeMillis());
                            }

                                    /* ALSO increase available transfer bytes */
                            Purchase newPurchase = purchaseDao.createPurchase(purchase, true);

                            if (newPurchase != null) {
                                successCreated.add(newPurchase);
                                LOGGER.info("Purchase created: " + newPurchase);
                            } else {
                                LOGGER.error("Skipped creating purchase: " + purchase.toString() + "\n" + ClopuccinoMessages.localizedMessage(clientLocale, "failed.create.purchase"));
                            }
                        }
                    }
                }

                String successCreatedString = mapper.writeValueAsString(successCreated);

                resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(successCreatedString);
                resp.getWriter().flush();
            } else {
                LOGGER.warn("Null purchases to be created. Sent by user: " + clientSession.getUserId() + "session id: " + clientSession.getSessionId());
            }
        } catch (IOException e) {
            String errorMessage = "Incorrect request parameter: " + e.getMessage();

            LOGGER.error(errorMessage, e);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        } catch (Exception e) {
            LOGGER.error("Error on creating multiple purchases.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
