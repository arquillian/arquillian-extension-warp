package org.jboss.arquillian.warp.testutils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javassist.CtClass;

import org.jboss.shrinkwrap.api.asset.NamedAsset;

public class CtClassAsset implements NamedAsset {

    private CtClass clazz;

    public CtClassAsset(CtClass clazz) {
        this.clazz = clazz;
    }

    @Override
    public String getName() {
        return clazz.getName().replace(".", "/") + ".class";
    }

    @Override
    public InputStream openStream() {
        try {
            return new ByteArrayInputStream(clazz.toBytecode());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
