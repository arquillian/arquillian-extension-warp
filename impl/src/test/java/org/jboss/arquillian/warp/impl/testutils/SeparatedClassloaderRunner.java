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
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.arquillian.warp.impl.utils.ClassLoaderUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class SeparatedClassloaderRunner extends BlockJUnit4ClassRunner {

    private static String ARCHIVE_MESSAGE =
        "There must be exactly one no-arg static method which returns JavaArchive with annotation @SeparatedClassPath defined";

    private static Logger log = Logger.getLogger(SeparatedClassloaderRunner.class.getName());

    private static ThreadLocal<ClassLoader> initializedClassLoader = new ThreadLocal<ClassLoader>();
    private ClassLoader classLoader;
    private ClassLoader originalClassLoader;

    public SeparatedClassloaderRunner(Class<?> testClass) throws InitializationError {
        super(getFromTestClassloader(initializeClassLoader(testClass), testClass));
        this.classLoader = initializedClassLoader.get();
        initializedClassLoader.set(null);
    }

    @Override
    protected Statement withBeforeClasses(Statement statement) {
        Statement original = super.withBeforeClasses(statement);

        Statement backupAndReplaceClassLoader = new Statement() {

            @Override
            public void evaluate() throws Throwable {
                if (originalClassLoader == null) {
                    originalClassLoader = Thread.currentThread().getContextClassLoader();
                }
                Thread.currentThread().setContextClassLoader(classLoader);
            }
        };

        return new ComposedStatement(backupAndReplaceClassLoader, original);
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        Statement original = super.withAfterClasses(statement);

        Statement restoreOriginalClassLoader = new Statement() {

            @Override
            public void evaluate() throws Throwable {
                if (originalClassLoader != null) {
                    Thread.currentThread().setContextClassLoader(originalClassLoader);
                }
            }
        };

        return new ComposedStatement(original, restoreOriginalClassLoader);
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

    static ClassLoader initializeClassLoader(Class<?> testClass) throws InitializationError {
        List<Method> classPath = SecurityActions.getMethodsWithAnnotation(testClass, SeparatedClassPath.class);

        if (classPath.isEmpty()) {
            throw new InitializationError(ARCHIVE_MESSAGE);
        }

        Method method = classPath.iterator().next();

        if (!checkClassPathMethodType(method)) {
            throw new InitializationError(ARCHIVE_MESSAGE);
        }

        JavaArchive[] archives;
        try {
            Object result = classPath.get(0).invoke(null);
            if (result instanceof JavaArchive) {
                archives = new JavaArchive[] {(JavaArchive) result};
            } else {
                archives = (JavaArchive[]) result;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to retrieve @SeparatedClassPath archive", e);
        }

        ClassLoader shrinkWrapClassLoader = getSeparatedClassLoader(archives, testClass);

        initializedClassLoader.set(shrinkWrapClassLoader);

        return initializedClassLoader.get();
    }

    static Class<?> getFromTestClassloader(ClassLoader classLoader, Class<?> clazz) throws InitializationError {

        final String className = clazz.getName();

        try {
            Class<?> loadedClazz = classLoader.loadClass(className);
            log.info("Loaded test class: " + className);
            return loadedClazz;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new InitializationError(e);
        }
    }

    private static ClassLoader getSeparatedClassLoader(JavaArchive[] archives, Class<?> testClass)
        throws InitializationError {
        try {
            ClassLoader bootstrapClassLoader = ClassLoaderUtils.getBootstrapClassLoader();

            JavaArchive baseArchive = ShrinkWrap.create(JavaArchive.class);
            // JUnit
            baseArchive.addClasses(Test.class);
            // ShrinkWrap - JavaArchive
            baseArchive.addClasses(SecurityActions.getAncestors(JavaArchive.class));
            // testClass
            baseArchive.addClasses(SecurityActions.getAncestors(testClass));

            archives = Arrays.copyOf(archives, archives.length + 1);
            archives[archives.length - 1] = baseArchive;

            ShrinkWrapClassLoader shrinkwrapClassLoader = new ShrinkWrapClassLoader(bootstrapClassLoader, archives);
            return shrinkwrapClassLoader;
        } catch (Exception e) {
            throw new InitializationError(e);
        }
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        if (classLoader == null) {
            classLoader = initializedClassLoader.get();
        }

        if (classLoader == null) {
            throw new IllegalStateException("classLoader must not be null in this state");
        }

        try {

            @SuppressWarnings("unchecked")
            Class<? extends Annotation> testAnnotation = (Class<? extends Annotation>) classLoader.loadClass(Test.class
                .getName());
            return getTestClass().getAnnotatedMethods(testAnnotation);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class ComposedStatement extends Statement {

        private Statement[] statements;

        ComposedStatement(Statement... statements) {
            this.statements = statements;
        }

        @Override
        public void evaluate() throws Throwable {
            for (Statement statement : statements) {
                statement.evaluate();
            }
        }
    }
}
