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
package org.jboss.arquillian.warp.impl.client.eventbus;

import java.lang.reflect.Method;

import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.TestEvent;
import org.jboss.arquillian.warp.WarpTest;

/**
 * <p>
 * Provides an event bus during test execution to listen for incoming {@link Command} events.
 * </p>
 *
 * <p>
 * Event Bus functionality is similar to ServletProtocol
 * </p>
 *
 * @author Aris Tzoumas
 */
public class CommandBusObserver {

    @Inject
    private Event<StartBus> startBus;

    @Inject
    private Event<StopBus> stopBus;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Instance<Injector> injector;

    @Inject
    @TestScoped
    private InstanceProducer<CommandBus> commandBusInst;

    void beforeTest(@Observes(precedence = 500) Before event) {
        Class<?> testClass = event.getTestInstance().getClass();

        if (testClass.isAnnotationPresent(WarpTest.class)) {
            startBus.fire(new StartBus(event));
        }
    }

    void afterTest(@Observes(precedence = -500) After event) {
        Class<?> testClass = event.getTestInstance().getClass();

        if (testClass.isAnnotationPresent(WarpTest.class)) {
            stopBus.fire(new StopBus(event));
        }
    }

    void startEventBus(@Observes StartBus event) throws Exception {
        CommandBus bus = new CommandBus();
        injector.get().inject(bus);
        commandBusInst.set(bus);
        bus.startBus(event);
    }

    /**
     * Stops the Event Bus
     */
    void stopEventBus(@Observes StopBus event) {
        CommandBus bus = commandBusInst.get();
        bus.stopBus();
    }

    public static final class StartBus extends BusEvent {
        public StartBus(TestEvent event) {
            super(event);
        }
    }

    public static final class StopBus extends BusEvent {
        public StopBus(TestEvent event) {
            super(event);
        }
    }

    public abstract static class BusEvent {
        private Object testInstance;
        private Method testMethod;

        public BusEvent(TestEvent event) {
            this.testInstance = event.getTestInstance();
            this.testMethod = event.getTestMethod();
        }

        public Object getTestInstance() {
            return testInstance;
        }

        public Method getTestMethod() {
            return testMethod;
        }
    }

}