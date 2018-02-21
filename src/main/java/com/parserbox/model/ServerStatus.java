package com.parserbox.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServerStatus {

    @JsonProperty("Status")
    boolean status;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
