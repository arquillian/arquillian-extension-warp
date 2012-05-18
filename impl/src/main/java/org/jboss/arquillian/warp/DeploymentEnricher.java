/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.extension.servlet.BeforeServletEvent;
import org.jboss.arquillian.warp.server.assertion.AssertionRegistry;
import org.jboss.arquillian.warp.server.filter.WarpFilter;
import org.jboss.arquillian.warp.server.lifecycle.LifecycleManagerImpl;
import org.jboss.arquillian.warp.server.request.RequestContext;
import org.jboss.arquillian.warp.server.test.LifecycleTestDriver;
import org.jboss.arquillian.warp.shared.ResponsePayload;
import org.jboss.arquillian.warp.spi.LifecycleEvent;
import org.jboss.arquillian.warp.spi.WarpLifecycleExtension;
import org.jboss.arquillian.warp.utils.SerializationUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * Adds all parts required by JSFUnit into web archive.
 * 
 * @author Lukas Fryc
 * 
 */
public class DeploymentEnricher implements ApplicationArchiveProcessor, AuxiliaryArchiveAppender {

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    Instance<TestClass> testClass;

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (testClass.isAnnotationPresent(WarpTest.class)) {
            if (applicationArchive instanceof WebArchive) {
                WebArchive webArchive = (WebArchive) applicationArchive;

                // add requred libraries
                webArchive.addAsLibrary(Maven.withPom("pom.xml").dependency("commons-codec:commons-codec:1.6"));

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

    @Override
    public Archive<?> createAuxiliaryArchive() {
        TestClass testClass = this.testClass.get();

        if (testClass.isAnnotationPresent(WarpTest.class)) {
            JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "extension-warp.jar");

            // add all required packages
            archive.addPackage(WarpFilter.class.getPackage());
            archive.addPackage(WarpRemoteExtension.class.getPackage());
            archive.addPackage(LifecycleManagerImpl.class.getPackage());
            archive.addPackage(RequestContext.class.getPackage());
            archive.addPackage(LifecycleTestDriver.class.getPackage());
            archive.addPackage(AssertionRegistry.class.getPackage());
            archive.addPackage(LifecycleEvent.class.getPackage());
            archive.addPackage(BeforeServletEvent.class.getPackage());
            archive.addPackage(ResponsePayload.class.getPackage());

            // add all required classes
            archive.addClasses(SerializationUtils.class, ServerAssertion.class);

            // register remote extension
            archive.addAsServiceProvider(RemoteLoadableExtension.class, WarpRemoteExtension.class);

            return archive;
        } else {
            return null;
        }
    }
}
