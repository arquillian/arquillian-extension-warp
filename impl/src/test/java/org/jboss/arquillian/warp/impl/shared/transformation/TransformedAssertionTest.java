package org.jboss.arquillian.warp.impl.shared.transformation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.extension.servlet.BeforeServlet;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.testutils.SeparatedClassPath;
import org.jboss.arquillian.warp.impl.testutils.SeparatedClassloader;
import org.jboss.arquillian.warp.impl.testutils.ShrinkWrapUtils;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SeparatedClassloader.class)
public class TransformedAssertionTest {

    @SeparatedClassPath
    public static JavaArchive archive() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
                .addClasses(ServerAssertion.class, RequestPayload.class, BeforeServlet.class)
                .addClasses(SerializationUtils.class)
                .addClasses(TransformedAssertionTest.class, TransformedAssertion.class, AssertionTransformationException.class);

        JavaArchive javassistArchive = ShrinkWrapUtils.getJavaArchiveFromClass(javassist.CtClass.class);
        JavaArchive junitArchive = ShrinkWrapUtils.getJavaArchiveFromClass(Test.class);

        return archive.merge(javassistArchive).merge(junitArchive);
    }

    @Test
    public void testAnonymousClass() throws Exception {

        ServerAssertion assertion = getAnonymousServerAssertion();

        TransformedAssertion transformedAssertion = new TransformedAssertion(assertion.getClass());
        Object modifiedAssertion = transformedAssertion.cloneToNew(assertion);

        verifyServerAssertionClass(modifiedAssertion);
    }

    @Test
    public void testSerialization() throws Exception {

        ServerAssertion assertion = getAnonymousServerAssertion();

        RequestPayload payload = new RequestPayload(assertion);

        RequestPayload deserializedPayload = SerializationUtils.deserializeFromBytes(SerializationUtils
                .serializeToBytes(payload));
        ServerAssertion deserializedAssertion = deserializedPayload.getAssertion();

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

        System.out.println(modifiedAssertion.getClass().getSuperclass());
        Assert.assertTrue("Verify new class is of type ServerAssertion", modifiedAssertion instanceof ServerAssertion);

        Method method = modifiedAssertion.getClass().getMethod("get");
        Assert.assertTrue("Verify Annotations are preserved", method.isAnnotationPresent(BeforeServlet.class));

        Assert.assertEquals("Test", method.invoke(modifiedAssertion));
    }

    private static void print(Class<?> clazz) {

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
