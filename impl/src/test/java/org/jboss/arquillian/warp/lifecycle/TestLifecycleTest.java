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

import java.util.List;

import javax.servlet.ServletRequest;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.warp.assertion.AssertionRegistry;
import org.jboss.arquillian.warp.request.BeforeRequest;
import org.jboss.arquillian.warp.spi.ObjectNotAssociatedException;
import org.jboss.arquillian.warp.test.BeforeServlet;
import org.jboss.arquillian.warp.test.BeforeServletEvent;
import org.jboss.arquillian.warp.test.LifecycleTestDriver;
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

    @Inject
    Instance<AssertionRegistry> registry;

    @Inject
    Instance<LifecycleManagerImpl> lifecycleManager;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        super.addExtensions(extensions);
        extensions.add(LifecycleManagerService.class);
        extensions.add(LifecycleTestDriver.class);
    }

    @Test
    public void test() throws ObjectNotAssociatedException {
        fire(new BeforeRequest(request));
        fire(new BindLifecycleManager<ServletRequest>(ServletRequest.class, request));

        TestingAssertion assertion = new TestingAssertion();
        registry.get().registerAssertion(assertion);

        LifecycleManagerImpl lifecycleManager = LifecycleManagerStoreImpl.get(ServletRequest.class, request);
        lifecycleManager.fireLifecycleEvent(new BeforeServletEvent());

        assertEventFired(BeforeServletEvent.class, 1);
        assertEventFired(org.jboss.arquillian.test.spi.event.suite.Test.class, 1);
        assertEventFired(Before.class, 1);
        assertEventFired(After.class, 1);
    }

    public static class TestingAssertion {

        @BeforeServlet
        public void assertion() {
        }
    }

}
