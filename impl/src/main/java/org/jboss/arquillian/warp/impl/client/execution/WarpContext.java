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

import java.util.Collection;

import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.warp.client.result.WarpResult;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.jboss.arquillian.warp.spi.observer.RequestObserverChainManager;

/**
 * Context of Warp execution which makes available executed groups, holds exceptions and execution results
 *
 * @author Lukas Fryc
 */
public interface WarpContext {

    /**
     * Registers group to be inspected
     */
    void addGroup(WarpGroup group);

    /**
     * Returns all registered groups
     */
    Collection<WarpGroup> getAllGroups();

    /**
     * <p>
     * Pushes {@link ResponsePayload} to context.
     * </p>
     *
     * <p>
     * Context should ensure propagating {@link ResponsePayload} to associated {@link WarpGroup}.
     * </p>
     */
    void pushResponsePayload(ResponsePayload payload);

    /**
     * Pushes exception observer during Warp execution to context.
     */
    void pushException(Exception exception);

    /**
     * Returns first exception observed during Warp execution.
     */
    Exception getFirstException();

    /**
     * <p>
     * Tries to finish Warp execution by asking all groups for their states - if they got responses for all generated payloads -
     * they are finalized.
     * </p>
     *
     * <p>
     * When all groups are finalized, Warp execution should be finished by invoking current
     * {@link SynchronizationPoint#finishResponse()}.
     * </p>
     */
    void tryFinalizeResponse();

    /**
     * Returns the point of synchronization of current Warp execution.
     */
    SynchronizationPoint getSynchronization();

    /**
     * Returns first observed non-successful result.
     */
    TestResult getFirstNonSuccessfulResult();

    /**
     * Exports response status as {@link WarpResult} to be available to user.
     */
    WarpResult getResult();

    /**
     * <p>
     * Initializes Warp context by available services.
     * </p>
     *
     * <p>
     * Note: {@link WarpContext} is used in another thread, that's why it can't access injected {@link ServiceLoader} directly.
     * </p>
     */
    void initialize(ServiceLoader serviceLoader);

    /**
     * Get all registered services for {@link RequestObserverChainManager}.
     *
     * @return
     */
    Collection<RequestObserverChainManager> getObserverChainManagers();
}
