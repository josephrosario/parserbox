package com.parserbox.vendors.intuit.controller;


import com.parserbox.model.ResponseContainer;

import com.parserbox.model.sso.OAuthAccessToken;
import com.parserbox.vendors.intuit.model.Quickbooks;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@RestController
@RequestMapping("/QuickbooksRestController")
public class QuickbooksRestController {

    private Log log = LogFactory.getLog(this.getClass());


    @RequestMapping(value = "/getSessionOAuthAccessToken", method = RequestMethod.POST)
    public
    @ResponseBody
    org.json.simple.JSONObject getSessionOAuthAccessToken(HttpServletRequest request) throws Exception {
        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();

        OAuthAccessToken token = (OAuthAccessToken)request.getSession().getAttribute("OAuthAccessToken");
        String origin = (String)request.getSession().getAttribute("origin");

        obj.put("success", true);
        obj.put("OAuthAccessToken", token);
        obj.put("origin", origin);

        return obj;

    }


    /**
     * Authenticates the user by redirecting the user to the remote authentication page.
     * @param request
     * @return JSON Object
     * @throws Exception
     */
    public ResponseContainer handleAuthentication(OAuthAccessToken oAuthAccessToken, HttpServletRequest request) throws Exception {

        ResponseContainer responseContainer = new ResponseContainer();

        HttpSession session = request.getSession();
        Quickbooks quickbooks = new Quickbooks(oAuthAccessToken, getCallBackUrl(request));

        // Invoke the helper class and retrieve the request token.
        OAuthConsumer oauthconsumer;

        // Initialize the Provider class with the request token, access token
        // and authorize URLs
        OAuthProvider provider = new DefaultOAuthProvider(quickbooks.getRequestTokenUrl(), quickbooks.getAccessTokenUrl(), quickbooks.getAuthorizeUrl());
        try {
            // Read the consumer key and secret from the Properties file to
            // create the OauthConsumer object
            oauthconsumer = new DefaultOAuthConsumer(quickbooks.getOauthConsumerKey(), quickbooks.getOauthConsumerSecret());
            // The retrieveRequestToken method in the signpost library calls the
            // request token URL configured in the OAuthProvider object
            // to retrieve the token and sends the response to the URL
            // configured in the Oauth Callback URL configured in the properties
            // file
            String authUrl = provider.retrieveRequestToken(oauthconsumer, quickbooks.getAuthCallBackUrl());
            session.setAttribute("oauthConsumer", oauthconsumer);
            responseContainer.setUrl(authUrl);
        } catch (Exception e) {
            throw e;
        }
        responseContainer.setStatus("OK");
        return responseContainer;
    }

    /**
     * The callback URL used by the authentication server to redirect the user back to this application.
     * @param request
     * @return
     * @throws Exception
     */
    public String getCallBackUrl(HttpServletRequest request) throws Exception {
        String authCallBackUrl = request.getRequestURL().toString() + "/acceptToken";
        return authCallBackUrl;
    }



}
