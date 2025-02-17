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
package org.jboss.arquillian.warp.impl.client.transformation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.impl.client.separation.SeparateInvocator;
import org.jboss.arquillian.warp.impl.client.separation.SeparatedClassLoader;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.testutils.SeparatedClassPath;
import org.jboss.arquillian.warp.impl.testutils.SeparatedClassloaderExtension;
import org.jboss.arquillian.warp.impl.utils.ClassLoaderUtils;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.arquillian.warp.impl.utils.ShrinkWrapUtils;
import org.jboss.arquillian.warp.servlet.BeforeServlet;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.ServiceExtensionLoader;
import org.jboss.shrinkwrap.spi.MemoryMapArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SeparatedClassloaderExtension.class)
public class TestTransformedInspection {

    @SeparatedClassPath
    public static JavaArchive[] archive() {
        JavaArchive archive = ShrinkWrap
            .create(JavaArchive.class)
            .addClasses(WarpCommons.class, Inspection.class, RequestPayload.class, BeforeServlet.class)
            .addClasses(SerializationUtils.class, ShrinkWrapUtils.class, ClassLoaderUtils.class)
            .addClasses(TestTransformedInspection.class, TransformedInspection.class, MigratedInspection.class,
                InspectionTransformationException.class, NoSerialVersionUIDException.class, InstanceCreator.class)
            .addClasses(SeparateInvocator.class, CtClassAsset.class,
                SeparatedClassLoader.class);

        JavaArchive javassistArchive = ShrinkWrapUtils.getJavaArchiveFromClass(javassist.CtClass.class);
        JavaArchive junitArchive = ShrinkWrapUtils.getJavaArchiveFromClass(Test.class);

        JavaArchive shrinkWrapSpi = ShrinkWrapUtils.getJavaArchiveFromClass(MemoryMapArchive.class);
        JavaArchive shrinkWrapApi = ShrinkWrapUtils.getJavaArchiveFromClass(JavaArchive.class);
        JavaArchive shrinkWrapImpl = ShrinkWrapUtils.getJavaArchiveFromClass(ServiceExtensionLoader.class);

        return new JavaArchive[] {archive, javassistArchive, junitArchive, shrinkWrapSpi, shrinkWrapApi, shrinkWrapImpl};
    }

    @Test
    public void testAnonymousClass() throws Exception {

        Inspection inspection = getAnonymousServerInspection();

        TransformedInspection transformedInspection = new TransformedInspection(inspection);
        Object modifiedInspection = transformedInspection.getTransformedInspection();

        verifyServerInspectionClass(modifiedInspection);
    }

    @Test
    public void testInnerClass() throws Exception {

        Inspection inspection = new InnerInspection();

        TransformedInspection transformedInspection = new TransformedInspection(inspection);
        Object modifiedInspection = transformedInspection.getTransformedInspection();

        verifyServerInspectionClass(modifiedInspection);
    }

    @Test
    public void testInnerClassWithArgsConstructor() throws Exception {

        Inspection inspection = new InnerWithArgsInspection("Test");

        TransformedInspection transformedInspection = new TransformedInspection(inspection);
        Object modifiedInspection = transformedInspection.getTransformedInspection();

        verifyServerInspectionClass(modifiedInspection);
    }

    @Test
    public void testInnerClassWithArgsConstructorInheritance() throws Exception {

        Inspection inspection = new InnerWithArgsInheritedInspection("Test");
        TransformedInspection transformedInspection = new TransformedInspection(inspection);
        Object modifiedInspection = transformedInspection.getTransformedInspection();

        verifyServerInspectionClass(modifiedInspection);
    }

    @Test
    public void testSerialization() throws Exception {

        Inspection inspection = getAnonymousServerInspection();

        RequestPayload payload = new RequestPayload(inspection);

        RequestPayload deserializedPayload = SerializationUtils.deserializeFromBytes(SerializationUtils
            .serializeToBytes(payload));
        Inspection deserializedInspection = deserializedPayload.getInspections().get(0);

        verifyServerInspectionClass(deserializedInspection);
    }

    public static Inspection getAnonymousServerInspection() {
        Inspection inspection = new Inspection() {
            private static final long serialVersionUID = 1L;

            @BeforeServlet
            public String get() {
                return "Test";
            }
        };

        print(inspection.getClass());

        return inspection;
    }

    public static class InnerInspection extends Inspection {
        private static final long serialVersionUID = 1L;

        @BeforeServlet
        public String get() {
            return "Test";
        }
    };

    public static class InnerWithArgsInspection extends Inspection {
        private static final long serialVersionUID = 1L;

        private String value;

        public InnerWithArgsInspection(String value) {
            this.value = value;
        }
        @BeforeServlet
        public String get() {
            return value;
        }
    };

    public static class InnerWithArgsInheritedInspection extends InnerWithArgsInspection {

        private static final long serialVersionUID = 1L;

        public InnerWithArgsInheritedInspection(String value) {
            super(value);
        }
    }

    public static void verifyServerInspectionClass(Object modifiedInspection) throws Exception {
        Class<?> newClass = modifiedInspection.getClass();

        print(newClass);

        Assertions.assertNotNull(newClass.getConstructor(), "Verify default constructor");

        Assertions.assertTrue(modifiedInspection instanceof Inspection, "Verify new class is of type ServerInspection");

        Method method = modifiedInspection.getClass().getMethod("get");
        Assertions.assertTrue(method.isAnnotationPresent(BeforeServlet.class), "Verify Annotations are preserved");

        Assertions.assertEquals("Test", method.invoke(modifiedInspection));
    }

    private static void print(Class<?> clazz) {

        if (WarpCommons.debugMode()) {

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