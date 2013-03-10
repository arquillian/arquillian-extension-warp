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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.warp.impl.server.execution.NonWritingResponse;
import org.junit.Before;
import org.junit.Test;

public class TestNonWritingResponse {

    private ByteArrayServletOutputStream output;
    private HttpServletResponse response;
    private NonWritingResponse nonWritingResponse;

    @Before
    public void setUp() throws IOException {
        // given
        output = new ByteArrayServletOutputStream();
        response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(output);
        nonWritingResponse = new NonWritingResponse(response);
    }

    @Test
    public void test_writing_to_printWriter() throws IOException {
        // when
        nonWritingResponse.getWriter().write("test");
        nonWritingResponse.finallyWriteAndClose(output);

        // then
        assertEquals("test", output.toString());
    }

    @Test
    public void test_not_closing() throws IOException {
        // when
        nonWritingResponse.finallyWriteAndClose(output);

        // then
        assertFalse(output.isClosed());
    }

    @Test
    public void test_closing_printWriter() throws IOException {
        // when
        nonWritingResponse.getWriter().close();
        nonWritingResponse.finallyWriteAndClose(output);

        // then
        assertTrue(output.isClosed());
    }

    @Test
    public void test_writing_to_outputStream() throws IOException {
        // when
        nonWritingResponse.getOutputStream().write(bytes("test"));
        nonWritingResponse.finallyWriteAndClose(output);

        // then
        assertEquals("test", output.toString());
    }

    @Test
    public void test_closing_outputStream() throws IOException {
        // when
        nonWritingResponse.getOutputStream().close();
        nonWritingResponse.finallyWriteAndClose(output);

        // then
        assertTrue(output.isClosed());
    }

    @Test
    public void test_setting_content_length() throws IOException {
        // when
        nonWritingResponse.setContentLength(1);
        nonWritingResponse.setContentLength(2);
        nonWritingResponse.finallyWriteAndClose(output);

        // then
        verify(response).setContentLength(2);
    }

    @Test
    public void test_not_setting_content_length() throws IOException {
        // when
        nonWritingResponse.finallyWriteAndClose(output);

        // then
        verifyNoMoreInteractions(response);
    }

    @Test
    public void test_adding_to_output_written_by_printWriter() throws IOException {
        // when
        nonWritingResponse.getWriter().write("a");
        response.getOutputStream().write(bytes("b"));
        nonWritingResponse.finallyWriteAndClose(output);

        // then
        assertEquals("ba", output.toString());
    }

    /**
     * Tests the {@link NonWritingResponse#flushBuffer()} method.
     *
     * @throws IOException if any error occurs
     */
    @Test
    public void test_flushing_buffer() throws IOException {
        // when
        nonWritingResponse.flushBuffer();

        // then
        assertTrue("The response hasn't been committed.", nonWritingResponse.isCommitted());
        assertFalse("The wrapped response commit hasn't been correctly handled.", response.isCommitted());
    }

    /**
     * Tests the {@link NonWritingResponse#sendError(int)} method.
     *
     * @throws IOException if any error occurs
     */
    @Test
    public void test_send_error() throws IOException {
        // when
        nonWritingResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);

        // then
        assertTrue("The response hasn't been committed.", nonWritingResponse.isCommitted());
        assertFalse("The wrapped response commit hasn't been correctly handled.", response.isCommitted());
        assertEquals("The response has invalid status.", HttpServletResponse.SC_SERVICE_UNAVAILABLE, nonWritingResponse.getStatus());
    }

    /**
     * Tests the {@link NonWritingResponse#sendError(int, String)} method.
     *
     * @throws IOException if any error occurs
     */
    @Test
    public void test_send_error_with_message() throws IOException {
        // when
        nonWritingResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The error message");

        // then
        assertTrue("The response hasn't been committed.", nonWritingResponse.isCommitted());
        assertFalse("The wrapped response commit hasn't been correctly handled.", response.isCommitted());
        assertEquals("The response has invalid status.", HttpServletResponse.SC_SERVICE_UNAVAILABLE, nonWritingResponse.getStatus());
    }

    private byte[] bytes(String string) {
        return string.getBytes();
    }
}
