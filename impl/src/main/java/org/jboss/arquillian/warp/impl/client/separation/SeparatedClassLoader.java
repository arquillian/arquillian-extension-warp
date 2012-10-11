package org.jboss.arquillian.warp.impl.client.separation;

import java.util.Arrays;

import org.jboss.arquillian.warp.impl.utils.ClassLoaderUtils;

public class SeparatedClassLoader extends ClassLoader {

    private ClassLoader[] chain;

    public SeparatedClassLoader(ClassLoader... classLoaderChain) {
        super(ClassLoaderUtils.getBootstrapClassLoader());
        
        chain = Arrays.copyOf(classLoaderChain, classLoaderChain.length + 1);
        chain[chain.length - 1] = super.getParent();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        for (ClassLoader classLoader : chain) {
            try {
                return classLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                // hide the exception
            }
        }
        throw new ClassNotFoundException("Class with name " + name + " wasnt found in any of the class loaders in chain");
    }
}
