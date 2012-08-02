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
package org.jboss.arquillian.warp.client.execution;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.RequestExecution;
import org.jboss.arquillian.warp.RequestFilter;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;
import org.jboss.arquillian.warp.exception.ServerWarpExecutionException;
import org.jboss.arquillian.warp.shared.RequestPayload;
import org.jboss.arquillian.warp.shared.ResponsePayload;

/**
 * The implementation of execution of client action and server assertion.
 *
 * @author Lukas Fryc
 *
 */
public class RequestExecutionImpl implements RequestExecution {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ClientAction action;
    private RequestFilter<?> filter;
    private ServerAssertion assertion;
    private FutureTask<ResponsePayload> payloadFuture;

    public RequestExecutionImpl(ClientAction action) {
        this.action = action;
    }

    public RequestExecutionImpl(RequestFilter<?> filter) {
        this.filter = filter;
    }

    @SuppressWarnings("unchecked")
    public <T extends ServerAssertion> T verify(T assertion) {
        this.assertion = assertion;
        execute();
        return (T) this.assertion;
    }

    @Override
    public RequestExecution filter(RequestFilter<?> filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public RequestExecution execute(ClientAction action) {
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
        AssertionHolder.advertise();
        AssertionHolder.setExpectedRequests(1);

        RequestPayload payload = new RequestPayload(assertion);
        RequestEnrichment request = new RequestEnrichment(payload, filter);
        AssertionHolder.addRequest(request);

        AssertionHolder.finished();

        payloadFuture = new FutureTask<ResponsePayload>(new PushAssertion());
        executor.submit(payloadFuture);
    }

    private void executeClientAction() {
        try {
            action.action();
        } catch (Exception e) {
            throw new ClientActionException(e);
        }
    }

    private void awaitServerExecution() {
        ResponsePayload responsePayload;

        try {
            responsePayload = payloadFuture.get();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Throwable throwable = responsePayload.getThrowable();
        if (throwable != null) {
            propagateFailure(throwable);
        }

        assertion = responsePayload.getAssertion();
    }

    private void cleanup() {
        AssertionHolder.finishEnrichmentRound();
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

    public class PushAssertion implements Callable<ResponsePayload> {
        @Override
        public ResponsePayload call() throws Exception {
            Set<ResponseEnrichment> responses = AssertionHolder.getResponses();
            ResponseEnrichment response = responses.iterator().next();
            return response.getPayload();

        }
    }

    public static class ClientActionException extends RuntimeException {
        private static final long serialVersionUID = 7267806785171391801L;

        public ClientActionException(Throwable cause) {
            super(cause);
        }
    }

}