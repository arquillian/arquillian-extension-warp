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
package org.jboss.arquillian.warp.impl.client.execution;

import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.arquillian.warp.client.filter.http.HttpMethod;
import org.jboss.arquillian.warp.impl.utils.URLUtils;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class HttpRequestWrapper implements org.jboss.arquillian.warp.client.filter.http.HttpRequest {

    private HttpRequest request;

    public HttpRequestWrapper(HttpRequest request) {
        this.request = request;
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.valueOf(request.getMethod().getName());
    }

    @Override
    public String getUri() {
        return request.getUri();
    }

    @Override
    public URL getUrl() {
        return URLUtils.buildUrl(request.getUri());
    }

    @Override
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    @Override
    public List<String> getHeaders(String name) {
        return request.getHeaders(name);
    }

    @Override
    public List<Entry<String, String>> getHeaders() {
        return request.getHeaders();
    }

    @Override
    public boolean containsHeader(String name) {
        return request.containsHeader(name);
    }

    @Override
    public Set<String> getHeaderNames() {
        return request.getHeaderNames();
    }

    public HttpRequest unwrap() {
        return request;
    }

    @Override
    public String toString() {
        return String.format("%s %s", request.getMethod(), request.getUri());
    }
}