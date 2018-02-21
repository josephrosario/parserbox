package com.parserbox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class ParserBoxApplication {
    private static Log log = LogFactory.getLog(ParserBoxApplication.class);

    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WebApplicationContext.class);
    }

    @PostConstruct
    public void init() {
        log.info("Started ParserBox Appliction");
    }

    public static void main(String[] args) {
        SpringApplication.run(ParserBoxApplication.class, args);
        log.info("Version: " + Constants.version);
    }
}
