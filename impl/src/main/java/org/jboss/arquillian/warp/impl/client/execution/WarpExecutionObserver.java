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
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.impl.client.event.AdvertiseEnrichment;
import org.jboss.arquillian.warp.impl.client.event.AwaitResponse;
import org.jboss.arquillian.warp.impl.client.event.CleanEnrichment;
import org.jboss.arquillian.warp.impl.client.event.ExecuteWarp;
import org.jboss.arquillian.warp.impl.client.event.FinishEnrichment;

/**
 * Observes on Warp execution events and invokes associated services.
 *
 * @author Lukas Fryc
 */
public class WarpExecutionObserver {

    @Inject
    private Instance<ServiceLoader> services;

    @Inject
    private Instance<WarpContext> warpContext;

    public void executeWarp(@Observes ExecuteWarp event) throws Exception {
        try {
            warpExecutor().execute(event.getActivity(), event.getWarpContext());
        } catch (Exception e) {
            warpContext.get().pushException(e);
        }
    }

    public void advertiseEnrichment(@Observes AdvertiseEnrichment event) {
        inspectionSynchronizer().advertise();
    }

    public void finishEnrichment(@Observes FinishEnrichment event) {
        inspectionSynchronizer().finish();
    }

    public void executeActivity(@Observes Activity activity) {
        activity.perform();
    }

    public void awaitResponse(@Observes AwaitResponse event) {
        inspectionSynchronizer().waitForResponse();
    }

    public void cleanEnrichment(@Observes CleanEnrichment event) {
        inspectionSynchronizer().clean();
    }

    private WarpExecutor warpExecutor() {
        return services.get().onlyOne(WarpExecutor.class);
    }

    private ExecutionSynchronizer inspectionSynchronizer() {
        return services.get().onlyOne(ExecutionSynchronizer.class);
    }
}
