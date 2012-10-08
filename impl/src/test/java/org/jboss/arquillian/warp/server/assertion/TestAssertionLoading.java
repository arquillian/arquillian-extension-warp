package org.jboss.arquillian.warp.server.assertion;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.arquillian.warp.testutils.SeparatedClassloader;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class TestAssertionLoading {

    @Test
    public void testStaticInnerClassOnClient() throws Throwable {

        // having
        ClassLoader classLoader = SeparatedClassloader.getShrinkWrapClassLoader(clientArchive(), SharingClass.class);

        // when
        getStaticInnerClass(classLoader);
    }

    @Test
    public void testStaticInnerClassOnOnServer() throws Throwable {
        // having
        ClassLoader clientClassLoader = SeparatedClassloader.getShrinkWrapClassLoader(clientArchive(), SharingClass.class);
        ClassLoader serverClassLoader = SeparatedClassloader.getShrinkWrapClassLoader(serverArchive(), SharingClass.class);
        Object shared = getStaticInnerClass(clientClassLoader);
        byte[] serialized = serialize(clientClassLoader, shared);
        
        // when
        Object deserialized = deserialize(serverClassLoader, serialized);
        Method serverMethod = deserialized.getClass().getMethod("server");
        serverMethod.invoke(deserialized);
    }
    
    @Test
    public void testInnerClassOnClient() throws Throwable {

        // having
        ClassLoader classLoader = SeparatedClassloader.getShrinkWrapClassLoader(clientArchive(), SharingClass.class);

        // when
        getInnerClass(classLoader);
    }
    
    @Test
    public void testInnerClassOnOnServer() throws Throwable {
        // having
        ClassLoader clientClassLoader = SeparatedClassloader.getShrinkWrapClassLoader(clientArchive(), SharingClass.class);
        ClassLoader serverClassLoader = SeparatedClassloader.getShrinkWrapClassLoader(serverArchive(), SharingClass.class);
        Object shared = getInnerClass(clientClassLoader);
        byte[] serialized = serialize(clientClassLoader, shared);
        
        // when
        Object deserialized = deserialize(serverClassLoader, serialized);
        Method serverMethod = deserialized.getClass().getMethod("server");
        serverMethod.invoke(deserialized);
    }
    
    @Test
    public void testAnonymousClassOnClient() throws Throwable {

        // having
        ClassLoader classLoader = SeparatedClassloader.getShrinkWrapClassLoader(clientArchive(), SharingClass.class);

        // when
        getAnonymousClass(classLoader);
    }
    
    @Test
    public void testAnonymousClassOnOnServer() throws Throwable {
        // having
        ClassLoader clientClassLoader = SeparatedClassloader.getShrinkWrapClassLoader(clientArchive(), SharingClass.class);
        ClassLoader serverClassLoader = SeparatedClassloader.getShrinkWrapClassLoader(serverArchive(), SharingClass.class);
        Object shared = getAnonymousClass(clientClassLoader);
        byte[] serialized = serialize(clientClassLoader, shared);
        
        // when
        Object deserialized = deserialize(serverClassLoader, serialized);
        Method serverMethod = deserialized.getClass().getMethod("server");
        serverMethod.invoke(deserialized);
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
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(ClientInterface.class, ClientImplementation.class)
                .addClasses(ServerInterface.class)
                .addClasses(SharingClass.class, ServerAssertion.class)
                .addClasses(SerializationUtils.class);
    }

    private static JavaArchive serverArchive() {
        return ShrinkWrap.create(JavaArchive.class).addClasses(ClientInterface.class)
                .addClasses(ServerInterface.class, ServerImplemenation.class)
                .addClasses(ServerAssertion.class)
                .addClasses(SerializationUtils.class);
    }

}
