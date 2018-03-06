package org.clopuccino.device.servlet;

import ch.qos.logback.classic.Logger;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.ConnectModel;
import org.clopuccino.domain.DeviceToken;
import org.clopuccino.domain.User;
import org.clopuccino.service.DeviceTokenService;
import org.clopuccino.service.LoginService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <code>AbstractLoginServlet</code> provides common utility for login and relogin servlets
 *
 * @author masonhsieh
 * @version 1.0
 */
public abstract class AbstractLoginServlet extends HttpServlet {

    private static final long serialVersionUID = 3966985526018568472L;

    public AbstractLoginServlet() {
        super();
    }

    abstract protected Logger getLogger();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp, boolean checkDesktopSocket, ConnectModel connectModel, User user) throws ServletException, IOException {
        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        LoginService loginService = new LoginService(dbAccess);

        DeviceTokenService deviceTokenService = new DeviceTokenService(dbAccess);

        DeviceToken deviceToken = connectModel.getDeviceToken();

        if (deviceToken != null) {
            // sometimes account is null, set the current user id to it.

            if (deviceToken.getAccount() == null) {
                deviceToken.setAccount(user.getAccount());
            }

            if (deviceToken.getFilelugVersion() == null || deviceToken.getFilelugVersion().equals(DatabaseConstants.DEFAULT_FILELUG_VERSION_VALUE)) {
                String filelugVersion = connectModel.getDeviceVersion();

                if (filelugVersion != null && !filelugVersion.equals(DatabaseConstants.DEFAULT_FILELUG_VERSION_VALUE)) {
                    deviceToken.setFilelugVersion(filelugVersion);
                }
            }

            if (deviceToken.getFilelugBuild() == null || deviceToken.getFilelugBuild().equals(DatabaseConstants.DEFAULT_FILELUG_BUILD_VALUE)) {
                String filelugBuild = connectModel.getDeviceBuild();

                if (filelugBuild != null && !filelugBuild.equals(DatabaseConstants.DEFAULT_FILELUG_BUILD_VALUE)) {
                    deviceToken.setFilelugBuild(filelugBuild);
                }
            }

            try {
                deviceTokenService.createOrUploadDeviceToken(deviceToken, connectModel.getLocale());
            } catch (Exception e) {
                getLogger().error("Error on processing device token: " + deviceToken, e);
            }
        } else {
            getLogger().warn("Device not sending device token for user: '" + user.getNickname() + "'(id=" + connectModel.getAccount() + ") connect to computer: " + connectModel.getComputerId());
        }

        if (checkDesktopSocket) {
            loginService.doLoginWithComputer(req, resp, connectModel.getLocale(), connectModel.getComputerId(), connectModel.getDeviceVersion(), connectModel.getDeviceBuild(), user, deviceToken);
        } else {
            loginService.doLoginWithoutComputer(req, resp, user, deviceToken);
        }
    }
}
