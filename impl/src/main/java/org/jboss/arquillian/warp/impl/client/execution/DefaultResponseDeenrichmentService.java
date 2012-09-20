package org.jboss.arquillian.warp.impl.client.execution;

import java.nio.charset.Charset;

import org.jboss.arquillian.warp.impl.client.enrichment.ResponseDeenrichmentService;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class DefaultResponseDeenrichmentService implements ResponseDeenrichmentService {

    @Override
    public boolean isEnriched(HttpResponse response) {
        return getHeader(response) != null;
    }

    @Override
    public void deenrichResponse(HttpResponse response) {
        try {
            int payloadLength = Integer.valueOf(getHeader(response));
            ChannelBuffer content = response.getContent();
            String responseEnrichment = content.toString(0, payloadLength, Charset.defaultCharset());
            content.readerIndex(payloadLength);
            content.discardReadBytes();

            long originalLength = HttpHeaders.getContentLength(response);
            HttpHeaders.setContentLength(response, originalLength - payloadLength);

            ResponsePayload payload = SerializationUtils.deserializeFromBase64(responseEnrichment);
            AssertionHolder.addResponse(new ResponseEnrichment(payload));
        } catch (Exception e) {
            ResponsePayload exceptionPayload = new ResponsePayload();
            exceptionPayload.setThrowable(e);
            AssertionHolder.addResponse(new ResponseEnrichment(exceptionPayload));
        }
    }

    private String getHeader(HttpResponse response) {
        return response.getHeader(WarpCommons.ENRICHMENT_RESPONSE);
    }
}
