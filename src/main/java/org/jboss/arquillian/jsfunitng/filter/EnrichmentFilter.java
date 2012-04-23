package org.jboss.arquillian.jsfunitng.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.jsfunitng.AssertionObject;
import org.jboss.arquillian.jsfunitng.utils.SerializationUtils;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.spi.event.suite.Test;

@WebFilter(urlPatterns = "/*")
public class EnrichmentFilter implements Filter {

    private static final String ENRICHMENT = "X-Arq-Enrichment";
    private static final String ENRICHMENT_REQUEST = ENRICHMENT + "-Request";
    private static final String ENRICHMENT_RESPONSE = ENRICHMENT + "-Response";

    private static final String DEFAULT_EXTENSION_CLASS = "org.jboss.arquillian.core.impl.loadable.LoadableExtensionLoader";

    private Manager manager;

    @Inject
    Instance<ServiceLoader> serviceLoader;

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
                        ManagerBuilder builder = ManagerBuilder.from().extension(Class.forName(DEFAULT_EXTENSION_CLASS));

                        final Method testMethod = AssertionObject.class.getMethod("method");

                        manager = builder.create();
                        manager.start();
                        manager.inject(this);
                        manager.fire(new BeforeSuite());
                        manager.fire(new BeforeClass(AssertionObject.class));
                        backupAllFields(assertionObject);
                        manager.fire(new Before(assertionObject, testMethod));
                        backupUpdatedFields(assertionObject);
                        
                        manager.fire(new Test(new TestMethodExecutor() {
                            
                            @Override
                            public void invoke(Object... parameters) throws Throwable {
                                getMethod().invoke(getInstance(), parameters);
                            }
                            
                            @Override
                            public Method getMethod() {
                                return testMethod;
                            }
                            
                            @Override
                            public Object getInstance() {
                                return assertionObject;
                            }
                        }));

                        assertionObject.method();
                        assertionObject.beanMethod();

                        manager.fire(new After(assertionObject, testMethod));
                        restoreFields(assertionObject);
                        manager.fire(new AfterClass(AssertionObject.class));
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

    Map<Field, Object> backupAll = new HashMap<Field, Object>();
    Map<Field, Object> backupUpdated = new HashMap<Field, Object>();

    private void backupAllFields(Object instance) {
        try {
            List<Field> fields = SecurityActions.getFields(instance.getClass());
            for (Field field : fields) {
                backupAll.put(field, field.get(instance));
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    private void backupUpdatedFields(Object instance) {
        try {
            for (Entry<Field, Object> entry : backupAll.entrySet()) {
                Field field = entry.getKey();
                Object oldValue = entry.getValue();
                
                Object newValue = field.get(instance);
                
                if (oldValue != newValue) {
                    backupUpdated.put(field, oldValue);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        backupAll.clear();
    }
    
    private void restoreFields(Object instance) {
        try {
            for (Entry<Field, Object> entry : backupUpdated.entrySet()) {
                Field field = entry.getKey();
                Object oldValue = entry.getValue();
                
                field.set(instance, oldValue);
                
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        backupUpdated.clear();
    }

}
