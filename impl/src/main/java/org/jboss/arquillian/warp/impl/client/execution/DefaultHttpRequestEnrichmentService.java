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
package org.jboss.arquillian.warp.impl.client.execution;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.filter.http.HttpMethod;
import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;
import org.jboss.arquillian.warp.impl.client.enrichment.HttpRequestEnrichmentService;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.arquillian.warp.impl.utils.URLUtils;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * Default implementation of service for enriching HTTP requests
 *
 * @author Lukas Fryc
 */
public class DefaultHttpRequestEnrichmentService implements HttpRequestEnrichmentService {

    private Logger log = Logger.getLogger("Warp");

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.impl.client.enrichment.HttpRequestEnrichmentService#getMatchingPayloads(org.jboss.netty.handler.codec.http.HttpRequest)
     */
    @Override
    public Collection<RequestPayload> getMatchingPayloads(HttpRequest request) {

        final Collection<WarpGroup> groups = warpContext().getAllGroups();

        final org.jboss.arquillian.warp.client.filter.http.HttpRequest httpRequest = new HttpRequestWrapper(request);
        final Collection<RequestPayload> payloads = new LinkedList<RequestPayload>();

        for (WarpGroup group : groups) {
            final RequestFilter<?> filter = group.getFilter();

            if (filter == null) {
                payloads.add(group.generateRequestPayload());
                continue;
            }

            if (isType(filter, org.jboss.arquillian.warp.client.filter.http.HttpRequest.class)) {

                @SuppressWarnings("unchecked")
                RequestFilter<org.jboss.arquillian.warp.client.filter.http.HttpRequest> httpRequestFilter = (RequestFilter<org.jboss.arquillian.warp.client.filter.http.HttpRequest>) filter;

                if (httpRequestFilter.matches(httpRequest)) {
                    payloads.add(group.generateRequestPayload());
                }
            }
        }

        return payloads;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.impl.client.enrichment.HttpRequestEnrichmentService#enrichRequest(org.jboss.netty.handler.codec.http.HttpRequest, java.util.Collection)
     */
    @Override
    public void enrichRequest(HttpRequest request, Collection<RequestPayload> payloads) {

        if (WarpCommons.debugMode()) {
            System.out.println("                (W) " + request.getUri());
        }

        if (log.isLoggable(Level.FINE)) {
            log.fine("Warp request: " + request.getUri());
        }

        try {
            RequestPayload assertion = payloads.iterator().next();
            String requestEnrichment = SerializationUtils.serializeToBase64(assertion);
            request.setHeader(WarpCommons.ENRICHMENT_REQUEST, Arrays.asList(requestEnrichment));
        } catch (Throwable originalException) {
            ClientWarpExecutionException explainingException = new ClientWarpExecutionException("enriching request failed: "
                    + originalException.getMessage(), originalException);
            warpContext().pushException(explainingException);
        }
    }

    private boolean isType(RequestFilter<?> filter, Type expectedType) {
        Type[] interfaces = filter.getClass().getGenericInterfaces();

        for (Type type : interfaces) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parametrizedType = (ParameterizedType) type;
                if (parametrizedType.getRawType() == RequestFilter.class) {
                    return parametrizedType.getActualTypeArguments()[0] == expectedType;
                }
            }
        }

        return false;
    }

    private WarpContext warpContext() {
        return WarpContextStore.get();
    }

    private class HttpRequestWrapper implements org.jboss.arquillian.warp.client.filter.http.HttpRequest {

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
    }
}
