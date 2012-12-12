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
package org.jboss.arquillian.warp.client.observer;

import org.jboss.arquillian.warp.RequestObserver;
import org.jboss.arquillian.warp.client.filter.RequestFilter;

/**
 * Provides means for creating an instance of given {@link RequestFilter} through a builder like pattern.
 * <p />
 * The builder can be copied through the {@link #copy()} method.
 *
 * @param <T> the builder type
 * @param <K> the filter type
 */
public interface ObserverBuilder<T extends ObserverBuilder<T, K>, K extends RequestObserver> {

    /**
     * Copies this builder.
     *
     * @return the copy of the builder
     */
    T copy();

    /**
     * Builds the filter.
     *
     * @return the instance of the filter
     */
    K build();
}
