package com.parserbox.model.sso;

import java.util.ArrayList;
import java.util.List;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class OAuthAdapter {

	public Log log = LogFactory.getLog(this.getClass());

	public String baseUrl;
	public String requestTokenUrl;
	public String accessTokenUrl;
	public String authorizeUrl;

	public String oauthConsumerKey;
	public String oauthConsumerSecret;

	public String authCallBackUrl;

	
	public OAuthConsumer oauthconsumer;


	public String appid;

	OAuthAccessToken tokenObject;

	public OAuthAdapter(OAuthAccessToken tokenObject, String authCallBackUrl) throws Exception {
		this.tokenObject = tokenObject;


		this.appid = tokenObject.getAppId__c();
		this.authCallBackUrl = authCallBackUrl;
		init();
	}

	public void init() throws Exception {

		// If token object not found, user will have to authenticate with OAuth service.
		/*
		this.tokenObject = EJBLocator.getSSOSession().getOAuthAccessTokenByUser(userid, appid);

		this.requestTokenUrl = Globals.getSystemSetting(appid + "RequestTokenUrl");
		this.accessTokenUrl = Globals.getSystemSetting(appid + "AccessTokenUrl");
		this.authorizeUrl = Globals.getSystemSetting(appid + "AuthorizeUrl");
		this.oauthConsumerKey = Globals.getSystemSetting(appid + "OAuthConsumerKey");
		this.oauthConsumerSecret = Globals.getSystemSetting(appid + "OAuthConsumerSecret");
		this.baseUrl = Globals.getSystemSetting(appid + "BaseURL");		
		*/

		this.requestTokenUrl = "https://oauth.intuit.com/oauth/v1/get_request_token";
		this.accessTokenUrl = "https://oauth.intuit.com/oauth/v1/get_access_token";
		this.authorizeUrl = "https://appcenter.intuit.com/connect/begin";
		this.oauthConsumerKey = "qyprdO8ulY5mMdImaFEJYX9qmHg30i";
		this.oauthConsumerSecret = "8rS313osaPjbNydqDuKt1XdIBiuWW6dPQF9yTDbE";


		List<String> missing = new ArrayList<>();
		if (StringUtils.isBlank(requestTokenUrl)) missing.add("requestTokenUrl");
		if (StringUtils.isBlank(accessTokenUrl)) missing.add("accessTokenUrl");
		if (StringUtils.isBlank(authorizeUrl)) missing.add("authorizeUrl");
		if (StringUtils.isBlank(oauthConsumerKey)) missing.add("oauthConsumerKey");
		if (StringUtils.isBlank(oauthConsumerSecret)) missing.add("oauthConsumerSecret");
		//if (StringUtils.isBlank(baseUrl)) missing.add("baseUrl");
		if (StringUtils.isBlank(authCallBackUrl)) missing.add("authCallBackUrl");
		
		
		
		if (missing.size() > 0 ) {
			throw new RuntimeException("The following variables have not been set : " + missing.toArray());
		}
		
		
		
	}

	public String getAuthenticationURL() throws Exception {
		
		// Invoke the helper class and retrieve the request token.

		// Initialize the Provider class with the request token, access token and authorize URLs
		OAuthProvider provider = new DefaultOAuthProvider(requestTokenUrl, accessTokenUrl, authorizeUrl);

		this.oauthconsumer = new DefaultOAuthConsumer(oauthConsumerKey, oauthConsumerSecret);

		// The retrieveRequestToken method in the signpost library calls the
		// request token URL configured in the OAuthProvider object
		// to retrieve the token and sends the response to the URL
		// configured in the Oauth Callback URL configured in the properties
		// file
		String authUrl = provider.retrieveRequestToken(oauthconsumer, authCallBackUrl);

		return authUrl;
	}
	
	
	public OAuthConsumer getAuthConsumer() throws Exception {
		if (tokenObject == null)
			return null;
		OAuthConsumer ouathconsumer = new DefaultOAuthConsumer(oauthConsumerKey, oauthConsumerSecret);
		ouathconsumer.setTokenWithSecret(tokenObject.getAccessToken__c(), tokenObject.getAccessTokenSecret__c());

		return ouathconsumer;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}
	
	

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getRequestTokenUrl() {
		return requestTokenUrl;
	}

	public void setRequestTokenUrl(String requestTokenUrl) {
		this.requestTokenUrl = requestTokenUrl;
	}

	public String getAccessTokenUrl() {
		return accessTokenUrl;
	}

	public void setAccessTokenUrl(String accessTokenUrl) {
		this.accessTokenUrl = accessTokenUrl;
	}

	public String getAuthorizeUrl() {
		return authorizeUrl;
	}

	public void setAuthorizeUrl(String authorizeUrl) {
		this.authorizeUrl = authorizeUrl;
	}

	public String getOauthConsumerKey() {
		return oauthConsumerKey;
	}

	public void setOauthConsumerKey(String oauthConsumerKey) {
		this.oauthConsumerKey = oauthConsumerKey;
	}

	public String getOauthConsumerSecret() {
		return oauthConsumerSecret;
	}

	public void setOauthConsumerSecret(String oauthConsumerSecret) {
		this.oauthConsumerSecret = oauthConsumerSecret;
	}



	public String getAuthCallBackUrl() {
		return authCallBackUrl;
	}

	public void setAuthCallBackUrl(String authCallBackUrl) {
		this.authCallBackUrl = authCallBackUrl;
	}

	public OAuthAccessToken getTokenObject() {
		return tokenObject;
	}

	public void setTokenObject(OAuthAccessToken tokenObject) {
		this.tokenObject = tokenObject;
	}

	public OAuthConsumer getOauthconsumer() {
		return oauthconsumer;
	}

	public void setOauthconsumer(OAuthConsumer oauthconsumer) {
		this.oauthconsumer = oauthconsumer;
	}

}
