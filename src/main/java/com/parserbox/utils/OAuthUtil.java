package com.parserbox.utils;

import com.parserbox.model.RequestContainer;
import com.parserbox.model.sso.OAuthAccessToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;

public class OAuthUtil {
    public OAuthUtil(){}

    public OAuth2ClientContext getClientContext(RequestContainer requestContainer)  throws Exception{
        OAuthAccessToken t = requestContainer.getoAuthAccessToken();
        if (t == null || StringUtils.isBlank(t.getAccessToken__c())) {
            throw new RuntimeException("Invalid or missing access token");
        }
        OAuth2ClientContext clientContext = new DefaultOAuth2ClientContext(new DefaultOAuth2AccessToken(t.getAccessToken__c()));

        return clientContext;
    }

}
