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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

/**
 * @author Lukas Fryc
 */
@ExtendWith(MockitoExtension.class)
public class TestLifecycleTestEnrichmentWatcher {

    @Mock
    EventContext<Before> beforeContext;

    @Mock
    EventContext<After> afterContext;

    @Mock
    Before beforeEvent;

    @Mock
    After afterEvent;

    TestInstance testInstance;

    @BeforeEach
    public void setupMocks() {
        testInstance = new TestInstance();
        when(beforeContext.getEvent()).thenReturn(beforeEvent);
        // Mockito for JUnit5 requires more "lenient" calls, as the MockitoExtension seems to validate the stubbings created in "@BeforeEach" methods for each test,
        // but not all test methods call all stubbed methods.
        lenient().when(afterContext.getEvent()).thenReturn(afterEvent);
        when(beforeEvent.getTestInstance()).thenReturn(testInstance);
        lenient().when(afterEvent.getTestInstance()).thenReturn(testInstance);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                enrich();
                return null;
            }
        }).when(beforeContext).proceed();
    }

    @Test
    public void testEnrichment() {

        // given
        LifecycleTestEnrichmentWatcher deenricher = new LifecycleTestEnrichmentWatcher();
        assertEquals(1, testInstance.injectedInteger);
        assertNull(testInstance.injectedObject);

        // when
        deenricher.rememberFieldValues(beforeContext);

        // then
        assertEquals(2, testInstance.injectedInteger);
        assertNotNull(testInstance.injectedObject);
    }

    @Test
    public void testDeenrichment() {

        // given
        LifecycleTestEnrichmentWatcher deenricher = new LifecycleTestEnrichmentWatcher();
        assertEquals(1, testInstance.injectedInteger);
        assertNull(testInstance.injectedObject);

        // when
        deenricher.rememberFieldValues(beforeContext);
        deenricher.restoreOriginalFieldValues(afterContext);

        // then
        assertEquals(1, testInstance.injectedInteger);
        assertNull(testInstance.injectedObject);
    }

    @Test
    public void testValueChanged() {

        // given
        LifecycleTestEnrichmentWatcher deenricher = new LifecycleTestEnrichmentWatcher();
        assertEquals(false, testInstance.bool);
        assertEquals(1, testInstance.injectedInteger);
        assertNull(testInstance.injectedObject);

        // when
        deenricher.rememberFieldValues(beforeContext);
        testInstance.bool = true;
        testInstance.object = "string";
        testInstance.injectedInteger = 4;
        testInstance.injectedObject = "string";
        deenricher.restoreOriginalFieldValues(afterContext);

        // then
        assertEquals(true, testInstance.bool);
        assertEquals("string", testInstance.object);
        assertEquals(1, testInstance.injectedInteger);
        assertNull(testInstance.injectedObject);
    }

    private void enrich() {
        testInstance.injectedInteger = 2;
        testInstance.injectedObject = new Object();
    }

    public static class TestInstance implements Serializable {

        private static final long serialVersionUID = 1L;

        // not enriched state of the object (in the control of user)
        public boolean bool = false;
        public Object object;

        // values which will be enriched
        public int injectedInteger = 1;
        public Object injectedObject;
    }
}
