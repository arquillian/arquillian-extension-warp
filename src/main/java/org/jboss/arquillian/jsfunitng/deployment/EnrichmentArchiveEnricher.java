package org.jboss.arquillian.jsfunitng.deployment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.jsfunitng.enrichment.EnrichmentExtension;
import org.jboss.arquillian.jsfunitng.filter.EnrichmentFilter;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class EnrichmentArchiveEnricher implements ApplicationArchiveProcessor, AuxiliaryArchiveAppender {

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (applicationArchive instanceof WebArchive) {
            WebArchive webArchive = (WebArchive) applicationArchive;

            webArchive.addPackage(EnrichmentFilter.class.getPackage());

            // webArchive.addPackage(EnrichmentExtension.class.getPackage());
            // webArchive.addAsServiceProvider(RemoteLoadableExtension.class, EnrichmentExtension.class);

            // add auxiliary archives
            List<Archive<?>> auxiliarryArchives = new ArrayList<Archive<?>>();
            Collection<AuxiliaryArchiveAppender> archiveAppenders = serviceLoader.get().all(AuxiliaryArchiveAppender.class);
            for (AuxiliaryArchiveAppender archiveAppender : archiveAppenders) {
                auxiliarryArchives.add(archiveAppender.createAuxiliaryArchive());
            }
            webArchive.addAsLibraries(auxiliarryArchives);
        } else {
            // TODO user logger
            throw new IllegalStateException("applicationAchieve must be WebArchive");
        }
    }

    @Override
    public Archive<?> createAuxiliaryArchive() {
        // throw new UnsupportedOperationException();
        return ShrinkWrap.create(JavaArchive.class, "jsfunitng-enrichment.jar")
                .addPackage(EnrichmentExtension.class.getPackage())
                .addAsServiceProvider(RemoteLoadableExtension.class, EnrichmentExtension.class);
    }

}
