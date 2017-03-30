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

import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;
import org.jboss.arquillian.warp.impl.client.event.FilterHttpRequest;
import org.jboss.arquillian.warp.impl.client.event.FilterHttpResponse;
import org.jboss.arquillian.warp.impl.client.event.TransformHttpResponse;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionScoped;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

public class DefaultHttpFiltersSource extends HttpFiltersSourceAdapter {

    private static final int MAX_BUFFER_SIZE_IN_BYTES = 5 * 1024 * 1024; // 5MB

    private Logger log = Logger.getLogger(Warp.class.getName());

    @Inject
    private Event<FilterHttpRequest> tryEnrichRequest;

    @Inject
    private Event<FilterHttpResponse> tryDeenrichResponse;

    @Inject @WarpExecutionScoped
    private InstanceProducer<HttpResponse> responseInstance;

    @Inject
    private Event<TransformHttpResponse> transformHttpResponse;

    @Override
    public HttpFilters filterRequest(final HttpRequest originalRequest) {

        return new HttpFiltersAdapter(originalRequest) {

            private HttpRequest request;

            @Override
            public HttpResponse requestPost(HttpObject httpObject) {

                final WarpContext context = WarpContextStore.get();

                if (httpObject instanceof HttpRequest) {
                    this.request = (HttpRequest) httpObject;
                }

                if (context == null) {
                    return null;
                }

                try {
                    final SynchronizationPoint synchronization = context.getSynchronization();

                    if (synchronization.isWaitingForRequests()) {

                        if (httpObject instanceof HttpRequest) {
                            tryEnrichRequest.fire(new FilterHttpRequest(new HttpRequestWrapper(this.request)));
                        }
                    }
                } catch (Exception originalException) {
                    ClientWarpExecutionException explainingException = new ClientWarpExecutionException(
                            "enriching request failed: " + originalException.getMessage(), originalException);
                    context.pushException(explainingException);
                }

                return null;
            }

            @Override
            public HttpObject responsePost(HttpObject httpObject) {

                try {
                    if (this.request instanceof HttpRequest && httpObject instanceof HttpResponse) {
                        HttpResponse response = (HttpResponse) httpObject;

                        tryDeenrichResponse.fire(new FilterHttpResponse(this.request, response));

                        TransformHttpResponse transformEvent = new TransformHttpResponse(request, response);
                        transformHttpResponse.fire(transformEvent);

                        return transformEvent.getResponse();
                    }

                } catch (Exception originalException) {
                    final WarpContext context = WarpContextStore.get();

                    if (context == null) {
                        log.log(Level.WARNING, originalException.getMessage(), originalException);
                        return httpObject;
                    }
                    ClientWarpExecutionException explainingException = new ClientWarpExecutionException(
                            "deenriching response failed: " + originalException.getMessage(), originalException);
                    context.pushException(explainingException);
                }

                return httpObject;
            }
        };
    }

    @Override
    public int getMaximumRequestBufferSizeInBytes() {
        return MAX_BUFFER_SIZE_IN_BYTES;
    }

    @Override
    public int getMaximumResponseBufferSizeInBytes() {
        return MAX_BUFFER_SIZE_IN_BYTES;
    }
}
