package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ConnectModel;
import org.clopuccino.domain.User;
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
 * <code>FindUserByPhoneServlet</code> finds the user by the country id and phone number.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "find-user-by-phone", displayName = "find-user-by-phone", description = "Find user by user country id and phone number", urlPatterns = {"/user/find-by-phone"})
public class FindUserByPhoneServlet extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FindUserByPhoneServlet.class.getSimpleName());

    private static final long serialVersionUID = 4330829340974912660L;

    private final UserDao userDao;


    public FindUserByPhoneServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = Utility.createObjectMapper();

        ConnectModel connectModel = mapper.readValue(new InputStreamReader(req.getInputStream(), "UTF-8"), ConnectModel.class);

        String countryId = connectModel.getCountryId();
        String phoneNumber = connectModel.getPhoneNumber();
        String password = connectModel.getPassword();
        String nickname = connectModel.getNickname();
        String verification = connectModel.getVerification();
        String clientLocale = connectModel.getLocale();

        if (countryId == null || phoneNumber == null || password == null || nickname == null || verification == null
            || countryId.trim().length() < 1 || phoneNumber.trim().length() < 1 || password.trim().length() < 1 || nickname.trim().length() < 1 || verification.trim().length() < 1
            || password.equals(DigestUtils.sha256Hex(""))
            || verification.equals(DigestUtils.sha256Hex(""))) {
            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "at.least.one.empty.account.property");

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(errorMessage);
            resp.getWriter().flush();
        } else {
            List<User> users = userDao.findUsersByPhone(countryId, phoneNumber, true);

            // It's possible to have two users with the same phone number
            // even when skipShouldUpdatePhoneNumber set to true:
            // 1. The current user whose shouldUpdatePhoneNumber is false
            // 2. The user who has registered but one verified yet.

            if (users != null && users.size() > 0) {
                User user = null;

                for (User currentUser : users) {
                    if (currentUser.getVerified()) {
                        user = currentUser;

                        break;
                    }
                }

                if (user == null) {
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "account.not.verified", phoneNumber);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    /* validate with password */
                    String foundPasswd = user.getPasswd();

                    if (password.equals(foundPasswd)) {
                        // Check verification code after checking password
                        if (!verification.equals(Utility.generateVerification(countryId, phoneNumber, password, nickname))) {
                            LOGGER.warn("User: (" + countryId + ") " + phoneNumber +  " is testing verification code for findUserById from desktop.");

                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            resp.getWriter().write(String.valueOf(errorMessage));
                            resp.getWriter().flush();
                        } else {
                            // clone user and reamin only what we want
                            User clone = new User();
                            clone.setAccount(user.getAccount());
                            clone.setNickname(user.getNickname());
                            clone.setShowHidden(user.getShowHidden());

                            String responseJsonString = mapper.writeValueAsString(clone);

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_OK);
                            resp.getWriter().write(responseJsonString);
                            resp.getWriter().flush();
                        }
                    } else {
                        /* incorrect password */
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "password.not.correct", phoneNumber);

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    }
                }
            } else {
                /* user not found */
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            }
        }
    }
}
