/*
 *  Copyright 2017 predic8 GmbH, www.predic8.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.predic8.membrane.core.interceptor.oauth2;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({AuthWithoutSessionOpenidRequestTest.class,
        AuthWithSessionRequestTest.class,
        AuthWithoutSessionRequestTest.class,
        ClaimsParameterTest.class,
        EmptyEndpointTest.class,
        EmptyEndpointOpenidTest.class,
        OAuth2AuthorizationServerInterceptorNormalTest.class,
        OAuth2AuthorizationServerInterceptorOpenidTest.class,
        OAuth2ResourceTest.class,
        PasswordGrantTest.class,
        RevocationRequestTest.class,
        TokenRequestTest.class,
        UserinfoRequestTest.class,
        WellknownFileTest.class})
public class OAuth2UnitTests {
}

