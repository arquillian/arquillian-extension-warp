package org.jboss.arquillian.jsfunitng.enrichment;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManagerService;
import org.jboss.arquillian.jsfunitng.request.RequestContextHandler;
import org.jboss.arquillian.jsfunitng.request.RequestContextImpl;
import org.jboss.arquillian.jsfunitng.test.LifecycleTestClassExecutor;
import org.jboss.arquillian.jsfunitng.test.LifecycleTestDeenricher;
import org.jboss.arquillian.jsfunitng.test.LifecycleTestDriver;

public class EnrichmentExtension implements RemoteLoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.context(RequestContextImpl.class);
        
        builder.observer(RequestContextHandler.class);
        builder.observer(LifecycleManagerService.class);
        
        builder.observer(LifecycleTestDriver.class);
        builder.observer(LifecycleTestClassExecutor.class);
        builder.observer(LifecycleTestDeenricher.class);
    }

}
