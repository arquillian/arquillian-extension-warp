/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.assertion.AssertionRegistry;
import org.jboss.arquillian.warp.lifecycle.BindLifecycleManager;
import org.jboss.arquillian.warp.lifecycle.LifecycleManager;
import org.jboss.arquillian.warp.lifecycle.LifecycleManagerStore;
import org.jboss.arquillian.warp.lifecycle.UnbindLifecycleManager;
import org.jboss.arquillian.warp.request.AfterRequest;
import org.jboss.arquillian.warp.request.BeforeRequest;
import org.jboss.arquillian.warp.test.AfterServletEvent;
import org.jboss.arquillian.warp.test.BeforeServletEvent;
import org.jboss.arquillian.warp.test.LifecycleEvent;
import org.jboss.arquillian.warp.utils.SerializationUtils;

/**
 * <p>
 * Filter which ensures detects and extracts {@link ServerAssertion}s from request and registers it in {@link AssertionRegistry}
 * .
 * </p>
 * 
 * <p>
 * The assertion can be retrieved from {@link AssertionRegistry} each time the {@link LifecycleEvent} is fired.
 * </p>
 * 
 * @author Lukas Fryc
 * 
 */
@WebFilter(urlPatterns = "/*")
public class WarpFilter implements Filter {

    private static final String ENRICHMENT = "X-Arq-Enrichment";
    public static final String ENRICHMENT_REQUEST = ENRICHMENT + "-Request";
    public static final String ENRICHMENT_RESPONSE = ENRICHMENT + "-Response";

    private static final String DEFAULT_EXTENSION_CLASS = "org.jboss.arquillian.core.impl.loadable.LoadableExtensionLoader";
    
    public static final String LIFECYCLE_MANAGER_STORE_REQUEST_ATTRIBUTE = WarpFilter.class.getName() + ".MANAGER_REQUEST_ATTRIBUTE";

    @Inject
    private Instance<LifecycleManager> lifecycleManager;
    
    @Inject
    private Instance<LifecycleManagerStore> lifecycleManagerStore;

    @Inject
    private Instance<AssertionRegistry> assertionRegistry;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        if (req instanceof HttpServletRequest && resp instanceof HttpServletResponse) {
            HttpServletRequest httpReq = ((HttpServletRequest) req);
            HttpServletResponse httpResp = ((HttpServletResponse) resp);

            String requestEnrichment = httpReq.getHeader(ENRICHMENT_REQUEST);

            if (requestEnrichment != null && !"null".equals(requestEnrichment)) {

                final AtomicReference<NonWritingServletOutputStream> stream = new AtomicReference<NonWritingServletOutputStream>();
                final AtomicReference<NonWritingPrintWriter> writer = new AtomicReference<NonWritingPrintWriter>();

                HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) resp) {
                    @Override
                    public ServletOutputStream getOutputStream() throws IOException {
                        stream.set(new NonWritingServletOutputStream());
                        return stream.get();
                    }

                    @Override
                    public PrintWriter getWriter() throws IOException {
                        writer.set(NonWritingPrintWriter.newInstance());
                        return writer.get();
                    }
                };

                String responseEnrichment = "null";

                try {
                    final Serializable assertionObject = SerializationUtils.deserializeFromBase64(requestEnrichment);

                    ManagerBuilder builder = ManagerBuilder.from().extension(Class.forName(DEFAULT_EXTENSION_CLASS));
                    Manager manager = builder.create();
                    manager.start();
                    manager.inject(this);
                    
                    req.setAttribute(LIFECYCLE_MANAGER_STORE_REQUEST_ATTRIBUTE, lifecycleManagerStore);

                    manager.fire(new BeforeSuite());
                    manager.fire(new BeforeRequest(req));
                    manager.fire(new BindLifecycleManager<ServletRequest>(ServletRequest.class, req));
                    

                    assertionRegistry.get().registerAssertion(assertionObject);

                    lifecycleManager.get().fireLifecycleEvent(new BeforeServletEvent());

                    chain.doFilter(req, responseWrapper);

                    lifecycleManager.get().fireLifecycleEvent(new AfterServletEvent());

                    assertionRegistry.get().unregisterAssertion(assertionObject);

                    manager.fire(new UnbindLifecycleManager<ServletRequest>(ServletRequest.class, req));
                    manager.fire(new AfterRequest(req));
                    manager.fire(new AfterSuite());

                    responseEnrichment = SerializationUtils.serializeToBase64(assertionObject);
                } catch (Exception e) {
                    throw new ServletException(e);
                }

                httpResp.setHeader(ENRICHMENT_RESPONSE, responseEnrichment);

                if (writer.get() != null) {
                    writer.get().finallyWriteAndClose(resp.getOutputStream());
                }
                if (stream.get() != null) {
                    stream.get().finallyWriteAndClose(resp.getOutputStream());
                }

                return;
            }
        }

        chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
    }

}
