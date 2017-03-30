/*
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
package org.jboss.arquillian.warp.impl.server.lifecycle;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.warp.spi.LifecycleManager;
import org.jboss.arquillian.warp.spi.LifecycleManagerStore;
import org.jboss.arquillian.warp.spi.event.BeforeRequest;
import org.jboss.arquillian.warp.spi.exception.ObjectAlreadyAssociatedException;
import org.jboss.arquillian.warp.spi.exception.ObjectNotAssociatedException;
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
    private ServletRequest request;

    @Mock
    private ServletResponse response;

    @Inject
    private Instance<LifecycleManager> lifecycleManager;

    private AnotherClass anotherInstance = new AnotherClass();

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        super.addExtensions(extensions);
        extensions.add(LifecycleManagerObserver.class);
    }

    @Before
    public void initialize() {
        fire(new BeforeRequest(request, response));
    }

    @Test
    public void test_bind_manager_to_request() throws ObjectAlreadyAssociatedException {
        lifecycleManager.get().bindTo(ServletRequest.class, request);

        try {
            LifecycleManager resolvedLifecycleManager = LifecycleManagerStore.get(ServletRequest.class, request);
            assertNotNull("lifecycle manager should be bound to request", resolvedLifecycleManager);
            assertSame("resolved lifecycle manager should be the one which which is in the context",
                lifecycleManager.get(),
                resolvedLifecycleManager);
        } catch (ObjectNotAssociatedException e) {
        } finally {
            try {
                lifecycleManager.get().unbindFrom(ServletRequest.class, request);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Test
    public void test_unbind_manager_from_request() {
        try {
            lifecycleManager.get().bindTo(ServletRequest.class, request);
        } catch (ObjectAlreadyAssociatedException e) {
            throw new IllegalStateException(e);
        }
        try {
            lifecycleManager.get().unbindFrom(ServletRequest.class, request);

            LifecycleManagerStoreImpl.get(ServletRequest.class, request);
            fail("lifecycle manager should be unbound from request");
        } catch (ObjectNotAssociatedException e) {
            // expected exception
        } finally {
            try {
                lifecycleManager.get().unbindFrom(ServletRequest.class, request);
            } catch (ObjectNotAssociatedException e) {
                // that is okay, we are cleaning store
            }
        }
    }

    @Test
    public void test_bind_manager_to_request_and_another_class() throws ObjectAlreadyAssociatedException {
        lifecycleManager.get().bindTo(ServletRequest.class, request);
        lifecycleManager.get().bindTo(AnotherClass.class, anotherInstance);

        try {
            // verify lifecycle manager for request
            LifecycleManager resolvedLifecycleManager = LifecycleManagerStore.get(ServletRequest.class, request);
            assertNotNull("lifecycle manager should be bound to request", resolvedLifecycleManager);
            assertSame("resolved lifecycle manager should be the one which which is in the context",
                lifecycleManager.get(),
                resolvedLifecycleManager);

            // verify lifecycle manager for another class
            resolvedLifecycleManager = LifecycleManagerStore.get(AnotherClass.class, anotherInstance);
            assertNotNull("lifecycle manager should be bound to another instance", resolvedLifecycleManager);
            assertSame("resolved lifecycle manager should be the one which which is in the context",
                lifecycleManager.get(),
                resolvedLifecycleManager);
        } catch (ObjectNotAssociatedException e) {
        } finally {
            try {
                lifecycleManager.get().unbindFrom(ServletRequest.class, request);
                lifecycleManager.get().unbindFrom(AnotherClass.class, anotherInstance);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Test
    public void test_unbind_manager_from_request_and_another_class() throws ObjectNotAssociatedException {
        try {
            lifecycleManager.get().bindTo(ServletRequest.class, request);
            lifecycleManager.get().bindTo(AnotherClass.class, anotherInstance);
        } catch (ObjectAlreadyAssociatedException e) {
            throw new IllegalStateException(e);
        }
        try {
            lifecycleManager.get().unbindFrom(ServletRequest.class, request);
            lifecycleManager.get().unbindFrom(AnotherClass.class, anotherInstance);

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
                lifecycleManager.get().unbindFrom(ServletRequest.class, request);
                lifecycleManager.get().unbindFrom(AnotherClass.class, anotherInstance);
            } catch (ObjectNotAssociatedException e) {
                // that is okay, we are cleaning store
            }
        }
    }

    public static class AnotherClass {
    }
}
