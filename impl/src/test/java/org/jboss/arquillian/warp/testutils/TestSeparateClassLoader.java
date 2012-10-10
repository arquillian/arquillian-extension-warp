package org.jboss.arquillian.warp.testutils;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import javassist.CtClass;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runners.model.InitializationError;

public class TestSeparateClassLoader {

    @Test
    public void test() throws Throwable {

        ClassLoader classLoader = SeparatedClassloader.initializeClassLoader(TestDynamicClassLoading.class);

        Class<?> loadedClass = SeparatedClassloader.getFromTestClassloader(classLoader, TestDynamicClassLoading.class);

        System.out.println(loadedClass.hashCode() + " " + TestDynamicClassLoading.class.hashCode());

        Method method = loadedClass.getMethod("test");

        Class<? extends Annotation> testAnnotation = (Class<? extends Annotation>) classLoader.loadClass(Test.class.getName());

        Object annotation = method.getAnnotation(testAnnotation);
        assertNotNull("Test annotation wasn't found", annotation);
    }

    @Test
    public void testCreatingRunner() throws InitializationError {
        try {
            new SeparatedClassloader(TestDynamicClassLoading.class);
        } catch (InitializationError e) {
            e.printStackTrace();
            for (Throwable cause : e.getCauses()) {
                cause.printStackTrace();
            }
            throw e;
        }
    }

    @Test
    public void testClasspathPropagation() throws Throwable {
        JavaArchive javassistArchive = getArchiveForClass(CtClass.class);

        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);

        archive = archive.merge(javassistArchive);

        ClassLoader classLoader = SeparatedClassloader.getShrinkWrapClassLoader(ClassLoader.getSystemClassLoader().getParent(),
                archive, TestSeparateClassLoader.class);

        classLoader.loadClass(javassist.CtClass.class.getName());
    }

    private JavaArchive getArchiveForClass(Class<?> clazz) throws MalformedURLException {
        URL url = clazz.getResource(clazz.getSimpleName() + ".class");
        String file = url.getFile();
        file = file.substring(file.indexOf(":") + 1);
        file = file.substring(0, file.indexOf('!'));

        JavaArchive jar = ShrinkWrap.create(ZipImporter.class).importFrom(new File(file)).as(JavaArchive.class);

        return jar;
    }
}
