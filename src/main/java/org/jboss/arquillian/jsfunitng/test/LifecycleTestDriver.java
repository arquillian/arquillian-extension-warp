package org.jboss.arquillian.jsfunitng.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.jsfunitng.assertion.AssertionRegistry;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.Test;

public class LifecycleTestDriver {

    @Inject
    private Instance<AssertionRegistry> registry;

    @Inject
    private Event<Test> test;

    @Inject
    private Event<Before> before;

    @Inject
    private Event<After> after;

    @Inject
    private Event<BeforeClass> beforeClass;

    @Inject
    private Event<AfterClass> afterClass;

    public void fireTest(@Observes LifecycleEvent event) {
        final AssertionRegistry registry = getRegistry();

        for (final Object assertionObject : registry.getAssertions()) {
            final Class<? extends Annotation> annotationType = event.getAnnotation();

            List<Method> methods = SecurityActions.getMethodsWithAnnotation(assertionObject.getClass(), annotationType);

            for (final Method testMethod : methods) {
                before.fire(new Before(assertionObject, testMethod));
                test.fire(new Test(new TestMethodExecutor() {

                    @Override
                    public void invoke(Object... parameters) throws Throwable {
                        getMethod().invoke(getInstance(), parameters);
                    }

                    @Override
                    public Method getMethod() {
                        return testMethod;
                    }

                    @Override
                    public Object getInstance() {
                        return assertionObject;
                    }
                }));
                after.fire(new After(assertionObject, testMethod));
            }
        }
    }

    private AssertionRegistry getRegistry() {
        return registry.get();
    }
}
