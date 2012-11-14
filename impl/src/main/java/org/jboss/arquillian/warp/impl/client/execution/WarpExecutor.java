package org.jboss.arquillian.warp.impl.client.execution;

import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.client.result.WarpResult;

public interface WarpExecutor {
    
    WarpResult execute(ClientAction action, WarpContextImpl warpContext);
}
