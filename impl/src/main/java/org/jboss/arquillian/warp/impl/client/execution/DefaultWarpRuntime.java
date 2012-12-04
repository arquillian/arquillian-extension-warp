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
package org.jboss.arquillian.warp.impl.client.execution;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.warp.client.execution.WarpClientActionBuilder;
import org.jboss.arquillian.warp.client.execution.WarpRuntime;
import org.jboss.arquillian.warp.client.filter.http.HttpFilterBuilder;

/**
 * The default implementation of the {@link WarpRuntime}.
 */
public class DefaultWarpRuntime extends WarpRuntime {

    /**
     * Instance of {@link ServiceLoader}.
     */
    @Inject
    private Instance<ServiceLoader> serviceLoaderInstance;

    /**
     * {@inheritDoc}
     */
    @Override
    public WarpClientActionBuilder getWarpClientActionBuilder() {

        return serviceLoaderInstance.get().onlyOne(WarpRequestSpecifier.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpFilterBuilder getHttpFilterBuilder() {

        // copies the filter builder
        return serviceLoaderInstance.get().onlyOne(HttpFilterBuilder.class).copy();
    }
}
