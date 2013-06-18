package org.arquillian.warp.ftest.configuration;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.api.annotation.Default;

public class IntegrationTestConfiguratorObserver {

    @Inject
    @ApplicationScoped
    private InstanceProducer<IntegrationTestConfiguration> configuration;

    public void configure(@Observes ArquillianDescriptor descriptor) {
        IntegrationTestConfiguration c = new IntegrationTestConfiguration();
        c.configure(descriptor, Default.class).validate();
        configuration.set(c);
    }
}