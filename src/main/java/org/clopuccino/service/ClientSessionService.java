package org.clopuccino.service;

import ch.qos.logback.classic.Logger;
import org.apache.commons.lang3.time.DateUtils;
import org.clopuccino.Constants;
import org.clopuccino.Utility;
import org.clopuccino.dao.ClientSessionDao;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.domain.*;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <code>ClientSessionService</code> handles client sessions with database.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ClientSessionService {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("C_SESSION");

    private static ScheduledExecutorService deleteTimeoutClientSessionService;

    private static boolean stopDelete = false;

    private ClientSessionDao clientSessionDao;

    public ClientSessionService(DatabaseAccess dbAccess) {
        clientSessionDao = new ClientSessionDao(dbAccess);
    }

    public void appendClientSession(String sessionId, User user, UserComputer userComputer, String deviceToken) throws Exception {
        if (sessionId == null || user == null || userComputer == null) {
            throw new IllegalArgumentException("Null session id, user or user computer.");
        }

        String userComputerId = userComputer.getUserComputerId();

        Long computerId = userComputer.getComputerId();

        String userId = user.getAccount();

        if (userComputerId == null || computerId == null || userId == null) {
            throw new IllegalArgumentException("Null user computer id, computer id or user id.");
        }

        Boolean showHidden = user.getShowHidden();
        showHidden = showHidden != null ? showHidden : Boolean.FALSE;

        clientSessionDao.createClientSession(new ClientSession(computerId, userId, sessionId, deviceToken, System.currentTimeMillis(), showHidden, user.getLocale()));
    }

    public void appendClientSession(String sessionId, User user, long computerId, String userComputerId, String deviceToken) throws Exception {
        if (sessionId == null || user == null || userComputerId == null) {
            throw new IllegalArgumentException("Null session id, user or user computer id.");
        }

        String userId = user.getAccount();

        if (userId == null) {
            throw new IllegalArgumentException("Null user id.");
        }

        Boolean showHidden = user.getShowHidden();
        showHidden = showHidden != null ? showHidden : Boolean.FALSE;

        clientSessionDao.createClientSession(new ClientSession(computerId, userId, sessionId, deviceToken, System.currentTimeMillis(), showHidden, user.getLocale()));
    }

    public void appendClientSession(String sessionId, String userId, Boolean showHidden, String clientLocale, long computerId, String userComputerId, String deviceToken) throws Exception {
        if (sessionId == null || userId == null || userComputerId == null) {
            throw new IllegalArgumentException("Null session id, user id or user computer id.");
        }

        clientSessionDao.createClientSession(new ClientSession(computerId, userId, sessionId, deviceToken, System.currentTimeMillis(), showHidden != null ? showHidden : Boolean.FALSE, clientLocale));
    }

    public void removeClientSessionsByUser(String userId) {
        clientSessionDao.deleteClientSessionsByUser(userId);
    }

    public void removeClientSessionsByComputer(Long computerId) {
        clientSessionDao.deleteClientSessionsByComputer(computerId);
    }

    public static String generateUniqueSessionId(String userComputerId) throws Exception {
        return new IdGenerators(userComputerId).generateId(Constants.DEFAULT_CLIENT_SESSION_ID_BYTE_COUNT);
    }

    public static void deleteInvalidClientSessions(Integer timeoutInSeconds) throws Exception {
        if (timeoutInSeconds == null) {
            timeoutInSeconds = Constants.CLIENT_SESSION_TO_DELETE_IN_SECONDS;
        }

        Date currentDate =  new Date();
        Date baseDate = DateUtils.addSeconds(currentDate, -timeoutInSeconds);

        ClientSessionDao clientSessionDao = new ClientSessionDao();

        clientSessionDao.deleteClientSessionsByLastAccessTimeStampSmallerThan(baseDate.getTime());
    }

    public ClientSession findClientSessionBySessionId(String sessionId) throws Exception {
        return clientSessionDao.findClientSessionById(sessionId);
    }

    public boolean existingClientSessionBySessionId(String sessionId) {
        boolean exists = false;

        if (sessionId != null) {
            exists = clientSessionDao.existingClientSessionById(sessionId);
        }

        return exists;
    }

    public static void startDeletingTimeoutClientSessions(Integer initialDelayInSeconds, Integer periodInSeconds) {
        if (deleteTimeoutClientSessionService == null) {
            if (periodInSeconds == null || periodInSeconds < Constants.DEFAULT_DELETE_INVALID_CLIENT_SESSION_INTERVAL) {
                periodInSeconds = Constants.DEFAULT_DELETE_INVALID_CLIENT_SESSION_INTERVAL;
            }

            deleteTimeoutClientSessionService = Executors.newSingleThreadScheduledExecutor();
            deleteTimeoutClientSessionService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (!isStopDelete()) {
                        try {
                            deleteInvalidClientSessions(Constants.CLIENT_SESSION_TO_DELETE_IN_SECONDS);
                        } catch (Exception e) {
                            LOGGER.error("Error on processing timeout client session deletion!\n" + e.getMessage(), e);
                        }
                    }
                }
            }, initialDelayInSeconds, periodInSeconds, TimeUnit.SECONDS);
        }
    }

    public static void terminateDeleteTimeoutClientSessionService() {
        if (deleteTimeoutClientSessionService != null) {
            setStopDelete(true);

            Utility.shutdownAndAwaitTermination(deleteTimeoutClientSessionService, Constants.DEFAULT_AWAIT_TERMINATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        }
    }

    public static boolean isStopDelete() {
        return ClientSessionService.stopDelete;
    }

    public static void setStopDelete(boolean stop) {
        stopDelete = stop;
    }

    /**
     * Checks if the specified session id is valid. If not valid, the system remove the client session(if any) and return false;
     * if it is valid session id, return true.
     *
     * @param sessionId client session id
     * @param updateLastAccessTime Set to true if you want the session updates its last access time.
     * @return true if valid
     */
    public ClientSession findValidClientSessionById(String sessionId, boolean updateLastAccessTime) throws Exception {
        ClientSession clientSession = clientSessionDao.findClientSessionById(sessionId);

        if (clientSession != null && !clientSession.checkTimeout(Constants.DEFAULT_CLIENT_SESSION_IDLE_TIMEOUT_IN_SECONDS)) {
            if (updateLastAccessTime) {
                long newLastAccessTime = System.currentTimeMillis();

                clientSessionDao.updateClientSessionLastAccessTimestamp(sessionId, newLastAccessTime);

                clientSession.setLastAccessTime(newLastAccessTime);
            }

            return clientSession;
        } else {
            return null;
        }
    }

    public void updateClientSessionDeviceToken(String sessionId, String deviceToken) {
        clientSessionDao.updateClientSessionDeviceToken(sessionId, deviceToken);
    }

    public void updateClientSession(ClientSession clientSession) {
        clientSessionDao.updateClientSession(clientSession);
    }

    public void updateClientSessionLastAccessTimestamp(String sessionId, long lastAccessTimestamp) {
        if (sessionId != null) {
            clientSessionDao.updateClientSessionLastAccessTimestamp(sessionId, lastAccessTimestamp);
        }
    }

    /**
     * Removes all client session informations
     */
    public void resetClientSessions() {
        clientSessionDao.truncateTable();
    }

    /**
     * Prepares a new ClientSession from the attributes of the specified HttpServletRequest.
     * The attributes of the request are set in filter <code>ClientSessionFilter</code>.
     *
     * @param request The specified HttpServletRequest to get the information of ClientSession
     * @return A new ClientSession, must not null. If ClientSession is null, exception throws.
     * @throws Exception if ClientSession not found.
     */
    public ClientSession prepareClientSessionFrom(HttpServletRequest request) throws Exception {
        ClientSession clientSession = (ClientSession) request.getAttribute("clientSession");

        if (clientSession == null) {
            throw new Exception("Servlet should validate session first.");
        } else {
            return clientSession;
        }
    }

    /**
     * All the records that contain the sessionId as the value of column 'replaced by' (level 1 records) will be deleted too.
     * And all the records that contain the sessionId in the "level 1 records" as the value of column 'replaced by' (level 2 records) will be deleted too.
     * And the level 2, 3, until no referenced by the column 'replaced by'.
     *
     * @param sessionId The id of the session table
     */
    public void removeClientSessionsBySessionId(String sessionId) throws Exception {
        Stack<String> sessionStack = new Stack<>();

        sessionStack.push(sessionId);

        List<String> replacedBySessionIds = clientSessionDao.findClientSessionIdByReplacedBy(sessionId);

        if (replacedBySessionIds != null && replacedBySessionIds.size() > 0) {
            for (String newSessionId : replacedBySessionIds) {
                sessionStack.push(newSessionId);

                // DEBUG
//                LOGGER.info(String.format("Sack pushed main session '%s'", newSessionId));
            }
        }

        removeClientSessionsBySessionId(sessionStack, replacedBySessionIds);
    }

    // size of the sessionStack must be equal or larger than 1.
    private void removeClientSessionsBySessionId(Stack<String> sessionStack, List<String> latestAddedSessionIds) throws Exception {
        if (sessionStack.size() > 0) {
            if (latestAddedSessionIds == null || latestAddedSessionIds.size() < 1) {
                // start to delete with sequence
                for (; !sessionStack.empty(); ) {
                    String sessionId = sessionStack.pop();

                    // DEBUG
//                    LOGGER.info(String.format("Sack poped session '%s'", sessionId));

                    clientSessionDao.deleteClientSessionsBySessionId(sessionId);

                    // DEBUG
//                    LOGGER.info(String.format("Session deleted with session id '%s'", sessionId));
                }
            } else {
                List<String> replacedBySessionIdsWithSameLevel = new ArrayList<>();

                for (String latestSessionId : latestAddedSessionIds) {
                    List<String> subReplacedBySessionIds = clientSessionDao.findClientSessionIdByReplacedBy(latestSessionId);

                    if (subReplacedBySessionIds != null && subReplacedBySessionIds.size() > 0) {
                        replacedBySessionIdsWithSameLevel.addAll(subReplacedBySessionIds);

                        for (String newSessionId : subReplacedBySessionIds) {
                            sessionStack.push(newSessionId);

                            // DEBUG
//                            LOGGER.info(String.format("Sack pushed sub session '%s'", newSessionId));
                        }
                    }
                }

                removeClientSessionsBySessionId(sessionStack, replacedBySessionIdsWithSameLevel);
            }
        }
    }

    public void updateClientSessionReplacedBy(String newSessionId, String oldSessionId) {
        clientSessionDao.updateClientSessionReplacedBy(newSessionId, oldSessionId);
    }
}
