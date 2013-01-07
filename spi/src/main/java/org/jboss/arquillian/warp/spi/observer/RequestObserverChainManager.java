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
package org.jboss.arquillian.warp.spi.observer;

import java.util.Collection;
import java.util.Deque;

import org.jboss.arquillian.warp.RequestObserver;

/**
 * Manages chain of {@link RequestObserver}s.
 *
 * @author Lukas Fryc
 */
public interface RequestObserverChainManager {

    /**
     * <p>
     * Manages observer chain.
     * </p>
     *
     * <p>
     * In modified collection, new observers might be added, observers might be removed or their behavior might be changed.
     * </p>
     *
     * @param observers collection of observers which were specified for given request
     * @param expectedObserverType the type of observers which is expected during currently processed request
     * @return modified collection of
     */
    Deque<RequestObserver> manageObserverChain(Deque<RequestObserver> observers, Class<? extends RequestObserver> expectedObserverType);

    /**
     * <p>
     * Defines priority of observer service which specifies in which order will be {@link #manageObserverChain(Collection)}
     * method applied on collection of request observers.
     * </p>
     *
     * <p>
     * <tt>DefaultRequestObserverService</tt> has priority 0 - no other observer can have this priority.
     * </p>
     *
     * <p>
     * Services with priority lower than zero will be applied after DefaultRequestObserverService; services with priority higher
     * will be applied before.
     * </p>
     */
    int priotity();
}
