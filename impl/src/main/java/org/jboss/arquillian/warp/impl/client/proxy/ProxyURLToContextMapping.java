/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContext;

/**
 * Maps classes to URLs of proxy that they use and also mapping of URL to {@link OperationalContext}.
 *
 * @author Lukas Fryc
 */
public class ProxyURLToContextMapping {

    private ConcurrentHashMap<URL, OperationalContext> urlToContext = new ConcurrentHashMap<URL, OperationalContext>();
    private ConcurrentHashMap<Class<?>, Set<URL>> testClassToUrls = new ConcurrentHashMap<Class<?>, Set<URL>>();

    public void register(URL proxyUrl, Class<?> testClass, OperationalContext context) {
        if (urlToContext.get(proxyUrl) != null) {
            throw new IllegalStateException("The OperatiocalContext was already set for URL: " + proxyUrl);
        }
        urlToContext.put(proxyUrl, context);

        Set<URL> urls = testClassToUrls.get(testClass);
        if (urls == null) {
            urls = new LinkedHashSet<URL>();
            testClassToUrls.put(testClass, urls);
        }
        urls.add(proxyUrl);
    }

    public OperationalContext get(URL proxyUrl) {
        OperationalContext operationalContext = urlToContext.get(proxyUrl);
        if (operationalContext == null) {
            throw new OperationalContextNotBoundException(
                "The OperationalContext wasn't setup for this URL: " + proxyUrl);
        }
        return operationalContext;
    }

    public void unregister(Class<?> testClass) {
        Set<URL> urls = testClassToUrls.get(testClass);
        if (urls != null) {
            for (URL url : urls) {
                if (urlToContext.remove(url) == null) {
                    throw new IllegalStateException("The OperationalContext wasn't setup for this URL: " + url);
                }
            }
        }
    }

    public boolean isRegistered(URL proxyUrl) {
        return urlToContext.containsKey(proxyUrl);
    }

    public static class OperationalContextNotBoundException extends RuntimeException {

        private static final long serialVersionUID = 4855105259819295574L;

        public OperationalContextNotBoundException() {
            super();
        }

        public OperationalContextNotBoundException(String message, Throwable cause) {
            super(message, cause);
        }

        public OperationalContextNotBoundException(String message) {
            super(message);
        }

        public OperationalContextNotBoundException(Throwable cause) {
            super(cause);
        }
    }
}
