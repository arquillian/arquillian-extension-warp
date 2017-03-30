/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.impl.client.eventbus;

import java.lang.reflect.Method;
import java.util.List;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.impl.client.commandBus.CommandBusObserver;
import org.jboss.arquillian.warp.impl.client.commandBus.CommandBusObserver.StartBus;
import org.jboss.arquillian.warp.impl.client.commandBus.CommandBusObserver.StopBus;
import org.junit.Test;

public class TestCommandEventBusLifecycle extends AbstractTestTestBase {

    @Inject
    private Event<Before> before;

    @Inject
    private Event<After> after;


    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(CommandBusObserver.class);
        extensions.add(StartBlockingObserver.class);
    }

    @Test
    public void when_test_is_not_annotated_as_warp_test_then_event_bus_is_not_started() throws Exception {
        // given
        Object testInstance = new Object();
        Method testMethod = Object.class.getMethod("toString");

        // when
        before.fire(new Before(testInstance, testMethod));
        after.fire(new After(testInstance, testMethod));

        // then
        assertEventNotFiredInContext(StartBus.class, SuiteContext.class);
        assertEventNotFiredInContext(StopBus.class, SuiteContext.class);
    }

    @Test
    public void when_test_is_annotated_as_warp_test_then_event_bus_is_started() throws Exception {
        // given
        FakeTest testInstance = new FakeTest();
        Method testMethod = FakeTest.class.getMethod("test");

        // when
        before.fire(new Before(testInstance, testMethod));
        after.fire(new After(testInstance, testMethod));

        // then
        assertEventFired(StartBus.class);
        assertEventFired(StopBus.class);
    }

    @WarpTest
    private static class FakeTest {
        @Test
        public void test() {
        }
    }

    private static class StartBlockingObserver {
        public void blockStart(@Observes EventContext<StartBus> ctx) {
        }

        public void blockStop(@Observes EventContext<StopBus> ctx) {
        }
    }
}
