package com.parserbox.sevice;

import com.parserbox.Constants;
import com.parserbox.model.ApplicationProperties;
import com.parserbox.model.FileCacheItem;
import com.parserbox.model.parser.ExcelDocument;
import com.parserbox.utils.DateHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;


import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

@Service
public class UploadServiceImpl {


    public static String[] allowedExtensions = {"xls", "XLS", "xlsx", "XLSX", "pdf", "PDF"};


    private Log log = LogFactory.getLog(this.getClass());
    static boolean monitorStarted = false;
    static Map<String, FileCacheItem> fileCache = new Hashtable<>();


    @Autowired
    ApplicationProperties applicationProperties;


    @PostConstruct
    public void init() {

        if (monitorStarted == false) {
            monitorStarted = true;
            // Create a daemon thread to monitor cache
            log.info("Starting file cache monitoring thread...");
            (new Thread() {
                Date cacheDate = null;
                long dateDif = 0;
                long documentTimeOut = 0;
                long DEFAULT_DOCUMENT_TIMEOUT = 15;     // The default timeout in minutes.

                long DEFAULT_KEEP_ALIVE_TIMEOUT = 30;   // The default time lapse allowed for a keep alive call.

                FileCacheItem container = null;

                public void run() {
                    try {
                        do {
                            // Check the document cache to see if we have any orphaned documents
                            documentTimeOut = applicationProperties.getDocumentTimeOut();
                            if (documentTimeOut <= 0) documentTimeOut = DEFAULT_DOCUMENT_TIMEOUT;
                            Date now = new Date();

                            for (String key : fileCache.keySet()) {
                                container = fileCache.get(key);
                                if (container == null) fileCache.remove(key);
                                cacheDate = container.getLastAccessed();
                                if (cacheDate == null) continue;

                                // Check for keep alive.  If time expires with no activity, set status inactive so
                                // that client applications waiting for status, can be informed.
                                // Don't remove the object since it may take client applications a little time to find
                                // out the status. Note *** Eventually this file cache item will be removed as an orphan.
                                dateDif = DateHelper.getSeconds(now) - DateHelper.getSeconds(cacheDate);
                                if (dateDif > DEFAULT_KEEP_ALIVE_TIMEOUT) {
                                    FileCacheItem item = fileCache.get(key);
                                    if (item != null) {
                                        if (item.getFileStatus() != null) {
                                            if (item.getFileStatus().equals(FileCacheItem.status.INACTIVE) == false) {
                                                log.info("Setting cache item status to inactive : " + container.getFileKey());
                                                item.setFileStatus(FileCacheItem.status.INACTIVE);
                                                continue;
                                            }
                                        }
                                    }
                                }

                                // Check for orphans
                                dateDif = DateHelper.getMinutes(now) - DateHelper.getMinutes(cacheDate);
                                if (dateDif > documentTimeOut) {
                                    log.info("Removing orphaned file cache item : " + container.getFileKey());
                                    container = fileCache.remove(key);
                                    container = null;
                                    continue;
                                }
                            }
                            Thread.sleep(5000);
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


    public void keepItemAlive(String fileKey) {
        FileCacheItem item = getFileCacheItem(fileKey);
        if (item != null) {
            item.setLastAccessed(new Date());
        }
    }


    public void setFileCacheItem(FileCacheItem item) {
        fileCache.put(item.getFileKey(), item);
    }

    public FileCacheItem getFileCacheItem(String fileId) {
        return fileCache.get(fileId);
    }

    public void removeFileCacheItem(String fileId) {
        fileCache.remove(fileId);
    }

    public boolean isItemImported(String fileId) {
        FileCacheItem item = fileCache.get(fileId);
        return (item != null && item.getFileStatus().equals(FileCacheItem.status.IMPORTED));
    }

    public boolean isItemInactive(String fileId) {
        FileCacheItem item = fileCache.get(fileId);
        return (item != null && item.getFileStatus().equals(FileCacheItem.status.INACTIVE));
    }

    public boolean isItemPending(String fileId) {
        FileCacheItem item = fileCache.get(fileId);
        return (item != null && item.getFileStatus().equals(FileCacheItem.status.PENDING));
    }

    public boolean isItemError(String fileId) {
        FileCacheItem item = fileCache.get(fileId);
        return (item != null && item.getFileStatus().equals(FileCacheItem.status.ERROR));
    }

    public boolean isItemStatusUnknown(String fileId) {
        FileCacheItem item = fileCache.get(fileId);
        return (item == null || item.getFileStatus().equals(FileCacheItem.status.UNKNOWN));
    }

    public String getError(String fileId) {
        FileCacheItem item = fileCache.get(fileId);
        return (item != null && item.getFileStatus().equals(FileCacheItem.status.ERROR)) ? item.getError() : "";
    }


    public String getUploadDirectory() {
        String uploadDir = applicationProperties.getUploadDir();
        if (!uploadDir.endsWith("/")) uploadDir += "/";
        return uploadDir;

    }

    public String getFileName(String fileKey, String fileName) {
        return getUploadDirectory() + Constants.FILE_PREFIX + fileKey + "____" + fileName;
    }

    public String getSimpleFileName(String name) {
        String simpleName =
                StringUtils.contains(name, "PBOX__SAMPLE__") ?
                        StringUtils.substringAfter(name, "PBOX__SAMPLE__") :
                        StringUtils.substringAfter(name, "____");

        return simpleName;
    }

    public String getFileExtension(File file) {
        return FilenameUtils.getExtension(file.getName()).toUpperCase();
    }

    public FileInputStream getFileInputStreamForId(String fileKey) {

        try {
            if (fileKey.startsWith(Constants.FILE_PREFIX)) {
                return new FileInputStream(getUploadDirectory() + fileKey);
            }

            Collection<File> files = FileUtils.listFiles(new File(getUploadDirectory()), allowedExtensions, false);
            for (File f : files) {
                String n = f.getName();
                if (StringUtils.startsWith(n, Constants.FILE_PREFIX + fileKey)) {
                    return new FileInputStream(f);
                }
            }
        }
        catch (FileNotFoundException e) {

        }

        return null;
    }

    public File getFileForId(String fileKey) {
        if (fileKey.startsWith(Constants.FILE_PREFIX)) {
            return new File(getUploadDirectory() + fileKey);
        }

        Collection<File> files = FileUtils.listFiles(new File(getUploadDirectory()), allowedExtensions, false);
        for (File f : files) {
            String n = f.getName();
            if (StringUtils.startsWith(n, Constants.FILE_PREFIX + fileKey)) {
                return f;
            }
        }

        return null;
    }

    public void clearForFileKey(String fileKey, File uploadedFile) throws IOException {
        Collection<File> files = FileUtils.listFiles(new File(getUploadDirectory()), allowedExtensions, false);
        for (File f : files) {
            String n = f.getName();
            if (uploadedFile != null && uploadedFile.getName().equalsIgnoreCase(n)) {
                continue;
            }
            if (StringUtils.containsIgnoreCase(n, fileKey)) {
                f.delete();
            }
        }
        String fileName = uploadedFile.getName();
        String fileExt = FilenameUtils.getExtension(fileName);
        if (StringUtils.containsIgnoreCase(fileExt, "XLS")) {
            ExcelDocument excelDocument = new ExcelDocument(fileName, fileExt, fileKey, this.applicationProperties);
            excelDocument.clearFilePath();
        }
    }

    public String getAllowedExtensionsString() {
        String str = "";
        for (int i = 0, len = allowedExtensions.length; i < len; i++) {
            str += str.length() > 0 ? ", " : "";
            str += allowedExtensions[i];
        }
        return str;
    }

    public InputStream getSampleFileInputStream(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource("static/" + fileName);
            if (resource != null) return resource.getInputStream();
        } catch (Exception e) {
            log.info(e);
        }
        return null;
    }
}
