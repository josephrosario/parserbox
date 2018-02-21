package com.parserbox.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSettings {

    @JsonProperty("Name")
    private String Name;
    @JsonProperty("AutoSaveSwitch__c")
    private boolean AutoSaveSwitch__c;
    @JsonProperty("FirstTimeSamplesCreated__c")
    private boolean FirstTimeSamplesCreated__c;
    @JsonProperty("ServerLocationOverride__c")
    private String ServerLocationOverride__c;
    @JsonProperty("LastOpenParsingTemplate__c")
    private String LastOpenParsingTemplate__c;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public boolean isAutoSaveSwitch__c() {
        return AutoSaveSwitch__c;
    }

    public void setAutoSaveSwitch__c(boolean autoSaveSwitch__c) {
        AutoSaveSwitch__c = autoSaveSwitch__c;
    }

    public boolean isFirstTimeSamplesCreated__c() {
        return FirstTimeSamplesCreated__c;
    }

    public void setFirstTimeSamplesCreated__c(boolean firstTimeSamplesCreated__c) {
        FirstTimeSamplesCreated__c = firstTimeSamplesCreated__c;
    }

    public String getServerLocationOverride__c() {
        return ServerLocationOverride__c;
    }

    public void setServerLocationOverride__c(String serverLocationOverride__c) {
        ServerLocationOverride__c = serverLocationOverride__c;
    }

    public String getLastOpenParsingTemplate__c() {
        return LastOpenParsingTemplate__c;
    }

    public void setLastOpenParsingTemplate__c(String lastOpenParsingTemplate__c) {
        LastOpenParsingTemplate__c = lastOpenParsingTemplate__c;
    }
}
