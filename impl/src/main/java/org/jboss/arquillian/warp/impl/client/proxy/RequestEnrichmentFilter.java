package org.jboss.arquillian.warp.impl.client.proxy;

import org.jboss.arquillian.warp.impl.client.enrichment.RequestEnrichmentService;
import org.littleshoot.proxy.HttpRequestFilter;

public interface RequestEnrichmentFilter extends HttpRequestFilter  {
    
    void setEnrichmentService(RequestEnrichmentService service);
}
