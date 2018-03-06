package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>RegisterVerificationModel</code> is the model for register verification service.
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistrationVerificationModel extends RegisterModel {

    @JsonProperty("code")
    protected String verifyCode;

    public RegistrationVerificationModel() {
        super();
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RegistrationVerificationModel{");
        sb.append("countryId='").append(countryId).append('\'');
        sb.append(", phoneNumber='").append(phoneNumber).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", verification='").append(verification).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", locale='").append(locale).append('\'');
        sb.append(", verifyCode='").append(verifyCode).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
