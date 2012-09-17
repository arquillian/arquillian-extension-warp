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
package org.jboss.arquillian.warp.impl.client.proxy;

import java.net.URL;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.warp.impl.client.event.RequireProxy;
import org.jboss.arquillian.warp.impl.client.event.StartProxy;

/**
 * Initializes and finalizes proxies.
 * 
 * @author Lukas Fryc
 * 
 */
public class ProxyObserver {

    @Inject
    @SuiteScoped
    private InstanceProducer<ProxyHolder> proxyHolder;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Event<StartProxy> startProxy;

    public void initializeProxies(@Observes BeforeSuite event, ServiceLoader services) {
        proxyHolder.set(new ProxyHolder());
    }

    public void finalizeProxies(@Observes AfterSuite event) {
        for (Entry entry : (Set<Entry<URL, Object>>) proxyHolder().getAllProxies()) {
            proxyService().stopProxy(entry.getValue());
        }
    }

    public void requireProxy(@Observes RequireProxy event, ServiceLoader services) {
        Object proxy = proxyHolder().getProxy(event.getRealUrl());
        if (proxy == null) {
            startProxy.fire(new StartProxy(event));
        }
    }

    public void startProxy(@Observes StartProxy event, ServiceLoader services) {
        Object proxy = proxyService().startProxy(event.getRealUrl(), event.getProxyUrl());
        proxyHolder().storeProxy(event.getRealUrl(), proxy);
    }

    private ProxyHolder proxyHolder() {
        return proxyHolder.get();
    }

    private ProxyService proxyService() {
        return serviceLoader.get().onlyOne(ProxyService.class);
    }
}
