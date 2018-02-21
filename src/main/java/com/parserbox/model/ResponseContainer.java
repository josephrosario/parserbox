package com.parserbox.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.parserbox.model.sso.OAuthAccessToken;

import java.util.List;

public class ResponseContainer {

    public enum bufferType {
        HTML,
        JSON,
        STRING
    }

    @JsonProperty("buffer")
    private String buffer;

    @JsonProperty("bufferType")
    private String bufferType;

    @JsonProperty("error")
    private String error;

    @JsonProperty("dataCache")
    private List<DataCacheItem> dataCache;

    @JsonProperty("message")
    private String message;

    @JsonProperty("returnValue")
    private double returnValue;

    @JsonProperty("parsingFileInfo")
    private FileInfo parsingFileInfo;

    @JsonProperty("status")
    private String status;

    @JsonProperty("url")
    private String url;

    @JsonProperty("oAuthAccessToken")
    private OAuthAccessToken oAuthAccessToken;

    public String getBuffer() {
        return buffer;
    }

    public void setBuffer(String buffer) {
        this.buffer = buffer;
    }

    public String getBufferType() {
        return bufferType;
    }

    public void setBufferType(String type) {
        this.bufferType = type;
    }
    public void setBufferType(bufferType type) {
        this.bufferType = type.toString();
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<DataCacheItem> getDataCache() {
        return dataCache;
    }

    public void setDataCache(List<DataCacheItem> dataCache) {
        this.dataCache = dataCache;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(double returnValue) {
        this.returnValue = returnValue;
    }

    public FileInfo getParsingFileInfo() {
        return parsingFileInfo;
    }

    public void setParsingFileInfo(FileInfo parsingFileInfo) {
        this.parsingFileInfo = parsingFileInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public OAuthAccessToken getoAuthAccessToken() {
        return oAuthAccessToken;
    }

    public void setoAuthAccessToken(OAuthAccessToken oAuthAccessToken) {
        this.oAuthAccessToken = oAuthAccessToken;
    }
}
