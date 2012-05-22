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
package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.server.filter.WarpFilter;
import org.jboss.arquillian.warp.shared.ResponsePayload;
import org.jboss.arquillian.warp.utils.SerializationUtils;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.littleshoot.proxy.HttpFilter;

public class ResponseDeenrichmentFilter implements HttpFilter {

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

        String responseEnrichment = response.getHeader(WarpFilter.ENRICHMENT_RESPONSE);

        if (responseEnrichment != null) {
            ResponsePayload payload = SerializationUtils.deserializeFromBase64(responseEnrichment);
            AssertionHolder.pushResponse(payload);
        }

        return response;
    }
}
