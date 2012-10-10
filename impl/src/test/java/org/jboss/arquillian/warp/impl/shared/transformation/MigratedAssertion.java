package org.jboss.arquillian.warp.impl.shared.transformation;

public class MigratedAssertion {

    private TransformedAssertion transformed;
    private byte[] migrated;

    public MigratedAssertion(TransformedAssertion transformed) throws AssertionTransformationException {
        this.transformed = transformed;
        this.migrated = migrate();
    }

    private byte[] migrate() throws AssertionTransformationException {
        byte[] oldClassFile = transformed.toBytecode();
        String oldClassName = transformed.toClass().getName();
        String newClassName = transformed.getOriginalClass().getName();

        try {
            return MigrationRunnable.invokeSeparately(oldClassName, newClassName, oldClassFile);
        } catch (Throwable e) {
            throw new AssertionTransformationException("Cannot migrate transformed assertion back to original name", e);
        }
    }

    public byte[] toBytecode() {
        return migrated;
    }
}
