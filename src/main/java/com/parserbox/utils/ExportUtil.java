package com.parserbox.utils;

import com.parserbox.model.*;
import com.parserbox.model.parser.ITextStripper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class ExportUtil extends SpecialAsyncTaskObserverAdapter {

    Log log = LogFactory.getLog(this.getClass());
    List<ParsingFilter> parsingFilters = new ArrayList<>();

    Map<String, ParsingFilter> parsingFiltersMap = new HashMap<>();
    Map<String, ParsingFilter> parsingFiltersWithFormattingOutputMap = new HashMap<>();


    int pagesToProcess = 255;
    boolean killProcess = false;
    String status;

    String defaultDatePattern = "MM/dd/yyyy";
    int logControlBreak = 0;

    public DocumentContainer documentContainer;
    public String path;
    public boolean includeHeader;

    public int totalPagesProcessed;
    public Date timeBreak = new Date();

    public ExportUtil(DocumentContainer documentContainer, String path) {
        this(documentContainer, path, true);
    }

    public ExportUtil(DocumentContainer documentContainer, String path, boolean includeHeader) {
        this.documentContainer = documentContainer;

        this.parsingFilters =
                documentContainer.getStripper().getBase().getParsingFilters();
        this.parsingFiltersMap =
                documentContainer.getStripper().getBase().getParsingFiltersMap();

        this.parsingFiltersWithFormattingOutputMap =
                documentContainer.getStripper().getBase().getParsingFiltersWithFormattingOutputMap();


        this.path = path;
        this.includeHeader = includeHeader;
        documentContainer.setCurrentGrabberMap(null);
    }

    public void setFormattersCache() {

    }


    public double getPercentageComplete() {
        int totalPages = documentContainer.getNumberOfPages();
        if (totalPagesProcessed == 0) return 0;
        if (totalPages == 0) return 0;
        double d = totalPagesProcessed / (double)totalPages;
        return d;
    }
    public void setCancelled() {
        this.killProcess = true;
        log.info("Export cancelled");
    }
    public void setStatus(SpecialAsyncTask.statusType statusType) {
        this.status = statusType.toString();
    }


    public void startExport(String exportType) throws Exception {
        log.info("Starting export task.");
        switch (exportType) {
            case "TAB":
                exportDelimited(exportType);
                break;
            case "CSV":
                exportDelimited(exportType);
                break;
            case "EXCEL":
            default:
                exportExcel();
                break;
        }
        log.info("Export task completed.");

    }

    /**
     * The header row is returned.
     *
     * @return The header row as a String[]
     */
    public String[] getHeaderRow() {
        int mLen = 0;
        for (ParsingFilter p : this.parsingFilters) {
            if (p.isIncludeInExport__c()) {
                mLen++;
            }
        }
        if (mLen == 0) return null;
        String[] headerRow = new String[mLen];
        for (int i = 0; i < (mLen); i++) {
            headerRow[i] = new String();
        }
        int pCnt = 0;
        for (ParsingFilter p : this.parsingFilters) {
            if (p.isIncludeInExport__c()) {
                headerRow[pCnt++] = p.getName();
            }
        }
        return headerRow;
    }

    /**
     * The parsed page is exported as an excel document.
     *
     * @throws Exception
     */
    public void exportExcel() throws Exception {
        try {

            Map<ParsingFilter, CellStyle> cellStyleMap = new HashMap<>();

            Workbook workbook = new SXSSFWorkbook(100); // keep 100 rows in memory, exceeding rows will be flushed to disk
            SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet();
            CreationHelper createHelper = workbook.getCreationHelper();

            sheet.trackAllColumnsForAutoSizing();
            int rowCount = 0;
            String[] headerRow = includeHeader ? getHeaderRow() : null;
            if (headerRow != null) {
                Row row = sheet.createRow(rowCount++);
                int columnCount = 0;
                for (int i = 0, len = headerRow.length; i < len; i++) {
                    Cell cell = row.createCell(columnCount++);
                    cell.setCellValue(headerRow[i]);
                }
            }

            DecimalFormat df = new DecimalFormat("#,###.00");
            Map<String, Double> colTotalsMap = new HashMap<>();
            for (ParsingFilter p : this.parsingFilters) {
                if (p.getDataType__c() != null) {
                    if (p.getDataType__c().equalsIgnoreCase("number") ||
                            p.getDataType__c().equalsIgnoreCase("currency") ||
                            p.getDataType__c().equalsIgnoreCase("formula")) {
                        colTotalsMap.put(p.getId(), 0.0);
                    }
                }
            }


            int totalPages = documentContainer.getNumberOfPages();
            List<RowValue> rows = new ArrayList<>();
            for (int startPage = 1; startPage <= totalPages; startPage += this.pagesToProcess) {
                int lastPage = startPage + this.pagesToProcess - 1;
                if (lastPage > totalPages) lastPage = totalPages;

                rows = getRowValues(startPage, lastPage);

                for (RowValue rowValue : rows) {
                    if (rowValue.isSkipRow()) continue;
                    Map<String, ColumnValue> columnValues = rowValue.getColumnValues();
                    Row row = sheet.createRow(rowCount++);
                    int columnCount = 0;
                    for (ParsingFilter p : this.parsingFilters) {
                        if (p.isIncludeInExport__c() == false) continue;

                        String value = "";
                        ColumnValue columnValue = columnValues.get(p.getId());
                        if (columnValue != null) {
                            value = columnValue.getText();
                        }
                        Cell cell = row.createCell(columnCount++);
                        String dataType = StringUtils.trimToEmpty(p.getDataType__c()).toLowerCase();
                        if (StringUtils.isNotBlank(value)) {
                            value = value.trim();
                            if (dataType.equals("number") || dataType.equals("currency")) {
                                double d = NumberHelper.getCleanNumber(value, documentContainer.getLocale()).doubleValue();
                                cell.setCellValue(d);
                                double dTotal = colTotalsMap.get(p.getId());
                                dTotal += d;
                                colTotalsMap.put(p.getId(), dTotal);
                            }

                            else if (dataType.equals("date")) {
                                try {
                                    Date date = DateHelper.getDate(value);
                                    if (date != null) {

                                        String pattern = p.isFormattingOutput() ? StringUtils.trimToEmpty(p.getFormattingPatternOutput__c()) : defaultDatePattern;
                                        pattern = StringUtils.remove(pattern, "'");
                                        pattern = StringUtils.remove(pattern, '"');

                                        CellStyle cellStyle = cellStyleMap.get(p);
                                        if (cellStyle == null) {
                                            cellStyle = workbook.createCellStyle();
                                            cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(pattern));
                                            cellStyleMap.put(p, cellStyle);
                                        }
                                        cell.setCellValue(date);
                                        cell.setCellStyle(cellStyle);
                                    }

                                } catch (Exception e) {
                                    cell.setCellValue(e.getMessage());
                                }
                            }

                            else if (dataType.equals("formula")) {
                                if (StringUtils.isNumeric(value)) {
                                    double d = Double.valueOf(value);
                                    cell.setCellValue(d);

                                    double dTotal = colTotalsMap.get(p.getId());
                                    dTotal += d;
                                    colTotalsMap.put(p.getId(), dTotal);
                                } else {
                                    cell.setCellValue(value);
                                }
                            }
                            else {
                                cell.setCellValue(value);
                            }
                        }
                    }
                }
            }

            Row row = sheet.createRow(rowCount++);

            int columnCount = 0;
            for (ParsingFilter p : this.parsingFilters) {
                if (p.isIncludeInExport__c() == false) continue;
                sheet.autoSizeColumn(columnCount);
                Cell cell = row.createCell(columnCount++);
                if (colTotalsMap.containsKey(p.getId())) {
                    //double d = colTotalsMap.get(p.getId());
                    //cell.setCellValue(d);
                    String charStr = getExcelColumnName(columnCount);
                    String formulaStr = "SUM("+charStr + (includeHeader ? 2 : 1) + ":" + charStr + (rowCount-1) + ")";
                    cell.setCellType(CellType.FORMULA);
                    cell.setCellFormula(formulaStr);
                }
            }

            FileOutputStream fout = new FileOutputStream(path);
           // workbook.setForceFormulaRecalculation(true);
            workbook.write(fout);
            fout.close();
        } catch (Exception e) {
            throw e;
        }
    }

    public String getExcelColumnName (int columnNumber)
    {
        int dividend = columnNumber;
        int i;
        String columnName = "";
        int mod;
        while (dividend > 0)
        {
            mod = (dividend - 1) % 26;
            i = 65 + mod;
            columnName = new Character((char)i).toString() + columnName;
            dividend = (int)((dividend - mod) / 26);
        }
        return columnName;
    }

    /**
     * The parsed page is exported as a delimited file (tab or csv).
     *
     * @param exportType
     * @throws Exception
     */
    public void exportDelimited(String exportType) throws Exception {
        FileWriter w = null;
        CSVPrinter csvFilePrinter = null;
        CSVFormat csvFileFormat = null;

        String[] headerRow = includeHeader ? getHeaderRow() : null;

        if (StringUtils.equalsIgnoreCase(exportType, "CSV")) {
            if (headerRow != null) {
                csvFileFormat = CSVFormat.DEFAULT.withHeader(headerRow);
            } else {
                csvFileFormat = CSVFormat.DEFAULT;
            }

        } else {
            if (headerRow != null) {
                csvFileFormat = CSVFormat.TDF.withHeader(headerRow);
            } else {
                csvFileFormat = CSVFormat.TDF;
            }
        }
        w = new FileWriter(path);
        csvFilePrinter = new CSVPrinter(w, csvFileFormat);

        int totalPages = documentContainer.getNumberOfPages();
        List<RowValue> rows = new ArrayList<>();
        for (int startPage = 1; startPage <= totalPages; startPage += this.pagesToProcess) {
            int lastPage = startPage + this.pagesToProcess - 1;
            if (lastPage > totalPages) lastPage = totalPages;

            rows = getRowValues(startPage, lastPage);

            for (RowValue rowValue : rows) {
                if (rowValue.isSkipRow()) continue;

                Map<String, ColumnValue> columnValues = rowValue.getColumnValues();
                StringBuffer buffer = new StringBuffer(500);
                List record = new ArrayList();

                for (ParsingFilter p : this.parsingFilters) {
                    if (p.isIncludeInExport__c() == false) continue;
                    String dataType = StringUtils.trimToEmpty(p.getDataType__c()).toLowerCase();
                    String value = "";
                    ColumnValue columnValue = columnValues.get(p.getId());
                    if (columnValue != null) {
                        value = columnValue.getText();
                        if (dataType.equals("date")) {
                            try {
                                Date date = DateHelper.getDate(value);
                                if (date != null) {
                                    String pattern = p.isFormattingOutput() ? StringUtils.trimToEmpty(p.getFormattingPatternOutput__c()) : defaultDatePattern;
                                    value = DateFormatUtils.format(date, pattern);
                                }

                            } catch (Exception e) {}
                        }
                        value = StringUtils.trimToEmpty(value);
                        buffer.append(value);
                    }
                    record.add(value);
                }
                if (StringUtils.isNotBlank(buffer.toString())) {
                    csvFilePrinter.printRecord(record);
                }
            }
        }
        w.flush();
        w.close();
        csvFilePrinter.close();
    }

    /**
     * Returns totals as an Json string for numeric and currency filters.
     * @return Json String
     */
    public String getTotalsAsJson() throws IOException {
        DecimalFormat df = new DecimalFormat("#,###.00");

        Map<String, Double> colTotalsMap = getTotalsMap();

        if (colTotalsMap.size() == 0) {
            return "There are no numeric or currency markers.";
        }

        String newline = "\n";
        StringBuffer html = new StringBuffer(1024);

        JSONArray jsonArray = new JSONArray();

        for (String id : colTotalsMap.keySet()) {
            double d = colTotalsMap.get(id);
            ParsingFilter p = this.parsingFiltersMap.get(id);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Id", id);
            jsonObject.put("Name", p.getName());
            jsonObject.put("Value", d);
            jsonArray.add(jsonObject);
        }

        return jsonArray.toJSONString();
    }

    /**
     * Returns totals as an HTML string for numeric and currency filters.
     * @return HTML String
     */
    public String getTotalsAsHTML() throws IOException {
        DecimalFormat df = new DecimalFormat("#,###.00");

        Map<String, Double> colTotalsMap = getTotalsMap();

        if (colTotalsMap.size() == 0) {
            return "There are no numeric or currency markers.";
        }

        String newline = "\n";
        StringBuffer html = new StringBuffer(1024);

        html.append("<table>" + newline);

        for (String id : colTotalsMap.keySet()) {
            double d = colTotalsMap.get(id);
            ParsingFilter p = this.parsingFiltersMap.get(id);
            html.append("<tr>" + newline);
            html.append("    <td><b>" + p.getName() + "</b></td>" + newline);
            html.append("    <td>&nbsp;&nbsp;&nbsp;</td>" + newline);
            html.append("    <td style='text-align: right;'>" + df.format(d) + "</td>" + newline);
            html.append("</tr>" + newline);
        }
        html.append("</table>" + newline);

        return html.toString();
    }

    /**
     * Returns totals Map for numeric and currency filters.
     * @return Map
     */
    public  Map<String, Double> getTotalsMap() throws IOException {
        DecimalFormat df = new DecimalFormat("#,###.00");
        Map<String, Double> colTotalsMap = new HashMap<>();

        for (ParsingFilter p : this.parsingFilters) {
            if (p.getDataType__c() != null) {
                if (p.getDataType__c().equalsIgnoreCase("number") ||
                        p.getDataType__c().equalsIgnoreCase("currency") ||
                        p.getDataType__c().equalsIgnoreCase("formula")) {
                    colTotalsMap.put(p.getId(), 0.0);
                }
            }
        }

        if (colTotalsMap.size() == 0) {
            return colTotalsMap;
        }

        int totalPages = documentContainer.getNumberOfPages();

        List<RowValue> rows = new ArrayList<>();

        for (int startPage = 1; startPage <= totalPages; startPage += this.pagesToProcess) {
            int lastPage = startPage + this.pagesToProcess - 1;
            if (lastPage > totalPages) lastPage = totalPages;
            rows = getRowValues(startPage, lastPage);
            for (RowValue rowValue : rows) {
                if (rowValue.isSkipRow()) continue;

                Map<String, ColumnValue> columnValues = rowValue.getColumnValues();
                String str = "";
                for (String id : colTotalsMap.keySet()) {
                    double d = colTotalsMap.get(id);
                    ColumnValue columnValue = columnValues.get(id);
                    if (columnValue != null) {
                        str = columnValue.getText();
                        try {
                            if (StringUtils.isNotBlank(str)) {
                                d += NumberHelper.getCleanNumber(str, documentContainer.getLocale()).doubleValue();
                                colTotalsMap.put(id, d);
                            }
                        } catch (Exception e) {
                            log.info(e);
                        }
                    }
                }
            }
        }
        return colTotalsMap;
    }



    public List<RowValue> getRowValues(int startPage, int lastPage) throws IOException {

        if (this.killProcess) {
            if (StringUtils.isNotBlank(status)) {
                throw new RuntimeException(status);
            } else {
                throw new RuntimeException("Transaction was stopped.");
            }
        }

        ITextStripper stripper = documentContainer.getStripper();
        stripper.getBase().initVariables();

        List<RowValue> rows = stripper.getBase().getRows();
        int totalPages = documentContainer.getNumberOfPages();
        if (lastPage > totalPages) {
            lastPage = totalPages;
        }
        stripper.setStartPage(startPage);
        stripper.setEndPage(lastPage);

        stripper.getText(documentContainer.getDoc());

        //if (documentContainer.getApplicationProperties() != null) {
        //   if (documentContainer.getApplicationProperties().isDebug()) {
        logControlBreak++;
        if (logControlBreak == 1 || lastPage == totalPages) {
            logControlBreak = 0;
            long t = (new Date().getTime() - timeBreak.getTime()) / 1000;
            log.info(lastPage + " pages processed (" + t + " seconds) for export " + documentContainer.getDocKey());
        }
        //     }
        // }

        this.totalPagesProcessed = lastPage;
        return stripper.getBase().getRows();
    }


}





