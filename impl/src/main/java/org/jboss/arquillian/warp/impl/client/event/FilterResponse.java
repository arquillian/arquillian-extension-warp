package org.jboss.arquillian.warp.impl.client.event;

import org.jboss.arquillian.warp.impl.client.enrichment.ResponseDeenrichmentService;
import org.jboss.netty.handler.codec.http.HttpResponse;

public interface FilterResponse<T> {
    
    T getResponse();
    
    ResponseDeenrichmentService getService();

}
