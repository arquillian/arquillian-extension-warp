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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

/**
 * The holder for {@link ServerAssertion} object.
 *
 * Provides methods for settings up the server assertion and its retrieval.
 *
 * @author Lukas Fryc
 */
class AssertionHolder {

    private static final long WAIT_TIMEOUT_MILISECONDS = 30000;
    private static final long THREAD_SLEEP = 50;
    private static final long NUMBER_OF_WAIT_LOOPS = WAIT_TIMEOUT_MILISECONDS / THREAD_SLEEP;

    private static final AtomicBoolean enrichmentAdvertised = new AtomicBoolean(false);
    private static final AtomicBoolean enrichmentFinished = new AtomicBoolean(false);
    private static final Set<RequestEnrichment> requests = new CopyOnWriteArraySet<RequestEnrichment>();
    private static final Set<ResponseEnrichment> responses = new CopyOnWriteArraySet<ResponseEnrichment>();
    private static CountDownLatch responsesLatch;

    /**
     * Advertizes that there will be taken client action which will lead into request.
     */
    public static void advertise() {
        enrichmentAdvertised.set(true);
    }

    public static void setExpectedRequests(int requests) {
        responsesLatch = new CountDownLatch(requests);
    }

    public static void finished() {
        enrichmentFinished.set(true);
    }

    /**
     * Returns true if there is client action advertised, see {@link #advertise()}.
     *
     * @return true if there is client action advertised, see {@link #advertise()}.
     */
    private static boolean isEnrichmentAdvertised() {
        return enrichmentAdvertised.get();
    }

    /**
     * Returns true if there is {@link ServerAssertion} pushed for current request.
     *
     * @return true if there is {@link ServerAssertion} pushed for current request.
     */
    private static boolean isEnrichmentFinished() {
        return enrichmentFinished.get();
    }

    /**
     * Returns true if the {@link ServerAssertion} is waiting for verification or the client action which should cause request
     * is advertised.
     *
     * @return true if the {@link ServerAssertion} is waiting for verification or the client action which should cause request
     *         is advertised.
     */
    static boolean isWaitingForRequests() {
        return isEnrichmentAdvertised();
    }

    private static boolean isWaitingForEnriching() {
        return isEnrichmentAdvertised() && !isEnrichmentFinished();
    }

    static boolean isWaitingForResponses() {
        return responsesLatch.getCount() > 0L;
    }

    /**
     * <p>
     * Pushes the {@link RequestEnrichment} to verify on the server.
     * </p>
     *
     * <p>
     * This method cancels flag set by {@link #advertise()}.
     *
     * @param request to be verified on the server
     */
    public static void addRequest(RequestEnrichment request) {
        Validate.notNull(request, "enrichment can't be null");

        requests.add(request);
    }

    /**
     * Waits until the {@link ServerAssertion} for request is available and returns it.
     *
     * @return the associated {@link ServerAssertion}
     * @throws SettingRequestTimeoutException when {@link ServerAssertion} isn't setup in time
     */
    static Set<RequestEnrichment> getRequests() {

        awaitRequests();

        return Collections.unmodifiableSet(requests);
    }

    /**
     * Pushes the verified {@link ResponsePayload} to be obtained by test.
     *
     * @param payload verified {@link ResponsePayload} to be obtained by test.
     */
    static void addResponse(ResponseEnrichment response) {
        Validate.notNull(response, "response can't be null");

        responses.add(response);
        responsesLatch.countDown();
    }

    static void finishEnrichmentRound() {
        responsesLatch = null;
        enrichmentAdvertised.set(false);
        enrichmentFinished.set(false);
        requests.clear();
        responses.clear();
    }

    /**
     * Waits until the for response is available and returns it.
     *
     * @return the {@link ResponsePayload}
     * @throws ServerResponseTimeoutException when the response wasn't returned in time
     */
    public static Set<ResponseEnrichment> getResponses() {

        awaitResponses();

        return Collections.unmodifiableSet(responses);
    }

    private static void awaitRequests() {
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

    private static void awaitResponses() {
        try {
            boolean finishedNicely = responsesLatch.await(WAIT_TIMEOUT_MILISECONDS, TimeUnit.MILLISECONDS);
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
