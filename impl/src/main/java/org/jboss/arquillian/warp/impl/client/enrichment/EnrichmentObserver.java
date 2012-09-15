package org.jboss.arquillian.warp.impl.client.enrichment;

import java.util.Collection;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.warp.impl.client.event.DeenrichHttpResponse;
import org.jboss.arquillian.warp.impl.client.event.EnrichHttpRequest;
import org.jboss.arquillian.warp.impl.client.event.FilterHttpResponse;
import org.jboss.arquillian.warp.impl.client.event.FilterHttpRequest;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class EnrichmentObserver {

    @Inject
    private Event<EnrichHttpRequest> enrichHttpRequest;

    @Inject
    private Event<DeenrichHttpResponse> deenrichHttpResponse;

    public void tryEnrichRequest(@Observes FilterHttpRequest event) {
        final HttpRequest request = event.getRequest();
        final RequestEnrichmentService enrichmentService = event.getService();

        Collection<RequestPayload> matchingPayloads = enrichmentService.getMatchingPayloads(request);

        if (!matchingPayloads.isEmpty()) {
            enrichHttpRequest.fire(new EnrichHttpRequest(request, matchingPayloads, enrichmentService));
        }
    }

    public void enrichRequest(@Observes EnrichHttpRequest event) {
        final HttpRequest request = event.getRequest();
        final Collection<RequestPayload> payloads = event.getPayloads();
        final RequestEnrichmentService enrichmentService = event.getService();

        enrichmentService.enrichRequest(request, payloads);
    }

    public void tryDeenrichResponse(@Observes FilterHttpResponse event) {
        final HttpResponse response = event.getResponse();
        final ResponseDeenrichmentService service = event.getService();

        if (service.isEnriched(response)) {
            deenrichHttpResponse.fire(new DeenrichHttpResponse(response, service));
        }
    }

    public void deenrichResponse(@Observes DeenrichHttpResponse event) {
        final HttpResponse response = event.getResponse();
        final ResponseDeenrichmentService service = event.getService();

        service.deenrichResponse(response);
    }
}
