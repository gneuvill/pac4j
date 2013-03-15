/*
  Copyright 2012 - 2013 Jerome Leleu

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */package org.pac4j.http.client;

import junit.framework.TestCase;

import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.MockWebContext;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.profile.ProfileHelper;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.TestsConstants;
import org.pac4j.core.util.TestsHelper;
import org.pac4j.http.credentials.SimpleTestUsernamePasswordAuthenticator;
import org.pac4j.http.credentials.UsernamePasswordAuthenticator;
import org.pac4j.http.credentials.UsernamePasswordCredentials;
import org.pac4j.http.profile.HttpProfile;
import org.pac4j.http.profile.ProfileCreator;
import org.pac4j.http.profile.UsernameProfileCreator;

/**
 * This class tests the {@link FormClient} class.
 * 
 * @author Jerome Leleu
 * @since 1.4.0
 */
public final class TestFormClient extends TestCase implements TestsConstants {
    
    public void testClone() {
        final FormClient oldClient = new FormClient();
        oldClient.setCallbackUrl(CALLBACK_URL);
        oldClient.setName(TYPE);
        oldClient.setPasswordParameter(PASSWORD);
        oldClient.setUsernameParameter(USERNAME);
        final ProfileCreator profileCreator = new UsernameProfileCreator();
        oldClient.setProfileCreator(profileCreator);
        final UsernamePasswordAuthenticator usernamePasswordAuthenticator = new SimpleTestUsernamePasswordAuthenticator();
        oldClient.setUsernamePasswordAuthenticator(usernamePasswordAuthenticator);
        final FormClient client = (FormClient) oldClient.clone();
        assertEquals(oldClient.getCallbackUrl(), client.getCallbackUrl());
        assertEquals(oldClient.getName(), client.getName());
        assertEquals(oldClient.getUsernameParameter(), client.getUsernameParameter());
        assertEquals(oldClient.getPasswordParameter(), client.getPasswordParameter());
        assertEquals(oldClient.getProfileCreator(), client.getProfileCreator());
        assertEquals(oldClient.getUsernamePasswordAuthenticator(), client.getUsernamePasswordAuthenticator());
    }
    
    public void testMissingUsernamePasswordAuthenticator() {
        final FormClient formClient = new FormClient(LOGIN_URL, null, new UsernameProfileCreator());
        TestsHelper.initShouldFail(formClient, "usernamePasswordAuthenticator cannot be null");
    }
    
    public void testMissingProfileCreator() {
        final FormClient formClient = new FormClient(LOGIN_URL, new SimpleTestUsernamePasswordAuthenticator(), null);
        TestsHelper.initShouldFail(formClient, "profileCreator cannot be null");
    }
    
    public void testMissingLoginUrl() {
        final FormClient formClient = new FormClient(null, new SimpleTestUsernamePasswordAuthenticator(),
                                                     new UsernameProfileCreator());
        TestsHelper.initShouldFail(formClient, "loginUrl cannot be blank");
    }
    
    private FormClient getFormClient() {
        return new FormClient(LOGIN_URL, new SimpleTestUsernamePasswordAuthenticator(), new UsernameProfileCreator());
    }
    
    public void testRedirectionUrl() {
        final FormClient formClient = getFormClient();
        assertEquals(LOGIN_URL, formClient.getRedirectionUrl(MockWebContext.create()));
    }
    
    public void testGetCredentialsMissingUsername() {
        final FormClient formClient = getFormClient();
        final MockWebContext context = MockWebContext.create();
        try {
            formClient.getCredentials(context.addRequestParameter(formClient.getUsernameParameter(), USERNAME));
            fail("should fail");
        } catch (final RequiresHttpAction e) {
            assertEquals("Username and password cannot be blank -> return to the form with error", e.getMessage());
            assertEquals(302, context.getResponseStatus());
            assertEquals(LOGIN_URL + "?" + formClient.getUsernameParameter() + "=" + USERNAME + "&"
                         + FormClient.ERROR_PARAMETER + "=" + FormClient.MISSING_FIELD_ERROR, context
                .getResponseHeaders().get(HttpConstants.LOCATION_HEADER));
        }
    }
    
    public void testGetCredentialsMissingPassword() {
        final FormClient formClient = getFormClient();
        final MockWebContext context = MockWebContext.create();
        try {
            formClient.getCredentials(context.addRequestParameter(formClient.getPasswordParameter(), PASSWORD));
            fail("should fail");
        } catch (final RequiresHttpAction e) {
            assertEquals("Username and password cannot be blank -> return to the form with error", e.getMessage());
            assertEquals(302, context.getResponseStatus());
            assertEquals(LOGIN_URL + "?" + formClient.getUsernameParameter() + "=&" + FormClient.ERROR_PARAMETER + "="
                             + FormClient.MISSING_FIELD_ERROR,
                         context.getResponseHeaders().get(HttpConstants.LOCATION_HEADER));
        }
    }
    
    public void testGetCredentials() {
        final FormClient formClient = getFormClient();
        final MockWebContext context = MockWebContext.create();
        try {
            formClient.getCredentials(context.addRequestParameter(formClient.getUsernameParameter(), USERNAME)
                .addRequestParameter(formClient.getPasswordParameter(), PASSWORD));
            fail("should fail");
        } catch (final RequiresHttpAction e) {
            assertEquals("Credentials validation fails -> return to the form with error", e.getMessage());
            assertEquals(302, context.getResponseStatus());
            assertEquals(LOGIN_URL + "?" + formClient.getUsernameParameter() + "=" + USERNAME + "&"
                         + FormClient.ERROR_PARAMETER + "=" + CredentialsException.class.getSimpleName(), context
                .getResponseHeaders().get(HttpConstants.LOCATION_HEADER));
        }
    }
    
    public void testGetRightCredentials() throws RequiresHttpAction {
        final FormClient formClient = getFormClient();
        final UsernamePasswordCredentials credentials = formClient.getCredentials(MockWebContext.create()
            .addRequestParameter(formClient.getUsernameParameter(), USERNAME)
            .addRequestParameter(formClient.getPasswordParameter(), USERNAME));
        assertEquals(USERNAME, credentials.getUsername());
        assertEquals(USERNAME, credentials.getPassword());
    }
    
    public void testGetUserProfile() {
        final FormClient formClient = getFormClient();
        final HttpProfile profile = formClient.getUserProfile(new UsernamePasswordCredentials(USERNAME, USERNAME,
                                                                                              formClient.getName()));
        assertEquals(USERNAME, profile.getId());
        assertEquals(HttpProfile.class.getSimpleName() + UserProfile.SEPARATOR + USERNAME, profile.getTypedId());
        assertTrue(ProfileHelper.isTypedIdOf(profile.getTypedId(), HttpProfile.class));
        assertEquals(USERNAME, profile.getUsername());
        assertEquals(1, profile.getAttributes().size());
    }
}
