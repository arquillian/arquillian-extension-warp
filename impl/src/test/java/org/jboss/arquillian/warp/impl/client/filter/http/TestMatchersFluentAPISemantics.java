/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.impl.client.filter.http;

import static org.jboss.arquillian.warp.client.filter.http.HttpFilters.request;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jboss.arquillian.warp.client.execution.WarpActivityBuilder;
import org.jboss.arquillian.warp.client.execution.WarpRuntime;
import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.filter.http.HttpFilterBuilder;
import org.jboss.arquillian.warp.client.filter.http.HttpRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestMatchersFluentAPISemantics {

    @Test
    public void when_index_is_used_then_it_is_not_evaluated_when_previous_condition_failed() {
        // given
        HttpRequest request = mock(HttpRequest.class);
        when(request.getUri()).thenReturn("abc");

        HttpRequest request2 = mock(HttpRequest.class);
        when(request2.getUri()).thenReturn("xyz");

        // when
        RequestFilter<HttpRequest> filter = request().uri().contains("xyz").index(1).build();

        // then
        assertFalse("the first request doesn't meet URL conditions", filter.matches(request));
        assertTrue("the second request fulfills conditions - it is first request with given URL", filter.matches(request2));
        assertFalse("the third request fulfills URL condition, but it isn't first such request", filter.matches(request2));
    }

    @Test
    public void when_index_is_two_then_second_request_which_fulfills_conditions_is_filtered() {
        // given
        HttpRequest request = mock(HttpRequest.class);
        when(request.getUri()).thenReturn("abc");

        HttpRequest request2 = mock(HttpRequest.class);
        when(request2.getUri()).thenReturn("xyz");

        // when
        RequestFilter<HttpRequest> filter = request().uri().contains("xyz").index(2).build();

        // then
        assertFalse("the first request doesn't meet URL conditions", filter.matches(request));
        assertFalse("the second request fulfills URL condition, but it isn't second such request", filter.matches(request2));
        assertTrue("the third request fulfills conditions - it is first request with given URL", filter.matches(request2));
    }

    @Before
    public void beforeTest() {
        WarpRuntime.setInstance(new WarpRuntime() {

            @Override
            public WarpActivityBuilder getWarpActivityBuilder() {
                throw new UnsupportedOperationException();
            }

            @Override
            public HttpFilterBuilder getHttpFilterBuilder() {
                return new DefaultHttpFilterBuilder();
            }
        });
    }

    @After
    public void afterTest() {
        WarpRuntime.setInstance(null);
    }

}
