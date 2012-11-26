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

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.client.execution.GroupVerificationSpecifier;
import org.jboss.arquillian.warp.client.execution.SingleVerificationSpecifier;
import org.jboss.arquillian.warp.client.execution.WarpVerificationBuilder;
import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.result.WarpResult;
import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;
import org.jboss.arquillian.warp.exception.ServerWarpExecutionException;
import org.jboss.arquillian.warp.impl.client.event.ExecuteWarp;

/**
 * The implementation of execution of client action and server assertion.
 *
 * @author Lukas Fryc
 */
public class DefaultWarpRequestSpecifier implements WarpRequestSpecifier {

    private int groupSequenceNumber = 0;

    private WarpContext warpContext;

    private ClientAction action;

    private WarpGroup singleGroup;

    @Inject
    private Event<ExecuteWarp> executeWarp;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.WarpClientActionBuilder#execute(org.jboss.arquillian.warp.ClientAction)
     */
    @Override
    public WarpVerificationBuilder execute(ClientAction action) {
        ensureContextInitialized();
        this.action = action;
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.SingleVerificationSpecifier#verify(org.jboss.arquillian.warp.ServerAssertion)
     */
    @SuppressWarnings("unchecked")
    public <T extends ServerAssertion> T verify(T assertion) {
        initializeSingleGroup();
        singleGroup.addAssertions(assertion);
        WarpResult result = execute();
        return (T) result.getGroup(SingleVerificationSpecifier.KEY).getAssertion();
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.SingleVerificationSpecifier#verifyAll(org.jboss.arquillian.warp.ServerAssertion[])
     */
    @Override
    public WarpResult verifyAll(ServerAssertion... assertions) {
        initializeSingleGroup();
        singleGroup.addAssertions(assertions);
        return execute();
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.GroupVerificationBuilder#verifyAll()
     */
    @Override
    public WarpResult verifyAll() {
        return execute();
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.GroupSpecifier#group()
     */
    @Override
    public GroupVerificationSpecifier group() {
        return group(groupSequenceNumber++);
    }

    @Override
    public GroupVerificationSpecifier group(Object identifier) {
        return new WarpGroupImpl(this, identifier);
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.FilterSpecifier#filter(org.jboss.arquillian.warp.client.filter.RequestFilter)
     */
    @Override
    public SingleVerificationSpecifier filter(RequestFilter<?> filter) {
        initializeSingleGroup();
        singleGroup.filter(filter);
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.client.execution.FilterSpecifier#filter(java.lang.Class)
     */
    @Override
    public SingleVerificationSpecifier filter(Class<? extends RequestFilter<?>> filterClass) {
        initializeSingleGroup();
        singleGroup.filter(createFilterInstance(filterClass));
        return this;
    }

    @SuppressWarnings("unchecked")
    private <T extends RequestFilter<?>> T createFilterInstance(Class<T> filterClass) {
        return (T) SecurityActions.newInstance(filterClass.getName(), new Class<?>[] {}, new Object[] {}, RequestFilter.class);
    }

    private WarpResult execute() {

        try {
            executeWarp.fire(new ExecuteWarp(action, warpContext));

            Exception executionException = warpContext.getFirstException();

            if (executionException != null) {
                propagateException(executionException);
            }

            return warpContext.getResult();
        } finally {
            finalizeContext();
        }
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
        }
    }

    private void finalizeContext() {
        warpContext = null;
        singleGroup = null;
    }

    private void initializeSingleGroup() {
        if (singleGroup == null) {
            singleGroup = new WarpGroupImpl(this, SingleVerificationSpecifier.KEY);
            warpContext.addGroup(singleGroup);
        }
    }

    // TODO remove exception
    public static class ClientActionException extends RuntimeException {
        private static final long serialVersionUID = 7267806785171391801L;

        public ClientActionException(Throwable cause) {
            super(cause);
        }
    }
}