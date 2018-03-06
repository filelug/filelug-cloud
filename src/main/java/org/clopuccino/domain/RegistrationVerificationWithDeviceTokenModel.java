package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RegistrationVerificationWithDeviceTokenModel</code>
 * is the model for register verification service with device token.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistrationVerificationWithDeviceTokenModel extends RegistrationVerificationModel {

    @JsonProperty("device-token")
    private DeviceToken deviceToken;

    public RegistrationVerificationWithDeviceTokenModel() {
        super();
    }

    public DeviceToken getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(DeviceToken deviceToken) {
        this.deviceToken = deviceToken;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RegistrationVerificationWithDeviceTokenModel{");
        sb.append("countryId='").append(countryId).append('\'');
        sb.append(", phoneNumber='").append(phoneNumber).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", verification='").append(verification).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", locale='").append(locale).append('\'');
        sb.append(", verifyCode='").append(verifyCode).append('\'');
        sb.append(", deviceToken='").append(deviceToken).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
