package org.jboss.arquillian.jsfunitng.deployment;

import org.jboss.arquillian.container.test.impl.enricher.resource.URLResourceProvider;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.jsfunitng.proxy.ProxyService;
import org.jboss.arquillian.jsfunitng.proxy.ProxyURLProvider;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class JSFUnitExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        // proxy
        builder.override(ResourceProvider.class, URLResourceProvider.class, ProxyURLProvider.class);
        builder.observer(ProxyService.class);
        
        // page extensions
        builder.service(ApplicationArchiveProcessor.class, PageExtensionArchiveProcessor.class);
        
        // enrichment
        builder.service(ApplicationArchiveProcessor.class, EnrichmentArchiveEnricher.class);
    }

}
