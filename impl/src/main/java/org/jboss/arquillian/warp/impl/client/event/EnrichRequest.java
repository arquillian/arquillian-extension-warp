package org.jboss.arquillian.warp.impl.client.event;

import java.util.Collection;

import org.jboss.arquillian.warp.impl.client.enrichment.RequestEnrichmentService;

public interface EnrichRequest<T, P> {

    T getRequest();
    
    Collection<P> getPayloads();
    
    RequestEnrichmentService getService();
}
