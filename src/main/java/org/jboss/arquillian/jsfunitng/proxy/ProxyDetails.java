package org.jboss.arquillian.jsfunitng.proxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.graphene.utils.URLUtils;

public class ProxyDetails {

    private static final int BASE = 18080;
    private int sequenceNumber = 0;

    private Map<URL, URL> map = new HashMap<URL, URL>();

    public synchronized URL getProxyURL(URL url) {
        URL base = URLUtils.getUrlBase(url);

        if (map.containsKey(base)) {
            return map.get(base);
        }

        int proxyPort = generatePort();
        URL proxyUrl = newProxyUrlWithPort(base, proxyPort);

        map.put(base, proxyUrl);
        return proxyUrl;
    }

    public int generatePort() {
        return newPort();
    }

    private int newPort() {
        return BASE + sequenceNumber++;
    }

    private URL newProxyUrlWithPort(URL url, int port) {
        try {
            return new URL(url.getProtocol(), "localhost", port, url.getFile());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }
}
