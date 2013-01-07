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
package org.jboss.arquillian.warp.impl.client.execution;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.warp.client.result.WarpGroupResult;
import org.jboss.arquillian.warp.client.result.WarpResult;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.jboss.arquillian.warp.spi.observer.RequestObserverChainManager;

public class WarpContextImpl implements WarpContext {

        private Map<Object, WarpGroup> groups = new HashMap<Object, WarpGroup>();
        private Queue<Exception> exceptions = new ConcurrentLinkedQueue<Exception>();

        private SynchronizationPoint synchronization = new SynchronizationPoint();
        private List<RequestObserverChainManager> observerChainManagers;

        @Override
        public void addGroup(WarpGroup group) {
            groups.put(group.getId(), group);
        }

        @Override
        public Collection<WarpGroup> getAllGroups() {
            return groups.values();
        }

        @Override
        public WarpGroup getGroup(Object identifier) {
            return groups.get(identifier);
        }

        public Collection<RequestObserverChainManager> getObserverChainManagers() {
            return observerChainManagers;
        }

        @Override
        public TestResult getFirstNonSuccessfulResult() {
            for (WarpGroup group : getAllGroups()) {
                TestResult result = group.getFirstNonSuccessfulResult();
                if (result != null) {
                    return result;
                }
            }

            return null;
        }

        @Override
        public void pushResponsePayload(ResponsePayload payload) {
            for (WarpGroup group : groups.values()) {
                if (group.pushResponsePayload(payload)) {
                    synchronization.finishOneResponse();
                    return;
                }
            }
            throw new IllegalStateException("There was no group found for given response payload");
        }

        @Override
        public void pushException(Exception exception) {
            exceptions.add(exception);
            synchronization.finishAll();
        }

        @Override
        public Exception getFirstException() {
            return exceptions.peek();
        }

        @Override
        public SynchronizationPoint getSynchronization() {
            return synchronization;
        }

        @Override
        public WarpResult getResult() {
            return new WarpResult() {
                @Override
                public WarpGroupResult getGroup(Object identifier) {
                    return groups.get(identifier);
                }
            };
        }

        @Override
        public void initialize(ServiceLoader serviceLoader) {
            // load observer chain managers and sort them by priority
            observerChainManagers = new LinkedList<RequestObserverChainManager>(serviceLoader.all(RequestObserverChainManager.class));
            Collections.sort(observerChainManagers, new Comparator<RequestObserverChainManager>() {
                public int compare(RequestObserverChainManager o1, RequestObserverChainManager o2) {
                    return o1.priotity() - o2.priotity();
                }
            });
        }

        @Override
        public int getExpectedRequestCount() {
            int count = 0;
            for (WarpGroup group : getAllGroups()) {
                count += group.getExpectedRequestCount();
            }
            return count;
        }
    }