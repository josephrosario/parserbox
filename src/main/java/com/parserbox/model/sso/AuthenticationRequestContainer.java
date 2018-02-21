package com.parserbox.model.sso;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticationRequestContainer {
    @JsonProperty("remoteLocation")
    public String remoteLocation;

    @JsonProperty("remoteUserId")
    public String remoteUserId;

    @JsonProperty("remotePassword")
    public String remotePassword;

    public String getRemoteLocation() {
        return remoteLocation;
    }

    public void setRemoteLocation(String remoteLocation) {
        this.remoteLocation = remoteLocation;
    }

    public String getRemoteUserId() {
        return remoteUserId;
    }

    public void setRemoteUserId(String remoteUserId) {
        this.remoteUserId = remoteUserId;
    }

    public String getRemotePassword() {
        return remotePassword;
    }

    public void setRemotePassword(String remotePassword) {
        this.remotePassword = remotePassword;
    }
}
