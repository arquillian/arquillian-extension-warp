package org.jboss.arquillian.warp.testutils;

import javassist.ClassPool;
import javassist.CtClass;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class TestJavassistAndSeparatedClassLoader {

    private static final String CLASS_NAME = TestJavassistAndSeparatedClassLoader.class.getName() + "Class";

    @Test
    public void test() throws Exception {
        ClassPool cp = ClassPool.getDefault();

        CtClass ctClass = cp.makeClass(CLASS_NAME);

        JavaArchive archive = ShrinkWrap.create(JavaArchive.class).add(new CtClassAsset(ctClass));

        System.out.println(archive.toString(true));

        ClassLoader classLoader = SeparatedClassloader.getShrinkWrapClassLoader(archive,
                TestJavassistAndSeparatedClassLoader.class);

        classLoader.loadClass(CLASS_NAME);

    }
}
