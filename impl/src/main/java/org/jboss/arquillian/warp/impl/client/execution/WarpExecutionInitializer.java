package org.jboss.arquillian.warp.impl.client.execution;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.warp.impl.client.event.ExecuteWarp;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionContext;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionScoped;

public class WarpExecutionInitializer {

    @Inject
    private Instance<WarpExecutionContext> warpExecutionContext;

    @Inject
    @WarpExecutionScoped
    private InstanceProducer<WarpContext> warpContext;

    @Inject
    @WarpExecutionScoped
    private InstanceProducer<SynchronizationPoint> synchronization;

    public void setupWarpContext(@Observes EventContext<ExecuteWarp> eventContext) {
        warpExecutionContext.get().activate();
        try {
            WarpContextImpl context = eventContext.getEvent().getWarpContext();
            warpContext.set(context);
            WarpContextStore.setCurrentInstance(context);

            synchronization.set(eventContext.getEvent().getWarpContext().getSynchronization());

            eventContext.proceed();

        } finally {
            WarpContextStore.setCurrentInstance(null);
            warpExecutionContext.get().deactivate();
        }
    }
}
