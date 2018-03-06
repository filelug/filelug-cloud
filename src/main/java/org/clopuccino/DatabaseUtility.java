package org.clopuccino;

import ch.qos.logback.classic.Logger;
import org.clopuccino.dao.FileDownloadDao;
import org.clopuccino.dao.FileUploadDao;
import org.clopuccino.dao.UserDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.PostgresqlDatabaseAccess;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class DatabaseUtility {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DatabaseUtility.class.getSimpleName());

//    private static ScheduledExecutorService failTimeoutTransferService;

    private static ScheduledExecutorService deleteTimeoutUnverifiedUserService;

//    private static boolean stopFailingTimeoutTransfer = false;

    private static boolean stopDeletingTimeoutUnverifiedUser = false;

    private DatabaseAccess dbAccess;

    public DatabaseUtility() {
        dbAccess = DatabaseUtility.createDatabaseAccess();
    }

    public static DatabaseAccess createDatabaseAccess() {
        return new PostgresqlDatabaseAccess();
    }

//    public static boolean isStopFailingTimeoutTransfer() {
//        return DatabaseUtility.stopFailingTimeoutTransfer;
//    }

//    public static void setStopFailingTimeoutTransfer(boolean stop) {
////        boolean oldValue = isStopFailingTimeoutTransfer();
//
//        stopFailingTimeoutTransfer = stop;
//
////        /* re-initiate failing timeout transfer process */
////        if (oldValue && !stop) {
////            startFailingTimeoutTransfers(Constants.FILE_TRANSFER_TIME_OUT_STATUS_UPDATE_TIME_INTERVAL_IN_SECONDS);
////        }
//    }

    public static boolean isStopDeletingTimeoutUnverifiedUser() {
        return stopDeletingTimeoutUnverifiedUser;
    }

    public static void setStopDeletingTimeoutUnverifiedUser(boolean stop) {
//        boolean oldValue = isStopDeletingTimeoutUnverifiedUser();

        stopDeletingTimeoutUnverifiedUser = stop;

        // This will invoke another schedule executor
//        /* re-initiate deleting timout-unverified user process */
//        if (oldValue && !stop) {
//            startDeleteUnverifiedUsers(Constants.DEFAULT_DELETE_TIMEOUT_UNVERIFIED_USER_INTERVAL);
//        }
    }

//    public static void startFailingTimeoutTransfers(Integer initialDelayInSeconds, Integer periodInSeconds) {
//        if (failTimeoutTransferService == null) {
//            if (periodInSeconds == null || periodInSeconds < Constants.FILE_TRANSFER_TIME_OUT_STATUS_UPDATE_TIME_INTERVAL_IN_SECONDS) {
//                periodInSeconds = Constants.FILE_TRANSFER_TIME_OUT_STATUS_UPDATE_TIME_INTERVAL_IN_SECONDS;
//            }
//
//            failTimeoutTransferService = Executors.newSingleThreadScheduledExecutor();
//            failTimeoutTransferService.scheduleAtFixedRate(new Runnable() {
//                @Override
//                public void run() {
//                    if (!stopFailingTimeoutTransfer) {
//                        try {
//                            final long timeoutTimestamp = System.currentTimeMillis() - (Constants.DEFAULT_TRANSFER_FILE_CONTENT_TIME_OUT_IN_SECONDS * 1000);
//
//                            failingTimeoutTransfers(timeoutTimestamp);
//                        } catch (Exception e) {
//                            LOGGER.error("Error on processing timeout transfer deletion!\n" + e.getMessage(), e);
//                        }
//                    }
//                }
//            }, initialDelayInSeconds, periodInSeconds, TimeUnit.SECONDS);
//        }
//    }

//    private static void failingTimeoutTransfers(long timeoutTimestamp) {
//        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();
//
//        FileDownloadDao fileDownloadDao = new FileDownloadDao(dbAccess);
//        fileDownloadDao.updateStatusProcessingToFailureForTimeoutStartTimestamp(timeoutTimestamp);
//
//        FileUploadDao fileUploadDao = new FileUploadDao(dbAccess);
//        fileUploadDao.updateStatusProcessingToFailureForTimeoutStartTimestamp(timeoutTimestamp);
//    }

//    public static void terminateFailTimeoutTransferService() {
//        if (failTimeoutTransferService != null) {
//            setStopDeletingTimeoutUnverifiedUser(true);
//
//            Utility.shutdownAndAwaitTermination(failTimeoutTransferService, Constants.DEFAULT_AWAIT_TERMINATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
//        }
//    }

    /**
     * @return 「user table的可用傳輸量」-「file download table中status=processing的預期檔案大小總和」-「file upload table中status=processing的預期檔案大小總和」
     */
    public long findAvailableTransferBytesForUser(String userId) {
        UserDao userDao = new UserDao(dbAccess);
        long availableBytes = userDao.findAvailableTransferBytesForUser(userId);

        FileUploadDao fileUploadDao = new FileUploadDao(dbAccess);
        long uploadingFileSizeSum = fileUploadDao.sumUploadingFileSizeForUser(userId);

        FileDownloadDao fileDownloadDao = new FileDownloadDao(dbAccess);
        long downloadingFileSizeSum = fileDownloadDao.sumDownloadingFileSizeForUser(userId);

        return BigDecimal.valueOf(availableBytes).subtract(BigDecimal.valueOf(uploadingFileSizeSum)).subtract(BigDecimal.valueOf(downloadingFileSizeSum)).longValue();
//        return availableBytes - uploadingFileSizeSum - downloadingFileSizeSum;
    }

    /**
     * Start deleting timeout unverified users for specified interval in seconds.
     */
    public static void startDeleteUnverifiedUsers(Integer initialDelayInSeconds, Integer periodInSeconds) {
        if (deleteTimeoutUnverifiedUserService == null) {
            if (periodInSeconds == null || periodInSeconds < Constants.DEFAULT_DELETE_TIMEOUT_UNVERIFIED_USER_INTERVAL) {
                periodInSeconds = Constants.DEFAULT_DELETE_TIMEOUT_UNVERIFIED_USER_INTERVAL;
            }

            deleteTimeoutUnverifiedUserService = Executors.newSingleThreadScheduledExecutor();

            deleteTimeoutUnverifiedUserService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (!isStopDeletingTimeoutUnverifiedUser()) {
                        try {
                            deleteTimeoutUnverifiedUsers();
                        } catch (Exception e) {
                            LOGGER.error("Error on processing timeout unverified user deletion!\n" + e.getMessage(), e);
                        }
                    }
                }
            }, initialDelayInSeconds, periodInSeconds, TimeUnit.SECONDS);
        }
    }

    private static void deleteTimeoutUnverifiedUsers() {
        DatabaseAccess dbAccess = DatabaseUtility.createDatabaseAccess();

        UserDao userDao = new UserDao(dbAccess);

        List<String> userIds = userDao.findTimeoutUnverifiedUsers();

        if (userIds != null && userIds.size() > 0) {
            for (String userId : userIds) {
                userDao.deleteUnverifiedUserById(userId);
            }
        }
    }

    public static void terminateDeleteTimeoutUnverifiedUserService() {
        if (deleteTimeoutUnverifiedUserService != null) {
            setStopDeletingTimeoutUnverifiedUser(true);

            Utility.shutdownAndAwaitTermination(deleteTimeoutUnverifiedUserService, Constants.DEFAULT_AWAIT_TERMINATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        }
    }

}
