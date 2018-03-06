package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ConnectModel;
import org.clopuccino.domain.User;
import org.clopuccino.domain.UserComputer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>AbstractFindAvailableComputersServlet</code> abstracts different versions of service
 * to find available computers for certain user.
 *
 * @author masonhsieh
 * @version 1.0
 */
public abstract class AbstractFindAvailableComputersServlet extends HttpServlet {

    private static final long serialVersionUID = 1126743332429126342L;

    protected UserDao userDao;

    private UserComputerDao userComputerDao;


    public AbstractFindAvailableComputersServlet() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);
    }

    abstract protected Logger getLogger();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp, boolean responseUserIdIfNoComputerFound) throws ServletException, IOException {
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
            List<User> users = userDao.findUsersByPhone(countryId, phoneNumber, false);

            /**
             * 篩選符合密碼的用戶，如果超過一個，先找到的就算是。
             * 找到使用者後，判斷順序為：
             * 1.若其中有需要更新電話號碼的用戶，則回傳錯誤碼467。
             * 2.若其中有已註冊驗證過的用戶，則使用此用戶檢查可用的電腦。
             * 3.若以上皆不符合，則回傳錯誤碼403。
             */

            if (users != null && users.size() > 0) {
                User foundUser = null;

                // find the correct user by password
                for (User currentUser : users) {
                    if (currentUser.getPasswd().equals(password)) {
                        foundUser = currentUser;

                        break;
                    }
                }

                if (foundUser == null) {
                    // incorrect password
                    String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "password.not.correct", phoneNumber);

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                } else {
                    if (foundUser.getShouldUpdatePhoneNumber()) {
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.should.update.phone.number", phoneNumber);

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(Constants.HTTP_STATUS_SHOULD_UPDATE_PHONE_NUMBER);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else if (foundUser.getVerified() == null || !foundUser.getVerified()) {
                        String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "account.not.verified", phoneNumber);

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write(errorMessage);
                        resp.getWriter().flush();
                    } else {
                        String userId = foundUser.getAccount();

                        // check verification code after checking password
                        if (!verification.equals(Utility.generateVerification(countryId, phoneNumber, password, nickname))) {
                            getLogger().warn("Be careful that user: (" + countryId + ")" + phoneNumber + " is trying to hack the verification code for registration");

                            String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "error");

                            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            resp.getWriter().write(String.valueOf(errorMessage));
                            resp.getWriter().flush();
                        } else {
                            // 使用者曾經連線computer清單
                            List<UserComputer> userComputers = userComputerDao.findUserComputersByUserId(userId);

                            // response user id if no computers found.

                            if (responseUserIdIfNoComputerFound && (userComputers == null || userComputers.size() < 1)) {
                                UserComputer userComputer = new UserComputer();

                                userComputer.setUserId(userId);

                                userComputers = new ArrayList<>();
                                userComputers.add(userComputer);
                            }

                            String responseString = mapper.writeValueAsString(userComputers);

                            resp.setContentType(Constants.CONTENT_TYPE_JSON_UTF8);
                            resp.setStatus(HttpServletResponse.SC_OK);
                            resp.getWriter().write(responseString);
                            resp.getWriter().flush();
                        }
                    }
                }
            } else {
                /* user id not found */
                String errorMessage = ClopuccinoMessages.localizedMessage(clientLocale, "user.not.found");

                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write(errorMessage);
                resp.getWriter().flush();
            }
        }
    }
}
