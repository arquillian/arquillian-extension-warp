package org.jboss.arquillian.warp.impl.client.event;

import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.impl.client.execution.WarpContextImpl;

public class ExecuteWarp {

    private ClientAction clientAction;
    private WarpContextImpl warpContext;

    public ExecuteWarp(ClientAction clientAction, WarpContextImpl warpContext) {
        this.clientAction = clientAction;
        this.warpContext = warpContext;
    }
    
    public ClientAction getClientAction() {
        return clientAction;
    }

    public WarpContextImpl getWarpContext() {
        return warpContext;
    }
    
    
}
