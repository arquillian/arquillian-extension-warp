package org.jboss.arquillian.warp.impl.testutils;

import static org.junit.Assert.assertNotNull;
import javassist.ClassPool;
import javassist.CtClass;

import org.jboss.arquillian.warp.impl.client.transformation.CtClassAsset;
import org.jboss.arquillian.warp.impl.utils.ClassLoaderUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class TestJavassistAndSeparatedClassLoader {

    private static final String CLASS_NAME = TestJavassistAndSeparatedClassLoader.class.getName() + "Class";

    @Test
    public void test() throws Exception {
        ClassPool cp = ClassPool.getDefault();

        CtClass ctClass = cp.makeClass(CLASS_NAME);

        JavaArchive archive = ShrinkWrap.create(JavaArchive.class).add(new CtClassAsset(ctClass));

        ClassLoader classLoader = new ShrinkWrapClassLoader(ClassLoaderUtils.getBootstrapClassLoader(), archive);

        assertNotNull(classLoader.loadClass(CLASS_NAME));
    }
}
