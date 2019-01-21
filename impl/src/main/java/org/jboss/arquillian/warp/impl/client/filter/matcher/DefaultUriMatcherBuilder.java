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
import org.jboss.arquillian.warp.client.filter.http.HttpRequestFilter;
import org.jboss.arquillian.warp.client.filter.matcher.UriMatcherBuilder;
import org.jboss.arquillian.warp.impl.client.filter.http.HttpFilterChainBuilder;
import org.jboss.arquillian.warp.impl.client.filter.http.NotHttpFilterChainBuilder;

/**
 * A default implementation of {@link UriMatcherBuilder}.
 */
public class DefaultUriMatcherBuilder extends AbstractMatcherFilterBuilder
    implements UriMatcherBuilder<HttpFilterBuilder> {

    /**
     * Creates new instance of {@link DefaultMethodMatcherBuilder} with given filter builder
     *
     * @param filterChainBuilder the filter builder
     */
    public DefaultUriMatcherBuilder(HttpFilterChainBuilder<HttpFilterBuilder> filterChainBuilder) {

        super(filterChainBuilder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpFilterBuilder equal(final String value) {

        return addUriRequestFilter(new Matcher<String>() {
            @Override
            public boolean matches(String val) {

                return val.equals(value);
            }

            @Override
            public String toString() {
                return String.format("equal('%s')", value);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpFilterBuilder equalIgnoreCase(final String value) {

        return addUriRequestFilter(new Matcher<String>() {
            @Override
            public boolean matches(String val) {

                return val.equalsIgnoreCase(value);
            }

            @Override
            public String toString() {
                return String.format("equalIgnoreCase('%s')", value);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpFilterBuilder startsWith(final String value) {

        return addUriRequestFilter(new Matcher<String>() {
            @Override
            public boolean matches(String val) {

                return val.startsWith(value);
            }

            @Override
            public String toString() {
                return String.format("startsWith('%s')", value);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpFilterBuilder contains(final String value) {

        return addUriRequestFilter(new Matcher<String>() {
            @Override
            public boolean matches(String val) {

                return val.contains(value);
            }

            @Override
            public String toString() {
                return String.format("contains('%s')", value);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpFilterBuilder endsWith(final String value) {

        return addUriRequestFilter(new Matcher<String>() {
            @Override
            public boolean matches(String val) {

                return val.endsWith(value);
            }

            @Override
            public String toString() {
                return String.format("endsWith('%s')", value);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpFilterBuilder matches(final String value) {

        return addUriRequestFilter(new Matcher<String>() {
            @Override
            public boolean matches(String val) {

                return val.matches(value);
            }

            @Override
            public String toString() {
                return String.format("matches('%s')", value);
            }
        });
    }

    /**
     * Adds the filter to the builder.
     *
     * @param matcher the request uri matcher
     */
    private HttpFilterBuilder addUriRequestFilter(Matcher<String> matcher) {

        return addFilter(new UriMatcherRequestFilter(matcher));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UriMatcherBuilder not() {

        return new DefaultUriMatcherBuilder(new NotHttpFilterChainBuilder(getFilterChainBuilder()));
    }

    /**
     * An implementation of {@link HttpRequestFilter} that filters the request uri.
     */
    private static final class UriMatcherRequestFilter implements RequestFilter<HttpRequest> {

        /**
         * The {@link Matcher} instance.
         */
        private Matcher<String> matcher;

        /**
         * Creates new instance of {@link UriMatcherRequestFilter} class with given request uri matcher.
         *
         * @param matcher the http method matcher
         */
        UriMatcherRequestFilter(Matcher<String> matcher) {

            this.matcher = matcher;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(HttpRequest request) {

            return matcher.matches(request.getUri());
        }

        @Override
        public String toString() {
            return String.format("uri.%s", matcher);
        }
    }
}
