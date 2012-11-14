package org.jboss.arquillian.warp.impl.client.execution;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.client.execution.GroupsExecutor;
import org.jboss.arquillian.warp.impl.client.testbase.AbstractWarpClientTestTestBase;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestRequestPayloadClosing extends AbstractWarpClientTestTestBase {

    @Mock
    ClientAction clientAction;

    @Mock
    ServerAssertion serverAssertion;

    @Mock
    GroupsExecutor groupsExecutor;

    @Test
    public void when_request_group_is_new_and_expects_zero_requests_then_is_has_all_generated_payloads_paired() {
        // having
        RequestGroupImpl group = new RequestGroupImpl(groupsExecutor, 1);
        group.expectCount(0);
        group.verify(serverAssertion);

        // then
        assertTrue(group.allRequestsPaired());
    }

    @Test
    public void when_group_is_created_then_it_should_state_that_all_requests_are_paired() {
        // having
        RequestGroupImpl group = new RequestGroupImpl(groupsExecutor, 1);
        group.verify(serverAssertion);
        RequestPayload requestPayload = group.generateRequestPayload();
        assertNotNull("serialId must be set", requestPayload.getSerialId());
        assertFalse(group.allRequestsPaired());

        // when
        ResponsePayload responsePayload = new ResponsePayload(requestPayload.getSerialId());
        group.pushResponsePayload(responsePayload);

        // then
        assertTrue(group.allRequestsPaired());
    }
}
