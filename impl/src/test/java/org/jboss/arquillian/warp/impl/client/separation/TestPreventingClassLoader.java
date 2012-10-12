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
package org.jboss.arquillian.warp.impl.client.separation;

import static org.junit.Assert.fail;

import org.jboss.arquillian.warp.impl.testutils.SeparatedClassPath;
import org.jboss.arquillian.warp.impl.testutils.SeparatedClassloaderRunner;
import org.jboss.arquillian.warp.impl.utils.ClassLoaderUtils;
import org.jboss.arquillian.warp.impl.utils.ShrinkWrapUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(SeparatedClassloaderRunner.class)
public class TestPreventingClassLoader {

    @SeparatedClassPath
    public static JavaArchive[] archive() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class).addClasses(PreventingClassLoader.class,
                ClassLoaderUtils.class);

        JavaArchive mockito = ShrinkWrapUtils.getJavaArchiveFromClass(Mockito.class);

        JavaArchive junit = ShrinkWrapUtils.getJavaArchiveFromClass(Assert.class);

        return new JavaArchive[] { archive, mockito, junit };
    }

    @Test
    public void testPreventLoadingClass() throws ClassNotFoundException {

        PreventingClassLoader cl = new PreventingClassLoader(ClassLoaderUtils.getBootstrapClassLoader(), String.class.getName());

        try {
            cl.loadClass(String.class.getName());
            fail("The class String should be prevented from being loaded");
        } catch (ClassNotFoundException e) {
        }
    }

    @Test
    public void testDoNotPreventLoading() throws ClassNotFoundException {

        PreventingClassLoader cl = new PreventingClassLoader(ClassLoaderUtils.getBootstrapClassLoader(), "NonExistingClass");

        cl.loadClass(String.class.getName());
    }
}
