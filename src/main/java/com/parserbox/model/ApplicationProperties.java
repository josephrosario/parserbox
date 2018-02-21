package com.parserbox.model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@ConfigurationProperties(prefix = "application")
@Component
public class ApplicationProperties {
    private boolean debug;
    private int maxBackgroundTasks = 2;
    private long documentTimeOut = 15;
    private String fileCacheLocation;
    private String emailHost;
    private String emailUserId;
    private String emailPassword;
    private String emailFromAddress;
    private String emailFromName;
    private int emailPort;
    private boolean enableSSL;
    private boolean enableTLS;
    private String uploadDir;

    private Log log = LogFactory.getLog(this.getClass());

    @PostConstruct
    public void init() {
        // Create directories as needed
        createDirectory(this.fileCacheLocation);
        createDirectory(this.uploadDir);

    }
    public void createDirectory(String path) {
        try {
            if (StringUtils.isNotBlank(path)) {
                File file = new File(path);
                if ( ! file.exists() ) {
                    FileUtils.forceMkdir(file);
                    log.info("Created directory : " + path);
                }
            }
        }
        catch (IOException e) {
            log.info("Could not create directory : " + path);
        }

    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public int getMaxBackgroundTasks() {
        return maxBackgroundTasks;
    }

    public void setMaxBackgroundTasks(int maxBackgroundTasks) {
        this.maxBackgroundTasks = maxBackgroundTasks;
    }

    public String getFileCacheLocation() {
        return fileCacheLocation;
    }

    public void setFileCacheLocation(String fileCacheLocation) {
        this.fileCacheLocation = fileCacheLocation;
    }

    public long getDocumentTimeOut() {
        return documentTimeOut;
    }

    public void setDocumentTimeOut(long documentTimeOut) {
        this.documentTimeOut = documentTimeOut;
    }

    public String getEmailHost() {
        return emailHost;
    }

    public void setEmailHost(String emailHost) {
        this.emailHost = emailHost;
    }

    public String getEmailUserId() {
        return emailUserId;
    }

    public void setEmailUserId(String emailUserId) {
        this.emailUserId = emailUserId;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }

    public String getEmailFromAddress() {
        return emailFromAddress;
    }

    public void setEmailFromAddress(String emailFromAddress) {
        this.emailFromAddress = emailFromAddress;
    }

    public String getEmailFromName() {
        return emailFromName;
    }

    public void setEmailFromName(String emailFromName) {
        this.emailFromName = emailFromName;
    }

    public int getEmailPort() {
        return emailPort;
    }

    public void setEmailPort(int emailPort) {
        this.emailPort = emailPort;
    }

    public boolean isEnableSSL() {
        return enableSSL;
    }

    public void setEnableSSL(boolean enableSSL) {
        this.enableSSL = enableSSL;
    }

    public boolean isEnableTLS() {
        return enableTLS;
    }

    public void setEnableTLS(boolean enableTLS) {
        this.enableTLS = enableTLS;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
