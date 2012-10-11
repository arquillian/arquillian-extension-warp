package org.jboss.arquillian.warp.impl.client.testbase;

import java.util.List;

import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.context.Context;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionContext;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionContextImpl;

public abstract class AbstractWarpClientTestTestBase extends AbstractManagerTestBase  {

    @Override
    protected void addContexts(List<Class<? extends Context>> contexts) {
        super.addContexts(contexts);
        contexts.add(WarpExecutionContextImpl.class);
    }

    @Override
    protected void startContexts(Manager manager) {
        super.startContexts(manager);
        manager.getContext(WarpExecutionContext.class).activate();
    }
    
    
}
