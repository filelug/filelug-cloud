package org.clopuccino.service;

import ch.qos.logback.classic.Logger;
import org.clopuccino.ClopuccinoMessages;
import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.PropertyConstants;
import org.clopuccino.dao.ComputerDao;
import org.clopuccino.dao.UserComputerPropertiesDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.Computer;
import org.clopuccino.domain.Version;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * <code>ComputerService</code> provides computer-related services for servlets.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ComputerService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ComputerService.class.getSimpleName());

    private final ComputerDao computerDao;

    private final UserComputerPropertiesDao userComputerPropertiesDao;

    public static long pseudoComputerId() {
        // By day: -yyyyMMdd
        return Long.parseLong("-" + Constants.PSEUDO_COMPUTER_ID_DATE_FORMAT.format(new Date()));
    }

    public ComputerService(DatabaseAccess dbAccess) {
        DatabaseAccess localDbAccess;

        if (dbAccess == null) {
            localDbAccess = DatabaseUtility.createDatabaseAccess();
        } else {
            localDbAccess = dbAccess;
        }

        computerDao = new ComputerDao(localDbAccess);

        userComputerPropertiesDao = new UserComputerPropertiesDao(localDbAccess);
    }

    public String findComputerNamesByUserId(String userId) {

        String computerNames;

        List<Computer> computers = computerDao.findComputerByAdminId(userId);


        if (computers != null && computers.size() > 0) {
            StringBuilder builder = new StringBuilder();

            for (Computer computer : computers) {
                builder.append(computer.getComputerName());
                builder.append("\n");
            }

            computerNames = builder.toString();
        } else {
            computerNames = "";
        }

        return computerNames;
    }

    public String messageIfConnectedDesktopVersionSmallerThan(String desktopVersion, Long computerId, String userComputerId, String clientLocale) {
        String responseMessage = null;

        try {
            // validate desktop version
            String currentDesktopVersion = userComputerPropertiesDao.findPropertyValue(userComputerId, PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);
//            // find the owner of the computer
//            String computerOwner = computerDao.findComputerOwnerById(computerId);
//
//            String ownerUserComputerId;
//
//            if (computerOwner != null && computerOwner.trim().length() > 0) {
//                ownerUserComputerId = Utility.generateUserComputerIdFrom(computerOwner, computerId);
//            } else {
//                ownerUserComputerId = userComputerId;
//            }
//
//            String currentDesktopVersion = userComputerPropertiesDao.findPropertyValue(ownerUserComputerId, PropertyConstants.PROPERTY_NAME_FILELUG_DESKTOP_CURRENT_VERSION);

            if (new Version(currentDesktopVersion).compareTo(new Version(desktopVersion)) < 0) {
                String computerName = computerDao.findComputerNameByComputerId(computerId);

                if (computerName != null) {
                    responseMessage = ClopuccinoMessages.localizedMessage(clientLocale, "desktop.need.update2", computerName);
                } else {
                    responseMessage = ClopuccinoMessages.localizedMessage(clientLocale, "desktop.need.update3");
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to compare current desktop version with verswion: '" + desktopVersion + "' with computer: '" + computerId + "' and userComputer: '" + userComputerId + "'", e);
        }

        return responseMessage;
    }
}
