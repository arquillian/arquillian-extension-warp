/**
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.warp.impl.client.execution;

import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;
import org.jboss.arquillian.warp.impl.client.enrichment.HttpResponseDeenrichmentService;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * Default service for de-enriching responses.
 *
 * @author Lukas Fryc
 */
public class DefaultResponseDeenrichmentService implements HttpResponseDeenrichmentService {

    private final Logger log = Logger.getLogger(HttpResponseDeenrichmentService.class.getName());

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.impl.client.enrichment.HttpResponseDeenrichmentService#isEnriched(org.jboss.netty.handler.codec.http.HttpResponse)
     */
    @Override
    public boolean isEnriched(HttpResponse response) {
        return getHeader(response) != null;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.impl.client.enrichment.HttpResponseDeenrichmentService#deenrichResponse(org.jboss.netty.handler.codec.http.HttpResponse)
     */
    @Override
    public void deenrichResponse(HttpResponse response) {
        final WarpContext context = WarpContextStore.get();
        try {

            final ChannelBuffer content = response.getContent();

            long originalLength = HttpHeaders.getContentLength(response);
            int payloadLength = Integer.valueOf(getHeader(response));

            String responseEnrichment = content.toString(0, payloadLength, Charset.defaultCharset());
            content.readerIndex(payloadLength);
            content.discardReadBytes();

            ResponsePayload payload = SerializationUtils.deserializeFromBase64(responseEnrichment);

            HttpHeaders.setContentLength(response, originalLength - payloadLength);

            HttpResponseStatus status = HttpResponseStatus.valueOf(payload.getStatus());
            response.setStatus(status);

            if (context != null) {
                context.pushResponsePayload(payload);
            }
        } catch (Exception originalException) {
            if (context != null) {
                ClientWarpExecutionException explainingException = new ClientWarpExecutionException("deenriching response failed: "
                    + originalException.getMessage(), originalException);

                context.pushException(explainingException);
            } else {
                log.log(Level.WARNING, "Unable to push exception to WarpContext", originalException);
            }
        }
    }

    private String getHeader(HttpResponse response) {
        return response.getHeader(WarpCommons.ENRICHMENT_RESPONSE);
    }
}
