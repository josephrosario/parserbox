package com.parserbox.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileInfo {

    @JsonProperty("Title__c")
    String Title__c;
    @JsonProperty("DocumentKey__c")
    String DocumentKey__c;
    @JsonProperty("Error__c")
    String Error__c;
    @JsonProperty("FileType__c")
    private String FileType__c;
    @JsonProperty("Message__c")
    String Message__c;
    @JsonProperty("PageCount__c")
    int PageCount__c;
    @JsonProperty("PageNumber__c")
    int PageNumber__c;
    @JsonProperty("Status__c")
    String Status__c;

    public String getTitle__c() {
        return Title__c;
    }

    public void setTitle__c(String title__c) {
        Title__c = title__c;
    }

    public String getDocumentKey__c() {
        return DocumentKey__c;
    }

    public void setDocumentKey__c(String documentKey__c) {
        DocumentKey__c = documentKey__c;
    }

    public String getError__c() {
        return Error__c;
    }

    public void setError__c(String error__c) {
        Error__c = error__c;
    }

    public String getFileType__c() {
        return FileType__c;
    }

    public void setFileType__c(String fileType__c) {
        FileType__c = fileType__c;
    }

    public String getMessage__c() {
        return Message__c;
    }

    public void setMessage__c(String message__c) {
        Message__c = message__c;
    }

    public int getPageCount__c() {
        return PageCount__c;
    }

    public void setPageCount__c(int pageCount__c) {
        PageCount__c = pageCount__c;
    }

    public int getPageNumber__c() {
        return PageNumber__c;
    }

    public void setPageNumber__c(int pageNumber__c) {
        PageNumber__c = pageNumber__c;
    }

    public String getStatus__c() {
        return Status__c;
    }

    public void setStatus__c(String status__c) {
        Status__c = status__c;
    }

}
