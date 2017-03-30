/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.warp.impl.server.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.test.spi.TestMethodExecutor;

public class LifecycleMethodExecutor implements TestMethodExecutor {

    private Object instance;
    private Method method;
    private List<Annotation> qualifiers;

    public LifecycleMethodExecutor(Object instance, Method method, List<Annotation> qualifiers) {
        Validate.notNull(instance, "instance must not be null");
        Validate.notNull(method, "method must not be null");
        Validate.notNull(qualifiers, "qualifiers must not be null");

        this.instance = instance;
        this.method = method;
        this.qualifiers = qualifiers;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getInstance() {
        return instance;
    }

    public List<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public void invoke(Object... parameters) throws Throwable {
        try {
            method.invoke(instance, parameters);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

}