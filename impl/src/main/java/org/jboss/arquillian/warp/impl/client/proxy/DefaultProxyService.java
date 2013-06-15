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

import java.lang.reflect.Field;
import java.net.URL;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.warp.impl.client.context.operation.ContextualOperation;
import org.jboss.arquillian.warp.impl.client.context.operation.Contextualizer;
import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContext;
import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContextRetriver;
import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContexts;
import org.jboss.arquillian.warp.impl.client.enrichment.HttpRequestEnrichmentFilter;
import org.jboss.arquillian.warp.impl.client.enrichment.HttpResponseDeenrichmentFilter;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.littleshoot.proxy.HttpFilter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpRequestFilter;
import org.littleshoot.proxy.LittleProxyConfig;

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

        OperationalContextRetriver retriever = new OperationalContextRetriver() {
            @Override
            public OperationalContext retrieve() {
                return urlToContextMapping.get(proxyUrl);
            }
        };

        HttpRequestFilter requestFilter = getHttpRequestEnrichmentFilter(retriever);

        HttpFilter responseFilter = getHttpResponseDeenrichmentFilter(retriever);
        String hostPort = realUrl.getHost() + ":" + realUrl.getPort();

        setupLittleProxyConfig();
        HttpProxyServer server = new WarpHttpProxyServer(proxyUrl.getPort(), hostPort, requestFilter, responseFilter);

        server.start();

        return server;
    }

    @Override
    public void stopProxy(HttpProxyServer proxy) {
        proxy.stop();
    }

    private ServiceLoader serviceLoader() {
        return serviceLoader.get();
    }

    private HttpRequestEnrichmentFilter getHttpRequestEnrichmentFilter(OperationalContextRetriver retriever) {
        final HttpRequestEnrichmentFilter requestFilter = serviceLoader().onlyOne(HttpRequestEnrichmentFilter.class);

        final ContextualOperation<HttpRequest, Void> operation = Contextualizer.contextualize(retriever,
                new ContextualOperation<HttpRequest, Void>() {
                    @Override
                    public Void performInContext(HttpRequest request) {
                        requestFilter.filter(request);
                        return null;
                    }
                });

        return new HttpRequestEnrichmentFilter() {
            @Override
            public void filter(HttpRequest request) {
                operation.performInContext(request);
            }
        };
    }

    private HttpResponseDeenrichmentFilter getHttpResponseDeenrichmentFilter(OperationalContextRetriver retriever) {
        final HttpResponseDeenrichmentFilter responseDeenrichmentFilter = serviceLoader().onlyOne(
                HttpResponseDeenrichmentFilter.class);

        final ContextualOperation<FilterResponseContext, HttpResponse> filterResponse = Contextualizer.contextualize(retriever,
                new ContextualOperation<FilterResponseContext, HttpResponse>() {
                    @Override
                    public HttpResponse performInContext(FilterResponseContext ctx) {
                        return responseDeenrichmentFilter.filterResponse(ctx.request, ctx.response);
                    }
                });

        final ContextualOperation<HttpRequest, Boolean> shouldFilterResponses = Contextualizer.contextualize(retriever,
                new ContextualOperation<HttpRequest, Boolean>() {
                    @Override
                    public Boolean performInContext(HttpRequest request) {
                        return responseDeenrichmentFilter.filterResponses(request);
                    }
                });

        final ContextualOperation<Void, Integer> getMaxResponseSize = Contextualizer.contextualize(retriever,
                new ContextualOperation<Void, Integer>() {
                    @Override
                    public Integer performInContext(Void argument) {
                        return responseDeenrichmentFilter.getMaxResponseSize();
                    }
                });

        return new HttpResponseDeenrichmentFilter() {

            @Override
            public HttpResponse filterResponse(HttpRequest request, HttpResponse response) {
                return filterResponse.performInContext(new FilterResponseContext(request, response));
            }

            @Override
            public boolean filterResponses(HttpRequest httpRequest) {
                return shouldFilterResponses.performInContext(httpRequest);
            }

            @Override
            public int getMaxResponseSize() {
                return getMaxResponseSize.performInContext(null);
            }
        };
    }

    private static class FilterResponseContext {
        private HttpRequest request;
        private HttpResponse response;

        public FilterResponseContext(HttpRequest request, HttpResponse response) {
            this.request = request;
            this.response = response;
        }
    }

    private void setupLittleProxyConfig() {

        try {
            Field field = LittleProxyConfig.class.getDeclaredField("transparent");
            field.setAccessible(true);
            field.set(null, true);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Warp proxy server hacks LittleProxy configuration in order to configure it transparently - this hack was designed to work with LittleProxy 0.5.3",
                    e);
        }
    }
}
