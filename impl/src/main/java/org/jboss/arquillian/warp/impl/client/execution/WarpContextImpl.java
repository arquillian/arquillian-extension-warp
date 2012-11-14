package org.jboss.arquillian.warp.impl.client.execution;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.warp.client.result.ResponseGroup;
import org.jboss.arquillian.warp.client.result.WarpResult;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

// TODO diverge interfaces
public class WarpContextImpl implements WarpContext {

        private Map<Object, RequestGroupImpl> groups = new HashMap<Object, RequestGroupImpl>();
        private List<Exception> exceptions = new LinkedList<Exception>();

        private SynchronizationPoint synchronization = new SynchronizationPoint();

        @Override
        public void addGroup(RequestGroupImpl group) {
            groups.put(group.getId(), group);
        }

        @Override
        public Collection<RequestGroupImpl> getAllGroups() {
            return groups.values();
        }

        TestResult getFirstNonSuccessfulResult() {
            for (RequestGroupImpl group : getAllGroups()) {
                TestResult result = group.getFirstNonSuccessfulResult();
                if (result != null) {
                    return result;
                }
            }

            return null;
        }
        
        @Override
        public void pushResponsePayload(ResponsePayload payload) {
            for (RequestGroupImpl group : groups.values()) {
                if (group.pushResponsePayload(payload)) {
                    tryFinalizeResponse();
                    return;
                }
            }
            throw new IllegalStateException("There was no group found for given response payload");
        }
        
        @Override
        public void tryFinalizeResponse() {
            boolean allPaired = true;
            for (RequestGroupImpl group : groups.values()) {
                if (!group.allRequestsPaired()) {
                    allPaired = false;
                    return;
                }
            }
            if (allPaired) {
                synchronization.finishResponse();
            }
        }

        @Override
        public void pushException(Exception exception) {
            exceptions.add(exception);
        }
        
        public Exception getFirstException() {
            if (exceptions.isEmpty()) {
                return null;
            }
            
            return exceptions.get(0);
        }
        
        public SynchronizationPoint getSynchronization() {
            return synchronization;
        }
        
        public WarpResult getResult() {
            return new WarpResult() {
                @Override
                public ResponseGroup getGroup(Object identifier) {
                    return groups.get(identifier);
                }
            };
        }
    }