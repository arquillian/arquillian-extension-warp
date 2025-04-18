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
package org.jboss.arquillian.warp.impl.server.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.warp.impl.server.enrichment.HttpRequestDeenricher;
import org.jboss.arquillian.warp.impl.server.enrichment.HttpResponseEnricher;
import org.jboss.arquillian.warp.impl.server.event.EnrichHttpResponse;
import org.jboss.arquillian.warp.impl.server.test.TestResultObserver;
import org.jboss.arquillian.warp.impl.server.testbase.AbstractWarpServerTestTestBase;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.jboss.arquillian.warp.spi.context.RequestContext;
import org.jboss.arquillian.warp.spi.context.RequestScoped;
import org.jboss.arquillian.warp.spi.servlet.event.ProcessHttpRequest;
import org.jboss.arquillian.warp.spi.servlet.event.ProcessWarpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestHttpRequestProcessor extends AbstractWarpServerTestTestBase {

    @Mock
    private ServiceLoader services;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpRequestDeenricher deenricher;

    @Mock
    private HttpResponseEnricher enricher;

    @Mock
    private RequestPayload requestPayload;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        super.addExtensions(extensions);
        extensions.add(HttpRequestProcessor.class);
        extensions.add(TestResultObserver.class);
    }

    @BeforeEach
    public void setup() {

        // having
        bind(ApplicationScoped.class, ServiceLoader.class, services);
        bind(RequestScoped.class, HttpServletRequest.class, request);
        bind(RequestScoped.class, HttpServletResponse.class, response);
        bind(RequestScoped.class, FilterChain.class, filterChain);
        // Mockito for JUnit5 requires more "lenient" calls, as the MockitoExtension seems to validate the stubbings created in "@BeforeEach" methods for each test,
        // but not all test methods call all stubbed methods.
        lenient().when(services.onlyOne(HttpRequestDeenricher.class)).thenReturn(deenricher);
        lenient().when(services.onlyOne(HttpResponseEnricher.class)).thenReturn(enricher);
    }

    @Test
    public void when_request_is_not_enriched_then_warp_executes_filter_chain() throws IOException, ServletException {

        // having
        when(deenricher.isEnriched()).thenReturn(false);

        // when
        fire(new ProcessHttpRequest(request, response, filterChain));

        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void when_request_is_not_enriched_then_warp_request_is_not_executed() {

        // having
        when(deenricher.isEnriched()).thenReturn(false);

        // when
        fire(new ProcessHttpRequest(request, response, filterChain));

        // then
        assertEventNotFiredInContext(ProcessWarpRequest.class, RequestContext.class);
    }

    @Test
    public void when_request_is_enriched_then_warp_request_is_executed() {

        // having
        when(deenricher.isEnriched()).thenReturn(true);
        when(deenricher.resolvePayload()).thenReturn(requestPayload);

        // when
        fire(new ProcessHttpRequest(request, response, filterChain));

        // then
        assertEventFired(ProcessWarpRequest.class);
    }

    @Test
    public void when_request_is_enriched_then_request_payload_is_deenriched_and_set_to_request_context() {

        // having
        when(deenricher.isEnriched()).thenReturn(true);
        when(deenricher.resolvePayload()).thenReturn(requestPayload);

        // when
        fire(new ProcessHttpRequest(request, response, filterChain));

        // then
        RequestPayload resolvedRequestPayload = getManager().getContext(RequestContext.class).getObjectStore()
            .get(RequestPayload.class);
        assertSame(requestPayload, resolvedRequestPayload);
    }

    @Test
    public void when_request_is_enriched_then_empty_response_payload_is_set_to_request_context() {

        // having
        when(deenricher.isEnriched()).thenReturn(true);
        when(deenricher.resolvePayload()).thenReturn(requestPayload);

        // when
        fire(new ProcessHttpRequest(request, response, filterChain));

        // then
        ResponsePayload responsePayload = getManager().getContext(RequestContext.class).getObjectStore()
            .get(ResponsePayload.class);
        assertNotNull(responsePayload, "response payload is not null");
        assertNull(responsePayload.getInspections(), "response payload has empty inspection");
        assertNull(responsePayload.getTestResult(), "response payload has empty test result");
    }

    @Test
    public void when_request_deenrichment_fails_then_response_payload_is_filled_with_throwable() {

        // having
        RuntimeException exception = new RuntimeException();
        when(deenricher.isEnriched()).thenReturn(true);
        when(deenricher.resolvePayload()).thenThrow(exception);

        // when
        try {
            fire(new ProcessHttpRequest(request, response, filterChain));
            fail();
        } catch (Exception e) {
            assertEquals(exception, e);
        }

        // then
        ResponsePayload responsePayload = getManager().getContext(RequestContext.class).getObjectStore()
            .get(ResponsePayload.class);
        assertNotNull(responsePayload, "response payload is not null");
        assertNull(responsePayload.getInspections(), "response payload has empty inspection");

        TestResult testResult = responsePayload.getTestResult();
        assertNotNull(testResult, "response payload has test result");
        assertEquals(testResult.getThrowable(), exception);
    }

    @Test
    public void when_response_enrichment_event_occurs_then_response_enrichment_service_is_invoked() {

        // when
        fire(new EnrichHttpResponse());

        // then
        verify(enricher).enrichResponse();
    }

    @Test
    public void when_response_enrichment_fails_then_response_payload_throwable_is_filled() {

        // having
        RuntimeException exception = new RuntimeException();
        ResponsePayload responsePayload = new ResponsePayload(-1L);
        bind(RequestScoped.class, ResponsePayload.class, responsePayload);
        doThrow(exception).when(enricher).enrichResponse();

        // when
        try {
            fire(new EnrichHttpResponse());
            fail();
        } catch (Exception e) {
            assertEquals(exception, e);
        }

        // then
        verify(enricher).enrichResponse();
        ResponsePayload resolvedResponsePayload = getManager().getContext(RequestContext.class).getObjectStore()
            .get(ResponsePayload.class);
        assertSame(responsePayload, resolvedResponsePayload);
        assertNull(responsePayload.getInspections(), "response payload has empty inspection");

        TestResult testResult = responsePayload.getTestResult();
        assertEquals(exception, testResult.getThrowable());
    }
}
