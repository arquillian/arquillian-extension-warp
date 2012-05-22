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
package org.jboss.arquillian.warp.client.proxy;

import java.lang.annotation.Annotation;
import java.net.URL;

import org.jboss.arquillian.container.test.impl.enricher.resource.URLResourceProvider;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.warp.utils.URLUtils;

/**
 * Provides the proxy URL instead of real URL.
 *
 * Stores the mapping between real URL and proxy URL in {@link URLMapping}.
 *
 * @author Lukas Fryc
 *
 */
public class ProxyURLProvider implements ResourceProvider {

    @Inject
    Instance<ServiceLoader> serviceLoader;

    @Inject
    Instance<URLMapping> mapping;

    @Inject
    Instance<ProxyHolder> proxyHolder;

    @Inject
    Instance<Injector> injector;

    URLResourceProvider urlResourceProvider = new URLResourceProvider();

    @Override
    public boolean canProvide(Class<?> type) {
        return URL.class.isAssignableFrom(type);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        injector.get().inject(urlResourceProvider);

        URL realUrl = (URL) urlResourceProvider.lookup(resource, qualifiers);
        if ("http".equals(realUrl.getProtocol())) {
            return getProxyUrl(realUrl);
        } else {
            return realUrl;
        }
    }

    private URL getProxyUrl(URL realUrl) {
        URL baseRealUrl = URLUtils.getUrlBase(realUrl);
        URL baseProxyUrl = mapping.get().getProxyURL(baseRealUrl);
        URL proxyUrl = URLUtils.buildUrl(baseProxyUrl, realUrl.getPath());

        proxyHolder.get().startProxyForUrl(baseProxyUrl, baseRealUrl);

        return proxyUrl;
    }

}
