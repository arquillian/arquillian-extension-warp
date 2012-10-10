package org.jboss.arquillian.warp.server.assertion;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.arquillian.warp.testutils.SeparatedClassloader;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Test;

public class TestAssertionLoading {

    private ClassLoader originalClassLoader = null;

    @Test
    public void testStaticInnerClassOnClient() throws Throwable {
        try {
            // having
            ClassLoader clientClassLoader = SeparatedClassloader.getShrinkWrapClassLoader(clientArchive(), SharingClass.class);

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
            ClassLoader clientClassLoader = SeparatedClassloader.getShrinkWrapClassLoader(clientArchive(), SharingClass.class);
            ClassLoader serverClassLoader = SeparatedClassloader.getShrinkWrapClassLoader(serverArchive(), SharingClass.class);

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
            ClassLoader clientClassLoader = SeparatedClassloader.getShrinkWrapClassLoader(clientArchive(), SharingClass.class);

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
            ClassLoader clientClassLoader = SeparatedClassloader.getShrinkWrapClassLoader(clientArchive(), SharingClass.class);
            ClassLoader serverClassLoader = SeparatedClassloader.getShrinkWrapClassLoader(serverArchive(), SharingClass.class);

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
            ClassLoader clientClassLoader = SeparatedClassloader.getShrinkWrapClassLoader(clientArchive(), SharingClass.class);

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
            ClassLoader clientClassLoader = SeparatedClassloader.getShrinkWrapClassLoader(clientArchive(), SharingClass.class);
            ClassLoader serverClassLoader = SeparatedClassloader.getShrinkWrapClassLoader(serverArchive(), SharingClass.class);

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

    private static JavaArchive clientArchive() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
                .addClasses(ClientInterface.class, ClientImplementation.class).addClasses(ServerInterface.class)
                .addClasses(SharingClass.class, ServerAssertion.class, RequestPayload.class)
                .addClasses(SerializationUtils.class);

        JavaArchive javassist = DependencyResolvers.use(MavenDependencyResolver.class)
                .artifact("javassist:javassist:3.12.1.GA").resolveAs(JavaArchive.class).iterator().next();

        return archive.merge(javassist);
    }

    private static JavaArchive serverArchive() {
        return ShrinkWrap.create(JavaArchive.class).addClasses(ClientInterface.class)
                .addClasses(ServerInterface.class, ServerImplemenation.class)
                .addClasses(ServerAssertion.class, RequestPayload.class).addClasses(SerializationUtils.class);
    }

}
