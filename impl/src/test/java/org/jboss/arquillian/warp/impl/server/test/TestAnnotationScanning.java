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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

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

        List<Method> methods =
            SecurityActions.getMethodsMatchingAllQualifiers(TestingClass.class, Arrays.<Annotation>asList(annotation));

        assertEquals(1, methods.size());

        assertEquals("testMethod2", methods.get(0).getName());
    }

    @Test
    public void test2() {
        TestAnnotation annotation = new TestAnnotation() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return TestAnnotation.class;
            }

            @Override
            public int value() {
                return 3;
            }
        };

        List<Method> methods =
            SecurityActions.getMethodsMatchingAllQualifiers(TestingClass.class, Arrays.<Annotation>asList(annotation));

        assertEquals(0, methods.size());
    }

    @Test
    public void test3() {
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

        AnotherTestAnnotation annotation2 = new AnotherTestAnnotation() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return AnotherTestAnnotation.class;
            }

            @Override
            public int value() {
                return 1;
            }
        };

        List<Method> methods = SecurityActions.getMethodsMatchingAllQualifiers(TestingClass.class,
            Arrays.<Annotation>asList(annotation, annotation2));

        assertEquals(1, methods.size());

        assertEquals("testMethod4", methods.get(0).getName());
    }

    public static class TestingClass {

        @TestAnnotation(1)
        public void testMethod1() {

        }

        @TestAnnotation(2)
        public void testMethod2() {

        }

        @TestAnnotation(1) @AnotherTestAnnotation(2)
        public void testMethod3() {

        }

        @TestAnnotation(2) @AnotherTestAnnotation(1)
        public void testMethod4() {

        }

        @TestAnnotation(3) @AnotherTestAnnotation(1)
        public void testMethod5() {

        }
    }
}
