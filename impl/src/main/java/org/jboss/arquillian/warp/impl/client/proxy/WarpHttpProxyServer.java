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

import java.util.logging.Logger;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.ChainProxyManager;
import org.littleshoot.proxy.DefaultHttpProxyServer;
import org.littleshoot.proxy.HttpFilter;
import org.littleshoot.proxy.HttpRequestFilter;
import org.littleshoot.proxy.HttpResponseFilters;

public class WarpHttpProxyServer extends DefaultHttpProxyServer {

    private static final Logger log = Logger.getLogger("WarpHttpProxyServer");

    public WarpHttpProxyServer(int port, String forwardHostPort, HttpRequestFilter requestFilter, HttpFilter responseFilter) {
        this(port, requestFilter, createResponseFilters(responseFilter), createChainProxy(forwardHostPort));
    }

    private WarpHttpProxyServer(int port, HttpRequestFilter requestFilter, HttpResponseFilters responseFilters,
            ChainProxyManager chainProxyManager) {

        super(port, responseFilters, chainProxyManager, null, requestFilter);
    }

    private static ChainProxyManager createChainProxy(final String forwardHostPort) {
        return new ChainProxyManager() {

            @Override
            public String getChainProxy(HttpRequest httpRequest) {
                return forwardHostPort;
            }

            @Override
            public void onCommunicationError(String hostAndPort) {
                log.severe("Communication error when communicating with " + hostAndPort);
            }
        };
    }

    private static HttpResponseFilters createResponseFilters(final HttpFilter responseFilter) {
        return new HttpResponseFilters() {

            @Override
            public HttpFilter getFilter(String hostAndPort) {
                return responseFilter;
            }
        };
    }
}