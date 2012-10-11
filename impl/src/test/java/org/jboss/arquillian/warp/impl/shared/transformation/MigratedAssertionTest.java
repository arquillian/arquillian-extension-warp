package org.jboss.arquillian.warp.impl.shared.transformation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import javassist.ClassPool;
import javassist.CtClass;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.impl.testutils.SeparateInvocator;
import org.jboss.arquillian.warp.impl.testutils.ShrinkWrapUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class MigratedAssertionTest {

    @Test
    public void testMigrate() throws Throwable {
        ServerAssertion originalAssertion = TransformedAssertionTest.getAnonymousServerAssertion();
        assertTrue(originalAssertion.getClass().isAnonymousClass());

        TransformedAssertion transformedAssertion = new TransformedAssertion(originalAssertion);
        MigratedAssertion migratedAssertion = new MigratedAssertion(transformedAssertion);

        byte[] bytecode = migratedAssertion.toBytecode();

        Class<?> migratedClass = SeparateInvocator.<MigrationTest, MigrationTestImpl>invoke(MigrationTestImpl.class,
                classPath()).process(bytecode);

        assertEquals(originalAssertion.getClass().getName(), migratedClass.getName());
    }

    public static interface MigrationTest {

        Class<?> process(byte[] migratedClassFile) throws Exception;
    }

    public static class MigrationTestImpl implements MigrationTest {
        public Class<?> process(byte[] migratedClassFile) throws Exception {

            ClassPool pool = ClassPool.getDefault();

            CtClass ctClazz = pool.makeClassIfNew(new ByteArrayInputStream(migratedClassFile));

            Class<?> clazz = ctClazz.toClass();

            return clazz;
        }
    }

    private static JavaArchive[] classPath() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class).addClasses(ShrinkWrapClassLoader.class)
                .addClasses(ServerAssertion.class, MigrationTest.class, MigrationTestImpl.class);

        JavaArchive javassistArchive = ShrinkWrapUtils.getJavaArchiveFromClass(javassist.CtClass.class);

        return new JavaArchive[] { archive, javassistArchive };
    }
}
