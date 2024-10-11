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
package org.jboss.arquillian.warp.impl.server.request;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.arquillian.warp.spi.context.RequestContext;
import org.jboss.arquillian.warp.spi.context.RequestScoped;
import org.jboss.arquillian.warp.spi.event.AfterRequest;
import org.jboss.arquillian.warp.spi.event.BeforeRequest;
import org.jboss.arquillian.warp.spi.servlet.event.ProcessHttpRequest;

/**
 * The handler for current context.
 *
 * @author Lukas Fryc
 */
public class RequestContextHandler {

    @Inject
    private Instance<RequestContext> requestContextInstance;

    @Inject
    @RequestScoped
    private InstanceProducer<ServletRequest> servletRequest;

    @Inject
    @RequestScoped
    private InstanceProducer<ServletResponse> servletResponse;

    @Inject
    @RequestScoped
    private InstanceProducer<HttpServletRequest> httpServletRequest;

    @Inject
    @RequestScoped
    private InstanceProducer<HttpServletResponse> httpServletResponse;

    @Inject
    @RequestScoped
    private InstanceProducer<FilterChain> filterChain;

    @Inject
    private Event<BeforeRequest> beforeRequest;

    @Inject
    private Event<AfterRequest> afterRequest;

    public void handleRequestContext(@Observes(precedence = 100) EventContext<ProcessHttpRequest> context) {
        RequestContext testContext = this.requestContextInstance.get();

        String requestId = String.valueOf(context.getEvent().getRequest().getAttribute(WarpCommons.WARP_REQUEST_ID));
        try {
            testContext.activate(requestId);

            servletRequest.set(context.getEvent().getRequest());
            servletResponse.set(context.getEvent().getResponse());
            httpServletRequest.set(context.getEvent().getRequest());
            httpServletResponse.set(context.getEvent().getResponse());
            filterChain.set(context.getEvent().getFilterChain());

            beforeRequest.fire(new BeforeRequest(context.getEvent().getRequest(), context.getEvent().getResponse()));

            context.proceed();
        } finally {
            try {
                afterRequest.fire(new AfterRequest(context.getEvent().getRequest(), context.getEvent().getResponse()));
            } finally {
                testContext.deactivate();
                testContext.destroy(requestId);
            }
        }
    }
}
