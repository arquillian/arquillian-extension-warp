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