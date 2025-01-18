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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * SecurityActions
 * <p>
 * A set of privileged actions that are not to leak out of this package
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
final class SecurityActions {

    // -------------------------------------------------------------------------------||
    // Constructor
    // ------------------------------------------------------------------||
    // -------------------------------------------------------------------------------||

    /**
     * No instantiation
     */
    private SecurityActions() {
        throw new UnsupportedOperationException("No instantiation");
    }

    // -------------------------------------------------------------------------------||
    // Utility Methods
    // --------------------------------------------------------------||
    // -------------------------------------------------------------------------------||


    static List<Method> getMethodsWithAnnotation(final Class<?> source,
        final Class<? extends Annotation> annotationClass) {
        List<Method> declaredAccessableMethods = AccessController.doPrivileged(new PrivilegedAction<List<Method>>() {
            public List<Method> run() {
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
        });
        return declaredAccessableMethods;
    }

    /**
     * Get all subclasses and interfaces in whole class hierarchy
     */
    static Class<?>[] getAncestors(Class<?> clazz) {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        while (clazz != null) {
            classes.add(clazz);
            if (clazz.getSuperclass() != null) {
                classes.add(clazz.getSuperclass());
            }
            classes.addAll(Arrays.asList(clazz.getInterfaces()));
            for (Class<?> interfaze : clazz.getInterfaces()) {
                classes.addAll(Arrays.asList(getAncestors(interfaze)));
            }

            clazz = clazz.getSuperclass();
        }

        return classes.toArray(new Class<?>[classes.size()]);
    }
}
