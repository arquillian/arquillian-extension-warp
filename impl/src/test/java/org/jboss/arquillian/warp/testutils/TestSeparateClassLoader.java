package org.jboss.arquillian.warp.testutils;

import static org.junit.Assert.assertNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.runners.model.InitializationError;

public class TestSeparateClassLoader {

    @Test
    public void test() throws Throwable {

        ClassLoader classLoader = SeparatedClassloader.initializeClassLoader(TestDynamicClassLoading.class);

        Class<?> loadedClass = SeparatedClassloader.getFromTestClassloader(classLoader, TestDynamicClassLoading.class);

        System.out.println(loadedClass.hashCode() + " " + TestDynamicClassLoading.class.hashCode());

        Method method = loadedClass.getMethod("test");

        Class<? extends Annotation> testAnnotation = (Class<? extends Annotation>) classLoader.loadClass(Test.class.getName());

        Object annotation = method.getAnnotation(testAnnotation);
        assertNotNull("Test annotation wasn't found", annotation);
    }

    @Test
    public void testCreatingRunner() throws InitializationError {
        try {
            new SeparatedClassloader(TestDynamicClassLoading.class);
        } catch (InitializationError e) {
            e.printStackTrace();
            for (Throwable cause : e.getCauses()) {
                cause.printStackTrace();
            }
            throw e;
        }
    }
}
