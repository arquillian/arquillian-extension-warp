package org.jboss.arquillian.warp.impl.shared.transformation;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

import javassist.ClassPool;
import javassist.CtClass;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.impl.testutils.CtClassAsset;
import org.jboss.arquillian.warp.impl.testutils.SeparateInvocator;
import org.jboss.arquillian.warp.impl.testutils.ShrinkWrapUtils;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.NamedAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class MigratedAssertion {

    private TransformedAssertion transformed;
    private MigrationResult migrated;

    public MigratedAssertion(TransformedAssertion transformed) throws AssertionTransformationException {
        this.transformed = transformed;
        this.migrated = migrate();
    }

    private MigrationResult migrate() throws AssertionTransformationException {
        byte[] oldClassFile = transformed.toBytecode();
        String oldClassName = transformed.getTransformedClass().getName();
        String newClassName = transformed.getOriginalClass().getName();
        ServerAssertion serverAssertion = transformed.getTransformedAssertion();
        NamedAsset transformedAsset = transformed.toShrinkWrapAsset();

        try {

            JavaArchive baseArchive = ShrinkWrap
                    .create(JavaArchive.class)
                    .addClasses(MigratedAssertion.class, MigratedAssertion.MigrationImpl.class,
                            MigratedAssertion.Migration.class, MigratedAssertion.MigrationResult.class, ServerAssertion.class)
                    .add(transformedAsset);
            JavaArchive javassistArchive = ShrinkWrapUtils.getJavaArchiveFromClass(javassist.ClassPath.class);

            return SeparateInvocator.<Migration, MigrationImpl>invoke(MigrationImpl.class, baseArchive, javassistArchive)
                    .process(oldClassName, newClassName, oldClassFile, serverAssertion);
        } catch (Throwable e) {
            throw new IllegalStateException("Cannot migrate transformed assertion back to original name", e);
        }
    }

    public byte[] toBytecode() {
        return migrated.bytecode;
    }

    public byte[] toSerializedForm() {
        return migrated.serializedMigratedAssertion;
    }

    public static interface Migration {
        MigrationResult process(String oldClassName, String newClassName, byte[] oldClassFile,
                ServerAssertion transformedAssertion) throws Exception;
    }

    public static class MigrationImpl implements Migration {

        @Override
        public MigrationResult process(String oldClassName, String newClassName, byte[] oldClassFile,
                ServerAssertion transformedAssertion) throws Exception {

            MigrationResult result = new MigrationResult();

            ClassPool pool = ClassPool.getDefault();

            CtClass clazz = pool.makeClassIfNew(new ByteArrayInputStream(oldClassFile));

            clazz.replaceClassName(oldClassName, newClassName);

            Class<Serializable> migratedClass = clazz.toClass();

            result.bytecode = clazz.toBytecode();

            try {
                Class<?> oldClass = transformedAssertion.getClass();
                Serializable migratedAssertion = migratedClass.newInstance();
                for (Field newF : migratedClass.getDeclaredFields()) {
                    if (java.lang.reflect.Modifier.isStatic(newF.getModifiers())
                            && java.lang.reflect.Modifier.isFinal(newF.getModifiers())) {
                        continue;
                    }
                    Field oldF = oldClass.getDeclaredField(newF.getName());
                    oldF.setAccessible(true);
                    newF.setAccessible(true);
                    newF.set(migratedAssertion, oldF.get(transformedAssertion));
                }
                result.serializedMigratedAssertion = SerializationUtils.serializeToBytes(migratedAssertion);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to clone " + transformedAssertion.getClass().getName()
                        + " to migrated class " + migratedClass.getName(), e);
            }

            return result;
        }
    }

    public static class MigrationResult implements Serializable {
        public byte[] bytecode;
        public byte[] serializedMigratedAssertion;
    }
}
