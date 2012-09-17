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

import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.Context;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.ServiceInjector;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.client.execution.RequestExecutor;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionContextImpl;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Lukas Fryc
 */
@SuppressWarnings("serial")
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class TestSingleAssertionExecution extends AbstractManagerTestBase {

    private CountDownLatch requestStarted;
    private CountDownLatch responseFinished;
    private CountDownLatch actionFinished;
    
    @Mock
    ServiceLoader serviceLoader;

    @Before
    public void initialize() {
        requestStarted = new CountDownLatch(1);
        responseFinished = new CountDownLatch(1);
        actionFinished = new CountDownLatch(1);
        
        
        RequestExecutor requestExecutor = new DefaultRequestExecutor();
        getManager().inject(requestExecutor);
        
        when(serviceLoader.onlyOne(RequestExecutor.class)).thenReturn(requestExecutor);
        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        
        fire(new BeforeClass(TestingClass.class));
    }
    
    @After
    public void finalize() {
        fire(new AfterClass(TestingClass.class));
    }
    
    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(ServiceInjector.class);
        extensions.add(DefaultRequestExecutor.class);
    }
    
    @Override
    protected void addContexts(List<Class<? extends Context>> contexts) {
        contexts.add(WarpExecutionContextImpl.class);
    }

    @Test //(timeout = 5000)
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
        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setAssertion(request.getPayload().getAssertion());
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
    
    @WarpTest
    public static final class TestingClass {
    }
}
