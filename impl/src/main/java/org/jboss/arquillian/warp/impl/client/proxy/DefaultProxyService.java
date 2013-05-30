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

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.warp.impl.client.enrichment.HttpRequestEnrichmentFilter;
import org.jboss.arquillian.warp.impl.client.enrichment.HttpResponseDeenrichmentFilter;
import org.jboss.arquillian.warp.impl.client.operation.Operation;
import org.jboss.arquillian.warp.impl.client.operation.SuiteContextOperator;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.littleshoot.proxy.DefaultHttpProxyServer;
import org.littleshoot.proxy.HttpFilter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpRequestFilter;

/**
 * The holder for instantiated proxies.
 *
 * @author Lukas Fryc
 */
public class DefaultProxyService implements ProxyService<HttpProxyServer> {

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Override
    public HttpProxyServer startProxy(URL realUrl, URL proxyUrl) {

        HttpRequestFilter requestFilter = getHttpRequestEnrichmentFilter();

        HttpFilter responseFilter = getHttpResponseDeenrichmentFilter();
        String hostPort = realUrl.getHost() + ":" + realUrl.getPort();
        ResponseFilterMap responseFilterMap = new ResponseFilterMap(hostPort, responseFilter);

        HttpProxyServer server = new DefaultHttpProxyServer(proxyUrl.getPort(), responseFilterMap, hostPort, null,
                requestFilter);

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

    private HttpRequestEnrichmentFilter getHttpRequestEnrichmentFilter() {
        final SuiteContextOperator operator = serviceLoader().onlyOne(SuiteContextOperator.class);

        final HttpRequestEnrichmentFilter requestFilter = serviceLoader().onlyOne(HttpRequestEnrichmentFilter.class);
        final Operation<HttpRequest, Void> operation = operator.wrap(new Operation<HttpRequest, Void>() {
            @Override
            public Void perform(HttpRequest request) {
                requestFilter.filter(request);
                return null;
            }
        });

        return new HttpRequestEnrichmentFilter() {
            @Override
            public void filter(HttpRequest request) {
                operation.perform(request);
            }
        };
    }

    private HttpResponseDeenrichmentFilter getHttpResponseDeenrichmentFilter() {
        final SuiteContextOperator operator = serviceLoader().onlyOne(SuiteContextOperator.class);

        final HttpResponseDeenrichmentFilter responseDeenrichmentFilter = serviceLoader().onlyOne(HttpResponseDeenrichmentFilter.class);

        final Operation<HttpResponse, HttpResponse> filterResponse = operator.wrap(new Operation<HttpResponse, HttpResponse>() {
            @Override
            public HttpResponse perform(HttpResponse response) {
                return responseDeenrichmentFilter.filterResponse(response);
            }
        });

        final Operation<HttpRequest, Boolean> shouldFilterResponses = operator.wrap(new Operation<HttpRequest, Boolean>() {
            @Override
            public Boolean perform(HttpRequest request) {
                return responseDeenrichmentFilter.shouldFilterResponses(request);
            }
        });

        final Operation<Void, Integer> getMaxResponseSize = operator.wrap(new Operation<Void, Integer>() {
            @Override
            public Integer perform(Void argument) {
                return responseDeenrichmentFilter.getMaxResponseSize();
            }
        });

        return new HttpResponseDeenrichmentFilter() {

            @Override
            public HttpResponse filterResponse(HttpResponse response) {
                return filterResponse.perform(response);
            }

            @Override
            public boolean shouldFilterResponses(HttpRequest httpRequest) {
                return shouldFilterResponses.perform(httpRequest);
            }

            @Override
            public int getMaxResponseSize() {
                return getMaxResponseSize.perform(null);
            }
        };
    }
}
