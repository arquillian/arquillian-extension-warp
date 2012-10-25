package org.jboss.arquillian.warp.impl.client.execution;

import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

import org.jboss.arquillian.warp.impl.shared.RequestPayload;

public interface WarpResultStore {
    
    void setResult(RequestPayload request, ResponsePayload payload);
}
