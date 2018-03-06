package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.ApplyConnectionDao;
import org.clopuccino.dao.ComputerDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ApplyConnection;
import org.clopuccino.domain.ChangeAdministratorModel;
import org.clopuccino.domain.Computer;
import org.clopuccino.domain.User;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <code>ChangeComputerAdministratorServlet</code> changes computer administrator
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "change-computer-administrator", displayName = "change-computer-administrator", description = "Change computer administrator", urlPatterns = {"/computer/chadmin"})
public class ChangeComputerAdministratorServlet extends HttpServlet {
    private static final long serialVersionUID = 1268628261720146651L;

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ChangeComputerAdministratorServlet.class.getSimpleName());

    private final UserDao userDao;

    private final ComputerDao computerDao;

    private final ApplyConnectionDao applyConnectionDao;


    public ChangeComputerAdministratorServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        applyConnectionDao = new ApplyConnectionDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = Utility.createObjectMapper();

            ChangeAdministratorModel computerModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), ChangeAdministratorModel.class);

            String oldAdminUserId = computerModel.getOldAdminUserId();
            String oldAdminPassword = computerModel.getOldAdminPassword();

            String newAdminUserId = computerModel.getNewAdminUserId();
            String newAdminPassword = computerModel.getNewAdminPassword();

            String clientLocale = computerModel.getLocale();
            Long computerId = computerModel.getComputerId();
            String recoveryKey = computerModel.getRecoveryKey();

            String verification = computerModel.getVerification();

            String encryptedEmpty = DigestUtils.sha256Hex("");

            if (oldAdminUserId == null || oldAdminPassword == null || verification == null || newAdminUserId == null || newAdminPassword == null || computerId == null || recoveryKey == null
                || oldAdminUserId.trim().length() < 1 || oldAdminPassword.trim().length() < 1 || verification.trim().length() < 1 || newAdminUserId.trim().length() < 1 || newAdminPassword.trim().length() < 1 || computerId < 0 || recoveryKey.trim().length() < 1
                || oldAdminPassword.equals(encryptedEmpty)
                || newAdminPassword.equals(encryptedEmpty)
                || verification.equals(encryptedEmpty)) {
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
//        } else if (!verification.equals(Utility.generateChangeComputerAdminVerification(oldAdminUserId, oldAdminPassword, newAdminUserId, newAdminPassword, computerId, recoveryKey))) {
//            LOGGER.warn("User: " + oldAdminUserId + " is testing verification code for login from device");
//
//            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");
//
//            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
//            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            resp.getWriter().write(String.valueOf(errorMessage));
//            resp.getWriter().flush();
            } else {
                // checking if computer exists must be prior than checking if user exists

                Computer computer = computerDao.findComputerById(computerId);

                if (computer == null) {
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "computer.not.found");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(Constants.HTTP_STATUS_COMPUTER_NOT_FOUND);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    User oldAdmin = userDao.findUserById(oldAdminUserId);

                    if (oldAdmin != null) {
                    /* if old admin verified */
                        Boolean oldAdminVerified = oldAdmin.getVerified();

                        if (oldAdminVerified == null || !oldAdminVerified) {
                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "account.not.verified", oldAdmin.getPhoneNumber());

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            resp.getWriter().write(errorMessage);
                            resp.getWriter().flush();
                        } else {
                        /* validate old admin password */
                            String foundOldAdminPasswd = oldAdmin.getPasswd();

                            if (oldAdminPassword.equals(foundOldAdminPasswd)) {
                            /* check new admin exists */
                                User newAdmin = userDao.findUserById(newAdminUserId);

                                if (newAdmin != null) {
                                /* if new admin verified */
                                    Boolean newAdminVerified = newAdmin.getVerified();

                                    if (newAdminVerified == null || !newAdminVerified) {
                                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "account.not.verified", newAdmin.getPhoneNumber());

                                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                        resp.getWriter().write(errorMessage);
                                        resp.getWriter().flush();
                                    } else {
                                    /* validate new admin password */
                                        if (newAdminPassword.equals(newAdmin.getPasswd())) {
                                        /* make sure the administrator of the computer is the same with the old admin user id */
                                            if (!oldAdminUserId.equals(computer.getUserId())) {
                                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.computer.admin", computer.getComputerName(), newAdmin.getNickname());

                                                resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                                resp.setStatus(Constants.HTTP_STATUS_USER_NOT_ADMIN);
                                                resp.getWriter().write(errorMessage);
                                                resp.getWriter().flush();
                                            } else {
                                            /* check approved apply connection */

                                                ApplyConnection applyConnect = applyConnectionDao.findApplyConnectionByApplyUserAndComputerId(newAdminUserId, computerId);

                                                if (applyConnect == null) {
                                                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.apply.connection", newAdmin.getNickname(), computer.getComputerName());

                                                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                                    resp.setStatus(Constants.HTTP_STATUS_USER_NOT_APPLY_CONNECTION_YET);
                                                    resp.getWriter().write(errorMessage);
                                                    resp.getWriter().flush();
                                                } else if (applyConnect.getApproved() == null || !applyConnect.getApproved()) {
                                                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "apply.connection.not.approved.yet", newAdmin.getNickname(), computer.getComputerName());

                                                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                                    resp.setStatus(Constants.HTTP_STATUS_APPLY_CONNECTION_NOT_APPROVED_YET);
                                                    resp.getWriter().write(errorMessage);
                                                    resp.getWriter().flush();
                                                } else if (!verification.equals(Utility.generateChangeComputerAdminVerification(oldAdminUserId, oldAdminPassword, newAdminUserId, newAdminPassword, computerId, recoveryKey))) {
                                                    LOGGER.warn("User: " + oldAdminUserId + " is testing verification code for login from device");

                                                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");

                                                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                                    resp.getWriter().write(String.valueOf(errorMessage));
                                                    resp.getWriter().flush();
                                                } else {
                                                    computer.setUserId(newAdminUserId);

                                                    computerDao.updateComputer(computer);

                                                    resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                                                    resp.setStatus(HttpServletResponse.SC_OK);
                                                    resp.getWriter().write("OK");
                                                    resp.getWriter().flush();
                                                }
                                            }
                                        } else {
                                        /* incorrect new admin password */
                                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "password.not.correct", newAdmin.getPhoneNumber());

                                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                            resp.getWriter().write(errorMessage);
                                            resp.getWriter().flush();
                                        }
                                    }
                                } else {
                                /* new admin not exists */
                                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                    resp.getWriter().write(errorMessage);
                                    resp.getWriter().flush();
                                }
                            } else {
                            /* incorrect old admin password */
                                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "password.not.correct", oldAdmin.getPhoneNumber());

                                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                resp.getWriter().write(errorMessage);
                                resp.getWriter().flush();
                            }
                        }
                    } else {
                    /* old admin not found */
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on changing computer administrator.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}
