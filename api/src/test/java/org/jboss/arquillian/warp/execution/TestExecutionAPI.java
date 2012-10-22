package org.jboss.arquillian.warp.execution;

import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.result.WarpResult;
import org.junit.Test;
import org.mockito.Mock;

public class TestExecutionAPI {

    @Mock
    private ClientAction clientAction;

    @Mock
    private ServerAssertion serverAssertion;

    @Mock
    private RequestFilter<?> requestFilter;


    /**
     * Single client action paired with single server assertion for most simplest
     * cases
     */
    @Test
    public void testSimpleExecution() {
        Warp.execute(clientAction)
            .verify(serverAssertion);
    }

    /**
     * Single client action and server assertion applied for request matching
     * given filter
     */
    @Test
    public void testSimpleFiltering() {
        Warp.execute(clientAction)
            .filter(requestFilter)
            .verify(serverAssertion);
    }

    /**
     * The result of simplest possible execution is ServerAssertion (modified
     * on the server)
     */
    @Test
    public void testSimpleResult() {
        ServerAssertion assertion = Warp.execute(clientAction)
            .verify(serverAssertion);
    }

    /**
     * Two requests caused by single client action are verified concurrently.
     */
    @Test
    public void testGroupOfTwoRequests() {
        Warp.execute(clientAction)
            .group("first")
                .filter(requestFilter)
                .verify(serverAssertion)
            .group("second")
                .filter(requestFilter)
                .verify(serverAssertion)
            .verifyAll();
    }

    /**
     * Complex Warp executions stores their results inside {@link WarpResult}
     * object where result of assertion and other details (e.g. filter hit
     * count) is stored.
     */
    @Test
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
}
