package org.jboss.arquillian.jsfunitng.lifecycle;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStarted;
import org.jboss.arquillian.jsfunitng.assertion.AssertionRegistry;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManagerStore.ObjectAlreadyAssociatedException;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManagerStore.ObjectNotAssociatedException;
import org.jboss.arquillian.jsfunitng.lifecycle.LifecycleManagerStore.StoreHasAssociatedObjectsException;
import org.jboss.arquillian.jsfunitng.request.AfterRequest;
import org.jboss.arquillian.jsfunitng.request.BeforeRequest;
import org.jboss.arquillian.jsfunitng.request.RequestScoped;
import org.jboss.arquillian.jsfunitng.test.LifecycleTestDriver;

public class LifecycleManagerService {

    @Inject
    @ApplicationScoped
    private InstanceProducer<LifecycleManagerStore> store;

    @Inject
    @RequestScoped
    private InstanceProducer<LifecycleManager> manager;

    @Inject
    @RequestScoped
    private InstanceProducer<AssertionRegistry> registry;

    @Inject
    private Instance<Injector> injector;

    public void initializeStore(@Observes ManagerStarted event) {
        store.set(inject(new LifecycleManagerStore()));
    }

    private <T> T inject(T object) {
        return injector.get().inject(object);
    }

    public void initializeManagerAndDriver(@Observes BeforeRequest event) {
        manager.set(inject(new LifecycleManager()));
        registry.set(inject(new AssertionRegistry()));
    }

    public void finalizeManager(@Observes AfterRequest event) {
        try {
            getStore().verifyManagerUnbound(getManager());
        } catch (StoreHasAssociatedObjectsException e) {
            throw new IllegalStateException(e);
        }
    }

    public <T> void bindManager(@Observes BindLifecycleManager event) {
        try {
            getStore().bind(event.getDeterminator(), event.getBoundObject());
        } catch (ObjectAlreadyAssociatedException e) {
            throw new IllegalStateException(e);
        }
    }

    public <T> void unbindManager(@Observes UnbindLifecycleManager event) {
        try {
            getStore().unbind(event.getDeterminator(), event.getBoundObject());
        } catch (ObjectNotAssociatedException e) {
            throw new IllegalStateException(e);
        }
    }

    private LifecycleManagerStore getStore() {
        return store.get();
    }

    private LifecycleManager getManager() {
        return manager.get();
    }
}
