package org.jboss.arquillian.jsfunitng.test;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.jsfunitng.request.AfterRequest;
import org.jboss.arquillian.jsfunitng.request.BeforeRequest;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

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
        Deque<Object> deque = new LinkedList<Object>(executedAssertions);
        Iterator<Object> iterator = deque.descendingIterator();

        
        while (iterator.hasNext()) {
            Object testInstance = iterator.next();
            afterClass.fire(new AfterClass(testInstance.getClass()));
        }
    }
}
