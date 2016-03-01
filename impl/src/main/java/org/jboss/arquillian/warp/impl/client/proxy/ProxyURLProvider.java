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

import java.lang.annotation.Annotation;
import java.net.URL;

import org.jboss.arquillian.container.test.impl.enricher.resource.URLResourceProvider;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.warp.impl.client.event.RequireProxy;
import org.jboss.arquillian.warp.impl.utils.URLUtils;
import org.jboss.arquillian.warp.impl.utils.WarpTestValidator;
import org.jboss.arquillian.warp.spi.WarpCommons;

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
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Instance<Injector> injector;

    @Inject
    private Event<RequireProxy> requireProxy;

    @Inject
    private Instance<TestClass> testClass;

    @Inject
    private Instance<URLMapping> urlMapping;

    @Override
    public boolean canProvide(Class<?> type) {
        return URL.class.isAssignableFrom(type);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        URL realURL = null;

        ResourceProvider coreResourceProvider =
            serviceLoader.get().onlyOne(ResourceProvider.class, URLResourceProvider.class);
        if (coreResourceProvider != null && coreResourceProvider instanceof URLResourceProvider) {
            if (WarpTestValidator.hasDeployment(testClass.get())) {
                realURL = (URL) coreResourceProvider.lookup(resource, qualifiers);
            } else {
                realURL = (URL) ((URLResourceProvider) coreResourceProvider).doLookup(resource, qualifiers);
            }
        }

        if (!WarpCommons.isWarpTest(testClass.get().getJavaClass())) {
            return realURL;
        }

        if (realURL != null && "http".equals(realURL.getProtocol())) {
            return getProxyUrl(realURL);
        } else {
            return realURL;
        }
    }

    private URL getProxyUrl(URL realURL) {
        URL baseRealURL = URLUtils.getUrlBase(realURL);
        URL baseProxyURL = urlMapping.get().getProxyURL(baseRealURL);
        URL proxyURL = URLUtils.buildUrl(baseProxyURL, realURL.getPath());

        requireProxy.fire(new RequireProxy(baseRealURL, baseProxyURL));

        return proxyURL;
    }

    private Injector injector() {
        return injector.get();
    }

}
