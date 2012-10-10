package org.jboss.arquillian.warp.impl.shared.transformation;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javassist.ClassPool;
import javassist.CtClass;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.impl.testutils.ClassLoaderUtils;
import org.jboss.arquillian.warp.impl.testutils.ShrinkWrapUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class MigrationTestRunnable {

    private static final String METHOD_NAME = "process";
    private static final Class<?>[] METHOD_ARGUMENTS = new Class<?>[] { (new byte[0]).getClass() };

    public static Class<?> process(byte[] migratedClassFile) throws Exception {

        ClassPool pool = ClassPool.getDefault();

        CtClass ctClazz = pool.makeClassIfNew(new ByteArrayInputStream(migratedClassFile));

        Class<?> clazz = ctClazz.toClass();

        return clazz;
    }

    public static Class<?> invokeSeparately(byte[] migratedClassFile) throws Throwable {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader separatedClassLoader = separatedClassLoader();
            Class<?> separatedClass = separatedClassLoader.loadClass(MigrationTestRunnable.class.getName());

            Thread.currentThread().setContextClassLoader(separatedClassLoader);

            return invoke(separatedClass, migratedClassFile);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    private static Class<?> invoke(Class<?> separatedClass, byte[] migratedClassFile) throws Throwable {
        try {

            Method processMethod = separatedClass.getMethod(MigrationTestRunnable.METHOD_NAME,
                    MigrationTestRunnable.METHOD_ARGUMENTS);

            return (Class<?>) processMethod.invoke(null, migratedClassFile);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                throw ((InvocationTargetException) e).getTargetException();
            }
            throw e;
        }
    }

    private static ClassLoader separatedClassLoader() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class).addClasses(MigrationTestRunnable.class,
                ShrinkWrapClassLoader.class)
                .addClasses(ServerAssertion.class);

        JavaArchive javassistArchive = ShrinkWrapUtils.getJavaArchiveFromClass(javassist.CtClass.class);

        return new ShrinkWrapClassLoader(ClassLoaderUtils.getBootstrapClassLoader(), archive, javassistArchive);
    }
}
