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
package org.jboss.arquillian.warp.impl.client.filter.http;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.filter.http.HttpFilterBuilder;
import org.jboss.arquillian.warp.client.filter.http.HttpRequest;
import org.jboss.arquillian.warp.client.filter.http.HttpRequestFilter;
import org.jboss.arquillian.warp.client.filter.matcher.HttpHeaderMatcherBuilder;
import org.jboss.arquillian.warp.client.filter.matcher.MethodMatcherBuilder;
import org.jboss.arquillian.warp.client.filter.matcher.UriMatcherBuilder;
import org.jboss.arquillian.warp.impl.client.filter.matcher.DefaultHttpHeaderMatcherBuilder;
import org.jboss.arquillian.warp.impl.client.filter.matcher.DefaultMethodMatcherBuilder;
import org.jboss.arquillian.warp.impl.client.filter.matcher.DefaultUriMatcherBuilder;

/**
 * The default implementation of the {@link HttpFilterBuilder} class.
 */
public class DefaultHttpFilterBuilder implements HttpFilterChainBuilder<HttpFilterBuilder>, HttpFilterBuilder {

    /**
     * The instance to the {@link HttpRequestFilter}.
     */
    private RequestFilter<HttpRequest> requestFilter;

    /**
     * Creates new instance of {@link DefaultHttpFilterBuilder} class.
     */
    public DefaultHttpFilterBuilder() {

        this(new TrueRequestFilter());
    }

    /**
     * Creates new instance of {@link DefaultHttpFilterBuilder} class with given {@link HttpRequestFilter} instance.
     *
     * @param requestFilter the {@link HttpRequestFilter} instance
     */
    private DefaultHttpFilterBuilder(RequestFilter<HttpRequest> requestFilter) {

        this.requestFilter = requestFilter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UriMatcherBuilder<HttpFilterBuilder> uri() {

        return new DefaultUriMatcherBuilder(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodMatcherBuilder<HttpFilterBuilder> method() {

        return new DefaultMethodMatcherBuilder(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpHeaderMatcherBuilder<HttpFilterBuilder> header() {

        return new DefaultHttpHeaderMatcherBuilder(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpFilterBuilder copy() {

        return new DefaultHttpFilterBuilder(requestFilter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestFilter<HttpRequest> build() {

        return requestFilter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpFilterBuilder addFilter(RequestFilter<HttpRequest> filter) {

        requestFilter = new ChainHttpRequestFilter(filter, requestFilter);

        return this;
    }

    /**
     * A custom {@link HttpRequestFilter} implementation that allows of chaining the filters. By default the result of
     * {@link #matches(org.jboss.arquillian.warp.client.filter.http.HttpRequest)} is being computed as logical AND of
     * all filter results.
     */
    private static final class ChainHttpRequestFilter implements RequestFilter<HttpRequest> {

        /**
         * Instance of {@link HttpRequestFilter}.
         */
        private RequestFilter<HttpRequest> filter;

        /**
         * Reference to the previous filter.
         */
        private RequestFilter<HttpRequest> previous;

        /**
         * Creates new instance of {@link ChainHttpRequestFilter} class with given filter and link to the previous
         * filter.
         *
         * @param filter   the filter
         * @param previous the reference to the previous filter in the chain
         */
        private ChainHttpRequestFilter(RequestFilter<HttpRequest> filter, RequestFilter<HttpRequest> previous) {

            this.filter = filter;
            this.previous = previous;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(HttpRequest request) {

            return filter.matches(request) && previous.matches(request);
        }

        @Override
        public String toString() {
            if (previous instanceof TrueRequestFilter) {
                return filter.toString();
            } else {
                return String.format("%s and %s", filter, previous);
            }
        }
    }

    private static final class IndexRequestFilter implements RequestFilter<HttpRequest> {

        private final int originalCount;
        private AtomicInteger count;

        public IndexRequestFilter(int count) {
            this.originalCount = count;
            this.count = new AtomicInteger(count);
        }

        @Override
        public boolean matches(HttpRequest request) {
            return count.decrementAndGet() == 0;
        }

        @Override
        public String toString() {
            return String.format("index(%s)", originalCount);
        }
    }

    /**
     * A plan instance of {@link HttpRequestFilter} that always returns true.
     */
    private static final class TrueRequestFilter implements RequestFilter<HttpRequest> {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(HttpRequest request) {
            return true;
        }

        @Override
        public String toString() {
            return "true";
        }
    }

    @Override
    public HttpFilterBuilder index(int count) {
        return this.addFilter(new IndexRequestFilter(count));
    }

    @Override
    public String toString() {
        return requestFilter.toString();
    }
}
