package com.parserbox.model.parser;
import com.parserbox.utils.FormulaProcessor;

import java.io.IOException;

public interface ITextStripper {

    public int getCurrentPageNumber();
    public BaseStripper getBase();
    public BaseStripper refreshBase() throws IOException;

    public void setStartPage(int pageNum);
    public void setEndPage(int pageNum);

    public String getText(Object doc) throws IOException ;

    public FormulaProcessor getFormulaProcessor();
    public void setFormulaProcessor(FormulaProcessor formulaProcessor);



}
