package org.clopuccino.service.sns;

import ch.qos.logback.classic.Logger;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.*;
import org.clopuccino.domain.DeviceToken;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <code>AbstractEndpointArn</code> prepares endpoint ARN for different platform applications
 *
 * @author masonhsieh
 * @version 1.0
 */
public abstract class AbstractEndpointArn {

    private static Hashtable<String, String> snsApplications;

//    private static Hashtable<String, PlatformApplication> platformApplications;

    protected String applicationName;

    protected DeviceToken.NotificationType notificationType;

    protected String token;

    protected String endpointArn;

//    public static void loadSnsApplicationsFromDb(boolean forceReload) {
//        if (snsApplications == null || forceReload) {
//            SnsApplicationDao snsApplicationDao = new SnsApplicationDao();
//
//            List<SnsApplication> allSnsApplications = snsApplicationDao.findAllSnsApplications();
//
//            if (allSnsApplications != null) {
//                snsApplications = new Hashtable<>();
//
//                for (SnsApplication snsApplication : allSnsApplications) {
//                    // platform should be the type in NotificationType
//
//                    snsApplications.put(snsApplication.getPlatform(), snsApplication.getApplicationArn());
//                }
//            }
//        }
//    }

    public static Hashtable<String, String> getPlatformApplications() {
        if (snsApplications == null) {
            snsApplications = new Hashtable<>();
//            loadSnsApplicationsFromDb(false);
        }

        return snsApplications;
    }

    // Return may be null
    public static String getPlatformApplicationArnByType(DeviceToken.NotificationType type) {
        return getPlatformApplications().get(type.name());
    }

    /**
     * Puts to memory.
     * Does nothing if data in db have not loaded from DB.
     */
    public static void createOrUpdateApplicationArnByType(String type, String newApplicationArn) {
        if (snsApplications != null) {
            snsApplications.put(type, newApplicationArn);
        }
    }

    /**
     * Deletes value in memory for the specified type.
     */
    public static void deleteApplicationByType(String type) {
        if (type != null) {
            snsApplications.remove(type);
        }
    }

//    public static SnsApplication getPlatformApplicationByType(DeviceToken.NotificationType type) {
//        return getPlatformApplications().get(type.name());
//    }

//    public static void preparePlatformApplications(AmazonSNSClient client, boolean updateIfExists) {
//        if (platformApplications == null || updateIfExists) {
//            platformApplications = PlatformApplicationFactory.preparePlatformApplications(client);
//        }
//    }
//
//    public static Hashtable<String, PlatformApplication> getPlatformApplications() {
//        if (platformApplications == null) {
//            // if occurred, invoke preparePlatformApplications(AmazonSNSClient, boolean) first
//
//            throw new RuntimeException("Platform applications not prepared. You should prepare platform applications first.");
//        }
//
//        return platformApplications;
//    }
//
//    // Return may be null
//    public static PlatformApplication getPlatformApplicationByType(DeviceToken.NotificationType type) {
//        return getPlatformApplications().get(type.name());
//    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getEndpointArn() {
        renewEndpointArnIfNeeded();

        return endpointArn;
    }

    public void setEndpointArn(String endpointArn) {
        this.endpointArn = endpointArn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public DeviceToken.NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(DeviceToken.NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public abstract AmazonSNSClient getClient();

    protected abstract String preparePlatformApplicationArn();

    abstract protected Logger getLogger();

    /**
     * Everytime you invoke getEndpointArn(), this method invoked to
     * create new, update existing one, or use the existing one.
     */
    private void renewEndpointArnIfNeeded() {
        boolean updateNeeded = false;
        boolean createNeeded = (null == endpointArn);

        if (createNeeded) {
            // No endpoint ARN is stored; need to call CreateEndpoint
            endpointArn = createEndpoint();
            createNeeded = false;
        }

        getLogger().debug("Retrieving endpoint data...");
        // Look up the endpoint and make sure the data in it is current, even if
        // it was just created
        try {
            GetEndpointAttributesRequest geaReq = new GetEndpointAttributesRequest().withEndpointArn(endpointArn);
            GetEndpointAttributesResult geaRes = getClient().getEndpointAttributes(geaReq);

            updateNeeded = !geaRes.getAttributes().get("Token").equals(token) || !geaRes.getAttributes().get("Enabled").equalsIgnoreCase("true");
        } catch (NotFoundException nfe) {
            // we had a stored ARN, but the endpoint associated with it
            // disappeared. Recreate it.
            createNeeded = true;
        } catch (Exception e) {
            getLogger().error("Error on getting endpoint.\n" + e.getMessage(), e);
        }

        if (createNeeded) {
            createEndpoint();
        }

        getLogger().debug("updateNeeded=" + updateNeeded);

        if (updateNeeded) {
            // endpoint is out of sync with the current data;
            // update the token and enable it.
            getLogger().debug("Updating endpoint " + endpointArn);
            Map<String, String> attribs = new HashMap<>();
            attribs.put("Token", token);
            attribs.put("Enabled", "true");

            try {
                SetEndpointAttributesRequest saeReq = new SetEndpointAttributesRequest().withEndpointArn(endpointArn).withAttributes(attribs);
                getClient().setEndpointAttributes(saeReq);
            } catch (Exception e) {
                getLogger().error("Error on updating endpoint attributes.\n" + e.getMessage(), e);
            }
        }
    }

    /**
     * @return null if exception occurred.
     */
    private String createEndpoint() {
        String endpointArn = null;

        try {
            getLogger().debug("Creating endpoint with token " + token);

            String platformApplicationArn = preparePlatformApplicationArn();

            CreatePlatformEndpointRequest cpeReq = new CreatePlatformEndpointRequest().withPlatformApplicationArn(platformApplicationArn).withToken(token);
            CreatePlatformEndpointResult cpeRes = getClient().createPlatformEndpoint(cpeReq);
            endpointArn = cpeRes.getEndpointArn();
        } catch (InvalidParameterException ipe) {
            String message = ipe.getErrorMessage();

            getLogger().debug("Exception on creating endpoint: " + message);

            Pattern p = Pattern.compile(".*Endpoint (arn:aws:sns[^ ]+) already exists with the same Token.*");
            Matcher m = p.matcher(message);
            if (m.matches()) {
                // the endpoint already exists for this token, but with additional custom data that
                // CreateEndpoint doesn't want to overwrite. Just use the existing endpoint.
                endpointArn = m.group(1);
            } else {
                // rethrow exception, the input is actually bad
                getLogger().error("Error on creating endpoint.\n" + message, ipe);
            }
        } catch (Exception e) {
            getLogger().error("Error on creating endpoint.\n" + e.getMessage(), e);
        }

        setEndpointArn(endpointArn);

        return endpointArn;
    }
}
