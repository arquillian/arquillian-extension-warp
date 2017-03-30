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
package org.jboss.arquillian.warp.impl.client.filter.http;

import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.filter.http.HttpFilterBuilder;
import org.jboss.arquillian.warp.client.filter.http.HttpRequest;
import org.jboss.arquillian.warp.client.filter.http.HttpRequestFilter;

/**
 * The implementation of {@link HttpFilterChainBuilder} that negates the last added filter.
 * <p/>
 * This is class implements the decorator pattern.
 */
public class NotHttpFilterChainBuilder implements HttpFilterChainBuilder<HttpFilterBuilder> {

    /**
     * Instance of {@link HttpFilterChainBuilder}.
     */
    private final HttpFilterChainBuilder<HttpFilterBuilder> httpFilterChainBuilder;

    /**
     * Creates new instance of {@link NotHttpFilterChainBuilder} with given {@link HttpFilterChainBuilder} instance
     *
     * @param httpFilterChainBuilder the filter chain builder
     */
    public NotHttpFilterChainBuilder(HttpFilterChainBuilder<HttpFilterBuilder> httpFilterChainBuilder) {
        this.httpFilterChainBuilder = httpFilterChainBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpFilterBuilder addFilter(RequestFilter<HttpRequest> filter) {

        return httpFilterChainBuilder.addFilter(new NotHttpRequestFilter(filter));
    }

    /**
     * An implementation of {@link HttpRequestFilter} thats decorates the given instance and returns the negated
     * {@link #matches(org.jboss.arquillian.warp.client.filter.http.HttpRequest)} method result.
     */
    private class NotHttpRequestFilter implements RequestFilter<HttpRequest> {

        /**
         * The decorated {@inheritDoc HttpRequestFilter} instance.
         */
        private RequestFilter<HttpRequest> filter;

        /**
         * Creates new instance of {@link HttpRequestFilter} with given filter.
         *
         * @param filter the filter instance
         */
        public NotHttpRequestFilter(RequestFilter<HttpRequest> filter) {

            this.filter = filter;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(HttpRequest request) {
            return !filter.matches(request);
        }

        @Override
        public String toString() {
            return String.format("not %s", filter);
        }
    }
}
