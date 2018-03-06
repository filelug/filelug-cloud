package org.clopuccino;

import ch.qos.logback.classic.Logger;
import org.clopuccino.dao.CountryDao;
import org.clopuccino.dao.TaskDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.Task;
import org.clopuccino.domain.TaskStatus;
import org.clopuccino.service.ConnectionDispatchService;
import org.clopuccino.service.sns.SnsMobilePushService;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class RepositoryScheduler {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("SCHEDULE");

//    private static boolean isAAServer() {
//        boolean result = false;
//
//        try {
//            InetAddress[] inetAddresses = InetAddress.getAllByName(Constants.FILELUG_HOSTNAME.toLowerCase());
//
//            InetAddress localInetAddress = InetAddress.getLocalHost();
//
//            String localInetAddressString = localInetAddress.toString().toLowerCase();
//
//            if (inetAddresses != null && inetAddresses.length > 0) {
//                LOGGER.info("AA Server Test Start for: " + localInetAddressString);
//
//                for (InetAddress theInetAddress : inetAddresses) {
//                    if (localInetAddressString.equals(theInetAddress.toString().toLowerCase())) {
//                        result = true;
//
//                        LOGGER.info("AA Server Test Passed for: " + localInetAddressString);
//
//                        break;
//                    }
//                }
//            }
//
//            if (!result) {
//                LOGGER.info("AA Server Test Not Passed for: " + localInetAddressString);
//            }
//        } catch (Exception e) {
//            result = false;
//
//            LOGGER.error("Failed to test if AA Server.\n" + e.getMessage(), e);
//        }
//
//        return result;
//    }

//    public static void startScheduleUpdateAvailableCountries(DatabaseAccess dbAccess) {
//        if (Utility.isAAServer()) {
//            try {
//                TaskDao taskDao = new TaskDao(dbAccess);
//
//                Task task = taskDao.findTaskById(Constants.TASK_NAME_UPDATE_AVAILABLE_COUNTRIES);
//
//                if (task != null) {
//                    ScheduledExecutorService updateAvailableCountriesScheduler = Utility.getScheduledExecutorService();
//
//                    updateAvailableCountriesScheduler.scheduleAtFixedRate(new Runnable() {
//                        @Override
//                        public void run() {
//                            // only AA server, and in production can do the task.
////                        if (isAAServer()) {
//                            TaskDao internalTaskDao = new TaskDao();
//
//                            Task internalTask = internalTaskDao.findTaskById(Constants.TASK_NAME_UPDATE_AVAILABLE_COUNTRIES);
//
//                            if (internalTask != null && !internalTask.getLatestTaskStatus().equals(TaskStatus.TASK_STATUS_PROCESS)) {
//                                LOGGER.info("Runnig schedule '" + Constants.TASK_NAME_UPDATE_AVAILABLE_COUNTRIES + "'");
//
//                                internalTask.setLatestTaskStatus(TaskStatus.TASK_STATUS_PROCESS);
//                                internalTask.setLatestTaskStartTimestamp(System.currentTimeMillis());
//                                internalTask.setLatestTaskEndTimestamp(0L);
//                                internalTask.setLatestTaskErrorMessage("");
//
//                                try {
//                                    // update task before executing
//                                    internalTaskDao.updateTask(internalTask);
//
//                                    CountryDao countryDao = new CountryDao();
//                                    countryDao.updateAvailableCountries();
//
//                                    // update task after executing successfully
//                                    internalTask.setLatestTaskStatus(TaskStatus.TASK_STATUS_SUCCESS);
//                                    internalTask.setLatestTaskEndTimestamp(System.currentTimeMillis());
//                                    internalTaskDao.updateTask(internalTask);
//
//                                    LOGGER.info("Done runnig schedule '" + Constants.TASK_NAME_UPDATE_AVAILABLE_COUNTRIES + "'");
//                                } catch (Exception e) {
//                                    // update task after executing successfully
//                                    internalTask.setLatestTaskStatus(TaskStatus.TASK_STATUS_FAILURE);
//                                    internalTask.setLatestTaskEndTimestamp(System.currentTimeMillis());
//                                    internalTask.setLatestTaskErrorMessage(e.getMessage());
//                                    internalTaskDao.updateTask(internalTask);
//
//                                    LOGGER.error("Failed to update available countries.\n" + e.getMessage(), e);
//                                }
//                            }
////                        }
//                        }
//                    }, task.getTaskInitialDelay(), task.getTaskInterval(), TimeUnit.SECONDS);
//
//                    LOGGER.info("Schedule '" + Constants.TASK_NAME_UPDATE_AVAILABLE_COUNTRIES + "' initialized");
//                }
////            } else {
////                LOGGER.debug("Don't have to execute schedule to update available countries for host name: '" + hostName + "' is not '" + Constants.FILELUG_HOSTNAME + "'");
////            }
////        } catch (UnknownHostException e) {
////            LOGGER.error("Failed to start schedule to update available countries for unknown host.\n" + e.getMessage(), e);
//            } catch (Exception e) {
//                LOGGER.error("Failed to start schedule to update available countries.\n" + e.getMessage(), e);
//            }
//        }
//    }

    public static void startScheduleUpdateAvailableLugServers(DatabaseAccess dbAccess) {
        if (Utility.isAAServer()) {
            try {
                TaskDao taskDao = new TaskDao(dbAccess);

                Task task = taskDao.findTaskById(Constants.TASK_NAME_UPDATE_AVAILABLE_LUG_SERVERS);

                if (task != null) {
                    ScheduledExecutorService updateAvailableLugServersScheduler = Utility.getScheduledExecutorService();

                    updateAvailableLugServersScheduler.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            TaskDao internalTaskDao = new TaskDao();

                            Task internalTask = internalTaskDao.findTaskById(Constants.TASK_NAME_UPDATE_AVAILABLE_LUG_SERVERS);

                            if (internalTask != null && !internalTask.getLatestTaskStatus().equals(TaskStatus.TASK_STATUS_PROCESS)) {
                                LOGGER.info("Runnig schedule '" + Constants.TASK_NAME_UPDATE_AVAILABLE_LUG_SERVERS + "'");

                                internalTask.setLatestTaskStatus(TaskStatus.TASK_STATUS_PROCESS);
                                internalTask.setLatestTaskStartTimestamp(System.currentTimeMillis());
                                internalTask.setLatestTaskEndTimestamp(0L);
                                internalTask.setLatestTaskErrorMessage("");

                                try {
                                    // update task before executing
                                    internalTaskDao.updateTask(internalTask);

                                    ConnectionDispatchService.updateAvailableLugServerIds();

                                    // update task after executing successfully
                                    internalTask.setLatestTaskStatus(TaskStatus.TASK_STATUS_SUCCESS);
                                    internalTask.setLatestTaskEndTimestamp(System.currentTimeMillis());
                                    internalTaskDao.updateTask(internalTask);

                                    LOGGER.info("Done runnig schedule '" + Constants.TASK_NAME_UPDATE_AVAILABLE_LUG_SERVERS + "'");
                                } catch (Exception e) {
                                    // update task after executing successfully
                                    internalTask.setLatestTaskStatus(TaskStatus.TASK_STATUS_FAILURE);
                                    internalTask.setLatestTaskEndTimestamp(System.currentTimeMillis());
                                    internalTask.setLatestTaskErrorMessage(e.getMessage());
                                    internalTaskDao.updateTask(internalTask);

                                    LOGGER.error("Failed to update available lug servers.\n" + e.getMessage(), e);
                                }
                            }
                        }
                    }, task.getTaskInitialDelay(), task.getTaskInterval(), TimeUnit.SECONDS);

                    LOGGER.info("Schedule '" + Constants.TASK_NAME_UPDATE_AVAILABLE_LUG_SERVERS + "' initialized");
                }
            } catch (Exception e) {
                LOGGER.error("Failed to start schedule " + Constants.TASK_NAME_UPDATE_AVAILABLE_LUG_SERVERS + ".\n" + e.getMessage(), e);
            }
        }
    }

    public static void startScheduleReloadSnsPlatformApplications(final DatabaseAccess dbAccess) {
        try {
            TaskDao taskDao = new TaskDao(dbAccess);

            Task task = taskDao.findTaskById(Constants.TASK_NAME_RELOAD_SNS_APPLICATIONS);

            if (task != null) {
                ScheduledExecutorService updateReloadSnsApplicationsScheduler = Utility.getScheduledExecutorService();

                updateReloadSnsApplicationsScheduler.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        // Ignore updating task status

                        try {
                            SnsMobilePushService pushService = new SnsMobilePushService(dbAccess);

                            pushService.reloadSnsPlatformApplications();

                            LOGGER.info("Done runnig schedule '" + Constants.TASK_NAME_RELOAD_SNS_APPLICATIONS + "'");
                        } catch (Exception e) {
                            LOGGER.error("Failed to run schedule '" + Constants.TASK_NAME_RELOAD_SNS_APPLICATIONS + "'.\n" + e.getMessage(), e);
                        }
                    }
                }, task.getTaskInitialDelay(), task.getTaskInterval(), TimeUnit.SECONDS);

                LOGGER.info("Schedule '" + Constants.TASK_NAME_RELOAD_SNS_APPLICATIONS + "' initialized");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to initialize schedule '" + Constants.TASK_NAME_RELOAD_SNS_APPLICATIONS + "'.\n" + e.getMessage(), e);
        }
    }

}
