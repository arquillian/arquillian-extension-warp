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

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.execution.AssertionHolder;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Lukas Fryc
 */
@SuppressWarnings("serial")
public class TestAssertionExecution {

    private final static Logger log = Logger.getLogger(TestAssertionExecution.class.getName());

    private CountDownLatch requestStarted;
    private CountDownLatch responseFinished;
    private CountDownLatch actionFinished;

    @Before
    public void initialize() {
        requestStarted = new CountDownLatch(1);
        responseFinished = new CountDownLatch(1);
        actionFinished = new CountDownLatch(1);
    }

    @Test(timeout=5000)
    public void testBlocking() {
        new Thread(new Runnable() {
            public void run() {
                awaitSafely(requestStarted);
                ServerAssertion assertion = AssertionHolder.popRequest();
                AssertionHolder.pushResponse(assertion);
                log.info("response finished");
                responseFinished.countDown();
            }
        }).start();

        Warp.execute(new ClientAction() {
            public void action() {
                log.info("request started");
                requestStarted.countDown();
                awaitSafely(responseFinished);
                log.info("action finished");
                actionFinished.countDown();
            }
        }).verify(new ServerAssertion() {
        });

        awaitSafely(actionFinished);
    }
    
    @Test(timeout=5000)
    public void testBlocking_modified() {
        new Thread(new Runnable() {
            public void run() {
                awaitSafely(requestStarted);
                if (AssertionHolder.isWaitingForProcessing()) {
                    ServerAssertion assertion = AssertionHolder.popRequest();
                    AssertionHolder.pushResponse(assertion);
                }
                log.info("response finished");
                responseFinished.countDown();
            }
        }).start();

        Warp.execute(new ClientAction() {
            public void action() {
                log.info("request started");
                requestStarted.countDown();
                awaitSafely(responseFinished);
                log.info("action finished");
                actionFinished.countDown();
            }
        }).verify(new ServerAssertion() {
        });

        awaitSafely(actionFinished);
    }

    @Test(timeout=5000)
    public void testNonBlocking() {
        new Thread(new Runnable() {
            public void run() {
                awaitSafely(requestStarted);
                ServerAssertion assertion = AssertionHolder.popRequest();
                AssertionHolder.pushResponse(assertion);
                log.info("response finished");
                responseFinished.countDown();
            }
        }).start();

        Warp.execute(new ClientAction() {
            public void action() {
                log.info("request started");
                requestStarted.countDown();
            }
        }).verify(new ServerAssertion() {
        });
        awaitSafely(responseFinished);
    }

    private void awaitSafely(CountDownLatch latch) {
        try {
            latch.await();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
