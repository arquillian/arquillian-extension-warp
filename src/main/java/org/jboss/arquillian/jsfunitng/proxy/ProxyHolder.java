package org.jboss.arquillian.jsfunitng.proxy;

import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.arquillian.graphene.utils.URLUtils;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.littleshoot.proxy.DefaultHttpProxyServer;
import org.littleshoot.proxy.HttpFilter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpRequestFilter;
import org.mockito.Mockito;

public class ProxyHolder {

    private static Logger log = Logger.getLogger("Proxy");

    private Map<URL, HttpProxyServer> servers = new HashMap<URL, HttpProxyServer>();

    public void startProxyForUrl(URL proxyUrl, URL realUrl) {

        if (servers.containsKey(proxyUrl)) {
            return;
        }

        Map<String, HttpFilter> responseFilters = createResponseFilters(proxyUrl, realUrl);
        HttpRequestFilter requestFilter = createRequestFilter(proxyUrl, realUrl);

        HttpProxyServer server = new DefaultHttpProxyServer(proxyUrl.getPort(), responseFilters, realUrl.getHost() + ":" + realUrl.getPort(), null, requestFilter);
        log.info("starting proxy");
        server.start();

        servers.put(proxyUrl, server);
    }

    public void freeAllProxies() {
        for (HttpProxyServer server : servers.values()) {
            server.stop();
        }
        servers.clear();
    }

    private HttpRequestFilter createRequestFilter(final URL proxyUrl, final URL realUrl) {
        return new HttpRequestFilter() {

            @Override
            public void filter(HttpRequest request) {
                log.info("filtering request: " + request.getUri());
            }
        };
    }

    private Map<String, HttpFilter> createResponseFilters(final URL proxyUrl, final URL realUrl) {

        final HttpFilter filter = new HttpFilter() {

            @Override
            public boolean shouldFilterResponses(HttpRequest httpRequest) {
                return true;
            }

            @Override
            public int getMaxResponseSize() {
                return Integer.MAX_VALUE;
            }

            @Override
            public HttpResponse filterResponse(HttpResponse response) {
                log.info("filtering response");
                return response;
            }
        };

        Map<String, HttpFilter> map = Mockito.mock(Map.class);
        when(map.get(Mockito.anyObject())).thenReturn(filter);
        when(map.isEmpty()).thenReturn(false);

        return map;
    }
}
