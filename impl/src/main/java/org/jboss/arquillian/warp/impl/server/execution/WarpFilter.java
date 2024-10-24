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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.hc.core5.http.protocol.RequestContent;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.jboss.arquillian.warp.impl.server.delegation.RequestDelegationService;
import org.jboss.arquillian.warp.impl.server.delegation.RequestDelegator;
import org.jboss.arquillian.warp.impl.server.event.ActivateManager;
import org.jboss.arquillian.warp.impl.server.event.PassivateManager;
import org.jboss.arquillian.warp.impl.server.request.RequestContextHandler;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.arquillian.warp.spi.servlet.event.ProcessHttpRequest;

/**
 * <p>
 * Filter that detects whenever the incoming request is enriched and thus should be processed by {@link WarpRequestProcessor}.
 * </p>
 * <p>
 * <p>
 * The filter is registered on server-side using /META-INF/web-fragment-warp.xml
 * </p>
 *
 * @author Lukas Fryc
 */
public class WarpFilter implements Filter {
    public static final String ARQUILLIAN_MANAGER_ATTRIBUTE = "org.jboss.arquillian.warp.TestManager";
    private static final String DEFAULT_EXTENSION_CLASS =
        "org.jboss.arquillian.core.impl.loadable.LoadableExtensionLoader";
    private Logger log = Logger.getLogger(WarpFilter.class.getSimpleName());
    private RequestDelegator delegator;
    private Manager manager;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            log.log(Level.FINE, "initializing {0}", WarpFilter.class.getSimpleName());
            ManagerBuilder builder = ManagerBuilder.from().extension(Class.forName(DEFAULT_EXTENSION_CLASS));
            manager = builder.create();
            manager.start();
            manager.bind(ApplicationScoped.class, Manager.class, manager);
            delegator = new RequestDelegator();
        } catch (Exception e) {
            throw new ServletException("Could not init " + WarpFilter.class.getSimpleName(), e);
        }
    }

    @Override
    public void destroy() {
        manager.shutdown();
        manager = null;
        delegator = null;
    }

    /**
     * Detects whenever the request is HTTP request and if yes, delegates to
     * {@link #doFilterHttp(HttpServletRequest, HttpServletResponse, FilterChain)}.
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, final FilterChain chain) throws IOException,
        ServletException {

        if (manager == null || isHttpRequest(req, resp)) {
            doFilterHttp((HttpServletRequest) req, (HttpServletResponse) resp, chain);
        } else {
            chain.doFilter(req, resp);
        }
    }

    private boolean isHttpRequest(ServletRequest req, ServletResponse resp) {
        return req instanceof HttpServletRequest && resp instanceof HttpServletResponse;
    }

    /**
     * <p>
     * Checks whether the request processing can be delegated to one of registered {@link RequestDelegationService}s.
     * </p>
     * <p>
     * <p>
     * If not, delegates processing to {@link #doFilterWarp(HttpServletRequest, HttpServletResponse, FilterChain)}.
     * </p>
     */
    private void doFilterHttp(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws IOException, ServletException {

        request.setAttribute(ARQUILLIAN_MANAGER_ATTRIBUTE, manager);

        boolean isDelegated = delegator.tryDelegateRequest(request, response, filterChain);

        if (!isDelegated) {
            doFilterWarp(request, response, filterChain);
        }
    }

    /**
     * <p>
     * Starts the Arquillian Manager, starts contexts and registers contextual instances.
     * </p>
     * <p>
     * <p>
     * Throws {@link ProcessHttpRequest} event which is used for further request processing.
     * </p>
     * <p>
     * <p>
     * Usually, the request is processed further by {@link HttpRequestProcessor} event observer.
     * </p>
     * <p>
     * <p>
     * The {@link ProcessHttpRequest} event is also intercepted by {@link RequestContextHandler} that activates {@link RequestContent}.
     * </p>
     *
     * @see HttpRequestProcessor
     * @see RequestContextHandler
     */
    private void doFilterWarp(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws IOException, ServletException {

        String requestId = UUID.randomUUID().toString();
        request.setAttribute(WarpCommons.WARP_REQUEST_ID, requestId);

        manager.fire(new ActivateManager(manager));
        manager.fire(new ProcessHttpRequest(request, response, filterChain));
        manager.fire(new PassivateManager(manager));
    }
}
