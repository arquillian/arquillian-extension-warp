/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.warp.impl.client.execution;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.warp.impl.client.event.ExecuteWarp;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionContext;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionScoped;

/**
 * Initializes {@link WarpExecutionContext}.
 *
 * @author Lukas Fryc
 */
public class WarpExecutionInitializer {

    @Inject
    private Instance<WarpExecutionContext> warpExecutionContext;

    @Inject
    @WarpExecutionScoped
    private InstanceProducer<WarpContext> warpContext;

    @Inject
    @WarpExecutionScoped
    private InstanceProducer<SynchronizationPoint> synchronization;

    /**
     * Activates/deactivates {@link WarpExecutionContext}.
     * <p>
     * Provides {@link WarpContext} instance.
     * <p>
     * Provides {@link SynchronizationPoint} instance.
     * <p>
     * Setups/resets {@link WarpContextStore}
     */
    public void provideWarpContext(@Observes EventContext<ExecuteWarp> eventContext) {
        warpExecutionContext.get().activate();
        try {
            WarpContext context = eventContext.getEvent().getWarpContext();
            warpContext.set(context);
            WarpContextStore.set(context);

            synchronization.set(eventContext.getEvent().getWarpContext().getSynchronization());

            eventContext.proceed();
        } finally {
            WarpContextStore.reset();
            warpExecutionContext.get().deactivate();
        }
    }
}
