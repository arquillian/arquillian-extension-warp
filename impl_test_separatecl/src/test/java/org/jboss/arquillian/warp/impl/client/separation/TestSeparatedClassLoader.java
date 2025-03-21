/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.warp.impl.client.separation;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.bytebuddy.agent.Installer;
import net.bytebuddy.dynamic.loading.ClassInjector;
import org.jboss.arquillian.warp.impl.testutils.SeparatedClassPath;
import org.jboss.arquillian.warp.impl.testutils.SeparatedClassloaderExtension;
import org.jboss.arquillian.warp.impl.utils.ClassLoaderUtils;
import org.jboss.arquillian.warp.impl.utils.ShrinkWrapUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.objenesis.ObjenesisStd;

@ExtendWith(SeparatedClassloaderExtension.class)
public class TestSeparatedClassLoader {

    @SeparatedClassPath
    public static JavaArchive[] archive() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class).addClasses(SeparatedClassLoader.class,
            ClassLoaderUtils.class);

        JavaArchive mockitoJar = ShrinkWrapUtils.getJavaArchiveFromClass(Mockito.class);
        // Add mockito dependencies "bytebuddy", "bytebuddy-agent" and "objenesis";
        // otherwise "ClassNotFoundException" will be thrown when executing the test.
        JavaArchive bytebuddyJar = ShrinkWrapUtils.getJavaArchiveFromClass(ClassInjector.class);
        JavaArchive bytebuddyAgentJar = ShrinkWrapUtils.getJavaArchiveFromClass(Installer.class);
        JavaArchive objenesisJar = ShrinkWrapUtils.getJavaArchiveFromClass(ObjenesisStd.class);

        JavaArchive junit = ShrinkWrapUtils.getJavaArchiveFromClass(Assertions.class);

        return new JavaArchive[] {archive, mockitoJar, bytebuddyJar, bytebuddyAgentJar, objenesisJar, junit};
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
            @SuppressWarnings("unused")
            Class<?> loadedClass = separated.loadClass("SomeNonExistingClassName");
            fail("class should not be found");
        } catch (ClassNotFoundException e) {
            // this is okay
        }
    }

    private class String1 {
    }

    private class String2 {
    }
}
