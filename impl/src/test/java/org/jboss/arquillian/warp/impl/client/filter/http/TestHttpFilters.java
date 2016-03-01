/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.warp.impl.client.filter.http;

import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.warp.WarpRuntimeInitializer;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.client.filter.http.HttpFilterBuilder;
import org.jboss.arquillian.warp.client.filter.http.HttpFilters;
import org.jboss.arquillian.warp.client.filter.http.HttpMethod;
import org.jboss.arquillian.warp.client.filter.http.HttpRequest;
import org.jboss.arquillian.warp.impl.client.execution.DefaultExecutionSynchronizer;
import org.jboss.arquillian.warp.impl.client.execution.DefaultWarpExecutor;
import org.jboss.arquillian.warp.impl.client.execution.DefaultWarpRequestSpecifier;
import org.jboss.arquillian.warp.impl.client.execution.ExecutionSynchronizer;
import org.jboss.arquillian.warp.impl.client.execution.WarpContext;
import org.jboss.arquillian.warp.impl.client.execution.WarpContextImpl;
import org.jboss.arquillian.warp.impl.client.execution.WarpExecutionInitializer;
import org.jboss.arquillian.warp.impl.client.execution.WarpExecutionObserver;
import org.jboss.arquillian.warp.impl.client.execution.WarpExecutor;
import org.jboss.arquillian.warp.impl.client.execution.WarpRequestSpecifier;
import org.jboss.arquillian.warp.impl.client.testbase.AbstractWarpClientTestTestBase;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * <p>Tests the {@link HttpFilters} class.</p>
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHttpFilters extends AbstractWarpClientTestTestBase {

    /**
     * Represents a url to resource, used for testing purpose.
     */
    private static final String TEST_URL = "/test/index.html";

    /**
     * Represents a url to resource, used for testing purpose.
     */
    private static final String TEST_INVALID_URL = "/style/main.css";

    /**
     * Represents the instance of builder class used for testing.
     */
    private HttpFilterBuilder builder;

    /**
     * Represents the instance of {@link HttpRequest} used for testing.
     */
    private HttpRequest request;

    /**
     * Represents the instance of {@link HttpRequest} used for testing.
     */
    private HttpRequest invalidRequest;

    /**
     * A mock instance of {@link ServiceLoader} that is used to make Arquillian injection running.
     */
    @Mock
    private ServiceLoader serviceLoader;

    /**
     * Registers the Warp runtime.
     */
    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(WarpRuntimeInitializer.class);
        extensions.add(DefaultWarpRequestSpecifier.class);
        extensions.add(WarpExecutionObserver.class);
        extensions.add(WarpExecutionInitializer.class);
    }

    /**
     * Sets up the test environment.
     */
    @Before
    public void setUp() {

        // given
        setUpWarpRuntime();

        // creates the the requests
        request = createRequest(HttpMethod.GET, TEST_URL);
        invalidRequest = createRequest(HttpMethod.POST, TEST_INVALID_URL);

        fire(new BeforeClass(TestingClass.class));
    }

    /**
     * Tears down the test environment.
     */
    public void tearDown() {

        fire(new AfterClass(TestingClass.class));
    }

    /**
     * Tests the creating of {@link HttpFilterBuilder} without any preconditions.
     * Filter created in such manner should accept any request.
     */
    @Test
    public void testShouldAcceptAll() {

        // when
        builder = HttpFilters.request();

        // then
        assertRequestMatches(builder, request);
    }

    /**
     * Tests the creating of {@link HttpFilterBuilder} that matches only request that has equal uri address.
     */
    @Test
    public void testShouldMatchUri() {

        // when
        builder = HttpFilters.request().uri().equal(TEST_URL);

        // then
        assertRequestMatches(builder, request);
        assertRequestNotMatches(builder, invalidRequest);
    }

    /**
     * Tests the creating of {@link HttpFilterBuilder} that matches only request that has equal uri address.
     */
    @Test
    public void testShouldNotMatchUri() {

        // when
        builder = HttpFilters.request().uri().not().equal(TEST_URL);

        // then
        assertRequestNotMatches(builder, request);
        assertRequestMatches(builder, invalidRequest);
    }

    /**
     * Tests the creating of {@link HttpFilterBuilder} that matches only request that has equal uri address.
     */
    @Test
    public void testShouldMatchUriIgnoreCase() {

        // when
        builder = HttpFilters.request().uri().equalIgnoreCase(TEST_URL.toUpperCase());

        // then
        assertRequestMatches(builder, request);
        assertRequestNotMatches(builder, invalidRequest);
    }

    /**
     * Tests the creating of {@link HttpFilterBuilder} that matches only request that has equal uri address.
     */
    @Test
    public void testShouldNotMatchUriIgnoreCase() {

        // when
        builder = HttpFilters.request().uri().not().equalIgnoreCase(TEST_URL.toUpperCase());

        // then
        assertRequestNotMatches(builder, request);
        assertRequestMatches(builder, invalidRequest);
    }

    /**
     * Tests the creating of {@link HttpFilterBuilder} that matches only request that contains given substring.
     */
    @Test
    public void testShouldContainUri() {

        // when
        builder = HttpFilters.request().uri().contains("index");

        // then
        assertRequestMatches(builder, request);
        assertRequestNotMatches(builder, invalidRequest);
    }

    /**
     * Tests the creating of {@link HttpFilterBuilder} that matches only request that contains given substring.
     */
    @Test
    public void testShouldNotContainUri() {

        // when
        builder = HttpFilters.request().uri().not().contains("index");

        // then
        assertRequestNotMatches(builder, request);
        assertRequestMatches(builder, invalidRequest);
    }

    /**
     * Tests the creating of {@link HttpFilterBuilder} that matches only request that starts with given substring.
     */
    @Test
    public void testShouldStartsWithUri() {

        // when
        builder = HttpFilters.request().uri().startsWith("/test");

        // then
        assertRequestMatches(builder, request);
        assertRequestNotMatches(builder, invalidRequest);
    }

    /**
     * Tests the creating of {@link HttpFilterBuilder} that matches only request that starts with given substring.
     */
    @Test
    public void testShouldNotStartsWithUri() {

        // when
        builder = HttpFilters.request().uri().not().startsWith("/test");

        // then
        assertRequestNotMatches(builder, request);
        assertRequestMatches(builder, invalidRequest);
    }

    /**
     * Tests the creating of {@link HttpFilterBuilder} that matches only request that ends with given substring.
     */
    @Test
    public void testShouldEndWithUri() {

        // when
        builder = HttpFilters.request().uri().endsWith(".html");

        // then
        assertRequestMatches(builder, request);
        assertRequestNotMatches(builder, invalidRequest);
    }

    /**
     * Tests the creating of {@link HttpFilterBuilder} that matches only request that ends with given substring.
     */
    @Test
    public void testShouldNotEndWithUri() {

        // when
        builder = HttpFilters.request().uri().not().endsWith(".html");

        // then
        assertRequestNotMatches(builder, request);
        assertRequestMatches(builder, invalidRequest);
    }

    /**
     * Tests the creating of {@link HttpFilterBuilder} that matches only request that matches given regular expression.
     */
    @Test
    public void testShouldMatchReqex() {

        // when
        builder = HttpFilters.request().uri().matches("/?(\\w+/)*index.html");

        // then
        assertRequestMatches(builder, request);
        assertRequestNotMatches(builder, invalidRequest);
    }

    /**
     * Tests the creating of {@link HttpFilterBuilder} that matches only request that matches given regular expression.
     */
    @Test
    public void testShouldNotMatchReqex() {

        // when
        builder = HttpFilters.request().uri().not().matches("/?(\\w+/)*index.html");

        // then
        assertRequestNotMatches(builder, request);
        assertRequestMatches(builder, invalidRequest);
    }

    /**
     * Tests the creating of {@link HttpFilterBuilder} that matches only request that matches given method.
     */
    @Test
    public void testShouldMatchMethod() {

        // when
        builder = HttpFilters.request().method().equal(HttpMethod.GET);

        // then
        assertRequestMatches(builder, request);
        assertRequestNotMatches(builder, invalidRequest);
    }

    /**
     * Tests the creating of {@link HttpFilterBuilder} that matches only request that matches given method.
     */
    @Test
    public void testShouldNotMatchMethod() {

        // when
        builder = HttpFilters.request().method().not().equal(HttpMethod.GET);

        // then
        assertRequestNotMatches(builder, request);
        assertRequestMatches(builder, invalidRequest);
    }

    /**
     * Tests the creating of {@link HttpFilterBuilder} that matches exactly the given request.
     */
    @Test
    public void testShouldMatchExact() {

        // when
        builder = HttpFilters.request().method().equal(HttpMethod.GET).uri().endsWith(".html");

        // then
        assertRequestMatches(builder, request);
        assertRequestNotMatches(builder, invalidRequest);
    }

    /**
     * Sets up the Arquillian runtime.
     */
    private void setUpWarpRuntime() {

        WarpRequestSpecifier requestExecutor = new DefaultWarpRequestSpecifier();
        getManager().inject(requestExecutor);

        ExecutionSynchronizer inspectionSynchronizer = new DefaultExecutionSynchronizer();
        getManager().inject(inspectionSynchronizer);

        WarpExecutor warpExecutor = new DefaultWarpExecutor();
        getManager().inject(warpExecutor);


        WarpContext warpContext = new WarpContextImpl();

        when(serviceLoader.onlyOne(WarpRequestSpecifier.class)).thenReturn(requestExecutor);
        when(serviceLoader.onlyOne(ExecutionSynchronizer.class)).thenReturn(inspectionSynchronizer);
        when(serviceLoader.onlyOne(WarpExecutor.class)).thenReturn(warpExecutor);
        when(serviceLoader.onlyOne(WarpContext.class)).thenReturn(warpContext);
        when(serviceLoader.onlyOne(HttpFilterBuilder.class)).thenReturn(new DefaultHttpFilterBuilder());

        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
    }

    /**
     * Verifies that the given filer matches the specified request.
     *
     * @param builder the filter builder
     * @param request the request
     */
    private static void assertRequestMatches(HttpFilterBuilder builder, HttpRequest request) {

        assertTrue("The filter does not match the request.", builder.build().matches(request));
    }

    /**
     * Verifies that the given filer does not match the specified request.
     *
     * @param builder the filter builder
     * @param request the request
     */
    private static void assertRequestNotMatches(HttpFilterBuilder builder, HttpRequest request) {

        assertFalse("The filter does not match the request.", builder.build().matches(request));
    }

    /**
     * Creates an instance of {@link HttpRequest} from the specified parameters.
     *
     * @param method the request http method
     * @param uri    the request url
     *
     * @return the created request instance
     */
    private static HttpRequest createRequest(HttpMethod method, String uri) {

        HttpRequest result = mock(HttpRequest.class);
        when(result.getMethod()).thenReturn(method);
        when(result.getUri()).thenReturn(uri);

        return result;
    }

    /**
     * A dummy class used for triggering the Warp internal events.
     */
    @WarpTest
    @RunAsClient
    public static final class TestingClass {
        @Deployment
        public static Archive deploy(){
            return null;
        }
    }
}
