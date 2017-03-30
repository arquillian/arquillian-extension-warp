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
package org.jboss.arquillian.warp.client.filter.http;

import org.jboss.arquillian.warp.client.execution.WarpRuntime;

/**
 * An utility class for creating new instance of {@link HttpRequestFilter}. This class introduced a builder pattern
 * for creating filters for common scenarios.
 *
 * <p>Sample usage:
 *
 * <pre>
 *     request().uri().endsWith("jsf").method().equal(HttpMethod.POST);
 * </pre>
 *
 * </p>
 */
public final class HttpFilters {

    /**
     * Creates new instance of {@link HttpFilters} class.
     */
    private HttpFilters() {

        throw new IllegalStateException("Could not instantiate class.");
    }

    /**
     * Creates new instance of request filter builder.
     *
     * @return instance of request filter builder
     */
    public static HttpFilterBuilder request() {

        return getBuilderInstance();
    }

    /**
     * Instantiates the {@link HttpFilterBuilder}.
     * <p/>
     * The default implementation delegates to {@link WarpRuntime} in order to create new instance of
     * {@link HttpFilterBuilder}.
     *
     * @return new instance of {@link HttpFilterBuilder}
     */
    private static HttpFilterBuilder getBuilderInstance() {

        return WarpRuntime.getInstance().getHttpFilterBuilder();
    }
}
