package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.ComputerDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.Computer;
import org.clopuccino.domain.ConnectModel;
import org.clopuccino.domain.User;
import org.clopuccino.server.servlet.websocket.ConnectSocket;
import org.clopuccino.server.servlet.websocket.ConnectSocketUtilities;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.CloseReason;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <code>Disconnect2Servlet</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "disconnect2", displayName = "disconnect2", description = "Disconnect non-admin user between repository and server", urlPatterns = {"/disconnect2"})
public class Disconnect2Servlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("DISCONNECT2");

    private static final long serialVersionUID = 787520598339323638L;

    private final UserDao userDao;

    private final ComputerDao computerDao;


    public Disconnect2Servlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            ConnectModel connectModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), ConnectModel.class);

            final String userId = connectModel.getAccount();
            final String adminUserId = connectModel.getAdminAccount();
            String adminPassword = connectModel.getPassword();
            String adminNickname = connectModel.getNickname();
            String adminVerification = connectModel.getVerification();
            Long computerId = connectModel.getComputerId();
            String localeString = connectModel.getLocale();

            if (userId == null || adminUserId == null || adminPassword == null || adminNickname == null || adminVerification == null || computerId == null
                || userId.trim().length() < 1 || adminUserId.trim().length() < 1 || adminPassword.trim().length() < 1 || adminNickname.trim().length() < 1 || adminVerification.trim().length() < 1 || computerId < 0
                || adminPassword.equals(DigestUtils.sha256Hex(""))) {
                String errorMessage = ClopuccinoMessages.localizedMessage(localeString, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            } else if (!adminVerification.equals(Utility.generateVerification(adminUserId, adminPassword, adminNickname))) {
                LOGGER.warn("User: " + userId + " is testing verification code for disconnect2");

                String errorMessage = ClopuccinoMessages.localizedMessage(localeString, "error");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(String.valueOf(errorMessage));
                resp.getWriter().flush();
            } else {
                // checking if computer exists must be prior than checking if user exists

                Computer computer = computerDao.findComputerById(computerId);

                if (computer == null) {
                    String errorMessage = ClopuccinoMessages.localizedMessage(localeString, "computer.not.found");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(Constants.HTTP_STATUS_COMPUTER_NOT_FOUND);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    User user = userDao.findUserById(userId);

                    if (user == null) {
                        /* user not exists */
                        String errorMessage = ClopuccinoMessages.localizedMessage(localeString, "user.not.found");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else {
                        /* if user verified */
                        Boolean verified = user.getVerified();

                        if (verified == null || !verified) {
                            String errorMessage = ClopuccinoMessages.localizedMessage(localeString, "account.not.verified", user.getPhoneNumber());

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            resp.getWriter().write(errorMessage);
                            resp.getWriter().flush();
                        } else {
                            User admin = userDao.findUserById(adminUserId);

                            if (admin == null) {
                                /* admin not exists */
                                String errorMessage = ClopuccinoMessages.localizedMessage(localeString, "user.not.found");

                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                resp.getWriter().write(errorMessage);
                                resp.getWriter().flush();
                            } else {
                                /* if admin verified */
                                Boolean adminVerified = admin.getVerified();

                                if (adminVerified == null || !adminVerified) {
                                    String errorMessage = ClopuccinoMessages.localizedMessage(localeString, "account.not.verified", admin.getPhoneNumber());

                                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                    resp.getWriter().write(errorMessage);
                                    resp.getWriter().flush();
                                } else {
                                    /* validate with admin password */
                                    String foundPasswd = admin.getPasswd();

                                    if (adminPassword.equals(foundPasswd)) {
                                        /* make sure the admin of the computer is the correct one */

                                        if (!adminUserId.equals(computer.getUserId())) {
                                            String errorMessage = ClopuccinoMessages.localizedMessage(localeString, "user.not.computer.admin", computer.getComputerName(), admin.getNickname());

                                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                            resp.setStatus(Constants.HTTP_STATUS_USER_NOT_ADMIN);
                                            resp.getWriter().write(errorMessage);
                                            resp.getWriter().flush();
                                        } else {
                                            /* DON'T check apply-connection - the applied one maybe deleted or set to not approved before disconnect */

                                            String userComputerId = Utility.generateUserComputerIdFrom(userId, computer.getComputerId());

                                            // When invoiked from desktop will NOT disconnect NOR close the socket session.
                                            // So the socket of the server should do the close.

                                            ConnectSocket socket = ConnectSocket.getInstance(userComputerId);

                                            if (socket != null) {
                                                ConnectSocket.removeInstance(userComputerId);

                                                ConnectSocketUtilities.closeSession(socket.getSession(), CloseReason.CloseCodes.NORMAL_CLOSURE, "Request to close socket by client.");
                                            }

                                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                            resp.setStatus(HttpServletResponse.SC_OK);
                                            resp.getWriter().write("OK");
                                            resp.getWriter().flush();
                                        }
                                    } else {
                                        /* password not correct */
                                        String errorMessage = ClopuccinoMessages.localizedMessage(localeString, "password.not.correct", admin.getPhoneNumber());

                                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                        resp.getWriter().write(errorMessage);
                                        resp.getWriter().flush();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (JsonProcessingException e) {
            String errorMessage = "Incorrect content format for disconnect request";

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        } catch (Exception e) {
            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(e.getMessage() != null ? e.getMessage() : "");
            resp.getWriter().flush();
        }
    }
}
