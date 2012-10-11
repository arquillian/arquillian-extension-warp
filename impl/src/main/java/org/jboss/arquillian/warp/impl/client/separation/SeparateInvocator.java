package org.jboss.arquillian.warp.impl.client.separation;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.jboss.arquillian.warp.impl.utils.ClassLoaderUtils;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class SeparateInvocator<T> {

    private ClassLoader separatedClassLoader;
    private Class<T> clazz;
    private Class<?> separatedClass;
    private Object instance;
    private InvocationHandler handler;

    private SeparateInvocator(Class<T> clazz, ClassLoader separatedClassLoader) {
        this.separatedClassLoader = separatedClassLoader;
        this.clazz = clazz;

        this.instance = instantiate();
        this.handler = new SeparationHandler();
    }

    public static <I, T extends I> I invoke(Class<T> clazz, JavaArchive... classPathArchives) {
        JavaArchive[] copy = Arrays.copyOf(classPathArchives, classPathArchives.length + 1);
        copy[copy.length - 1] = ShrinkWrap.create(JavaArchive.class).addClass(SerializationUtils.class);

        ClassLoader separatedClassLoader = new ShrinkWrapClassLoader(ClassLoaderUtils.getBootstrapClassLoader(), copy);
        return invoke(clazz, separatedClassLoader);
    }

    @SuppressWarnings("unchecked")
    public static <I, T extends I> I invoke(Class<T> clazz, ClassLoader separatedClassLoader) {
        SeparateInvocator<T> magic = new SeparateInvocator<T>(clazz, separatedClassLoader);

        Class<?>[] interfaces = clazz.getInterfaces();

        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, magic.handler);
    }

    @SuppressWarnings("unchecked")
    private static <R, T> R invokeStatic(ClassLoader separatedClassLoader, Class<T> clazz, Method method, Object... args) {
        SeparateInvocator<T> magic = new SeparateInvocator<T>(clazz, separatedClassLoader);

        Method adoptedMethod = magic.adoptMethod(method);
        Object[] adoptedArgs = magic.adaptArgs(args, Thread.currentThread().getContextClassLoader(), separatedClassLoader);

        try {
            return (R) adoptedMethod.invoke(null, adoptedArgs);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to invoke static method " + method.getName() + " from class "
                    + clazz.getName(), e);
        }

    }

    private class SeparationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, final Method method, Object[] args) throws Throwable {

            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

            final Object[] adoptedArgs = adaptArgs(args, contextClassLoader, separatedClassLoader);
            final Method adoptedMethod = adoptMethod(method);

            Object result = new InvokeSeparately<Object>() {

                @Override
                public Object invoke() {
                    try {
                        return adoptedMethod.invoke(instance, adoptedArgs);
                    } catch (Exception e) {
                        throw new IllegalStateException("Unable to invoke method separately", e);
                    }
                }
            }.run(separatedClassLoader);

            Object adoptedResult = adapt(result, separatedClassLoader, contextClassLoader);

            return adoptedResult;
        }
    }

    private Method adoptMethod(Method method) {
        try {
            return separatedClass.getMethod(method.getName(), adoptMethodParameterTypes(method.getParameterTypes()));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot find method " + method.getName() + " with arguments "
                    + Arrays.asList(method.getParameterTypes()) + " on class " + separatedClass.getName()
                    + " loaded on separated class loader");
        }
    }

    private Class<?>[] adoptMethodParameterTypes(Class<?>[] parameterTypes) {
        Class<?>[] adopted = new Class<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            try {
                adopted[i] = adoptType(parameterTypes[i]);
            } catch (Exception e) {
                throw new IllegalStateException("Cannot adopt method parameter type " + parameterTypes[i], e);
            }
        }
        return adopted;
    }

    private Class<?> adoptType(Class<?> type) {
        try {
            if (type.isPrimitive()) {
                return type;
            } else if (type.isArray()) {
                Class<?> componentType = type.getComponentType();

                Class<?> adoptedComponentType = adoptType(componentType);

                return Array.newInstance(adoptedComponentType, 0).getClass();
            } else {
                return loadSeparatedClassSafely(type);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to adopt type of " + type.getName(), e);
        }
    }

    private Object instantiate() {
        try {
            separatedClass = loadSeparatedClassSafely(clazz);
            return separatedClass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to instantiate class " + clazz.getName() + " on separated classloader", e);
        }
    }

    private Object[] adaptArgs(Object[] args, ClassLoader from, ClassLoader to) {
        Object[] adapted = new Object[args.length];

        for (int i = 0; i < adapted.length; i++) {
            adapted[i] = adapt(args[i], from, to);
        }

        return adapted;
    }

    private Object adapt(final Object object, final ClassLoader from, final ClassLoader to) {

        if (from == to) {
            return object;
        }

        if (object.getClass().getName().startsWith("java.")) {
            return object;
        }

        if (object instanceof Serializable) {
            final Method serializeMethod = getMethodSafely(SerializationUtils.class, "serializeToBytes", Serializable.class);
            final Method deserializeMethod = getMethodSafely(SerializationUtils.class, "deserializeFromBytes",
                    new byte[0].getClass());

            final byte[] serialized = new InvokeSeparately<byte[]>() {

                @Override
                public byte[] invoke() {
                    // return SerializationUtils.serializeToBytes((Serializable) object);
                    return invokeStatic(from, SerializationUtils.class, serializeMethod, object);
                }
            }.run(from);

            final Object deserialized = new InvokeSeparately<Object>() {

                @Override
                public Object invoke() {
                    // return SerializationUtils.deserializeFromBytes(serialized);
                    return invokeStatic(to, SerializationUtils.class, deserializeMethod, serialized);
                }
            }.run(to);

            return deserialized;
        }

        throw new IllegalStateException("Unable to adapt instance of " + object.getClass().getName());
    }

    private abstract class InvokeSeparately<R> {
        public R run(ClassLoader separatedClassLoader) {
            final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

            try {
                Thread.currentThread().setContextClassLoader(separatedClassLoader);

                return invoke();
            } finally {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
            }
        }

        public abstract R invoke();
    }

    private static Method getMethodSafely(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot obtain method " + methodName + " from class " + clazz.getName(), e);
        }
    }

    private Class<?> loadSeparatedClassSafely(Class<?> clazz) {
        try {
            String className = clazz.getName();
            return separatedClassLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Class " + clazz.getName() + " wasn't found on separated class loader", e);
        }
    }
}
