package org.jboss.arquillian.warp.server.assertion;

import java.io.ByteArrayInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javassist.ClassPool;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.client.execution.AssertionTransformer;
import org.jboss.arquillian.warp.extension.servlet.BeforeServlet;
import org.junit.Assert;
import org.junit.Test;

public class AssertionTransformerTestCase {
    @Test
    public void removeInnerClass() throws Exception {

        ServerAssertion assertion = new ServerAssertion() {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unused")
            @BeforeServlet
            public String get() {
                return "Test";
            }
        };

        print(assertion.getClass());

        byte[] classFile = AssertionTransformer.transform(assertion.getClass());

        ClassPool pool = ClassPool.getDefault();
        Class<?> newClass = pool.toClass(pool.makeClassIfNew(new ByteArrayInputStream(classFile)));

        print(newClass);
        // ?
        // Assert.assertTrue(
        // "Verify class is public",
        // Modifier.isPublic(newClass.getModifiers()));
        Assert.assertNotNull("Verify default constructor", newClass.getConstructor());

        Constructor<?> constructor = newClass.getConstructor();
        Object modifiedAssertion = constructor.newInstance();

        Assert.assertTrue("Verify new class is of tpye ServerAssertion", modifiedAssertion instanceof ServerAssertion);

        Method method = modifiedAssertion.getClass().getMethod("get");
        Assert.assertTrue("Verify Annotations are preserved", method.isAnnotationPresent(BeforeServlet.class));

        Assert.assertEquals("Test", method.invoke(modifiedAssertion));
    }

    private void print(Class<?> clazz) {

        System.out.println();
        System.out.println("Class: " + clazz.getName());
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

    public void printAnnotation(AnnotatedElement elem) {
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
