package org.jboss.arquillian.warp.impl.client.event;

import java.net.URL;

public class RequireProxy extends AbstractProxyInitializationEvent {

    public RequireProxy(URL realURL, URL proxyURL) {
        super(realURL, proxyURL);
    }

}
