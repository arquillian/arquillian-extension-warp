package org.jboss.arquillian.warp.impl.client.separation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.warp.impl.utils.ClassLoaderUtils;

public class PreventingClassLoader extends ClassLoader {

    private Set<String> preventClassNames;
    private ClassLoader delegate;

    public PreventingClassLoader(ClassLoader delegate, String... preventClassNames) {
        super(ClassLoaderUtils.getBootstrapClassLoader());
        this.preventClassNames = new HashSet<String>(Arrays.asList(preventClassNames));
        this.delegate = delegate;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (preventClassNames.contains(name)) {
            throw new ClassNotFoundException("Class " + name + " was presented from being loaded");
        }
        return delegate.loadClass(name);
    }
}
