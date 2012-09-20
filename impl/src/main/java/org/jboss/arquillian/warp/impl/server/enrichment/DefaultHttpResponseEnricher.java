package org.jboss.arquillian.warp.impl.server.enrichment;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.warp.impl.server.execution.NonWritingResponse;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.arquillian.warp.spi.WarpCommons;

public class DefaultHttpResponseEnricher implements HttpResponseEnricher {

    private static final Logger log = Logger.getLogger(DefaultHttpResponseEnricher.class.getName());

    @Inject
    private Instance<ResponsePayload> responsePayload;

    @Inject
    private Instance<HttpServletResponse> response;

    @Inject
    private Instance<NonWritingResponse> nonWritingResponse;

    @Override
    public void enrichResponse() {
        try {
            enrich(responsePayload.get(), response.get(), nonWritingResponse.get());
        } catch (Exception e) {
            log.log(Level.WARNING, "Response enrichment failed", e);
            
            ResponsePayload exceptionPayload = new ResponsePayload();
            try {
                enrich(exceptionPayload, response.get(), nonWritingResponse.get());
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Response enrichment failed to attach enrichment failure", ex);
            }

        }
    }

    private void enrich(ResponsePayload payload, HttpServletResponse response, NonWritingResponse nonWritingResponse)
            throws IOException {

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
