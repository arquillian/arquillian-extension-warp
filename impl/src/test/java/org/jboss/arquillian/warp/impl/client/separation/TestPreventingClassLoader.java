package org.jboss.arquillian.warp.impl.client.separation;

import static org.junit.Assert.fail;

import org.jboss.arquillian.warp.impl.testutils.SeparatedClassPath;
import org.jboss.arquillian.warp.impl.testutils.SeparatedClassloaderRunner;
import org.jboss.arquillian.warp.impl.utils.ClassLoaderUtils;
import org.jboss.arquillian.warp.impl.utils.ShrinkWrapUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(SeparatedClassloaderRunner.class)
public class TestPreventingClassLoader {

    @SeparatedClassPath
    public static JavaArchive[] archive() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class).addClasses(PreventingClassLoader.class,
                ClassLoaderUtils.class);

        JavaArchive mockito = ShrinkWrapUtils.getJavaArchiveFromClass(Mockito.class);

        JavaArchive junit = ShrinkWrapUtils.getJavaArchiveFromClass(Assert.class);

        return new JavaArchive[] { archive, mockito, junit };
    }

    @Test
    public void testPreventLoadingClass() throws ClassNotFoundException {

        PreventingClassLoader cl = new PreventingClassLoader(ClassLoaderUtils.getBootstrapClassLoader(), String.class.getName());

        try {
            cl.loadClass(String.class.getName());
            fail("The class String should be prevented from being loaded");
        } catch (ClassNotFoundException e) {
        }
    }

    @Test
    public void testDoNotPreventLoading() throws ClassNotFoundException {

        PreventingClassLoader cl = new PreventingClassLoader(ClassLoaderUtils.getBootstrapClassLoader(), "NonExistingClass");

        cl.loadClass(String.class.getName());
    }
}
