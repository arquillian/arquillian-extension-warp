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

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.filter.http.HttpFilterBuilder;
import org.jboss.arquillian.warp.client.filter.http.HttpRequest;
import org.jboss.arquillian.warp.client.filter.matcher.HttpParameterMatcherBuilder;
import org.jboss.arquillian.warp.impl.client.filter.http.HttpFilterChainBuilder;
import org.jboss.arquillian.warp.impl.client.filter.http.NotHttpFilterChainBuilder;

/**
 * A default implementation of {@link DefaultHttpParameterMatcherBuilder}.
 */
public class DefaultHttpParameterMatcherBuilder extends AbstractMatcherFilterBuilder
    implements HttpParameterMatcherBuilder<HttpFilterBuilder> {

    private Logger log = Logger.getLogger(DefaultHttpParameterMatcherBuilder.class.getName());

    /**
     * Creates new instance of {@link DefaultHttpParameterMatcherBuilder} with given filter builder.
     *
     * @param filterChainBuilder the instance of {@link HttpFilterChainBuilder}
     */
    public DefaultHttpParameterMatcherBuilder(HttpFilterChainBuilder<HttpFilterBuilder> filterChainBuilder) {
        super(filterChainBuilder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpFilterBuilder equal(final String parameterName, final String value) {

        return addFilter(new RequestFilter<HttpRequest>() {

            @Override
            public boolean matches(HttpRequest request) {
                final List<String> parameters = getParameterListOrNull(request, parameterName);
                return parameters != null && parameters.size() == 1 && parameters.contains(value);
            }

            @Override
            public String toString() {
                return String.format("parameter.equal('%s', '%s')", parameterName, value);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpFilterBuilder containsParameter(final String parameterName) {

        return addFilter(new RequestFilter<HttpRequest>() {

            @Override
            public boolean matches(HttpRequest request) {
                final List<String> parameters = getParameterListOrNull(request, parameterName);
                return parameters != null;
            }

            @Override
            public String toString() {
                return String.format("containsParameter('%s')", parameterName);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpFilterBuilder containsValue(final String parameterName, final String value) {

        return addFilter(new RequestFilter<HttpRequest>() {

            @Override
            public boolean matches(HttpRequest request) {
                final List<String> parameters = getParameterListOrNull(request, parameterName);
                return parameters != null && parameters.contains(value);
            }

            @Override
            public String toString() {
                return String.format("containsValue('%s', '%s')", parameterName, value);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpParameterMatcherBuilder not() {

        return new DefaultHttpParameterMatcherBuilder(new NotHttpFilterChainBuilder(getFilterChainBuilder()));
    }

    private List<String> getParameterListOrNull(HttpRequest request, String parameterName) {
        try {
            List<String> queryParameterList = getParameterListFromMapOrNull(request.getQueryParameters(), parameterName);
            if (queryParameterList != null) {
                return queryParameterList;
            }
        } catch (Exception e) {
            log.log(Level.FINE, "unable to parse query parameter list", e);
        }

        try {
            List<String> formDataList = getParameterListFromMapOrNull(request.getHttpDataAttributes(), parameterName);
            if (formDataList != null) {
                return formDataList;
            }
        } catch (Exception e) {
            log.log(Level.FINE, "unable to parse HTTP data attribute list", e);
        }

        return null;
    }

    private List<String> getParameterListFromMapOrNull(Map<String, List<String>> parameterMap, String parameterName) {
        if (parameterMap != null && !parameterMap.isEmpty()) {
            List<String> queryParameterList = parameterMap.get(parameterName);
            if (queryParameterList != null) {
                return queryParameterList;
            }
        }
        return null;
    }
}
