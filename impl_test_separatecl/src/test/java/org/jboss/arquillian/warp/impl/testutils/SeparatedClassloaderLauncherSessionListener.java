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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

/**
 * Part two of the "separated classloader" workaround (see README.md)
 *
 * @author WolfgangHG
 */
public class SeparatedClassloaderLauncherSessionListener implements LauncherSessionListener {

    private final Deque<AutoCloseable> closeables = new ConcurrentLinkedDeque<>();
    private ClassLoader originalClassLoader;

    private static final Logger log = Logger.getLogger(SeparatedClassloaderLauncherSessionListener.class.getName());

    /**
     * If the test contains a method that is annotated with {@link org.jboss.arquillian.warp.impl.testutils.SeparatedClassPath},
     * a new classloader is installed before test execution, that forwards the loading of test classes to the ShrinkWrapClassLoader.
     *
     */
    @Override
    public void launcherSessionOpened(LauncherSession session) {
        originalClassLoader = Thread.currentThread().getContextClassLoader();
        var defaultClassLoader = ClassLoaderUtils.getDefaultClassLoader();
        Thread.currentThread().setContextClassLoader(new ClassLoader() {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                Class<?> clazz = defaultClassLoader.loadClass(name);
                // Also called for interfaces...
                if (clazz.isInterface()) {
                    // Ignore interfaces (in "org.jboss.arquillian.warp.impl.server.lifecycle.TestLifecycleTest", this code is called
                    // for e.g. "jakarta.servlet.ServletRequest", which would fail
                    // as "SecurityActions.getMethodsWithAnnotation" works only for real classes.
                    return clazz;
                }
                List<Method> classPath = SecurityActions.getMethodsWithAnnotation(clazz, SeparatedClassPath.class);
                if (!classPath.isEmpty()) {
                    log.info("Found annotation " + SeparatedClassPath.class.getName() + " on class " + clazz);
                    ClassLoader classLoaderSeparated = initializeClassLoader(clazz);
                    //Register classloader for cleanup:
                    if (classLoaderSeparated instanceof AutoCloseable) {
                        closeables.push((AutoCloseable) classLoaderSeparated);
                    }
                    Class<?> cFound = classLoaderSeparated.loadClass(name);
                    return cFound;
                } else {
                    return clazz;
                }
            }
        });
    }

    @Override
    public void launcherSessionClosed(LauncherSession session) {
        ThrowableCollector collector = new ThrowableCollector(__ -> false);
        var iterator = closeables.descendingIterator();
        while (iterator.hasNext()) {
            collector.execute(() -> {
                var closeable = iterator.next();
                System.out.println("Closing classloader: " + closeable);
                closeable.close();
                iterator.remove();
            });
        }
        Thread.currentThread().setContextClassLoader(originalClassLoader);
        collector.assertEmpty();
    }

    private static boolean checkClassPathMethodType(Method method) {
        if (!Modifier.isStatic(method.getModifiers())) {
            return false;
        }

        if (method.getParameterTypes().length != 0) {
            return false;
        }

        if (method.getReturnType().isAssignableFrom(JavaArchive.class)) {
            return true;
        }

        if (method.getReturnType().isAssignableFrom(Array.newInstance(JavaArchive.class, 0).getClass())) {
            return true;
        }

        return false;
    }

    static ClassLoader initializeClassLoader(Class<?> testClass) {
        List<Method> classPath = SecurityActions.getMethodsWithAnnotation(testClass, SeparatedClassPath.class);

        String message = "There must be exactly one no-arg static method which returns JavaArchive with annotation @SeparatedClassPath defined";
        if (classPath.isEmpty()) {
            throw new RuntimeException(message);
        }

        Method method = classPath.iterator().next();

        if (!checkClassPathMethodType(method)) {
            throw new RuntimeException(message);
        }

        JavaArchive[] archives;
        try {
            Object result = classPath.get(0).invoke(null);
            if (result instanceof JavaArchive) {
                archives = new JavaArchive[] { (JavaArchive) result };
            } else {
                archives = (JavaArchive[]) result;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to retrieve @SeparatedClassPath archive", e);
        }

        return getSeparatedClassLoader(archives, testClass);
    }

    private static ClassLoader getSeparatedClassLoader(JavaArchive[] archives, Class<?> testClass) {

        // The old SeparatedClassLoaderRunner used this code (which returns systemClassLoader.parent), but this does not work here - no tests are discovered.
        // ClassLoader bootstrapClassLoader = org.jboss.arquillian.warp.impl.utils.ClassLoaderUtils.getBootstrapClassLoader();
        // Use this classloader:
        ClassLoader bootstrapClassLoader = ClassLoader.getSystemClassLoader();

        JavaArchive baseArchive = ShrinkWrap.create(JavaArchive.class);
        // JUnit
        baseArchive.addClasses(Test.class);
        // ShrinkWrap - JavaArchive
        baseArchive.addClasses(SecurityActions.getAncestors(JavaArchive.class));
        // testClass
        baseArchive.addClasses(SecurityActions.getAncestors(testClass));

        archives = Arrays.copyOf(archives, archives.length + 1);
        archives[archives.length - 1] = baseArchive;

        // The filtering classLoader pretends that the test class wasn't yet loaded so the child class loader will load it again (after asking its parent).
        ClassLoader filteringClassLoader = new ClassLoader(bootstrapClassLoader) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.equals(testClass.getName())) {
                    log.info("Forwarding class " + name + " to separated classloader");

                    return null;
                } else if (name.startsWith(testClass.getName() + "$")) {
                    // Internal class:
                    log.info("Forwarding class " + name + " to separated classloader");
                    return null;
                } else if (name.startsWith("org.jboss.arquillian.warp.impl.client.deployment.DeploymentEnricher")) {
                    // forward all classes from the warp implementation (but not from the test packages, e.g.
                    // "SeparatedClassloaderExtension") to the ShrinkWrapClassLoader.
                    // Used by "org.jboss.arquillian.warp.impl.client.deployment.TestDeploymentEnricherClassPath"
                    // Also exclude anonymous inner classes.
                    log.info("Forwarding class " + name + " to separated classloader");
                    return null;
                }
                else if (name.startsWith("org.jboss.arquillian.warp.impl.shared.RequestPayload")
                        || name.startsWith("org.jboss.arquillian.warp.impl.utils.SerializationUtils")) {
                    // forward all classes used by
                    // "org.jboss.arquillian.warp.impl.client.transformation.TestTransformedInspection".
                    log.info("Forwarding class " + name + " to separated classloader");
                    return null;
                } else {
                    return super.loadClass(name, resolve);
                }
            }
        };

        return new ShrinkWrapClassLoader(filteringClassLoader, archives);
    }
}
