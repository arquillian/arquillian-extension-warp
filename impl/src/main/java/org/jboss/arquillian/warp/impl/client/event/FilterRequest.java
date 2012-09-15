package org.jboss.arquillian.warp.impl.client.event;

import org.jboss.arquillian.warp.impl.client.enrichment.RequestEnrichmentService;

public interface FilterRequest<T> {

    T getRequest();
    
    RequestEnrichmentService getService();
}
