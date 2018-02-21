package com.parserbox.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserInfo {

    @JsonProperty("UserId")
    private String UserId;
    @JsonProperty("FirstName")
    private String FirstName;
    @JsonProperty("LastName")
    private String LastName;
    @JsonProperty("UserEmail")
    private String UserEmail;
    @JsonProperty("OrganizationId")
    private String OrganizationId;
    @JsonProperty("OrganizationName")
    private String OrganizationName;
    @JsonProperty("DefaultCurrency")
    private String DefaultCurrency;
    @JsonProperty("Locale")
    private String Locale;

    public UserInfo(){}

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getUserEmail() {
        return UserEmail;
    }

    public void setUserEmail(String userEmail) {
        UserEmail = userEmail;
    }

    public String getOrganizationId() {
        return OrganizationId;
    }

    public void setOrganizationId(String organizationId) {
        OrganizationId = organizationId;
    }

    public String getOrganizationName() {
        return OrganizationName;
    }

    public void setOrganizationName(String organizationName) {
        OrganizationName = organizationName;
    }

    public String getDefaultCurrency() {
        return DefaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        DefaultCurrency = defaultCurrency;
    }

    public String getLocale() {
        return Locale;
    }

    public void setLocale(String locale) {
        Locale = locale;
    }
}
