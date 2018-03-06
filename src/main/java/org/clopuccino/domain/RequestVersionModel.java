package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.clopuccino.ClopuccinoMessages;

/**
 * <code>RequestVersionModel</code> represents the version information of the desktop.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestVersionModel extends RequestModel {

    @JsonProperty("current-version")
    private String currentVersion;

    @JsonProperty("latest-version")
    private String latestVersion;

    /* The download url for the latest desktop software */
    @JsonProperty("download-url")
    private String downloadUrl;


    public RequestVersionModel() {
        super();
    }

    public RequestVersionModel(Integer sid, String operatorId, String locale, String currentVersion, String latestVersion, String downloadUrl) {
        super(sid, operatorId, locale);

        this.currentVersion = currentVersion;

        this.latestVersion = latestVersion;

        this.downloadUrl = downloadUrl;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
