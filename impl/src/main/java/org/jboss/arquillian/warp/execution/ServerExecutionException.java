package org.jboss.arquillian.warp.execution;

public class ServerExecutionException extends RuntimeException {

    private static final long serialVersionUID = 7447102661182849547L;

    public ServerExecutionException(Throwable cause) {
        super("The error occured during server request: " + cause.getMessage(), cause);
    }

}
