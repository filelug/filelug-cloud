package org.clopuccino.server.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.ApplyConnectionDao;
import org.clopuccino.dao.ComputerDao;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.ApprovedUserModel;
import org.clopuccino.domain.ClientSession;
import org.clopuccino.domain.User;
import org.clopuccino.service.ClientSessionService;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>CheckUserExistingServlet</code> makes sure the user eixsts and the password is correct.
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebServlet(name = "find-approved-connection-users2", displayName = "find-approved-connection-users2", description = "Find approved connection-users by computer (V2)", urlPatterns = {"/user/approved2"})
public class FindApprovedConnectionUsersServlet2 extends HttpServlet {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FindApprovedConnectionUsersServlet2.class.getSimpleName());

    private static final long serialVersionUID = -5671497169842380945L;

    private final UserDao userDao;

    private final ApplyConnectionDao applyConnectionDao;

    private final ComputerDao computerDao;

    private final UserComputerDao userComputerDao;

    private final ClientSessionService clientSessionService;


    public FindApprovedConnectionUsersServlet2() {
        super();

        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        userDao = new UserDao(dbAccess);

        applyConnectionDao = new ApplyConnectionDao(dbAccess);

        computerDao = new ComputerDao(dbAccess);

        userComputerDao = new UserComputerDao(dbAccess);

        clientSessionService = new ClientSessionService(dbAccess);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ClientSession clientSession = clientSessionService.prepareClientSessionFrom(req);

            String adminUserId = clientSession.getUserId();

            long computerId = clientSession.getComputerId();

            String locale = clientSession.getLocale();

            // check if computer exists

            String computerName = computerDao.findComputerNameByComputerId(computerId);

            if (computerName == null) {
                // computer not found
                
                resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                resp.setStatus(Constants.HTTP_STATUS_COMPUTER_NOT_FOUND);
                resp.getWriter().write("Computer Not Found");
                resp.getWriter().flush();
            } else {
                User adminUser = userDao.findUserById(adminUserId);

                if (adminUser != null) {
                    List<User> tmpUsers = applyConnectionDao.findApprovedUsersByComputerId(computerId);

                    List<ApprovedUserModel> approvedUsers = new ArrayList<>();

                    if (tmpUsers != null && tmpUsers.size() > 0) {
                        for (User tmpUser : tmpUsers) {
                            String approvedUserId = tmpUser.getAccount();

                            String userComputerId = Utility.generateUserComputerIdFrom(approvedUserId, computerId);

                            boolean allowAlias = userComputerDao.findAllowAliasById(userComputerId);

                            ApprovedUserModel approvedUser = new ApprovedUserModel(approvedUserId, tmpUser.getCountryId(), tmpUser.getPhoneNumber(), tmpUser.getNickname(), tmpUser.getShowHidden(), allowAlias, null);

                            approvedUsers.add(approvedUser);
                        }

                        ObjectMapper mapper = Utility.createObjectMapper();

                        String responseJsonString = mapper.writeValueAsString(approvedUsers);

                        resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write(responseJsonString);
                        resp.getWriter().flush();
                    }
                } else {
                    // user not found
                    String errorMessage = ClopuccinoMessages.localizedMessage(locale, "user.not.found");

                    resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(errorMessage);
                    resp.getWriter().flush();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on finding approved connection users.", e);

            String errorMessage = e.getMessage();

            resp.setContentType(Constants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(errorMessage != null ? errorMessage : "ERROR");
            resp.getWriter().flush();
        }
    }
}