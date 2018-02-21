package com.parserbox.model.parser;

import com.parserbox.utils.FormulaProcessor;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelStripperImpl implements ITextStripper {
    Log log = LogFactory.getLog(this.getClass());
    public ExcelDocument excel = null;
    int startPage = 0;
    int endPage = 0;
    int currentPageNumber = 0;

    float defaultFontPt = 7;

    float columnWidthAddition = 25;
    FormulaProcessor formulaProcessor;

    Map<Integer, Float> columnWidthsMap = new HashMap<>();

    BaseStripper base = new BaseStripper(this);

    public ExcelStripperImpl() throws IOException {
         super();
    }

    public BaseStripper refreshBase() throws IOException {
        this.base = new BaseStripper(this);
        return this.base;
    }

    public BaseStripper getBase() {return base;}

    @Override
    public FormulaProcessor getFormulaProcessor() {
        return formulaProcessor;
    }

    @Override
    public void setFormulaProcessor(FormulaProcessor formulaProcessor) {
        this.formulaProcessor = formulaProcessor;
    }

    public String getText(Object doc) throws IOException{
        this.excel = (ExcelDocument)doc;
        this.processExcelPages();
        base.processColumns();
        return "";
    }

    public void processExcelPages() throws IOException {
        CSVParser parser = null;

        try {
            parser = this.excel.getCsvParser();

            int startPage = getStartPage();
            int endPage = getEndPage();

            int totalPages = excel.getNumberOfPages();
            if (endPage > totalPages) endPage = totalPages;

            int startRow = excel.getFirstRowNum() + ((startPage - 1) * excel.getDefaultRowsPerPage());

            int endRow = (endPage >= totalPages)
                    ? excel.getLastRowNum()
                    : excel.getFirstRowNum() + (endPage * excel.getDefaultRowsPerPage()-1);

            if (startRow > excel.getLastRowNum()) startRow = excel.getLastRowNum();
            if (endRow > excel.getLastRowNum()) endRow = excel.getLastRowNum();

            int rowCnt = 0;
            List<List<TextPositionWrapper>> rowPositions = new ArrayList<>();
            int rw = 0;
            for (CSVRecord row : parser) {
                rw++;
                if (rw <= startRow) continue;

                rowCnt++;
                if (rowCnt > excel.getDefaultRowsPerPage()) {
                    base.writePageEnd();
                    rowCnt = 1;
                }
                if (rowCnt == 1) {
                    if (currentPageNumber == 0) {
                        currentPageNumber = startPage;
                    } else {
                        currentPageNumber++;
                    }
                    base.writePageStart();
                }
                float colWidthTot = 1;
                for (String cell : row) {
                    float colWidth = 90;
                    String v = StringUtils.trimToEmpty(cell);

                    float x = colWidthTot;
                    float y = rowCnt * (25);
                    TextPositionWrapper t =
                            new TextPositionWrapper(x, y, v, defaultFontPt, true, colWidth);
                    base.writeString(v, t);
                    colWidthTot += colWidth + 9;
                 }
                base.writeLineSeparator();
                if (base.isKillSwitch()) break;

                if (rw > endRow) break;
            }
            base.writePageEnd();

         }
        catch (IOException e) {
            throw e;
        }
        finally {
            try {
                if (parser != null) {
                    parser.close();
                }
            } catch (Exception closeException) {
                log.info(closeException);
            }
        }
    }

    @Override
    public int getCurrentPageNumber() {
        return currentPageNumber;
    }

    public void setCurrentPageNumber(int currentPageNumber) {
        this.currentPageNumber = currentPageNumber;
    }

    public ExcelDocument getExcel() {
        return excel;
    }

    public void setExcel(ExcelDocument excel) {
        this.excel = excel;
    }

    public int getStartPage() {
        return startPage;
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public void setEndPage(int endPage) {
        this.endPage = endPage;
    }

    public void setBase(BaseStripper base) {
        this.base = base;
    }

}
