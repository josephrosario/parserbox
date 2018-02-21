package com.parserbox.model.parser;

import com.parserbox.utils.FormulaProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class extends key methods to handle more complex parsing features.
 * The primary processing is handled in the BaseStripper class.
 * @see BaseStripper
 * @see org.apache.pdfbox.text.PDFTextStripper
 */
public class PDFStripperImpl extends PDFTextStripper implements ITextStripper {
    Log log = LogFactory.getLog(this.getClass());
    FormulaProcessor formulaProcessor;

    BaseStripper base = new BaseStripper(this);

    /**
     * Constructor
     * @throws IOException
     */
    public PDFStripperImpl() throws IOException {
        super();
        this.setSortByPosition(true);

    }

    /**
     * Getter for the base text stripper class
     * @return BaseStripper
     */
    public BaseStripper getBase() {return base;}

    /**
     * Creates a new instances of the base text stripper object
     * @return BaseStripper
     * @throws IOException
     */
    public BaseStripper refreshBase() throws IOException {
        this.base = new BaseStripper(this);
        return this.base;
    }

    /**
     * Getter for the forumula processor
     * @return FormulaProcessor
     */
    public FormulaProcessor getFormulaProcessor() {
        return formulaProcessor;
    }

    /**
     * Setter for the formula processor
     */
    @Override
    public void setFormulaProcessor(FormulaProcessor formulaProcessor) {
        this.formulaProcessor = formulaProcessor;
    }

    /**
     * Sets the starting page number in the parent
     * @param pageNum
     */
    public void setStartPage(int pageNum) {
        super.setStartPage(pageNum);
    }

    /**
     * Sets the ending page number in the parent
     * @param pageNum
     */
    public void setEndPage(int pageNum) {
        super.setEndPage(pageNum);
    }

    /**
     * Calls the base stripper class text processing method
     * @param doc
     * @return String
     * @throws IOException
     */
    public String getText(Object doc) throws IOException {
        String text = super.getText((PDDocument)doc);
        base.processColumns();
        return text;
    }

    /**
     * Getter for the page number
     * @return int
     */
    public int getCurrentPageNumber(){
        return this.getCurrentPageNo();
    }

    /**
     * Getter for the text positions as a list
     * @param textPositions
     * @return List
     * @throws IOException
     */
    List<TextPositionWrapper> getTextPositionWrappers(List<TextPosition> textPositions) throws IOException {
        List<TextPositionWrapper> list = new ArrayList<>();
        getCurrentPageNo();
        if (textPositions != null || textPositions.size() > 0) {
            for (TextPosition p : textPositions) {
                TextPositionWrapper w = new TextPositionWrapper(
                        p.getXDirAdj(), p.getYDirAdj(), p.getUnicode(), p.getFontSizeInPt());
                list.add(w);
            }
        }
        return list;
    }

    /**
     * Method override to handle writing of a string.
     *
     * @param text          The text to write to the stream.
     * @param textPositions The TextPositions belonging to the text.
     * @throws IOException If there is an error when writing the text.
     */
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        base.writeString(text, getTextPositionWrappers(textPositions));
        super.writeString(text, textPositions);
    }

    /**
     * Method override to handle Line Separator processing.
     *
     * @throws IOException If there is an error when writing the text.
     */
    protected void writeLineSeparator() throws IOException {
        base.writeLineSeparator();
        super.writeLineSeparator();
    }

    /**
     * Method override to handle Start of Page processing.
     *
     * @throws IOException if something went wrong
     */
    protected void writePageStart() throws IOException {
        base.writePageStart();
        super.writePageStart();
    }

    /**
     * Method override to handle End of Page processing.
     *
     * @throws IOException If there is an error when writing the text.
     */
    protected void writePageEnd() throws IOException {
        base.writePageEnd();
        super.writePageEnd();

    }

}
