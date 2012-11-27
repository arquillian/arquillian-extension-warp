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
package org.jboss.arquillian.warp.impl.server.lifecycle;

import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.impl.server.assertion.AssertionRegistry;
import org.jboss.arquillian.warp.impl.server.test.LifecycleTestDriver;
import org.jboss.arquillian.warp.spi.event.BeforeRequest;
import org.jboss.arquillian.warp.spi.exception.ObjectAlreadyAssociatedException;
import org.jboss.arquillian.warp.spi.exception.ObjectNotAssociatedException;
import org.jboss.arquillian.warp.spi.servlet.event.BeforeServlet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Lukas Fryc
 */
@RunWith(MockitoJUnitRunner.class)
public class TestLifecycleTest extends AbstractLifecycleTestBase {

    @Mock
    ServletRequest request;

    @Mock
    ServletResponse response;

    @Inject
    Instance<AssertionRegistry> registry;

    @Inject
    Instance<LifecycleManagerImpl> lifecycleManager;

    @Inject
    Instance<LifecycleManagerStoreImpl> lifecycleManagerStore;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        super.addExtensions(extensions);
        extensions.add(LifecycleManagerService.class);
        extensions.add(LifecycleTestDriver.class);
    }

    @Test
    public void test() throws ObjectNotAssociatedException, ObjectAlreadyAssociatedException {
        fire(new BeforeRequest(request, response));
        lifecycleManagerStore.get().bind(ServletRequest.class, request);

        TestingAssertion assertion = new TestingAssertion();
        registry.get().registerAssertions(assertion);

        LifecycleManagerImpl lifecycleManager = LifecycleManagerStoreImpl.get(ServletRequest.class, request);
        lifecycleManager.fireLifecycleEvent(new BeforeServlet());

        assertEventFired(BeforeServlet.class, 1);
        assertEventFired(org.jboss.arquillian.test.spi.event.suite.Test.class, 1);
        assertEventFired(Before.class, 1);
        assertEventFired(After.class, 1);
    }

    public static class TestingAssertion extends ServerAssertion {
        private static final long serialVersionUID = 1L;

        @org.jboss.arquillian.warp.servlet.BeforeServlet
        public void assertion() {
        }
    }

}
