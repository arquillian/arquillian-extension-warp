/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.proxy;

import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.filter.WarpFilter;
import org.jboss.arquillian.warp.utils.SerializationUtils;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.littleshoot.proxy.DefaultHttpProxyServer;
import org.littleshoot.proxy.HttpFilter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpRequestFilter;
import org.mockito.Mockito;

/**
 * The holder for instantiated proxies.
 * 
 * @author Lukas Fryc
 * 
 */
public class ProxyHolder {

    private static Logger log = Logger.getLogger("Proxy");

    private Map<URL, HttpProxyServer> servers = new HashMap<URL, HttpProxyServer>();

    public void startProxyForUrl(URL proxyUrl, URL realUrl) {

        if (servers.containsKey(proxyUrl)) {
            return;
        }

        Map<String, HttpFilter> responseFilters = createResponseFilters(proxyUrl, realUrl);
        HttpRequestFilter requestFilter = createRequestFilter(proxyUrl, realUrl);

        HttpProxyServer server = new DefaultHttpProxyServer(proxyUrl.getPort(), responseFilters, realUrl.getHost() + ":"
                + realUrl.getPort(), null, requestFilter);
        server.start();

        servers.put(proxyUrl, server);
    }

    public void freeAllProxies() {
        for (HttpProxyServer server : servers.values()) {
            server.stop();
        }
        servers.clear();
    }

    private HttpRequestFilter createRequestFilter(final URL proxyUrl, final URL realUrl) {
        return new HttpRequestFilter() {

            @Override
            public void filter(HttpRequest request) {
                if (AssertionHolder.isWaitingForProcessing()) {
                    try {
                        ServerAssertion assertion = AssertionHolder.popRequest();
                        String requestEnrichment = SerializationUtils.serializeToBase64(assertion);
                        request.setHeader(WarpFilter.ENRICHMENT_REQUEST, Arrays.asList(requestEnrichment));
                    } catch (Exception e) {
                        log.severe("enriching request failed: " + e.getMessage());
                    }
                }
            }
        };
    }

    private Map<String, HttpFilter> createResponseFilters(final URL proxyUrl, final URL realUrl) {

        final HttpFilter filter = new HttpFilter() {

            @Override
            public boolean shouldFilterResponses(HttpRequest httpRequest) {
                return true;
            }

            @Override
            public int getMaxResponseSize() {
                return Integer.MAX_VALUE;
            }

            @Override
            public HttpResponse filterResponse(HttpResponse response) {

                String responseEnrichment = response.getHeader(WarpFilter.ENRICHMENT_RESPONSE);

                if (responseEnrichment != null) {
                    ServerAssertion assertion = SerializationUtils.deserializeFromBase64(responseEnrichment);
                    AssertionHolder.pushResponse(assertion);
                }

                return response;
            }
        };

        // TODO replace Mockito
        Map<String, HttpFilter> map = Mockito.mock(Map.class);
        when(map.get(Mockito.anyObject())).thenReturn(filter);
        when(map.isEmpty()).thenReturn(false);

        return map;
    }
}
