/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.execution;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.RequestExecution;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.assertion.RequestPayload;
import org.jboss.arquillian.warp.assertion.ResponsePayload;

/**
 * The implementation of execution of client action and server assertion.
 * 
 * @author Lukas Fryc
 * 
 */
public class RequestExecutionImpl implements RequestExecution {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ClientAction action;
    private ServerAssertion assertion;
    private FutureTask<ResponsePayload> payloadFuture;

    public RequestExecutionImpl(ClientAction action) {
        this.action = action;
    }

    @SuppressWarnings("unchecked")
    public <T extends ServerAssertion> T verify(T assertion) {
        this.assertion = assertion;
        execute();
        return (T) this.assertion;
    }

    private void execute() {
        setupServerAssertion();
        executeClientAction();
        awaitServerExecution();
    }

    private void setupServerAssertion() {
        AssertionHolder.advertise();

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
            if (throwable instanceof AssertionError) {
                throw (AssertionError) throwable;
            } else {
                throw new ServerExecutionException(responsePayload.getThrowable());
            }
        }

        assertion = responsePayload.getAssertion();
    }

    public class PushAssertion implements Callable<ResponsePayload> {
        @Override
        public ResponsePayload call() throws Exception {
            AssertionHolder.pushRequest(new RequestPayload(assertion));
            return AssertionHolder.popResponse();
        }
    }

    public static class ClientActionException extends RuntimeException {
        private static final long serialVersionUID = 7267806785171391801L;

        public ClientActionException(Throwable cause) {
            super(cause);
        }
    }

}