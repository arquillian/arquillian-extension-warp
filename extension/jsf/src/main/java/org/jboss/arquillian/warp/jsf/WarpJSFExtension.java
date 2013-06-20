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
package org.jboss.arquillian.warp.jsf;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.warp.spi.WarpDeploymentEnrichmentExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class WarpJSFExtension implements LoadableExtension, WarpDeploymentEnrichmentExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(WarpDeploymentEnrichmentExtension.class, this.getClass());
    }

    @Override
    public JavaArchive getEnrichmentLibrary() {
        return ShrinkWrap.create(JavaArchive.class, "arquillian-warp-jsf.jar")
                .addPackage("org.jboss.arquillian.warp.jsf")
                .addPackage("org.jboss.arquillian.warp.jsf.provider")
                .addPackage("org.jboss.arquillian.warp.jsf.enricher")
                .addAsManifestResource("META-INF/warp-extensions/faces-config.xml", "faces-config.xml")
                .addAsServiceProvider(RemoteLoadableExtension.class, WarpJSFRemoteExtension.class);
    }

    @Override
    public void enrichWebArchive(WebArchive webArchive) {
    }
}
