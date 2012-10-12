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
import org.jboss.arquillian.warp.impl.client.enrichment.RequestEnrichmentService;
import org.jboss.arquillian.warp.impl.client.enrichment.ResponseDeenrichmentService;
import org.littleshoot.proxy.DefaultHttpProxyServer;
import org.littleshoot.proxy.HttpProxyServer;

/**
 * The holder for instantiated proxies.
 *
 * @author Lukas Fryc
 *
 */
public class DefaultProxyService implements ProxyService<HttpProxyServer> {

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Override
    public HttpProxyServer startProxy(URL realUrl, URL proxyUrl) {

        RequestEnrichmentFilter requestFilter = serviceLoader().onlyOne(RequestEnrichmentFilter.class);
        requestFilter.setEnrichmentService(serviceLoader().onlyOne(RequestEnrichmentService.class));
        ResponseDeenrichmentFilter responseDeenrichmentFilter = serviceLoader().onlyOne(ResponseDeenrichmentFilter.class);
        responseDeenrichmentFilter.setDeenrichmentService(serviceLoader().onlyOne(ResponseDeenrichmentService.class));

        String hostPort = realUrl.getHost() + ":" + realUrl.getPort();
        ResponseFilterMap responseFilterMap = new ResponseFilterMap(hostPort, responseDeenrichmentFilter);

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
}
