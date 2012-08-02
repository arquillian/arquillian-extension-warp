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
package org.jboss.arquillian.warp.server.filter;

import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletRequest;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.extension.servlet.AfterServletEvent;
import org.jboss.arquillian.warp.extension.servlet.BeforeServletEvent;
import org.jboss.arquillian.warp.server.assertion.AssertionRegistry;
import org.jboss.arquillian.warp.server.lifecycle.LifecycleManagerImpl;
import org.jboss.arquillian.warp.server.lifecycle.LifecycleManagerStoreImpl;
import org.jboss.arquillian.warp.server.request.AfterRequest;
import org.jboss.arquillian.warp.server.request.BeforeRequest;
import org.jboss.arquillian.warp.server.test.TestResultStore;
import org.jboss.arquillian.warp.shared.ResponsePayload;
import org.jboss.arquillian.warp.spi.WarpCommons;

/**
 * The lifecycle of Warp request verification
 * 
 * @author Lukas Fryc
 */
public class WarpLifecycle {

    @Inject
    private Instance<LifecycleManagerImpl> lifecycleManager;

    @Inject
    private Instance<LifecycleManagerStoreImpl> lifecycleManagerStore;

    @Inject
    private Instance<AssertionRegistry> assertionRegistry;

    @Inject
    private Instance<TestResultStore> testResultStore;

    /**
     * Executes the lifecycle
     * 
     * @return {@link ResponsePayload} based on the lifecycle tests results
     */
    public ResponsePayload execute(Manager manager, ServletRequest request, DoFilterCommand filterCommand,
            ServerAssertion serverAssertion) throws Throwable {
        try {
            request.setAttribute(WarpCommons.LIFECYCLE_MANAGER_STORE_REQUEST_ATTRIBUTE, lifecycleManagerStore);
            
            manager.fire(new BeforeSuite());
            manager.fire(new BeforeRequest(request));

            lifecycleManagerStore.get().bind(ServletRequest.class, request);
            assertionRegistry.get().registerAssertion(serverAssertion);
            lifecycleManager.get().fireLifecycleEvent(new BeforeServletEvent());

            filterCommand.executeFilterChain();

            lifecycleManager.get().fireLifecycleEvent(new AfterServletEvent());

            return processTestResult(serverAssertion);
        } catch (Throwable e) {
            // TODO log exception
            e.printStackTrace();
            throw e;
        } finally {
            assertionRegistry.get().unregisterAssertion(serverAssertion);

            lifecycleManagerStore.get().unbind(ServletRequest.class, request);

            manager.fire(new AfterRequest(request));
            manager.fire(new AfterSuite());
        }
    }

    /**
     * Processes the test results and returns appropriate {@link ResponsePayload} for successful or failed lifecycle tests.
     * 
     * The successful lifecycle tests is where no test failed. Failed test is test where at least on lifecycle test failed.
     * 
     * @param serverAssertion
     * @return appropriate {@link ResponsePayload} for successful or failed lifecycle tests
     */
    private ResponsePayload processTestResult(ServerAssertion serverAssertion) {
        TestResult firstFailedResult = testResultStore.get().getFirstFailed();

        if (firstFailedResult == null) {
            return new ResponsePayload(serverAssertion);
        } else {
            Throwable throwable = firstFailedResult.getThrowable();
            if (throwable instanceof InvocationTargetException) {
                throwable = throwable.getCause();
            }
            return new ResponsePayload(throwable);
        }
    }
}
