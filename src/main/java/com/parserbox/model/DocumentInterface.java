package com.parserbox.model;

public interface DocumentInterface {

    ParsingTemplate getParsingTemplate();
    String getDocSourceTitle();
    String getDocSourceExtension();
    UserInfo getUserInfo();
    ApplicationProperties getApplicationProperties();
}
