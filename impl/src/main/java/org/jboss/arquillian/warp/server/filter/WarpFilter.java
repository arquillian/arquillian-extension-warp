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
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.extension.servlet.AfterServletEvent;
import org.jboss.arquillian.warp.extension.servlet.BeforeServletEvent;
import org.jboss.arquillian.warp.server.assertion.AssertionRegistry;
import org.jboss.arquillian.warp.server.lifecycle.LifecycleManagerImpl;
import org.jboss.arquillian.warp.server.lifecycle.LifecycleManagerStoreImpl;
import org.jboss.arquillian.warp.server.request.AfterRequest;
import org.jboss.arquillian.warp.server.request.BeforeRequest;
import org.jboss.arquillian.warp.server.test.TestResultStore;
import org.jboss.arquillian.warp.shared.RequestPayload;
import org.jboss.arquillian.warp.shared.ResponsePayload;
import org.jboss.arquillian.warp.spi.LifecycleEvent;
import org.jboss.arquillian.warp.spi.WarpCommons;
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

    private static Logger log = Logger.getLogger("Proxy");

    @Inject
    private Instance<LifecycleManagerImpl> lifecycleManager;

    @Inject
    private Instance<LifecycleManagerStoreImpl> lifecycleManagerStore;

    @Inject
    private Instance<AssertionRegistry> assertionRegistry;

    @Inject
    private Instance<TestResultStore> testResultStore;

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
                    RequestPayload requestPayload = SerializationUtils.deserializeFromBase64(requestEnrichment);
                    ServerAssertion serverAssertion = requestPayload.getAssertion();

                    ManagerBuilder builder = ManagerBuilder.from().extension(Class.forName(DEFAULT_EXTENSION_CLASS));
                    Manager manager = builder.create();
                    manager.start();
                    manager.bind(ApplicationScoped.class, Manager.class, manager);
                    manager.inject(this);

                    req.setAttribute(WarpCommons.LIFECYCLE_MANAGER_STORE_REQUEST_ATTRIBUTE, lifecycleManagerStore);

                    manager.fire(new BeforeSuite());
                    manager.fire(new BeforeRequest(req));
                    lifecycleManagerStore.get().bind(ServletRequest.class, req);

                    assertionRegistry.get().registerAssertion(serverAssertion);

                    lifecycleManager.get().fireLifecycleEvent(new BeforeServletEvent());

                    try {
                        chain.doFilter(req, responseWrapper);

                        lifecycleManager.get().fireLifecycleEvent(new AfterServletEvent());

                        // request successfully finished
                        TestResult firstFailedResult = testResultStore.get().getFirstFailed();
                        if (firstFailedResult == null) {
                            ResponsePayload responsePayload = new ResponsePayload(serverAssertion);
                            responseEnrichment = SerializationUtils.serializeToBase64(responsePayload);
                            httpResp.setHeader(ENRICHMENT_RESPONSE, responseEnrichment);
                        } else {
                            Throwable throwable = firstFailedResult.getThrowable();
                            if (throwable instanceof InvocationTargetException) {
                                throwable = throwable.getCause();
                            }
                            ResponsePayload responsePayload = new ResponsePayload(throwable);
                            enrichResponse(httpResp, responsePayload);
                        }
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "The error occured during request execution", e);
                        throw e;
                    } finally {
                        assertionRegistry.get().unregisterAssertion(serverAssertion);

                        lifecycleManagerStore.get().unbind(ServletRequest.class, req);
                        manager.fire(new AfterRequest(req));
                        manager.fire(new AfterSuite());
                    }

                } catch (Throwable e) {
                    // exception occured during request execution
                    ResponsePayload responsePayload = new ResponsePayload(e);
                    responseEnrichment = SerializationUtils.serializeToBase64(responsePayload);
                    httpResp.setHeader(ENRICHMENT_RESPONSE, responseEnrichment);
                    httpResp.sendError(500);
                }

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

    public void enrichResponse(HttpServletResponse httpResp, ResponsePayload payload) {
        String enrichment = SerializationUtils.serializeToBase64(payload);
        httpResp.setHeader(ENRICHMENT_RESPONSE, enrichment);
    }

    @Override
    public void destroy() {
    }

}
