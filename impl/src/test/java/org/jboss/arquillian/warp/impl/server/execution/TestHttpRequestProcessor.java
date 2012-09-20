package org.jboss.arquillian.warp.impl.server.execution;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.warp.impl.server.enrichment.HttpRequestDeenricher;
import org.jboss.arquillian.warp.impl.server.enrichment.HttpResponseEnricher;
import org.jboss.arquillian.warp.impl.server.event.EnrichHttpResponse;
import org.jboss.arquillian.warp.impl.server.event.ProcessHttpRequest;
import org.jboss.arquillian.warp.impl.server.event.ProcessWarpRequest;
import org.jboss.arquillian.warp.impl.server.request.RequestContext;
import org.jboss.arquillian.warp.impl.server.request.RequestScoped;
import org.jboss.arquillian.warp.impl.server.testbase.AbstractWarpTestTestBase;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestHttpRequestProcessor extends AbstractWarpTestTestBase {

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
    }

    @Before
    public void setup() {

        // having
        bind(ApplicationScoped.class, ServiceLoader.class, services);
        bind(RequestScoped.class, HttpServletRequest.class, request);
        bind(RequestScoped.class, HttpServletResponse.class, response);
        bind(RequestScoped.class, FilterChain.class, filterChain);
        when(services.onlyOne(HttpRequestDeenricher.class)).thenReturn(deenricher);
        when(services.onlyOne(HttpResponseEnricher.class)).thenReturn(enricher);
    }

    @Test
    public void when_request_is_not_enriched_then_warp_executes_filter_chain() throws IOException, ServletException {

        // having
        when(deenricher.isEnriched()).thenReturn(false);

        // when
        fire(new ProcessHttpRequest());

        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void when_request_is_not_enriched_then_warp_request_is_not_executed() {

        // having
        when(deenricher.isEnriched()).thenReturn(false);

        // when
        fire(new ProcessHttpRequest());

        // then
        assertEventNotFiredInContext(ProcessWarpRequest.class, RequestContext.class);
    }

    @Test
    public void when_request_is_enriched_then_warp_request_is_executed() {

        // having
        when(deenricher.isEnriched()).thenReturn(true);

        // when
        fire(new ProcessHttpRequest());

        // then
        assertEventFired(ProcessWarpRequest.class);
    }

    @Test
    public void when_request_is_enriched_then_request_payload_is_deenriched_and_set_to_request_context() {

        // having
        when(deenricher.isEnriched()).thenReturn(true);
        when(deenricher.resolvePayload()).thenReturn(requestPayload);

        // when
        fire(new ProcessHttpRequest());

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
        fire(new ProcessHttpRequest());

        // then
        ResponsePayload responsePayload = getManager().getContext(RequestContext.class).getObjectStore()
                .get(ResponsePayload.class);
        assertNotNull("response payload is not null", responsePayload);
        assertNull("response payload has empty assertion", responsePayload.getAssertion());
        assertNull("response payload has empty throwable", responsePayload.getThrowable());
        assertNull("response payload has empty test result", responsePayload.getTestResult());
    }

    @Test
    public void when_request_deenrichment_fails_then_response_payload_is_filled_with_throwable() {

        // having
        when(deenricher.isEnriched()).thenReturn(true);
        when(deenricher.resolvePayload()).thenThrow(IllegalStateException.class);

        // when
        fire(new ProcessHttpRequest());

        // then
        ResponsePayload responsePayload = getManager().getContext(RequestContext.class).getObjectStore()
                .get(ResponsePayload.class);
        assertNotNull("response payload is not null", responsePayload);
        assertNull("response payload has empty assertion", responsePayload.getAssertion());
        assertTrue("response payload has set throwable with deenrichment exception",
                responsePayload.getThrowable() instanceof IllegalStateException);
        assertNull("response payload has empty test result", responsePayload.getTestResult());
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
        ResponsePayload responsePayload = new ResponsePayload();
        bind(RequestScoped.class, ResponsePayload.class, responsePayload);
        doThrow(IllegalStateException.class).when(enricher).enrichResponse();

        // when
        fire(new EnrichHttpResponse());

        // then
        verify(enricher).enrichResponse();
        ResponsePayload resolvedResponsePayload = getManager().getContext(RequestContext.class).getObjectStore()
                .get(ResponsePayload.class);
        assertSame(responsePayload, resolvedResponsePayload);
        assertNull("response payload has empty assertion", responsePayload.getAssertion());
        assertNull("response payload has empty test result", responsePayload.getTestResult());
        assertTrue("response payload has set throwable with response enrichment exception",
                responsePayload.getThrowable() instanceof IllegalStateException);
    }

}
