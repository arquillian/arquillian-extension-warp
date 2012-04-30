package org.jboss.arquillian.jsfunitng.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import javax.resource.spi.IllegalStateException;
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
import org.jboss.arquillian.jsfunitng.assertion.AssertionRegistry;
import org.jboss.arquillian.jsfunitng.lifecycle.BindLifecycleManager;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManager;
import org.jboss.arquillian.jsfunitng.lifecycle.UnbindLifecycleManager;
import org.jboss.arquillian.jsfunitng.request.AfterRequest;
import org.jboss.arquillian.jsfunitng.request.BeforeRequest;
import org.jboss.arquillian.jsfunitng.test.AfterServletEvent;
import org.jboss.arquillian.jsfunitng.test.BeforeServletEvent;
import org.jboss.arquillian.jsfunitng.utils.SerializationUtils;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

@WebFilter(urlPatterns = "/*")
public class EnrichmentFilter implements Filter {

    private static final String ENRICHMENT = "X-Arq-Enrichment";
    public static final String ENRICHMENT_REQUEST = ENRICHMENT + "-Request";
    public static final String ENRICHMENT_RESPONSE = ENRICHMENT + "-Response";

    private static final String DEFAULT_EXTENSION_CLASS = "org.jboss.arquillian.core.impl.loadable.LoadableExtensionLoader";

    @Inject
    private Instance<LifecycleManager> lifecycleManager;

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

            // String requestEnrichment = req.getParameter(ENRICHMENT_REQUEST);
            String requestEnrichment = httpReq.getHeader(ENRICHMENT_REQUEST);

            if (requestEnrichment != null && !"null".equals(requestEnrichment)) {
                // final NonClosingPrintWriter out = new NonClosingPrintWriter(resp.getWriter());

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

                    manager.fire(new BeforeSuite());
                    manager.fire(new BeforeRequest(req));
                    manager.fire(new BindLifecycleManager<ServletRequest>(req, ServletRequest.class, req));

                    assertionRegistry.get().registerAssertion(assertionObject);

                    lifecycleManager.get().fireLifecycleEvent(new BeforeServletEvent());

                    chain.doFilter(req, responseWrapper);

                    lifecycleManager.get().fireLifecycleEvent(new AfterServletEvent());

                    assertionRegistry.get().unregisterAssertion(assertionObject);

                    manager.fire(new UnbindLifecycleManager<ServletRequest>(req, ServletRequest.class, req));
                    manager.fire(new AfterRequest(req));
                    manager.fire(new AfterSuite());

                    responseEnrichment = SerializationUtils.serializeToBase64(assertionObject);
                } catch (Exception e) {
                    throw new ServletException(e);
                }

                // out.write(ENRICHMENT_RESPONSE + "=" + responseEnrichment);
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
        try {
            // testRunnerAdaptor.afterSuite();
            // testRunnerAdaptor.shutdown();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

}
