package org.jboss.arquillian.warp.impl.client.enrichment;

import org.jboss.netty.handler.codec.http.HttpResponse;

public interface ResponseDeenrichmentService {
    
    boolean isEnriched(HttpResponse response);
    
    void deenrichResponse(HttpResponse response);
    
}
