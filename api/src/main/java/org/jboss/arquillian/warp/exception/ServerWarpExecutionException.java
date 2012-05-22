package org.jboss.arquillian.warp.exception;

public class ServerWarpExecutionException extends RuntimeException {

    private static final long serialVersionUID = 7447102661182849547L;

    public ServerWarpExecutionException(Throwable cause) {
        super("The error occured during server request: " + cause.getMessage(), cause);
    }

}
