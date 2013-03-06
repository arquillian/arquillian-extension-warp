/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.warp.extension.jaxrs.cxf.client;

import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.warp.extension.jaxrs.cxf.interceptor.WarpCxfInterceptor;
import org.jboss.arquillian.warp.extension.jaxrs.spi.WarpRestInterceptorEnricher;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * The rest extension responsible for packaging CXF specific interceptors.
 *
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 */
public class WarpCxfInterceptorEnricher implements LoadableExtension, WarpRestInterceptorEnricher {

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(ExtensionBuilder builder) {

        builder.service(WarpRestInterceptorEnricher.class, this.getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enrichWebArchive(WebArchive archive) {

        archive.addPackage(WarpCxfInterceptor.class.getPackage());
    }
}
