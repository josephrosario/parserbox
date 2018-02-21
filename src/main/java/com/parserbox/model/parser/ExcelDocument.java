package com.parserbox.model.parser;

import com.monitorjbl.xlsx.StreamingReader;
import com.parserbox.Constants;
import com.parserbox.model.ApplicationProperties;
import com.parserbox.model.SpecialAsyncTask;
import com.parserbox.model.SpecialAsyncTaskObserverAdapter;
import com.parserbox.utils.DateHelper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.*;


public class ExcelDocument extends SpecialAsyncTaskObserverAdapter {
    private Log log = LogFactory.getLog(this.getClass());
    boolean killSwitch = false;
    Map<Integer, Float> columnWidths = new HashMap<>();

    String sheetName = null;
    List<String> sheetNames = new ArrayList<>();

    String fileName;
    String fileExtension;
    String status;

    String projectId;
    ApplicationProperties applicationProperties;

    boolean xlsType = false;

    int firstRowNum = 0;
    int lastRowNum = 0;
    int totalRows = 0;
    int totalRowsProcessed = 0;

    int defaultRowsPerPage = 10;

    String convertedFilePath;

    public ExcelDocument(String fileName, String fileExtension, String projectId, ApplicationProperties applicationProperties) {
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.projectId = projectId;
        this.applicationProperties = applicationProperties;

        this.xlsType = StringUtils.endsWithIgnoreCase(fileExtension, "xls");
    }

    public double getPercentageComplete() {
        if (totalRowsProcessed == 0) return 0;
        if (totalRows == 0) return 0;
        double d = totalRowsProcessed / (double)totalRows;
        return d;
    }
    public void setCancelled() {
      setKillSwitch(true);
    }
    public void setStatus(SpecialAsyncTask.statusType statusType) {
        this.status = statusType.toString();
    }


    public boolean isKillSwitch() {
        return killSwitch;
    }

    public void setKillSwitch(boolean killSwitch) {
        this.killSwitch = killSwitch;
        log.info("Import canceled.");
    }

    public boolean isConvertedFileExists() throws IOException {
        String p = getConvertedFilePath();
        File f = new File(p);
        return f.exists();
    }

    public String getConvertedFilePath() {
        String fname = Constants.FILE_PREFIX + this.projectId + ".CSV";
        this.convertedFilePath = this.applicationProperties.getFileCacheLocation() + File.separatorChar + fname;
        return this.convertedFilePath;
    }

    public void clearFilePath() throws IOException{
        if (isConvertedFileExists()) {
            new File(getConvertedFilePath()).delete();
        }
    }

    public void loadDocument(InputStream fileData, String sheetNameParam) throws Exception {
        if (isConvertedFileExists()) {
            initConvertedFile();
            return;
        }
        clearFilePath();
        if (this.xlsType) {
            loadXLSDocument(fileData, sheetNameParam);
        } else {
            loadXLSXDocument(fileData, sheetNameParam);
        }
    }

    private void loadXLSXDocument(InputStream inputStream, String sheetNameParam) throws Exception {
        try {
            Workbook workbook = null;
            this.convertedFilePath = getConvertedFilePath();

            workbook = StreamingReader.builder()
                    .rowCacheSize(100)      // number of rows to keep in memory (defaults to 10)
                    .bufferSize(4096)       // buffer size to use when reading InputStream to file (defaults to 1024)
                    .open(inputStream);         // InputStream or File for XLSX file (required)

            if (workbook == null) {
                new RuntimeException("Could not load excel data source.");
            }
            this.sheetName = sheetNameParam;

            CSVPrinter csvFilePrinter = null;
            CSVFormat csvFileFormat = null;

            csvFileFormat = CSVFormat.DEFAULT;

            PrintStream printStream = new PrintStream(this.convertedFilePath);
            csvFilePrinter = new CSVPrinter(printStream, csvFileFormat);
            Sheet sheet = workbook.getSheet(this.sheetName);
            this.totalRows = sheet.getLastRowNum();

            int rowCnt = 0;
            for (Row row : sheet) {
                rowCnt++;
                List record = new ArrayList();
                for (int i = 0; i <= row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i);
                    record.add(getCellValue(cell));
                }
                csvFilePrinter.printRecord(record);
                totalRowsProcessed++;
                if (isKillSwitch()) break;
            }
            try {
                printStream.close();
                workbook.close();
            }
            catch (Exception e) {
                log.info(e);
            }

            try {
                if (isKillSwitch()) {
                    FileUtils.forceDelete(new File(this.convertedFilePath));
                    return;
                }
            }
            catch (Exception e) {
                log.info(e);
            }

            setFirstRowNum(0);
            setLastRowNum(rowCnt == 0 ? 0 : rowCnt-1);

        } catch(Exception e) {
            throw e;
        }
    }



    private void loadXLSDocument(InputStream inputStream, String sheetNameParam) throws Exception {
        try {
            Workbook workbook = null;
            this.convertedFilePath = getConvertedFilePath();

            workbook = WorkbookFactory.create(inputStream);
            if (workbook == null) {
                new RuntimeException("Could not load excel data source.");
            }
            this.sheetName = sheetNameParam;

            CSVPrinter csvFilePrinter = null;
            CSVFormat csvFileFormat = null;

            csvFileFormat = CSVFormat.DEFAULT;

            PrintStream printStream = new PrintStream(this.convertedFilePath);
            csvFilePrinter = new CSVPrinter(printStream, csvFileFormat);
            Sheet sheet = workbook.getSheet(this.sheetName);
            this.totalRows = sheet.getLastRowNum();
            int rowCnt = 0;
            for (Row row : sheet) {
                rowCnt++;
                List record = new ArrayList();
                for (int i = 0; i <= row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i);
                    record.add(getCellValue(cell));
                }
                csvFilePrinter.printRecord(record);
                totalRowsProcessed++;
                if (isKillSwitch()) break;
            }
            try {
                printStream.close();
                workbook.close();
            }
            catch (Exception e) {
                log.info(e);
            }

            try {
                if (isKillSwitch()) {
                    FileUtils.forceDelete(new File(this.convertedFilePath));
                }
            }
            catch (Exception e) {
                log.info(e);
            }

            setFirstRowNum(0);
            setLastRowNum(rowCnt == 0 ? 0 : rowCnt-1);

        //    initConvertedFile();
         } catch(Exception e) {
            throw e;
        }
     }

    public void initConvertedFile() throws IOException{
        int rowCnt = 0;
        Reader reader = new FileReader(this.convertedFilePath);
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        for (CSVRecord r : csvParser) {
            rowCnt++;
        }
        reader.close();
        csvParser.close();

        setFirstRowNum(0);
        setLastRowNum(rowCnt == 0 ? 0 : rowCnt-1);
    }

    public List<String> getSheetNames() throws Exception {
        return this.sheetNames;
    }
    public List<String> getSheetNames(Object fileData) throws Exception {
        InputStream istream = null;
        Workbook workbook = null;
        if (fileData instanceof InputStream) {
            istream = (InputStream)fileData;
        }
        else
        if (fileData instanceof File) {
            File file = (File) fileData;
            istream = new FileInputStream(file);
        }
        else
        if (fileData instanceof byte[]) {
            istream = new ByteArrayInputStream((byte[]) fileData);
        }
        if (this.xlsType) {
            this.convertedFilePath = getConvertedFilePath();
            workbook = WorkbookFactory.create(istream);
            for (int i = 0, len = workbook.getNumberOfSheets(); i < len; i++) {
                this.sheetNames.add(workbook.getSheetName(i));
            }
        }
        else {
            workbook = StreamingReader.builder()
                    .rowCacheSize(10)       // number of rows to keep in memory (defaults to 10)
                    .bufferSize(4096)       // buffer size to use when reading InputStream to file (defaults to 1024)
                    .open(istream);         // InputStream or File for XLSX file (required)
            for (int i = 0, len = workbook.getNumberOfSheets(); i < len; i++) {
                this.sheetNames.add(workbook.getSheetName(i));
            }
        }
        return sheetNames;
    }

    public String getCellValue(Cell cell) {
        String v = "";
        if (cell == null) return "";
        CellType cellType =  cell.getCellTypeEnum();
        if (cellType == CellType.STRING) {
            v = cell.getStringCellValue();
        }
        else if (cellType == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                Date dt = cell.getDateCellValue();
                if (dt != null) {
                    v = DateHelper.getSimpleDateStr(dt);
                }
                else {
                    v = "";
                }
            }
            else {
                v = "" + cell.getNumericCellValue();
            }
        }
        else if (cellType == CellType.BOOLEAN) {
            v = "" + cell.getBooleanCellValue();
        }
        else if (cellType == CellType.BLANK) {
            v = "";
        }
        return v;
    }

    public int getFirstRowNum() {
        return firstRowNum;
    }

    public void setFirstRowNum(int firstRowNum) {
        this.firstRowNum = firstRowNum;
    }

    public int getLastRowNum() {
        return lastRowNum;
    }

    public void setLastRowNum(int lastRowNum) {
        this.lastRowNum = lastRowNum;
    }

    public int getDefaultRowsPerPage() {
        return defaultRowsPerPage;
    }

    public void setDefaultRowsPerPage(int defaultRowsPerPage) {
        this.defaultRowsPerPage = defaultRowsPerPage;
    }

    public int getNumberOfPages() {
        double trueLastRowNumber = this.lastRowNum + 1;
        double d = (trueLastRowNumber <= 0) ? 1 : trueLastRowNumber / this.defaultRowsPerPage;
        int pages = (int)Math.ceil(d);
        return pages;
    }

    public CSVParser getCsvParser() throws IOException {
        Reader reader = new FileReader(this.convertedFilePath);
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        return csvParser;
    }

    public void close() {
        try {
        }
        catch(Exception e) {
            log.info(e);
        }
    }

}
