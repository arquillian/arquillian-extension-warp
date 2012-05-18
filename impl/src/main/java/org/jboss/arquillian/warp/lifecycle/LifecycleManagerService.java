/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.warp.lifecycle;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStarted;
import org.jboss.arquillian.warp.assertion.AssertionRegistry;
import org.jboss.arquillian.warp.request.AfterRequest;
import org.jboss.arquillian.warp.request.BeforeRequest;
import org.jboss.arquillian.warp.request.RequestScoped;
import org.jboss.arquillian.warp.test.TestResultStore;

/**
 * Drives {@link LifecycleManagerImpl} and {@link AssertionRegistry} lifecycle.
 * 
 * @author Lukas Fryc
 * 
 */
public class LifecycleManagerService {

    @Inject
    @ApplicationScoped
    private InstanceProducer<LifecycleManagerStoreImpl> store;

    @Inject
    @RequestScoped
    private InstanceProducer<LifecycleManagerImpl> manager;

    @Inject
    @RequestScoped
    private InstanceProducer<AssertionRegistry> registry;

    @Inject
    @RequestScoped
    private InstanceProducer<TestResultStore> testResultStore;

    @Inject
    private Instance<Injector> injector;

    public void initializeStore(@Observes ManagerStarted event) {
        store.set(inject(new LifecycleManagerStoreImpl()));
    }

    private <T> T inject(T object) {
        return injector.get().inject(object);
    }

    public void initializeManagerAndDriver(@Observes BeforeRequest event) {
        manager.set(inject(new LifecycleManagerImpl()));
        registry.set(inject(new AssertionRegistry()));
        testResultStore.set(inject(new TestResultStore()));
    }

    public void finalizeManager(@Observes AfterRequest event) {
        try {
            getStore().verifyManagerUnbound();
        } catch (StoreHasAssociatedObjectsException e) {
            throw new IllegalStateException(e);
        }
    }

    private LifecycleManagerStoreImpl getStore() {
        return store.get();
    }
}
