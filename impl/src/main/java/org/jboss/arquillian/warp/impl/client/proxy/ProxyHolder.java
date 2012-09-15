package org.jboss.arquillian.warp.impl.client.proxy;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ProxyHolder<T> {

    private Map<URL, T> proxies = new HashMap<URL, T>();

    public T getProxy(URL realUrl) {
        return proxies.get(realUrl);
    }

    public void storeProxy(URL realUrl, T proxy) {
        proxies.put(realUrl, proxy);
    }

    public Set<Entry<URL, T>> getAllProxies() {
        return Collections.unmodifiableSet(proxies.entrySet());
    }

    public void cleanAllProxies() {
        proxies.clear();
    }
}
