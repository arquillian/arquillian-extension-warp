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
package org.jboss.arquillian.warp.server.filter;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Filter that detects whenever the incoming request is enriched and thus should be processed by {@link WarpRequestProcessor}.
 * </p>
 *
 * @author Lukas Fryc
 */
@WebFilter(urlPatterns = "/*", asyncSupported = true)
public class WarpFilter implements Filter {
    
    private static final Logger LOG = Logger.getLogger(WarpFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * Detects whenever the request is HTTP request and if yes, delegates to
     * {@link #doFilterHttp(HttpServletRequest, HttpServletResponse, FilterChain)}.
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, final FilterChain chain) throws IOException,
            ServletException {

        if (req instanceof HttpServletRequest && resp instanceof HttpServletResponse) {
            doFilterHttp((HttpServletRequest) req, (HttpServletResponse) resp, chain);
        } else {
            chain.doFilter(req, resp);
        }
    }

    /**
     * Detects whenever the request is enriched by Warp and if yes, delegates to {
     * {@link #doFilterWarp(HttpServletRequest, HttpServletResponse, FilterChain, WarpRequest)}
     */
    private void doFilterHttp(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException,
            ServletException {

        WarpRequest warpRequest = new WarpRequest(req);

        if (warpRequest.isEnriched()) {
            LOG.fine("Warp request filtering started");
            doFilterWarp(req, resp, chain, warpRequest);
            LOG.fine("Warp request filtering ended");
        } else {
            chain.doFilter(req, resp);
        }
    }

    /**
     * Delegates to {@link WarpRequestProcessor} in order to process the request
     */
    private void doFilterWarp(HttpServletRequest req, HttpServletResponse resp, FilterChain chain, WarpRequest warpRequest)
            throws IOException, ServletException {

        DoFilterCommand filterCommand = createFilterCommand(chain);
        WarpRequestProcessor requestProcessor = new WarpRequestProcessor(req, resp);
        requestProcessor.process(warpRequest, filterCommand);
    }

    /**
     * Creates the command which delegates to provided chain
     */
    private DoFilterCommand createFilterCommand(final FilterChain chain) {
        return new DoFilterCommand() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws ServletException, IOException {
                chain.doFilter(request, response);
            }
        };
    }

    @Override
    public void destroy() {
    }

}
