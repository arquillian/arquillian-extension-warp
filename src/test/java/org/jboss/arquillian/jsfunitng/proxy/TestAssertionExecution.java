package org.jboss.arquillian.jsfunitng.proxy;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.jboss.arquillian.jsfunitng.ClientAction;
import org.jboss.arquillian.jsfunitng.ServerAssertion;
import org.jboss.arquillian.jsfunitng.Warp;
import org.jboss.arquillian.jsfunitng.proxy.AssertionHolder;
import org.junit.Before;
import org.junit.Test;

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

    @Test
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
    
    @Test
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

    @Test
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
