package org.jboss.arquillian.warp.impl.client.event;

import java.net.URL;

public abstract class AbstractProxyInitializationEvent {

    private URL realUrl;
    private URL proxyUrl;

    public AbstractProxyInitializationEvent(URL realUrl, URL proxyUrl) {
        this.realUrl = realUrl;
        this.proxyUrl = proxyUrl;
    }

    public AbstractProxyInitializationEvent(AbstractProxyInitializationEvent event) {
        this.realUrl = event.realUrl;
        this.proxyUrl = event.proxyUrl;
    }

    public URL getRealUrl() {
        return realUrl;
    }

    public URL getProxyUrl() {
        return proxyUrl;
    }
}
