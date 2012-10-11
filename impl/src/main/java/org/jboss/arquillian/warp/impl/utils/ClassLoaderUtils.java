package org.jboss.arquillian.warp.impl.utils;

public final class ClassLoaderUtils {

    public static ClassLoader getBootstrapClassLoader() {
        return ClassLoader.getSystemClassLoader().getParent();
    }
}
