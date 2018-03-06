package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <code>CountryModel</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CountryModel {

    @JsonProperty("country-id")
    private String countryId;

    @JsonProperty("country-code")
    private Integer countryCode;

    @JsonProperty("country-name")
    private String countryName;

    @JsonProperty("phone-sample")
    private String phoneSample;

    public CountryModel() {
    }

    public CountryModel(String countryId, Integer countryCode, String countryName, String phoneSample) {
        this.countryId = countryId;
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.phoneSample = phoneSample;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public Integer getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(Integer countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getPhoneSample() {
        return phoneSample;
    }

    public void setPhoneSample(String phoneSample) {
        this.phoneSample = phoneSample;
    }
}
