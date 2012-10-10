package org.jboss.arquillian.warp.server.assertion;

public class MigratedAssertion {

    private TransformedAssertion transformed;
    private byte[] migrated;

    public MigratedAssertion(TransformedAssertion transformed) {
        this.transformed = transformed;
        this.migrated = migrate(); 
    }
    
    private byte[] migrate() {
        // TODO
        return null;
    }
    
    public byte[] toBytecode() {
        return migrated;
    }
}
