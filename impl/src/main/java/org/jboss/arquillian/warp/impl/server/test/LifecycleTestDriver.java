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
package org.jboss.arquillian.warp.impl.server.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.Test;
import org.jboss.arquillian.warp.impl.server.inspection.InspectionRegistry;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.arquillian.warp.spi.WarpLifecycleEvent;
import org.jboss.arquillian.warp.spi.WarpLifecycleTest;

/**
 * Observes {@link WarpLifecycleEvent} events and executed verification methods annotated with
 * {@link WarpLifecycleEvent#getQualifiers()} annotation.
 *
 * See {@link LifecycleTestClassExecutor} which executes {@link BeforeClass} and {@link AfterClass} events.
 *
 * @author Lukas Fryc
 *
 */
public class LifecycleTestDriver {

    @Inject
    private Instance<InspectionRegistry> registry;

    @Inject
    private Event<Before> before;

    @Inject
    private Event<After> after;

    @Inject
    private Event<Test> test;

    public void fireTest(@Observes WarpLifecycleEvent event) {

        for (final Object inspection : registry().getInspections()) {
            final List<Annotation> qualifiers = event.getQualifiers();

            if (qualifiers == null || qualifiers.size() == 0 || !isWarpLifecycleEvent(qualifiers)) {
                throw new IllegalStateException("Warp lifecycle event must contain annotation marked with @"
                        + WarpLifecycleTest.class.getSimpleName());
            }

            List<Method> methods = SecurityActions.getMethodsMatchingAllQualifiers(inspection.getClass(), qualifiers);

            for (final Method testMethod : methods) {
                executeTest(inspection, testMethod, qualifiers);
            }
        }
    }

    private boolean isWarpLifecycleEvent(List<Annotation> qualifiers) {
        for (Annotation qualifier : qualifiers) {
            if (!WarpCommons.isWarpLifecycleTest(qualifier.annotationType())) {
                return false;
            }
        }

        return true;
    }

    private void executeTest(Object inspection, Method method, List<Annotation> qualifiers) {
        before.fire(new Before(inspection, method));

        test.fire(new Test(new LifecycleMethodExecutor(inspection, method, qualifiers)));

        after.fire(new After(inspection, method));
    }

    private InspectionRegistry registry() {
        return registry.get();
    }

}
