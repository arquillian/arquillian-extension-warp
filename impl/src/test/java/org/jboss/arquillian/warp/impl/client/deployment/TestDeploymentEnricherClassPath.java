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
package org.jboss.arquillian.warp.impl.client.deployment;

import java.io.File;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.WarpRemoteExtension;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.impl.testutils.SeparatedClassPath;
import org.jboss.arquillian.warp.impl.testutils.SeparatedClassloaderRunner;
import org.jboss.arquillian.warp.impl.utils.ShrinkWrapUtils;
import org.jboss.arquillian.warp.servlet.AfterServlet;
import org.jboss.arquillian.warp.servlet.BeforeServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.ServiceExtensionLoader;
import org.jboss.shrinkwrap.spi.MemoryMapArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SeparatedClassloaderRunner.class)
public class TestDeploymentEnricherClassPath {

    @SeparatedClassPath
    public static JavaArchive[] archive() {

        JavaArchive coreSpi = ShrinkWrapUtils.getJavaArchiveFromClass(LoadableExtension.class);

        JavaArchive coreApi = ShrinkWrapUtils.getJavaArchiveFromClass(Instance.class);

        JavaArchive containerTestSpi = ShrinkWrapUtils.getJavaArchiveFromClass(ApplicationArchiveProcessor.class);

        JavaArchive testSpi = ShrinkWrapUtils.getJavaArchiveFromClass(TestClass.class);

        JavaArchive shrinkWrapSpi = ShrinkWrapUtils.getJavaArchiveFromClass(MemoryMapArchive.class);
        JavaArchive shrinkWrapApi = ShrinkWrapUtils.getJavaArchiveFromClass(JavaArchive.class);
        JavaArchive shrinkWrapImpl = ShrinkWrapUtils.getJavaArchiveFromClass(ServiceExtensionLoader.class).addAsResource(new File("src/main/resources/org/jboss/arquillian/warp/impl/server/command/web-fragment.xml"),"org/jboss/arquillian/warp/impl/server/command/web-fragment.xml");

        JavaArchive base = ShrinkWrap.create(JavaArchive.class).addClasses(DeploymentEnricher.class, WarpTest.class,
                Inspection.class, BeforeServlet.class, AfterServlet.class, WarpRemoteExtension.class);

        JavaArchive warp = ShrinkWrap.create(JavaArchive.class);
        for (String packageName : DeploymentEnricher.REQUIRED_WARP_PACKAGES) {
            warp.addPackage(packageName);
        }

        return new JavaArchive[] { base, warp, coreApi, coreSpi, containerTestSpi, testSpi, shrinkWrapSpi, shrinkWrapApi,
                shrinkWrapImpl };
    }

    @Test
    public void test_that_warp_does_not_require_any_non_required_dependencies_during_packaging() {
        DeploymentEnricher deploymentEnricher = new DeploymentEnricher();
        deploymentEnricher.testClass = new Instance<TestClass>() {
            @Override
            public TestClass get() {
                return new TestClass(TestingClass.class);
            }
        };

        deploymentEnricher.createAuxiliaryArchive();
    }

    @WarpTest
    private static class TestingClass {
    }
}
