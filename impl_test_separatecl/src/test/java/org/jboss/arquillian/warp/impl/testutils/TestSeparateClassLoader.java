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
package org.jboss.arquillian.warp.impl.testutils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

public class TestSeparateClassLoader {

    @Test
    public void test() throws Throwable {

        ClassLoader classLoader = SeparatedClassloaderLauncherSessionListener.initializeClassLoader(TestDynamicClassLoading.class);

        Class<?> loadedClass =
                TestSeparateClassLoader.getFromTestClassloader(classLoader, TestDynamicClassLoading.class);

        Method method = loadedClass.getMethod("test");

        @SuppressWarnings("unchecked")
        Class<? extends Annotation> testAnnotation =
            (Class<? extends Annotation>) classLoader.loadClass(Test.class.getName());

        Object annotation = method.getAnnotation(testAnnotation);
        assertNotNull(annotation, "Test annotation wasn't found");
    }

    /*Removed as part of the JUnit5 migration, the runner does not exist
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
    }*/

    private static Class<?> getFromTestClassloader(ClassLoader classLoader, Class<?> clazz) throws ClassNotFoundException{ //throws InitializationError {

        final String className = clazz.getName();

        try {
            Class<?> loadedClazz = classLoader.loadClass(className);
            //log.info("Loaded test class: " + className);
            return loadedClazz;
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }
}