package org.jboss.arquillian.warp.impl.testutils;

import static org.junit.Assert.assertNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.runners.model.InitializationError;

public class TestSeparateClassLoader {

    @Test
    public void test() throws Throwable {

        ClassLoader classLoader = SeparatedClassloaderRunner.initializeClassLoader(TestDynamicClassLoading.class);

        Class<?> loadedClass = SeparatedClassloaderRunner.getFromTestClassloader(classLoader, TestDynamicClassLoading.class);

        Method method = loadedClass.getMethod("test");

        Class<? extends Annotation> testAnnotation = (Class<? extends Annotation>) classLoader.loadClass(Test.class.getName());

        Object annotation = method.getAnnotation(testAnnotation);
        assertNotNull("Test annotation wasn't found", annotation);
    }

    @Test
    public void testCreatingRunner() throws InitializationError {
        try {
            new SeparatedClassloaderRunner(TestDynamicClassLoading.class);
        } catch (InitializationError e) {
            e.printStackTrace();
            for (Throwable cause : e.getCauses()) {
                cause.printStackTrace();
            }
            throw e;
        }
    }
}
