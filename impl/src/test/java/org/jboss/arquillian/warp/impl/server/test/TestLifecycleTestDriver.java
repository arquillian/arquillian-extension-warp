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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.container.test.impl.execution.ContainerTestExecuter;
import org.jboss.arquillian.container.test.impl.execution.LocalTestExecuter;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.impl.server.inspection.InspectionRegistry;
import org.jboss.arquillian.warp.impl.server.testbase.AbstractWarpServerTestTestBase;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.jboss.arquillian.warp.spi.context.RequestScoped;
import org.jboss.arquillian.warp.spi.servlet.event.BeforeServlet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestLifecycleTestDriver extends AbstractWarpServerTestTestBase {

    @Mock
    private InspectionRegistry inspectionRegistry;

    @Spy
    private ResponsePayload responsePayload = new ResponsePayload(-1L);

    @Mock
    private ServiceLoader services;

    @Inject
    private Instance<TestResult> testResult;

    @Mock
    private RuntimeException exception;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(LifecycleTestDriver.class);
        extensions.add(LocalTestExecuter.class);
        extensions.add(ContainerTestExecuter.class);
        extensions.add(ExceptionThrowingSuiteEventObserver.class);
        extensions.add(TestResultObserver.class);
    }

    @Before
    public void setup() {
        // having
        bind(ApplicationScoped.class, ServiceLoader.class, services);
        bind(RequestScoped.class, InspectionRegistry.class, inspectionRegistry);
        bind(RequestScoped.class, ResponsePayload.class, responsePayload);
        when(services.all(TestEnricher.class)).thenReturn(Arrays.<TestEnricher>asList());
    }

    @Test
    public void when_registry_contains_inspection_with_annotated_method__matching_current_lifecycle_event_then_method_is_fired() {

        // having
        TestingInspection inspection = mock(TestingInspection.class);
        when(inspectionRegistry.getInspections()).thenReturn(Arrays.<Inspection>asList(inspection));

        // when
        fire(new BeforeServlet());

        // then
        verify(inspection).test();
        assertEventFired(org.jboss.arquillian.test.spi.event.suite.Before.class, 1);
        assertEventFired(org.jboss.arquillian.test.spi.event.suite.Test.class, 1);
        assertEventFired(org.jboss.arquillian.test.spi.event.suite.After.class, 1);
    }

    @Test
    public void when_registry_contains_two_inspection_then_all_methods_are_executed() {

        // having
        TestingInspection inspection1 = mock(TestingInspection.class);
        TestingInspectionForMultipleInspections inspection2 = mock(TestingInspectionForMultipleInspections.class);
        when(inspectionRegistry.getInspections()).thenReturn(Arrays.<Inspection>asList(inspection1, inspection2));

        // when
        fire(new BeforeServlet());

        // then
        verify(inspection1).test();
        verify(inspection2).test();
        assertEventFired(org.jboss.arquillian.test.spi.event.suite.Before.class, 2);
        assertEventFired(org.jboss.arquillian.test.spi.event.suite.Test.class, 2);
        assertEventFired(org.jboss.arquillian.test.spi.event.suite.After.class, 2);
    }

    @Test
    public void when_registry_contains_inspection_with_multiple_methods_annotated_with_given_lifecycle_event_annotation_then_all_methods_are_executed() {

        // having
        TestingInspectionForMultipleMethods inspection = mock(TestingInspectionForMultipleMethods.class);
        when(inspectionRegistry.getInspections()).thenReturn(Arrays.<Inspection>asList(inspection));

        // when
        fire(new BeforeServlet());

        // then
        verify(inspection).test1();
        verify(inspection).test2();
        assertEventFired(org.jboss.arquillian.test.spi.event.suite.Before.class, 2);
        assertEventFired(org.jboss.arquillian.test.spi.event.suite.Test.class, 2);
        assertEventFired(org.jboss.arquillian.test.spi.event.suite.After.class, 2);
    }

    @Test
    public void when_lifecycle_test_execution_fails_then_test_result_is_filled() {

        // having
        TestingInspection inspection = mock(TestingInspection.class);
        when(inspectionRegistry.getInspections()).thenReturn(Arrays.<Inspection>asList(inspection));
        doThrow(exception).when(inspection).test();

        // when
        fire(new BeforeServlet());

        // then
        TestResult testResult = responsePayload.getTestResult();
        assertNotNull("response payload test result must be set", testResult);

        Throwable throwable = testResult.getThrowable();
        assertNotNull("response payload throwable must be set", throwable);
        assertEquals(exception, throwable);
    }

    @Test
    public void when_before_event_fails_then_request_payload_is_filled_with_exception() {

        // having
        when(inspectionRegistry.getInspections()).thenReturn(Arrays.<Inspection>asList(new TestingInspectionForFailingBeforeTest()));

        // when
        try {
            fire(new BeforeServlet());
            fail();
        } catch (RuntimeException e) {
        }

        // then
        TestResult testResult = responsePayload.getTestResult();
        assertNotNull("response payload test result must be set", testResult);

        Throwable throwable = testResult.getThrowable();
        assertNotNull("response payload throwable must be set", throwable);
        assertEquals("before failed", throwable.getMessage());
    }

    @Test
    public void when_after_event_fails_then_request_payload_is_filled_with_exception() {

        // having
        when(inspectionRegistry.getInspections()).thenReturn(Arrays.<Inspection>asList(new TestingInspectionForFailingAfterTest()));

        // when
        try {
            fire(new BeforeServlet());
        } catch (RuntimeException e) {
        }

        // then
        TestResult testResult = responsePayload.getTestResult();
        assertNotNull("response payload test result must be set", testResult);

        Throwable throwable = testResult.getThrowable();
        assertNotNull("response payload throwable must be set", throwable);
        assertEquals("after failed", throwable.getMessage());
    }

    TestResult testResult() {
        return testResult.get();
    }

    static class TestingInspection extends Inspection {
        private static final long serialVersionUID = -1L;

        @org.jboss.arquillian.warp.servlet.BeforeServlet
        public void test() {
        }
    }

    static class TestingInspectionForMultipleInspections extends Inspection {
        private static final long serialVersionUID = -1L;

        @org.jboss.arquillian.warp.servlet.BeforeServlet
        public void test() {
        }
    }

    static class TestingInspectionForMultipleMethods extends Inspection {
        private static final long serialVersionUID = -1L;

        @org.jboss.arquillian.warp.servlet.BeforeServlet
        public void test1() {
        }

        @org.jboss.arquillian.warp.servlet.BeforeServlet
        public void test2() {
        }
    }

    static class TestingInspectionForFailingBeforeTest extends Inspection {
        private static final long serialVersionUID = -1L;

        @org.jboss.arquillian.warp.servlet.BeforeServlet
        public void test() {
        }
    }

    static class TestingInspectionForFailingAfterTest extends Inspection {
        private static final long serialVersionUID = -1L;

        @org.jboss.arquillian.warp.servlet.BeforeServlet
        public void test() {
        }
    }

    static class ExceptionThrowingSuiteEventObserver {

        public void beforeTest(@Observes org.jboss.arquillian.test.spi.event.suite.Before event) {
            if (event.getTestClass().getJavaClass() == TestingInspectionForFailingBeforeTest.class) {
                throw new RuntimeException("before failed");
            }
        }

        public void afterTest(@Observes org.jboss.arquillian.test.spi.event.suite.After event) {
            if (event.getTestClass().getJavaClass() == TestingInspectionForFailingAfterTest.class) {
                throw new RuntimeException("after failed");
            }
        }
    }
}
