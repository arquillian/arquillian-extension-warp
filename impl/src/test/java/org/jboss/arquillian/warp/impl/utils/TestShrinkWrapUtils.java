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
package org.jboss.arquillian.warp.impl.utils;

import static org.junit.Assert.assertNotNull;

import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class TestShrinkWrapUtils {

    @Test
    public void testJavaArchiveFromClass() throws Throwable {
        JavaArchive archive = ShrinkWrapUtils.getJavaArchiveFromClass(Test.class);

        assertNotNull(archive.get("/org/junit/Test.class"));
        assertNotNull(archive.get("/org/junit/Ignore.class"));
    }

    @Test
    public void testMultipleUse() throws Exception {
        JavaArchive archive = ShrinkWrapUtils.getJavaArchiveFromClass(Test.class);

        try (ShrinkWrapClassLoader classLoader =
            new ShrinkWrapClassLoader(ClassLoaderUtils.getBootstrapClassLoader(), archive)) {
            Class<?> nestedClass = classLoader.loadClass(Test.class.getName());

            JavaArchive nestedArchive = ShrinkWrapUtils.getJavaArchiveFromClass(nestedClass);

            assertNotNull(nestedArchive.get("/org/junit/Test.class"));
            assertNotNull(nestedArchive.get("/org/junit/Ignore.class"));
        }
    }
}
