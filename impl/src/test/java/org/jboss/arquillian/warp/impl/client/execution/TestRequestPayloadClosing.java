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

import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.client.execution.GroupVerificationBuilder;
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
    GroupVerificationBuilder groupsExecutor;

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
