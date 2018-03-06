package org.clopuccino.domain;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class SnsApplication implements Cloneable {

    private String platform;

    private String applicationArn;

//    private Boolean enabled;
//
//    private long expirationDate;

    private long lastModifiedDate;

    public SnsApplication() {
    }

    public SnsApplication(String platform, String applicationArn, long lastModifiedDate) {
        this.platform = platform;
        this.applicationArn = applicationArn;
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getApplicationArn() {
        return applicationArn;
    }

    public void setApplicationArn(String applicationArn) {
        this.applicationArn = applicationArn;
    }

    public long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SnsApplication{");
        sb.append("applicationArn='").append(applicationArn).append('\'');
        sb.append(", platform='").append(platform).append('\'');
        sb.append(", lastModifiedDate=").append(lastModifiedDate);
        sb.append('}');
        return sb.toString();
    }
}
