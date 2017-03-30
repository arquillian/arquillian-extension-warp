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

/**
 * A matcher builder responsible for handling request parameters (either query parameters or HTTP form data)
 *
 * @param <T> the parent builder type for which the matcher is being build
 */
public interface HttpParameterMatcherBuilder<T> extends MatcherBuilder<HttpParameterMatcherBuilder<T>> {

    /**
     * Matches request that HTTP parameter (either query parameter of HTTP form parameter) is equal to the given value.
     *
     * @param name  the parameter name
     * @param value the parameter value
     *
     * @return builder type for which the matcher is being build
     */
    T equal(String name, String value);

    /**
     * Matches request that contains HTTP parameter (either query parameter of HTTP form parameter)with given name.
     *
     * @param name the parameter name
     *
     * @return builder type for which the matcher is being build
     */
    T containsParameter(String name);

    /**
     * Matches request that HTTP parameter (either query parameter of HTTP form parameter) contains the given value.
     *
     * @param name  the parameter name
     * @param value the parameter value
     *
     * @return builder type for which the matcher is being build
     */
    T containsValue(String name, String value);
}
