/*
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

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.impl.client.execution.DefaultWarpRequestSpecifier.ActivityException;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionContext;
import org.jboss.arquillian.warp.impl.client.testbase.AbstractWarpClientTestTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Lukas Fryc
 */
@ExtendWith(MockitoExtension.class)
public class TestDefaultWarpExecutor extends AbstractWarpClientTestTestBase {

    @Mock
    private Activity activity;

    @Mock
    private WarpContext warpContext;

    private WarpExecutor executor;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        super.addExtensions(extensions);

        extensions.add(WarpExecutionInitializer.class);
        extensions.add(ActivityObserver.class);
    }

    @Override
    protected void startContexts(Manager manager) {
        super.startContexts(manager);

        manager.getContext(WarpExecutionContext.class).activate();
    }

    @BeforeEach
    public void before() {
        executor = new DefaultWarpExecutor();
        getManager().inject(executor);
    }

    @Test
    public void when_client_activity_fails_and_request_result_is_failure_then_server_failure_should_be_reported() {
        // given
        TestResult result = TestResult.failed(new RuntimeException("server"));
        when(warpContext.getFirstNonSuccessfulResult()).thenReturn(result);

        doThrow(new RuntimeException("client")).when(activity).perform();

        // when
        try {
            executor.execute(activity, warpContext);
            fail("client activity should fail the request");
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (!"server".equals(message)) {
                throw e;
            }
        }
    }

    @Test
    public void when_server_activity_fails_without_client_failure_then_server_failure_should_be_reported() {
        // given
        TestResult result = TestResult.failed(new RuntimeException("server"));
        when(warpContext.getFirstNonSuccessfulResult()).thenReturn(result);

        // when
        try {
            executor.execute(activity, warpContext);
            fail("server execution should fail the request");
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (!"server".equals(message)) {
                throw e;
            }
        }
    }

    @Test
    public void when_client_activity_fails_without_server_failure_then_client_failure_should_be_reported() {

        // given
        doThrow(new RuntimeException("client")).when(activity).perform();
        when(warpContext.getFirstNonSuccessfulResult()).thenReturn(null);

        // when
        try {
            executor.execute(activity, warpContext);
            fail("server execution should fail the request");
        } catch (ActivityException e) {
            String message = e.getCause().getMessage();
            if (!"client".equals(message)) {
                throw e;
            }
        }
    }

    static class ActivityObserver {

        public void executeActivity(@Observes Activity activity) {
            activity.perform();
        }
    }
}
