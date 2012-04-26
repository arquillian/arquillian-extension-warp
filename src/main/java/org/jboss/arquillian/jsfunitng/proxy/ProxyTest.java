package org.jboss.arquillian.jsfunitng.proxy;

import java.net.URL;

import org.jboss.arquillian.graphene.utils.URLUtils;
import org.littleshoot.proxy.DefaultHttpProxyServer;
import org.littleshoot.proxy.HttpProxyServer;

public class ProxyTest {
    
    public static void main(final String... args) {

        URL realUrl = URLUtils.buildUrl("http://localhost:8080/");
        URL proxyUrl = URLUtils.buildUrl("http://localhost:18080/");

        ProxyHolder proxyHolder = new ProxyHolder();
        proxyHolder.startProxyForUrl(proxyUrl, realUrl);

        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
