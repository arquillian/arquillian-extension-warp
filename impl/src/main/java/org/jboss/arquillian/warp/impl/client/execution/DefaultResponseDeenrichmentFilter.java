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

import org.jboss.arquillian.warp.impl.client.proxy.ResponseDeenrichmentFilter;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class DefaultResponseDeenrichmentFilter implements ResponseDeenrichmentFilter {

    @Override
    public boolean shouldFilterResponses(HttpRequest httpRequest) {
        return true;
    }

    @Override
    public int getMaxResponseSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public HttpResponse filterResponse(HttpResponse response) {

        String header = response.getHeader(WarpCommons.ENRICHMENT_RESPONSE);

        if (header != null) {
            int payloadLength = Integer.valueOf(header);
            ChannelBuffer content = response.getContent();
            String responseEnrichment = content.toString(0, payloadLength, Charset.defaultCharset());
            content.readerIndex(payloadLength);
            content.discardReadBytes();

            long originalLength = HttpHeaders.getContentLength(response);
            HttpHeaders.setContentLength(response, originalLength - payloadLength);

            ResponsePayload payload = SerializationUtils.deserializeFromBase64(responseEnrichment);
            ResponseEnrichment enrichment = new ResponseEnrichment(payload);
            AssertionHolder.addResponse(enrichment);
        }

        return response;
    }
}
