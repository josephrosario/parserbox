package com.parserbox.model;

import com.parserbox.model.parser.TextPositionWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RowValue {

    int rowNumber;
    StringBuffer line = new StringBuffer(255);
    Map<String, ColumnValue> columnValues = new HashMap<>();
    boolean skipRow = false;
    boolean filterTokenFound = false;
    List<TextPositionWrapper> rowPositions = new ArrayList<>();

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public StringBuffer getLine() {
        return line;
    }

    public void setLine(StringBuffer line) {
        this.line = line;
    }

    public Map<String, ColumnValue> getColumnValues() {
        return columnValues;
    }

    public void setColumnValues(Map<String, ColumnValue> columnValues) {
        this.columnValues = columnValues;
    }

    public List<TextPositionWrapper> getRowPositions() {
        return rowPositions;
    }

    public void setRowPositions(List<TextPositionWrapper> rowPositions) {
        this.rowPositions = rowPositions;
    }

    public ColumnValue setColumnValue(String id, String value) {
       ColumnValue c = getColumnValue(id);
       c.setText(value);
       return c;
    }

    public ColumnValue getColumnValue(String id) {
        ColumnValue c = this.columnValues.get(id);
        if (c == null) {
            c = new ColumnValue();
            this.columnValues.put(id, c);
        }
        return c;
    }
    public void appendToLine(String text) {
        line.append(text);
    }


    public boolean isSkipRow() {
        return skipRow;
    }

    public void setSkipRow(boolean skipRow) {
        this.skipRow = skipRow;
    }

    public boolean isFilterTokenFound() {
        return filterTokenFound;
    }

    public void setFilterTokenFound(boolean filterTokenFound) {
        this.filterTokenFound = filterTokenFound;
    }
}
