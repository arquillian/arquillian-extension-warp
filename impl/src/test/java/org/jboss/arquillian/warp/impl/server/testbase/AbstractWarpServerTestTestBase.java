package org.jboss.arquillian.warp.impl.server.testbase;

import java.util.List;

import org.jboss.arquillian.container.test.test.AbstractContainerTestTestBase;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.context.Context;
import org.jboss.arquillian.warp.impl.server.request.RequestContext;
import org.jboss.arquillian.warp.impl.server.request.RequestContextImpl;

public abstract class AbstractWarpServerTestTestBase extends AbstractContainerTestTestBase  {

    @Override
    protected void addContexts(List<Class<? extends Context>> contexts) {
        super.addContexts(contexts);
        contexts.add(RequestContextImpl.class);
    }

    @Override
    protected void startContexts(Manager manager) {
        super.startContexts(manager);
        manager.getContext(RequestContext.class).activate();
    }
    
    
}
