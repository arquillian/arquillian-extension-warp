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
package org.jboss.arquillian.warp.impl.testutils;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Part one of the "separated classloader" workaround (see Readme.md)
 *
 * @author WolfgangHG
 */
public class SeparatedClassloaderExtension implements BeforeAllCallback, AfterAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(SeparatedClassloaderExtension.class);
    private static final String KEY = "originalClassLoader";

    @Override
    public void beforeAll(ExtensionContext context) {
        context.getStore(NAMESPACE).put(KEY, Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(context.getRequiredTestClass().getClassLoader());
    }

    @Override
    public void afterAll(ExtensionContext context) {
        var originalClassLoader = context.getStore(NAMESPACE).get(KEY, ClassLoader.class);
        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }
}
