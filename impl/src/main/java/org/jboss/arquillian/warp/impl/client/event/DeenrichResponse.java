package org.jboss.arquillian.warp.impl.client.event;

import org.jboss.arquillian.warp.impl.client.enrichment.ResponseDeenrichmentService;

public interface DeenrichResponse<T> {
    
    T getResponse();
    
    ResponseDeenrichmentService getService();
}
