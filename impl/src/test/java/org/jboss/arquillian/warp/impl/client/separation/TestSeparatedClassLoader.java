package org.jboss.arquillian.warp.impl.client.separation;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@RunWith(SeparatedClassloaderRunner.class)
public class TestSeparatedClassLoader {

    @SeparatedClassPath
    public static JavaArchive[] archive() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class).addClasses(SeparatedClassLoader.class,
                ClassLoaderUtils.class);

        JavaArchive mockito = ShrinkWrapUtils.getJavaArchiveFromClass(Mockito.class);

        JavaArchive junit = ShrinkWrapUtils.getJavaArchiveFromClass(Assert.class);

        return new JavaArchive[] { archive, mockito, junit };
    }

    @Test
    public void testFirstClassLoaderHit() throws ClassNotFoundException {

        ClassLoader cl1 = mock(ClassLoader.class);
        ClassLoader cl2 = mock(ClassLoader.class);

        when(cl1.loadClass(Mockito.anyString())).thenAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                return String1.class;
            }
        });

        when(cl2.loadClass(Mockito.anyString())).thenAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                return String2.class;
            }
        });

        SeparatedClassLoader separated = new SeparatedClassLoader(cl1, cl2);

        Class<?> loadedClass = separated.loadClass(String.class.getName());
        assertSame(String1.class, loadedClass);
    }

    @Test
    public void testClassLoaderDelegationToSecondClassLoader() throws ClassNotFoundException {

        ClassLoader cl1 = mock(ClassLoader.class);
        ClassLoader cl2 = mock(ClassLoader.class);

        when(cl1.loadClass(Mockito.anyString())).thenAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                throw new ClassNotFoundException();
            }
        });

        when(cl2.loadClass(Mockito.anyString())).thenAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                return String2.class;
            }
        });

        SeparatedClassLoader separated = new SeparatedClassLoader(cl1, cl2);

        Class<?> loadedClass = separated.loadClass(String.class.getName());
        assertSame(String2.class, loadedClass);
    }

    @Test
    public void testClassLoaderDelegationToBootstrapClassLoader() throws ClassNotFoundException {

        ClassLoader cl1 = mock(ClassLoader.class);
        ClassLoader cl2 = mock(ClassLoader.class);

        when(cl1.loadClass(Mockito.anyString())).thenAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                throw new ClassNotFoundException();
            }
        });

        when(cl2.loadClass(Mockito.anyString())).thenAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                throw new ClassNotFoundException();
            }
        });

        SeparatedClassLoader separated = new SeparatedClassLoader(cl1, cl2);

        Class<?> loadedClass = separated.loadClass(String.class.getName());
        assertSame(String.class, loadedClass);
    }

    @Test
    public void testClassLoaderNotFound() throws ClassNotFoundException {

        ClassLoader cl1 = mock(ClassLoader.class);
        ClassLoader cl2 = mock(ClassLoader.class);

        when(cl1.loadClass(Mockito.anyString())).thenAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                throw new ClassNotFoundException();
            }
        });

        when(cl2.loadClass(Mockito.anyString())).thenAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                throw new ClassNotFoundException();
            }
        });

        SeparatedClassLoader separated = new SeparatedClassLoader(cl1, cl2);

        try {
            Class<?> loadedClass = separated.loadClass("SomeNonExistingClassName");
            fail("class should not be found");
        } catch (ClassNotFoundException e) {
            // TODO: handle exception
        }
    }

    private class String1 {
    }

    private class String2 {
    }
}
