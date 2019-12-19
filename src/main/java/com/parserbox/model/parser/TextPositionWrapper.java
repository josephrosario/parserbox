package com.parserbox.model.parser;

import org.apache.pdfbox.text.TextPosition;

public class TextPositionWrapper {

    float XDirAdj;
    float YDirAdj;
    float fontSizeInPt;
    String unicode;

    boolean textBoxMode = false;
    float widthInPixels;
    TextPosition textPosition;

    public TextPositionWrapper(float X, float Y,
                               String unicode,
                               float fontSizeInPt) {
        this.XDirAdj = X;
        this.YDirAdj = Y;
        this.unicode = unicode;
        this.fontSizeInPt = fontSizeInPt;
     }

    public TextPositionWrapper(float X, float Y,
                               String unicode,
                               float fontSizeInPt,
                               boolean textBoxMode,
                               float widthInPixels) {
        this.XDirAdj = X;
        this.YDirAdj = Y;
        this.unicode = unicode;
        this.fontSizeInPt = fontSizeInPt;
        this.textBoxMode = textBoxMode;
        this.widthInPixels = widthInPixels;
    }

    public float getXDirAdj() {
        return XDirAdj;
    }

    public void setXDirAdj(float XDirAdj) {
        this.XDirAdj = XDirAdj;
    }

    public float getYDirAdj() {
        return YDirAdj;
    }

    public void setYDirAdj(float YDirAdj) {
        this.YDirAdj = YDirAdj;
    }

    public float getFontSizeInPt() {
        return fontSizeInPt;
    }

    public void setFontSizeInPt(float fontSizeInPt) {
        this.fontSizeInPt = fontSizeInPt;
    }

    public String getUnicode() {
        return unicode;
    }

    public void setUnicode(String unicode) {
        this.unicode = unicode;
    }

    public boolean isTextBoxMode() {
        return textBoxMode;
    }

    public void setTextBoxMode(boolean textBoxMode) {
        this.textBoxMode = textBoxMode;
    }

    public float getWidthInPixels() {
        return widthInPixels;
    }

    public void setWidthInPixels(float widthInPixels) {
        this.widthInPixels = widthInPixels;
    }

    public TextPosition getTextPosition() {
        return textPosition;
    }

    public void setTextPosition(TextPosition textPosition) {
        this.textPosition = textPosition;
    }
}
