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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.impl.server.request.AfterRequest;
import org.jboss.arquillian.warp.impl.server.request.BeforeRequest;

/**
 * The observer which watches all {@link Before} and {@link AfterRequest} events and executes the {@link BeforeClass} and
 * {@link AfterClass} events once per {@link ServerAssertion} class on the end of a request.
 *
 * @author Lukas Fryc
 *
 */
public class LifecycleTestClassExecutor {

    private LinkedHashSet<Object> executedAssertions;

    @Inject
    private Event<BeforeClass> beforeClass;

    @Inject
    private Event<AfterClass> afterClass;

    public void beforeRequest(@Observes BeforeRequest event) {
        executedAssertions = new LinkedHashSet<Object>();
    }

    public void beforeTest(@Observes(precedence = 100) EventContext<Before> context) {
        Object assertionObject = context.getEvent().getTestInstance();
        if (!executedAssertions.contains(assertionObject)) {
            executedAssertions.add(assertionObject);
            beforeClass.fire(new BeforeClass(assertionObject.getClass()));
        }
        context.proceed();
    }

    public void afterRequest(@Observes(precedence = 100) EventContext<AfterRequest> context) {
        List<Object> list = new LinkedList<Object>(executedAssertions);
        Collections.reverse(list);

        Iterator<Object> iterator = list.iterator();

        while (iterator.hasNext()) {
            Object testInstance = iterator.next();
            afterClass.fire(new AfterClass(testInstance.getClass()));
        }
    }
}
