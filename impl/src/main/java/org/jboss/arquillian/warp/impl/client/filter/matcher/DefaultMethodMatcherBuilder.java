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
import org.jboss.arquillian.warp.client.filter.http.HttpMethod;
import org.jboss.arquillian.warp.client.filter.http.HttpRequest;
import org.jboss.arquillian.warp.client.filter.http.HttpRequestFilter;
import org.jboss.arquillian.warp.client.filter.matcher.MethodMatcherBuilder;
import org.jboss.arquillian.warp.impl.client.filter.http.HttpFilterChainBuilder;
import org.jboss.arquillian.warp.impl.client.filter.http.NotHttpFilterChainBuilder;

/**
 * A default implementation of {@link MethodMatcherBuilder}.
 */
public class DefaultMethodMatcherBuilder extends AbstractMatcherFilterBuilder
    implements MethodMatcherBuilder<HttpFilterBuilder> {

    /**
     * Creates new instance of {@link DefaultMethodMatcherBuilder} class with given filter builder.
     *
     * @param filterChainBuilder the filer builder
     */
    public DefaultMethodMatcherBuilder(HttpFilterChainBuilder<HttpFilterBuilder> filterChainBuilder) {

        super(filterChainBuilder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpFilterBuilder equal(final HttpMethod value) {

        return addMethodRequestFilter(new Matcher<HttpMethod>() {

            @Override
            public boolean matches(HttpMethod val) {
                return val.equals(value);
            }

            @Override
            public String toString() {
                return String.format("method(%s)", value);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodMatcherBuilder not() {

        return new DefaultMethodMatcherBuilder(new NotHttpFilterChainBuilder(getFilterChainBuilder()));
    }

    /**
     * Adds the filter to the builder.
     *
     * @param matcher the http method matcher
     */
    private HttpFilterBuilder addMethodRequestFilter(Matcher<HttpMethod> matcher) {

        return addFilter(new MethodMatcherRequestFilter(matcher));
    }

    /**
     * An implementation of {@link HttpRequestFilter} that filters the http method.
     */
    private static final class MethodMatcherRequestFilter implements RequestFilter<HttpRequest> {

        /**
         * The {@link Matcher} instance.
         */
        private Matcher<HttpMethod> matcher;

        /**
         * Creates new instance of {@link MethodMatcherRequestFilter} class with given http method matcher.
         *
         * @param matcher the http method matcher
         */
        public MethodMatcherRequestFilter(Matcher<HttpMethod> matcher) {

            this.matcher = matcher;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(HttpRequest request) {

            return matcher.matches(request.getMethod());
        }

        @Override
        public String toString() {
            return matcher.toString();
        }
    }
}
