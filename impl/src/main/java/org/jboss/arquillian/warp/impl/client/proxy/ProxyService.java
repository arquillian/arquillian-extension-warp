package org.jboss.arquillian.warp.impl.client.proxy;

import java.net.URL;

public interface ProxyService<T> {
    
    T startProxy(URL realUrl, URL proxyUrl);
    
    void stopProxy(T proxy);
}
