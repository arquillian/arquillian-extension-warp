package org.jboss.arquillian.warp.exception;

public class ClientWarpExecutionException extends RuntimeException {

    private static final long serialVersionUID = -2549252400983707523L;

    public ClientWarpExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientWarpExecutionException(String message) {
        super(message);
    }

    public ClientWarpExecutionException(Throwable cause) {
        super(cause);
    }
}
