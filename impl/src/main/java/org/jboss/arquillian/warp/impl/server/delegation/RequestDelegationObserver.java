/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.impl.server.delegation;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.warp.impl.server.event.ProcessHttpRequest;

/**
 * Checks each request for delegation to request processing services.
 *
 * @author Lukas Fryc
 */
public class RequestDelegationObserver {

    private Logger log = Logger.getLogger(RequestDelegationObserver.class.getName());

    @Inject
    private Event<DelegateRequest> delegate;

    /**
     * Checks whether the request should be delegated by some of the registered services.
     */
    public void checkRequestDelegation(@Observes EventContext<ProcessHttpRequest> ctx, ServiceLoader services,
            HttpServletRequest request, HttpServletResponse response) {

        Collection<RequestDelegationService> delegationServices = services.all(RequestDelegationService.class);

        for (RequestDelegationService service : delegationServices) {
            if (canDelegate(service, request)) {
                delegate.fire(new DelegateRequest(service));
                return;
            }
        }

        ctx.proceed();
    }

    /**
     * Delegates given request and response to be processed by given service.
     *
     * @throws RequestDelegationException in case the delegated request processing fails
     */
    public void delegate(@Observes DelegateRequest event, HttpServletRequest request, HttpServletResponse response) {
        RequestDelegationService service = event.getService();

        try {
            service.delegate(request, response);
        } catch (Exception e) {
            throw new RequestDelegationException(String.format("The request processing delegation failed: %s", e.getCause()), e);
        }
    }

    /**
     * Checks whether the given service can serve given request.
     */
    private boolean canDelegate(RequestDelegationService delegate, HttpServletRequest request) {
        try {
            return delegate.canDelegate(request);
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    String.format("The delegation service can't check the delegability of the request: %s", e.getCause()),
                    e.getCause());
            return false;
        }
    }
}
