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
package org.jboss.arquillian.warp.impl.client.execution;

import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicStampedReference;

import org.jboss.arquillian.warp.RequestObserver;
import org.jboss.arquillian.warp.client.execution.SingleInspectionSpecifier;
import org.jboss.arquillian.warp.client.filter.http.HttpRequestFilter;
import org.jboss.arquillian.warp.impl.client.observer.FaviconIgnore;
import org.jboss.arquillian.warp.impl.client.observer.OnlyFirstRequest;
import org.jboss.arquillian.warp.spi.observer.RequestObserverChainManager;

public class DefaultRequestObserverChainManager implements RequestObserverChainManager {

    private RequestObserver FAVICON_IGNORE = new FaviconIgnore();

    private AtomicStampedReference<OnlyFirstRequest> firstRequestObserver = new AtomicStampedReference<OnlyFirstRequest>(null,
            0);

    @Override
    public Deque<RequestObserver> manageObserverChain(Deque<RequestObserver> observers,
            Class<? extends RequestObserver> expectedObserverType) {

        if (expectedObserverType == HttpRequestFilter.class) {

            // ignore favicon.ico requests
            observers.addFirst(FAVICON_IGNORE);

            // when the group is single execution group, then we will observe only first request which matches defined criteria
            if (allGroups().size() == 1 && allGroups().iterator().next().getId() == SingleInspectionSpecifier.GROUP_ID) {
                observers.addLast(retrieveFirstRequestObserver());
            }
        }

        return observers;
    }

    @Override
    public int priotity() {
        return 0;
    }

    private Collection<WarpGroup> allGroups() {
        return warpContext().getAllGroups();
    }

    private WarpContext warpContext() {
        return WarpContextStore.get();
    }

    /**
     * Obtains new firestRequestObserver for each new warpContext (invaliding previous one)
     */
    private OnlyFirstRequest retrieveFirstRequestObserver() {

        int key = warpContext().hashCode();

        int[] stampHolder = new int[1];
        OnlyFirstRequest onlyFirstRequest = firstRequestObserver.get(stampHolder);

        if (key != stampHolder[0]) {
            firstRequestObserver.compareAndSet(onlyFirstRequest, new OnlyFirstRequest(), stampHolder[0], key);
        }

        onlyFirstRequest = firstRequestObserver.get(stampHolder);

        if (stampHolder[0] != key) {
            warpContext().pushException(new IllegalStateException("There should be only one concurrent Warp execution"));
        }

        return onlyFirstRequest;
    }
}
