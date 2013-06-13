/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.impl.client.proxy;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStarted;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContext;
import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContexts;
import org.jboss.arquillian.warp.impl.client.event.RequireProxy;

/**
 * Tracks what classes use which proxy
 *
 * @author Lukas Fryc
 */
public class ClassProxyUsageTracker {

    @Inject
    private Instance<TestClass> testClass;

    @Inject
    @ApplicationScoped
    private InstanceProducer<ProxyURLToContextMapping> mapping;

    @Inject
    private Instance<OperationalContexts> contexts;

    public void initializeMapping(@Observes ManagerStarted event) {
        mapping.set(new ProxyURLToContextMapping());
    }

    public void registerOperationalContextToUrl(@Observes RequireProxy requireProxy) {
        OperationalContext context = contexts.get().test();
        mapping.get().register(requireProxy.getProxyUrl(), testClass.get().getJavaClass(), context);
    }

    public void unregisterOperationalContext(@Observes After afterTest) {
        mapping.get().unregister(testClass.get().getJavaClass());
    }
}
