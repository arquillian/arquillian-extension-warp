package org.jboss.arquillian.jsfunitng.proxy;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.jboss.arquillian.jsfunitng.ServerAssertion;

@SuppressWarnings("unchecked")
class AssertionHolder {
    
    private final static Logger log = Logger.getLogger(AssertionExecution.class.getName());

    private static final long WAIT_TIMEOUT_MILISECONDS = 5000;
    private static final long THREAD_SLEEP = 50;
    private static final long NUMBER_OF_WAIT_LOOPS = WAIT_TIMEOUT_MILISECONDS / THREAD_SLEEP;

    private static final AtomicBoolean advertisement = new AtomicBoolean();
    private static final AtomicReference<ServerAssertion> request = new AtomicReference<ServerAssertion>();
    private static final AtomicReference<ServerAssertion> response = new AtomicReference<ServerAssertion>();

    public static void advertise() {
        log.info("advertise");
        advertisement.set(true);
    }

    public static void pushRequest(ServerAssertion assertion) {
        log.info("pushRequest");
        if (request.get() != null) {
            throw new ServerAssertionAlreadySetException();
        }
        request.set(assertion);
        response.set(null);
        advertisement.set(false);
    }

    private static boolean isAdvertised() {
        return advertisement.get();
    }
    
    private static boolean isEnriched() {
        return request.get() != null;
    }
    
    static boolean isWaitingForProcessing() {
        return isAdvertised() || isEnriched();
    }

    static <T extends ServerAssertion> T popRequest() {
        log.info("popRequest");
        awaitRequest();
        return (T) request.getAndSet(null);
    }

    static void pushResponse(ServerAssertion assertion) {
        log.info("pushResponse");
        response.set(assertion);
    }

    public static <T extends ServerAssertion> T popResponse() {
        log.info("popResponse");
        awaitResponse();
        return (T) response.getAndSet(null);
    }

    private static void awaitRequest() {
        if (!isAdvertised()) {
            return;
        }
        for (int i = 0; i < NUMBER_OF_WAIT_LOOPS; i++) {
            try {
                Thread.sleep(THREAD_SLEEP);
                if (!isAdvertised()) {
                    return;
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new SettingRequestTimeoutException();
    }

    private static void awaitResponse() {
        if (response.get() != null) {
            return;
        }
        for (int i = 0; i < NUMBER_OF_WAIT_LOOPS; i++) {
            try {
                Thread.sleep(THREAD_SLEEP);
                if (response.get() != null) {
                    return;
                }
            } catch (InterruptedException e) {

            }
        }
        throw new ServerResponseTimeoutException();
    }

    public static class SettingRequestTimeoutException extends RuntimeException {
        private static final long serialVersionUID = -6743564150233628034L;
    }

    public static class ServerResponseTimeoutException extends RuntimeException {
        private static final long serialVersionUID = 7267806785171391801L;
    }

    public static class ServerAssertionAlreadySetException extends RuntimeException {
        private static final long serialVersionUID = 8333157142743791135L;
    }
}
