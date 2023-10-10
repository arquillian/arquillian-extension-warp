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

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.RequestObserver;
import org.jboss.arquillian.warp.client.execution.GroupExecutionSpecifier;
import org.jboss.arquillian.warp.client.execution.SingleInspectionSpecifier;
import org.jboss.arquillian.warp.client.execution.WarpExecutionBuilder;
import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.observer.ObserverBuilder;
import org.jboss.arquillian.warp.client.result.WarpResult;
import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;
import org.jboss.arquillian.warp.exception.ServerWarpExecutionException;
import org.jboss.arquillian.warp.impl.client.event.ExecuteWarp;

/**
 * The implementation of execution of client activity and server inspection.
 *
 * @author Lukas Fryc
 */
public class DefaultWarpRequestSpecifier implements WarpRequestSpecifier {

    private int groupSequenceNumber = 0;

    private WarpContext warpContext;

    private Activity activity;

    private WarpGroup singleGroup;

    @Inject
    private Event<ExecuteWarp> executeWarp;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.WarpActivityBuilder#execute(org.jboss.arquillian.warp.Activity)
     */
    @Override
    public WarpExecutionBuilder initiate(Activity activity) {
        ensureContextInitialized();
        this.activity = activity;
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.SingleVerificationSpecifier#verify(org.jboss.arquillian.warp.ServerInspection)
     */
    public <T extends Inspection> T inspect(T inspection) {
        initializeSingleGroup();
        singleGroup.addInspections(inspection);
        WarpResult result = execute();
        return result.getGroup(SingleInspectionSpecifier.GROUP_ID).getInspection();
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.SingleVerificationSpecifier#verifyAll(org.jboss.arquillian.warp.ServerInspection[])
     */
    @Override
    public WarpResult inspectAll(Inspection... inspections) {
        initializeSingleGroup();
        singleGroup.addInspections(inspections);
        return execute();
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.GroupVerificationBuilder#verifyAll()
     */
    @Override
    public WarpResult execute() {
        try {
            executeWarp.fire(new ExecuteWarp(activity, warpContext));

            Exception executionException = warpContext.getFirstException();
            if (executionException != null) {
                propagateException(executionException);
            }

            return warpContext.getResult();
        } finally {
            finalizeContext();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.GroupSpecifier#group()
     */
    @Override
    public GroupExecutionSpecifier group() {
        return group(groupSequenceNumber++);
    }

    @Override
    public GroupExecutionSpecifier group(Object identifier) {
        WarpGroup group = new WarpGroupImpl(this, identifier);
        warpContext.addGroup(group);
        return group;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.FilterSpecifier#filter(org.jboss.arquillian.warp.client.filter.RequestFilter)
     */
    @Override
    public SingleInspectionSpecifier observe(RequestObserver what) {
        initializeSingleGroup();
        singleGroup.observe(what);
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.FilterSpecifier#filter(java.lang.Class)
     */
    @Override
    public SingleInspectionSpecifier observe(Class<? extends RequestObserver> what) {
        initializeSingleGroup();
        singleGroup.observe(createFilterInstance(what));
        return this;
    }

    @Override
    public SingleInspectionSpecifier observe(ObserverBuilder<?, ?> filterBuilder) {
        initializeSingleGroup();
        singleGroup.observe(filterBuilder.build());
        return this;
    }

    @SuppressWarnings("unchecked")
    private <T extends RequestObserver> T createFilterInstance(Class<T> filterClass) {
        return (T) SecurityActions.newInstance(filterClass.getName(), new Class<?>[] {}, new Object[] {},
            RequestFilter.class);
    }

    private void propagateException(Throwable e) {
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

    private void ensureContextInitialized() {
        if (warpContext == null) {
            warpContext = serviceLoader.get().onlyOne(WarpContext.class);

            warpContext.initialize(serviceLoader.get());
        }
    }

    private void finalizeContext() {
        warpContext = null;
        singleGroup = null;
    }

    private void initializeSingleGroup() {
        if (singleGroup == null) {
            singleGroup = new WarpGroupImpl(this, SingleInspectionSpecifier.GROUP_ID);
            warpContext.addGroup(singleGroup);
        }
    }

    // TODO remove exception
    public static class ActivityException extends RuntimeException {
        private static final long serialVersionUID = 7267806785171391801L;

        public ActivityException(Throwable cause) {
            super(cause);
        }
    }
}