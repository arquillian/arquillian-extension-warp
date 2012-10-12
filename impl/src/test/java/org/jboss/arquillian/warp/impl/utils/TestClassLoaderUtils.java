/**
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestClassLoaderUtils {

    @Test
    public void testBootstrapClassLoader() throws Exception {

        ClassLoader classLoader = ClassLoaderUtils.getBootstrapClassLoader();

        assertNotNull("bootstrap classloader should not be null", classLoader);
        assertNull("bootstrap classloader should not have parent", classLoader.getParent());

        assertNotNull("java.lang.String should be on bootstrap classpath", classLoader.loadClass(String.class.getName()));

        try {
            classLoader.loadClass(Test.class.getName());
            fail("@Test annotation should not be present on bootstrap classpath");
        } catch (ClassNotFoundException e) {
            // expected exception
        }
    }
}
