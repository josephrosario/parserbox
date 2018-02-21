package com.parserbox.model;

import com.parserbox.model.parser.*;
import com.parserbox.utils.FormulaProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DocumentContainer implements DocumentInterface{
    private Log log = LogFactory.getLog(this.getClass());

    long pageNumber = 1;
    PDDocument pdDoc;
    ExcelDocument excelDoc;
    ITextStripper stripper;
    String docKey;
    String docSourceType;
    String docSourceTitle;
    String docSourceExtension;
    String sheetName;
    Locale locale;

    FormulaProcessor formulaProcessor;

    boolean killLoadingSwitch = false;

    //  SpecialAsyncTask specialAsyncTask = null;

    Map<String, List<GrabberFilterValue>> currentGrabberMap = null;
    UserInfo userInfo;
    ParsingTemplate parsingTemplate;

    String error;
    Exception exception;

    public enum status {
        LOADING,
        LOADED,
        CANCELLED,
        ERROR,
        UNKNOWN;
    }
    status docStatus;
    Date loadingStarted;
    Date loadingCompleted;
    Date lastAccessed = new Date();
    ApplicationProperties applicationProperties;


    @Autowired
    private ApplicationContext context;

    @Override
    public ApplicationProperties getApplicationProperties() {
        return applicationProperties;
    }


    public DocumentContainer(ApplicationProperties applicationProperties){
        this.applicationProperties = applicationProperties;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            this.setKillLoadingSwitch(true);
        }
        catch (Exception e) {
            log.info("Could not clear internal documents", e);
        }
    }

    public boolean isKillLoadingSwitch() {
        return killLoadingSwitch;
    }

    public synchronized void setKillLoadingSwitch(boolean killLoadingSwitch) {
        try {
            if (this.excelDoc != null) {
                this.excelDoc.setKillSwitch(true);
            }
            if (this.stripper != null && this.stripper.getBase() != null) {
                this.stripper.getBase().setKillSwitch(true);
            }
            this.closeDoc();
        }
        catch (Exception e) {
            log.info(e);
        }
        this.killLoadingSwitch = killLoadingSwitch;
    }

    public void refreshBase() {
        try {
            if (stripper != null) {
                stripper.getBase().initVariables();
            }
        }
        catch (Exception e) {
            log.info(e);
        }
    }

    public void loadDocument(File file, String type) throws Exception{
        InputStream inputStream = new FileInputStream(file);
        loadDocument(inputStream, type);
    }

    public void loadDocument(ContentVersion contentVersion, String type) throws Exception{
        InputStream inputStream = new ByteArrayInputStream(contentVersion.getVersionData());
        loadDocument(inputStream, contentVersion.getFileType());
    }

    public double getPercentageComplete() {
        double d = 0;
        if (this.excelDoc != null) {
            d = this.excelDoc.getPercentageComplete();
        }
        return d;
    }

    public void loadDocument(InputStream inputStream, String type) throws Exception{
        if (type == null) return;

        try {
            setDocStatus(status.LOADING);

            type = type.toLowerCase();
            if (type.contains("pdf")) {
                this.pdDoc = PDDocument.load(inputStream);
            }
            else
            if (type.contains("xls") || type.contains("excel")) {
                this.excelDoc = new ExcelDocument(this.docSourceTitle, this.docSourceExtension,
                        this.parsingTemplate.getId(), this.applicationProperties);
                this.excelDoc.loadDocument(inputStream, this.sheetName);
            }
            setDocStatus(status.LOADED);
        }
        catch (Exception e) {
            setDocStatus(status.ERROR);
            setException(e);
            setError(e.getMessage());
        }
        finally {
            try {if (inputStream != null) inputStream.close();} catch (Exception e) {log.info(e);}
        }
    }



    public boolean isLoading() {
        return getDocStatus() == status.LOADING;
    }
    public boolean isLoaded() {
        return getDocStatus() == status.LOADED;
    }
    public boolean isError() {
        return getDocStatus() == status.ERROR;
    }

    public Object getDoc() {
        if (pdDoc != null) return pdDoc;
        if (excelDoc != null) return excelDoc;
        return null;
    }
    public long getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(long pageNumber) {
        this.pageNumber = pageNumber;
    }


    public ITextStripper getStripper() {
        return stripper;
    }

    public void setStripper(ITextStripper stripper) {
        this.stripper = stripper;
    }

    public String getDocKey() {
        return docKey;
    }

    public void setDocKey(String docKey) {
        this.docKey = docKey;
    }

    public String getDocSourceType() {
        return docSourceType;
    }

    public void setDocSourceType(String docSourceType) {
        this.docSourceType = docSourceType;
    }

    public String getDocSourceTitle() {
        return docSourceTitle;
    }

    public void setDocSourceTitle(String docSourceTitle) {
        this.docSourceTitle = docSourceTitle;
    }

    public String getDocSourceExtension() {
        return docSourceExtension;
    }

    public void setDocSourceExtension(String docSourceExtension) {
        this.docSourceExtension = docSourceExtension;
    }

    public Map<String, List<GrabberFilterValue>> getCurrentGrabberMap() {
        return currentGrabberMap;
    }

    public void setCurrentGrabberMap(Map<String, List<GrabberFilterValue>> currentGrabberMap) {
        this.currentGrabberMap = currentGrabberMap;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public Date getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(Date lastAccessed) {
        this.lastAccessed = lastAccessed;
    }


    public Date getLoadingStarted() {
        return loadingStarted;
    }

    public void setLoadingStarted(Date loadingStarted) {
        this.loadingStarted = loadingStarted;
    }

    public Date getLoadingCompleted() {
        return loadingCompleted;
    }

    public void setLoadingCompleted(Date loadingCompleted) {
        this.loadingCompleted = loadingCompleted;
    }

    public status getDocStatus() {
        return docStatus;
    }

    public void setDocStatus(status docStatus) {
        this.docStatus = docStatus;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public ParsingTemplate getParsingTemplate() {
        return parsingTemplate;
    }

    public void setParsingTemplate(ParsingTemplate parsingTemplate) {
        this.parsingTemplate = parsingTemplate;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public FileInfo getFileInfo() {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setDocumentKey__c(this.docKey);
        fileInfo.setTitle__c(this.docSourceTitle);
        fileInfo.setFileType__c(this.docSourceType);

        int pageCount = 0;
        if (pdDoc != null) {
            pageCount = pdDoc.getNumberOfPages();
        }
        else
        if (excelDoc != null) {
            pageCount = excelDoc.getNumberOfPages();
        }

        int startPage = pageCount > 0 ? 1 : 0;

        fileInfo.setPageCount__c(pageCount);
        fileInfo.setPageNumber__c(startPage);
        fileInfo.setStatus__c(getDocStatus().toString());
        return fileInfo;
    }

    public ITextStripper getNewTextStripper() throws Exception {

        ITextStripper st = null;

        if (this.pdDoc != null) {
            st = new PDFStripperImpl();
        }
        else
        if (this.excelDoc != null) {
            st =  new ExcelStripperImpl();
        }

        if (formulaProcessor == null) {
            formulaProcessor = new FormulaProcessor();
        }
        if (st != null) {
            st.setFormulaProcessor(formulaProcessor);
        }

        return st;
    }

    public int getNumberOfPages() {
        if (pdDoc != null) return pdDoc.getNumberOfPages();
        if (excelDoc != null) return excelDoc.getNumberOfPages();
        return 0;
    }

    public void closeDoc() {
        try {
            if (this.excelDoc != null) {
                this.refreshBase();
                this.excelDoc.close();
                this.excelDoc = null;
            }
            if (this.pdDoc != null) {
                this.pdDoc.close();
                this.pdDoc = null;
            }
        } catch (IOException e) {}
    }
    /*
    public SpecialAsyncTask getSpecialAsyncTask() {
        return specialAsyncTask;
    }

    public void setSpecialAsyncTask(SpecialAsyncTask specialAsyncTask) {
        this.specialAsyncTask = specialAsyncTask;
    }
    */

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
