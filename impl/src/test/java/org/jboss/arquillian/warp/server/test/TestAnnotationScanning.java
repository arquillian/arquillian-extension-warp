package org.jboss.arquillian.warp.server.test;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.arquillian.warp.server.test.SecurityActions;
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
