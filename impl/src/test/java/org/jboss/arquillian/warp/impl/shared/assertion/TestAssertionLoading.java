package org.jboss.arquillian.warp.impl.shared.assertion;

import static org.junit.Assert.assertFalse;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.impl.client.separation.SeparateInvocator;
import org.jboss.arquillian.warp.impl.client.separation.SeparatedClassLoader;
import org.jboss.arquillian.warp.impl.client.transformation.AssertionTransformationException;
import org.jboss.arquillian.warp.impl.client.transformation.CtClassAsset;
import org.jboss.arquillian.warp.impl.client.transformation.MigratedAssertion;
import org.jboss.arquillian.warp.impl.client.transformation.TransformedAssertion;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.utils.ClassLoaderUtils;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.arquillian.warp.impl.utils.ShrinkWrapUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.ServiceExtensionLoader;
import org.jboss.shrinkwrap.spi.MemoryMapArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestAssertionLoading {

    private ClassLoader originalClassLoader = null;
    private ClassLoader clientClassLoader;
    private ClassLoader serverClassLoader;

    @Before
    public void setUp() {
        clientClassLoader = separatedClassLoader(clientArchive());
        serverClassLoader = separatedClassLoader(serverArchive());

        replaceClassLoader(clientClassLoader);
    }

    @After
    public void replaceClassLoader() {
        restoreOriginalClassLoader();
    }

    @Test
    public void testStaticInnerClassOnClient() throws Throwable {
        getStaticInnerClass();
    }

    @Test
    public void testStaticInnerClassOnOnServer() throws Throwable {
        Object assertion = getStaticInnerClass();
        testOnServer(assertion);
    }

    @Test
    public void testInnerClassOnClient() throws Throwable {
        getInnerClass();
    }

    @Test
    public void testInnerClassOnOnServer() throws Throwable {
        Object assertion = getInnerClass();
        testOnServer(assertion);
    }

    @Test
    public void testAnonymousClassOnClient() throws Throwable {
        getAnonymousClass();
    }

    @Test
    public void testAnonymousClassOnOnServer() throws Throwable {
        Object assertion = getAnonymousClass();
        testOnServer(assertion);
    }

    private void testOnServer(Object assertion) throws Throwable {
        try {
            byte[] serialized = serialize(assertion);

            replaceClassLoader(serverClassLoader);
            Object deserializedPayload = deserialize(serialized);
            Method getAssertionMethod = deserializedPayload.getClass().getMethod("getAssertion");
            Object deserializedAssertion = getAssertionMethod.invoke(deserializedPayload);

            Class<?> deserializedClass = deserializedAssertion.getClass();
            Method serverMethod = deserializedAssertion.getClass().getMethod("server");
            serverMethod.invoke(deserializedAssertion);

            checkClass(deserializedClass);
        } finally {
            restoreOriginalClassLoader();
        }
    }

    private void checkClass(Class<?> clazz) {
        // check member class invocation
        assertFalse(clazz.isMemberClass());
    }

    private void replaceClassLoader(ClassLoader classLoader) {
        if (originalClassLoader == null) {
            originalClassLoader = Thread.currentThread().getContextClassLoader();
        }
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    private void restoreOriginalClassLoader() {
        if (originalClassLoader != null) {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    private Object getStaticInnerClass() throws Throwable {

        Class<?> clazz = clientClassLoader.loadClass(SharingClass.class.getName());
        Object instance = clazz.newInstance();
        Method method = clazz.getMethod("getStaticInnerClass");

        // when
        Object shared = method.invoke(instance);
        return shared;
    }

    private Object getInnerClass() throws Throwable {

        Class<?> clazz = clientClassLoader.loadClass(SharingClass.class.getName());
        Object instance = clazz.newInstance();
        Method method = clazz.getMethod("getInnerClass");

        // when
        Object shared = method.invoke(instance);
        return shared;
    }

    private Object getAnonymousClass() throws Throwable {

        Class<?> clazz = clientClassLoader.loadClass(SharingClass.class.getName());
        Object instance = clazz.newInstance();
        Method method = clazz.getMethod("getAnonymousClass");

        // when
        Object shared = method.invoke(instance);
        return shared;
    }

    private byte[] serialize(Object object) throws Throwable {
        Class<?> serializationUtilsClass = serializationUtils(clientClassLoader);
        Method serializeToBytes = serializationUtilsClass.getMethod("serializeToBytes", Serializable.class);
        byte[] serialized = (byte[]) serializeToBytes.invoke(null, object);
        return serialized;
    }

    private Object deserialize(byte[] bytes) throws Throwable {
        Class<?> serializationUtilsClass = serializationUtils(serverClassLoader);
        Method deserializeFromBytes = serializationUtilsClass.getMethod("deserializeFromBytes", (new byte[0]).getClass());
        Object deserialized = (Object) deserializeFromBytes.invoke(null, bytes);
        return deserialized;
    }

    private Class<?> serializationUtils(ClassLoader classLoader) throws Throwable {
        Class<?> serializationUtilsClass = classLoader.loadClass(SerializationUtils.class.getName());
        return serializationUtilsClass;
    }

    private static JavaArchive[] clientArchive() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
                .addClasses(ClientInterface.class, ClientImplementation.class).addClasses(ServerInterface.class)
                .addClasses(SharingClass.class, ServerAssertion.class, RequestPayload.class)
                .addClasses(TransformedAssertion.class, MigratedAssertion.class, AssertionTransformationException.class)
                .addClasses(SerializationUtils.class, ShrinkWrapUtils.class, ClassLoaderUtils.class)
                .addClasses(SeparateInvocator.class, CtClassAsset.class, SeparatedClassLoader.class);

        JavaArchive javassistArchive = ShrinkWrapUtils.getJavaArchiveFromClass(javassist.CtClass.class);

        JavaArchive shrinkWrapSpi = ShrinkWrapUtils.getJavaArchiveFromClass(MemoryMapArchive.class);
        JavaArchive shrinkWrapApi = ShrinkWrapUtils.getJavaArchiveFromClass(JavaArchive.class);
        JavaArchive shrinkWrapImpl = ShrinkWrapUtils.getJavaArchiveFromClass(ServiceExtensionLoader.class);

        return new JavaArchive[] { archive, javassistArchive, shrinkWrapSpi, shrinkWrapApi, shrinkWrapImpl };
    }

    private static JavaArchive[] serverArchive() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class).addClasses(ClientInterface.class)
                .addClasses(ServerInterface.class, ServerImplemenation.class)
                .addClasses(ServerAssertion.class, RequestPayload.class).addClasses(SerializationUtils.class);
        

        return new JavaArchive[] { archive };
    }

    private ClassLoader separatedClassLoader(JavaArchive... archive) {
        return new ShrinkWrapClassLoader(ClassLoaderUtils.getBootstrapClassLoader(), archive);
    }

}
