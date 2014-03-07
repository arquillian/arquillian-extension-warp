/**
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

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javassist.ClassPool;
import javassist.CtClass;

import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.impl.client.separation.SeparateInvocator;
import org.jboss.arquillian.warp.impl.client.separation.SeparatedClassLoader;
import org.jboss.arquillian.warp.impl.utils.ClassLoaderUtils;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.NamedAsset;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * <p>
 * Takes {@link TransformedInspection} and renames it to original name, providing bytecode of transformed and renamed class.
 * </p>
 *
 * <p>
 * In order to allow renaming transformed class to the name of original class, {@link MigratedInspection} uses
 * {@link SeparatedClassLoader} internally.
 * </p>
 *
 * @author Lukas Fryc
 */
public class MigratedInspection {

    private TransformedInspection transformed;
    private MigrationResult migrated;

    public MigratedInspection(TransformedInspection transformed) throws InspectionTransformationException {
        this.transformed = transformed;
        this.migrated = migrate();
    }

    private MigrationResult migrate() throws InspectionTransformationException {
        byte[] oldClassFile = transformed.toBytecode();
        String oldClassName = transformed.getTransformedClass().getName();
        String newClassName = transformed.getOriginalClass().getName();
        Inspection serverInspection = transformed.getTransformedInspection();
        NamedAsset transformedAsset = transformed.toShrinkWrapAsset();

        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

            JavaArchive archive = ShrinkWrap.create(JavaArchive.class).add(transformedAsset);
            ShrinkWrapClassLoader shrinkWrapClassLoader = new ShrinkWrapClassLoader(ClassLoaderUtils.getBootstrapClassLoader(),
                    archive);

            SeparatedClassLoader separatedClassLoader = new SeparatedClassLoader(shrinkWrapClassLoader, contextClassLoader);

            return SeparateInvocator.<Migration, MigrationImpl>invoke(MigrationImpl.class, separatedClassLoader).process(
                    oldClassName, newClassName, oldClassFile, serverInspection);
        } catch (Throwable e) {
            throw new IllegalStateException("Cannot migrate transformed inspection back to original name", e);
        }
    }

    public byte[] toBytecode() {
        return migrated.bytecode;
    }

    public byte[] toSerializedForm() {
        return migrated.serializedMigratedInspection;
    }

    public interface Migration {
        MigrationResult process(String oldClassName, String newClassName, byte[] oldClassFile,
                Inspection transformedInspection) throws Exception;
    }

    public static class MigrationImpl implements Migration {

        @Override
        public MigrationResult process(String oldClassName, String newClassName, byte[] oldClassFile,
                Inspection transformedInspection) throws Exception {

            MigrationResult result = new MigrationResult();

            ClassPool pool = new ClassPool();

            CtClass clazz = pool.makeClassIfNew(new ByteArrayInputStream(oldClassFile));

            clazz.replaceClassName(oldClassName, newClassName);

            @SuppressWarnings("unchecked")
            Class<Serializable> migratedClass = (Class<Serializable>) clazz.toClass();

            result.bytecode = clazz.toBytecode();

            try {
                Class<?> oldClass = transformedInspection.getClass();
                Serializable migratedInspection = (Serializable)createInstance(migratedClass);
                for (Field newF : migratedClass.getDeclaredFields()) {
                    if (java.lang.reflect.Modifier.isStatic(newF.getModifiers())
                            && java.lang.reflect.Modifier.isFinal(newF.getModifiers())) {
                        continue;
                    }
                    Field oldF = oldClass.getDeclaredField(newF.getName());
                    oldF.setAccessible(true);
                    newF.setAccessible(true);
                    newF.set(migratedInspection, oldF.get(transformedInspection));
                }
                result.serializedMigratedInspection = SerializationUtils.serializeToBytes(migratedInspection);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to clone " + transformedInspection.getClass().getName()
                        + " to migrated class " + migratedClass.getName(), e);
            }

            return result;
        }

        private static Object createInstance(Class<?> clazz) {
            Object instance = createUnSafeInstance(clazz);
            if(instance == null) {
                try {
                    instance = clazz.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Could not create new instance of Transformed class: " + clazz.getName(), e);
                }
            }
            return instance;
        }

        private static Object createUnSafeInstance(Class<?> clazz) {
            Object unsafe = getUnsafe();
            if(unsafe == null) {
                return null;
            }
            try {
                Method newInstance = unsafe.getClass().getMethod("allocateInstance", new Class[] {Class.class});
                return newInstance.invoke(unsafe, clazz);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private static Object getUnsafe() {
            try {
                Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                Field field = unsafeClass.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                return field.get(null);
            }
            catch(Exception e) {
                return null;
            }
        }
    }

    public static class MigrationResult implements Serializable {
        private static final long serialVersionUID = 1L;
        public byte[] bytecode;
        public byte[] serializedMigratedInspection;
    }
}
