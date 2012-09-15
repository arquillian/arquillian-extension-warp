package org.jboss.arquillian.warp.impl.client.execution;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.impl.client.enrichment.RequestEnrichmentService;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class DefaultRequestEnrichmentService implements RequestEnrichmentService {

    @Override
    public void enrichRequest(HttpRequest request, Collection<RequestPayload> payloads) {
        RequestPayload assertion = payloads.iterator().next();
        String requestEnrichment = SerializationUtils.serializeToBase64(assertion);
        request.setHeader(WarpCommons.ENRICHMENT_REQUEST, Arrays.asList(requestEnrichment));
    }

    @Override
    public Collection<RequestPayload> getMatchingPayloads(HttpRequest request) {
        final Set<RequestEnrichment> requests = AssertionHolder.getRequests();
        final org.jboss.arquillian.warp.client.filter.HttpRequest httpRequest = new HttpRequestWrapper(request);
        final Collection<RequestPayload> payloads = new LinkedList<RequestPayload>();

        for (RequestEnrichment enrichment : requests) {
            RequestFilter<?> filter = enrichment.getFilter();

            if (filter == null) {
                payloads.add(enrichment.getPayload());
                continue;
            }

            if (isType(filter, org.jboss.arquillian.warp.client.filter.HttpRequest.class)) {

                @SuppressWarnings("unchecked")
                RequestFilter<org.jboss.arquillian.warp.client.filter.HttpRequest> httpRequestFilter = (RequestFilter<org.jboss.arquillian.warp.client.filter.HttpRequest>) filter;

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

    private class HttpRequestWrapper implements org.jboss.arquillian.warp.client.filter.HttpRequest {

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
