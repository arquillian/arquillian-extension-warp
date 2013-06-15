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
import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;
import org.jboss.arquillian.warp.impl.client.enrichment.HttpResponseDeenrichmentService;
import org.jboss.arquillian.warp.impl.client.event.VerifyResponsePayload;
import org.jboss.arquillian.warp.impl.server.inspection.PayloadRegistry;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.jboss.arquillian.warp.impl.shared.command.Command;
import org.jboss.arquillian.warp.impl.shared.command.CommandService;
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

            // ensures that some content has been already written
            // we should actually ensure here that whole message was read
            response.getContent().readableBytes();

            long serialId = getSerialId(request);

            ResponsePayload payload = remoteOperationService().execute(new RetrievePayloadFromServer(serialId))
                    .getResponsePayload();

            if (context != null) {
                verifyResponsePayload.fire(new VerifyResponsePayload(payload));
                context.pushResponsePayload(payload);
            }
        } catch (Exception originalException) {

            if (context != null) {

                ClientWarpExecutionException explainingException;

                if (originalException instanceof ClientWarpExecutionException) {
                    explainingException = (ClientWarpExecutionException) originalException;
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
        private ResponsePayload responsePayload;

        public RetrievePayloadFromServer(long serialId) {
            this.serialId = serialId;
        }

        public ResponsePayload getResponsePayload() {
            return responsePayload;
        }

        @Override
        public void perform() {
            responsePayload = registry.get().retrieveResponsePayload(serialId);
        }
    }
}
