package org.clopuccino.domain;

import org.apache.commons.lang3.time.DateUtils;
import org.clopuccino.Constants;
import org.clopuccino.Utility;

import java.io.Serializable;
import java.util.Date;

/**
 * <code>ClientSession</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class ClientSession implements Serializable, Cloneable {

    private static final long serialVersionUID = 3450441430134320353L;

    private Long computerId;

//    private String computerGroup;
//
//    private String computerName;

    private String userId;

    private String sessionId;

    /* in ms */
    private Long lastAccessTime;

    private boolean showHidden;

    private String locale;

    private String userComputerId;

    private String deviceToken;

    public ClientSession() {
        super();
    }

    public ClientSession(Long computerId, String userId, String sessionId, String deviceToken, Long lastAccessTime, boolean showHidden, String locale) {
        this.computerId = computerId;
        this.userId = userId;
        this.sessionId = sessionId;
        this.deviceToken = deviceToken;
        this.lastAccessTime = lastAccessTime;
        this.showHidden = showHidden;
        this.locale = locale;
        this.userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);
    }

    public ClientSession(Long computerId, String userId, String sessionId, Long lastAccessTime, boolean showHidden, String locale) {
        this.computerId = computerId;
        this.userId = userId;
        this.sessionId = sessionId;
        this.lastAccessTime = lastAccessTime;
        this.showHidden = showHidden;
        this.locale = locale;
        this.userComputerId = Utility.generateUserComputerIdFrom(userId, computerId);
    }


    public Long getComputerId() {
        return computerId;
    }

    public void setComputerId(Long computerId) {
        this.computerId = computerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserComputerId() {
        return userComputerId;
    }

    public void setUserComputerId(String userComputerId) {
        this.userComputerId = userComputerId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public Long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(Long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public boolean isShowHidden() {
        return showHidden;
    }

    public void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * @return true if the session is time out.
     */
    public boolean checkTimeout() {
        return checkTimeout(this.lastAccessTime, Constants.DEFAULT_CLIENT_SESSION_IDLE_TIMEOUT_IN_SECONDS);
    }

    public boolean checkTimeout(Integer timeout) {
        return checkTimeout(this.lastAccessTime, timeout);
    }

    public static boolean checkTimeout(long lastAccessTime, Integer timeout) {
        if (timeout == null) {
            timeout = Constants.DEFAULT_CLIENT_SESSION_IDLE_TIMEOUT_IN_SECONDS;
        }

        Date currentDate = new Date();

        Date lastAccessDate = new Date();
        lastAccessDate.setTime(lastAccessTime);
        lastAccessDate = DateUtils.addSeconds(lastAccessDate, timeout);

        return currentDate.compareTo(lastAccessDate) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientSession that = (ClientSession) o;

        if (!sessionId.equals(that.sessionId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sessionId.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClientSession{");
        sb.append("computerId=").append(computerId);
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", sessionId='").append(sessionId).append('\'');
        sb.append(", deviceToken='").append(deviceToken).append('\'');
        sb.append(", lastAccessTime=").append(lastAccessTime);
        sb.append(", showHidden=").append(showHidden);
        sb.append(", locale='").append(locale).append('\'');
        sb.append(", userComputerId='").append(userComputerId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
