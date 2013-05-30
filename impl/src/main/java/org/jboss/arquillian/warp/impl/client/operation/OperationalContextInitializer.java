package org.jboss.arquillian.warp.impl.client.operation;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStarted;

public class OperationalContextInitializer {

    @Inject
    @ApplicationScoped
    private InstanceProducer<OperationalContexts> contextsInst;

    @Inject
    private Instance<Injector> injector;

    public void initializeOperationalContexts(@Observes ManagerStarted event) {
        OperationalContexts contexts = new OperationalContexts();

        injector.get().inject(contexts);

        contextsInst.set(contexts);
    }
}
