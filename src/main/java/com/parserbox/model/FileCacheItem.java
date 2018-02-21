package com.parserbox.model;

import java.util.Date;

public class FileCacheItem {

    String error;
    String fileKey;
    String status;

    public enum status {
        ERROR,
        IMPORTED,
        INACTIVE,
        PENDING,
        UNKNOWN;
    }
    status fileStatus;

    Date lastAccessed = new Date();

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public FileCacheItem(String fileKey, status fileStatus) {
        this.fileKey = fileKey;
        this.fileStatus = fileStatus;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileId) {
        this.fileKey = fileKey;
    }

    public FileCacheItem.status getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(FileCacheItem.status fileStatus) {
        this.fileStatus = fileStatus;
    }

    public Date getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(Date lastAccessed) {
        this.lastAccessed = lastAccessed;
    }
}
