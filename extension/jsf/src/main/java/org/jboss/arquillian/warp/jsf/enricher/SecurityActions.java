/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.jsf.enricher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A set of privileged actions that are not to leak out of this package
 */
final class SecurityActions {

    // -------------------------------------------------------------------------------||
    // Constructor ------------------------------------------------------------------||
    // -------------------------------------------------------------------------------||

    /**
     * No instantiation
     */
    private SecurityActions() {
        throw new UnsupportedOperationException("No instantiation");
    }

    // -------------------------------------------------------------------------------||
    // Utility Methods --------------------------------------------------------------||
    // -------------------------------------------------------------------------------||

    /**
     * Obtains the Thread Context ClassLoader
     */
    static ClassLoader getThreadContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    static boolean isClassPresent(String name) {
        try {
            loadClass(name);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static Class<?> loadClass(String className) {
        try {
            return Class.forName(className, true, getThreadContextClassLoader());
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(className, true, SecurityActions.class.getClassLoader());
            } catch (ClassNotFoundException e2) {
                throw new RuntimeException("Could not load class " + className, e2);
            }
        }
    }

    static <T> T newInstance(final String className, final Class<?>[] argumentTypes, final Object[] arguments,
        final Class<T> expectedType) {
        return newInstance(className, argumentTypes, arguments, expectedType, getThreadContextClassLoader());
    }

    static <T> T newInstance(final String className, final Class<?>[] argumentTypes, final Object[] arguments,
        final Class<T> expectedType, ClassLoader classLoader) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className, false, classLoader);
        } catch (Exception e) {
            throw new RuntimeException("Could not load class " + className, e);
        }
        Object obj = newInstance(clazz, argumentTypes, arguments);
        try {
            return expectedType.cast(obj);
        } catch (Exception e) {
            throw new RuntimeException("Loaded class " + className + " is not of expected type " + expectedType, e);
        }
    }

    /**
     * Create a new instance by finding a constructor that matches the argumentTypes signature using the arguments for
     * instantiation.
     *
     * @param className
     *     Full classname of class to create
     * @param argumentTypes
     *     The constructor argument types
     * @param arguments
     *     The constructor arguments
     *
     * @return a new instance
     *
     * @throws IllegalArgumentException
     *     if className, argumentTypes, or arguments are null
     * @throws RuntimeException
     *     if any exceptions during creation
     * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
     * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
     */
    static <T> T newInstance(final Class<T> implClass, final Class<?>[] argumentTypes, final Object[] arguments) {
        if (implClass == null) {
            throw new IllegalArgumentException("ImplClass must be specified");
        }
        if (argumentTypes == null) {
            throw new IllegalArgumentException("ArgumentTypes must be specified. Use empty array if no arguments");
        }
        if (arguments == null) {
            throw new IllegalArgumentException("Arguments must be specified. Use empty array if no arguments");
        }
        final T obj;
        try {
            Constructor<T> constructor = getConstructor(implClass, argumentTypes);
            constructor.setAccessible(true);
            obj = constructor.newInstance(arguments);
        } catch (Exception e) {
            throw new RuntimeException("Could not create new instance of " + implClass, e);
        }

        return obj;
    }

    /**
     * Obtains the Constructor specified from the given Class and argument types
     *
     * @throws NoSuchMethodException
     */
    static <T> Constructor<T> getConstructor(final Class<T> clazz, final Class<?>... argumentTypes)
        throws NoSuchMethodException {
        return clazz.getDeclaredConstructor(argumentTypes);
    }

    /**
     * Set a single Field value
     *
     * @param target
     *     The object to set it on
     * @param fieldName
     *     The field name
     * @param value
     *     The new value
     */
    public static void setFieldValue(final Class<?> source, final Object target, final String fieldName,
        final Object value)
        throws NoSuchFieldException, IllegalAccessException {
        Field field = source.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    public static List<Field> getFieldsWithAnnotation(final Class<?> source,
        final Class<? extends Annotation> annotationClass) {
        List<Field> foundFields = new ArrayList<Field>();
        Class<?> nextSource = source;
        while (nextSource != Object.class) {
            for (Field field : nextSource.getDeclaredFields()) {
                if (field.isAnnotationPresent(annotationClass)) {
                    field.setAccessible(true);
                    foundFields.add(field);
                }
            }
            nextSource = nextSource.getSuperclass();
        }
        return foundFields;
    }

    public static List<Method> getMethodsWithAnnotation(final Class<?> source,
        final Class<? extends Annotation> annotationClass) {
        List<Method> foundMethods = new ArrayList<Method>();
        Class<?> nextSource = source;
        while (nextSource != Object.class) {
            for (Method method : nextSource.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotationClass)) {
                    method.setAccessible(true);
                    foundMethods.add(method);
                }
            }
            nextSource = nextSource.getSuperclass();
        }
        return foundMethods;
    }

    static String getProperty(final String key) {
        return System.getProperty(key);
    }

    @SuppressWarnings("unchecked")
    static <T extends Annotation> T findAnnotation(final Annotation[] annotations, final Class<T> needle) {
        for (Annotation a : annotations) {
            if (a.annotationType() == needle) {
                return (T) a;
            }
        }
        return null;
    }
}
