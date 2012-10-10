package org.jboss.arquillian.warp.testutils;

import static org.jboss.modules.DependencySpec.createLocalDependencySpec;
import static org.jboss.modules.ResourceLoaderSpec.createResourceLoaderSpec;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Logger;

import javassist.CtClass;

import org.jboss.arquillian.warp.testutils.TestResourceLoader.TestResourceLoaderBuilder;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.filter.ClassFilter;
import org.jboss.modules.filter.ClassFilters;
import org.jboss.modules.filter.PathFilter;
import org.jboss.modules.filter.PathFilters;
import org.jboss.modules.util.ModulesTestBase;
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

    private static Base base = new Base();

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
        ClassLoader moduleClassLoader = getModuleClassLoader();

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

        ClassLoader shrinkWrapClassLoader = getShrinkWrapClassLoader(moduleClassLoader, archive, testClass);

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

    static ClassLoader getModuleClassLoader() throws InitializationError {
        try {
            final ModuleIdentifier identifier = ModuleIdentifier.create("module-identifier");
            ModuleSpec.Builder specBuilder = ModuleSpec.build(identifier);

            PathFilter in = PathFilters.acceptAll();

            ClassFilter classImportFilter = ClassFilters.acceptAll();
            ClassFilter classExportFilter = ClassFilters.fromResourcePathFilter(in);

            PathFilter importFilter = PathFilters.acceptAll();
            PathFilter exportFilter = PathFilters.acceptAll();
            PathFilter resourceImportFilter = PathFilters.acceptAll();
            PathFilter resourceExportFilter = PathFilters.acceptAll();

            specBuilder.addResourceRoot(createResourceLoaderSpec(getTestResourceLoader()));
            specBuilder.addDependency(createLocalDependencySpec(importFilter, exportFilter, resourceImportFilter,
                    resourceExportFilter, classImportFilter, classExportFilter));
            base.addModuleSpec(specBuilder.create());

            ModuleClassLoader moduleClassLoader = base.loadModule(identifier).getClassLoader();
            return moduleClassLoader;
        } catch (Exception e) {
            throw new InitializationError(e);
        }
    }

    static ClassLoader getShrinkWrapClassLoader(ClassLoader classLoader, JavaArchive archive, Class<?> testClass)
            throws InitializationError {
        try {

            JavaArchive finalArchive = ShrinkWrap.create(JavaArchive.class);
            // JUnit
            finalArchive.addClasses(Test.class);
            // ShrinkWrap - JavaArchive
            finalArchive.addClasses(SecurityActions.getAncestors(JavaArchive.class));
            // testClass
            finalArchive.addClasses(SecurityActions.getAncestors(testClass));
            // merge with user-provided archive
            finalArchive.merge(archive);

            ShrinkWrapClassLoader shrinkwrapClassLoader = new ShrinkWrapClassLoader(classLoader, finalArchive);
            return shrinkwrapClassLoader;
        } catch (Exception e) {
            throw new InitializationError(e);
        }
    }

    public static ClassLoader getShrinkWrapClassLoader(JavaArchive archive, Class<?> testClass) throws InitializationError {
        ClassLoader classLoader = getModuleClassLoader();
        return getShrinkWrapClassLoader(classLoader, archive, testClass);
    }

    private static TestResourceLoader getTestResourceLoader() throws Exception {
        TestResourceLoaderBuilder builder = new TestResourceLoaderBuilder();
        return builder.create();
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

    private static class Base extends ModulesTestBase {

        public Base() {
            try {
                setUp();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public Class<?> loadClass(ModuleIdentifier identifier, String className) throws Exception {
            System.out.println("loadclass: " + className);
            return super.loadClass(identifier, className);
        }

        @Override
        protected void addModuleSpec(ModuleSpec moduleSpec) {
            super.addModuleSpec(moduleSpec);
        }

        @Override
        public Module loadModule(ModuleIdentifier identifier) throws ModuleLoadException {
            return super.loadModule(identifier);
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