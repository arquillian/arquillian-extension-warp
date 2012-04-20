package org.jboss.arquillian.jsfunitng.deployment;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class JSFUnitExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        // page extensions
        builder.service(ApplicationArchiveProcessor.class, PageExtensionArchiveProcessor.class);
        
        // enrichment
        builder.service(ApplicationArchiveProcessor.class, EnrichmentArchiveEnricher.class);
        builder.service(AuxiliaryArchiveAppender.class, EnrichmentArchiveEnricher.class);
    }

}
