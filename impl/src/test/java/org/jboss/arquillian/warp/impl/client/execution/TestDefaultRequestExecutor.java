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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.client.execution.RequestExecutor;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionScoped;
import org.jboss.arquillian.warp.impl.client.testbase.AbstractWarpClientTestTestBase;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Lukas Fryc
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultRequestExecutor extends AbstractWarpClientTestTestBase {

    @Mock
    private ClientAction action;

    @Mock
    private ServerAssertion assertion;

    private RequestExecutor requestExecutor;

    @Inject
    @WarpExecutionScoped
    private InstanceProducer<ResponsePayload> payload;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        super.addExtensions(extensions);

        extensions.add(FailClientObserver.class);
    }

    @Before
    public void before() {
        this.requestExecutor = new DefaultRequestExecutor();
        getManager().inject(requestExecutor);
        payload.set(new ResponsePayload());
    }

    @Test
    public void when_client_action_fails_and_request_result_is_failure_then_server_failure_should_be_reported() {
        // having
        TestResult result = new TestResult(Status.FAILED, new RuntimeException("server"));
        payload.get().setTestResult(result);

        // when
        try {
            requestExecutor.execute(action).verify(assertion);
            fail("client action should fail the request");
        } catch (RuntimeException e) {
            assertEquals("server", e.getMessage());
        }
    }

    static class FailClientObserver {

        public void failClientAction(@Observes ClientAction action) {
            throw new RuntimeException("client");
        }
    }
}
