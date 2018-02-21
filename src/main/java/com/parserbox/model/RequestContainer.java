package com.parserbox.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.parserbox.model.sso.OAuthAccessToken;

import java.util.List;

public class RequestContainer {

    @JsonProperty("parsingTemplate")
    private ParsingTemplate parsingTemplate;

    @JsonProperty("contentVersion")
    private ContentVersion contentVersion;

    @JsonProperty("pageNumber")
    int pageNumber;

    @JsonProperty("parsingFilters")
    List<ParsingFilter> parsingFilters;

    @JsonProperty("returnType")
    String returnType;

    @JsonProperty("exportType")
    String exportType;

    @JsonProperty("footerSwitch")
    boolean footerSwitch;

    @JsonProperty("headerSwitch")
    boolean headerSwitch;

    @JsonProperty("requestEmail")
    String requestEmail = "";

    @JsonProperty("userSettings")
    private UserSettings userSettings;

    @JsonProperty("userInfo")
    private UserInfo userInfo;

    @JsonProperty("parsingFileInfo")
    private FileInfo parsingFileInfo;

    @JsonProperty("specialParameter")
    private String specialParameter;

    @JsonProperty("deliveryMethod")
    private String deliveryMethod;

    @JsonProperty("transactionKey")
    private String transactionKey;


    @JsonProperty("url")
    private String url;

    @JsonProperty("oAuthAccessToken")
    private OAuthAccessToken oAuthAccessToken;

    public ParsingTemplate getParsingTemplate() {
        return parsingTemplate;
    }

    public void setParsingTemplate(ParsingTemplate parsingTemplate) {
        this.parsingTemplate = parsingTemplate;
    }

    public ContentVersion getContentVersion() {
        return contentVersion;
    }

    public void setContentVersion(ContentVersion contentVersion) {
        this.contentVersion = contentVersion;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public List<ParsingFilter> getParsingFilters() {
        return parsingFilters;
    }

    public void setParsingFilters(List<ParsingFilter> parsingFilters) {
        this.parsingFilters = parsingFilters;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public boolean isHeaderSwitch() {
        return headerSwitch;
    }

    public void setHeaderSwitch(boolean headerSwitch) {
        this.headerSwitch = headerSwitch;
    }

    public boolean isFooterSwitch() {
        return footerSwitch;
    }

    public void setFooterSwitch(boolean footerSwitch) {
        this.footerSwitch = footerSwitch;
    }

    public String getRequestEmail() {
        return requestEmail;
    }

    public void setRequestEmail(String requestEmail) {
        this.requestEmail = requestEmail;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    public FileInfo getParsingFileInfo() {
        return parsingFileInfo;
    }

    public void setParsingFileInfo(FileInfo parsingFileInfo) {
        this.parsingFileInfo = parsingFileInfo;
    }

    public String getSpecialParameter() {
        return specialParameter;
    }

    public void setSpecialParameter(String specialParameter) {
        this.specialParameter = specialParameter;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getTransactionKey() {
        return transactionKey;
    }

    public void setTransactionKey(String transactionKey) {
        this.transactionKey = transactionKey;
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
