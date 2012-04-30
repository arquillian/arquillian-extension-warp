/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.jsfunitng.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

/**
 * The implementation {@link ServletOutputStream} which caches all the written input and writes only when
 * {@link #finallyWriteAndClose(ServletOutputStream)} method is called.
 * 
 * @author Lukas Fryc
 * 
 */
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
