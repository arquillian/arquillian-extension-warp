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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.ExceptionProxy;
import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;
import org.jboss.arquillian.warp.exception.ServerWarpExecutionException;
import org.jboss.arquillian.warp.exception.WarpExecutionException;
import org.jboss.arquillian.warp.impl.client.enrichment.HttpResponseDeenrichmentService;
import org.jboss.arquillian.warp.impl.client.event.VerifyResponsePayload;
import org.jboss.arquillian.warp.impl.server.inspection.PayloadRegistry;
import org.jboss.arquillian.warp.impl.server.inspection.PayloadRegistry.ResponsePayloadWasNeverRegistered;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.jboss.arquillian.warp.impl.shared.command.Command;
import org.jboss.arquillian.warp.impl.shared.command.CommandService;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 * Default service for de-enriching responses.
 *
 * @author Lukas Fryc
 */
public class DefaultResponseDeenrichmentService implements HttpResponseDeenrichmentService {

    private final Logger log = Logger.getLogger(HttpResponseDeenrichmentService.class.getName());

    @Inject
    private Event<VerifyResponsePayload> verifyResponsePayload;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.arquillian.warp.impl.client.enrichment.HttpResponseDeenrichmentService#isEnriched(org.jboss.netty.handler.codec
     * .http.HttpResponse)
     */
    @Override
    public boolean isEnriched(HttpRequest request, HttpResponse response) {
        Long serialId = getSerialId(request);
        return serialId != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.arquillian.warp.impl.client.enrichment.HttpResponseDeenrichmentService#deenrichResponse(org.jboss.netty.handler
     * .codec.http.HttpResponse)
     */
    @Override
    public void deenrichResponse(HttpRequest request, HttpResponse response) {
        final WarpContext context = WarpContextStore.get();
        try {

            long serialId = getSerialId(request);

            ResponsePayload payload = retrieveResponsePayload(serialId);

            if (context != null) {
                verifyResponsePayload.fire(new VerifyResponsePayload(payload));
                context.pushResponsePayload(payload);
            }
        } catch (Exception originalException) {

            if (context != null) {

                WarpExecutionException explainingException;

                if (originalException instanceof WarpExecutionException) {
                    explainingException = (WarpExecutionException) originalException;
                } else {
                    explainingException = new ClientWarpExecutionException("deenriching response failed: "
                            + originalException.getMessage(), originalException);
                }

                context.pushException(explainingException);
            } else {
                log.log(Level.WARNING, "Unable to push exception to WarpContext", originalException);
            }
        }
    }

    /**
     * Contacts server and tries to retrieve response payload via serialId.
     *
     * Repeats the retrieval until the payload is found or number of allowed iterations is reached.
     */
    private ResponsePayload retrieveResponsePayload(long serialId) throws InterruptedException {
        ResponsePayloadWasNeverRegistered last = null;
        for (int i = 0; i <= 10; i++) {
            try {
                RetrievePayloadFromServer result = remoteOperationService().execute(new RetrievePayloadFromServer(serialId));
                if (result.getExceptionProxy() != null) {
                    throw new ServerWarpExecutionException(result.getExceptionProxy().createException());
                } else {
                    return result.getResponsePayload();
                }
            } catch (ResponsePayloadWasNeverRegistered e) {
                Thread.sleep(300);
                last = e;
            }
        }
        throw last;
    }

    private Long getSerialId(HttpRequest request) {
        String header = request.getHeader(WarpCommons.ENRICHMENT_REQUEST);

        if (header == null || header.isEmpty()) {
            return null;
        }

        return Long.valueOf(header);
    }

    private CommandService remoteOperationService() {
        return serviceLoader.get().onlyOne(CommandService.class);
    }

    public static class RetrievePayloadFromServer implements Command {

        private static final long serialVersionUID = 1L;

        @Inject
        private transient Instance<PayloadRegistry> registry;

        private long serialId;
        private byte[] serializedPayload;
        private ExceptionProxy exceptionProxy;

        public RetrievePayloadFromServer(long serialId) {
            this.serialId = serialId;
        }

        public ResponsePayload getResponsePayload() {
            return SerializationUtils.deserializeFromBytes(serializedPayload);
        }

        @Override
        public void perform() {
            try {
                ResponsePayload responsePayload = registry.get().retrieveResponsePayload(serialId);
                serializedPayload = SerializationUtils.serializeToBytes(responsePayload);
            } catch (Throwable e) {
                exceptionProxy = ExceptionProxy.createForException(e);
            }
        }

        public ExceptionProxy getExceptionProxy() {
            return exceptionProxy;
        }
    }
}
