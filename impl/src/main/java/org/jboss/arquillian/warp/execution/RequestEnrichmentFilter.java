package org.jboss.arquillian.warp.execution;

import java.util.Arrays;
import java.util.logging.Logger;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.filter.WarpFilter;
import org.jboss.arquillian.warp.utils.SerializationUtils;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.HttpRequestFilter;

public class RequestEnrichmentFilter implements HttpRequestFilter {

    private static Logger log = Logger.getLogger("Proxy");

    @Override
    public void filter(HttpRequest request) {
        if (AssertionHolder.isWaitingForProcessing()) {
            try {
                ServerAssertion assertion = AssertionHolder.popRequest();
                String requestEnrichment = SerializationUtils.serializeToBase64(assertion);
                request.setHeader(WarpFilter.ENRICHMENT_REQUEST, Arrays.asList(requestEnrichment));
            } catch (Exception e) {
                log.severe("enriching request failed: " + e.getMessage());
            }
        }
    }
}
