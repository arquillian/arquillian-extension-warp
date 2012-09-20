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
import org.jboss.arquillian.warp.impl.server.event.ProcessHttpRequest;
import org.jboss.arquillian.warp.impl.server.event.ProcessWarpRequest;
import org.jboss.arquillian.warp.impl.server.request.RequestScoped;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

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

        HttpRequestDeenricher requestDeenricher = services.onlyOne(HttpRequestDeenricher.class);

        if (requestDeenricher.isEnriched()) {
            
            responsePayload.set(new ResponsePayload());
            requestPayload.set(requestDeenricher.resolvePayload());
            
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
