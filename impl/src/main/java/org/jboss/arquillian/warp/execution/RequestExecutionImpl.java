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

/**
 * The execution of client action and server assertion.
 * 
 * @author Lukas Fryc
 * 
 */
public class RequestExecutionImpl implements RequestExecution {

    private ClientAction action;
    // TODO AtomicReference
    private ServerAssertion assertion;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public RequestExecutionImpl(ClientAction action) {
        this.action = action;
    }

    public <T extends ServerAssertion> T verify(T assertion) {
        this.assertion = assertion;
        execute();
        return assertion;
    }

    private void execute() {
        AssertionHolder.advertise();
        FutureTask<ServerAssertion> future = new FutureTask<ServerAssertion>(new PushAssertion());
        executor.submit(future);
        try {
            action.action();
        } catch (Exception e) {
            throw new ClientActionException(e);
        }
        try {
            assertion = future.get();
        } catch (Exception e) {
            throw new ServerAssertionException(e);
        }
    }

    public class PushAssertion implements Callable<ServerAssertion> {
        @Override
        public ServerAssertion call() throws Exception {
            AssertionHolder.pushRequest(assertion);
            return AssertionHolder.popResponse();
        }
    }

    public static class ClientActionException extends RuntimeException {
        private static final long serialVersionUID = 7267806785171391801L;

        public ClientActionException(Throwable cause) {
            super(cause);
        }
    }

    public static class ServerAssertionException extends RuntimeException {
        private static final long serialVersionUID = -5318390607884452966L;

        public ServerAssertionException(Throwable cause) {
            super(cause);
        }
    }

}