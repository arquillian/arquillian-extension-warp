package org.jboss.arquillian.warp.impl.client.event;

import java.net.URL;

public class StartProxy {

    private URL realUrl;
    private URL proxyUrl;

    public StartProxy(URL realUrl, URL proxyUrl) {
        super();
        this.realUrl = realUrl;
        this.proxyUrl = proxyUrl;
    }

    public URL getRealUrl() {
        return realUrl;
    }

    public URL getProxyUrl() {
        return proxyUrl;
    }
}
