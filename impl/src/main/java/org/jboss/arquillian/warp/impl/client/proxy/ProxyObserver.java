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
package org.jboss.arquillian.warp.impl.client.proxy;

import java.net.URL;
import java.util.Map.Entry;

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
 */
public class ProxyObserver<TProxy> {

    @Inject
    @SuiteScoped
    private InstanceProducer<ProxyHolder<TProxy>> proxyHolder;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Event<StartProxy> startProxy;

    public void initializeProxies(@Observes BeforeSuite event, ServiceLoader services) {
        proxyHolder.set(new ProxyHolder<TProxy>());
    }

    public void finalizeProxies(@Observes AfterSuite event) {
        for (Entry<URL, TProxy> entry : proxyHolder().getAllProxies()) {
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
        TProxy proxy = proxyService().startProxy(event.getRealUrl(), event.getProxyUrl());
        proxyHolder().storeProxy(event.getRealUrl(), proxy);
    }

    private ProxyHolder<TProxy> proxyHolder() {
        return proxyHolder.get();
    }

    @SuppressWarnings("unchecked")
    private ProxyService<TProxy> proxyService() {
        return (ProxyService<TProxy>)serviceLoader.get().onlyOne(ProxyService.class);
    }
}
