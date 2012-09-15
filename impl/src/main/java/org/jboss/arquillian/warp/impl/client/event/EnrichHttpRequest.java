package org.jboss.arquillian.warp.impl.client.event;

import java.util.Collection;

import org.jboss.arquillian.warp.impl.client.enrichment.RequestEnrichmentService;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class EnrichHttpRequest implements EnrichRequest<HttpRequest, RequestPayload> {

    private HttpRequest request;
    private Collection<RequestPayload> payloads;
    private RequestEnrichmentService service;

    public EnrichHttpRequest(HttpRequest request, Collection<RequestPayload> payload, RequestEnrichmentService service) {
        super();
        this.request = request;
        this.payloads = payload;
        this.service = service;
    }

    @Override
    public HttpRequest getRequest() {
        return request;
    }

    @Override
    public Collection<RequestPayload> getPayloads() {
        return payloads;
    }
    
    @Override
    public RequestEnrichmentService getService() {
        return service;
    }
}
