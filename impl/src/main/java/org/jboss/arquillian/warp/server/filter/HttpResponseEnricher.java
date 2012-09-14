package org.jboss.arquillian.warp.server.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.warp.server.filter.WarpRequestProcessor.NonWritingResponse;
import org.jboss.arquillian.warp.shared.ResponsePayload;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.arquillian.warp.utils.SerializationUtils;

public class HttpResponseEnricher {
    
    public void enrichHttpResponseBody(@Observes EnrichHttpResponse event, HttpServletResponse response, NonWritingResponse nonWritingResponse) throws IOException {
        final ResponsePayload payload = event.getPayload();
        
        String enrichment = SerializationUtils.serializeToBase64(payload);

        // set a header with the size of the payload
        response.setHeader(WarpCommons.ENRICHMENT_RESPONSE, Integer.toString(enrichment.length()));

        if (nonWritingResponse.getContentLength() != null) {
            nonWritingResponse.setContentLength(nonWritingResponse.getContentLength() + enrichment.length());
        }

        nonWritingResponse.finalize();

        response.getOutputStream().write(enrichment.getBytes());
    }
}
