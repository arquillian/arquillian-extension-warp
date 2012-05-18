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

import org.jboss.arquillian.container.test.impl.enricher.resource.URLResourceProvider;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.warp.proxy.ProxyService;
import org.jboss.arquillian.warp.proxy.ProxyURLProvider;

/**
 * The client side extension for enhancing test with JSFUnit's logic.
 * 
 * @author Lukas Fryc
 */
public class WarpExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {

        // proxy
        builder.override(ResourceProvider.class, URLResourceProvider.class, ProxyURLProvider.class);
        builder.observer(ProxyService.class);

        // deployment enrichment
        builder.service(ApplicationArchiveProcessor.class, DeploymentEnricher.class);
        builder.service(AuxiliaryArchiveAppender.class, DeploymentEnricher.class);
    }

}
