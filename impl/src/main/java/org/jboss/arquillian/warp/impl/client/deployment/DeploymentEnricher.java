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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jboss.arquillian.container.spi.client.deployment.Validate;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.container.test.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.WarpRemoteExtension;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.impl.client.commandBus.RemoteSuiteLifecyclePropagation.FireAfterSuiteOnServer;
import org.jboss.arquillian.warp.impl.client.commandBus.RemoteSuiteLifecyclePropagation.FireBeforeSuiteOnServer;
import org.jboss.arquillian.warp.impl.client.execution.DefaultHttpRequestEnrichmentService.RegisterPayloadRemotely;
import org.jboss.arquillian.warp.impl.client.execution.DefaultResponseDeenrichmentService.RetrievePayloadFromServer;
import org.jboss.arquillian.warp.impl.server.commandBus.CommandBusOnServer;
import org.jboss.arquillian.warp.impl.server.delegation.RequestDelegationService;
import org.jboss.arquillian.warp.impl.server.execution.WarpFilter;
import org.jboss.arquillian.warp.impl.server.lifecycle.LifecycleManagerStoreImpl;
import org.jboss.arquillian.warp.servlet.AfterServlet;
import org.jboss.arquillian.warp.servlet.BeforeServlet;
import org.jboss.arquillian.warp.spi.LifecycleManagerStore;
import org.jboss.arquillian.warp.spi.WarpDeploymentEnrichmentExtension;
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


    static String[] REQUIRED_WARP_PACKAGES = new String[] {
         // SPI
            "org.jboss.arquillian.warp.spi",
            "org.jboss.arquillian.warp.spi.context",
            "org.jboss.arquillian.warp.spi.event",
            "org.jboss.arquillian.warp.spi.exception",
            "org.jboss.arquillian.warp.spi.servlet.event",

            // Implementation
            "org.jboss.arquillian.warp.impl.server.inspection",
            "org.jboss.arquillian.warp.impl.server.enrichment",
            "org.jboss.arquillian.warp.impl.server.event",
            "org.jboss.arquillian.warp.impl.server.manager",
            "org.jboss.arquillian.warp.impl.server.execution",
            "org.jboss.arquillian.warp.impl.server.lifecycle",
            "org.jboss.arquillian.warp.impl.server.provider",
            "org.jboss.arquillian.warp.impl.server.request",
            "org.jboss.arquillian.warp.impl.server.test",
            "org.jboss.arquillian.warp.impl.server.commandBus",
            "org.jboss.arquillian.warp.impl.server.delegation",
            "org.jboss.arquillian.warp.impl.server.remote",
            "org.jboss.arquillian.warp.impl.shared",
            "org.jboss.arquillian.warp.impl.shared.command",
            "org.jboss.arquillian.warp.impl.utils",

            // Servlet Extension
            "org.jboss.arquillian.warp.servlet.event",
            "org.jboss.arquillian.warp.servlet.provider"
    };

    static Class<?>[] REQUIRED_WARP_INNER_CLASSES = new Class<?>[] {
        FireBeforeSuiteOnServer.class,
        FireAfterSuiteOnServer.class,
        RegisterPayloadRemotely.class,
        RetrievePayloadFromServer.class
    };

    @Inject
    Instance<ServiceLoader> serviceLoader;

    @Inject
    Instance<TestClass> testClass;

    /**
     * Adds Warp lifecycle extensions to the application archive
     */
    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (testClass.isAnnotationPresent(WarpTest.class)) {
            if (applicationArchive instanceof WebArchive) {
                WebArchive webArchive = (WebArchive) applicationArchive;

                // add warp extensions
                Collection<WarpDeploymentEnrichmentExtension> lifecycleExtensions = serviceLoader.get().all(WarpDeploymentEnrichmentExtension.class);
                for (WarpDeploymentEnrichmentExtension extension : lifecycleExtensions) {
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
            archive.addClass(Inspection.class);
            archive.addClasses(BeforeServlet.class, AfterServlet.class);

            for (String packageName : REQUIRED_WARP_PACKAGES) {
                archive.addPackage(packageName);
            }

            archive.addClasses(REQUIRED_WARP_INNER_CLASSES);

            // Delete the WarpFilter from the auxiliary jar.
            // It must go into the testable war that may be only a module of an ear
            archive.delete("org/jboss/arquillian/warp/impl/server/execution/WarpFilter.class");

            // register remote extension
            archive.addClass(WarpRemoteExtension.class);
            archive.addAsServiceProvider(RemoteLoadableExtension.class.getName(), WarpRemoteExtension.class.getName(),"!org.jboss.arquillian.protocol.servlet.runner.ServletRemoteExtension");
            archive.addAsServiceProvider(LifecycleManagerStore.class, LifecycleManagerStoreImpl.class);

            // register RequestProcessingDelegationService
            archive.addAsServiceProvider(RequestDelegationService.class, CommandBusOnServer.class);

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
                // remove TestClass and its anonymous classes - do not remove static inner classes
                if (path.matches("/WEB-INF/classes/" + classPath + "(\\$[0-9]*)?\\.class")) {
                    classPathsToRemove.add(archivePath);
                }
            }
            for (ArchivePath archivePath : classPathsToRemove) {
                applicationArchive.delete(archivePath);
            }

            // Add the WebFilter to the protocolArchive and not to the auxiliary jar that may be added as
            // a library to the ear
            // TODO: Add the filter to the web.xml for the Servlet 2.5 protocol.
            if (Validate.isArchiveOfType(WebArchive.class, protocolArchive)) {
                protocolArchive.as(WebArchive.class).addAsLibrary(
                    ShrinkWrap.create(JavaArchive.class, "arquillian-warp-filter.jar")
                        .addClass(WarpFilter.class));
            } else {
                throw new IllegalArgumentException("Protocol archives of type "+protocolArchive.getClass()+" not supported. Please use the Servlet 3.0 protocol.");
            }
        }
    }
}
