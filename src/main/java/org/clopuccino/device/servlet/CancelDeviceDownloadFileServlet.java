package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.PostgresqlDatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ClientDownloadResponseUtility;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.CloseLatchAndDownloadResponse;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <code>CancelDeviceDownloadFileServlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "cancel-device-download-file", displayName = "cancel-device-download-file", description = "Cancel currently running file-downloading requested by device", urlPatterns = {"/directory/ddcancel"})
public class CancelDeviceDownloadFileServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(CancelDeviceDownloadFileServlet.class.getSimpleName());

    private static final long serialVersionUID = -8555811603880487134L;

    private final ClientSessionService clientSessionService;


    public CancelDeviceDownloadFileServlet() {
        super();

        DatabaseAccess dbAccess = new PostgresqlDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String clientLocale = clientSession.getLocale();

                    /* check json input */
            ObjectMapper mapper = Utility.createObjectMapper();

            JsonNode jsonNode = mapper.readTree(new InputStreamReader(req.getInputStream(), "UTF-8"));

            JsonNode transferKeyNode = jsonNode.get("transferKey");

            if (transferKeyNode == null || transferKeyNode.textValue() == null) {
                        /* transfer key not found or invalid */
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "param.null.or.empty", "transfermation key");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else {
                        /* update status of the upload file to db */
                final String transferKey = transferKeyNode.textValue();

                try {
                    CloseLatchAndDownloadResponse wrapper = ClientDownloadResponseUtility.get(transferKey);

                    if (wrapper != null) {
                        HttpServletResponse clientResponse = wrapper.getResponse();

                        if (clientResponse != null && clientResponse.getOutputStream() != null) {
                            clientResponse.getOutputStream().close();
                        }

                        ClientDownloadResponseUtility.remove(transferKey);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed cancel download for transfer key: " + transferKey, e);
                } finally {
                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(transferKey);
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on canceling download", e);

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(ClopuccinoMessages.getMessage("failed.cancel.download"));
            resp.getWriter().flush();
        }
    } 
}
