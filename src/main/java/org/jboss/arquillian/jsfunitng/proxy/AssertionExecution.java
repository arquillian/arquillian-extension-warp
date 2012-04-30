package org.jboss.arquillian.jsfunitng.proxy;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import org.jboss.arquillian.jsfunitng.ClientAction;
import org.jboss.arquillian.jsfunitng.ServerAssertion;

public class AssertionExecution {

    private final static Logger log = Logger.getLogger(AssertionExecution.class.getName());

    private ClientAction action;
    // TODO AtomicReference
    private ServerAssertion assertion;
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AssertionExecution(ClientAction action) {
        this.action = action;
    }

    public <T extends ServerAssertion> T verify(T assertion) {
        this.assertion = assertion;
        execute();
        return assertion;
    }

    public void execute() {
        log.info("advertise");
        AssertionHolder.advertise();
        FutureTask<ServerAssertion> future = new FutureTask<ServerAssertion>(new PushAssertion());
        log.info("future.run");
        executor.submit(future);
        try {
            log.info("action");
            action.action();
        } catch (Exception e) {
            throw new ClientActionException(e);
        }
        try {
            log.info("future.get");
            assertion = future.get();
        } catch (Exception e) {
            throw new ServerAssertionException(e);
        }
    }

    public class PushAssertion implements Callable<ServerAssertion> {
        @Override
        public ServerAssertion call() throws Exception {
            AssertionHolder.pushRequest(assertion);
            return AssertionHolder.popResponse();
        }
    }

    public static class ClientActionException extends RuntimeException {
        private static final long serialVersionUID = 7267806785171391801L;

        public ClientActionException(Throwable cause) {
            super(cause);
        }
    }

    public static class ServerAssertionException extends RuntimeException {
        private static final long serialVersionUID = -5318390607884452966L;

        public ServerAssertionException(Throwable cause) {
            super(cause);
        }
    }

}