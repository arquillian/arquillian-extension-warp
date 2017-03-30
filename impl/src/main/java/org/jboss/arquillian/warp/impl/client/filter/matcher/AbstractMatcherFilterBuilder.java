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
package org.jboss.arquillian.warp.impl.client.filter.matcher;

import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.filter.http.HttpFilterBuilder;
import org.jboss.arquillian.warp.client.filter.http.HttpRequest;
import org.jboss.arquillian.warp.impl.client.filter.http.HttpFilterChainBuilder;

/**
 * A base class for all matcher builders.
 */
public abstract class AbstractMatcherFilterBuilder {

    /**
     * Instance of {@link HttpFilterChainBuilder}.
     */
    private final HttpFilterChainBuilder<HttpFilterBuilder> filterChainBuilder;

    /**
     * Creates new instance of {@link AbstractMatcherFilterBuilder} with given filter chain.
     *
     * @param filterChainBuilder the instance of {@link HttpFilterChainBuilder}
     */
    protected AbstractMatcherFilterBuilder(HttpFilterChainBuilder<HttpFilterBuilder> filterChainBuilder) {

        this.filterChainBuilder = filterChainBuilder;
    }

    /**
     * Adds the filter to the builder.
     *
     * @param filter the filter instance
     */
    protected HttpFilterBuilder addFilter(RequestFilter<HttpRequest> filter) {

        return filterChainBuilder.addFilter(filter);
    }

    /**
     * Retrieves the filter chain builder.
     *
     * @return the filter chain builder
     */
    protected HttpFilterChainBuilder<HttpFilterBuilder> getFilterChainBuilder() {

        return filterChainBuilder;
    }

    @Override
    public String toString() {
        return filterChainBuilder.toString();
    }
}
