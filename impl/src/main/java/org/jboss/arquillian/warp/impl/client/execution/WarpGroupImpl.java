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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.RequestObserver;
import org.jboss.arquillian.warp.client.execution.GroupExecutionSpecifier;
import org.jboss.arquillian.warp.client.execution.GroupInspectionBuilder;
import org.jboss.arquillian.warp.client.filter.Request;
import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.observer.ObserverBuilder;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

/**
 * Default implemetnation of {@link WarpGroup}.
 *
 * @author Lukas Fryc
 */
public class WarpGroupImpl implements WarpGroup {

    private Object id;
    private RequestObserver observer;
    private GroupInspectionBuilder groupsExecutor;
    private int expectCount = 1;

    private Inspection[] inspections;

    private Map<Long, Request> requests = Collections.synchronizedMap(new LinkedHashMap<Long, Request>());
    private Map<Long, ResponsePayload> payloads = Collections.synchronizedMap(new LinkedHashMap<Long, ResponsePayload>());

    public WarpGroupImpl(GroupInspectionBuilder groupsExecutor, Object identifier) {
        this.groupsExecutor = groupsExecutor;
        this.id = identifier;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.FilterSpecifier#filter(org.jboss.arquillian.warp.client.filter.RequestFilter)
     */
    @Override
    public GroupExecutionSpecifier observe(RequestObserver what) {
        this.observer = what;
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.FilterSpecifier#filter(java.lang.Class)
     */
    @Override
    public GroupExecutionSpecifier observe(Class<? extends RequestObserver> observer) {
        this.observer = SecurityActions.newInstance(observer.getName(), new Class<?>[] {}, new Object[] {},
            RequestFilter.class);
        return this;
    }

    @Override
    public GroupExecutionSpecifier observe(ObserverBuilder<?, ?> observerBuilder) {
        this.observer = observerBuilder.build();
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.GroupVerificationSpecifier#expectCount(int)
     */
    @Override
    public GroupExecutionSpecifier expectCount(int numberOfRequests) {
        this.expectCount = numberOfRequests;
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.GroupInspectionSpecifier#verify(org.jboss.arquillian.warp.ServerInspection[])
     */
    @Override
    public GroupInspectionBuilder inspect(Inspection... inspections) {
        addInspections(inspections);
        return groupsExecutor;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.impl.client.execution.WarpGroup#addInspections(org.jboss.arquillian.warp.ServerInspection[])
     */
    @Override
    public void addInspections(Inspection... inspections) {
        this.inspections = inspections;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.impl.client.execution.WarpGroup#getFilter()
     */
    @Override
    public RequestObserver getObserver() {
        return observer;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.result.WarpGroupResult#getInspection()
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inspection> T getInspection() {
        return (T) payloads.values().iterator().next().getInspections().get(0);
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.result.WarpGroupResult#getInspectionForHitNumber(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Inspection> T getInspectionForHitNumber(int hitNumber) {
        return (T) getInspectionsForHitNumber(hitNumber).get(0);
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.result.WarpGroupResult#getInspections()
     */
    @Override
    public List<Inspection> getInspections() {
        return payloads.values().iterator().next().getInspections();
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.result.WarpGroupResult#getInspectionsForHitNumber(int)
     */
    @Override
    public List<Inspection> getInspectionsForHitNumber(int hitNumber) {
        ResponsePayload payload = (ResponsePayload) payloads.values().toArray()[hitNumber];
        return payload.getInspections();
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
    public RequestPayload generateRequestPayload(Request request) {
        if (payloads.size() + 1 > expectCount) {
            throw new TooManyRequestsException(this, request);
        }
        RequestPayload requestPayload = new RequestPayload(inspections);
        requests.put(requestPayload.getSerialId(), request);
        payloads.put(requestPayload.getSerialId(), null);
        return requestPayload;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.impl.client.execution.WarpGroup#pushResponsePayload(org.jboss.arquillian.warp.impl.shared.ResponsePayload)
     */
    public boolean pushResponsePayload(ResponsePayload responsePayload) {
        if (payloads.containsKey(responsePayload.getSerialId())) {
            payloads.put(responsePayload.getSerialId(), responsePayload);
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.impl.client.execution.WarpGroup#getFirstNonSuccessfulResult()
     */
    public TestResult getFirstNonSuccessfulResult() {
        for (ResponsePayload payload : payloads.values()) {

            if (payload != null) {

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
        }

        return null;
    }

    @Override
    public String toString() {
        return "WarpGroupImpl [id=" + id + "]";
    }

    @Override
    public int getExpectedRequestCount() {
        return expectCount;
    }

    @Override
    public Collection<Request> getRequestsWithoutResponse() {
        List<Request> requestsWithoutResponse = new LinkedList<Request>();
        for (Entry<Long, ResponsePayload> payload : payloads.entrySet()) {
            if (payload.getValue() == null) {
                requestsWithoutResponse.add(requests.get(payload.getKey()));
            }
        }
        return Collections.unmodifiableCollection(requestsWithoutResponse);
    }

    @Override
    public Collection<Request> getAllRequests() {
        return Collections.unmodifiableCollection(requests.values());
    }
}