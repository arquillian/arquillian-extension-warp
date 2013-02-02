/**
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.warp.impl.client.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.warp.impl.utils.URLUtils;

/**
 * Maps real URL to proxied URL.
 *
 * @author Lukas Fryc
 *
 */
public class DefaultURLMapping implements URLMapping {

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

    private int generatePort() {
        return newPort();
    }

    private int newPort() {
        return BASE + sequenceNumber++;
    }

    private URL newProxyUrlWithPort(URL url, int port) {
        try {
            String localHost = InetAddress.getLocalHost().getHostAddress();
            return new URL(url.getProtocol(), localHost, port, url.getFile());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
