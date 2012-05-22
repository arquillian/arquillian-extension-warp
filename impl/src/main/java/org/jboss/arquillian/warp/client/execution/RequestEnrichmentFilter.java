package org.jboss.arquillian.warp.client.execution;

import java.util.Arrays;

import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;
import org.jboss.arquillian.warp.server.filter.WarpFilter;
import org.jboss.arquillian.warp.shared.RequestPayload;
import org.jboss.arquillian.warp.shared.ResponsePayload;
import org.jboss.arquillian.warp.utils.SerializationUtils;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.HttpRequestFilter;

public class RequestEnrichmentFilter implements HttpRequestFilter {

    @Override
    public void filter(HttpRequest request) {
        if (AssertionHolder.isWaitingForProcessing()) {
            try {
                RequestPayload assertion = AssertionHolder.popRequest();
                String requestEnrichment = SerializationUtils.serializeToBase64(assertion);
                request.setHeader(WarpFilter.ENRICHMENT_REQUEST, Arrays.asList(requestEnrichment));
            } catch (Exception originalException) {
                ClientWarpExecutionException wrappedException = new ClientWarpExecutionException("enriching request failed: "
                        + originalException.getMessage(), originalException);
                ResponsePayload exceptionPayload = new ResponsePayload(wrappedException);
                AssertionHolder.pushResponse(exceptionPayload);
            }
        }
    }
}
