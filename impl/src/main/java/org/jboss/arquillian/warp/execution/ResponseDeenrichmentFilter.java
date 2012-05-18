package org.jboss.arquillian.warp.execution;

import org.jboss.arquillian.warp.assertion.ResponsePayload;
import org.jboss.arquillian.warp.filter.WarpFilter;
import org.jboss.arquillian.warp.utils.SerializationUtils;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.littleshoot.proxy.HttpFilter;

public class ResponseDeenrichmentFilter implements HttpFilter {
    
    @Override
    public boolean shouldFilterResponses(HttpRequest httpRequest) {
        return true;
    }

    @Override
    public int getMaxResponseSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public HttpResponse filterResponse(HttpResponse response) {

        String responseEnrichment = response.getHeader(WarpFilter.ENRICHMENT_RESPONSE);

        if (responseEnrichment != null) {
            ResponsePayload payload = SerializationUtils.deserializeFromBase64(responseEnrichment);
            AssertionHolder.pushResponse(payload);
        }

        return response;
    }
}
