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
package org.jboss.arquillian.warp.spi.servlet.event;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.warp.spi.context.RequestContext;

/**
 * <p>
 * The event is fired for each incoming request (except Warp command service requests).
 * </p>
 *
 * <p>
 * The event activates {@link RequestContext} and is observed by Warp implementation in order to trigger request filtering and
 * Warp processing if required.
 * </p>
 *
 * <p>
 * As a consequence of triggering this event, Warp implementation needs to decide whether HTTP request that fulfills conditions
 * to be processed as Warp request. If yes, {@link ProcessWarpRequest} is fired. Otherwise the request is processed as usually
 * without Warp processing.
 * </p>
 *
 * @see ProcessWarpRequest
 */
public class ProcessHttpRequest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    public ProcessHttpRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        this.request = request;
        this.response = response;
        this.filterChain = filterChain;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public FilterChain getFilterChain() {
        return filterChain;
    }

}
