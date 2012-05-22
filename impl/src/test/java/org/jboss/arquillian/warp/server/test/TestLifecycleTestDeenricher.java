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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * @author Lukas Fryc
 */
@RunWith(MockitoJUnitRunner.class)
public class TestLifecycleTestDeenricher {

    @Mock
    EventContext<Before> beforeContext;

    @Mock
    EventContext<After> afterContext;

    @Mock
    Before beforeEvent;

    @Mock
    After afterEvent;

    TestInstance testInstance;

    @org.junit.Before
    public void setupMocks() {
        testInstance = new TestInstance();
        when(beforeContext.getEvent()).thenReturn(beforeEvent);
        when(afterContext.getEvent()).thenReturn(afterEvent);
        when(beforeEvent.getTestInstance()).thenReturn(testInstance);
        when(afterEvent.getTestInstance()).thenReturn(testInstance);
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
        LifecycleTestDeenricher deenricher = new LifecycleTestDeenricher();
        assertEquals(testInstance.integer, 1);
        assertNull(testInstance.object);

        // when
        deenricher.beforeTest(beforeContext);

        // then
        assertEquals(testInstance.integer, 2);
        assertNotNull(testInstance.object);
    }

    @Test
    public void testDeenrichment() {

        // given
        LifecycleTestDeenricher deenricher = new LifecycleTestDeenricher();
        assertEquals(testInstance.integer, 1);
        assertNull(testInstance.object);

        // when
        deenricher.beforeTest(beforeContext);
        deenricher.afterTest(afterContext);

        // then
        assertEquals(testInstance.integer, 1);
        assertNull(testInstance.object);
    }

    private void enrich() {
        testInstance.integer = 2;
        testInstance.object = new Object();
    }

    public static class TestInstance implements Serializable {

        private static final long serialVersionUID = 1L;

        public int integer = 1;
        public Object object;
    }
}
