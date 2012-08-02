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
package org.jboss.arquillian.warp.client.execution;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.jboss.arquillian.warp.RequestFilter;
import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;
import org.jboss.arquillian.warp.shared.RequestPayload;
import org.jboss.arquillian.warp.shared.ResponsePayload;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.arquillian.warp.utils.SerializationUtils;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.HttpRequestFilter;

public class RequestEnrichmentFilter implements HttpRequestFilter {

    @Override
    public void filter(HttpRequest request) {
        if (AssertionHolder.isWaitingForRequests()) {
            try {

                Collection<RequestPayload> payloads = getMatchingPayloads(request);
                if (!payloads.isEmpty()) {
                    RequestPayload assertion = payloads.iterator().next();
                    String requestEnrichment = SerializationUtils.serializeToBase64(assertion);
                    request.setHeader(WarpCommons.ENRICHMENT_REQUEST, Arrays.asList(requestEnrichment));
                }
            } catch (Exception originalException) {
                ClientWarpExecutionException wrappedException = new ClientWarpExecutionException("enriching request failed: "
                        + originalException.getMessage(), originalException);
                ResponsePayload exceptionPayload = new ResponsePayload(wrappedException);
                ResponseEnrichment responseEnrichment = new ResponseEnrichment(exceptionPayload);
                AssertionHolder.addResponse(responseEnrichment);
            }
        }
    }

    private Collection<RequestPayload> getMatchingPayloads(HttpRequest request) {
        final Set<RequestEnrichment> requests = AssertionHolder.getRequests();
        final org.jboss.arquillian.warp.HttpRequest httpRequest = new HttpRequestWrapper(request);
        final Collection<RequestPayload> payloads = new LinkedList<RequestPayload>();

        for (RequestEnrichment enrichment : requests) {
            RequestFilter<?> filter = enrichment.getFilter();

            if (filter == null) {
                payloads.add(enrichment.getPayload());
                continue;
            }

            if (isType(filter, org.jboss.arquillian.warp.HttpRequest.class)) {

                @SuppressWarnings("unchecked")
                RequestFilter<org.jboss.arquillian.warp.HttpRequest> httpRequestFilter = (RequestFilter<org.jboss.arquillian.warp.HttpRequest>) filter;

                if (httpRequestFilter.matches(httpRequest)) {
                    payloads.add(enrichment.getPayload());
                }
            }
        }

        return payloads;
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

    private class HttpRequestWrapper implements org.jboss.arquillian.warp.HttpRequest {

        private HttpRequest request;

        public HttpRequestWrapper(HttpRequest request) {
            this.request = request;
        }

        @Override
        public String getMethod() {
            return request.getMethod().getName();
        }

        @Override
        public String getUri() {
            return request.getUri();
        }

    }
}
