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
package org.jboss.arquillian.warp.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.assertion.AssertionRegistry;
import org.jboss.arquillian.warp.filter.WarpFilter;
import org.jboss.arquillian.warp.lifecycle.LifecycleManagerImpl;
import org.jboss.arquillian.warp.request.RequestContext;
import org.jboss.arquillian.warp.spi.LifecycleEvent;
import org.jboss.arquillian.warp.spi.WarpLifecycleExtension;
import org.jboss.arquillian.warp.test.LifecycleTestDriver;
import org.jboss.arquillian.warp.utils.SerializationUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * Adds all parts required by JSFUnit into web archive.
 * 
 * @author Lukas Fryc
 * 
 */
public class DeploymentEnricher implements ApplicationArchiveProcessor {

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        // TODO should check that testClass is annotated (no need to process each WAR)
        if (applicationArchive instanceof WebArchive) {
            WebArchive webArchive = (WebArchive) applicationArchive;

            // add requred libraries
            webArchive.addAsLibrary(Maven.withPom("pom.xml").dependency("commons-codec:commons-codec:1.6"));

            // add all required packages
            webArchive.addPackage(WarpFilter.class.getPackage());
            webArchive.addPackage(WarpRemoteExtension.class.getPackage());
            webArchive.addPackage(LifecycleManagerImpl.class.getPackage());
            webArchive.addPackage(RequestContext.class.getPackage());
            webArchive.addPackage(LifecycleTestDriver.class.getPackage());
            webArchive.addPackage(AssertionRegistry.class.getPackage());
            webArchive.addPackage(LifecycleEvent.class.getPackage());

            // add all required classes
            webArchive.addClasses(SerializationUtils.class, ServerAssertion.class);

            // register remote extension
            webArchive.addAsServiceProvider(RemoteLoadableExtension.class, WarpRemoteExtension.class);

            // add all Arquillian's auxilliary archives
            List<Archive<?>> auxiliarryArchives = new ArrayList<Archive<?>>();
            Collection<AuxiliaryArchiveAppender> archiveAppenders = serviceLoader.get().all(AuxiliaryArchiveAppender.class);
            for (AuxiliaryArchiveAppender archiveAppender : archiveAppenders) {
                auxiliarryArchives.add(archiveAppender.createAuxiliaryArchive());
            }
            webArchive.addAsLibraries(auxiliarryArchives);

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
