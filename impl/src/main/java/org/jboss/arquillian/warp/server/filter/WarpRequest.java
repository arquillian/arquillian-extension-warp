package org.jboss.arquillian.warp.server.filter;

import javax.servlet.http.HttpServletRequest;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.shared.RequestPayload;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.arquillian.warp.utils.SerializationUtils;

public class WarpRequest {

    private String requestEnrichment;

    public WarpRequest(HttpServletRequest request) {
        this.requestEnrichment = request.getHeader(WarpCommons.ENRICHMENT_REQUEST);
    }

    public boolean isEnriched() {
        return requestEnrichment != null;
    }

    public ServerAssertion getServerAssertion() {
        RequestPayload requestPayload = SerializationUtils.deserializeFromBase64(requestEnrichment);
        ServerAssertion serverAssertion = requestPayload.getAssertion();
        return serverAssertion;
    }

}
