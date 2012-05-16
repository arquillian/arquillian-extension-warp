package org.jboss.arquillian.warp.test;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.arquillian.warp.jsf.BeforePhase;
import org.jboss.arquillian.warp.jsf.Phase;
import org.junit.Test;

public class TestAnnotationScanner {
    @Test
    public void test() {
        BeforePhase annotation = new BeforePhase() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return BeforePhase.class;
            }

            @Override
            public Phase value() {
                return Phase.APPLY_REQUEST_VALUES;
            }
        };

        List<Method> methods = SecurityActions.getMethodsWithAnnotation(TestingClass.class, annotation);

        assertEquals(1, methods.size());

    }

    public static class TestingClass {
        @BeforePhase(Phase.APPLY_REQUEST_VALUES)
        public void applyRequestValues() {

        }
        
        @BeforePhase(Phase.INVOKE_APPLICATION)
        public void invokeApplication() {

        }
    }

}
