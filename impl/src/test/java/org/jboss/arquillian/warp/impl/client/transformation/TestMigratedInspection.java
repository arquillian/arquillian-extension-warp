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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import javassist.ClassPool;
import javassist.CtClass;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.impl.client.separation.SeparateInvocator;
import org.jboss.arquillian.warp.impl.utils.ShrinkWrapUtils;
import org.jboss.arquillian.warp.servlet.BeforeServlet;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;

public class TestMigratedInspection {

    @Test
    public void testMigrate() throws Throwable {
        Inspection originalInspection = TestMigratedInspection.getAnonymousServerInspection();
        assertTrue(originalInspection.getClass().isAnonymousClass());

        TransformedInspection transformedInspection = new TransformedInspection(originalInspection);
        MigratedInspection migratedInspection = new MigratedInspection(transformedInspection);

        byte[] bytecode = migratedInspection.toBytecode();

        Class<?> migratedClass = SeparateInvocator.<MigrationTest, MigrationTestImpl>invoke(MigrationTestImpl.class,
            classPath()).process(bytecode);

        assertEquals(originalInspection.getClass().getName(), migratedClass.getName());
    }

    public interface MigrationTest {

        Class<?> process(byte[] migratedClassFile) throws Exception;
    }

    public static class MigrationTestImpl implements MigrationTest {
        public Class<?> process(byte[] migratedClassFile) throws Exception {

            ClassPool pool = ClassPool.getDefault();

            CtClass ctClazz = pool.makeClassIfNew(new ByteArrayInputStream(migratedClassFile));

            Class<?> clazz = ctClazz.toClass();

            return clazz;
        }
    }

    private static JavaArchive[] classPath() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class).addClasses(ShrinkWrapClassLoader.class)
            .addClasses(Inspection.class, MigrationTest.class, MigrationTestImpl.class);

        JavaArchive javassistArchive = ShrinkWrapUtils.getJavaArchiveFromClass(javassist.CtClass.class);

        return new JavaArchive[] {archive, javassistArchive};
    }

    // Same code in impl_test_separatecl/src/test/java/org/jboss/arquillian/warp/impl/client/transformation

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
