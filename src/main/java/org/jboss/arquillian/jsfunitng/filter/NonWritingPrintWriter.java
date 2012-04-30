package org.jboss.arquillian.jsfunitng.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;

public class NonWritingPrintWriter extends PrintWriter {

    private ByteArrayOutputStream baos;
    private PrintWriter delegate;
    private boolean wasClosed = false;

    private NonWritingPrintWriter(ByteArrayOutputStream baos, PrintWriter delegate) {
        super(delegate);
        this.baos = baos;
        this.delegate = delegate; 
    }
    
    public static NonWritingPrintWriter newInstance() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter delegate = new PrintWriter(baos);
        return new NonWritingPrintWriter(baos, delegate);
    }
    
    

    @Override
    public void close() {
        wasClosed = true;
    }

    void finallyWriteAndClose(ServletOutputStream delegateStream) throws IOException {

        if (wasClosed) {
            delegate.close();
        }

        byte[] byteArray = baos.toByteArray();

        delegateStream.write(byteArray);
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    @Override
    public boolean checkError() {

        return delegate.checkError();
    }

    @Override
    public void write(int c) {

        delegate.write(c);
    }

    @Override
    public void write(char[] buf, int off, int len) {

        delegate.write(buf, off, len);
    }

    @Override
    public void write(char[] buf) {

        delegate.write(buf);
    }

    @Override
    public void write(String s, int off, int len) {

        delegate.write(s, off, len);
    }

    @Override
    public void write(String s) {

        delegate.write(s);
    }

    @Override
    public void print(boolean b) {

        delegate.print(b);
    }

    @Override
    public void print(char c) {

        delegate.print(c);
    }

    @Override
    public void print(int i) {

        delegate.print(i);
    }

    @Override
    public void print(long l) {

        delegate.print(l);
    }

    @Override
    public void print(float f) {

        delegate.print(f);
    }

    @Override
    public void print(double d) {

        delegate.print(d);
    }

    @Override
    public void print(char[] s) {

        delegate.print(s);
    }

    @Override
    public void print(String s) {

        delegate.print(s);
    }

    @Override
    public void print(Object obj) {

        delegate.print(obj);
    }

    @Override
    public void println() {

        delegate.println();
    }

    @Override
    public void println(boolean x) {

        delegate.println(x);
    }

    @Override
    public void println(char x) {

        delegate.println(x);
    }

    @Override
    public void println(int x) {

        delegate.println(x);
    }

    @Override
    public void println(long x) {

        delegate.println(x);
    }

    @Override
    public void println(float x) {

        delegate.println(x);
    }

    @Override
    public void println(double x) {

        delegate.println(x);
    }

    @Override
    public void println(char[] x) {

        delegate.println(x);
    }

    @Override
    public void println(String x) {

        delegate.println(x);
    }

    @Override
    public void println(Object x) {

        delegate.println(x);
    }

    @Override
    public PrintWriter printf(String format, Object... args) {

        return delegate.printf(format, args);
    }

    @Override
    public PrintWriter printf(Locale l, String format, Object... args) {

        return delegate.printf(l, format, args);
    }

    @Override
    public PrintWriter format(String format, Object... args) {

        return delegate.format(format, args);
    }

    @Override
    public PrintWriter format(Locale l, String format, Object... args) {

        return delegate.format(l, format, args);
    }

    @Override
    public PrintWriter append(CharSequence csq) {

        return delegate.append(csq);
    }

    @Override
    public PrintWriter append(CharSequence csq, int start, int end) {

        return delegate.append(csq, start, end);
    }

    @Override
    public PrintWriter append(char c) {

        return delegate.append(c);
    }

}