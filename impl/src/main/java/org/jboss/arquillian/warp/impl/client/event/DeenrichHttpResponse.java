package org.jboss.arquillian.warp.impl.client.event;

import org.jboss.arquillian.warp.impl.client.enrichment.ResponseDeenrichmentService;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class DeenrichHttpResponse implements DeenrichResponse<HttpResponse> {

    private HttpResponse response;
    private ResponseDeenrichmentService deenrichmentService;

    public DeenrichHttpResponse(HttpResponse response, ResponseDeenrichmentService deenrichmentService) {
        this.response = response;
        this.deenrichmentService = deenrichmentService;
    }

    public HttpResponse getResponse() {
        return response;
    }

    @Override
    public ResponseDeenrichmentService getService() {
        return deenrichmentService;
    }

}
