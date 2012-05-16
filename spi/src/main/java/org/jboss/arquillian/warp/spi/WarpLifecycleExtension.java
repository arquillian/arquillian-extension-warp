package org.jboss.arquillian.warp.spi;

import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public interface WarpLifecycleExtension {

    /**
     * Adds the library to enrich final {@link WebArchive} deployment.
     * 
     * Can return null when no enrichment library is provided by extension.
     */
    JavaArchive getEnrichmentLibrary();

    /**
     * Provides final {@link WebArchive} to be enriched (any resource can be manipulated).
     * 
     * Doesn't have to process any manipulation.
     */
    void enrichWebArchive(WebArchive webArchive);
}
