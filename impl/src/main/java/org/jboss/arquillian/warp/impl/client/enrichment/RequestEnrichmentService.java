package org.jboss.arquillian.warp.impl.client.enrichment;

import java.util.Collection;

import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.netty.handler.codec.http.HttpRequest;

public interface RequestEnrichmentService {

    Collection<RequestPayload> getMatchingPayloads(HttpRequest request);
    
    void enrichRequest(HttpRequest request, Collection<RequestPayload> payloads);
}
