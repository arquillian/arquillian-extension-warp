package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.client.result.WarpResult;

public interface GroupExecutor {
    
    WarpResult verifyAll();
    
    ExecutionGroup group(Object identifier);
    
}
