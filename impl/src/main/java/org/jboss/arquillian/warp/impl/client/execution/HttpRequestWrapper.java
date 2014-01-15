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

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.arquillian.warp.client.filter.http.HttpMethod;
import org.jboss.arquillian.warp.impl.utils.URLUtils;

public class HttpRequestWrapper implements org.jboss.arquillian.warp.client.filter.http.HttpRequest {

    private final HttpRequest request;

    private Map<String, List<String>> queryParameters;
    private Map<String, List<String>> httpDataAttributes;

    public HttpRequestWrapper(HttpRequest request) {
        this.request = request;
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.valueOf(request.getMethod().name());
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
        return request.headers().get(name);
    }

    @Override
    public List<String> getHeaders(String name) {
        return request.headers().getAll(name);
    }

    @Override
    public List<Entry<String, String>> getHeaders() {
        return request.headers().entries();
    }

    @Override
    public boolean containsHeader(String name) {
        return request.headers().contains(name);
    }

    @Override
    public Set<String> getHeaderNames() {
        return request.headers().names();
    }

    public HttpRequest unwrap() {
        return request;
    }

    @Override
    public String toString() {
        return String.format("%s %s", request.getMethod(), request.getUri());
    }

    @Override
    public Map<String, List<String>> getQueryParameters() {
        if (queryParameters == null) {
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
            queryParameters = queryStringDecoder.parameters();
        }
        return Collections.unmodifiableMap(queryParameters);
    }

    @Override
    public Map<String, List<String>> getHttpDataAttributes() {

        if (!HttpMethod.POST.equals(getMethod())) {
            throw new IllegalArgumentException("Cannot parse HttpData for requests other than POST");
        }

        try {
            if (httpDataAttributes == null) {
                final HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
                final Map<String, List<String>> map = new HashMap<String, List<String>>();

                try {
                    for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                        if (data.getHttpDataType() == HttpDataType.Attribute) {
                            Attribute attribute = (Attribute) data;

                            List<String> list = map.get(attribute.getName());
                            if (list == null) {
                                list = new LinkedList<String>();
                                map.put(attribute.getName(), list);
                            }

                            list.add(attribute.getValue());
                        }
                    }
                } finally {
                    decoder.destroy();
                }

                httpDataAttributes = map;
            }

            return Collections.unmodifiableMap(httpDataAttributes);

        } catch (IOException e) {
            throw new IllegalStateException("Cannot parse http request data", e);
        }
    }

}