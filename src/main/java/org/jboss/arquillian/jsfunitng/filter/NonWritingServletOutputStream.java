package org.jboss.arquillian.jsfunitng.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

public class NonWritingServletOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private boolean wasClosed = false;

    public NonWritingServletOutputStream() {
        super();
    }

    @Override
    public void close() throws IOException {
        wasClosed = true;
    }

    public void finallyWriteAndClose(ServletOutputStream delegate) throws IOException {
        byte[] byteArray = baos.toByteArray();

        delegate.write(byteArray);

        if (wasClosed) {
            delegate.close();
        }
    }

    @Override
    public void write(int b) throws IOException {
        baos.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        baos.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        baos.write(b, off, len);
    }

}
