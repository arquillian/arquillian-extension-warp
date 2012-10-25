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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.client.execution.ClientActionExecutor;
import org.jboss.arquillian.warp.client.execution.ExecutionGroup;
import org.jboss.arquillian.warp.client.execution.GroupAssertionSpecifier;
import org.jboss.arquillian.warp.client.execution.GroupsExecutor;
import org.jboss.arquillian.warp.client.execution.RequestExecutor;
import org.jboss.arquillian.warp.client.execution.SingleRequestExecutor;
import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.result.ResponseGroup;
import org.jboss.arquillian.warp.client.result.WarpResult;
import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;
import org.jboss.arquillian.warp.exception.ServerWarpExecutionException;
import org.jboss.arquillian.warp.impl.client.event.AdvertiseEnrichment;
import org.jboss.arquillian.warp.impl.client.event.AwaitResponse;
import org.jboss.arquillian.warp.impl.client.event.CleanEnrichment;
import org.jboss.arquillian.warp.impl.client.event.FinishEnrichment;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionScoped;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

/**
 * The implementation of execution of client action and server assertion.
 * 
 * @author Lukas Fryc
 * 
 */
public class DefaultRequestExecutor implements RequestExecutor, ClientActionExecutor, GroupsExecutor, SingleRequestExecutor {

    private int groupSequenceNumber = 0;

    private ClientAction action;

    private WarpContext context = new WarpContext();

    private Group singleGroup;

    private ClientActionException actionException;

    @Inject
    private Event<AdvertiseEnrichment> advertiseEnrichment;

    @Inject
    private Event<RequestEnrichment> requestEnrichment;

    @Inject
    private Event<FinishEnrichment> finishEnrichment;

    @Inject
    private Event<CleanEnrichment> cleanEnrichment;

    @Inject
    private Event<AwaitResponse> awaitResponse;

    @Inject
    private Event<ClientAction> executeClientAction;

    @Inject
    @WarpExecutionScoped
    private InstanceProducer<WarpResultStore> warpResultStore;

    @SuppressWarnings("unchecked")
    public <T extends ServerAssertion> T verify(T assertion) {
        singleGroup.addAssertion(assertion);
        WarpResult result = execute();
        return (T) result.getGroup(SingleRequestExecutor.KEY).getAssertion();
    }

    @Override
    public ClientActionExecutor execute(ClientAction action) {
        this.action = action;
        return this;
    }

    @Override
    public WarpResult verifyAll(ServerAssertion... assertions) {
        singleGroup.addAssertions(assertions);
        return execute();
    }

    @Override
    public WarpResult verifyAll() {
        return execute();
    }

    @Override
    public ExecutionGroup group() {
        return group(groupSequenceNumber++);
    }

    @Override
    public ExecutionGroup group(Object identifier) {
        return new Group(identifier);
    }

    @Override
    public SingleRequestExecutor filter(RequestFilter<?> filter) {
        singleGroup = new Group(SingleRequestExecutor.KEY);
        context.addGroup(singleGroup);
        return this;
    }

    @Override
    public SingleRequestExecutor filter(Class<RequestFilter<?>> filterClass) {
        singleGroup = new Group(SingleRequestExecutor.KEY);
        singleGroup.filter = SecurityActions.newInstance(filterClass.getName(), new Class<?>[] {}, new Object[] {},
                RequestFilter.class);
        context.addGroup(singleGroup);
        return this;
    }

    private WarpResult execute() {
        try {
            warpResultStore.set(context);

            setupServerAssertion();
            executeClientAction();
            awaitServerExecution();
            checkClientActionFailure();

            return context;
        } finally {
            cleanup();
        }
    }

    private void setupServerAssertion() {
        final Collection<Group> groups = context.getAllGroups();

        advertiseEnrichment.fire(new AdvertiseEnrichment(groups.size()));

        for (Group group : groups) {
            requestEnrichment.fire(group);
        }

        finishEnrichment.fire(new FinishEnrichment());
    }

    private void executeClientAction() {
        actionException = null;
        try {
            executeClientAction.fire(action);
        } catch (Exception e) {
            actionException = new ClientActionException(e);
        }
    }

    private void checkClientActionFailure() {
        if (actionException != null) {
            throw actionException;
        }
    }

    private void awaitServerExecution() {
        awaitResponse.fire(new AwaitResponse());

        TestResult testResult = context.getFirstNonSuccessfulResult();

        switch (testResult.getStatus()) {
            case FAILED:
                propagateFailure(testResult);
                break;
            case SKIPPED:
                propagateSkip();
                break;
            case PASSED:
        }
    }

    private void cleanup() {
        cleanEnrichment.fire(new CleanEnrichment());
    }

    private void propagateFailure(TestResult testResult) {
        Throwable e = testResult.getThrowable();

        if (e instanceof AssertionError) {
            throw (AssertionError) e;
        } else if (e instanceof ClientWarpExecutionException) {
            throw (ClientWarpExecutionException) e;
        } else if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new ServerWarpExecutionException(e);
        }
    }

    private void propagateSkip() {
        throw new ServerWarpExecutionException("execution was skipped");
    }

    public static class ClientActionException extends RuntimeException {
        private static final long serialVersionUID = 7267806785171391801L;

        public ClientActionException(Throwable cause) {
            super(cause);
        }
    }

    private class WarpContext implements WarpResult, WarpResultStore {

        private Map<Object, Group> groups = new HashMap<Object, Group>();

        @Override
        public ResponseGroup getGroup(Object identifier) {
            return groups.get(identifier);
        }

        private void addGroup(Group group) {
            groups.put(group.id, group);
        }

        private Collection<Group> getAllGroups() {
            return groups.values();
        }

        private Collection<ResponsePayload> getAllResponsePayloads() {
            List<ResponsePayload> paylods = new LinkedList<ResponsePayload>();
            for (Group group : getAllGroups()) {
                paylods.addAll(group.getResponsePayloads());
            }
            return paylods;
        }

        private TestResult getFirstNonSuccessfulResult() {
            for (ResponsePayload payload : getAllResponsePayloads()) {
                TestResult testResult = payload.getTestResult();

                if (testResult != null) {
                    switch (testResult.getStatus()) {
                        case FAILED:
                            return testResult;
                        case SKIPPED:
                            return testResult;
                        case PASSED:
                    }
                }
            }

            return null;
        }
    }

    private class Group implements ExecutionGroup, GroupAssertionSpecifier, ResponseGroup, RequestEnrichment,
            ResponseEnrichment {

        private Object id;
        private RequestFilter<?> filter;

        private Map<RequestPayload, ResponsePayload> payloads = new LinkedHashMap<RequestPayload, ResponsePayload>();

        public Group(Object identifier) {
            this.id = identifier;
        }

        @Override
        public GroupAssertionSpecifier filter(RequestFilter<?> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public GroupAssertionSpecifier filter(Class<RequestFilter<?>> filterClass) {
            this.filter = SecurityActions.newInstance(filterClass.getName(), new Class<?>[] {}, new Object[] {},
                    RequestFilter.class);
            return this;
        }

        @Override
        public GroupsExecutor verify(ServerAssertion... assertions) {
            addAssertions(assertions);
            return DefaultRequestExecutor.this;
        }

        private void addAssertions(ServerAssertion... assertions) {
            for (ServerAssertion assertion : assertions) {
                addAssertion(assertion);
            }
        }

        private void addAssertion(ServerAssertion assertion) {
            RequestPayload payload = new RequestPayload(assertion);
            payloads.put(payload, null);
        }

        @Override
        public RequestFilter<?> getFilter() {
            return filter;
        }

        @Override
        public <T extends ServerAssertion> T getAssertion() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getHitCount() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Collection<RequestPayload> getRequestPayloads() {
            return payloads.keySet();
        }

        @Override
        public Collection<ResponsePayload> getResponsePayloads() {
            return payloads.values();
        }
    }
}