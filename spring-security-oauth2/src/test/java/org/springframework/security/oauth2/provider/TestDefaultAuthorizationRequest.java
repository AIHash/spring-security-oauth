/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.oauth2.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.util.StringUtils;

/**
 * @author Dave Syer
 * @author Christian Hilmersson
 *
 */
public class TestDefaultAuthorizationRequest {

	private Map<String, String> parameters;

	@Before
	public void prepare() {
		parameters = new HashMap<String, String>();
		parameters.put("client_id", "theClient");
		parameters.put("state", "XYZ123");
		parameters.put("redirect_uri", "http://www.callistaenterprise.se");
	}
	
	private OAuth2Request createFromParameters(Map<String, String> authorizationParameters) {
		OAuth2Request request = new OAuth2Request(authorizationParameters, Collections.<String, String> emptyMap(), 
				authorizationParameters.get(OAuth2Request.CLIENT_ID), 
				OAuth2Utils.parseParameterList(authorizationParameters.get(OAuth2Request.SCOPE)), null,
				null, false, authorizationParameters.get(OAuth2Request.STATE), 
				authorizationParameters.get(OAuth2Request.REDIRECT_URI), 
				OAuth2Utils.parseParameterList(authorizationParameters.get(OAuth2Request.RESPONSE_TYPE)));
		return request;
	}
	
	@Test
	public void testApproval() throws Exception {
		OAuth2Request oAuth2Request = createFromParameters(parameters);
		assertFalse(oAuth2Request.isApproved());
		oAuth2Request.setApproved(true);
		assertTrue(oAuth2Request.isApproved());
	}

	/**
	 * Ensure that setting the scope does not alter the original request parameters.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testScopeNotSetInParameters() throws Exception {
		parameters.put("scope", "read,write");
		OAuth2Request oAuth2Request = createFromParameters(parameters);
		oAuth2Request.setScope(StringUtils.commaDelimitedListToSet("foo,bar"));
		assertFalse(oAuth2Request.getRequestParameters().get(OAuth2Request.SCOPE).contains("bar"));
		assertFalse(oAuth2Request.getRequestParameters().get(OAuth2Request.SCOPE).contains("foo"));
	}

	@Test
	public void testClientIdNotOverwitten() throws Exception {
		OAuth2Request oAuth2Request = new OAuth2Request("client", Arrays.asList("read"));
		parameters = new HashMap<String, String>();
		parameters.put("scope", "write");
		oAuth2Request.setRequestParameters(parameters);
		
		assertEquals("client", oAuth2Request.getClientId());
		assertEquals(1, oAuth2Request.getScope().size());
		assertTrue(oAuth2Request.getScope().contains("read"));
		assertFalse(oAuth2Request.getRequestParameters().get(OAuth2Request.SCOPE).contains("read"));
	}

	@Test
	public void testScopeWithSpace() throws Exception {
		parameters.put("scope", "bar foo");
		OAuth2Request oAuth2Request = createFromParameters(parameters);
		oAuth2Request.setScope(Collections.singleton("foo bar"));
		assertEquals("bar foo", oAuth2Request.getRequestParameters().get(OAuth2Request.SCOPE));
	}

	/**
	 * Tests that the construction of an AuthorizationRequest objects using
	 * a parameter Map maintains a sorted order of the scope.
	 */
	@Test
	public void testScopeSortedOrder() {
		// Arbitrary scope set
		String scopeString = "AUTHORITY_A AUTHORITY_X AUTHORITY_B AUTHORITY_C AUTHORITY_D " +
				"AUTHORITY_Y AUTHORITY_V AUTHORITY_ZZ AUTHORITY_DYV AUTHORITY_ABC AUTHORITY_BA " +
				"AUTHORITY_AV AUTHORITY_AB AUTHORITY_CDA AUTHORITY_ABCD";
		// Create correctly sorted scope string
		Set<String> sortedSet = OAuth2Utils.parseParameterList(scopeString);
		Assert.assertTrue(sortedSet instanceof SortedSet);
		String sortedScopeString = OAuth2Utils.formatParameterList(sortedSet);

		parameters.put("scope", scopeString);
		OAuth2Request oAuth2Request = createFromParameters(parameters);
		oAuth2Request.setScope(sortedSet);
				
		// Assert that the scope parameter is still sorted
		
		String fromAR = OAuth2Utils.formatParameterList(oAuth2Request.getScope());
		
		Assert.assertEquals(sortedScopeString, fromAR);
	}	

	@Test
	public void testRedirectUriDefaultsToMap() {
		parameters.put("scope", "one two");
		OAuth2Request oAuth2Request = createFromParameters(parameters);

		assertEquals("XYZ123", oAuth2Request.getState());
		assertEquals("theClient", oAuth2Request.getClientId());
		assertEquals("http://www.callistaenterprise.se", oAuth2Request.getRedirectUri());
		assertEquals("http://www.callistaenterprise.se", oAuth2Request.getRequestParameters().get(OAuth2Request.REDIRECT_URI));
		assertEquals("[one, two]", oAuth2Request.getScope().toString());
	}

}
