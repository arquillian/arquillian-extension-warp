package org.jboss.arquillian.warp.impl.client.proxy;

import org.jboss.arquillian.warp.impl.client.enrichment.ResponseDeenrichmentService;
import org.littleshoot.proxy.HttpFilter;

public interface ResponseDeenrichmentFilter extends HttpFilter {

    void setDeenrichmentService(ResponseDeenrichmentService deenrichmentService);
}
