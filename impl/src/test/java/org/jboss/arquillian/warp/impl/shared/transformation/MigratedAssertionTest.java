package org.jboss.arquillian.warp.impl.shared.transformation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.warp.ServerAssertion;
import org.junit.Test;

public class MigratedAssertionTest {

    @Test
    public void testMigrate() throws Throwable {
        ServerAssertion originalAssertion = TransformedAssertionTest.getAnonymousServerAssertion();
        assertTrue(originalAssertion.getClass().isAnonymousClass());

        TransformedAssertion transformedAssertion = new TransformedAssertion(originalAssertion);
        MigratedAssertion migratedAssertion = new MigratedAssertion(transformedAssertion);

        byte[] bytecode = migratedAssertion.toBytecode();

        Class<?> migratedClass = MigrationTestRunnable.invokeSeparately(bytecode);
        assertEquals(originalAssertion.getClass().getName(), migratedClass.getName());
    }
}
