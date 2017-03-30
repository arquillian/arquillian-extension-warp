/*
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.warp.RequestObserver;
import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.filter.http.HttpRequest;
import org.jboss.arquillian.warp.client.filter.http.HttpRequestFilter;
import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;
import org.jboss.arquillian.warp.impl.client.enrichment.HttpRequestEnrichmentService;
import org.jboss.arquillian.warp.impl.server.inspection.PayloadRegistry;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.shared.command.Command;
import org.jboss.arquillian.warp.impl.shared.command.CommandService;
import org.jboss.arquillian.warp.impl.utils.Rethrow;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.arquillian.warp.spi.observer.RequestObserverChainManager;

/**
 * Default implementation of service for enriching HTTP requests
 *
 * @author Lukas Fryc
 */
public class DefaultHttpRequestEnrichmentService implements HttpRequestEnrichmentService {

    private Logger log = Logger.getLogger("Warp");

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.arquillian.warp.impl.client.enrichment.HttpRequestEnrichmentService#getMatchingPayloads(org.jboss.netty.handler
     * .codec.http.HttpRequest)
     */
    @Override
    public Collection<RequestPayload> getMatchingPayloads(HttpRequest request) {

        final Collection<WarpGroup> groups = warpContext().getAllGroups();

        final Collection<RequestPayload> payloads = new LinkedList<RequestPayload>();

        groupIteration : for (WarpGroup group : groups) {

            Deque<RequestObserver> observers = new LinkedList<RequestObserver>();
            if (group.getObserver() != null) {
                observers.add(group.getObserver());
            }
            Collection<RequestObserverChainManager> observerChainManagers = warpContext().getObserverChainManagers();

            for (RequestObserverChainManager chainManager : observerChainManagers) {
                chainManager.manageObserverChain(observers, HttpRequestFilter.class);
            }

            for (RequestObserver observer : observers) {
                if (!isHttpFilter(observer)) {
                    log.warning("One of the defined observers (" + observer.toString() + ") of class " + observer.getClass()
                            + " doesn't match expected type (" + HttpRequestFilter.class
                            + ") - continuing without processing this request group");
                    continue groupIteration;
                }

                @SuppressWarnings("unchecked")
                RequestFilter<HttpRequest> filter = (RequestFilter<HttpRequest>) observer;

                if (!filter.matches(request)) {
                    continue groupIteration;
                }
            }

            payloads.add(group.generateRequestPayload(request));
        }

        return payloads;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.arquillian.warp.impl.client.enrichment.HttpRequestEnrichmentService#enrichRequest(org.jboss.netty.handler.codec
     * .http.HttpRequest, java.util.Collection)
     */
    @Override
    public void enrichRequest(HttpRequest request, RequestPayload payload) {

        if (WarpCommons.debugMode()) {
            System.out.println("                (W) " + request.getUri());
        }

        if (log.isLoggable(Level.FINE)) {
            log.fine("Warp request: " + request.getUri());
        }

        try {
            String requestEnrichment = SerializationUtils.serializeToBase64(payload);
            long serialId = payload.getSerialId();
            remoteOperationService().execute(new RegisterPayloadRemotely(requestEnrichment));

            io.netty.handler.codec.http.HttpRequest nettyHttpRequest = ((HttpRequestWrapper) request).unwrap();
            nettyHttpRequest.headers().set(WarpCommons.ENRICHMENT_REQUEST, Arrays.asList(Long.toString(serialId)));
        } catch (Throwable originalException) {
            Throwable cause = Rethrow.getOriginalCause(originalException);
            ClientWarpExecutionException explainingException = new ClientWarpExecutionException("enriching request failed; caused by:\n"
                    + cause.getClass().getName() + ": " + cause.getMessage(), originalException);
            warpContext().pushException(explainingException);
        }
    }

    private boolean isHttpFilter(RequestObserver observer) {
        return observer instanceof RequestFilter
                && isType((RequestFilter<?>) observer, org.jboss.arquillian.warp.client.filter.http.HttpRequest.class);
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

    private CommandService remoteOperationService() {
        return serviceLoader.get().onlyOne(CommandService.class);
    }

    public static class RegisterPayloadRemotely implements Command {

        private static final long serialVersionUID = 1L;

        @Inject
        private transient Instance<PayloadRegistry> registry;

        private String requestPayload;

        public RegisterPayloadRemotely(String requestPayload) {
            this.requestPayload = requestPayload;
        }

        @Override
        public void perform() {
            RequestPayload payload = SerializationUtils.deserializeFromBase64(requestPayload);
            registry.get().registerRequestPayload(payload);
        }
    }

}
