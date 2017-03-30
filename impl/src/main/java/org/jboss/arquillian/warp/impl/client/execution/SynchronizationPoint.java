/*
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

import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.WarpProperties;

/**
 * <p>
 * The point of synchronization of response finishing.
 * </p>
 *
 * <p>
 * The expected order of calling methods:
 * </p>
 *
 * <ul>
 * <li>{@link #advertise()} - advertizes incoming Warp activity</li>
 * <li>{@link #close(int)} - closes Warp specification and makes it available for execution, setups number of requests to be
 * inspected</li>
 * <li>{@link #finishOneResponse()} - advertizes closing of one request with response (called multiple times)</li>
 * <li>{@link #awaitResponses()} - waits for all request to be finished (run parallely to {@link #finishOneResponse()} calls)</li>
 * </ul>
 *
 *
 * @author Lukas Fryc
 */
public class SynchronizationPoint {

    private static final long THREAD_SLEEP = 50;

    private final long WAIT_TIMEOUT_MILISECONDS = Long.parseLong(System.getProperty(WarpProperties.SYNCHRONIZATION_TIMEOUT,
            "5000"));
    private final long NUMBER_OF_WAIT_LOOPS = WAIT_TIMEOUT_MILISECONDS / THREAD_SLEEP;

    private final AtomicBoolean enrichmentAdvertised = new AtomicBoolean(false);
    private final AtomicBoolean enrichmentClosed = new AtomicBoolean(false);
    private CountDownLatch responseFinished;

    /**
     * Advertizes that there will be taken client activity which will lead into request.
     */
    void advertise() {
        enrichmentAdvertised.set(true);
    }

    /**
     * Closes the Warp specification with expected number of requests to be done
     */
    void close(int expectedRequestCount) {
        responseFinished = new CountDownLatch(expectedRequestCount);
        enrichmentClosed.set(true);
    }

    /**
     * Finishes one request with adequate response
     */
    void finishOneResponse() {
        responseFinished.countDown();
    }

    /**
     * Finishes all requests prematurely (without waiting for responses)
     */
    void finishAll() {
        for (long i = responseFinished.getCount(); i >= 0; i--) {
            responseFinished.countDown();
        }
    }

    /**
     * Returns true if there is client activity advertised, see {@link #advertise()}.
     *
     * @return true if there is client activity advertised, see {@link #advertise()}.
     */
    private boolean isEnrichmentAdvertised() {
        return enrichmentAdvertised.get();
    }

    /**
     * Returns true if there is {@link Inspection} pushed for current request.
     *
     * @return true if there is {@link Inspection} pushed for current request.
     */
    private boolean isEnrichmentClosed() {
        return enrichmentClosed.get();
    }

    /**
     * Returns true if the enrichment was advertised.
     */
    boolean isWaitingForRequests() {
        return isEnrichmentAdvertised();
    }

    /**
     * Returns true if the enrichment is advertised but it has not been closed yet.
     */
    boolean isWaitingForEnriching() {
        return isEnrichmentAdvertised() && !isEnrichmentClosed();
    }

    /**
     * Returns true if the enrichment was advertised, all requests were done and they are waiting for responses
     */
    boolean isWaitingForResponses() {
        return isEnrichmentAdvertised() && isEnrichmentClosed() && responseFinished.getCount() > 0;
    }

    /**
     * Await client activity causing requests in order to enrich requests
     */
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

    /**
     * Await responses for requests or premature finishing
     */
    void awaitResponses() {
        try {
            boolean finishedNicely = responseFinished.await(WAIT_TIMEOUT_MILISECONDS, TimeUnit.MILLISECONDS);
            if (!finishedNicely) {
                throw new WarpSynchronizationException(WarpContextStore.get());
            }
        } catch (InterruptedException e) {
        }
    }

    public static class SettingRequestTimeoutException extends RuntimeException {
        private static final long serialVersionUID = -6743564150233628034L;
    }

    public static class RequestEnrichmentAlreadySetException extends RuntimeException {
        private static final long serialVersionUID = 8333157142743791135L;
    }
}
