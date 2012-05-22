/**
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.List;

import javax.servlet.ServletRequest;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.warp.server.lifecycle.LifecycleManagerImpl;
import org.jboss.arquillian.warp.server.lifecycle.LifecycleManagerService;
import org.jboss.arquillian.warp.server.lifecycle.LifecycleManagerStoreImpl;
import org.jboss.arquillian.warp.server.request.BeforeRequest;
import org.jboss.arquillian.warp.spi.ObjectAlreadyAssociatedException;
import org.jboss.arquillian.warp.spi.ObjectNotAssociatedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Lukas Fryc
 */
@RunWith(MockitoJUnitRunner.class)
public class ManagerBindingTestCase extends AbstractLifecycleTestBase {

    @Mock
    ServletRequest request;

    @Inject
    Instance<LifecycleManagerStoreImpl> store;

    @Inject
    Instance<LifecycleManagerImpl> lifecycleManager;

    AnotherClass anotherInstance = new AnotherClass();

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        super.addExtensions(extensions);
        extensions.add(LifecycleManagerService.class);
    }

    @Before
    public void initialize() {
        fire(new BeforeRequest(request));
    }

    @Test
    public void test_bind_manager_to_request() throws ObjectAlreadyAssociatedException {
        store.get().bind(ServletRequest.class, request);

        try {
            LifecycleManagerImpl resolvedLifecycleManager = LifecycleManagerStoreImpl.get(ServletRequest.class, request);
            assertNotNull("lifecycle manager should be bound to request", resolvedLifecycleManager);
            assertSame("resolved lifecycle manager should be the one which which is in the context", lifecycleManager.get(),
                    resolvedLifecycleManager);
        } catch (ObjectNotAssociatedException e) {
        } finally {
            try {
                store.get().unbind(ServletRequest.class, request);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Test
    public void test_unbind_manager_from_request() {
        try {
            store.get().bind(ServletRequest.class, request);
        } catch (ObjectAlreadyAssociatedException e) {
            throw new IllegalStateException(e);
        }
        try {
            store.get().unbind(ServletRequest.class, request);

            LifecycleManagerStoreImpl.get(ServletRequest.class, request);
            fail("lifecycle manager should be unbound from request");
        } catch (ObjectNotAssociatedException e) {
            // expected exception
        } finally {
            try {
                store.get().unbind(ServletRequest.class, request);
            } catch (ObjectNotAssociatedException e) {
                // that is okay, we are cleaning store
            }
        }
    }

    @Test
    public void test_bind_manager_to_request_and_another_class() throws ObjectAlreadyAssociatedException {
        store.get().bind(ServletRequest.class, request);
        store.get().bind(AnotherClass.class, anotherInstance);

        try {
            // verify lifecycle manager for request
            LifecycleManagerImpl resolvedLifecycleManager = LifecycleManagerStoreImpl.get(ServletRequest.class, request);
            assertNotNull("lifecycle manager should be bound to request", resolvedLifecycleManager);
            assertSame("resolved lifecycle manager should be the one which which is in the context", lifecycleManager.get(),
                    resolvedLifecycleManager);

            // verify lifecycle manager for another class
            resolvedLifecycleManager = LifecycleManagerStoreImpl.get(AnotherClass.class, anotherInstance);
            assertNotNull("lifecycle manager should be bound to another instance", resolvedLifecycleManager);
            assertSame("resolved lifecycle manager should be the one which which is in the context", lifecycleManager.get(),
                    resolvedLifecycleManager);

        } catch (ObjectNotAssociatedException e) {
        } finally {
            try {
                store.get().unbind(ServletRequest.class, request);
                store.get().unbind(AnotherClass.class, anotherInstance);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Test
    public void test_unbind_manager_from_request_and_another_class() throws ObjectNotAssociatedException {
        try {
            store.get().bind(ServletRequest.class, request);
            store.get().bind(AnotherClass.class, anotherInstance);
        } catch (ObjectAlreadyAssociatedException e) {
            throw new IllegalStateException(e);
        }
        try {
            store.get().unbind(ServletRequest.class, request);
            store.get().unbind(AnotherClass.class, anotherInstance);

            try {
                LifecycleManagerStoreImpl.get(ServletRequest.class, request);
                fail("lifecycle manager should be unbound from request");
            } catch (ObjectNotAssociatedException e) {
                // expected condition
            }
            try {
                LifecycleManagerStoreImpl.get(AnotherClass.class, anotherInstance);
                fail("lifecycle manager should be unbound from request");
            } catch (ObjectNotAssociatedException e) {
                // expected condition

            }
        } finally {
            try {
                store.get().unbind(ServletRequest.class, request);
                store.get().unbind(AnotherClass.class, anotherInstance);
            } catch (ObjectNotAssociatedException e) {
                // that is okay, we are cleaning store
            }
        }
    }

    public static class AnotherClass {
    }
}
