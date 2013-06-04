package org.jboss.arquillian.warp.impl.server.inspection;

import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStarted;

public class InspectionRegistrator {

    @Inject
    @ApplicationScoped
    private InstanceProducer<PayloadRegistry> registry;

    public void initializePayloadRegistry(@Observes ManagerStarted event) {
        registry.set(new PayloadRegistry());
    }
}
