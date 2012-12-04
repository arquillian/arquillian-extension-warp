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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.client.execution.GroupVerificationBuilder;
import org.jboss.arquillian.warp.client.execution.GroupVerificationSpecifier;
import org.jboss.arquillian.warp.client.filter.FilterBuilder;
import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

/**
 * Default implemetnation of {@link WarpGroup}.
 *
 * @author Lukas Fryc
 */
public class WarpGroupImpl implements WarpGroup {

    private Object id;
    private RequestFilter<?> filter;
    private GroupVerificationBuilder groupsExecutor;
    private int expectCount = 1;

    private ServerAssertion[] assertions;

    private LinkedHashMap<RequestPayload, ResponsePayload> payloads = new LinkedHashMap<RequestPayload, ResponsePayload>();

    public WarpGroupImpl(GroupVerificationBuilder groupsExecutor, Object identifier) {
        this.groupsExecutor = groupsExecutor;
        this.id = identifier;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.FilterSpecifier#filter(org.jboss.arquillian.warp.client.filter.RequestFilter)
     */
    @Override
    public GroupVerificationSpecifier filter(RequestFilter<?> filter) {
        this.filter = filter;
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.FilterSpecifier#filter(java.lang.Class)
     */
    @Override
    public GroupVerificationSpecifier filter(Class<? extends RequestFilter<?>> filterClass) {
        this.filter = SecurityActions.newInstance(filterClass.getName(), new Class<?>[] {}, new Object[] {},
                RequestFilter.class);
        return this;
    }

    @Override
    public GroupVerificationSpecifier filter(FilterBuilder<?, ?> filterBuilder) {
        this.filter = filterBuilder.build();
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.GroupVerificationSpecifier#expectCount(int)
     */
    @Override
    public GroupVerificationSpecifier expectCount(int numberOfRequests) {
        this.expectCount = numberOfRequests;
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.GroupAssertionSpecifier#verify(org.jboss.arquillian.warp.ServerAssertion[])
     */
    @Override
    public GroupVerificationBuilder verify(ServerAssertion... assertions) {
        addAssertions(assertions);
        return groupsExecutor;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.impl.client.execution.WarpGroup#addAssertions(org.jboss.arquillian.warp.ServerAssertion[])
     */
    @Override
    public void addAssertions(ServerAssertion... assertions) {
        this.assertions = assertions;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.impl.client.execution.WarpGroup#getFilter()
     */
    @Override
    public RequestFilter<?> getFilter() {
        return filter;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.result.WarpGroupResult#getAssertion()
     */
    @Override
    public <T extends ServerAssertion> T getAssertion() {
        return (T) payloads.values().iterator().next().getAssertions().get(0);
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.result.WarpGroupResult#getAssertionForHitNumber(int)
     */
    @Override
    public <T extends ServerAssertion> T getAssertionForHitNumber(int hitNumber) {
        return (T) getAssertionsForHitNumber(hitNumber).get(0);
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.result.WarpGroupResult#getAssertions()
     */
    @Override
    public List<ServerAssertion> getAssertions() {
        return payloads.values().iterator().next().getAssertions();
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.result.WarpGroupResult#getAssertionsForHitNumber(int)
     */
    @Override
    public List<ServerAssertion> getAssertionsForHitNumber(int hitNumber) {
        ResponsePayload payload = (ResponsePayload) payloads.values().toArray()[hitNumber];
        return payload.getAssertions();
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.result.WarpGroupResult#getHitCount()
     */
    @Override
    public int getHitCount() {
        return payloads.size();
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.impl.client.execution.WarpGroup#getId()
     */
    public Object getId() {
        return id;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.impl.client.execution.WarpGroup#generateRequestPayload()
     */
    public RequestPayload generateRequestPayload() {
        if (payloads.size() + 1 > expectCount) {
            throw new IllegalStateException(String.format("There were more requests executed (%s) then expected (%s)",
                    payloads.size() + 1, expectCount));
        }
        RequestPayload requestPayload = new RequestPayload(assertions);
        payloads.put(requestPayload, null);
        return requestPayload;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.impl.client.execution.WarpGroup#pushResponsePayload(org.jboss.arquillian.warp.impl.shared.ResponsePayload)
     */
    public boolean pushResponsePayload(ResponsePayload payload) {
        for (Entry<RequestPayload, ResponsePayload> entry : payloads.entrySet()) {
            if (payload.getSerialId() == entry.getKey().getSerialId()) {
                if (entry.getValue() != null) {
                    throw new IllegalStateException("");
                }
            }
            entry.setValue(payload);
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.impl.client.execution.WarpGroup#allRequestsPaired()
     */
    public boolean allRequestsPaired() {
        if (payloads.size() < expectCount) {
            return false;
        }
        for (ResponsePayload responsePayload : payloads.values()) {
            if (responsePayload == null) {
                return false;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.impl.client.execution.WarpGroup#getFirstNonSuccessfulResult()
     */
    public TestResult getFirstNonSuccessfulResult() {
        for (ResponsePayload payload : payloads.values()) {
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