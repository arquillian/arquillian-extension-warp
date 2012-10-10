package org.jboss.arquillian.warp.impl.testutils;

import static org.junit.Assert.assertNotNull;

import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class ShrinkWrapUtilsTest {

    @Test
    public void testJavaArchiveFromClass() throws Throwable {
        JavaArchive archive = ShrinkWrapUtils.getJavaArchiveFromClass(Test.class);

        assertNotNull(archive.get("/org/junit/Test.class"));
        assertNotNull(archive.get("/org/junit/Ignore.class"));
    }

    @Test
    public void testMultipleUse() throws ClassNotFoundException {
        JavaArchive archive = ShrinkWrapUtils.getJavaArchiveFromClass(Test.class);
        
        System.out.println(archive.getName());
        
        ShrinkWrapClassLoader classLoader = new ShrinkWrapClassLoader(ClassLoaderUtils.getBootstrapClassLoader(), archive);
        Class<?> nestedClass = classLoader.loadClass(Test.class.getName());
        
        JavaArchive nestedArchive = ShrinkWrapUtils.getJavaArchiveFromClass(nestedClass);
        
        assertNotNull(nestedArchive.get("/org/junit/Test.class"));
        assertNotNull(nestedArchive.get("/org/junit/Ignore.class"));
    }
}
