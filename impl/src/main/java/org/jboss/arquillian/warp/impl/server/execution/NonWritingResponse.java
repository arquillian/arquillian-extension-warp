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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class NonWritingResponse extends HttpServletResponseWrapper {

    private Integer contentLength = null;
    private Integer status;
    private Map<String, List<String>> headers = new HashMap<String, List<String>>();

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

    @Override
    public void sendRedirect(String location) throws IOException {
        setStatus(SC_MOVED_TEMPORARILY);
        setHeader("Location", location);
    }

    public int getStatus() {
        return status == null ? SC_OK : status;
    }

    @Override
    public void setHeader(String name, String value) {
        List<String> list = new LinkedList<String>();
        headers.put(name, list);
        list.add(value);
    }

    @Override
    public void addHeader(String name, String value) {
        if (headers.get(name) == null) {
            setHeader(name, value);
        } else {
            headers.get(name).add(value);
        }
    }

    @Override
    public String getHeader(String name) {
        List<String> list = headers.get(name);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public void setStatus(int sc) {
        this.status = sc;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return super.getHeaders(name);
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public NonWritingServletOutputStream getNonWritingServletOutputStream() {
        return stream;
    }

    public NonWritingPrintWriter getNonWritingPrintWriter() {
        return writer;
    }

    public void finallyWriteAndClose(ServletOutputStream delegateStream) throws IOException {
        if (contentLength != null) {
            super.setContentLength(contentLength);
        }

        if (writer != null) {
            writer.finallyWriteAndClose(delegateStream);
        }
        if (stream != null) {
            stream.finallyWriteAndClose(delegateStream);
        }
    }
}