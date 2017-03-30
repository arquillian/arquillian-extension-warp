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
package org.jboss.arquillian.warp.client.filter.matcher;

import org.jboss.arquillian.warp.client.filter.http.HttpMethod;

/**
 * A matcher builder responsible for handling {@link HttpMethod} filtering.
 *
 * @param <T> the parent builder type for which the matcher is being build
 */
public interface MethodMatcherBuilder<T> extends MatcherBuilder<MethodMatcherBuilder<T>> {

    /**
     * Matches request that http method is equal to the given value.
     *
     * @param value the method
     *
     * @return builder type for which the matcher is being build
     */
    T equal(HttpMethod value);
}
