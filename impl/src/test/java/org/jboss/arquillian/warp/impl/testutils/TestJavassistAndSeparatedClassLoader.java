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

import static org.junit.Assert.assertNotNull;
import javassist.ClassPool;
import javassist.CtClass;

import org.jboss.arquillian.warp.impl.client.transformation.CtClassAsset;
import org.jboss.arquillian.warp.impl.utils.ClassLoaderUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class TestJavassistAndSeparatedClassLoader {

    private static final String CLASS_NAME = TestJavassistAndSeparatedClassLoader.class.getName() + "Class";

    @Test
    public void test() throws Exception {
        ClassPool cp = ClassPool.getDefault();

        CtClass ctClass = cp.makeClass(CLASS_NAME);

        JavaArchive archive = ShrinkWrap.create(JavaArchive.class).add(new CtClassAsset(ctClass));

        ClassLoader classLoader = new ShrinkWrapClassLoader(ClassLoaderUtils.getBootstrapClassLoader(), archive);

        assertNotNull(classLoader.loadClass(CLASS_NAME));
    }
}
