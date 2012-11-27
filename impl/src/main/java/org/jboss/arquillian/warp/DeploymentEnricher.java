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
package org.jboss.arquillian.warp;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.container.test.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.warp.spi.WarpLifecycleExtension;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Adds all parts required by JSFUnit into web archive.
 *
 * @author Lukas Fryc
 *
 */
public class DeploymentEnricher implements ApplicationArchiveProcessor, AuxiliaryArchiveAppender, ProtocolArchiveProcessor {

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Instance<TestClass> testClass;

    /**
     * Adds Warp lifecycle extensions to the application archive
     */
    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (testClass.isAnnotationPresent(WarpTest.class)) {
            if (applicationArchive instanceof WebArchive) {
                WebArchive webArchive = (WebArchive) applicationArchive;

                // add warp extensions
                Collection<WarpLifecycleExtension> lifecycleExtensions = serviceLoader.get().all(WarpLifecycleExtension.class);
                for (WarpLifecycleExtension extension : lifecycleExtensions) {
                    JavaArchive library = extension.getEnrichmentLibrary();
                    if (library != null) {
                        webArchive.addAsLibrary(library);
                    }
                    extension.enrichWebArchive(webArchive);
                }
            }
        }
    }

    /**
     * Creates Warp archive
     */
    @Override
    public Archive<?> createAuxiliaryArchive() {
        TestClass testClass = this.testClass.get();

        if (testClass.isAnnotationPresent(WarpTest.class)) {
            JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "arquillian-warp.jar");

            // API
            archive.addClass(ServerAssertion.class);

            // SPI
            archive.addPackage("org.jboss.arquillian.warp.spi");
            archive.addPackage("org.jboss.arquillian.warp.spi.event");

            // Implementation
            archive.addPackage("org.jboss.arquillian.warp.impl.server.assertion");
            archive.addPackage("org.jboss.arquillian.warp.impl.server.enrichment");
            archive.addPackage("org.jboss.arquillian.warp.impl.server.event");
            archive.addPackage("org.jboss.arquillian.warp.impl.server.execution");
            archive.addPackage("org.jboss.arquillian.warp.impl.server.lifecycle");
            archive.addPackage("org.jboss.arquillian.warp.impl.server.provider");
            archive.addPackage("org.jboss.arquillian.warp.impl.server.request");
            archive.addPackage("org.jboss.arquillian.warp.impl.server.test");
            archive.addPackage("org.jboss.arquillian.warp.impl.shared");
            archive.addPackage("org.jboss.arquillian.warp.impl.utils");

            // Servlet Extension
            archive.addPackage("org.jboss.arquillian.warp.extension.servlet");

            // register remote extension
            archive.addClass(WarpRemoteExtension.class);
            archive.addAsServiceProvider(RemoteLoadableExtension.class, WarpRemoteExtension.class);

            return archive;
        } else {
            return null;
        }
    }

    /**
     * Removes test class from web archive
     *
     * (test class will be suplied by transformed asserions)
     */
    @Override
    public void process(TestDeployment testDeployment, Archive<?> protocolArchive) {
        TestClass testClass = this.testClass.get();

        if (testClass.isAnnotationPresent(WarpTest.class)) {
            Archive<?> applicationArchive = testDeployment.getApplicationArchive();
            List<ArchivePath> classPathsToRemove = new LinkedList<ArchivePath>();
            for (ArchivePath archivePath : applicationArchive.getContent().keySet()) {
                String path = archivePath.get();
                String classPath = testClass.getName().replace(".", "/");
                if (path.matches("/WEB-INF/classes/" + classPath + "(\\$.*)?\\.class")) {
                    classPathsToRemove.add(archivePath);
                }
            }
            for (ArchivePath archivePath : classPathsToRemove) {
                applicationArchive.delete(archivePath);
            }

        }
    }
}
