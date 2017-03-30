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
package org.jboss.arquillian.warp.impl.server.execution;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.warp.impl.server.enrichment.HttpRequestDeenricher;
import org.jboss.arquillian.warp.impl.server.enrichment.HttpResponseEnricher;
import org.jboss.arquillian.warp.impl.server.event.EnrichHttpResponse;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.arquillian.warp.spi.context.RequestScoped;
import org.jboss.arquillian.warp.spi.servlet.event.ProcessHttpRequest;
import org.jboss.arquillian.warp.spi.servlet.event.ProcessWarpRequest;

public class HttpRequestProcessor {

    @Inject
    private Event<ProcessWarpRequest> processWarpRequest;

    @Inject
    @RequestScoped
    private InstanceProducer<RequestPayload> requestPayload;

    @Inject
    @RequestScoped
    private InstanceProducer<ResponsePayload> responsePayload;

    public void processHttpRequest(@Observes ProcessHttpRequest event, ServiceLoader services, HttpServletRequest request,
        HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        // setup responsePayload with temporary serialId before requestPayload is de-serialized
        responsePayload.set(new ResponsePayload(RequestPayload.FAILURE_SERIAL_ID));

        HttpRequestDeenricher requestDeenricher = services.onlyOne(HttpRequestDeenricher.class);

        if (requestDeenricher.isEnriched()) {

            RequestPayload p = requestDeenricher.resolvePayload();
            long serialId = p.getSerialId();
            responsePayload.set(new ResponsePayload(serialId));
            response.setHeader(WarpCommons.ENRICHMENT_RESPONSE, Long.toString(serialId));
            requestPayload.set(p);

            processWarpRequest.fire(new ProcessWarpRequest());
        } else {
            filterChain.doFilter(request, response);
        }
    }

    public void enrichHttpResponse(@Observes EnrichHttpResponse event, ServiceLoader services) {
        HttpResponseEnricher responseEnricher = services.onlyOne(HttpResponseEnricher.class);
        responseEnricher.enrichResponse();
    }
}
