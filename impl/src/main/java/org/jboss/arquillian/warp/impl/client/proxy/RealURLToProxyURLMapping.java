/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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

import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps real request URLs to URLs of proxy.
 *
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class RealURLToProxyURLMapping {

    private ConcurrentHashMap<URL, URL> realToProxy = new ConcurrentHashMap<URL, URL>();

    public void register(URL realUrl, URL proxyUrl) {
        if (realToProxy.get(realUrl) != null) {
            throw new IllegalStateException("The ProxyURL was already set for URL: " + realUrl);
        }
        realToProxy.put(realUrl, proxyUrl);
    }

    public Set<Map.Entry<URL, URL>> getMap() {
        return realToProxy.entrySet();
    }

    public boolean isRegistered(URL realUrl) {
        return realToProxy.containsKey(realUrl);
    }
}
