package com.parserbox.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataCacheItem {

    @JsonProperty("category")
    public String category;
    @JsonProperty("id")
    public String id;
    @JsonProperty("longDescription")
    public String longDescription;
    @JsonProperty("shortDescription")
    public String shortDescription;
    @JsonProperty("example")
    public String example;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }
}
