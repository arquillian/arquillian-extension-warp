package org.jboss.arquillian.warp.assertion;

import java.io.Serializable;

import org.jboss.arquillian.warp.ServerAssertion;

public class ResponsePayload implements Serializable {

    private static final long serialVersionUID = 8058318295131558079L;

    private ServerAssertion assertion;
    private Exception exception;

    public ResponsePayload(Exception exception) {
        this.exception = exception;
    }

    public ResponsePayload(ServerAssertion assertion) {
        this.assertion = assertion;
    }

    public ResponsePayload(ServerAssertion assertion, Exception exception) {
        this.assertion = assertion;
        this.exception = exception;
    }

    public ServerAssertion getAssertion() {
        return assertion;
    }

    public Exception getException() {
        return exception;
    }

}
