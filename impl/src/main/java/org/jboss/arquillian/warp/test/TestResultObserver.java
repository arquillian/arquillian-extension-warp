package org.jboss.arquillian.warp.test;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.event.suite.Test;

public class TestResultObserver {

    @Inject
    Instance<TestResult> testResult;

    @Inject
    Instance<TestResultStore> testResultStore;

    public void destroyRequestContext(@Observes(precedence = 50) EventContext<Test> context) {
        context.proceed();
        TestResult result = testResult.get();
        testResultStore.get().pushResult(result);
    }
}
