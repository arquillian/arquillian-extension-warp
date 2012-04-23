package org.jboss.arquillian.jsfunitng.enrichment;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

public class EnrichmentService {

    public EnrichmentService() {
        System.out.println("created");
    }
    
    public void afterDeployment(@Observes BeforeSuite afterDeploy) {
        System.out.println("event");
    }
}
