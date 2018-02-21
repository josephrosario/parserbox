package com.parserbox.utils;

import com.parserbox.model.ApplicationProperties;
import com.parserbox.model.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.StringTokenizer;


public class EmailUtil {
    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    ApplicationProperties applicationProperties;

    UserInfo userInfo;
    String requestEmail;
    String subject;
    String message;
    String description;
    String documentName;
    String documentPath;



    public EmailUtil(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }
    public EmailUtil(ApplicationProperties applicationProperties,
                     UserInfo userInfo,
                     String requestEmail,
                     String subject,
                     String message,
                     String description,
                     String documentName,
                     String documentPath) {
        this.applicationProperties = applicationProperties;
        this.userInfo = userInfo;
        this.requestEmail = requestEmail;
        this.subject = subject;
        this.message = message;
        this.description = description;
        this.documentName = documentName;
        this.documentPath = documentPath;

    }


    public void emailDocument() throws Exception {
        emailDocument(userInfo,
                requestEmail,
                subject,
                message,
                description,
                documentName,
                documentPath);
    }
    public void emailDocument(UserInfo userInfo,
                              String requestEmail,
                              String subject,
                              String message,
                              String description,
                              String documentName,
                              String documentPath) throws Exception {

        try {
            if (StringUtils.isBlank(userInfo.getUserEmail())) {
                throw new RuntimeException("Invalid or missing email.");
            }
            String formattedName = StringUtils.trimToEmpty(userInfo.getFirstName());
            if (StringUtils.isNotBlank(formattedName)) {
                formattedName += " ";
            }
            formattedName += StringUtils.trimToEmpty(userInfo.getLastName());

            // Create the attachment
            EmailAttachment attachment = new EmailAttachment();
            attachment.setDisposition(EmailAttachment.ATTACHMENT);
            attachment.setPath(documentPath);
            attachment.setName(documentName);

            if (StringUtils.isNotBlank(description)) {
                attachment.setDescription(description);
            }
            // Create the email message
            MultiPartEmail email = new MultiPartEmail();

            email.setHostName(applicationProperties.getEmailHost());
            if (applicationProperties.getEmailPort() > 0) {
                email.setSmtpPort(applicationProperties.getEmailPort());
            }
            if (applicationProperties.isEnableSSL()) {
                email.setSSLOnConnect(true);
            }
            if (applicationProperties.isEnableTLS()) {
                email.setStartTLSRequired(true);
            }
            email.setAuthenticator(
                    new DefaultAuthenticator(applicationProperties.getEmailUserId(),
                            applicationProperties.getEmailPassword()));

            if (StringUtils.containsAny(requestEmail, ";,")) {
                StringTokenizer st = new StringTokenizer(requestEmail, ",;");
                while (st.hasMoreTokens()) {
                   String em = st.nextToken();
                   if (StringUtils.equalsIgnoreCase(em, userInfo.getUserEmail())) {
                       email.addTo(
                               StringUtils.trimToEmpty(userInfo.getUserEmail()),
                               StringUtils.trimToEmpty(formattedName));
                   }
                   else {
                       email.addTo(StringUtils.trimToEmpty(em));
                   }
                }

            } else {
                email.addTo(userInfo.getUserEmail(), formattedName);

            }

            email.setFrom(applicationProperties.getEmailFromAddress(), applicationProperties.getEmailFromName());
            email.setSubject(subject);
            email.setMsg(message);

            // add the attachment
            email.attach(attachment);
            // send the email
            email.send();
        } catch (Exception e) {
            log.info(e);
            throw e;
        }

    }
    public void email(UserInfo userInfo,
                              String requestEmail,
                              String subject,
                              String message) throws Exception {

        try {
            if (StringUtils.isBlank(userInfo.getUserEmail())) {
                throw new RuntimeException("Invalid or missing email.");
            }
            String formattedName = StringUtils.trimToEmpty(userInfo.getFirstName());
            if (StringUtils.isNotBlank(formattedName)) {
                formattedName += " ";
            }
            formattedName += StringUtils.trimToEmpty(userInfo.getLastName());

            // Create the email message
            MultiPartEmail email = new MultiPartEmail();

            email.setHostName(applicationProperties.getEmailHost());
            if (applicationProperties.getEmailPort() > 0) {
                email.setSmtpPort(applicationProperties.getEmailPort());
            }
            if (applicationProperties.isEnableSSL()) {
                email.setSSLOnConnect(true);
            }
            if (applicationProperties.isEnableTLS()) {
                email.setStartTLSRequired(true);
            }
            email.setAuthenticator(
                    new DefaultAuthenticator(applicationProperties.getEmailUserId(),
                            applicationProperties.getEmailPassword()));

            if (StringUtils.containsAny(requestEmail, ";,")) {
                StringTokenizer st = new StringTokenizer(requestEmail, ",;");
                while (st.hasMoreTokens()) {
                    String em = st.nextToken();
                    if (StringUtils.equalsIgnoreCase(em, userInfo.getUserEmail())) {
                        email.addTo(
                                StringUtils.trimToEmpty(userInfo.getUserEmail()),
                                StringUtils.trimToEmpty(formattedName));
                    }
                    else {
                        email.addTo(StringUtils.trimToEmpty(em));
                    }
                }

            } else {
                email.addTo(userInfo.getUserEmail(), formattedName);

            }

            email.setFrom(applicationProperties.getEmailFromAddress(), applicationProperties.getEmailFromName());
            email.setSubject(subject);
            email.setMsg(message);

            // send the email
            email.send();
        } catch (Exception e) {
            log.info(e);
            throw e;
        }

    }


}