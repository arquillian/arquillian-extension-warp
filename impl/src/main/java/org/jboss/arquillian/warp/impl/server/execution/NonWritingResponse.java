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
package org.jboss.arquillian.warp.impl.server.execution;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class NonWritingResponse extends HttpServletResponseWrapper {

    private Integer contentLength = null;

    private NonWritingServletOutputStream stream;
    private NonWritingPrintWriter writer;

    public NonWritingResponse(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (stream == null) {
            stream = new NonWritingServletOutputStream();
        }
        return stream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = NonWritingPrintWriter.newInstance();
        }
        return writer;
    }

    @Override
    public void setContentLength(int len) {
        this.contentLength = len;
    }

    public Integer getContentLength() {
        return contentLength;
    }

    public void finalize() {
        if (contentLength != null) {
            super.setContentLength(contentLength);
        }
    }

    public NonWritingServletOutputStream getNonWritingServletOutputStream() {
        return stream;
    }

    public NonWritingPrintWriter getNonWritingPrintWriter() {
        return writer;
    }
}