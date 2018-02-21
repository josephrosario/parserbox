package com.parserbox.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsingTemplate {


    @JsonProperty("Id")
    private String Id;
    @JsonProperty("Name")
    private String CurrentDocumentId__c;
    @JsonProperty("CurrentDocumentId__c")
    private String Description__c;
    @JsonProperty("LastModifiedById")
    private String LastModifiedById;
    @JsonProperty("MetaData__c")
    private String MetaData__c;
    @JsonProperty("OwnerId")
    private String OwnerId;
    @JsonProperty("ScalingMultiplier__c")
    private String ScalingMultiplier__c;


    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getCurrentDocumentId__c() {
        return CurrentDocumentId__c;
    }

    public void setCurrentDocumentId__c(String currentDocumentId__c) {
        CurrentDocumentId__c = currentDocumentId__c;
    }

    public String getDescription__c() {
        return Description__c;
    }

    public void setDescription__c(String description__c) {
        Description__c = description__c;
    }

    public String getLastModifiedById() {
        return LastModifiedById;
    }

    public void setLastModifiedById(String lastModifiedById) {
        LastModifiedById = lastModifiedById;
    }

    public String getMetaData__c() {
        return MetaData__c;
    }

    public void setMetaData__c(String metaData__c) {
        MetaData__c = metaData__c;
    }

    public String getOwnerId() {
        return OwnerId;
    }

    public void setOwnerId(String ownerId) {
        OwnerId = ownerId;
    }

    public String getScalingMultiplier__c() {
        return ScalingMultiplier__c;
    }

    public void setScalingMultiplier__c(String scalingMultiplier__c) {
        ScalingMultiplier__c = scalingMultiplier__c;
    }
}
