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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.RequestExecutorInjector;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.client.execution.WarpClientActionBuilder;
import org.jboss.arquillian.warp.client.execution.WarpRuntime;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionContext;
import org.jboss.arquillian.warp.impl.client.testbase.AbstractWarpClientTestTestBase;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Lukas Fryc
 */
@RunWith(MockitoJUnitRunner.class)
// TODO add multiple request execution tests
public class TestRequestExecutionSynchronization extends AbstractWarpClientTestTestBase {

    private CountDownLatch requestStarted;
    private CountDownLatch responseFinished;
    private CountDownLatch actionFinished;

    @Mock
    private ServiceLoader serviceLoader;

    @Mock
    private ServerAssertion serverAssertion;

    private static AtomicReference<Exception> failure = new AtomicReference<Exception>(null);
    private static AtomicReference<WarpClientActionBuilder> requestExecutor = new AtomicReference<WarpClientActionBuilder>();
    private static AtomicReference<WarpContext> warpContextReference = new AtomicReference<WarpContext>();

    @Inject
    private Instance<WarpContext> warpContext;

    @Inject
    private Instance<WarpExecutionContext> warpExecutionContext;

    @Before
    public void initialize() throws Exception {
        requestStarted = new CountDownLatch(1);
        responseFinished = new CountDownLatch(1);
        actionFinished = new CountDownLatch(1);

        WarpRequestSpecifier requestExecutor = new DefaultWarpRequestSpecifier();
        getManager().inject(requestExecutor);
        TestRequestExecutionSynchronization.requestExecutor.set(requestExecutor);

        ExecutionSynchronizer assertionSynchronizer = new DefaultExecutionSynchronizer();
        getManager().inject(assertionSynchronizer);

        WarpExecutor warpExecutor = new DefaultWarpExecutor();
        getManager().inject(warpExecutor);

        WarpRuntime warpRuntime = new DefaultWarpRuntime();
        getManager().inject(warpRuntime);

        WarpContext warpContext = new WarpContextImpl();

        when(serviceLoader.onlyOne(WarpRequestSpecifier.class)).thenReturn(requestExecutor);
        when(serviceLoader.onlyOne(ExecutionSynchronizer.class)).thenReturn(assertionSynchronizer);
        when(serviceLoader.onlyOne(WarpExecutor.class)).thenReturn(warpExecutor);
        when(serviceLoader.onlyOne(WarpRuntime.class)).thenReturn(warpRuntime);
        when(serviceLoader.onlyOne(WarpContext.class)).thenReturn(warpContext);

        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);

        fire(new BeforeClass(TestingClass.class));
    }

    @After
    public void finalize() {
        fire(new AfterClass(TestingClass.class));
    }

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(RequestExecutorInjector.class);
        extensions.add(DefaultWarpRequestSpecifier.class);
        extensions.add(WarpExecutionObserver.class);
        extensions.add(WarpExecutionInitializer.class);
    }

    @Test
    public void test_zero_requests_execution() {

        assertBefore();

        Warp.execute(new ClientAction() {
            public void action() {
                assertDuringClientAction();
            }
        }).group().expectCount(0).verify(serverAssertion).verifyAll();

        assertAfter();
    }

    @Test
    public void test_single_request_with_blocking_client_action() throws Exception {

        assertBefore();

        new Thread(new Runnable() {
            public void run() {
                awaitSafely(requestStarted);
                handshake();
                responseFinished.countDown();
            }
        }).start();

        Warp.execute(new ClientAction() {
            public void action() {
                assertDuringClientAction();
                prepareForRequest();
                requestStarted.countDown();
                actionFinished.countDown();
            }
        }).verify(serverAssertion);

        assertAfter();

        awaitSafely(actionFinished);
    }

    @Test
    public void test_single_request_with_blocking_client_action_with_waiting_for_requests() {

        assertBefore();

        new Thread(new Runnable() {
            public void run() {
                awaitSafely(requestStarted);
                if (warpContextReference.get().getSynchronization().isWaitingForRequests()) {
                    handshake();
                }
                responseFinished.countDown();
            }
        }).start();

        Warp.execute(new ClientAction() {
            public void action() {
                assertDuringClientAction();
                prepareForRequest();
                requestStarted.countDown();
                actionFinished.countDown();
            }
        }).verify(serverAssertion);

        assertAfter();

        awaitSafely(actionFinished);
    }

    @Test
    public void test_single_request_with_nonblocking_client_action() throws Exception {

        assertBefore();

        new Thread(new Runnable() {
            public void run() {
                awaitSafely(requestStarted);
                try {
                    handshake();
                } catch (Exception e) {
                    e.printStackTrace();
                    failure.set(e);
                } finally {
                    responseFinished.countDown();
                }
            }
        }).start();

        Warp.execute(new ClientAction() {
            public void action() {
                assertDuringClientAction();
                prepareForRequest();
                requestStarted.countDown();
            }
        }).verify(serverAssertion);
        awaitSafely(responseFinished);

        assertAfter();

        if (failure.get() != null) {
            throw failure.get();
        }
    }

    private void prepareForRequest() {
        WarpContext warpContext = this.warpContext.get();
        warpContextReference.set(warpContext);
    }

    private void handshake() {
        awaitSafely(requestStarted);
        WarpContext warpContext = warpContextReference.get();
        assertNotNull("WarpContext should be available", warpContext);
        WarpGroup group = warpContext.getAllGroups().iterator().next();
        RequestPayload requestPayload = group.generateRequestPayload();
        ResponsePayload responsePayload = new ResponsePayload(requestPayload.getSerialId());
        responsePayload.setAssertions(requestPayload.getAssertions());
        warpContext.pushResponsePayload(responsePayload);
    }

    private void assertDuringClientAction() {
        assertTrue("warp execution context should be active", warpExecutionContext.get().isActive());
        WarpContext warpContext = TestRequestExecutionSynchronization.this.warpContext.get();
        assertNotNull("WarpContext should be available", warpContext);
    }

    private void assertBefore() {
        assertFalse("warp execution context shouldn't be active before request", warpExecutionContext.get().isActive());
    }

    private void assertAfter() {
        assertFalse("warp execution context shouldn't be active after response", warpExecutionContext.get().isActive());
    }

    private void awaitSafely(CountDownLatch latch) {
        try {
            latch.await(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @WarpTest
    public static final class TestingClass {
    }
}
