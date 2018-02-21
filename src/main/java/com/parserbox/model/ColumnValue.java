package com.parserbox.model;

import com.parserbox.model.parser.TextPositionWrapper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ColumnValue {

    String text = "";
    String textOriginal = "";
    double formulaValue;
    boolean formulaHandled;

    List<TextPositionWrapper> textPositions = new ArrayList<>();

    double columnWidth;
    double columnLeft;

    public ColumnValue(){}

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void appendText(TextPositionWrapper t) {
        String code = t.getUnicode();
        if (StringUtils.isBlank(code)) {
            code = " ";
        }
        this.text += t.getUnicode();
        this.textOriginal += t.getUnicode();
        textPositions.add(t);
    }

    public String getTextOriginal() {
        return textOriginal;
    }

    public void setTextOriginal(String textOriginal) {
        this.textOriginal = textOriginal;
    }


    public double getFormulaValue() {
        return formulaValue;
    }

    public void setFormulaValue(double formulaValue) {
        this.formulaValue = formulaValue;
    }

    public boolean isFormulaHandled() {
        return formulaHandled;
    }

    public void setFormulaHandled(boolean formulaHandled) {
        this.formulaHandled = formulaHandled;
    }

    public List<TextPositionWrapper> getTextPositions() {
        return textPositions;
    }

    public void setTextPositions(List<TextPositionWrapper> textPositions) {
        this.textPositions = textPositions;
    }

    public TextPositionWrapper getTextPosition(int location) {
        if (textPositions.size() > location) {
            return textPositions.get(location);
        }
        return null;
    }

    public double getColumnWidth() {
        return columnWidth;
    }

    public void setColumnWidth(double columnWidth) {
        this.columnWidth = columnWidth;
    }

    public double getColumnLeft() {
        return columnLeft;
    }

    public void setColumnLeft(double columnLeft) {
        this.columnLeft = columnLeft;
    }
}
