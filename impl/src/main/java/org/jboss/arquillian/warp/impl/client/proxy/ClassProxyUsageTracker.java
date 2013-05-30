package org.jboss.arquillian.warp.impl.client.proxy;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStarted;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.warp.impl.client.event.RequireProxy;
import org.jboss.arquillian.warp.impl.client.operation.OperationalContext;
import org.jboss.arquillian.warp.impl.client.operation.OperationalContexts;

public class ClassProxyUsageTracker {

    @Inject
    private Instance<TestClass> testClass;

    @Inject
    @ApplicationScoped
    private InstanceProducer<URLToContextMapping> mapping;

    @Inject
    private Instance<OperationalContexts> contexts;

    public void initializeMapping(@Observes ManagerStarted event) {
        mapping.set(new URLToContextMapping());
    }

    public void registerOperationalContextToUrl(@Observes RequireProxy requireProxy) {
        OperationalContext context = contexts.get().test();
        mapping.get().register(requireProxy.getProxyUrl(), testClass.get().getJavaClass(), context);
    }

    public void unregisterOperationalContext(@Observes After afterTest) {
        mapping.get().unregister(testClass.get().getJavaClass());
    }
}
