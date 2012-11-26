/**
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
package org.jboss.arquillian.warp.impl.client.scope;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.warp.impl.client.event.AdvertiseEnrichment;
import org.jboss.arquillian.warp.impl.client.event.CleanEnrichment;

/**
 * The handler for current context.
 *
 * @author Lukas Fryc
 */
public class WarpExecutionContextHandler {

    @Inject
    private Instance<WarpExecutionContext> warpExecutionContextInstance;

    public void createWarpExecutionContext(@Observes(precedence = 100) EventContext<AdvertiseEnrichment> context) {
        WarpExecutionContext warpExecutionContext = this.warpExecutionContextInstance.get();
        warpExecutionContext.activate();
        context.proceed();
    }

    public void destroyWarpExecutionContextContext(@Observes(precedence = -100) EventContext<CleanEnrichment> context) {
        WarpExecutionContext warpExecutionContext = this.warpExecutionContextInstance.get();
        try {
            context.proceed();
        } finally {
            warpExecutionContext.deactivate();
        }
    }
}
