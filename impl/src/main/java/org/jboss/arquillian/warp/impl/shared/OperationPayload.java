package org.jboss.arquillian.warp.impl.shared;

import java.io.Serializable;

public class OperationPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    protected RemoteOperation operation;
    protected Throwable throwable;

    public OperationPayload(RemoteOperation operation) {
        this.operation = operation;
    }

    public RemoteOperation getOperation() {
        return operation;
    }

    public void setOperation(RemoteOperation result) {
        this.operation = result;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
