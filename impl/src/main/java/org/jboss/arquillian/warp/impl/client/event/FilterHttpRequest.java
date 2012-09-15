package org.jboss.arquillian.warp.impl.client.event;

import org.jboss.arquillian.warp.impl.client.enrichment.RequestEnrichmentService;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class FilterHttpRequest implements FilterRequest<HttpRequest> {

    private HttpRequest request;
    private RequestEnrichmentService service;

    public FilterHttpRequest(HttpRequest request, RequestEnrichmentService service) {
        super();
        this.request = request;
        this.service = service;
    }

    @Override
    public HttpRequest getRequest() {
        return request;
    }
    
    @Override
    public RequestEnrichmentService getService() {
        return service;
    }
}
