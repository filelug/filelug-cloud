package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.FileUploadDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.TransferHistoryModel;
import org.clopuccino.domain.TransferHistoryTimeType;
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
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "find-all-file-uploaded", displayName = "find-all-file-uploaded", description = "Find information of all successfully uploaded files", urlPatterns = {"/directory/uhis"})
public class FindAllFileUploadedServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FindAllFileUploadedServlet.class.getSimpleName());

    private static final long serialVersionUID = 5406569028377224245L;

    private final DatabaseAccess dbAccess;

    private final ClientSessionService clientSessionService;


    public FindAllFileUploadedServlet() {
        super();

        dbAccess = DatabaseUtility.createDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String clientLocale = null;

        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String userId = clientSession.getUserId();
            clientLocale = clientSession.getLocale();

            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode typeNode = jsonNode.get("type");

            TransferHistoryTimeType type = TransferHistoryTimeType.TRANSFER_HISTORY_TYPE_LATEST_20;

            if (typeNode != null && typeNode.isInt()) {
                type = TransferHistoryTimeType.toType(typeNode.intValue());
            }

            FileUploadDao fileUploadDao = new FileUploadDao(dbAccess);

            List<TransferHistoryModel> histories = fileUploadDao.findFileUploadedForUser(userId, true, type);

            String historiesJson = mapper.writeValueAsString(histories);

            resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(historiesJson);
            resp.getWriter().flush();
        } catch (Exception e) {
            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "failed.find.uploaded.files");

            LOGGER.error(errorMessage, e);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
