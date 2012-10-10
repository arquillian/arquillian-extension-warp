package org.jboss.arquillian.warp.impl.testutils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class SeparatedClassloader extends BlockJUnit4ClassRunner {

    private static String ARCHIVE_MESSAGE = "There must be exactly one no-arg static method which returns JavaArchive with annotation @SeparatedClassPath defined";

    private static Logger log = Logger.getLogger(SeparatedClassloader.class.getName());

    private static ThreadLocal<ClassLoader> initializedClassLoader = new ThreadLocal<ClassLoader>();
    private ClassLoader classLoader;
    private ClassLoader originalClassLoader;

    public SeparatedClassloader(Class<?> testClass) throws InitializationError {
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

    static ClassLoader initializeClassLoader(Class<?> testClass) throws InitializationError {
        List<Method> classPath = SecurityActions.getMethodsWithAnnotation(testClass, SeparatedClassPath.class);

        if (classPath.isEmpty()) {
            throw new InitializationError(ARCHIVE_MESSAGE);
        }

        Method method = classPath.iterator().next();

        if (!Modifier.isStatic(method.getModifiers()) || method.getParameterTypes().length != 0
                || !method.getReturnType().isAssignableFrom(JavaArchive.class)) {
            throw new InitializationError(ARCHIVE_MESSAGE);
        }

        JavaArchive archive;
        try {
            archive = (JavaArchive) classPath.get(0).invoke(null);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to retrieve @SeparatedClassPath archive", e);
        }

        ClassLoader shrinkWrapClassLoader = getSeparatedClassLoader(archive, testClass);

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

    private static ClassLoader getSeparatedClassLoader(JavaArchive archive, Class<?> testClass) throws InitializationError {
        try {
            ClassLoader bootstrapClassLoader = ClassLoaderUtils.getBootstrapClassLoader();

            JavaArchive finalArchive = ShrinkWrap.create(JavaArchive.class);
            // JUnit
            finalArchive.addClasses(Test.class);
            // ShrinkWrap - JavaArchive
            finalArchive.addClasses(SecurityActions.getAncestors(JavaArchive.class));
            // testClass
            finalArchive.addClasses(SecurityActions.getAncestors(testClass));
            // merge with user-provided archive
            finalArchive.merge(archive);

            ShrinkWrapClassLoader shrinkwrapClassLoader = new ShrinkWrapClassLoader(bootstrapClassLoader, finalArchive);
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

            Class<? extends Annotation> testAnnotation = (Class<? extends Annotation>) classLoader.loadClass(Test.class
                    .getName());
            return getTestClass().getAnnotatedMethods(testAnnotation);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class ComposedStatement extends Statement {

        private Statement[] statements;

        public ComposedStatement(Statement... statements) {
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