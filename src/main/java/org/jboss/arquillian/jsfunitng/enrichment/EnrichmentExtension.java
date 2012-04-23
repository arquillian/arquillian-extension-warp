package org.jboss.arquillian.jsfunitng.enrichment;

import org.jboss.arquillian.container.test.impl.ClientTestInstanceEnricher;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.test.impl.TestContextHandler;
import org.jboss.arquillian.test.impl.context.ClassContextImpl;
import org.jboss.arquillian.test.impl.context.SuiteContextImpl;
import org.jboss.arquillian.test.impl.context.TestContextImpl;

public class EnrichmentExtension implements RemoteLoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(EnrichmentService.class);
    }

}
