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
package org.jboss.arquillian.warp.extension.spring.extension;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.warp.extension.spring.container.SpringWarpRemoteExtension;
import org.jboss.arquillian.warp.extension.spring.WarpDispatcherServlet;
import org.jboss.arquillian.warp.spi.WarpLifecycleExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 *
 */
public class SpringExtension implements LoadableExtension, WarpLifecycleExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(WarpLifecycleExtension.class, this.getClass());
    }

    @Override
    public JavaArchive getEnrichmentLibrary() {
        return ShrinkWrap.create(JavaArchive.class, "warp-extension-spring.jar")
                .addPackage(WarpDispatcherServlet.class.getPackage())
                .addPackage(SpringWarpRemoteExtension.class.getPackage())
                .addAsServiceProvider(RemoteLoadableExtension.class, SpringWarpRemoteExtension.class);
    }

    @Override
    public void enrichWebArchive(WebArchive webArchive) {
    }
}