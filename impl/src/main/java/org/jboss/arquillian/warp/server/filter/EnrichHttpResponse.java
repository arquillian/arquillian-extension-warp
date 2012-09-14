package org.jboss.arquillian.warp.server.filter;

import org.jboss.arquillian.warp.shared.ResponsePayload;
import org.jboss.arquillian.warp.spi.event.EnrichResponse;

public class EnrichHttpResponse implements EnrichResponse<ResponsePayload> {

    private ResponsePayload payload;

    public EnrichHttpResponse(ResponsePayload payload) {
        this.payload = payload;
    }

    @Override
    public ResponsePayload getPayload() {
        return payload;
    }

}
