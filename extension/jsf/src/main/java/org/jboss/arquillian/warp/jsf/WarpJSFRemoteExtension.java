/*
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
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.warp.jsf.enricher.ManagedPropertyTestEnricher;
import org.jboss.arquillian.warp.jsf.provider.ApplicationProvider;
import org.jboss.arquillian.warp.jsf.provider.ELContextProvider;
import org.jboss.arquillian.warp.jsf.provider.ELResolverProvider;
import org.jboss.arquillian.warp.jsf.provider.ExceptionHandlerProvider;
import org.jboss.arquillian.warp.jsf.provider.ExpressionFactoryProvider;
import org.jboss.arquillian.warp.jsf.provider.ExternalContextProvider;
import org.jboss.arquillian.warp.jsf.provider.FacesContextProvider;
import org.jboss.arquillian.warp.jsf.provider.FlashProvider;
import org.jboss.arquillian.warp.jsf.provider.NavigationHandlerProvider;
import org.jboss.arquillian.warp.jsf.provider.PartialViewContextProvider;
import org.jboss.arquillian.warp.jsf.provider.RenderKitProvider;
import org.jboss.arquillian.warp.jsf.provider.ResourceHandlerProvider;
import org.jboss.arquillian.warp.jsf.provider.StateManagerProvider;
import org.jboss.arquillian.warp.jsf.provider.UIViewRootProvider;
import org.jboss.arquillian.warp.jsf.provider.ViewHandlerProvider;

public class WarpJSFRemoteExtension implements RemoteLoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(JSFInstanceProducers.class);

        builder.service(ResourceProvider.class, ApplicationProvider.class);

        builder.service(ResourceProvider.class, ELContextProvider.class);
        builder.service(ResourceProvider.class, ELResolverProvider.class);
        builder.service(ResourceProvider.class, ExceptionHandlerProvider.class);
        builder.service(ResourceProvider.class, ExpressionFactoryProvider.class);
        builder.service(ResourceProvider.class, ExternalContextProvider.class);
        builder.service(ResourceProvider.class, FacesContextProvider.class);
        builder.service(ResourceProvider.class, FlashProvider.class);
        builder.service(ResourceProvider.class, NavigationHandlerProvider.class);
        builder.service(ResourceProvider.class, PartialViewContextProvider.class);
        builder.service(ResourceProvider.class, RenderKitProvider.class);
        builder.service(ResourceProvider.class, ResourceHandlerProvider.class);
        builder.service(ResourceProvider.class, StateManagerProvider.class);
        builder.service(ResourceProvider.class, UIViewRootProvider.class);
        builder.service(ResourceProvider.class, ViewHandlerProvider.class);

        builder.service(TestEnricher.class, ManagedPropertyTestEnricher.class);
    }
}
