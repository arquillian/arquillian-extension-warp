package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.RequestFilter;
import org.jboss.arquillian.warp.shared.RequestPayload;

public class RequestEnrichment {

    private RequestPayload payload;
    private RequestFilter filter;

    public RequestEnrichment(RequestPayload payload, RequestFilter filter) {
        this.payload = payload;
        this.filter = filter;
    }

    public RequestPayload getPayload() {
        return payload;
    }

    public RequestFilter getFilter() {
        return filter;
    }
}
