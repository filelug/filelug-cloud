package org.clopuccino.device.servlet;

/**
 * <code>TWSMSSender</code> sends verification SMS in country TW after user registered.
 *
 * @author masonhsieh
 * @version 1.0
 */
public abstract class SMSSender implements ClopuccinoNotification {

    protected final SMSSenderFactory.Purpose purpose;

    protected String userId;

    protected String userLocale;

    public SMSSender(SMSSenderFactory.Purpose purpose, String userId, String userLocale) {
        if (purpose == null) {
            this.purpose = SMSSenderFactory.Purpose.GENERAL;
        } else {
            this.purpose = purpose;
        }

        this.userId = userId;
        this.userLocale = userLocale;
    }

    public SMSSenderFactory.Purpose getPurpose() {
        return purpose;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserLocale() {
        return userLocale;
    }

    public void setUserLocale(String userLocale) {
        this.userLocale = userLocale;
    }
}
