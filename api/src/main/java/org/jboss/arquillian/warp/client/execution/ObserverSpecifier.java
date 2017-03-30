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
package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.RequestObserver;
import org.jboss.arquillian.warp.client.observer.ObserverBuilder;

public interface ObserverSpecifier<R> {

    /**
     * Specifies what requests should be observed and verified on server
     *
     * @param what what requests should be observed and verified on server
     * @return the interface for executing server verification
     */
    R observe(RequestObserver what);

    /**
     * Specifies class which specifies what requests should be observed and verified on server
     *
     * @param what the class which specifies what requests should be observed and verified on server
     * @return the interface for executing server inspection
     */
    R observe(Class<? extends RequestObserver> what);

    /**
     * Specifies builder which will be used to construct concrete observer
     *
     * @param what the observerBuilder which will be used for constructing the observer instance
     * @return the interface for executing server verification
     */
    R observe(ObserverBuilder<?, ?> what);
}
