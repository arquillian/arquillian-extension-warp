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

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.warp.impl.server.enrichment.DefaultHttpRequestDeenricher;
import org.jboss.arquillian.warp.impl.server.enrichment.DefaultHttpResponseEnricher;
import org.jboss.arquillian.warp.impl.server.enrichment.HttpRequestDeenricher;
import org.jboss.arquillian.warp.impl.server.enrichment.HttpResponseEnricher;
import org.jboss.arquillian.warp.impl.server.execution.HttpRequestProcessor;
import org.jboss.arquillian.warp.impl.server.execution.WarpLifecycle;
import org.jboss.arquillian.warp.impl.server.execution.WarpRequestProcessor;
import org.jboss.arquillian.warp.impl.server.lifecycle.LifecycleManagerService;
import org.jboss.arquillian.warp.impl.server.provider.HttpServletRequestEnricher;
import org.jboss.arquillian.warp.impl.server.provider.HttpServletResponseEnricher;
import org.jboss.arquillian.warp.impl.server.request.RequestContextHandler;
import org.jboss.arquillian.warp.impl.server.request.RequestContextImpl;
import org.jboss.arquillian.warp.impl.server.test.LifecycleTestClassExecutor;
import org.jboss.arquillian.warp.impl.server.test.LifecycleTestDeenricher;
import org.jboss.arquillian.warp.impl.server.test.LifecycleTestDriver;
import org.jboss.arquillian.warp.impl.server.test.TestResultObserver;

/**
 * Registers extension logic on the server-side.
 *
 * @author Lukas Fryc
 */
public class WarpRemoteExtension implements RemoteLoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.context(RequestContextImpl.class);

        builder.service(ResourceProvider.class, HttpServletRequestEnricher.class);
        builder.service(ResourceProvider.class, HttpServletResponseEnricher.class);

        builder.observer(RequestContextHandler.class);
        builder.observer(LifecycleManagerService.class);

        builder.observer(LifecycleTestDriver.class);
        builder.observer(LifecycleTestClassExecutor.class);
        builder.observer(LifecycleTestDeenricher.class);
        builder.observer(TestResultObserver.class);
        
        builder.observer(HttpRequestProcessor.class);
        builder.observer(WarpRequestProcessor.class);
        builder.observer(WarpLifecycle.class);
        
        builder.service(HttpRequestDeenricher.class, DefaultHttpRequestDeenricher.class);
        builder.service(HttpResponseEnricher.class, DefaultHttpResponseEnricher.class);
    }

}
