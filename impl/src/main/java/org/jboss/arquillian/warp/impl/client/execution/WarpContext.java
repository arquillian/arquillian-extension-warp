package org.jboss.arquillian.warp.impl.client.execution;

import java.util.Collection;

import org.jboss.arquillian.warp.client.result.WarpResult;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

public interface WarpContext {
    
    void addGroup(RequestGroupImpl group);
    
    // TODO diverge interface
    Collection<RequestGroupImpl> getAllGroups();
    
    void pushResponsePayload(ResponsePayload payload);
    
    void pushException(Exception exception);
    
    void tryFinalizeResponse();
    
    SynchronizationPoint getSynchronization();
    
}
