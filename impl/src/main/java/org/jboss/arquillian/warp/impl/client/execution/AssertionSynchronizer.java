package org.jboss.arquillian.warp.impl.client.execution;

import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

public interface AssertionSynchronizer {

    void advertise();
    
    void addEnrichment(RequestEnrichment enrichment);
    
    void finish();
    
    ResponsePayload waitForResponse();
    
    void clean();
}
