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
package org.jboss.arquillian.warp.impl.server.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.arquillian.warp.impl.server.test.TestResultObserver;
import org.jboss.arquillian.warp.impl.server.testbase.AbstractWarpServerTestTestBase;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.jboss.arquillian.warp.spi.context.RequestScoped;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestTestResultObserver extends AbstractWarpServerTestTestBase {

    private ResponsePayload responsePayload;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        super.addExtensions(extensions);
        extensions.add(TestResultObserver.class);

        extensions.add(ThrowingObserver.class);
        extensions.add(TestResultProvider.class);
    }

    @BeforeEach
    public void setup() {
        // having
        responsePayload = new ResponsePayload(-1L);
        bind(RequestScoped.class, ResponsePayload.class, responsePayload);
    }

    @Test
    public void when_exception_is_thrown_then_exception_is_propagated_as_test_result_to_response_payload() {

        // when
        try {
            fire(new IllegalStateException("some message"));
        } catch (IllegalStateException e) {
            // excepted exception
        }

        // then
        assertNotNull(responsePayload.getTestResult());
        assertEquals(Status.FAILED, responsePayload.getTestResult().getStatus());
        assertTrue(responsePayload.getTestResult().getThrowable() instanceof IllegalStateException);
        assertEquals("some message", responsePayload.getTestResult().getThrowable().getMessage());
    }

    @Test
    public void when_test_result_with_failure_is_observed_then_it_is_propagated_to_response_payload() {

        // having
        TestResult testResult = TestResult.failed(null);

        // when
        fire(testResult);

        // then
        assertEquals(testResult, responsePayload.getTestResult());
    }

    @Test
    public void when_test_result_passed_is_observed_then_it_is_ignored() {
        // having
        TestResult testResult = TestResult.passed();

        // when
        fire(testResult);

        // then
        assertNull(responsePayload.getTestResult());
    }

    @Test
    public void when_two_failed_test_results_are_observed_then_first_one_is_saved_in_response_payload() {
        // having
        TestResult testResult1 = TestResult.failed(null);
        TestResult testResult2 = TestResult.failed(null);

        // when
        fire(testResult1);
        fire(testResult2);

        // then
        assertEquals(testResult1, responsePayload.getTestResult());
    }

    @Test
    public void when_test_result_is_produced_then_it_is_propagated_to_response_payload() {

        // having
        TestResultSetup setup = new TestResultSetup();
        setup.testResult = TestResult.failed(null);

        // when
        fire(setup);

        // then
        assertEquals(setup.testResult, responsePayload.getTestResult());
    }

    @Test
    public void when_observer_fails_then_exception_is_propagated_as_test_result_to_response_payload() {

        // when
        try {
            fire(new ThrowingEvent());
        } catch (IllegalStateException e) {
            // excepted exception
        }

        // then
        assertNotNull(responsePayload.getTestResult());
        assertEquals(Status.FAILED, responsePayload.getTestResult().getStatus());
        assertTrue(responsePayload.getTestResult().getThrowable() instanceof IllegalStateException);
        assertEquals("some message", responsePayload.getTestResult().getThrowable().getMessage());
    }

    class ThrowingEvent {
    }

    static class ThrowingObserver {
        public void observes(@Observes ThrowingEvent event) {
            throw new IllegalStateException("some message");
        }
    }

    class TestResultSetup {
        TestResult testResult;
    }

    static class TestResultProvider {

        @Inject
        @RequestScoped
        private InstanceProducer<TestResult> testResult;

        public void observes(@Observes TestResultSetup event) {
            testResult.set(event.testResult);
        }
    }
}
