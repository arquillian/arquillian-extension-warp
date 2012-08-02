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
import java.util.concurrent.CountDownLatch;

import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.shared.ResponsePayload;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Lukas Fryc
 */
@SuppressWarnings("serial")
public class TestSingleAssertionExecution {

    private CountDownLatch requestStarted;
    private CountDownLatch responseFinished;
    private CountDownLatch actionFinished;

    @Before
    public void initialize() {
        requestStarted = new CountDownLatch(1);
        responseFinished = new CountDownLatch(1);
        actionFinished = new CountDownLatch(1);
    }

    @Test(timeout = 5000)
    public void testBlocking() {
        new Thread(new Runnable() {
            public void run() {
                awaitSafely(requestStarted);
                handshake();
                responseFinished.countDown();
            }
        }).start();

        Warp.execute(new ClientAction() {
            public void action() {
                requestStarted.countDown();
                actionFinished.countDown();
            }
        }).verify(new ServerAssertion() {
        });

        awaitSafely(actionFinished);
    }

    @Test(timeout = 5000)
    public void testBlocking_with_waiting_for_requests() {
        new Thread(new Runnable() {
            public void run() {
                awaitSafely(requestStarted);
                if (AssertionHolder.isWaitingForRequests()) {
                    handshake();
                }
                responseFinished.countDown();
            }
        }).start();

        Warp.execute(new ClientAction() {
            public void action() {
                requestStarted.countDown();
                actionFinished.countDown();
            }
        }).verify(new ServerAssertion() {
        });

        awaitSafely(actionFinished);
    }

    @Test(timeout = 5000)
    public void testNonBlocking() {
        new Thread(new Runnable() {
            public void run() {
                awaitSafely(requestStarted);
                handshake();
                responseFinished.countDown();
            }
        }).start();

        Warp.execute(new ClientAction() {
            public void action() {
                requestStarted.countDown();
            }
        }).verify(new ServerAssertion() {
        });
        awaitSafely(responseFinished);
    }

    private static void handshake() {
        Set<RequestEnrichment> requests = AssertionHolder.getRequests();

        RequestEnrichment request = requests.iterator().next();
        ResponsePayload responsePayload = new ResponsePayload(request.getPayload().getAssertion());
        ResponseEnrichment response = new ResponseEnrichment(responsePayload);

        AssertionHolder.addResponse(response);
    }

    private void awaitSafely(CountDownLatch latch) {
        try {
            latch.await();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
