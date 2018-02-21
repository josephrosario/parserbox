package com.parserbox.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContentVersion {

    @JsonProperty("Id")
    private String Id;
    @JsonProperty("Title")
    private String Title;
    @JsonProperty("VersionData")
    private byte[] versionData;
    @JsonProperty("FileType")
    private String FileType;
    @JsonProperty("FileExtension")
    private String FileExtension;


    public ContentVersion() {
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public byte[] getVersionData() {
        return versionData;
    }

    public void setVersionData(byte[] versionData) {
        this.versionData = versionData;
    }

    public String getFileType() {
        return FileType;
    }

    public void setFileType(String fileType) {
        FileType = fileType;
    }

    public String getFileExtension() {
        return FileExtension;
    }

    public void setFileExtension(String fileExtension) {
        FileExtension = fileExtension;
    }
}
