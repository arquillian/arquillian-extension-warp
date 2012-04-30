package org.jboss.arquillian.jsfunitng.proxy;

import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.jboss.arquillian.jsfunitng.ServerAssertion;
import org.jboss.arquillian.jsfunitng.filter.EnrichmentFilter;
import org.jboss.arquillian.jsfunitng.utils.SerializationUtils;
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

        HttpProxyServer server = new DefaultHttpProxyServer(proxyUrl.getPort(), responseFilters, realUrl.getHost() + ":"
                + realUrl.getPort(), null, requestFilter);
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
                log.info("filter");
                if (AssertionHolder.isWaitingForProcessing()) {
                    try {
                        log.info("enriching request: " + request.getUri());
//                        Serializable assertion = requestEnrichmentRef.getAndSet(null);
                        ServerAssertion assertion = AssertionHolder.popRequest();
                        String requestEnrichment = SerializationUtils.serializeToBase64(assertion);
                        request.setHeader(EnrichmentFilter.ENRICHMENT_REQUEST, Arrays.asList(requestEnrichment));
                    } catch (Exception e) {
                        log.severe("enriching request failed: " + e.getMessage());
                    }
                }
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
                
                String responseEnrichment = response.getHeader(EnrichmentFilter.ENRICHMENT_RESPONSE);

                if (responseEnrichment != null) {
                    log.info("filtering response");
                    ServerAssertion assertion = SerializationUtils.deserializeFromBase64(responseEnrichment);
//                    responseEnrichmentRef.set(assertion);
                    AssertionHolder.pushResponse(assertion);
                }

                return response;
            }
        };

        Map<String, HttpFilter> map = Mockito.mock(Map.class);
        when(map.get(Mockito.anyObject())).thenReturn(filter);
        when(map.isEmpty()).thenReturn(false);

        return map;
    }
}
