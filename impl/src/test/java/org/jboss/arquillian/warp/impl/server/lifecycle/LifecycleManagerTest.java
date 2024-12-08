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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.context.Context;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.jboss.arquillian.warp.impl.server.request.RequestContextHandler;
import org.jboss.arquillian.warp.impl.server.request.RequestContextImpl;
import org.jboss.arquillian.warp.spi.LifecycleManager;
import org.jboss.arquillian.warp.spi.servlet.event.ProcessHttpRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Lukas Fryc
 */
@ExtendWith(MockitoExtension.class)
public class LifecycleManagerTest extends AbstractManagerTestBase {

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain filterChain;

    @Inject
    Instance<LifecycleManager> lifecycleManager;

    @Inject
    Instance<Injector> injector;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(LifecycleManagerObserver.class);
        extensions.add(RequestContextHandler.class);
        extensions.add(VerifyLifecycleManager.class);
    }

    @Override
    protected void addContexts(List<Class<? extends Context>> contexts) {
        super.addContexts(contexts);
        contexts.add(RequestContextImpl.class);
    }

    @Test
    public void lifecycle_manager_should_be_initialized_before_request() {
        // having
        assertNull(lifecycleManager.get());
        VerifyLifecycleManager.invoked = false;

        // when
        fire(new ProcessHttpRequest(request, response, filterChain));

        // then
        assertTrue(VerifyLifecycleManager.invoked);
        injector.get().inject(this);
        assertNull(lifecycleManager.get());
    }

    @Test
    public void lifecycle_manager_should_be_finalized_after_request() {
        // having
        assertNull(lifecycleManager.get());
        VerifyLifecycleManager.invoked = false;

        // when
        fire(new ProcessHttpRequest(request, response, filterChain));

        // then
        assertTrue(VerifyLifecycleManager.invoked);
        injector.get().inject(this);
        assertNull(lifecycleManager.get(), "lifecycle manager should be finalized after ProcessHttpRequest");
    }

    public static class VerifyLifecycleManager {

        private static boolean invoked = false;

        @Inject
        Instance<LifecycleManager> lifecycleManager;

        public void observeProcessHttpRequest(@Observes ProcessHttpRequest processHttpRequest) {
            invoked = true;
            assertNotNull(lifecycleManager.get(), "lifecycle manager should be initialized by ProcessHttpRequest event");
        }
    }
}
