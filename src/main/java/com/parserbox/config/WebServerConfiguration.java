package com.parserbox.config;

import com.parserbox.model.ApplicationProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class WebServerConfiguration {
    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    ApplicationProperties applicationProperties;

    @PostConstruct
    public void init() {
           log.info("WebServer Configuration - ParserBox Appliction");
    }
}


