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
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.client.execution.RequestExecutor;
import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;
import org.jboss.arquillian.warp.exception.ServerWarpExecutionException;
import org.jboss.arquillian.warp.impl.client.event.InstallEnrichment;
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
public class DefaultRequestExecutor implements RequestExecutor {

    private ClientAction action;
    private RequestFilter<?> filter;
    private ServerAssertion assertion;

    @Inject
    private Event<AdvertiseEnrichment> advertiseEnrichment;

    @Inject
    private Event<InstallEnrichment> addEnrichment;

    @Inject
    private Event<FinishEnrichment> finishEnrichment;

    @Inject
    private Event<CleanEnrichment> cleanEnrichment;

    @Inject
    private Event<AwaitResponse> awaitResponse;

    @Inject
    @WarpExecutionScoped
    private InstanceProducer<RequestEnrichment> requestEnrichment;

    @Inject
    private Instance<ResponsePayload> responsePayload;

    @SuppressWarnings("unchecked")
    public <T extends ServerAssertion> T verify(T assertion) {
        this.assertion = assertion;
        execute();
        return (T) this.assertion;
    }

    @Override
    public RequestExecutor filter(RequestFilter<?> filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public RequestExecutor execute(ClientAction action) {
        this.action = action;
        return this;
    }

    private void execute() {
        try {
            setupServerAssertion();
            executeClientAction();
            awaitServerExecution();
        } finally {
            cleanup();
        }
    }

    private void setupServerAssertion() {
        advertiseEnrichment.fire(new AdvertiseEnrichment(1));

        RequestPayload payload = new RequestPayload(assertion);
        requestEnrichment.set(new RequestEnrichment(payload, filter));

        addEnrichment.fire(new InstallEnrichment());

        finishEnrichment.fire(new FinishEnrichment());
    }

    private void executeClientAction() {
        try {
            action.action();
        } catch (Exception e) {
            throw new ClientActionException(e);
        }
    }

    private void awaitServerExecution() {
        awaitResponse.fire(new AwaitResponse());

        Throwable throwable = responsePayload.get().getThrowable();
        if (throwable != null) {
            propagateFailure(throwable);
        }

        assertion = responsePayload.get().getAssertion();
    }

    private void cleanup() {
        cleanEnrichment.fire(new CleanEnrichment());
    }

    private void propagateFailure(Throwable throwable) {
        if (throwable instanceof AssertionError) {
            throw (AssertionError) throwable;
        } else if (throwable instanceof ClientWarpExecutionException) {
            throw (ClientWarpExecutionException) throwable;
        } else {
            throw new ServerWarpExecutionException(throwable);
        }
    }

    public static class ClientActionException extends RuntimeException {
        private static final long serialVersionUID = 7267806785171391801L;

        public ClientActionException(Throwable cause) {
            super(cause);
        }
    }
}