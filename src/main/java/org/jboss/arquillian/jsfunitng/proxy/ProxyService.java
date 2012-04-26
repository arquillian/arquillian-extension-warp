package org.jboss.arquillian.jsfunitng.proxy;

import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

public class ProxyService {

    @Inject
    @SuiteScoped
    private InstanceProducer<ProxyDetails> proxyDetails;

    @Inject
    @SuiteScoped
    private InstanceProducer<ProxyHolder> proxyStore;

    public void initializeProxies(@Observes BeforeSuite event) {
        proxyDetails.set(new ProxyDetails());
        proxyStore.set(new ProxyHolder());
    }
    
    public void finalizeProxies(@Observes AfterSuite event) {
        proxyStore.get().freeAllProxies();
    }

}
