package org.jboss.arquillian.warp;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.client.execution.RequestExecutor;

public class ServiceInjector {
    
    @Inject
    Instance<ServiceLoader> serviceLoader;
    
    public void injectRequestExecutor(@Observes BeforeClass event) {
        if (event.getTestClass().isAnnotationPresent(WarpTest.class)) {
            RequestExecutor executor = serviceLoader.get().onlyOne(RequestExecutor.class);
            Warp.setExecutor(executor);
        }
    }
    
    public void cleanRequestExecutor(@Observes AfterClass event) {
        if (event.getTestClass().isAnnotationPresent(WarpTest.class)) {
            Warp.setExecutor(null);
        }
    }
}
