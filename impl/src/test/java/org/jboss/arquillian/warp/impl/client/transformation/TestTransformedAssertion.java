/**
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
package org.jboss.arquillian.warp.impl.client.transformation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.extension.servlet.BeforeServlet;
import org.jboss.arquillian.warp.impl.client.separation.SeparateInvocator;
import org.jboss.arquillian.warp.impl.client.separation.SeparatedClassLoader;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.testutils.SeparatedClassPath;
import org.jboss.arquillian.warp.impl.testutils.SeparatedClassloaderRunner;
import org.jboss.arquillian.warp.impl.utils.ClassLoaderUtils;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.arquillian.warp.impl.utils.ShrinkWrapUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.ServiceExtensionLoader;
import org.jboss.shrinkwrap.spi.MemoryMapArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SeparatedClassloaderRunner.class)
public class TestTransformedAssertion {

    @SeparatedClassPath
    public static JavaArchive[] archive() {
        JavaArchive archive = ShrinkWrap
                .create(JavaArchive.class)
                .addClasses(ServerAssertion.class, RequestPayload.class, BeforeServlet.class)
                .addClasses(SerializationUtils.class, ShrinkWrapUtils.class, ClassLoaderUtils.class)
                .addClasses(TestTransformedAssertion.class, TransformedAssertion.class, MigratedAssertion.class,
                        AssertionTransformationException.class)
                .addClasses(SeparateInvocator.class, CtClassAsset.class,
                        SeparatedClassLoader.class);

        JavaArchive javassistArchive = ShrinkWrapUtils.getJavaArchiveFromClass(javassist.CtClass.class);
        JavaArchive junitArchive = ShrinkWrapUtils.getJavaArchiveFromClass(Test.class);

        JavaArchive shrinkWrapSpi = ShrinkWrapUtils.getJavaArchiveFromClass(MemoryMapArchive.class);
        JavaArchive shrinkWrapApi = ShrinkWrapUtils.getJavaArchiveFromClass(JavaArchive.class);
        JavaArchive shrinkWrapImpl = ShrinkWrapUtils.getJavaArchiveFromClass(ServiceExtensionLoader.class);

        return new JavaArchive[] { archive, javassistArchive, junitArchive, shrinkWrapSpi, shrinkWrapApi, shrinkWrapImpl };
    }

    @Test
    public void testAnonymousClass() throws Exception {

        ServerAssertion assertion = getAnonymousServerAssertion();

        TransformedAssertion transformedAssertion = new TransformedAssertion(assertion);
        Object modifiedAssertion = transformedAssertion.getTransformedAssertion();

        verifyServerAssertionClass(modifiedAssertion);
    }

    @Test
    public void testSerialization() throws Exception {

        ServerAssertion assertion = getAnonymousServerAssertion();

        RequestPayload payload = new RequestPayload(assertion);

        RequestPayload deserializedPayload = SerializationUtils.deserializeFromBytes(SerializationUtils
                .serializeToBytes(payload));
        ServerAssertion deserializedAssertion = deserializedPayload.getAssertions().get(0);

        verifyServerAssertionClass(deserializedAssertion);
    }

    public static ServerAssertion getAnonymousServerAssertion() {
        ServerAssertion assertion = new ServerAssertion() {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unused")
            @BeforeServlet
            public String get() {
                return "Test";
            }
        };

        print(assertion.getClass());

        return assertion;
    }

    public static void verifyServerAssertionClass(Object modifiedAssertion) throws Exception {
        Class<?> newClass = modifiedAssertion.getClass();

        print(newClass);

        Assert.assertNotNull("Verify default constructor", newClass.getConstructor());

        Assert.assertTrue("Verify new class is of type ServerAssertion", modifiedAssertion instanceof ServerAssertion);

        Method method = modifiedAssertion.getClass().getMethod("get");
        Assert.assertTrue("Verify Annotations are preserved", method.isAnnotationPresent(BeforeServlet.class));

        Assert.assertEquals("Test", method.invoke(modifiedAssertion));
    }

    private static void print(Class<?> clazz) {

        if (System.getProperty("arquillian.debug") != null) {

            System.out.println();
            System.out.println("Class: " + clazz.getName());
            System.out.println("SuperClass: " + clazz.getSuperclass() + " " + clazz.getSuperclass().hashCode());
            System.out.println("Interfaces: " + Arrays.asList(clazz.getInterfaces()));
            System.out.println("Fields");
            for (Field field : clazz.getDeclaredFields()) {
                printAnnotation(field);
                System.out.println("\t" + field);
            }

            System.out.println("Constructors");
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                printAnnotation(constructor);
                System.out.println("\t" + constructor);
            }

            System.out.println("Methods");
            for (Method method : clazz.getDeclaredMethods()) {
                printAnnotation(method);
                System.out.println("\t" + method);
            }
        }
    }

    public static void printAnnotation(AnnotatedElement elem) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t");
        for (Annotation annotation : elem.getAnnotations()) {
            sb.append("@" + annotation.annotationType().getSimpleName() + " ");
        }
        if (elem.getAnnotations().length > 0) {
            System.out.println(sb.toString());
        }
    }
}
