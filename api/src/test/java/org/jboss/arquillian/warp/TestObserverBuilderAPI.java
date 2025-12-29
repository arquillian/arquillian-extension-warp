/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp;

import static org.jboss.arquillian.warp.client.filter.http.HttpFilters.request;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.jboss.arquillian.warp.client.execution.SingleInspectionSpecifier;
import org.jboss.arquillian.warp.client.execution.WarpExecutionBuilder;
import org.jboss.arquillian.warp.client.filter.http.HttpFilterBuilder;
import org.jboss.arquillian.warp.client.filter.http.HttpFilters;
import org.jboss.arquillian.warp.client.filter.http.HttpMethod;
import org.jboss.arquillian.warp.client.filter.matcher.HttpHeaderMatcherBuilder;
import org.jboss.arquillian.warp.client.filter.matcher.MethodMatcherBuilder;
import org.jboss.arquillian.warp.client.filter.matcher.UriMatcherBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestObserverBuilderAPI {

    private Activity activity;
    private Inspection inspection;

    @Mock
    private WarpExecutionBuilder mockBuilder;

    @Mock
    private HttpFilterBuilder mockFilterBuilder;

    @Mock
    private UriMatcherBuilder<HttpFilterBuilder> mockUriMatcherBuilder;

    @Mock
    private MethodMatcherBuilder<HttpFilterBuilder> mockMethodMatcherBuilder;

    @Mock
    private HttpHeaderMatcherBuilder<HttpFilterBuilder> mockHeaderMatcherBuilder;

    @Mock
    private SingleInspectionSpecifier mockInspectionSpecifier;

    private MockedStatic<Warp> mockWarp;
    private MockedStatic<HttpFilters> mockFilters;

    /**
     * Single client activity and server inspection applied for request matching
     * given URI
     */
    @Test
    public void testFilterBuilderUriNot() {
        // Prepare a lot of mocks:

        // "Warp.initiate(activity)"
        mockWarp.when(() -> Warp.initiate(activity)).thenReturn(mockBuilder);

        // "HttpFilters.request"()
        mockFilters.when(() -> HttpFilters.request()).thenReturn(mockFilterBuilder);

        // "request().uri()" => returns a "UriMatcherBuilder<...>"
        when(mockFilterBuilder.uri()).thenReturn(mockUriMatcherBuilder);

        // "request().uri().not()" is again a "UriMatcherBuilder":
        when(mockUriMatcherBuilder.not()).thenReturn(mockUriMatcherBuilder);

        // "request().uri().not().endsWith(...)" is a "HttpFilterBuilder":
        when(mockUriMatcherBuilder.endsWith(".jsf")).thenReturn(mockFilterBuilder);

        // ".observe(..)" returns a "SingleInspectionSpecifier".
        when(mockBuilder.observe(mockFilterBuilder)).thenReturn(mockInspectionSpecifier);

        // The final step: ".inspect(inspection)":
        // Return value does not matter:
        when(mockInspectionSpecifier.inspect(inspection)).thenReturn(inspection);

        // Now invoke the test:
        Warp
            .initiate(activity)
            .observe(request().uri().not().endsWith(".jsf"))
            .inspect(inspection);
    }

    /**
     * Single client activity and server inspection applied for request matching
     * given HTTP method
     */
    @Test
    public void testFilterBuilderMethod() {
        // Prepare a lot of mocks:

        // "Warp.initiate(activity)"
        mockWarp.when(() -> Warp.initiate(activity)).thenReturn(mockBuilder);

        // "HttpFilters.request"()
        mockFilters.when(() -> HttpFilters.request()).thenReturn(mockFilterBuilder);

        // "request().method()" => returns a "MethodMatcherBuilder<...>"
        when(mockFilterBuilder.method()).thenReturn(mockMethodMatcherBuilder);

        // "request().method().not()" is again a "MethodMatcherBuilder":
        when(mockMethodMatcherBuilder.not()).thenReturn(mockMethodMatcherBuilder);

        // "request().method().not().equal(...)" is a "HttpFilterBuilder":
        when(mockMethodMatcherBuilder.equal(HttpMethod.POST)).thenReturn(mockFilterBuilder);

        // ".observe(...)" returns a "SingleInspectionSpecifier".
        when(mockBuilder.observe(mockFilterBuilder)).thenReturn(mockInspectionSpecifier);

        // The final step: ".inspect(inspection)":
        // Return value does not matter:
        when(mockInspectionSpecifier.inspect(inspection)).thenReturn(inspection);

        // Now invoke the test:
        Warp
            .initiate(activity)
            .observe(request().method().not().equal(HttpMethod.POST))
            .inspect(inspection);
    }

    /**
     * Single client activity and server inspection applied for request not matching
     * given HTTP header
     */
    @Test
    public void testFilterBuilderHeaderNot() {
        // Prepare a lot of mocks:

        // "Warp.initiate(activity)"
        mockWarp.when(() -> Warp.initiate(activity)).thenReturn(mockBuilder);

        // "HttpFilters.request"()
        mockFilters.when(() -> HttpFilters.request()).thenReturn(mockFilterBuilder);

        // "request().header()" => returns a "HttpHeaderMatcherBuilder<...>"
        when(mockFilterBuilder.header()).thenReturn(mockHeaderMatcherBuilder);

        // "request().header().not()" is again a "HttpHeaderMatcherBuilder":
        when(mockHeaderMatcherBuilder.not()).thenReturn(mockHeaderMatcherBuilder);

        // "request().header().not().equal(containsValue(...))" is a "HttpFilterBuilder":
        when(mockHeaderMatcherBuilder.containsValue("Accept", "application/json")).thenReturn(mockFilterBuilder);

        // ".observe(...)" returns a "SingleInspectionSpecifier".
        when(mockBuilder.observe(mockFilterBuilder)).thenReturn(mockInspectionSpecifier);

        // The final step: ".inspect(inspection)":
        // Return value does not matter:
        when(mockInspectionSpecifier.inspect(inspection)).thenReturn(inspection);

        // Now invoke the test:
        Warp
            .initiate(activity)
            .observe(request().header().not().containsValue("Accept", "application/json"))
            .inspect(inspection);
    }

    /**
     * Single client activity and server inspection applied for request matching
     * given condition
     */
    @Test
    public void testFilterBuilderComplex() {

        // Prepare a lot of mocks:

        // "Warp.initiate(activity)"
        mockWarp.when(() -> Warp.initiate(activity)).thenReturn(mockBuilder);

        // "HttpFilters.request"()
        mockFilters.when(() -> HttpFilters.request()).thenReturn(mockFilterBuilder);

        // "request().uri()" => returns a "UriMatcherBuilder<...>"
        when(mockFilterBuilder.uri()).thenReturn(mockUriMatcherBuilder);

        // "request().uri().endsWith()" is again a "HttpFilterBuilder":
        when(mockUriMatcherBuilder.endsWith("resource/Client/1")).thenReturn(mockFilterBuilder);

        // "request().uri().endsWith().method()" is a "MethodMatcherBuilder":
        when(mockFilterBuilder.method()).thenReturn(mockMethodMatcherBuilder);

        // "request().uri().endsWith().method().equal(...)" is a "HttpFilterBuilder":
        when(mockMethodMatcherBuilder.equal(HttpMethod.POST)).thenReturn(mockFilterBuilder);

        // "request().uri().endsWith().method().equal(...).header()" is a "HttpHeaderFilterBuilder":
        when(mockFilterBuilder.header()).thenReturn(mockHeaderMatcherBuilder);

        // "request().uri().endsWith().method().equal(...).header().containsValue(...)" is a "HttpFilterBuilder":
        when(mockHeaderMatcherBuilder.containsValue("Cookie", "name=Client")).thenReturn(mockFilterBuilder);

        // ".observe(...)" returns a "SingleInspectionSpecifier".
        when(mockBuilder.observe(mockFilterBuilder)).thenReturn(mockInspectionSpecifier);

        // The final step: ".inspect(inspection)":
        // Return value does not matter:
        when(mockInspectionSpecifier.inspect(inspection)).thenReturn(inspection);

        // Now invoke the test:
        Warp
            .initiate(activity)
            .observe(request().uri().endsWith("resource/Client/1")
                .method().equal(HttpMethod.POST)
                .header().containsValue("Cookie", "name=Client"))
            .inspect(inspection);
    }

    @BeforeEach
    public void before() {
        this.mockWarp = mockStatic(Warp.class);
        this.mockFilters = mockStatic(HttpFilters.class);
    }

    @AfterEach
    public void after() {
        this.mockWarp.close();
        this.mockFilters.close();
    }
}
