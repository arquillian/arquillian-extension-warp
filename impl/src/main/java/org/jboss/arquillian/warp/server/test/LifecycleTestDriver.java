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
package org.jboss.arquillian.warp.server.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.Test;
import org.jboss.arquillian.warp.server.assertion.AssertionRegistry;
import org.jboss.arquillian.warp.spi.LifecycleEvent;

/**
 * Observes {@link LifecycleEvent} events and executed verification methods annotated with
 * {@link LifecycleEvent#getAnnotation()} annotation.
 *
 * See {@link LifecycleTestClassExecutor} which executes {@link BeforeClass} and {@link AfterClass} events.
 *
 * @author Lukas Fryc
 *
 */
public class LifecycleTestDriver {

    @Inject
    private Instance<AssertionRegistry> registry;

    @Inject
    private Event<Before> before;

    @Inject
    private Event<After> after;

    @Inject
    private Event<Test> test;

    public void fireTest(@Observes LifecycleEvent event) {
        final AssertionRegistry registry = getRegistry();

        for (final Object assertionObject : registry.getAssertions()) {
            final Annotation annotation = event.getAnnotation();

            List<Method> methods = SecurityActions.getMethodsWithAnnotation(assertionObject.getClass(), annotation);

            for (final Method testMethod : methods) {
                before.fire(new Before(assertionObject, testMethod));

                test.fire(new Test(new LifecycleMethodExecutor(assertionObject, testMethod)));

                after.fire(new After(assertionObject, testMethod));
            }
        }
    }

    private AssertionRegistry getRegistry() {
        return registry.get();
    }

    private static class LifecycleMethodExecutor implements TestMethodExecutor {

        private Object instance;
        private Method method;

        public LifecycleMethodExecutor(Object instance, Method method) {
            super();
            this.instance = instance;
            this.method = method;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public Object getInstance() {
            return instance;
        }

        @Override
        public void invoke(Object... parameters) throws Throwable {
            method.invoke(instance, parameters);
        }

    }
}
