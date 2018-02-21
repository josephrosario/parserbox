package com.parserbox.model;

public class GrabberFilterValue {
    private String name = "";
    private String value = "";
    private boolean skip = false;
    private RowValue row;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public RowValue getRow() {
        return row;
    }

    public void setRow(RowValue row) {
        this.row = row;
    }
}
