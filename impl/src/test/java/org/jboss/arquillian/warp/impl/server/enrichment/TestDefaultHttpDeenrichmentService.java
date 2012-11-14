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
package org.jboss.arquillian.warp.impl.server.enrichment;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.warp.impl.server.execution.NonWritingResponse;
import org.jboss.arquillian.warp.impl.server.request.RequestScoped;
import org.jboss.arquillian.warp.impl.server.testbase.AbstractWarpServerTestTestBase;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestDefaultHttpDeenrichmentService extends AbstractWarpServerTestTestBase {

    private HttpResponseEnricher enricher;
    private ByteArrayServletOutputStream output;
    private HttpServletResponse response;
    private NonWritingResponse nonWritingResponse;
    private ResponsePayload payload;

    @Before
    public void setUp() throws IOException {
        // given
        enricher = new DefaultHttpResponseEnricher();
        payload = new ResponsePayload(-1L);

        output = new ByteArrayServletOutputStream();
        response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(output);
        nonWritingResponse = new NonWritingResponse(response);

        bind(RequestScoped.class, ResponsePayload.class, payload);
        bind(RequestScoped.class, HttpServletResponse.class, response);
        bind(RequestScoped.class, NonWritingResponse.class, nonWritingResponse);

        getManager().inject(enricher);
    }

    @Test
    public void test_writing_response() throws IOException {

        // when
        nonWritingResponse.getWriter().write("test");
        enricher.enrichResponse();

        String payloadBase64 = SerializationUtils.serializeToBase64(payload);

        // then
        assertEquals(payloadBase64 + "test", output.toString());
    }

    @Test
    public void test_content_length() throws IOException {

        int enrichmentLength = SerializationUtils.serializeToBase64(payload).length();

        // when
        nonWritingResponse.setContentLength(1);
        enricher.enrichResponse();

        // then
        verify(response).setContentLength(1 + enrichmentLength);
    }

    @Test
    public void test_no_content_length() throws IOException {

        // when
        enricher.enrichResponse();

        // then
        verify(response, Mockito.atMost(0)).setContentLength(Mockito.anyInt());
    }

}
