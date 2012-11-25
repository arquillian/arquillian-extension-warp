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
package org.jboss.arquillian.warp;

import static org.mockito.Mockito.mock;

import java.lang.reflect.Modifier;

import org.jboss.arquillian.warp.client.execution.RequestExecutor;
import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.filter.http.HttpRequest;
import org.jboss.arquillian.warp.client.result.WarpResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@SuppressWarnings({"unused", "serial"})
public class TestExecutionAPI {

    private ClientAction clientAction;

    private ServerAssertion serverAssertion;

    private RequestFilter<?> requestFilter;


    /**
     * Single client action paired with single server assertion for most simplest
     * cases
     */
    public void testSimpleExecution() {
        Warp.execute(clientAction)
            .verify(serverAssertion);
    }

    /**
     * Single client action and server assertion applied for request matching
     * given filter
     */
    public void testSimpleFiltering() {
        Warp.execute(clientAction)
            .filter(requestFilter)
            .verify(serverAssertion);
    }

    /**
     * The result of simplest possible execution is ServerAssertion (modified
     * on the server)
     */
    public void testSimpleResult() {
        ServerAssertion assertion = Warp.execute(clientAction)
            .verify(serverAssertion);
    }

    /**
     * Two requests caused by single client action are verified concurrently.
     */
    public void testGroupOfTwoRequests() {
        Warp.execute(clientAction)
            .group()
                .filter(requestFilter)
                .verify(serverAssertion)
            .group()
                .filter(requestFilter)
                .verify(serverAssertion)
            .verifyAll();
    }

    /**
     * Complex Warp executions stores their results inside {@link WarpResult}
     * object where result of assertion and other details (e.g. filter hit
     * count) is stored.
     */
    public void testResultOfComplexGroupExecution() {
        WarpResult result = Warp.execute(clientAction)
            .group("first")
                .filter(requestFilter)
                .verify(serverAssertion)
            .group("second")
                .filter(requestFilter)
                .verify(serverAssertion)
            .verifyAll();


        ServerAssertion firstAssertion = result.getGroup("first").getAssertion();

        int hitCount = result.getGroup("second").getHitCount();
    }

    /**
     * Test may specify multiple assertions verified in one request.
     *
     * These assertions will preserve order of definition and execution.
     */
    public void testMultipleAssertions() {
        WarpResult result = Warp.execute(clientAction)
            .verifyAll(serverAssertion, serverAssertion, serverAssertion);

        result = Warp.execute(clientAction)
            .group()
                .filter(requestFilter)
                .verify(serverAssertion, serverAssertion)
            .group()
                .filter(requestFilter)
                .verify(serverAssertion, serverAssertion, serverAssertion)
            .verifyAll();
    }

    /**
     * Filters can be specified by annotation - then they will be applied in any
     * Warp execution where no other filter was specified.
     */
    @Filter(TestingFilter.class)
    public void testFilterSpecifiedByAnnotation() {
        Warp.execute(clientAction)
            .verify(serverAssertion);
    }

    /**
     * Assertions can be specified by annotation - all specified assertions
     * will be used for all requests.
     */
    @Verify({TestingAssertion1.class, TestingAssertion2.class})
    public void testSpecifyAssertionByAnnotation() {
        clientAction.action();
    }

    private static abstract class TestingFilter implements RequestFilter<HttpRequest> {
    }

    private static abstract class TestingAssertion1 extends ServerAssertion {
    }

    private static abstract class TestingAssertion2 extends ServerAssertion {
    }
}
