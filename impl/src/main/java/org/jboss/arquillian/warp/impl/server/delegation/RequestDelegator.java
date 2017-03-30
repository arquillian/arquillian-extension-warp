/*
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

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Checks each request for delegation to request processing services.
 *
 * @author Lukas Fryc
 */
public class RequestDelegator {

    private Logger log = Logger.getLogger(RequestDelegator.class.getName());

    private final List<RequestDelegationService> delegationServices = new ArrayList<RequestDelegationService>();

    public RequestDelegator() {
        // Keeping a ServiceLoader Instance and iterating over it at runtime is not a good idea.
        // NoSuchElementExceptions may arise due to ServiceLoader's LazyIterator.
        // Instead, we iterate over the ServiceLoader once and cache the result on a List.
        for (RequestDelegationService service : ServiceLoader.load(RequestDelegationService.class)) {
            delegationServices.add(service);
        }
    }

    /**
     * Checks whether the request should be delegated to some of the registered {@link RequestDelegationService}s.
     */
    public boolean tryDelegateRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

        for (RequestDelegationService service : delegationServices) {
            if (canDelegate(service, request)) {
                delegate(service, request, response, filterChain);
                return true;
            }
        }

        return false;
    }

    /**
     * Delegates given request and response to be processed by given service.
     *
     * @throws RequestDelegationException in case the delegated request processing fails
     */
    private void delegate(RequestDelegationService service, HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) {
        try {
            service.delegate(request, response, filterChain);
        } catch (Exception e) {
            throw new RequestDelegationException(
                String.format("The request processing delegation failed: %s", e.getCause()), e);
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
