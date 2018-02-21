package com.parserbox.vendors.intuit.controller;

import com.parserbox.model.ApplicationProperties;
import com.parserbox.model.sso.OAuthAccessToken;
import com.parserbox.sevice.UploadServiceImpl;
import com.parserbox.vendors.intuit.model.Quickbooks;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.http.HttpParameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Controller
@RequestMapping("/QuickbooksController")
public class QuickbooksController {

    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    UploadServiceImpl uploadService;

    @Autowired
    MultipartProperties multipartProperties;


    @Autowired
    ApplicationProperties applicationProperties;




    @RequestMapping("/handleAuthentication")

    public String handleAuthentication(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        OAuthAccessToken tokenObject = new OAuthAccessToken();
        tokenObject.setAppId__c("quickbooks");
        String origin = request.getParameter("origin");
        log.info(origin);

        Quickbooks quickbooks = new Quickbooks(tokenObject, getCallBackUrl(request));

        // Invoke the helper class and retrieve the request token.
        OAuthConsumer oauthconsumer;

        // Initialize the Provider class with the request token, access token
        // and authorize URLs
        OAuthProvider provider = new DefaultOAuthProvider(quickbooks.getRequestTokenUrl(), quickbooks.getAccessTokenUrl(), quickbooks.getAuthorizeUrl());

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
        session.setAttribute("origin", origin);
        return "redirect:" + authUrl;
    }


    /**
     * The callback URL used by the authentication server to redirect the user back to this application.
     * @param request
     * @return
     * @throws Exception
     */
    public String getCallBackUrl(HttpServletRequest request) throws Exception {
        String authCallBackUrl = getURLLessFile(request) + "/acceptTokenCallBack";
        return authCallBackUrl;
    }

    public String getURLLessFile(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        int index = url.lastIndexOf("/");
        if (index <= 0)
            return null;

        String u = url.substring(0, index);
        return u;
    }


    @RequestMapping("/acceptTokenCallBack")

    public String acceptTokenCallBack(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            HttpSession session = request.getSession();
            //  String userid = "ablsoft";
            request.getSession().getAttribute("");
            OAuthAccessToken tokenObject = new OAuthAccessToken();
            tokenObject.setAppId__c("quickbooks");

            Quickbooks quickbooks = new Quickbooks(tokenObject, getCallBackUrl(request));

            //  String loanNum = (String) request.getSession().getAttribute(Constants.LOAN_NUMBER);
            //  String loanNum = "ABC";

            // The realm Id is returned in the callback and read into the
            // session
            String realmID = request.getParameter("realmId");
            session.setAttribute("realmId", realmID);

            OAuthConsumer oauthconsumer = (OAuthConsumer) session.getAttribute("oauthConsumer");

            HttpParameters additionalParams = new HttpParameters();
            additionalParams.put("oauth_callback", OAuth.OUT_OF_BAND);
            additionalParams.put("oauth_verifier", request.getParameter("oauth_verifier"));
            oauthconsumer.setAdditionalParameters(additionalParams);

            // Sign the call to retrieve the access token request
            String signedURL = oauthconsumer.sign(quickbooks.getAccessTokenUrl());
            URL url = new URL(signedURL);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            String accesstokenresponse = "";
            String accessToken, accessTokenSecret = "";
            if (urlConnection != null) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
                accesstokenresponse = sb.toString();
            }
            if (accesstokenresponse != null) {
                String[] responseElements = accesstokenresponse.split("&");
                if (responseElements.length > 1) {
                    accessToken = responseElements[1].split("=")[1];
                    accessTokenSecret = responseElements[0].split("=")[1];
                    OAuthAccessToken token = new OAuthAccessToken(quickbooks.getAppid(), accessToken, accessTokenSecret);
                    token.setSpecialId1__c(realmID);

                    session.setAttribute("accessToken", accessToken);
                    session.setAttribute("accessTokenSecret", accessTokenSecret);

                    quickbooks.setTokenObject(token);
                    session.setAttribute("OAuthAccessToken", token);
                    return "redirect:oauthconnected.html";
                }
            }

        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
        return "";
    }

}