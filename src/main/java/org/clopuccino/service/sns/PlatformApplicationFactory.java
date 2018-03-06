package org.clopuccino.service.sns;

import ch.qos.logback.classic.Logger;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.*;
import org.clopuccino.domain.DeviceToken;
import org.clopuccino.domain.SnsApplication;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class PlatformApplicationFactory {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(AbstractEndpointArn.class.getSimpleName());

//    public static void syncSnsApplicationsWithAmazon(final AmazonSNSClient client, boolean createNewToAmazonSnsIfNotFound) {
//        ListPlatformApplicationsResult listPlatformApplicationsResult = client.listPlatformApplications();
//
//        List<PlatformApplication> platformApplications = listPlatformApplicationsResult.getPlatformApplications();
//
//        final SnsApplicationDao snsApplicationDao = new SnsApplicationDao();
//
//        LOGGER.info("Compare SNS platform applications: -----------------");
//
//        for (PlatformApplication platformApplication : platformApplications) {
//            SnsApplication snsApplication = convertSnsApplicationFromPlatformApplication(platformApplication);
//
//            if (snsApplication != null) {
//                String platform = snsApplication.getPlatform();
//
//                if (snsApplicationDao.existingApplicationByPlatform(platform)) {
//                    snsApplicationDao.updateSnsApplication(snsApplication);
//                } else {
//                    snsApplicationDao.createSnsApplication(snsApplication);
//                }
//            }
//        }
//
//        LOGGER.info("----------------- End comparing SNS platform applications");
//
//        // create others, if any.
//
//        if (createNewToAmazonSnsIfNotFound) {
//            Utility.getExecutorService().execute(new Runnable() {
//                @Override
//                public void run() {
//                    boolean atLeastOneCreated = false;
//
//                    List<SnsApplication> allSnsApplications = snsApplicationDao.findAllSnsApplications();
//
//                    DeviceToken.NotificationType[] types = DeviceToken.NotificationType.values();
//
//                    for (DeviceToken.NotificationType type : types) {
//                        String typeName = type.name();
//
//                        boolean foundPlatform = false;
//
//                        for (SnsApplication platformApplication : allSnsApplications) {
//                            String applicationArn = platformApplication.getPlatform();
//
//                            if (typeName.equals(applicationArn)) {
//                                foundPlatform = true;
//
//                                break;
//                            }
//                        }
//
//                        if (!foundPlatform) {
//                            // platform not found in DB, created to Amazon SNS
//                            String newApplicationArn = createPlatformApplicationByType(type, client);
//
//                            if (newApplicationArn != null) {
//                                atLeastOneCreated = true;
//
//                                LOGGER.info("New SNS application created with arn: " + newApplicationArn);
//                            }
//                        }
//                    }
//
//                    if (atLeastOneCreated) {
//                        // TODO: delay 5 seconds to wait for Amazon SNS get newly created applications when listPlatformApplications invoked.
//
//                        LOGGER.info("At lease one SNS application just created. Recursive to sync SNS applications again");
//
//                        Utility.getScheduledExecutorService().schedule(new Runnable() {
//                            @Override
//                            public void run() {
//                                syncSnsApplicationsWithAmazon(client, false);
//                            }
//                        }, 5, TimeUnit.SECONDS);
//
//                    } else {
//                        AbstractEndpointArn.loadSnsApplicationsFromDb(true);
//
//                        LOGGER.info("No SNS application created. Forced loading SNS applications from DB.");
//                    }
//                }
//            });
//        } else {
//            AbstractEndpointArn.loadSnsApplicationsFromDb(true);
//
//            LOGGER.info("No more checking if any notification type not found. Forced loading SNS applications from DB.");
//        }
//    }

    // Return null if platformApplication is null
    // or no correspondant notification type found for the platform of the application.
    public static SnsApplication convertSnsApplicationFromPlatformApplication(PlatformApplication platformApplication) {
        if (platformApplication != null) {
            SnsApplication snsApplication = new SnsApplication();


            //List SNS platform applications from Amazon SNS:
            //
            //APNS:
            //    ARN: arn:aws:sns:us-east-1:028435935783:app/APNS/filelug_ios_apple_prod
            //    Attributes:
            //        Enabled : true
            //        AppleCertificateExpirationDate : 2016-06-21T09:30:24Z
            //
            //APNS_SANDBOX:
            //    ARN: arn:aws:sns:us-east-1:028435935783:app/APNS_SANDBOX/filelug_ios_apple_dev
            //    Attributes:
            //        Enabled : true
            //        AppleCertificateExpirationDate : 2016-06-21T09:29:15Z


            String applicationArn = platformApplication.getPlatformApplicationArn();

            snsApplication.setApplicationArn(applicationArn);

            LOGGER.info("\n");
            LOGGER.info("ARN: " + applicationArn);

//            Map<String, String> attributes = platformApplication.getAttributes();
//
//            if (attributes != null) {
//                Set<Map.Entry<String, String>> entries = attributes.entrySet();
//
//                for (Map.Entry<String, String> entry : entries) {
//                    String attributeName = entry.getKey().toLowerCase();
//                    String attributeValue = entry.getValue();
//
//                    if (attributeName.equals("enabled")) {
//                        snsApplication.setEnabled(Boolean.valueOf(attributeValue));
//                    } else if (attributeName.contains("expirationdate")) {
//                        // FIX: There may be no such attribute for non-apple notifications.
//
//                        try {
//                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
//                            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//                            Date date = dateFormat.parse(attributeValue);
//
//                            snsApplication.setExpirationDate(date.getTime());
//                        } catch (Exception e) {
//                            snsApplication.setExpirationDate(0);
//                        }
//                    }
//
//                    LOGGER.info(attributeName + " : " + attributeValue);
//                }
//            }

            DeviceToken.NotificationType[] types = DeviceToken.NotificationType.values();

            for (DeviceToken.NotificationType type : types) {
                String typeName = type.name();

                if (applicationArn.contains("/" + typeName + "/")) {
                    snsApplication.setPlatform(typeName);

                    LOGGER.info("Found platform '" + typeName + "' for SNS platform application with arn: " + applicationArn);

                    break;
                }
            }

            if (snsApplication.getPlatform() == null) {
                snsApplication = null;

                LOGGER.error("Null platform for SNS application with arn: " + applicationArn + ". Skipped this platform application.");
            }

            LOGGER.info("\n");

            return snsApplication;
        } else {
            return null;
        }
    }

//    public static Hashtable<String, PlatformApplication> preparePlatformApplications(AmazonSNSClient client) {
//
//        Hashtable<String, PlatformApplication> platformApplications = new Hashtable<>();
//
//        //List current SNS platform applications:
//        //
//        //APNS:
//        //    ARN: arn:aws:sns:us-east-1:028435935783:app/APNS/filelug_ios_apple_prod
//        //    Attributes:
//        //        Enabled : true
//        //        AppleCertificateExpirationDate : 2016-06-21T09:30:24Z
//        //
//        //APNS_SANDBOX:
//        //    ARN: arn:aws:sns:us-east-1:028435935783:app/APNS_SANDBOX/filelug_ios_apple_dev
//        //    Attributes:
//        //        Enabled : true
//        //        AppleCertificateExpirationDate : 2016-06-21T09:29:15Z
//
//
//        // Newly created or updatedIfExists is true.
//        DeviceToken.NotificationType[] types = DeviceToken.NotificationType.values();
//
//        ListPlatformApplicationsResult currentSnsApplications = client.listPlatformApplications();
//
//        List<PlatformApplication> snsApplications = currentSnsApplications.getPlatformApplications();
//
//        LOGGER.debug("Prepare SNS platform applications: -----------------");
//
//        for (PlatformApplication snsApplication : snsApplications) {
//            String applicationArn = snsApplication.getPlatformApplicationArn();
//
//            LOGGER.debug("\n");
//            LOGGER.debug("ARN: " + applicationArn);
//
//            Map<String, String> attributes = snsApplication.getAttributes();
//
//            if (attributes != null) {
//                Set<Map.Entry<String, String>> entries = attributes.entrySet();
//
//                for (Map.Entry<String, String> entry : entries) {
//                    LOGGER.debug(entry.getKey() + " : " + entry.getValue());
//                }
//            }
//
//            // Load to repository from SNS
//
//            for (DeviceToken.NotificationType type : types) {
//                String typeName = type.name();
//
//                if (applicationArn.contains("/" + typeName + "/")) {
//                    platformApplications.put(typeName, snsApplication);
//
//                    LOGGER.debug("Platform application set with key: " + typeName);
//
//                    break;
//                }
//            }
//
//            LOGGER.debug("\n");
//        }
//
//        // create others, if any.
//
//        boolean atLeastOneCreated = false;
//
//        for (DeviceToken.NotificationType type : types) {
//            String typeName = type.name();
//
//            if (!platformApplications.containsKey(typeName) || platformApplications.get(typeName) == null) {
//                String newApplicationArn = createPlatformApplicationByType(type, client);
//
//                if (newApplicationArn != null) {
//                    atLeastOneCreated = true;
//                }
//            }
//        }
//
//        LOGGER.debug("----------------- End preparing platform applications");
//
//        if (atLeastOneCreated) {
//            LOGGER.debug("Recursive to add newly created applications.");
//
//            return preparePlatformApplications(client);
//        } else {
//            return platformApplications;
//        }
//    }

    public static void deletePlatformApplication(String applicationArn, AmazonSNSClient client) {
        DeletePlatformApplicationRequest request = new DeletePlatformApplicationRequest();

        request.setPlatformApplicationArn(applicationArn);

        client.deletePlatformApplication(request);

        LOGGER.info("Deleted SNS platform with application ARN: " + applicationArn);
    }

    public static void deleteAllPlatformApplications(AmazonSNSClient client) {
        ListPlatformApplicationsResult listPlatformApplicationsResult = client.listPlatformApplications();

        List<PlatformApplication> platformApplications = listPlatformApplicationsResult.getPlatformApplications();

        if (platformApplications != null) {
            for (PlatformApplication platformApplication : platformApplications) {
                String applicationArn = platformApplication.getPlatformApplicationArn();

                try {
                    deletePlatformApplication(applicationArn, client);
                } catch (Exception e) {
                    LOGGER.error("Failed to delete platform application with ARN: " + applicationArn, e);
                }
            }
        }
    }

    public PlatformApplicationFactory() {
    }

    public AmazonSNSClient prepareAmazonSnsClient() {
        AmazonSNSClient snsClient;

        try {
            String credentialFilename = "AwsCredentials.properties";

            ClassLoader classLoader = getClass().getClassLoader();

            InputStream credentialInputStream = classLoader.getResourceAsStream("/" + credentialFilename);

            if (credentialInputStream == null) {
                credentialInputStream = classLoader.getResourceAsStream(credentialFilename);
            }

            if (credentialInputStream == null) {
                throw new FileNotFoundException("File not found: " + credentialFilename);
            }

            snsClient = new AmazonSNSClient(new PropertiesCredentials(credentialInputStream));

            // MUST SET THE REGION
            snsClient.setRegion(Region.getRegion(Regions.US_WEST_2));
        } catch (Exception e) {
            snsClient = null;

            LOGGER.error("Failed to creating AmazonSNSClient.\n" + e.getMessage(), e);
        }

        return snsClient;
    }

    private static String createPlatformApplicationByType(DeviceToken.NotificationType type, AmazonSNSClient client) {
        String applicationArn = null;

        switch (type) {
            case APNS_SANDBOX:
                applicationArn = createPlatformApplicationForApnsSandbox(client);

                break;
            case APNS:
                applicationArn = createPlatformApplicationForApns(client);

                break;
            case GCM:
                // TODO: createPlatformApplicationForGcm(client);

                break;
            case BAIDU:
                // TODO: createPlatformApplicationForBaidu(client);

                break;
            case WNS:
                // TODO: createPlatformApplicationForWns(client);

                break;
//            default:
//                // TODO: for type of NONE
        }

        return applicationArn;
    }

    private static String createPlatformApplicationForApnsSandbox(AmazonSNSClient client) {
        String applicationName = "filelug_ios_apple_dev";
        DeviceToken.NotificationType notificationType = DeviceToken.NotificationType.APNS_SANDBOX;

        String certificate = "-----BEGIN CERTIFICATE-----\n" +
                             "MIIFizCCBHOgAwIBAgIIEM71KZkYUNIwDQYJKoZIhvcNAQEFBQAwgZYxCzAJBgNV\n" +
                             "BAYTAlVTMRMwEQYDVQQKDApBcHBsZSBJbmMuMSwwKgYDVQQLDCNBcHBsZSBXb3Js\n" +
                             "ZHdpZGUgRGV2ZWxvcGVyIFJlbGF0aW9uczFEMEIGA1UEAww7QXBwbGUgV29ybGR3\n" +
                             "aWRlIERldmVsb3BlciBSZWxhdGlvbnMgQ2VydGlmaWNhdGlvbiBBdXRob3JpdHkw\n" +
                             "HhcNMTUwNjIyMDkyOTE1WhcNMTYwNjIxMDkyOTE1WjCBijEjMCEGCgmSJomT8ixk\n" +
                             "AQEME2NvbS5maWxlbHVnLmZpbGVsdWcxQTA/BgNVBAMMOEFwcGxlIERldmVsb3Bt\n" +
                             "ZW50IElPUyBQdXNoIFNlcnZpY2VzOiBjb20uZmlsZWx1Zy5maWxlbHVnMRMwEQYD\n" +
                             "VQQLDApBN0xHREE3UkRIMQswCQYDVQQGEwJVUzCCASIwDQYJKoZIhvcNAQEBBQAD\n" +
                             "ggEPADCCAQoCggEBAM/gzQNLk00vzcda0F8+5v0lwdckb+LZgskq4iCKTat0BcMD\n" +
                             "topiY+GHkRv0jPvwOL6AXKAQbfg2foAYnsgDor0X547mvbqc4KHnQk0yK3tcUeYB\n" +
                             "6Eqst5zkzhni1MgkEUlgd6lZyuYiRy6jBrrWH7smJeSyabzU3jNXD+qi4v0nJwlc\n" +
                             "S6HKTdefui+KQ7BFRhbJ5xd7w3tb+xYxNcfeqAfVl/q6iiQKvYnxsQKEqcMUNFID\n" +
                             "/iDAkpqMXmlODthHY4OxHmlt6+WXHMIM2CHh+dTW5NzvhzsctcgxW9hEQf+27FQm\n" +
                             "RXpwYdMFRoa+MRx/ILYceLz7SxCBNqG6vt3cntMCAwEAAaOCAeUwggHhMB0GA1Ud\n" +
                             "DgQWBBQ7JiCUuc5aFfteel8M6ZGvWSZW/DAJBgNVHRMEAjAAMB8GA1UdIwQYMBaA\n" +
                             "FIgnFwmpthhgi+zruvZHWcVSVKO3MIIBDwYDVR0gBIIBBjCCAQIwgf8GCSqGSIb3\n" +
                             "Y2QFATCB8TCBwwYIKwYBBQUHAgIwgbYMgbNSZWxpYW5jZSBvbiB0aGlzIGNlcnRp\n" +
                             "ZmljYXRlIGJ5IGFueSBwYXJ0eSBhc3N1bWVzIGFjY2VwdGFuY2Ugb2YgdGhlIHRo\n" +
                             "ZW4gYXBwbGljYWJsZSBzdGFuZGFyZCB0ZXJtcyBhbmQgY29uZGl0aW9ucyBvZiB1\n" +
                             "c2UsIGNlcnRpZmljYXRlIHBvbGljeSBhbmQgY2VydGlmaWNhdGlvbiBwcmFjdGlj\n" +
                             "ZSBzdGF0ZW1lbnRzLjApBggrBgEFBQcCARYdaHR0cDovL3d3dy5hcHBsZS5jb20v\n" +
                             "YXBwbGVjYS8wTQYDVR0fBEYwRDBCoECgPoY8aHR0cDovL2RldmVsb3Blci5hcHBs\n" +
                             "ZS5jb20vY2VydGlmaWNhdGlvbmF1dGhvcml0eS93d2RyY2EuY3JsMAsGA1UdDwQE\n" +
                             "AwIHgDATBgNVHSUEDDAKBggrBgEFBQcDAjAQBgoqhkiG92NkBgMBBAIFADANBgkq\n" +
                             "hkiG9w0BAQUFAAOCAQEArwubdEkr8UF2n1s22OYALj/ONRUKpJBnaVKIsyshToLk\n" +
                             "zHhH2QGZ5yhB5HUDa5J387Zwn7qslVaeGSgRFuidSdT9bK3fDGXwkmpn3s+Hu+ze\n" +
                             "bj7WGENeVNhkS5E+sgqYmsQ2dfFID0I9KnQa1RXisE0vtEGkLNf0YO3vC3nV7E5i\n" +
                             "fGlSoqxozGgTRQAJdEk7Gk+4GDM/gqSequiDUkp4rSk9RKKofIRyk02Z0kn48PTS\n" +
                             "D5AXnb8xeNqUsl3sU6PwOoGpfAh2Kkb9Z+C76DrZFzL8w+ZXqps6O94cPMz9y9+L\n" +
                             "H8oCTKt2dq1ou5vhZqUfXUdJd2W/aGAkvQbNJk417Q==\n" +
                             "-----END CERTIFICATE-----";

        String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
                            "MIIEpAIBAAKCAQEAz+DNA0uTTS/Nx1rQXz7m/SXB1yRv4tmCySriIIpNq3QFwwO2\n" +
                            "imJj4YeRG/SM+/A4voBcoBBt+DZ+gBieyAOivRfnjua9upzgoedCTTIre1xR5gHo\n" +
                            "Sqy3nOTOGeLUyCQRSWB3qVnK5iJHLqMGutYfuyYl5LJpvNTeM1cP6qLi/ScnCVxL\n" +
                            "ocpN15+6L4pDsEVGFsnnF3vDe1v7FjE1x96oB9WX+rqKJAq9ifGxAoSpwxQ0UgP+\n" +
                            "IMCSmoxeaU4O2Edjg7EeaW3r5ZccwgzYIeH51Nbk3O+HOxy1yDFb2ERB/7bsVCZF\n" +
                            "enBh0wVGhr4xHH8gthx4vPtLEIE2obq+3dye0wIDAQABAoIBAB1TC/ikB6KC1NeD\n" +
                            "gxN8jx803VRZKoNoxZ9VMlL+z5gZJNruwihhWs+szRaRd/XuXELbXrWhu7GhFlVB\n" +
                            "o9x5TtK39Y1eFXOmv7V3Jw42vpWHX3I/4KHwHXK/gCd8rSGSnPGp7xdvuESgn0P/\n" +
                            "8WoX3KAM8JmDZZEiwjmh1Xf3BGhLhYlfC6R28AiZIpSZTgNToWF/zxXwyQH8y7h9\n" +
                            "IFqj2nvLnrv9C3yAEaa7Qy9E/lBPcSyy3t9CPE+GwqfbBWBN2+TKWkh1GhGswiHD\n" +
                            "mDNwPShnsedH5lgp+VqIbO6N+XNdMIxdKJh4R/xj/h8LeP6y9EdmThf4Rsd3n14W\n" +
                            "GjjMPakCgYEA6lnkFm0jhNC5opgoxN7Bjen5huEeLT32guEQesKgbx/ZSuMx7Sv5\n" +
                            "IycCPMIaUFl8Sy8+vaUUHeaDL4Hxh9Y5VZp1P54kiaRd0e0gWg0fdR02SS8VH5Kx\n" +
                            "om5iuiwoFIfXoPzzueuT+r802mZOGOBnsTh+46fBBi6PvZrho6DMGw8CgYEA4xTb\n" +
                            "SNoWSBy23+muPKXiSoQ6YxlghgT34MmGuDPXNa0VrcVThF1Yg721Kul9VDK+uDPa\n" +
                            "uVljwI31kr05qI9hpZwxEaOtqixu0tyEw6qxR0EBNlT/00omhuPLtg2chgqPPjpE\n" +
                            "0nTVzLYN4aUPQmcfzHdQSLtxvYmumrv8vFhwD/0CgYAYNzmuZ5CoBLlp57cuwKXX\n" +
                            "eDnkihaILkLuuzrstHfHg878QgXCsCyKSVmd1aDKdLS3QCaHiitN+6fKnO7MToKW\n" +
                            "KeO7syerYWSgcJRLOdfqTh5x6X+RR6M7WZSyECYEGRVa+UhmCcW8v01C1Bd10ppk\n" +
                            "YaYa+BamAOGxgfOW6OKuWQKBgQCg+/89lYaoVtr6wJKKYlub0kQGT05TlKAaVyRP\n" +
                            "4EiYv/0XdUXxF7ARrWMLaf40u8jjk7djIVELiFhpUAbZTdS+8I0E4VHTy65BhJ4G\n" +
                            "jTMivZ3bY8T9iJrplAKuLNYjXMcSKYLy5GtRtlr+9eJsg+lSLd/XwDMxWLNU9SKG\n" +
                            "b/f6DQKBgQC21uUrjYrb3ThJOZHClaV+HVg5MVjydktQmcbDdfDcENyCihXJOvyX\n" +
                            "ZncaFS7dXSk0ixvEg9OB9MZlMRNL8M2WUq04dUG2NrUO8nhhopaQtsgYrPXqVPLF\n" +
                            "ePnCH5MTdZprWeEEf/KJSBdciQK9RBW/56QiriTRPeFAD1bIHzyamw==\n" +
                            "-----END RSA PRIVATE KEY-----";

        return createPlatformApplication(applicationName, notificationType, certificate, privateKey, client);
    }

    private static String createPlatformApplicationForApns(AmazonSNSClient client) {
        String applicationName = "filelug_ios_apple_prod";
        DeviceToken.NotificationType notificationType = DeviceToken.NotificationType.APNS;

        String certificate = "-----BEGIN CERTIFICATE-----\n" +
                             "MIIFijCCBHKgAwIBAgIINVONGSSAGnwwDQYJKoZIhvcNAQEFBQAwgZYxCzAJBgNV\n" +
                             "BAYTAlVTMRMwEQYDVQQKDApBcHBsZSBJbmMuMSwwKgYDVQQLDCNBcHBsZSBXb3Js\n" +
                             "ZHdpZGUgRGV2ZWxvcGVyIFJlbGF0aW9uczFEMEIGA1UEAww7QXBwbGUgV29ybGR3\n" +
                             "aWRlIERldmVsb3BlciBSZWxhdGlvbnMgQ2VydGlmaWNhdGlvbiBBdXRob3JpdHkw\n" +
                             "HhcNMTUwNzIyMDIzMDQwWhcNMTYwNzIxMDIzMDQwWjCBiTEjMCEGCgmSJomT8ixk\n" +
                             "AQEME2NvbS5maWxlbHVnLmZpbGVsdWcxQDA+BgNVBAMMN0FwcGxlIFByb2R1Y3Rp\n" +
                             "b24gSU9TIFB1c2ggU2VydmljZXM6IGNvbS5maWxlbHVnLmZpbGVsdWcxEzARBgNV\n" +
                             "BAsMCkE3TEdEQTdSREgxCzAJBgNVBAYTAlVTMIIBIjANBgkqhkiG9w0BAQEFAAOC\n" +
                             "AQ8AMIIBCgKCAQEA1dNBG4Il0PUodnhDNUqeWTeODRwt+nsswjtnzLv1RhGTOYg6\n" +
                             "Xdeo/1J6Yr9xMbcueVKtR3ajy3J81z9511OGXCaYIOgQt54H7Rj/kxRfPpglEDrD\n" +
                             "diUC1B/9qydSvWQqQbyTRtpmKBubMKjr1UJTel1XuKMtVaOrWsZxugsGaGXrBQfe\n" +
                             "+bksrRtbvph5URtuKVkqS5tS8SmT5UeAEUYg7vrBcDx6OU9jWoQQo6liMkRL5vt5\n" +
                             "bcZ1az2Q6NC1YBwoZcvXIGCotRnSrM6dBT/qG8HpbIQtUoYibVIkAFuQOHh+g7Eg\n" +
                             "PWbNZgSvlnqMRRJrIlVm5LDLc0gcNef83/wEOwIDAQABo4IB5TCCAeEwHQYDVR0O\n" +
                             "BBYEFAU73kuVy1XiUl9YskDKR0KlFYRLMAkGA1UdEwQCMAAwHwYDVR0jBBgwFoAU\n" +
                             "iCcXCam2GGCL7Ou69kdZxVJUo7cwggEPBgNVHSAEggEGMIIBAjCB/wYJKoZIhvdj\n" +
                             "ZAUBMIHxMIHDBggrBgEFBQcCAjCBtgyBs1JlbGlhbmNlIG9uIHRoaXMgY2VydGlm\n" +
                             "aWNhdGUgYnkgYW55IHBhcnR5IGFzc3VtZXMgYWNjZXB0YW5jZSBvZiB0aGUgdGhl\n" +
                             "biBhcHBsaWNhYmxlIHN0YW5kYXJkIHRlcm1zIGFuZCBjb25kaXRpb25zIG9mIHVz\n" +
                             "ZSwgY2VydGlmaWNhdGUgcG9saWN5IGFuZCBjZXJ0aWZpY2F0aW9uIHByYWN0aWNl\n" +
                             "IHN0YXRlbWVudHMuMCkGCCsGAQUFBwIBFh1odHRwOi8vd3d3LmFwcGxlLmNvbS9h\n" +
                             "cHBsZWNhLzBNBgNVHR8ERjBEMEKgQKA+hjxodHRwOi8vZGV2ZWxvcGVyLmFwcGxl\n" +
                             "LmNvbS9jZXJ0aWZpY2F0aW9uYXV0aG9yaXR5L3d3ZHJjYS5jcmwwCwYDVR0PBAQD\n" +
                             "AgeAMBMGA1UdJQQMMAoGCCsGAQUFBwMCMBAGCiqGSIb3Y2QGAwIEAgUAMA0GCSqG\n" +
                             "SIb3DQEBBQUAA4IBAQDA9tc7QFdeB35SbceLi/wNl0EwSziExaCIhCC3GIh1gkYL\n" +
                             "E6LWZIHkQP7qGFBvtcR4lbHj8gXbIe0xMnGFhelwjjw5QkJr4912WCCEgIFtFddw\n" +
                             "YSZfWjKg8as3IKFsac8Yj3tTMCfL5/taXwxaybm5lILjRwOVkGjDLySpf32gOgzU\n" +
                             "L19iJpub9BfSHr/ffu7Kte5iQ3DGot75IiQ1S6rU4GYvG8gsRpAEqOJtQf7skXCv\n" +
                             "S2ti4VGbTGmVROi+E50FMi9tmvoS/D4PAwRkjNTqmGXzjieN8dtjszr7/q9feChd\n" +
                             "sgsvRWfm64GkFaqwhzNdpI+iQcw7/faq2lgFPdG+\n" +
                             "-----END CERTIFICATE-----";

        String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
                            "MIIEpgIBAAKCAQEA1dNBG4Il0PUodnhDNUqeWTeODRwt+nsswjtnzLv1RhGTOYg6\n" +
                            "Xdeo/1J6Yr9xMbcueVKtR3ajy3J81z9511OGXCaYIOgQt54H7Rj/kxRfPpglEDrD\n" +
                            "diUC1B/9qydSvWQqQbyTRtpmKBubMKjr1UJTel1XuKMtVaOrWsZxugsGaGXrBQfe\n" +
                            "+bksrRtbvph5URtuKVkqS5tS8SmT5UeAEUYg7vrBcDx6OU9jWoQQo6liMkRL5vt5\n" +
                            "bcZ1az2Q6NC1YBwoZcvXIGCotRnSrM6dBT/qG8HpbIQtUoYibVIkAFuQOHh+g7Eg\n" +
                            "PWbNZgSvlnqMRRJrIlVm5LDLc0gcNef83/wEOwIDAQABAoIBAQCSBhcshhD1Rrc/\n" +
                            "UJn1RuQk4X+tDFWJ/ZxveMpGUwYJt+7nF1VkyOasltLlpP/Uh9jBGekkKK1rPuBq\n" +
                            "zvqSRoQ3YlP6BQGFo/KdxLik5QC2aojFMJxTjqsD2mUOAWjL1h/VjMa3WGbzdQbi\n" +
                            "kDmeBhGkOyKxAR7woTlgYK1B9E2H9PG1bitkjiFzJArRVu9nvgqPRbkYsC1dKvpa\n" +
                            "Q4h6gjGHvN/solaSMRghS0BAUG5BqzwLXA3Qdd2CwCNg90tNPNOR0cw8DPSppB1v\n" +
                            "9gOM2wL4ZsmtT6mUd4PlH2DLi8IEg3mxsUWJlAi7SQ74A0nNH3sKsrlmYAepPtjI\n" +
                            "qxBlb5/BAoGBAPxeD6vuEgIQHvlFk5OZNfLnM4BxjFV9LQX7BwKDVYgkQEFiwUWN\n" +
                            "1ejZ7TfHNJFFmzZoVIASazqDz/Eri5o7hRktl5xuEuwoVlKUNL8nYpfGTqC8kXMj\n" +
                            "TWm5KvB+SEpfD0oBD02Fq+otItuz/XumIwT1pYgtg38uRcodkbdO+PbZAoGBANjn\n" +
                            "K6DIj641oVAGG5EYHqrXKRIIIXzoX4jIbNDBhKCyfDlM3iRI9PJPJmKNm/ingn/H\n" +
                            "UTzHt08r/S5u5Fi7V9alYLIMgtvhTKVjsfBWQ+owa8pYcue8rhGn8Ifk71fJCyJn\n" +
                            "5Er05rBXgfjaH+OF1DG+sdBur6eBlHbL9zlG+S8zAoGBAOWuvdiefek+vtHjjuKg\n" +
                            "aEdAQNE/hpcvq9BrnP+ehDO5unlH5drlsa+Q16MSPDeTACZOYcmF+e1xlclK9m1S\n" +
                            "ilpV8kw+qh4gwB+8Hx1yPPshUuJbfI0rrgLwsbHbXPPC/Gk/+at2YvZoSfdnIuZs\n" +
                            "WCblYthDhBA2DvJaYwpQZ6pRAoGBAICioKIrL+L+VVvTM3gaWN2y2t2hytJ8MWQ6\n" +
                            "kSZBDnoLQFbvOnNEfVhTDo22NAEEvwUu3QPx4cnnabCm/W+OM6rNy4nsl5L1i/Iu\n" +
                            "LPI/BuoVq2eGiybaJFW+Ro0hfbyPM7ZE7QRzN3jvfNpr6iuSS+Twlh3p9uydkcHe\n" +
                            "dxEWAWQ3AoGBAK1WuIIfi0gvi5DW31FddDtSCvcSIq+9eoc05sjRhS5EuGYty6o2\n" +
                            "y7WIBiEliWCqUTh4t9ax0ntVEEvnIimmAs0qDTK2+ryImQjNqtbPHJnemjFrksQD\n" +
                            "gD1KGYsnCinIxeAjwwhtbF5R1XopgHTUZL3OpfefyiRKWelX2yQIhtcO\n" +
                            "-----END RSA PRIVATE KEY-----";

        return createPlatformApplication(applicationName, notificationType, certificate, privateKey, client);
    }

    private static String createPlatformApplication(String applicationName, DeviceToken.NotificationType platform, String principal, String credential, AmazonSNSClient client) {
        String applicationArn = null;

        try {
            CreatePlatformApplicationRequest platformApplicationRequest = new CreatePlatformApplicationRequest();

            Map<String, String> attributes = new HashMap<>();

            attributes.put("PlatformPrincipal", principal);
            attributes.put("PlatformCredential", credential);
            platformApplicationRequest.setAttributes(attributes);
            platformApplicationRequest.setName(applicationName);
            platformApplicationRequest.setPlatform(platform.name());

            CreatePlatformApplicationResult applicationResult = client.createPlatformApplication(platformApplicationRequest);

            if (applicationResult != null) {
                applicationArn = applicationResult.getPlatformApplicationArn();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to create platform application result for application name:" + applicationName + "\n" + e.getClass().getName() + "\n" + e.getMessage(), e);
        }

        return applicationArn;
    }


}
