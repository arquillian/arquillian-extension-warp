package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.shared.ResponsePayload;

public class ResponseEnrichment {

    private ResponsePayload payload;

    public ResponseEnrichment(ResponsePayload payload) {
        this.payload = payload;
    }

    public ResponsePayload getPayload() {
        return payload;
    }
}
