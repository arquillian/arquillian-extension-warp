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
package org.jboss.arquillian.warp.lifecycle;

import java.util.List;

import org.jboss.arquillian.container.test.test.AbstractContainerTestTestBase;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.context.Context;
import org.jboss.arquillian.warp.server.request.RequestContext;
import org.jboss.arquillian.warp.server.request.RequestContextImpl;

/**
 * @author Lukas Fryc
 */
public class AbstractLifecycleTestBase extends AbstractContainerTestTestBase {

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
