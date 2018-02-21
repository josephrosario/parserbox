package com.parserbox.controller;


import com.parserbox.Constants;
import com.parserbox.model.*;
import com.parserbox.sevice.UploadServiceImpl;
import com.parserbox.utils.*;
import com.parserbox.model.parser.ExcelDocument;
import com.parserbox.model.parser.ITextStripper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;


/**
 * Servlet implementation class JSONServlet
 */
@RestController
@RequestMapping("/filemapper")
public class FileMapperController {
    private static final long serialVersionUID = 1L;
    private static int cnt = 0;
    static Map<String, DocumentContainer> documentCache = new Hashtable<>();

    static Stack<SpecialAsyncTask> specialPendingTaskStack = new Stack<>();
    static Stack<SpecialAsyncTask> specialProcessingTaskStack = new Stack<>();

    private Log log = LogFactory.getLog(this.getClass());
    static boolean monitorStarted = false;



    @Autowired
    ApplicationProperties applicationProperties;

    @Autowired
    UploadServiceImpl uploadService;

    public void startDebugThread() {
        log.info("Starting debug thread...");
        (new Thread() {
            public void run() {
                try {
                    do {
                        Runtime r = Runtime.getRuntime();
                        r.gc();
                        log.info("Memory used = " + (r.totalMemory() - r.freeMemory()) / 1000000);
                        Thread.sleep(2000);
                        log.info("Total documents in cache: " + documentCache.size());
                    } while (true);

                } catch (RuntimeException e) {
                    log.info(e);

                } catch (InterruptedException e) {
                }
            }
        }).start();
    }

    public void setSpecialBackGroundTask(SpecialAsyncTask task) {
        specialPendingTaskStack.push(task);
    }

    public SpecialAsyncTask getAsyncTaskProcessingOrPending(String transaction, String taskid) {
        SpecialAsyncTask task = getAsyncTaskProcessing(transaction, taskid);
        if (task != null) return task;

        task = getAsyncTaskPending(transaction, taskid);
        return task;
    }
    public SpecialAsyncTask getAsyncTaskPending(String transaction, String taskid) {
        return getAsyncTask(transaction, taskid, specialPendingTaskStack);
    }
    public SpecialAsyncTask getAsyncTaskProcessing(String transaction, String taskid) {
        return getAsyncTask(transaction, taskid, specialProcessingTaskStack);
    }
    public SpecialAsyncTask getAsyncTask(String transaction, String taskid, Stack<SpecialAsyncTask> stack) {
        if (transaction == null || taskid == null) return null;
        if (stack == null || stack.empty()) return null;
        for (SpecialAsyncTask task : stack) {
            if (task.getTaskId() == null || task.getTransaction() == null) continue;
            if (task.getTaskId().equals(taskid)) {
                if (task.getTransaction().equals(task.getTransaction())) {
                    return task;
                }
            }
        }
        return null;
    }
    public void startSpecialBackgroundTasksThread() {
        log.info("Starting special background tasks thread...");

       (new Thread() {
            public void run() {
                try {
                    Runtime r = Runtime.getRuntime();
                    int maxBackgroundTasks = applicationProperties.getMaxBackgroundTasks();
                    List<SpecialAsyncTask> tasksToPurge = new ArrayList<>();
                    int loggingCounter = 0;
                    do {
                        try {

                            tasksToPurge.clear();

                            if (specialPendingTaskStack.size() > 0) {
                                if (specialProcessingTaskStack.size() < maxBackgroundTasks) {
                                    // Pop and start the next task from the pending stack and push to the processing stack.
                                    log.info("Total special pending tasks on stack " + specialPendingTaskStack.size());
                                    SpecialAsyncTask task = specialPendingTaskStack.pop();
                                    if (task.isOkToPurge()) {
                                        continue;
                                    }
                                    specialProcessingTaskStack.push(task);
                                    task.setStatus(SpecialAsyncTask.statusType.PROCESSING);
                                    task.start();
                                } else {
                                    if (++loggingCounter > 10) {
                                        loggingCounter = 0;
                                        log.info("Waiting for special tasks to complete...");
                                        log.info("Maximum concurrent tasks allowed: " + maxBackgroundTasks);
                                        log.info("Tasks in queue: " + specialPendingTaskStack.size());
                                        log.info("Tasks in progress: " + specialProcessingTaskStack.size());
                                    }
                                }
                            }

                            // Check the processing stack to see what should be purged
                            for (SpecialAsyncTask task : specialProcessingTaskStack) {
                                if (task.isOkToPurge()) {
                                    tasksToPurge.add(task);
                                    continue;
                                }
                                if (task.getCompleted() != null && ! task.isAlive()) {
                                    tasksToPurge.add(task);
                                    continue;
                                }
                            }

                            if (tasksToPurge.size() > 0) {
                                for (SpecialAsyncTask task : tasksToPurge) {
                                    if (specialPendingTaskStack.size() > 0) {
                                        if (specialPendingTaskStack.contains(task)) {
                                            specialPendingTaskStack.remove(task);
                                        }
                                    }
                                    if (specialProcessingTaskStack.size() > 0) {
                                        if (specialProcessingTaskStack.contains(task)) {
                                            specialProcessingTaskStack.remove(task);
                                        }
                                    }
                                }
                                r.gc();
                                log.info("specialPendingTaskStack: " + specialPendingTaskStack.size() + "  " +
                                        "specialProcessingTaskStack: " + specialProcessingTaskStack.size());
                                log.info("Memory in use = " + (r.totalMemory() - r.freeMemory()) / 1000000 + "MB");
                            }
                        }
                        catch(Exception e) {
                            log.info(e);
                        }
                        Thread.sleep(1000);

                    } while (true);

                } catch (RuntimeException e) {
                    log.info(e);

                } catch (InterruptedException e) {
                }
            }
        }).start();
    }

    public void loadDataCaches() {
        try {
            FormulaProcessor.loadSupportedFunctions();
        }
        catch (Exception e) {

            log.info("Could not load data caches.", e);
        }
    }

    @PostConstruct
    public void init() {

        if (monitorStarted == false) {
            monitorStarted = true;

            if (applicationProperties.isDebug()) {
                startDebugThread();
            }
            startSpecialBackgroundTasksThread();

            // Create a daemon thread to monitor cache
            log.info("Starting document container cache monitoring thread...");
            (new Thread() {
                Date cacheDate = null;
                long dateDif = 0;
                long documentTimeOut = 0;
                long DEFAULT_DOCUMENT_TIMEOUT = 15; // The default timeout in minutes.
                DocumentContainer container = null;

                public void run() {
                    try {
                        List<String> docsToPurge = new ArrayList<>();

                        do {
                            // Check the document cache to see if we have any orphaned documents
                            documentTimeOut = applicationProperties.getDocumentTimeOut();
                            if (documentTimeOut <= 0) documentTimeOut = DEFAULT_DOCUMENT_TIMEOUT;
                            Date now = new Date();

                            docsToPurge.clear();

                            for (String key : documentCache.keySet()) {

                                container = documentCache.get(key);
                                if (container == null) {
                                    docsToPurge.add(key);
                                }
                                cacheDate = container.getLastAccessed();
                                if (cacheDate == null) continue;

                                dateDif = DateHelper.getMinutes(now) - DateHelper.getMinutes(cacheDate);
                                if (dateDif > documentTimeOut) {
                                    log.info("Removing orphaned document cache item : " + container.getDocKey());
                                    docsToPurge.add(key);
                                } else if (container.isKillLoadingSwitch()) {
                                    log.info("Removing canceled document : " + container.getDocKey());
                                    docsToPurge.add(key);
                                }
                            }
                            if (docsToPurge.size() > 0) {

                                for (String key : docsToPurge) {
                                    container = documentCache.remove(key);
                                    if (container != null) {
                                        container.closeDoc();
                                        container = null;
                                    }
                                }
                                Runtime r = Runtime.getRuntime();
                                r.gc();
                                log.info("Memory in use: " + (r.totalMemory() - r.freeMemory()) / 1000000);
                                log.info("Total documents in cache: " + documentCache.size());
                            }
                            Thread.sleep(2000);
                        } while (true);

                    } catch (RuntimeException e) {
                        System.out.println("** RuntimeException from monitoring thread");
                        System.out.println("** It may be necessary to restart the application.");
                        throw e;
                    } catch (InterruptedException e) {
                    }
                }
            }).start();
        }
    }

    @RequestMapping(value = "/getUniqueFileKey", method = RequestMethod.POST)
    public
    @ResponseBody
    String getUniqueFileKey(HttpServletRequest request) throws Exception {
        String uniqueFileKey = Constants.FILE_PREFIX + new Date().getTime();
        return uniqueFileKey;
    }


    @RequestMapping(value = "/keepFileAlive", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseContainer keepFileAlive(HttpServletRequest request) throws Exception {
        ResponseContainer responseContainer = new ResponseContainer();
        String fileKey = (String) request.getSession().getAttribute("fileKey");
        if (StringUtils.isBlank(fileKey)) {
            responseContainer.setStatus("Error");
            responseContainer.setError("Invalid key");
            return responseContainer;
        }

        FileCacheItem item = uploadService.getFileCacheItem(fileKey);
        if (item == null) {
            responseContainer.setStatus("Error");
            responseContainer.setError("Timeout");
            return responseContainer;
        }
        if (uploadService.isItemInactive(fileKey)) {
            responseContainer.setStatus("Error");
            responseContainer.setError("Inactive");
            return responseContainer;
        }

        item.setLastAccessed(new Date());
        responseContainer.setStatus("Ready");
        return responseContainer;

    }

    @RequestMapping(value = "/clearDocumentFromCache", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseContainer clearDocumentFromCache(@RequestBody String documentKey,
                                             HttpServletRequest request) throws Exception {


        ResponseContainer responseContainer = new ResponseContainer();
        try {
            if (StringUtils.isBlank(documentKey)) {
                throw new RuntimeException("Document key missing.");
            }
            DocumentContainer documentContainer = documentCache.remove(documentKey);
            if (documentContainer != null) {
                log.info("Removing from document cache : " + documentContainer.getDocSourceTitle());
                documentContainer.refreshBase();
                documentContainer.setKillLoadingSwitch(true);

                Runtime.getRuntime().gc();
            }
        } catch (Exception e) {
            log.info(e);
            responseContainer.setError(e.getMessage());
        }
        return responseContainer;

    }

    @RequestMapping(value = "/setFileInactive", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseContainer setFileInactive(HttpServletRequest request) throws Exception {
        ResponseContainer responseContainer = new ResponseContainer();
        String fileKey = (String) request.getSession().getAttribute("fileKey");
        if (StringUtils.isNotBlank(fileKey)) {
            FileCacheItem item = uploadService.getFileCacheItem(fileKey);
            if (item != null) {
                item.setFileStatus(FileCacheItem.status.INACTIVE);
                uploadService.setFileCacheItem(item);
            }
        }
        responseContainer.setStatus("Inactive");
        return responseContainer;

    }

    @RequestMapping(value = "/prepForUpload", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseContainer prepForUpload(@RequestBody RequestContainer requestContainer,
                                    HttpServletRequest request) throws Exception {
        ResponseContainer responseContainer = new ResponseContainer();
        ParsingTemplate p = requestContainer.getParsingTemplate();
        uploadService.removeFileCacheItem(p.getId());

        responseContainer.setStatus("Ready");
        return responseContainer;

    }


    @RequestMapping(value = "/getUploadedFileStatus", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseContainer getUploadedFileStatus(@RequestBody RequestContainer requestContainer,
                                            HttpServletRequest request) throws Exception {
        ResponseContainer responseContainer = new ResponseContainer();
        ParsingTemplate p = requestContainer.getParsingTemplate();
        if (p == null || StringUtils.isBlank(p.getId())) {
            responseContainer.setError("Invalid or empty project.");
            return responseContainer;
        }
        if (uploadService.isItemStatusUnknown(p.getId())) {
            responseContainer.setStatus("Unknown");

        } else if (uploadService.isItemPending(p.getId())) {
            responseContainer.setStatus("Pending");

        } else if (uploadService.isItemError(p.getId())) {
            responseContainer.setStatus("Error");
            responseContainer.setError(uploadService.getError(p.getId()));

        } else if (uploadService.isItemInactive(p.getId())) {
            responseContainer.setStatus("Inactive");
            uploadService.removeFileCacheItem(p.getId());

        } else if (uploadService.isItemImported(p.getId())) {
            File f = uploadService.getFileForId(p.getId());
            if (f != null && f.exists()) {
                String n = f.getName();
                responseContainer.setStatus("Imported");
                responseContainer.setBuffer(n);
                uploadService.removeFileCacheItem(p.getId());
            } else {
                // Should not get here but make sure we don't have a missing file.
                FileCacheItem item = uploadService.getFileCacheItem(p.getId());
                if (item != null) {
                    item.setFileStatus(FileCacheItem.status.ERROR);
                    responseContainer.setStatus("Error");

                    String msg = "The uploaded file could not be processed.<br>Make sure your file type is one of the following:<br>";
                    msg += uploadService.getAllowedExtensionsString();

                    item.setError(msg);
                    responseContainer.setError(msg);

                }
            }
        }

        return responseContainer;

    }

    @RequestMapping(value = "/getSheetsList", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseContainer getSheetsList(@RequestBody RequestContainer requestContainer,
                                    HttpServletRequest request) throws Exception {

        ResponseContainer responseContainer = new ResponseContainer();
        FileInfo fileInfo = new FileInfo();
        try {
            if (requestContainer == null) {
                throw new RuntimeException("Invalid request.");
            }
            if (requestContainer.getContentVersion() == null) {
                throw new RuntimeException("Invalid content parameter.");
            }
            ContentVersion content = requestContainer.getContentVersion();
            final boolean serverFileMode = (content.getId().startsWith("PBOX"));

            File file = null;
            String fileExtension = null;

            if (serverFileMode) {
                file = uploadService.getFileForId(content.getId());
                if (file == null || file.exists() == false) {
                    throw new RuntimeException("File not available.  Re-import your file using the import button.");
                }
                fileExtension = uploadService.getFileExtension(file);
                content.setFileExtension(fileExtension);
                content.setFileType(fileExtension);
                content.setTitle(uploadService.getSimpleFileName(file.getName()));

            } else {
                fileExtension = StringUtils.trimToEmpty(content.getFileExtension());
            }

            String sheetName = "";
            if (StringUtils.containsIgnoreCase(fileExtension, "XLS")) {
                List<String> sheetNames = new ArrayList<>();
                // This is a new import
                ExcelDocument excelDocument = new ExcelDocument(content.getTitle(), content.getFileExtension(),
                        requestContainer.getParsingTemplate().getId(), this.applicationProperties);
                if (serverFileMode) {
                    sheetNames = excelDocument.getSheetNames(file);
                } else {
                    sheetNames = excelDocument.getSheetNames(content.getVersionData());
                }
                JSONArray jsonArray = new JSONArray();
                for (String name : sheetNames) {
                    jsonArray.add(name);
                }
                responseContainer.setBuffer(jsonArray.toJSONString());
                responseContainer.setBufferType(ResponseContainer.bufferType.JSON);
                responseContainer.setStatus("SHEETS");
                return responseContainer;
            }
        } catch (Exception e) {
            responseContainer.setError(e.getMessage());
        }
        return responseContainer;
    }


    @RequestMapping(value = "/isConvertedFileExists", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseContainer isConvertedFileExists(@RequestBody RequestContainer requestContainer,
                                            HttpServletRequest request) throws Exception {

        ResponseContainer responseContainer = new ResponseContainer();
        FileInfo fileInfo = new FileInfo();
        try {
            if (requestContainer == null) {
                throw new RuntimeException("Invalid request.");
            }
            if (requestContainer.getParsingFileInfo() == null) {
                throw new RuntimeException("Invalid file info parameter.");
            }

            fileInfo = requestContainer.getParsingFileInfo();

            ParsingTemplate parsingTemplate = requestContainer.getParsingTemplate();
            if (StringUtils.containsIgnoreCase(fileInfo.getFileType__c(), "XLS")) {
                ExcelDocument excelDocument = new ExcelDocument(fileInfo.getTitle__c(), fileInfo.getFileType__c(),
                        requestContainer.getParsingTemplate().getId(), this.applicationProperties);
                if (excelDocument.isConvertedFileExists()) {
                    responseContainer.setStatus("CONVERTED_FILE_EXISTS");
                }
            }
            responseContainer.setParsingFileInfo(fileInfo);
        } catch (Exception e) {
            responseContainer.setError(e.getMessage());
        }
        return responseContainer;
    }

    @RequestMapping(value = "/setFileToCache", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseContainer setFileToCache(@RequestBody RequestContainer requestContainer,
                                     HttpServletRequest request) throws Exception {

        ResponseContainer responseContainer = new ResponseContainer();
        FileInfo fileInfo = new FileInfo();
        try {
            if (requestContainer == null) {
                throw new RuntimeException("Invalid request.");
            }
            if (requestContainer.getContentVersion() == null) {
                throw new RuntimeException("Invalid content parameter.");
            }

            ContentVersion content = requestContainer.getContentVersion();

            final boolean serverFileMode = (content.getId().startsWith(Constants.FILE_PREFIX));

            UserInfo userInfo = requestContainer.getUserInfo();

            String userid = userInfo.getUserId();
            String key = "____" + userid + "____" + requestContainer.getParsingTemplate().getId();


            if (documentCache.containsKey(key)) {
                DocumentContainer container = documentCache.remove(key);
                container.closeDoc();
                container = null;
            }

            // File file = null;
            InputStream fileInputStream = null;
            String fileExtension = null;
            String fileName = null;

            if (serverFileMode) {
                fileInputStream = uploadService.getFileInputStreamForId(content.getId());
                if (fileInputStream == null) {
                    // If this is a sample request, try to ge the sample from the resources.
                    if (content.getId().startsWith("PBOX__SAMPLE")) {
                        fileInputStream = uploadService.getSampleFileInputStream(content.getId());
                    }
                    if (fileInputStream == null) {
                        throw new RuntimeException("File not available.  Re-import your file using the import button.");
                    }
                }

                fileExtension = FilenameUtils.getExtension(content.getId());
                content.setTitle(uploadService.getSimpleFileName(content.getId()));

                content.setFileExtension(fileExtension);
                content.setFileType(fileExtension);

            } else {
                fileExtension = StringUtils.trimToEmpty(content.getFileExtension()).toUpperCase();
            }

            // For now we will support PDF and Excel files only.
            if (!StringUtils.containsIgnoreCase(fileExtension, "PDF") &&
                    !StringUtils.containsIgnoreCase(fileExtension, "XLS")) {
                throw new RuntimeException("This version currently supports PDF and Excel file imports only.");
            }
            ParsingTemplate parsingTemplate = requestContainer.getParsingTemplate();
            JSONObject metaDataObject = null;
            if (parsingTemplate != null && StringUtils.isNotBlank(parsingTemplate.getMetaData__c())) {
                String json = parsingTemplate.getMetaData__c();
                JSONParser parser = new JSONParser();
                metaDataObject = (JSONObject) parser.parse(json);
            }

            String sheetName = "";
            if (StringUtils.containsIgnoreCase(fileExtension, "XLS")) {
                if (metaDataObject != null) {
                    sheetName = (String) metaDataObject.get("ExcelSheetName");
                }
                if (StringUtils.isBlank(sheetName)) {
                    List<String> sheetNames = new ArrayList<>();

                    // This is a new import
                    ExcelDocument excelDocument = new ExcelDocument(content.getTitle(), content.getFileExtension(),
                            requestContainer.getParsingTemplate().getId(), this.applicationProperties);


                    excelDocument.clearFilePath(); // Clear the prior file if exists.
                    if (serverFileMode) {
                        sheetNames = excelDocument.getSheetNames(fileInputStream);
                    } else {
                        sheetNames = excelDocument.getSheetNames(content.getVersionData());
                    }
                    JSONArray jsonArray = new JSONArray();
                    for (String name : sheetNames) {
                        jsonArray.add(name);
                    }
                    responseContainer.setBuffer(jsonArray.toJSONString());
                    responseContainer.setBufferType(ResponseContainer.bufferType.JSON);
                    responseContainer.setStatus("SHEETS");
                    return responseContainer;
                }
            }

            DocumentContainer documentContainer = new DocumentContainer(this.applicationProperties);
            documentContainer.setDocKey(key);
            documentContainer.setDocSourceExtension(content.getFileExtension());
            documentContainer.setDocSourceType(content.getFileType());
            documentContainer.setDocSourceTitle(content.getTitle());
            documentContainer.setUserInfo(userInfo);
            documentContainer.setParsingTemplate(requestContainer.getParsingTemplate());
            documentContainer.setDocStatus(DocumentContainer.status.LOADING);
            documentContainer.setSheetName(sheetName);

            fileInfo = documentContainer.getFileInfo();
            responseContainer.setParsingFileInfo(fileInfo);
            documentCache.put(key, documentContainer);

            final InputStream fileInputStreamTemp = fileInputStream;
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        //  sleep(10*1000);
                        if (serverFileMode) {
                            documentContainer.loadDocument(fileInputStreamTemp, content.getFileExtension());
                        } else {
                            documentContainer.loadDocument(content, content.getFileExtension());
                        }
                        log.info("File imported by " + userInfo.getFirstName() + " " + userInfo.getLastName());
                        Runtime.getRuntime().gc();
                    } catch (Exception e) {
                        log.info(e);
                    }
                }
            }.start();
        } catch (Exception e) {
            responseContainer.setError(e.getMessage());
        }
        return responseContainer;
    }


    @RequestMapping(value = "/getFileInfo", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseContainer getFileInfo(@RequestBody String documentKey,
                                  HttpServletRequest request) throws Exception {

        ResponseContainer container = new ResponseContainer();

        try {
            FileInfo fileInfo = new FileInfo();

            if (documentKey == null || StringUtils.isBlank(documentKey)) {
                throw new RuntimeException("Document key missing.");
            }
            DocumentContainer documentContainer = documentCache.get(documentKey);
            if (documentContainer == null) {
                throw new RuntimeException("Document not available.  Reload the project or re-import the source file if necessary.");
            } else {
                fileInfo = documentContainer.getFileInfo();
                container.setParsingFileInfo(fileInfo);
                //container.setReturnValue(documentContainer.getPercentageComplete());   // DON'T user since inconsistent with Excel streaming
            }

        } catch (Exception e) {
            container.setError(e.getMessage());
        }

        return container;
    }


    @RequestMapping(value = "/handleSearch", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseContainer handleSearch(@RequestBody RequestContainer requestContainer, HttpServletRequest request) throws Exception {
        ResponseContainer container = new ResponseContainer();
        String documentKey = requestContainer.getParsingFileInfo().getDocumentKey__c();
        String searchString = requestContainer.getSpecialParameter();
        try {
            if (StringUtils.isBlank(documentKey)) {
                throw new RuntimeException("Document key missing.");
            }

            if (StringUtils.isNotBlank(searchString)) {

            }

        } catch (Exception e) {
            container.setError(e.getMessage());
            log.info(e);
        }
        return container;
    }

    @RequestMapping(value = "/getPage", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseContainer getPage(@RequestBody RequestContainer requestContainer, HttpServletRequest request) throws Exception {
        DocumentContainer documentContainer = null;
        ResponseContainer responseContainer = new ResponseContainer();
        try {
            boolean parsingMode = false;

            FileInfo fileInfo = requestContainer.getParsingFileInfo();
            if (fileInfo == null) {
                throw new RuntimeException("Document info object missing.");
            }
            String documentKey = fileInfo.getDocumentKey__c();
            if (StringUtils.isBlank(documentKey)) {
                throw new RuntimeException("Document key missing.");
            }
            documentContainer = documentCache.get(documentKey);
            if (documentContainer == null) {
                throw new RuntimeException("Document not available.  Reload the project or re-import the source file if necessary.");
            }

            // Prevent multiple thread access to same container
            synchronized (documentContainer ) {

                UserInfo userInfo = documentContainer.getUserInfo();
                long lastPageNumber = documentContainer.getPageNumber();

                String exportType = requestContainer.getExportType();
                boolean headerSwitch = requestContainer.isHeaderSwitch();

                documentContainer.setLastAccessed(new Date());
                documentCache.put(documentKey, documentContainer);

                List<ParsingFilter> parsingFilters = new ArrayList<>();
                if (requestContainer != null) {
                    if (requestContainer.getParsingFilters() != null) {
                        parsingMode = true;
                        parsingFilters = requestContainer.getParsingFilters();
                        for (ParsingFilter p : parsingFilters) {    // This should never happen, but lets default to 'marker' type if necessary.
                            if (StringUtils.isBlank(p.getFilterType__c())) {
                                p.setFilterType__c(ParsingFilter.MARKER_TYPE);
                            }
                        }
                    }
                }
                // Determine the page to parse
                int startPage = 0;
                int endPage = 0;

                startPage = (fileInfo.getPageNumber__c() > fileInfo.getPageCount__c()) ?
                        fileInfo.getPageCount__c() : fileInfo.getPageNumber__c();

                int totalpages = documentContainer.getNumberOfPages();

                endPage = startPage;

                // Set the locale of the user to be used for currency validation, ...
                Locale locale = null;
                if (userInfo != null && userInfo.getLocale() != null) {
                    String localeStr = userInfo.getLocale();
                    if (StringUtils.contains(userInfo.getLocale(), "_")) {
                        String[] lArray = StringUtils.split(userInfo.getLocale(), "_");
                        if (lArray.length == 2) {
                            locale = new Locale(lArray[0], lArray[1]);
                        }
                    }
                    if (locale == null) {
                        locale = Locale.US; // this should not happen if the above is correctly working
                    }
                }

                double dScale = 0;
                if (requestContainer.getParsingTemplate() != null) {
                    ParsingTemplate pT = requestContainer.getParsingTemplate();
                    if (StringUtils.isNotBlank(pT.getScalingMultiplier__c())) {
                        String scale = StringUtils.remove(pT.getScalingMultiplier__c(), "%");
                        if (NumberUtils.isCreatable(scale)) {
                            int sc = NumberUtils.toInt(scale);
                            dScale = (sc * .01);
                        }
                    } else {
                        dScale = 1;
                    }
                }
                documentContainer.setLocale(locale);

                ITextStripper stripper = documentContainer.getNewTextStripper();
                documentContainer.setStripper(stripper);
                stripper.getBase().setLocale(locale);
                stripper.getBase().setScalingMultiplier(dScale);

                if (StringUtils.isNotBlank(exportType)) {
                    startPage = 1;
                    endPage = totalpages;
                }

                if (startPage == 1) {
                    documentContainer.setCurrentGrabberMap(null);
                }

                // If the user jumps to a location in the document other than the next page;
                // we must backup until we discover all possible parsing grabbers.
                // This is needed inorder to make sure we capture all grabber values correctly.
                int MAX_PAGE_SCAN = 20;
                int firstPage = startPage - MAX_PAGE_SCAN;
                if (firstPage < 1) firstPage = 1;
                int lastPage = startPage;

                if (parsingMode) {
                    if (startPage != 1) {
                        List<ParsingFilter> grabbersToFind = new ArrayList<>();
                        for (ParsingFilter p : parsingFilters) {
                            if (StringUtils.isNotBlank(p.getGrabberType__c())) {
                                grabbersToFind.add(p);
                            }
                        }

                        if (grabbersToFind.size() > 0) {
                            for (int scanCnt = 0; scanCnt < MAX_PAGE_SCAN; scanCnt++) {
                                firstPage--;
                                if (firstPage < 1) break;
                                if (lastPage > totalpages) lastPage = totalpages;
                                findGrabbers(documentContainer, firstPage, lastPage, grabbersToFind);
                                if (grabbersToFind.size() == 0) {
                                    break;
                                }
                            }
                        }
                    }
                }

                stripper.getBase().setCurrentGrabberMap(new HashMap<>());

                if (documentContainer.getCurrentGrabberMap() != null) {
                    stripper.getBase().setCurrentGrabberMap(documentContainer.getCurrentGrabberMap());
                }

                if (parsingFilters != null && parsingFilters.size() > 0) {
                    stripper.getBase().setParsingFilters(parsingFilters);
                }
                if (parsingMode) {
                    documentContainer.setPageNumber(startPage);
                }

                // Handle the export and other long running tasks in a different method (thread).
                if (StringUtils.isNotBlank(exportType)) {

                    if (StringUtils.startsWithIgnoreCase(exportType, "REPORT_TOTALS")) {
                        return processTotals(requestContainer, documentContainer);
                    } else {
                        return processExport(requestContainer, documentContainer);
                    }
                }

                if (StringUtils.isNotBlank(exportType)) {
                    stripper.setStartPage(startPage);
                    stripper.setEndPage(endPage);
                } else {
                    stripper.setStartPage(firstPage);
                    stripper.setEndPage(lastPage);
                }
                stripper.getText(documentContainer.getDoc());

                if (stripper.getBase().getCurrentGrabberMap() != null) {
                    documentContainer.setCurrentGrabberMap(stripper.getBase().getCurrentGrabberMap());
                }

                String ret = "";
                if (StringUtils.isBlank(exportType)) {
                    if (parsingFilters.size() > 0) {
                        if (StringUtils.isNotBlank(requestContainer.getReturnType())) {
                            if (requestContainer.getReturnType().equalsIgnoreCase("json")) {
                                ret = stripper.getBase().getParsedPageAsJSON(startPage);
                                responseContainer.setBuffer(ret);
                                responseContainer.setBufferType(ResponseContainer.bufferType.JSON);
                            }

                        } else {
                            ret = stripper.getBase().getParsedPageAsJSONArrays(startPage, true);
                            responseContainer.setBuffer(ret);
                            responseContainer.setBufferType(ResponseContainer.bufferType.JSON);
                        }
                    } else {
                        ret = stripper.getBase().getUnparsedPageAsHTML(false, startPage);
                        responseContainer.setBuffer(ret);
                        responseContainer.setBufferType(ResponseContainer.bufferType.HTML);
                    }
                }
            }
        } catch (Exception e) {
            responseContainer.setError(e.getMessage());
        } finally {
            if (documentContainer != null) {
                documentContainer.refreshBase(); // clear some memory before exiting
            }
        }
        return responseContainer;
    }



    public List<ParsingFilter> findGrabbers(DocumentContainer documentContainer,
                                     int firstPage,
                                     int lastPage,
                                     List<ParsingFilter> parsingFilters) throws Exception {
        documentContainer.setCurrentGrabberMap(null);
        ITextStripper sStr = documentContainer.getNewTextStripper();

        sStr.getBase().setLocale(documentContainer.getStripper().getBase().getLocale());
        sStr.getBase().setScalingMultiplier(documentContainer.getStripper().getBase().getScalingMultiplier());

        if (documentContainer.getCurrentGrabberMap() != null) {
            sStr.getBase().setCurrentGrabberMap(documentContainer.getCurrentGrabberMap());
        }
        if (parsingFilters != null && parsingFilters.size() > 0) {
            sStr.getBase().setParsingFilters(parsingFilters);
        }
        sStr.setStartPage(firstPage);
        sStr.setEndPage(lastPage);
        sStr.getText(documentContainer.getDoc());

        if (sStr.getBase().getCurrentGrabberMap() != null) {
            documentContainer.setCurrentGrabberMap(sStr.getBase().getCurrentGrabberMap());
        }
        List<ParsingFilter> grabbersFound = sStr.getBase().getGrabbersFound();
        if (grabbersFound.size() > 0) {
            for (ParsingFilter p : grabbersFound) {
                parsingFilters.remove(p);
            }
        }
        return grabbersFound;
    }


        @RequestMapping(value = "/killSpecialAsyncTask", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseContainer killSpecialAsyncTask(@RequestBody RequestContainer requestContainer, HttpServletRequest request) throws Exception {
        ResponseContainer container = new ResponseContainer();
        String documentKey = requestContainer.getParsingFileInfo().getDocumentKey__c();
        String transactionKey = requestContainer.getTransactionKey();
        try {
            if (StringUtils.isBlank(documentKey)) {
                throw new RuntimeException("Document key missing.");
            }
            if (StringUtils.isNotBlank(transactionKey)) {
                // If there is already a task running warn user.
                SpecialAsyncTask priorTask = getAsyncTaskProcessingOrPending(transactionKey, documentKey);
                if (priorTask != null) {
                    priorTask.setStatus(SpecialAsyncTask.statusType.CANCELLED);
                    priorTask.setOkToPurge(true);
                    container.setStatus(SpecialAsyncTask.statusType.CANCELLED.toString());
                }
            }
        } catch (Exception e) {
            container.setError(e.getMessage());
            log.info(e);
        }
        return container;
    }

    @RequestMapping(value = "/getSpecialAsyncTaskResponse", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseContainer getSpecialAsyncTaskResponse(@RequestBody RequestContainer requestContainer, HttpServletRequest request) throws Exception {
        ResponseContainer container = new ResponseContainer();
        try {
            String documentKey = requestContainer.getParsingFileInfo().getDocumentKey__c();
            String transactionKey = requestContainer.getTransactionKey();
            if (documentKey == null || StringUtils.isBlank(documentKey)) {
                throw new RuntimeException("Document key missing.");
            }
            DocumentContainer documentContainer = documentCache.get(documentKey);
            if (documentContainer == null) {
                throw new RuntimeException("Document not available.  Reload the project or re-import the source file if necessary.");
            }
            SpecialAsyncTask task = null;
            if (StringUtils.isNotBlank(transactionKey)) {
                task = getAsyncTaskProcessingOrPending(transactionKey, documentKey);
            } else {
            }
            if (task == null) {
                throw new RuntimeException("The task is no longer active.");
            }
            if (task.getStatus() != null) {
                container.setStatus(task.getStatus().toString());
                if (task.getStatus().equals(SpecialAsyncTask.statusType.PROCESSING)) {
                    container.setReturnValue(task.getPercentageComplete());
                }
            }
            container.setBuffer(task.getBuffer());
            if (task.getBufferType() != null) {
                container.setBufferType(task.getBufferType().toString());
            }
            container.setError(task.getException() != null ? task.getException().getMessage() : null);
            if (task.getStatus() != null) {
                if (task.getStatus().equals(SpecialAsyncTask.statusType.PROCESSING) == false &&
                    task.getStatus().equals(SpecialAsyncTask.statusType.PENDING) == false) {
                    if (task.getCompleted() != null) task.setCompleted(new Date());
                    task.setOkToPurge(true);
                }
            }
        } catch (Exception e) {
            container.setError(e.getMessage());
            log.info(e);
        }
        return container;
    }

    ResponseContainer processTotals(RequestContainer requestContainer, DocumentContainer documentContainer) throws Exception {
        ResponseContainer responseContainer = new ResponseContainer();
        try {
            String transaction = "REPORT_TOTALS";
            String documentKey = documentContainer.getDocKey();

            SpecialAsyncTask task = getAsyncTaskProcessingOrPending(transaction, documentKey);
            if (task != null && task.getStatus().equals(SpecialAsyncTask.statusType.PROCESSING)) {
                if ("OK_TO_REPLACE".equals(requestContainer.getSpecialParameter())) {
                    task.setStatus(SpecialAsyncTask.statusType.CANCELLED);
                    task.setOkToPurge(true);
                    Thread.sleep(2000);
                } else {
                    throw new RuntimeException("Another task is already in progress.");
                }
            }
            final boolean jsonRequest = requestContainer.getReturnType() != null
                    && requestContainer.getReturnType().equalsIgnoreCase("Json");

            task = new SpecialAsyncTask("REPORT_TOTALS", documentKey) {
                @Override
                public void run() {
                    super.run();
                    try {
                        ExportUtil u = new ExportUtil(documentContainer, null, false);
                        this.addObserver(u);
                        if (jsonRequest) {
                            this.setBuffer(u.getTotalsAsJson());
                            this.setBufferType(SpecialAsyncTask.bufferType.JSON);
                        } else {
                            this.setBuffer(u.getTotalsAsHTML());
                            this.setBufferType(SpecialAsyncTask.bufferType.HTML);
                        }
                        this.setStatus(SpecialAsyncTask.statusType.FINAL);
                    } catch (Exception e) {
                        String msg = StringUtils.trimToEmpty(e.getMessage());
                        if (msg.equals(SpecialAsyncTask.statusType.CANCELLED.toString()) == false) {
                            this.setStatus(SpecialAsyncTask.statusType.ERROR);
                            this.setException(e);
                        }
                    }
                }
            };
            setSpecialBackGroundTask(task); // Set task to be started based on application settings.
        } catch (Exception e) {
            responseContainer.setError(e.getMessage());
        }
        return responseContainer;
    }

    ResponseContainer processExport(RequestContainer requestContainer, DocumentContainer documentContainer) throws Exception {
        ResponseContainer responseContainer = new ResponseContainer();
        try {

            FileInfo fileInfo = requestContainer.getParsingFileInfo();
            if (fileInfo == null) {
                throw new RuntimeException("Document info object missing.");
            }

            String documentKey = fileInfo.getDocumentKey__c();
            if (StringUtils.isBlank(documentKey)) {
                throw new RuntimeException("Document key missing.");
            }

            String exportType = StringUtils.trimToEmpty(requestContainer.getExportType());
            String deliveryMethod = StringUtils.trimToEmpty(requestContainer.getDeliveryMethod());
            boolean headerSwitch = requestContainer.isHeaderSwitch();

            String transaction = "EXPORT";

            // If there is already a task running warn user.
            SpecialAsyncTask priorTask = getAsyncTaskProcessingOrPending(transaction, documentKey);
            if (priorTask != null && priorTask.getCompleted() != null) {
                priorTask.setOkToPurge(true);
            }
            else
            if (priorTask != null) {
                String specialParameter = requestContainer.getSpecialParameter();
                if (specialParameter != null && specialParameter.equalsIgnoreCase("OK_TO_REPLACE")) {
                    priorTask.setStatus(SpecialAsyncTask.statusType.CANCELLED);
                    UserInfo u = requestContainer.getUserInfo();
                    if (u != null) {
                        log.info("User " + u.getFirstName() + " " +
                                u.getLastName() + " [ " + u.getUserEmail() + " ]" +
                                " cancelling export " +  documentKey);
                    }
                    priorTask.setOkToPurge(true);
                } else {
                    //priorTask.interrupt();
                    responseContainer.setStatus("IN_PROGRESS");
                    responseContainer.setError("An export is already in progress.");
                    return responseContainer;
                }
            }
            // Push the export task onto the pending tasks stack where it will be handled and monitored accordingly.
            SpecialAsyncTask task = new SpecialAsyncTask("EXPORT", documentKey) {
                @Override
                public void run() {
                    super.run();
                    String path = "";
                    String fileName = "";
                    try {
                        super.run();

                        path = "PPARSED_"
                                + documentContainer.getDocSourceTitle()
                                + GenerateShortUUID.getNumber();
                        fileName = FilenameUtils.getBaseName(documentContainer.getDocSourceTitle());
                        switch (exportType) {
                            case "TAB":
                                fileName += ".tab";
                                path += ".tab";
                                break;
                            case "CSV":
                                fileName += ".csv";
                                path += ".csv";
                                break;
                            case "EXCEL":
                            default:
                                fileName += ".xlsx";
                                path += ".xlsx";
                                break;
                        }

                        String fullPath = applicationProperties.getFileCacheLocation() + File.separatorChar + path;

                        ExportUtil util = new ExportUtil(documentContainer, fullPath, headerSwitch);
                        this.addObserver(util);
                        util.startExport(exportType);
                        this.setStatus(SpecialAsyncTask.statusType.FINAL);

                        this.setBuffer(path);
                        this.setBufferType(bufferType.PATH);

                        if (StringUtils.equalsIgnoreCase(deliveryMethod,"DOWNLOAD") == false) {
                            new EmailUtil(applicationProperties,
                                    requestContainer.getUserInfo(),
                                    requestContainer.getRequestEmail(),
                                    "Parsed Document " + fileName,
                                    "Please find your processed document attached: ** " + fileName + " ** ",
                                    "",
                                    fileName,
                                    fullPath).emailDocument();
                        }


                    } catch (Exception e) {
                        if (e.getMessage().equals(SpecialAsyncTask.statusType.CANCELLED.toString()) == false) {
                            this.setStatus(SpecialAsyncTask.statusType.ERROR);
                            this.setException(e);
                            log.info(e);
                            try {
                                String message = "The system was not able to export ** " + fileName + " ** \n";
                                for (int i = 0; i < 85; i++) message += "-";
                                message += "\n" + e.getMessage() + " \n\n\n\n";

                                new EmailUtil(applicationProperties).email(
                                        requestContainer.getUserInfo(),
                                        requestContainer.getRequestEmail(),
                                        "Parsed Document Error " + fileName,
                                        message);
                            } catch (Exception ee) {
                                log.info(ee);
                            }
                        }
                    }
                    finally {
                        if (StringUtils.equalsIgnoreCase(deliveryMethod,"EMAIL")) {
                            this.setCompleted(new Date());
                        }
                    }
                }
            };
            setSpecialBackGroundTask(task); // Set task to be started based on application settings.

            responseContainer.setStatus("Exported");
            responseContainer.setMessage("Your export is processing and will be available shortly.");
        } catch (Exception e) {
            responseContainer.setError(e.getMessage());
        }

        return responseContainer;
    }


}