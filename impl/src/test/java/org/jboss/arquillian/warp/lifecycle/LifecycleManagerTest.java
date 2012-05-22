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
import static org.junit.Assert.assertNull;

import java.util.List;

import javax.servlet.ServletRequest;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.context.Context;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.jboss.arquillian.warp.server.lifecycle.LifecycleManagerImpl;
import org.jboss.arquillian.warp.server.lifecycle.LifecycleManagerService;
import org.jboss.arquillian.warp.server.request.AfterRequest;
import org.jboss.arquillian.warp.server.request.BeforeRequest;
import org.jboss.arquillian.warp.server.request.RequestContextHandler;
import org.jboss.arquillian.warp.server.request.RequestContextImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Lukas Fryc
 */
@RunWith(MockitoJUnitRunner.class)
public class LifecycleManagerTest extends AbstractManagerTestBase {

    @Mock
    ServletRequest request;

    @Inject
    Instance<LifecycleManagerImpl> lifecycleManager;

    @Inject
    Instance<Injector> injector;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(LifecycleManagerService.class);
        extensions.add(RequestContextHandler.class);
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

        // when
        fire(new BeforeRequest(request));

        // then
        assertNotNull("lifecycle manager should be initialized on BeforeRequest", lifecycleManager.get());
    }

    @Test
    public void lifecycle_manager_should_be_finalized_after_request() {
        // having
        // - lifecycle manager instantiated on before request
        fire(new BeforeRequest(request));

        // when
        fire(new AfterRequest(request));

        // then
        injector.get().inject(this);
        assertNull("lifecycle manager should be finalized on AfterRequest", lifecycleManager.get());
    }
}
