package org.jboss.arquillian.jsfunitng.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.jsfunitng.AssertionObject;
import org.jboss.arquillian.jsfunitng.utils.SerializationUtils;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder;

@WebFilter(urlPatterns = "/*")
public class EnrichmentFilter implements Filter {

    private static final String ENRICHMENT = "X-Arq-Enrichment";
    private static final String ENRICHMENT_REQUEST = ENRICHMENT + "-Request";
    private static final String ENRICHMENT_RESPONSE = ENRICHMENT + "-Response";

    Manager manager;

    TestRunnerAdaptor testRunnerAdaptor;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // ManagerBuilder builder = ManagerBuilder.from()
        // .extension(SecurityActions.loadClass(DEFAULT_EXTENSION_CLASS));
        // manager = ManagerBuilder.from().create();
        // manager.fire(new BeforeSuite());

        try {
            testRunnerAdaptor = TestRunnerAdaptorBuilder.build();
            testRunnerAdaptor.beforeSuite();
        } catch (Exception e) {
            // TODO: handle exception
        }
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
                    
                    
                    AssertionObject assertionObject = SerializationUtils.deserializeFromBase64(requestEnrichment);

                    // inject

                    assertionObject.method();

                    assertionObject.setPayload("client");
                    responseEnrichment = SerializationUtils.serializeToBase64(assertionObject);
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
            testRunnerAdaptor.afterSuite();
            testRunnerAdaptor.shutdown();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}
