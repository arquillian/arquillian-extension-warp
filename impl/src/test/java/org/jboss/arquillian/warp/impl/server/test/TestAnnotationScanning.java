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

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.arquillian.warp.impl.server.test.SecurityActions;
import org.junit.Test;

public class TestAnnotationScanning {

    @Test
    public void test() {
        TestAnnotation annotation = new TestAnnotation() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return TestAnnotation.class;
            }

            @Override
            public int value() {
                return 2;
            }
        };

        List<Method> methods = SecurityActions.getMethodsWithAnnotation(TestingClass.class, annotation);

        assertEquals(1, methods.size());

        assertEquals("testMethod2", methods.get(0).getName());

    }

    public static class TestingClass {

        @TestAnnotation(1)
        public void testMethod1() {

        }

        @TestAnnotation(2)
        public void testMethod2() {

        }
    }

}
