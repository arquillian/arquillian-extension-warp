package org.jboss.arquillian.warp.impl.testutils;

public final class ClassLoaderUtils {

    public static ClassLoader getBootstrapClassLoader() {
        return ClassLoader.getSystemClassLoader().getParent();
    }
}
