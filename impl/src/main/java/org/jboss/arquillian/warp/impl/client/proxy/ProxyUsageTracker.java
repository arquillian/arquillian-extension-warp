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
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContext;
import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContexts;
import org.jboss.arquillian.warp.impl.client.event.RequireProxy;

/**
 * Tracks what classes use which proxy
 *
 * @author Lukas Fryc
 */
public class ProxyUsageTracker {

    @Inject
    private Instance<TestClass> testClass;

    @Inject
    @ApplicationScoped // contexts are stored for whole suite
    private InstanceProducer<ProxyURLToContextMapping> contextMapping;

    @Inject
    @ApplicationScoped // proxy url mapping can be kept for the whole execution
    private InstanceProducer<URLMapping> urlMapping;

    @Inject
    @TestScoped // we keep just URLs for current context
    private InstanceProducer<RealURLToProxyURLMapping> realUrlMapping;

    @Inject
    private Instance<OperationalContexts> contexts;

    public void initializeContextMapping(@Observes ManagerStarted event) {
        if (urlMapping.get() == null) {
            urlMapping.set(new DefaultURLMapping());
        }
        contextMapping.set(new ProxyURLToContextMapping());
    }

    public void initializeRealURLMapping(@Observes(precedence = 1000) Before before) {
        realUrlMapping.set(new RealURLToProxyURLMapping());
    }

    public void registerOperationalContextToUrl(@Observes RequireProxy event) {
        if (!contextMapping.get().isRegistered(event.getProxyUrl())) {
            OperationalContext context = contexts.get().test();
            contextMapping.get().register(event.getProxyUrl(), testClass.get().getJavaClass(), context);
        }

        if (!realUrlMapping.get().isRegistered(event.getRealUrl())) {
            realUrlMapping.get().register(event.getRealUrl(), event.getProxyUrl());
        }
    }

    public void unregisterOperationalContext(@Observes After afterTest) {
        contextMapping.get().unregister(testClass.get().getJavaClass());
    }
}
