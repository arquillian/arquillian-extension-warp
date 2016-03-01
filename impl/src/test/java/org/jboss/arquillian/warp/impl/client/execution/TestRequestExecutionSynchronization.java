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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpRuntimeInitializer;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.client.execution.SingleInspectionSpecifier;
import org.jboss.arquillian.warp.client.execution.WarpActivityBuilder;
import org.jboss.arquillian.warp.client.execution.WarpRuntime;
import org.jboss.arquillian.warp.client.filter.Request;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionContext;
import org.jboss.arquillian.warp.impl.client.testbase.AbstractWarpClientTestTestBase;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Lukas Fryc
 */
@RunWith(MockitoJUnitRunner.class)
// TODO add multiple request execution tests
public class TestRequestExecutionSynchronization extends AbstractWarpClientTestTestBase {

    private CountDownLatch requestStarted;
    private CountDownLatch responseFinished;
    private CountDownLatch activityFinished;

    @Mock
    private ServiceLoader serviceLoader;

    @Mock
    private Inspection serverInspection;

    private static AtomicReference<Throwable> failure = new AtomicReference<Throwable>(null);
    private static AtomicReference<WarpActivityBuilder> requestExecutor = new AtomicReference<WarpActivityBuilder>();
    private static AtomicReference<WarpContext> warpContextReference = new AtomicReference<WarpContext>();

    @Inject
    private Instance<WarpContext> warpContext;

    @Inject
    private Instance<WarpExecutionContext> warpExecutionContext;

    @Before
    public void initialize() throws Exception {
        requestStarted = new CountDownLatch(1);
        responseFinished = new CountDownLatch(1);
        activityFinished = new CountDownLatch(1);

        WarpRequestSpecifier requestExecutor = new DefaultWarpRequestSpecifier();
        getManager().inject(requestExecutor);
        TestRequestExecutionSynchronization.requestExecutor.set(requestExecutor);

        ExecutionSynchronizer inspectionSynchronizer = new DefaultExecutionSynchronizer();
        getManager().inject(inspectionSynchronizer);

        WarpExecutor warpExecutor = new DefaultWarpExecutor();
        getManager().inject(warpExecutor);

        WarpRuntime warpRuntime = new DefaultWarpRuntime();
        getManager().inject(warpRuntime);

        WarpContext warpContext = new WarpContextImpl();

        when(serviceLoader.onlyOne(WarpRequestSpecifier.class)).thenReturn(requestExecutor);
        when(serviceLoader.onlyOne(ExecutionSynchronizer.class)).thenReturn(inspectionSynchronizer);
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
        extensions.add(WarpRuntimeInitializer.class);
        extensions.add(DefaultWarpRequestSpecifier.class);
        extensions.add(WarpExecutionObserver.class);
        extensions.add(WarpExecutionInitializer.class);
    }

    @Test
    public void test_zero_requests_execution() {

        assertBefore();

        Warp.initiate(new Activity() {
            public void perform() {
                assertDuringActivity();
            }
        }).group().expectCount(0).inspect(serverInspection).execute();

        assertAfter();
    }

    @Test
    public void test_single_request_with_blocking_client_activity() throws Exception {

        assertBefore();

        new Thread(new Runnable() {
            public void run() {
                awaitSafely(requestStarted);
                singleRequestHandshake();
                responseFinished.countDown();
            }
        }).start();

        Warp.initiate(new Activity() {
            public void perform() {
                assertDuringActivity();
                prepareForRequest();
                requestStarted.countDown();
                activityFinished.countDown();
            }
        }).inspect(serverInspection);

        assertAfter();

        awaitSafely(activityFinished);
    }

    @Test
    public void test_single_request_with_blocking_client_activity_with_waiting_for_requests() {

        assertBefore();

        new Thread(new Runnable() {
            public void run() {
                awaitSafely(requestStarted);
                if (warpContextReference.get().getSynchronization().isWaitingForRequests()) {
                    singleRequestHandshake();
                }
                responseFinished.countDown();
            }
        }).start();

        Warp.initiate(new Activity() {
            public void perform() {
                assertDuringActivity();
                prepareForRequest();
                requestStarted.countDown();
                activityFinished.countDown();
            }
        }).inspect(serverInspection);

        assertAfter();

        awaitSafely(activityFinished);
    }

    @Test
    public void test_single_request_with_nonblocking_client_activity() throws Throwable {

        assertBefore();

        new Thread(new Runnable() {
            public void run() {
                awaitSafely(requestStarted);
                try {
                    singleRequestHandshake();
                } catch (Exception e) {
                    failure.set(e);
                } finally {
                    responseFinished.countDown();
                }
            }
        }).start();

        Warp.initiate(new Activity() {
            public void perform() {
                assertDuringActivity();
                prepareForRequest();
                requestStarted.countDown();
            }
        }).inspect(serverInspection);
        awaitSafely(responseFinished);

        assertAfter();

        if (failure.get() != null) {
            throw failure.get();
        }
    }

    @Test
    public void test_multiple_request_with_nonblocking_client_activity() throws Throwable {

        final CountDownLatch secondResponseFinished = new CountDownLatch(1);
        final CountDownLatch secondRequestStarted = new CountDownLatch(1);

        assertBefore();

        new Thread(new Runnable() {
            public void run() {
                awaitSafely(requestStarted);
                try {
                    handshakeForGroup(1);
                } catch (Throwable e) {
                    failure.set(e);
                } finally {
                    responseFinished.countDown();
                }
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                awaitSafely(secondRequestStarted);
                try {
                    handshakeForGroup(2);
                } catch (Throwable e) {
                    failure.set(e);
                } finally {
                    secondResponseFinished.countDown();
                }
            }
        }).start();

        Warp
            .initiate(new Activity() {
                public void perform() {
                    assertDuringActivity();
                    prepareForRequest();
                    requestStarted.countDown();
                    secondRequestStarted.countDown();
                }})
            .group(1)
                .inspect(serverInspection)
            .group(2)
                .inspect(serverInspection)
            .execute();

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

    private void singleRequestHandshake() {
        handshakeForGroup(SingleInspectionSpecifier.GROUP_ID);
    }

    private void handshakeForGroup(Object groupId) {
        awaitSafely(requestStarted);
        WarpContext warpContext = warpContextReference.get();
        assertNotNull("WarpContext should be available", warpContext);
        WarpGroup group = warpContext.getGroup(groupId);
        RequestPayload requestPayload = group.generateRequestPayload(mock(Request.class));
        ResponsePayload responsePayload = new ResponsePayload(requestPayload.getSerialId());
        responsePayload.setInspections(requestPayload.getInspections());
        warpContext.pushResponsePayload(responsePayload);
    }

    private void assertDuringActivity() {
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
    @RunAsClient
    public static final class TestingClass {
        @Deployment
        public static Archive deploy(){
            return null;
        }
    }
}
