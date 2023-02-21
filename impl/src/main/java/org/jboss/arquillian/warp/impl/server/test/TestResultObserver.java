/*
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

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

public class TestResultObserver {

    @Inject
    private Instance<ResponsePayload> responsePayload;

    public void propagateTestResult(@Observes TestResult testResult) {
        storeFirstFailure(testResult);
    }

    public void propagateThrowableAsTestResultAndRethrow(@Observes Throwable throwable) throws Throwable {
        throwable.printStackTrace();

        if (responsePayload() != null) {
            storeFirstFailure(TestResult.failed(throwable));
        }

        // throwable must be rethrown, because Arquillian Core checks whether throwable was observed
        // and if yes, it does not let it bubble down the stack
        throw throwable;
    }

    private void storeFirstFailure(TestResult testResult) {
        // setup just a first failure
        if (testResult.getStatus() != Status.PASSED) {
            if (responsePayload().getTestResult() == null) {
                responsePayload().setTestResult(testResult);
            }
        }
    }

    private ResponsePayload responsePayload() {
        return responsePayload.get();
    }
}
