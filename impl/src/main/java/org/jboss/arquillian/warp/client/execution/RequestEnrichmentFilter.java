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

import java.util.Arrays;

import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;
import org.jboss.arquillian.warp.server.filter.WarpFilter;
import org.jboss.arquillian.warp.shared.RequestPayload;
import org.jboss.arquillian.warp.shared.ResponsePayload;
import org.jboss.arquillian.warp.utils.SerializationUtils;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.HttpRequestFilter;

public class RequestEnrichmentFilter implements HttpRequestFilter {

    @Override
    public void filter(HttpRequest request) {
        if (AssertionHolder.isWaitingForProcessing()) {
            try {
                RequestPayload assertion = AssertionHolder.popRequest();
                String requestEnrichment = SerializationUtils.serializeToBase64(assertion);
                request.setHeader(WarpFilter.ENRICHMENT_REQUEST, Arrays.asList(requestEnrichment));
            } catch (Exception originalException) {
                ClientWarpExecutionException wrappedException = new ClientWarpExecutionException("enriching request failed: "
                        + originalException.getMessage(), originalException);
                ResponsePayload exceptionPayload = new ResponsePayload(wrappedException);
                AssertionHolder.pushResponse(exceptionPayload);
            }
        }
    }
}
