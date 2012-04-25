package org.jboss.arquillian.jsfunitng.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.jboss.arquillian.jsfunitng.AssertionObject;
import org.jboss.arquillian.jsfunitng.assertion.AssertionRegistry;
import org.jboss.arquillian.jsfunitng.lifecycle.BindLifecycleManager;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManager;
import org.jboss.arquillian.jsfunitng.lifecycle.UnbindLifecycleManager;
import org.jboss.arquillian.jsfunitng.request.AfterRequest;
import org.jboss.arquillian.jsfunitng.request.BeforeRequest;
import org.jboss.arquillian.jsfunitng.test.BeforeServlet;
import org.jboss.arquillian.jsfunitng.test.BeforeServletEvent;
import org.jboss.arquillian.jsfunitng.test.LifecycleEvent;
import org.jboss.arquillian.jsfunitng.utils.SerializationUtils;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

@WebFilter(urlPatterns = "/*")
public class EnrichmentFilter implements Filter {

    private static final String ENRICHMENT = "X-Arq-Enrichment";
    private static final String ENRICHMENT_REQUEST = ENRICHMENT + "-Request";
    private static final String ENRICHMENT_RESPONSE = ENRICHMENT + "-Response";

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
        if (resp instanceof HttpServletResponse) {

            if (req.getParameter(ENRICHMENT_REQUEST) != null) {
                final NonClosingPrintWriter out = new NonClosingPrintWriter(resp.getWriter());

                HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) resp) {
                    public PrintWriter getWriter() throws IOException {
                        return out;
                    }
                };

                String requestEnrichment = req.getParameter(ENRICHMENT_REQUEST);

                chain.doFilter(req, responseWrapper);

                String responseEnrichment = "null";

                if (!"null".equals(requestEnrichment)) {

                    try {
                        final AssertionObject assertionObject = SerializationUtils.deserializeFromBase64(requestEnrichment);
                        final Method testMethod = AssertionObject.class.getMethod("method");

                        ManagerBuilder builder = ManagerBuilder.from().extension(Class.forName(DEFAULT_EXTENSION_CLASS));
                        Manager manager = builder.create();
                        manager.start();
                        manager.inject(this);

                        manager.fire(new BeforeSuite());
                        manager.fire(new BeforeRequest(req));
                        manager.fire(new BindLifecycleManager<ServletRequest>(req, ServletRequest.class, req));
                        
                        assertionRegistry.get().registerAssertion(assertionObject);

                        lifecycleManager.get().fireLifecycleEvent(new BeforeServletEvent());

                        manager.fire(new UnbindLifecycleManager<ServletRequest>(req, ServletRequest.class, req));
                        manager.fire(new AfterRequest(req));
                        manager.fire(new AfterSuite());

                        assertionObject.setPayload("client");
                        responseEnrichment = SerializationUtils.serializeToBase64(assertionObject);
                    } catch (Exception e) {
                        // TODO handle exception
                        e.printStackTrace();
                    }
                }

                out.write(ENRICHMENT_RESPONSE + "=" + responseEnrichment);

                out.closeFinally();

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
