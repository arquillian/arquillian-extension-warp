package org.jboss.arquillian.warp.impl.shared.assertion;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.shared.transformation.AssertionTransformationException;
import org.jboss.arquillian.warp.impl.shared.transformation.MigratedAssertion;
import org.jboss.arquillian.warp.impl.shared.transformation.MigrationRunnable;
import org.jboss.arquillian.warp.impl.shared.transformation.TransformedAssertion;
import org.jboss.arquillian.warp.impl.testutils.ClassLoaderUtils;
import org.jboss.arquillian.warp.impl.testutils.CtClassAsset;
import org.jboss.arquillian.warp.impl.testutils.SeparateInvocator;
import org.jboss.arquillian.warp.impl.testutils.ShrinkWrapUtils;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.ServiceExtensionLoader;
import org.jboss.shrinkwrap.spi.MemoryMapArchive;
import org.junit.Test;

public class TestAssertionLoading {

    private ClassLoader originalClassLoader = null;

    @Test
    public void testStaticInnerClassOnClient() throws Throwable {
        try {
            // having
            ClassLoader clientClassLoader = separatedClassLoader(clientArchive());

            // when
            replaceClassLoader(clientClassLoader);
            getStaticInnerClass(clientClassLoader);
        } finally {
            restoreOriginalClassLoader();
        }
    }

    @Test
    public void testStaticInnerClassOnOnServer() throws Throwable {
        try {
            // having
            ClassLoader clientClassLoader = separatedClassLoader(clientArchive());
            ClassLoader serverClassLoader = separatedClassLoader(serverArchive());

            replaceClassLoader(clientClassLoader);
            Object shared = getStaticInnerClass(clientClassLoader);
            byte[] serialized = serialize(clientClassLoader, shared);

            // when
            replaceClassLoader(serverClassLoader);
            Object deserializedPayload = deserialize(serverClassLoader, serialized);
            Method getAssertionMethod = deserializedPayload.getClass().getMethod("getAssertion");
            Object deserializedAssertion = getAssertionMethod.invoke(deserializedPayload);

            Method serverMethod = deserializedAssertion.getClass().getMethod("server");
            serverMethod.invoke(deserializedAssertion);
        } finally {
            restoreOriginalClassLoader();
        }
    }

    @Test
    public void testInnerClassOnClient() throws Throwable {
        try {
            // having
            ClassLoader clientClassLoader = separatedClassLoader(clientArchive());

            // when
            replaceClassLoader(clientClassLoader);
            getInnerClass(clientClassLoader);
        } finally {
            restoreOriginalClassLoader();
        }
    }

    @Test
    public void testInnerClassOnOnServer() throws Throwable {
        try {
            // having
            ClassLoader clientClassLoader = separatedClassLoader(clientArchive());
            ClassLoader serverClassLoader = separatedClassLoader(serverArchive());

            replaceClassLoader(clientClassLoader);
            Object shared = getInnerClass(clientClassLoader);
            byte[] serialized = serialize(clientClassLoader, shared);

            // when
            replaceClassLoader(serverClassLoader);
            Object deserializedPayload = deserialize(serverClassLoader, serialized);
            Method getAssertionMethod = deserializedPayload.getClass().getMethod("getAssertion");
            Object deserializedAssertion = getAssertionMethod.invoke(deserializedPayload);

            Method serverMethod = deserializedAssertion.getClass().getMethod("server");
            serverMethod.invoke(deserializedAssertion);
        } finally {
            restoreOriginalClassLoader();
        }
    }

    @Test
    public void testAnonymousClassOnClient() throws Throwable {
        try {
            // having
            ClassLoader clientClassLoader = separatedClassLoader(clientArchive());

            // when
            replaceClassLoader(clientClassLoader);

            // when
            getAnonymousClass(clientClassLoader);
        } finally {
            restoreOriginalClassLoader();
        }
    }

    @Test
    public void testAnonymousClassOnOnServer() throws Throwable {

        try {
            // having
            ClassLoader clientClassLoader = separatedClassLoader(clientArchive());
            ClassLoader serverClassLoader = separatedClassLoader(serverArchive());

            replaceClassLoader(clientClassLoader);
            Object shared = getAnonymousClass(clientClassLoader);
            byte[] serialized = serialize(clientClassLoader, shared);

            // when
            replaceClassLoader(serverClassLoader);
            Object deserializedPayload = deserialize(serverClassLoader, serialized);
            Method getAssertionMethod = deserializedPayload.getClass().getMethod("getAssertion");
            Object deserializedAssertion = getAssertionMethod.invoke(deserializedPayload);

            Method serverMethod = deserializedAssertion.getClass().getMethod("server");
            serverMethod.invoke(deserializedAssertion);
        } finally {
            restoreOriginalClassLoader();
        }
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

    private Object getStaticInnerClass(ClassLoader classLoader) throws Throwable {

        Class<?> clazz = classLoader.loadClass(SharingClass.class.getName());
        Object instance = clazz.newInstance();
        Method method = clazz.getMethod("getStaticInnerClass");

        // when
        Object shared = method.invoke(instance);
        return shared;
    }

    private Object getInnerClass(ClassLoader classLoader) throws Throwable {

        Class<?> clazz = classLoader.loadClass(SharingClass.class.getName());
        Object instance = clazz.newInstance();
        Method method = clazz.getMethod("getInnerClass");

        // when
        Object shared = method.invoke(instance);
        return shared;
    }

    private Object getAnonymousClass(ClassLoader classLoader) throws Throwable {

        Class<?> clazz = classLoader.loadClass(SharingClass.class.getName());
        Object instance = clazz.newInstance();
        Method method = clazz.getMethod("getAnonymousClass");

        // when
        Object shared = method.invoke(instance);
        return shared;
    }

    private byte[] serialize(ClassLoader classLoader, Object object) throws Throwable {
        Class<?> serializationUtilsClass = serializationUtils(classLoader);
        Method serializeToBytes = serializationUtilsClass.getMethod("serializeToBytes", Serializable.class);
        byte[] serialized = (byte[]) serializeToBytes.invoke(null, object);
        return serialized;
    }

    private Object deserialize(ClassLoader classLoader, byte[] bytes) throws Throwable {
        Class<?> serializationUtilsClass = serializationUtils(classLoader);
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
                .addClasses(TransformedAssertion.class, MigratedAssertion.class, MigrationRunnable.class, AssertionTransformationException.class)
                .addClasses(SerializationUtils.class, ShrinkWrapUtils.class, ClassLoaderUtils.class)
                .addClasses(SeparateInvocator.class, CtClassAsset.class);

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
