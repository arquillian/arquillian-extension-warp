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

import java.util.Collection;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.warp.client.exception.MultipleGroupsPerRequestException;
import org.jboss.arquillian.warp.client.filter.http.HttpRequest;
import org.jboss.arquillian.warp.impl.client.enrichment.HttpRequestEnrichmentService;
import org.jboss.arquillian.warp.impl.client.enrichment.HttpResponseDeenrichmentService;
import org.jboss.arquillian.warp.impl.client.event.DeenrichHttpResponse;
import org.jboss.arquillian.warp.impl.client.event.EnrichHttpRequest;
import org.jboss.arquillian.warp.impl.client.event.FilterHttpRequest;
import org.jboss.arquillian.warp.impl.client.event.FilterHttpResponse;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 * Listens on filter and tries enrich request and de-enrich response
 *
 * @author Lukas Fryc
 */
public class EnrichmentObserver {

    @Inject
    private Event<EnrichHttpRequest> enrichHttpRequest;

    @Inject
    private Event<DeenrichHttpResponse> deenrichHttpResponse;

    public void tryEnrichRequest(@Observes FilterHttpRequest event) {
        final HttpRequest request = event.getRequest();
        final HttpRequestEnrichmentService enrichmentService = event.getService();

        if (WarpCommons.debugMode()) {
            System.out.println("        (R) " + request.getUri());
        }

        Collection<RequestPayload> matchingPayloads = enrichmentService.getMatchingPayloads(request);

        if (matchingPayloads.isEmpty()) {
            warpContext().addUnmatchedRequest(request);
        } else {
            if (matchingPayloads.size() > 1) {
                warpContext().pushException(new MultipleGroupsPerRequestException(request.getUri()));
            } else {
                enrichHttpRequest.fire(new EnrichHttpRequest(request, matchingPayloads.iterator().next(), enrichmentService));
            }
        }
    }

    public void enrichRequest(@Observes EnrichHttpRequest event) {
        final HttpRequest request = event.getRequest();
        final RequestPayload payload = event.getPayload();
        final HttpRequestEnrichmentService enrichmentService = event.getService();

        enrichmentService.enrichRequest(request, payload);
    }

    public void tryDeenrichResponse(@Observes FilterHttpResponse event) {
        final HttpResponse response = event.getResponse();
        final HttpResponseDeenrichmentService service = event.getService();

        if (service.isEnriched(response)) {
            deenrichHttpResponse.fire(new DeenrichHttpResponse(response, service));
        }
    }

    public void deenrichResponse(@Observes DeenrichHttpResponse event) {
        final HttpResponse response = event.getResponse();
        final HttpResponseDeenrichmentService service = event.getService();

        service.deenrichResponse(response);
    }

    private WarpContext warpContext() {
        return WarpContextStore.get();
    }
}
