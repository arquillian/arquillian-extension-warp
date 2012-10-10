package org.jboss.arquillian.warp.impl.shared.transformation;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javassist.ClassPool;
import javassist.CtClass;

import org.jboss.arquillian.warp.impl.testutils.ClassLoaderUtils;
import org.jboss.arquillian.warp.impl.testutils.ShrinkWrapUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class MigrationRunnable {

    private static final String METHOD_NAME = "process";
    private static final Class<?>[] METHOD_ARGUMENTS = new Class<?>[] { String.class, String.class, (new byte[0]).getClass() };

    public static byte[] process(String oldClassName, String newClassName, byte[] oldClassFile) throws Exception {

        ClassPool pool = ClassPool.getDefault();

        CtClass clazz = pool.makeClassIfNew(new ByteArrayInputStream(oldClassFile));

        clazz.replaceClassName(oldClassName, newClassName);

        return clazz.toBytecode();
    }

    public static byte[] invokeSeparately(String oldClassName, String newClassName, byte[] oldClassFile) throws Throwable {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader separatedClassLoader = separatedClassLoader();
            Class<?> separatedClass = separatedClassLoader.loadClass(MigrationRunnable.class.getName());

            Thread.currentThread().setContextClassLoader(separatedClassLoader);

            return invoke(separatedClass, oldClassName, newClassName, oldClassFile);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    private static byte[] invoke(Class<?> separatedClass, String oldClassName, String newClassName, byte[] oldClassFile)
            throws Throwable {
        try {

            Method processMethod = separatedClass.getMethod(MigrationRunnable.METHOD_NAME, MigrationRunnable.METHOD_ARGUMENTS);

            return (byte[]) processMethod.invoke(null, oldClassName, newClassName, oldClassFile);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                throw ((InvocationTargetException) e).getTargetException();
            }
            throw e;
        }
    }

    private static ClassLoader separatedClassLoader() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class).addClasses(MigrationRunnable.class,
                ShrinkWrapClassLoader.class);

        JavaArchive javassistArchive = ShrinkWrapUtils.getJavaArchiveFromClass(javassist.CtClass.class);

        return new ShrinkWrapClassLoader(ClassLoaderUtils.getBootstrapClassLoader(), archive, javassistArchive);
    }
}