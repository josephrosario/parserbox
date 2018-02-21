package com.parserbox.model.sso;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class OAuthAccessToken  {

    private static final long serialVersionUID = 1L;

    @JsonProperty("Id")
    private String Id;
    @JsonProperty("AppId__c")
    public String AppId__c;
    @JsonProperty("SpecialId1__c")
    public String SpecialId1__c;
    @JsonProperty("AccessToken__c")
    public String AccessToken__c;
    @JsonProperty("AccessTokenSecret__c")
    public String AccessTokenSecret__c;
    @JsonProperty("LastModifiedDate")
    public Date LastModifiedDate;
    @JsonProperty("BaseURI__c")
    public String BaseURI__c;


    public OAuthAccessToken() {
        this( null, null, null);
    }

    public OAuthAccessToken(String AppId, String AccessToken, String AccessTokenSecret) {
        this.AppId__c = AppId;
        this.AccessToken__c = AccessToken;
        this.AccessTokenSecret__c = AccessToken__c;
    }


    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getAppId__c() {
        return AppId__c;
    }

    public void setAppId__c(String appId__c) {
        AppId__c = appId__c;
    }

    public String getSpecialId1__c() {
        return SpecialId1__c;
    }

    public void setSpecialId1__c(String specialId1__c) {
        SpecialId1__c = specialId1__c;
    }

    public String getAccessToken__c() {
        return AccessToken__c;
    }

    public void setAccessToken__c(String accessToken__c) {
        AccessToken__c = accessToken__c;
    }

    public String getAccessTokenSecret__c() {
        return AccessTokenSecret__c;
    }

    public void setAccessTokenSecret__c(String accessTokenSecret__c) {
        AccessTokenSecret__c = accessTokenSecret__c;
    }

    public Date getLastModifiedDate() {
        return LastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        LastModifiedDate = lastModifiedDate;
    }

    public String getBaseURI__c() {
        return BaseURI__c;
    }

    public void setBaseURI__c(String baseURI__c) {
        BaseURI__c = baseURI__c;
    }
}
