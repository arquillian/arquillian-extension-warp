/**
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.impl.client.execution;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.arquillian.warp.ServerAssertion;

/**
 * The holder for {@link ServerAssertion} object.
 *
 * Provides methods for settings up the server assertion and its retrieval.
 *
 * @author Lukas Fryc
 */
public class SynchronizationPoint {

    private static final long WAIT_TIMEOUT_MILISECONDS = 5000;
    private static final long THREAD_SLEEP = 50;
    private static final long NUMBER_OF_WAIT_LOOPS = WAIT_TIMEOUT_MILISECONDS / THREAD_SLEEP;

    private final AtomicBoolean enrichmentAdvertised = new AtomicBoolean(false);
    private final AtomicBoolean enrichmentClosed = new AtomicBoolean(false);
    private final CountDownLatch responseFinished = new CountDownLatch(1);

    /**
     * Advertizes that there will be taken client action which will lead into request.
     */
    void advertise() {
        enrichmentAdvertised.set(true);
    }

    void close() {
        enrichmentClosed.set(true);
    }

    void finishResponse() {
        responseFinished.countDown();
    }

    /**
     * Returns true if there is client action advertised, see {@link #advertise()}.
     *
     * @return true if there is client action advertised, see {@link #advertise()}.
     */
    private boolean isEnrichmentAdvertised() {
        return enrichmentAdvertised.get();
    }

    /**
     * Returns true if there is {@link ServerAssertion} pushed for current request.
     *
     * @return true if there is {@link ServerAssertion} pushed for current request.
     */
    private boolean isEnrichmentClosed() {
        return enrichmentClosed.get();
    }

    /**
     * Returns true if the {@link ServerAssertion} is waiting for verification or the client action which should cause request
     * is advertised.
     *
     * @return true if the {@link ServerAssertion} is waiting for verification or the client action which should cause request
     *         is advertised.
     */
    boolean isWaitingForRequests() {
        return isEnrichmentAdvertised();
    }

    boolean isWaitingForEnriching() {
        return isEnrichmentAdvertised() && !isEnrichmentClosed();
    }

    boolean isWaitingForResponses() {
        return isEnrichmentAdvertised() && isEnrichmentClosed() && responseFinished.getCount() > 0;
    }

    void awaitRequests() {
        if (!isWaitingForEnriching()) {
            return;
        }
        for (int i = 0; i < NUMBER_OF_WAIT_LOOPS; i++) {
            try {
                Thread.sleep(THREAD_SLEEP);
                if (!isEnrichmentAdvertised()) {
                    return;
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new SettingRequestTimeoutException();
    }

    void awaitResponses() {
        try {
            boolean finishedNicely = responseFinished.await(WAIT_TIMEOUT_MILISECONDS, TimeUnit.MILLISECONDS);
            if (!finishedNicely) {
                throw new ServerResponseTimeoutException();
            }
        } catch (InterruptedException e) {
        }
    }

    public static class SettingRequestTimeoutException extends RuntimeException {
        private static final long serialVersionUID = -6743564150233628034L;
    }

    public static class ServerResponseTimeoutException extends RuntimeException {
        private static final long serialVersionUID = 7267806785171391801L;
    }

    public static class RequestEnrichmentAlreadySetException extends RuntimeException {
        private static final long serialVersionUID = 8333157142743791135L;
    }
}
