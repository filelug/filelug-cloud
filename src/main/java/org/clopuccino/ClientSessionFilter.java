package org.clopuccino;

import ch.qos.logback.classic.Logger;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.service.ClientSessionService;
import org.clopuccino.domain.ClientSession;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <code>ClientSessionFilter</code> validates sessions from clients
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebFilter(filterName = "ClientSession")
public class ClientSessionFilter implements Filter {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ClientSessionFilter.class.getSimpleName());

    private ClientSessionService clientSessionService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;

        HttpServletResponse resp = (HttpServletResponse)response;

        try {
            // validate client session id

            String clientSessionId = req.getHeader(Constants.HTTP_HEADER_FILELUG_SESSION_ID_NAME);

            if (clientSessionId == null || clientSessionId.trim().length() < 1) {
                clientSessionId = req.getHeader(Constants.HTTP_HEADER_AUTHORIZATION_NAME);
            }

            if (clientSessionId == null || clientSessionId.trim().length() < 1) {
                String errorMessage = ClopuccinoMessages.localizedMessage(ClopuccinoMessages.DEFAULT_LOCALE, "session.not.provided");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();

                LOGGER.warn("Session not provided for service: " + req.getRequestURI());
            } else {
                ClientSession clientSession = clientSessionService.findClientSessionBySessionId(clientSessionId);

                if (clientSession == null) {
                    // session not found

                    String errorMessage = ClopuccinoMessages.localizedMessage(ClopuccinoMessages.DEFAULT_LOCALE, "session.not.exists");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();

                    LOGGER.debug(String.format("Session not found.\nSession id: '%s'\nRequest URI: '%s'", clientSessionId, req.getRequestURI()));
                } else if (clientSession.checkTimeout()) {
                    // invalid session

                    String errorMessage = ClopuccinoMessages.localizedMessage(ClopuccinoMessages.DEFAULT_LOCALE, "invalid.session");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();

                    LOGGER.debug(String.format("Session timeout.\nSession id: '%s'\nRequest URI: '%s'", clientSessionId, req.getRequestURI()));
                } else {
                    // update session last access timestamp

                    long lastAccessTimestamp = System.currentTimeMillis();

                    clientSessionService.updateClientSessionLastAccessTimestamp(clientSessionId, lastAccessTimestamp);

                    clientSession.setLastAccessTime(lastAccessTimestamp);

                    req.setAttribute("clientSession", clientSession);
//                    req.setAttribute("sessionId", clientSessionId);
//                    request.setAttribute("userId", clientSession.getUserId());
//                    request.setAttribute("computerId", clientSession.getComputerId());
//                    request.setAttribute("locale", clientSession.getLocale());
//                    request.setAttribute("deviceToken", clientSession.getDeviceToken());
//                    request.setAttribute("showHidden", clientSession.isShowHidden());

                    chain.doFilter(req, resp);
                }
            }
        } catch (Exception e) {
            String errorMessage = e.getClass().getName() + ": " + e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();

            LOGGER.error(String.format("Error on checking session.\nRequest URI: '%s'", req.getRequestURI()), e);
        }
    }

    @Override
    public void destroy() {
        clientSessionService = null;
    }
}
