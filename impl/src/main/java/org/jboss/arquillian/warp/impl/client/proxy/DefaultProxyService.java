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

import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.Callable;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.warp.impl.client.context.operation.ContextualOperation;
import org.jboss.arquillian.warp.impl.client.context.operation.Contextualizer;
import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContext;
import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContextRetriver;
import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContexts;
import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyAdapter;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
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

        final OperationalContextRetriver retriever = new OperationalContextRetriver() {
            @Override
            public OperationalContext retrieve() {
                return urlToContextMapping.get(proxyUrl);
            }
        };

        final HttpFiltersSourceAdapter httpFiltersSource = serviceLoader().onlyOne(HttpFiltersSourceAdapter.class);

        final ContextualOperation<HttpRequest, HttpFilters> filterRequest = Contextualizer.contextualize(retriever,
            new ContextualOperation<HttpRequest, HttpFilters>() {
                @Override
                public HttpFilters performInContext(final HttpRequest originalRequest) {
                    return new ContextualHttpFilters(retriever, httpFiltersSource.filterRequest(originalRequest));
                }
            }
        );

        final InetSocketAddress bindToAddress = new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort());
        final InetSocketAddress forwardToAddress = new InetSocketAddress(realUrl.getHost(), realUrl.getPort());

        return DefaultHttpProxyServer
            .bootstrap()
            .withAddress(bindToAddress)
            .withTransparent(true)
            .withChainProxyManager(new ChainedProxyManager() {

                @Override
                public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies) {
                    chainedProxies.add(new ChainedProxyAdapter() {
                        @Override
                        public InetSocketAddress getChainedProxyAddress() {
                            return forwardToAddress;
                        }
                    });
                }
            })
            .withFiltersSource(new HttpFiltersSourceAdapter() {

                @Override
                public HttpFilters filterRequest(HttpRequest originalRequest) {
                    return filterRequest.performInContext(originalRequest);
                }

                @Override
                public int getMaximumRequestBufferSizeInBytes() {
                    return httpFiltersSource.getMaximumRequestBufferSizeInBytes();
                }

                @Override
                public int getMaximumResponseBufferSizeInBytes() {
                    return httpFiltersSource.getMaximumResponseBufferSizeInBytes();
                }
            })
            .start();
    }

    @Override
    public void stopProxy(HttpProxyServer proxy) {
        proxy.stop();
    }

    private ServiceLoader serviceLoader() {
        return serviceLoader.get();
    }

    private static class ContextualHttpFilters implements HttpFilters {
        private HttpFilters delegate;
        private OperationalContextRetriver retriver;

        public ContextualHttpFilters(OperationalContextRetriver retriver, HttpFilters delegate) {
            this.retriver = retriver;
            this.delegate = delegate;
        }

        @Override
        public HttpResponse requestPre(final HttpObject httpObject) {
            return contextual(new Callable<HttpResponse>() {
                public HttpResponse call() throws Exception { return delegate.requestPre(httpObject); }
             });
        }

        @Override
        public HttpResponse requestPost(final HttpObject httpObject) {
            return contextual(new Callable<HttpResponse>() {
               public HttpResponse call() throws Exception { return delegate.requestPost(httpObject); }
            });
        }

        @Override
        public HttpObject responsePre(final HttpObject httpObject) {
            return contextual(new Callable<HttpObject>() {
                public HttpObject call() throws Exception { return delegate.responsePre(httpObject); }
             });
        }

        @Override
        public HttpObject responsePost(final HttpObject httpObject) {
            return contextual(new Callable<HttpObject>() {
                public HttpObject call() throws Exception { return delegate.responsePost(httpObject); }
             });
        }

        private <T> T contextual(Callable<T> callable) {
            OperationalContext context = retriver.retrieve();
            try {
                context.activate();
                return callable.call();
            } catch(Exception e) {
                throw new RuntimeException(e);
            } finally {
                context.deactivate();
            }
        }
    }
}
