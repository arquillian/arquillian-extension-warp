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
package org.jboss.arquillian.warp.impl.shared.inspection;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.impl.client.separation.SeparateInvocator;
import org.jboss.arquillian.warp.impl.client.separation.SeparatedClassLoader;
import org.jboss.arquillian.warp.impl.client.transformation.CtClassAsset;
import org.jboss.arquillian.warp.impl.client.transformation.InspectionTransformationException;
import org.jboss.arquillian.warp.impl.client.transformation.InstanceCreator;
import org.jboss.arquillian.warp.impl.client.transformation.MigratedInspection;
import org.jboss.arquillian.warp.impl.client.transformation.NoSerialVersionUIDException;
import org.jboss.arquillian.warp.impl.client.transformation.TransformedInspection;
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

import static org.junit.Assert.assertFalse;

public class TestInspectionLoading {

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
        Object inspection = getStaticInnerClass();
        testOnServer(inspection);
    }

    @Test
    public void testInnerClassOnClient() throws Throwable {
        getInnerClass();
    }

    @Test
    public void testInnerClassOnOnServer() throws Throwable {
        Object inspection = getInnerClass();
        testOnServer(inspection);
    }

    @Test
    public void testAnonymousClassOnClient() throws Throwable {
        getAnonymousClass();
    }

    @Test
    public void testAnonymousClassOnOnServer() throws Throwable {
        Object inspection = getAnonymousClass();
        testOnServer(inspection);
    }

    private void testOnServer(Object inspection) throws Throwable {
        try {
            byte[] serialized = serialize(inspection);

            replaceClassLoader(serverClassLoader);
            Object deserializedPayload = deserialize(serialized);
            Method getInspectionsMethod = deserializedPayload.getClass().getMethod("getInspections");
            List<?> deserializedInspectionList = (List<?>) getInspectionsMethod.invoke(deserializedPayload);
            Object deserializedInspection = deserializedInspectionList.iterator().next();

            Class<?> deserializedClass = deserializedInspection.getClass();
            Method serverMethod = deserializedInspection.getClass().getMethod("server");
            serverMethod.invoke(deserializedInspection);

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
        JavaArchive archive = ShrinkWrap
            .create(JavaArchive.class)
            .addClasses(ClientInterface.class, ClientImplementation.class)
            .addClasses(ServerInterface.class)
            .addClasses(SharingClass.class, Inspection.class, RequestPayload.class)
            .addClasses(TransformedInspection.class, MigratedInspection.class, InspectionTransformationException.class,
                NoSerialVersionUIDException.class, InstanceCreator.class)
            .addClasses(SerializationUtils.class, ShrinkWrapUtils.class, ClassLoaderUtils.class)
            .addClasses(SeparateInvocator.class, CtClassAsset.class, SeparatedClassLoader.class);

        JavaArchive javassistArchive = ShrinkWrapUtils.getJavaArchiveFromClass(javassist.CtClass.class);

        JavaArchive shrinkWrapSpi = ShrinkWrapUtils.getJavaArchiveFromClass(MemoryMapArchive.class);
        JavaArchive shrinkWrapApi = ShrinkWrapUtils.getJavaArchiveFromClass(JavaArchive.class);
        JavaArchive shrinkWrapImpl = ShrinkWrapUtils.getJavaArchiveFromClass(ServiceExtensionLoader.class);

        return new JavaArchive[] {archive, javassistArchive, shrinkWrapSpi, shrinkWrapApi, shrinkWrapImpl};
    }

    private static JavaArchive[] serverArchive() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class).addClasses(ClientInterface.class)
            .addClasses(ServerInterface.class, ServerImplemenation.class)
            .addClasses(Inspection.class, RequestPayload.class).addClasses(SerializationUtils.class);

        return new JavaArchive[] {archive};
    }

    private ClassLoader separatedClassLoader(JavaArchive... archive) {
        return new ShrinkWrapClassLoader(ClassLoaderUtils.getBootstrapClassLoader(), archive);
    }
}
