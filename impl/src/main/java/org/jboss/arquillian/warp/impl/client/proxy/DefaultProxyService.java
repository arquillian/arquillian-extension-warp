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

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Queue;

import io.netty.handler.codec.http.HttpRequest;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.warp.impl.client.context.operation.Contextualizer;
import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContext;
import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContextRetriever;
import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContexts;
import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyAdapter;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.ClientDetails;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

/**
 * The holder for instantiated proxies.
 *
 * @author Lukas Fryc
 */
public class DefaultProxyService implements ProxyService<HttpProxyServer> {

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Instance<OperationalContexts> operationalContexts;

    @Inject
    private Instance<ProxyURLToContextMapping> urlToContextMappingInst;

    @Override
    public HttpProxyServer startProxy(final URL realUrl, final URL proxyUrl) {
        final ProxyURLToContextMapping urlToContextMapping = urlToContextMappingInst.get();

        final OperationalContextRetriever retriever = new OperationalContextRetriever() {
            @Override
            public OperationalContext retrieve() {
                return urlToContextMapping.get(proxyUrl);
            }
        };

        final HttpFiltersSource httpFiltersSource = Contextualizer.contextualize(retriever,
            serviceLoader().onlyOne(HttpFiltersSource.class), HttpFiltersSource.class, HttpFilters.class);

        final InetSocketAddress bindToAddress = new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort());
        final InetSocketAddress forwardToAddress = new InetSocketAddress(realUrl.getHost(), realUrl.getPort());

        return DefaultHttpProxyServer
            .bootstrap()
            .withAddress(bindToAddress)
            .withTransparent(true)
            .withChainProxyManager(new ChainedProxyManager() {

                @Override
                public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies, ClientDetails clientDetails) {
                    chainedProxies.add(new ChainedProxyAdapter() {
                        @Override
                        public InetSocketAddress getChainedProxyAddress() {
                            return forwardToAddress;
                        }
                    });
                }
            })
            .withFiltersSource(httpFiltersSource)
            //Required by LittleProxy 1.1.2 - otherwise "proxyToServerRequest" is never called.
            .withAllowRequestToOriginServer(true)
            .start();
    }

    @Override
    public void stopProxy(HttpProxyServer proxy) {
        proxy.stop();
    }

    private ServiceLoader serviceLoader() {
        return serviceLoader.get();
    }
}
