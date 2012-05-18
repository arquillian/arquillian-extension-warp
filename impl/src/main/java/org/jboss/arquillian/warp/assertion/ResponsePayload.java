package org.jboss.arquillian.warp.assertion;

import java.io.Serializable;

import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.warp.ServerAssertion;

public class ResponsePayload implements Serializable {

    private static final long serialVersionUID = 8058318295131558079L;

    private ServerAssertion assertion;
    private TestResult testResult;
    private Throwable throwable;

    public ResponsePayload(ServerAssertion assertion) {
        this.assertion = assertion;
    }

    public ResponsePayload(TestResult testResult) {
        this.testResult = testResult;
    }

    public ResponsePayload(Throwable throwable) {
        this.throwable = throwable;
    }

    public ServerAssertion getAssertion() {
        return assertion;
    }

    public TestResult getTestResult() {
        return testResult;
    }

    public Throwable getThrowable() {
        return throwable;
    }

}
