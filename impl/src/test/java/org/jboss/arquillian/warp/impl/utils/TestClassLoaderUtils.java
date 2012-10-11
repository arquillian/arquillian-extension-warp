package org.jboss.arquillian.warp.impl.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.jboss.arquillian.warp.impl.utils.ClassLoaderUtils;
import org.junit.Test;

public class TestClassLoaderUtils {

    @Test
    public void testBootstrapClassLoader() throws Exception {

        ClassLoader classLoader = ClassLoaderUtils.getBootstrapClassLoader();

        assertNotNull("bootstrap classloader should not be null", classLoader);
        assertNull("bootstrap classloader should not have parent", classLoader.getParent());

        assertNotNull("java.lang.String should be on bootstrap classpath", classLoader.loadClass(String.class.getName()));

        try {
            classLoader.loadClass(Test.class.getName());
            fail("@Test annotation should not be present on bootstrap classpath");
        } catch (ClassNotFoundException e) {
            // expected exception
        }
    }
}
