package org.clopuccino;

import ch.qos.logback.classic.Logger;
import org.clopuccino.dao.*;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.server.servlet.websocket.ConnectSocket;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * <code>RepositoryContextListener</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@WebListener
public class RepositoryContextListener implements ServletContextListener {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("INIT");

    private DatabaseAccess dbAccess;

    public RepositoryContextListener() {
        dbAccess = DatabaseUtility.createDatabaseAccess();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
//        /* DEBUG */
//        LOGGER.info("user.dir=" + System.getProperty("user.dir"));

        /* load all client(ios,android)-to-server(java) locales mapping */
        ClopuccinoMessages.loadLocales();

        try {
            dbAccess.initDatabase();

            /* Table: filelug_properties */

            FilelugPropertiesDao filelugPropertiesDao = new FilelugPropertiesDao(dbAccess);

            boolean shouldCreateDefaultFilelugProperties = !filelugPropertiesDao.isFilelugPropertiesTableExists();

            if (!filelugPropertiesDao.createTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_FILELUG_PROPERTIES);
            }

            if (shouldCreateDefaultFilelugProperties) {
                if (!filelugPropertiesDao.recreateDefaultFilelugProperties()) {
                    throw new SQLException("Failed to re-create default filelug properties");
                }
            }

            /* Table: computer_data */

            ComputerDataDao computerDataDao = new ComputerDataDao(dbAccess);

            if (!computerDataDao.createTableIfNotExists()) {
                throw new SQLException("Failed to cretae table " + DatabaseConstants.TABLE_NAME_COMPUTER_DATA);
            }

            /* Table: country */

            CountryDao countryDao = new CountryDao(dbAccess);

            boolean shouldCreateDefaultCountries = !countryDao.isTableExists();

            if (!countryDao.createTableIfNotExists()) {
                throw new SQLException("Failed to cretae table " + DatabaseConstants.TABLE_NAME_COUNTRY);
            }

            if (shouldCreateDefaultCountries) {
                if (!countryDao.recreateDefaultCountries()) {
                    throw new SQLException("Failed to re-create default countries");
                }
            }

            /* Table: auth_user */

            UserDao userDao = new UserDao(dbAccess);

            if (!userDao.createTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_AUTH_USER);
            }

            // Table: sns_application

            SnsApplicationDao snsApplicationDao = new SnsApplicationDao(dbAccess);

            if (!snsApplicationDao.createTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_SNS_APPLICATION);
            }

            /* Table: cl_computer */

            ComputerDao computerDao = new ComputerDao(dbAccess);

            if (!computerDao.createTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_COMPUTER);
            }

            /* Table: user_computer */

            UserComputerDao userComputerDao = new UserComputerDao(dbAccess);

            if (!userComputerDao.createTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_USER_COMPUTER);
            }

            /* Table: user_computer_properties */

            UserComputerPropertiesDao userComputerPropertiesDao = new UserComputerPropertiesDao(dbAccess);

            if (!userComputerPropertiesDao.createTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_USER_COMPUTER_PROPERTIES);
            }

            /* Table: client_session */

            ClientSessionDao clientSessionDao = new ClientSessionDao(dbAccess);

            if (!clientSessionDao.createTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_CLIENT_SESSION);
            }

            /* Table: sms_notification */

            SMSNotificationDao smsNotificationDao = new SMSNotificationDao(dbAccess);

            if (!smsNotificationDao.createTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_SMS_NOTIFICATION);
            }

            /* Table: apply_connection */
            ApplyConnectionDao applyConnectionDao = new ApplyConnectionDao(dbAccess);

            if (!applyConnectionDao.createTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_APPLY_CONNECTION);
            }

            // Table: file_upload_group

            FileUploadGroupDao fileUploadGroupDao = new FileUploadGroupDao(dbAccess);

            if (!fileUploadGroupDao.createMasterTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_FILE_UPLOAD_GROUP);
            }

           // Table: file_upload_group_detail

            if (!fileUploadGroupDao.createDetailTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_FILE_UPLOAD_GROUP_DETAIL);
            }

            // Table: file_uploaded

            FileUploadDao fileUploadDao = new FileUploadDao(dbAccess);

            if (!fileUploadDao.createTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_FILE_UPLOADED);
            }

            /* Table: file_download_group */

            FileDownloadGroupDao fileDownloadGroupDao = new FileDownloadGroupDao(dbAccess);

            if (!fileDownloadGroupDao.createMasterTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_FILE_DOWNLOAD_GROUP);
            }

            /* Table: file_download_group_detail */

            if (!fileDownloadGroupDao.createDetailTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_FILE_DOWNLOAD_GROUP_DETAIL);
            }

            /* Table: file_downloaded */

            FileDownloadDao fileDownloadDao = new FileDownloadDao(dbAccess);

            if (!fileDownloadDao.createTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_FILE_DOWNLOADED);
            }

            /* Table: account_kit */
            AccountKitDao accountKitDao = new AccountKitDao(dbAccess);

            if (!accountKitDao.createTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_ACCOUNT_KIT);
            }

            /* Table: product_detail & product */

            ProductDao productDao = new ProductDao(dbAccess);

            boolean shouldCreateDefaultProducts = !productDao.isProductTableExists();

            if (!productDao.createTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_PRODUCT + " and " + DatabaseConstants.TABLE_NAME_PRODUCT_DETAIL);
            }

            if (shouldCreateDefaultProducts) {
                if (!productDao.createDefaultProductsIfNotExists()) {
                    throw new SQLException("Failed to create or update default products");
                }
            }

            /* Table: purchase */

            PurchaseDao purchaseDao = new PurchaseDao(dbAccess);

            if (!purchaseDao.createTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_PURCHASE);
            }

            // Table: task

            TaskDao taskDao = new TaskDao(dbAccess);

            boolean shouldCreateDefaultTasks = !taskDao.isTableExists();

            if (!taskDao.createTableIfNotExists()) {
                throw new SQLException("Failed to cretae table " + DatabaseConstants.TABLE_NAME_TASK);
            }

            if (shouldCreateDefaultTasks) {
                if (!taskDao.recreateDefaultTasks()) {
                    throw new SQLException("Failed to re-create default tasks");
                }
            }

            // Table: device_token

            DeviceTokenDao deviceTokenDao = new DeviceTokenDao(dbAccess);

            if (!deviceTokenDao.createTableIfNotExists()) {
                throw new SQLException("Failed to create table " + DatabaseConstants.TABLE_NAME_DEVICE_TOKEN);
            }

            // DEBUG: REMOVE IN PRODUCTION
//            AmazonSNSClient client = new PlatformApplicationFactory().prepareAmazonSnsClient();
//            PlatformApplicationFactory.deleteAllPlatformApplications(client);

            /* do not start this schedule */
//            ConnectSocket.startDeletingInvalidConnectSockets(Constants.INIT_DELAY_DELETE_TIMEOUT_CONNECT_SOCKET_IN_SECONDS, Constants.DEFAULT_DELETE_INVALID_CONNECT_SOCKET_INTERVAL);

            /* do not start this schedule */
//            if (Utility.isAAServer()) {
//                ClientSessionService.startDeletingTimeoutClientSessions(Constants.INIT_DELAY_DELETE_TIMEOUT_CLIENT_SESSION_IN_SECONDS, Constants.DEFAULT_DELETE_INVALID_CLIENT_SESSION_INTERVAL);
//            }

            /* don't have to start this schedule - only AA server start this schedule */
//            DatabaseUtility.startDeleteUnverifiedUsers(Constants.INIT_DELAY_DELETE_TIMEOUT_UNVERIFIED_USER_IN_SECONDS, Constants.DEFAULT_DELETE_TIMEOUT_UNVERIFIED_USER_INTERVAL);

            /* only AA server start this schedule */
//            DatabaseUtility.startFailingTimeoutTransfers(Constants.INIT_DELAY_DELETE_TIMEOUT_FILE_TRANSFER_IN_SECONDS, Constants.FILE_TRANSFER_TIME_OUT_STATUS_UPDATE_TIME_INTERVAL_IN_SECONDS);

//            // start schedule for update available countries
//            RepositoryScheduler.startScheduleUpdateAvailableCountries(dbAccess);

            // start schedule for update available lug servers
            RepositoryScheduler.startScheduleUpdateAvailableLugServers(dbAccess);

            // start schedule for reload sns application
            RepositoryScheduler.startScheduleReloadSnsPlatformApplications(dbAccess);

            /* TODO: send email for start repository successfully! */
        } catch (Exception e) {
            LOGGER.error("Error on initializing database!\n" + e.getClass().getName() + "\n" + e.getMessage(), e);

            /* TODO: send email for failure to start repository! */
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        /* Close scheduled executions */
        try {
//            DatabaseUtility.terminateFailTimeoutTransferService();

            DatabaseUtility.terminateDeleteTimeoutUnverifiedUserService();

//            ClientSessionService.terminateDeleteTimeoutClientSessionService();
//
//            ConnectSocket.terminateDeleteInvalidConnectSocketService();
        } finally {
            try {
                Utility.shutdownAndAwaitTermination(Utility.getScheduledExecutorService(), Constants.DEFAULT_AWAIT_TERMINATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            } catch (Exception e) {
                // ignored
            }

            try {
                Utility.shutdownAndAwaitTermination(Utility.getExecutorService(), Constants.DEFAULT_AWAIT_TERMINATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            } catch (Exception e) {
                // ignored
            }
        }

        /* Close all sockets to desktops */
        try {
            ConnectSocket.closeAllConnectSockets();
        } catch (Exception e) {
            System.err.println("Error on closing connect sockets before server closed.\n" + e.getClass().getName() + "\n" + e.getMessage());

            e.printStackTrace();
        }

        /* Close data source */
        try {

            /* for remote db server -- e.g. postgresql */
            dbAccess.closeDataSource(false);

            /* for embedded sql -- e.g. hsqldb */
//            dbAccess.closeDataSource(true);
        } catch (Exception e) {
            System.err.println("Error on closing database!\n" + e.getClass().getName() + "\n" + e.getMessage());

            e.printStackTrace();
        }
    }
}
