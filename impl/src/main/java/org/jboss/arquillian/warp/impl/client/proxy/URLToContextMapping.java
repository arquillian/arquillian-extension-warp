package org.jboss.arquillian.warp.impl.client.proxy;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.arquillian.warp.impl.client.operation.OperationalContext;

public class URLToContextMapping {

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
            throw new IllegalStateException("The OperationalContext wasn't setup for this URL: " + proxyUrl);
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

}
