package org.clopuccino.service;

import org.clopuccino.Constants;
import org.clopuccino.DatabaseUtility;
import org.clopuccino.Utility;
import org.clopuccino.dao.FilelugPropertiesDao;
import org.clopuccino.dao.UserComputerDao;
import org.clopuccino.domain.UserComputer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <code>ConnectionDispatchService</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ConnectionDispatchService {

    private static List<String> lugServerDomainNames;

//    private static String[] LUG_SERVER_IDS = new String[] {"repo"};
//    private static final String[] LUG_SERVER_IDS = new String[] {"repo1", "repo2"};
//
//    private static final int LUG_SERVER_SIZE = LUG_SERVER_IDS.length;

    private static int lastSelectedLugServerIdIndex = 0;

//    private static String lastUserComputerId;

    private static UserComputerDao userComputerDao;

    private static BaseService pingServerService;

//    /**
//     * Set the lug server domain name to the next one.
//     * To prevent certain one lug server shut down suddenly,
//     * change the lug server domain name even when the last user computer id is the same with the current one.
//     */
//    public static void nextLugServerIdIndex(String userComputerId) {
//        if (lugServerDomainNames != null && lugServerDomainNames.size() > lastSelectedLugServerIdIndex + 1) {
//            lastSelectedLugServerIdIndex++;
//        } else {
//            lastSelectedLugServerIdIndex = 0;
//        }
//    }

    /**
     * Set the lug server domain name to the next one.
     */
    public static void nextLugServerIdIndex() {
        if (lugServerDomainNames != null && lugServerDomainNames.size() > lastSelectedLugServerIdIndex + 1) {
            lastSelectedLugServerIdIndex++;
        } else {
            lastSelectedLugServerIdIndex = 0;
        }
    }

    /**
     * The developer should not use <code>lugServerDomainNames.get(lastSelectedLugServerIdIndex)</code> directly,
     * but use this method instead to get lug server id safely.
     */
    public static String getLastSelectedLugServerId(int lugServerIdIndex) {
        String lugServerId;

        if (lugServerDomainNames != null && lugServerDomainNames.size() > 0) {
            if (lugServerDomainNames.size() > lugServerIdIndex) {
                lugServerId = lugServerDomainNames.get(lugServerIdIndex);
            } else {
                lugServerId = lugServerDomainNames.get(0);
            }
        } else {
            lugServerId = Constants.AA_SERVER_ID_AS_LUG_SERVER;
        }

        return lugServerId;
    }

//    /**
//     * Set the lug server domain name to the next one.
//     * Changed only when the last user computer id is not the same with the current one.
//     */
//    public static void nextLugServerIdIndex(String userComputerId) {
//        if (lastUserComputerId == null || !lastUserComputerId.equals(userComputerId)) {
//            if (lugServerDomainNames != null && lugServerDomainNames.size() > lastSelectedLugServerIdIndex + 1) {
//                lastSelectedLugServerIdIndex++;
//            } else {
//                lastSelectedLugServerIdIndex = 0;
//            }
//
//            lastUserComputerId = userComputerId;
//        }
//    }

    /**
     * Given the current lug server id is the current-used lug server id of the user computer.
     * Given the last lug server id is the the lug server id that will be used for the next newly created user computer.
     *
     * Rules of dispatch are:
     * <ul>
     *     <li>
     *         If the current lug server id of this user computer is still alive, returns the current lug server id.
     *     </li>
     *     <li>
     *         If the current lug server id, say A, of this user computer is not alive, invoke <code>nextLugServerIdIndex()</code>
     *         until the last lug server is not the same with the current lug server id and then return the last lug server id.
     *     </li>
     *     <li>
     *         If the current lug server id of this user computer is empty, or even no such user computer,
     *         invoke <code>nextLugServerIdIndex()</code>, and return the last lug server id.
     *     </li>
     * </ul>
     */
    public static String dispatchConnectionBy(String userId, Long computerId) {
        String theDomainName;

        if (pingServerService == null) {
            pingServerService = new BaseService(Constants.SOCKET_TIMEOUT_TO_PING_LUG_SERVER, Constants.CONNECT_TIMEOUT_TO_PING_LUG_SERVER);
        }

        // Routing only if SSL supported. If not SSL, always use aa as the subdomain name
        if (pingServerService.getRepositoryUseHttps()) {
            if (lugServerDomainNames == null || lugServerDomainNames.size() < 1) {
                updateAvailableLugServerIds();
            }

            if (userComputerDao == null) {
                userComputerDao = new UserComputerDao(DatabaseUtility.createDatabaseAccess());
            }

            String userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);

            UserComputer userComputer = userComputerDao.findUserComputerById(userComputerId);

            String lastSelectedLugServerId = getLastSelectedLugServerId(lastSelectedLugServerIdIndex);

            if (userComputer != null) {
                String currentLugServerId = userComputer.getLugServerId();

                if (currentLugServerId == null || currentLugServerId.trim().length() < 1) {
                    theDomainName = lastSelectedLugServerId;

                    nextLugServerIdIndex();
                } else if (pingServerService.pingLugServer(currentLugServerId)) { // current lug server is alive
                    theDomainName = currentLugServerId;
                } else { // current lug server not alive
                    boolean theSameOne = currentLugServerId.equals(lastSelectedLugServerId);

                    if (theSameOne) {
                        // choose another one

                        int maxTestingCount = lugServerDomainNames.size();

                        for (int testCount = 1; (theSameOne && testCount < maxTestingCount); testCount++) {
                            nextLugServerIdIndex();

                            lastSelectedLugServerId = getLastSelectedLugServerId(lastSelectedLugServerIdIndex);

                            theSameOne = currentLugServerId.equals(lastSelectedLugServerId);
                        }
                    }

                    theDomainName = lastSelectedLugServerId;
                }
            } else {
                nextLugServerIdIndex();

                theDomainName = lastSelectedLugServerId;
            }
        } else {
            theDomainName = Constants.AA_SERVER_ID_AS_LUG_SERVER;
        }

        return theDomainName;
    }

//    public static String dispatchConnectionBy(String userId, Long computerId) {
//        String theDomainName;
//
//        BaseService baseService = new BaseService();
//
//        // Routing only if SSL supported. If not SSL, always use aa as the subdomain name
//        if (baseService.getRepositoryUseHttps()) {
//            if (lugServerDomainNames == null || lugServerDomainNames.size() < 1) {
//                updateAvailableLugServerIds();
//            }
//
//            if (lugServerDomainNames != null && lugServerDomainNames.size() > 0 && lugServerDomainNames.size() > lastSelectedLugServerIdIndex) {
//                theDomainName = lugServerDomainNames.get(lastSelectedLugServerIdIndex);
//            } else if (lugServerDomainNames != null && lugServerDomainNames.size() > 0) {
//                theDomainName = lugServerDomainNames.get(0);
//            } else {
//                theDomainName = Constants.FILELUG_AA_SERVER_HOSTNAME;
//            }
//        } else {
//            theDomainName = Constants.AA_SERVER_ID_AS_LUG_SERVER;
//        }
//
//        return theDomainName;
//    }

    public static void updateAvailableLugServerIds() {
        FilelugPropertiesDao filelugPropertiesDao = new FilelugPropertiesDao();

        String lugServerNames = filelugPropertiesDao.findValueByKey(Constants.FILELUG_PROPERTY_KEY_LUG_SERVER_DOMAIN_NAMES);

        StringTokenizer tokenizer = new StringTokenizer(lugServerNames, Constants.FILELUG_PROPERTY_VALUE_LUG_SERVER_DOMAIN_NAMES_DELIMITERS);

        List<String> lugServerNameList = Collections.synchronizedList(new ArrayList<String>());

        for (; tokenizer.hasMoreTokens(); ) {
            lugServerNameList.add(tokenizer.nextToken().trim());
        }

        lugServerDomainNames = lugServerNameList;
    }
}
