/*
  Copyright 2012 Jerome Leleu

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.scribe.up.test.provider.impl;

import junit.framework.TestCase;

import org.scribe.model.Token;
import org.scribe.up.credential.OAuthCredential;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.provider.impl.YahooProvider;
import org.scribe.up.test.util.SingleUserSession;
import org.scribe.up.test.util.WebHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * This class tests the YahooProvider by simulating a complete authentication.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public final class TestYahooProvider extends TestCase {
    
    private static final Logger logger = LoggerFactory.getLogger(TestYahooProvider.class);
    
    public void testProvider() throws Exception {
        // init provider
        YahooProvider yahooProvider = new YahooProvider();
        yahooProvider
            .setKey("dj0yJmk9ekJzMk9vQnJIVk1ZJmQ9WVdrOVUxaE5Za3R0TmpJbWNHbzlOVEUyTmpFME1EWXkmcz1jb25zdW1lcnNlY3JldCZ4PWNk");
        yahooProvider.setSecret("7bb5e2585da4ce9032d6a9cddb556ce7ffc493f7");
        yahooProvider.setCallbackUrl("http://www.google.com/");
        yahooProvider.init();
        
        // authorization url
        SingleUserSession testSession = new SingleUserSession();
        String authorizationUrl = yahooProvider.getAuthorizationUrl(testSession);
        logger.debug("authorizationUrl : {}", authorizationUrl);
        WebClient webClient = WebHelper.newClient();
        HtmlPage loginPage = webClient.getPage(authorizationUrl);
        HtmlForm form = loginPage.getFormByName("login_form");
        HtmlTextInput login = form.getInputByName("login");
        login.setValueAttribute("testscribeup@yahoo.fr");
        HtmlPasswordInput passwd = form.getInputByName("passwd");
        passwd.setValueAttribute("testpwdscribeup");
        HtmlSubmitInput submit = form.getInputByName(".save");
        HtmlPage confirmPage = submit.click();
        form = confirmPage.getFormByName("rcForm");
        submit = form.getInputByName("agree");
        HtmlPage callbackPage = submit.click();
        String callbackUrl = callbackPage.getUrl().toString();
        logger.debug("callbackUrl : {}", callbackUrl);
        
        OAuthCredential credential = yahooProvider.getCredential(WebHelper.getParametersFromUrl(callbackUrl));
        // access token
        Token accessToken = yahooProvider.getAccessToken(testSession, credential);
        logger.debug("accessToken : {}", accessToken);
        // user profile
        UserProfile userProfile = yahooProvider.getUserProfile(accessToken);
        logger.debug("userProfile : {}", userProfile);
        assertEquals("PCSXZCYSWC6XUJNMZKRGWVPHNU", userProfile.getId());
        assertEquals("Test", userProfile.getAttributes().get("nickname"));
        assertEquals(8, userProfile.getAttributes().size());
    }
}
