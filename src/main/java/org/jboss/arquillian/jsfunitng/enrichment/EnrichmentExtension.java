package org.jboss.arquillian.jsfunitng.enrichment;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;

public class EnrichmentExtension implements RemoteLoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(EnrichmentService.class);
    }

}
