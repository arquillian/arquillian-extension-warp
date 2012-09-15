package org.jboss.arquillian.warp.impl.client.execution;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.warp.impl.client.event.AdvertiseEnrichment;
import org.jboss.arquillian.warp.impl.client.event.AwaitResponse;
import org.jboss.arquillian.warp.impl.client.event.CleanEnrichment;
import org.jboss.arquillian.warp.impl.client.event.FinishEnrichment;
import org.jboss.arquillian.warp.impl.client.event.InstallEnrichment;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionScoped;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

public class RequestExecutionObserver {

    @Inject
    private Instance<ServiceLoader> services;

    @Inject
    private Instance<RequestEnrichment> requestEnrichment;

    @Inject
    @WarpExecutionScoped
    private InstanceProducer<ResponsePayload> responsePayload;

    public void advertiseEnrichment(@Observes AdvertiseEnrichment event) {
        assertionSynchronizer().advertise();
    }

    public void installEnrichment(@Observes InstallEnrichment enrichment) {
        assertionSynchronizer().addEnrichment(requestEnrichment.get());
    }

    public void finishEnrichment(@Observes FinishEnrichment event) {
        assertionSynchronizer().finish();
    }

    public void awaitResponse(@Observes AwaitResponse event) {
        ResponsePayload payload = assertionSynchronizer().waitForResponse();
        responsePayload.set(payload);
    }

    public void cleanEnrichment(@Observes CleanEnrichment event) {
        assertionSynchronizer().clean();
    }

    private AssertionSynchronizer assertionSynchronizer() {
        return services.get().onlyOne(AssertionSynchronizer.class);
    }
}
