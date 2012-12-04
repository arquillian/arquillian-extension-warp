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
package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.client.filter.FilterBuilder;
import org.jboss.arquillian.warp.client.filter.RequestFilter;

public interface FilterSpecifier<R> {

    /**
     * Specifies filter which will be used to select which requests will be enriched and verified
     *
     * @param filter the filter which specifies which requests will be enriched and verified
     * @return the interface for executing single server verification
     */
    R filter(RequestFilter<?> filter);

    /**
     * Specifies class of a filter which will be used to select which requests will be enriched and verified
     *
     * @param filterClass the class of the filter which specifies which requests will be enriched and verified
     * @return the interface for executing single server verification
     */
    R filter(Class<? extends RequestFilter<?>> filterClass);


    /**
     * Specifies filter biulder which will be used to construct concrete filter
     *
     * @param filterBuilder the filterBuilder which will be used for constructing the filter instance
     * @return the interface for executing single server verification
     */
    R filter(FilterBuilder<?, ?> filterBuilder);
}
