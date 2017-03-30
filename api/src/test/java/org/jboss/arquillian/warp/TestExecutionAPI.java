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
package org.jboss.arquillian.warp;

import org.jboss.arquillian.warp.client.result.WarpResult;

@SuppressWarnings({"unused", "serial"})
public class TestExecutionAPI {

    private Activity activity;
    private Inspection inspection;
    private RequestObserver what;

    /**
     * Single client activity paired with single server inspection for most
     * simplest cases.
     */
    public void testSimpleExecution() {
        Warp
            .initiate(activity)
            .inspect(inspection);
    }

    /**
     * Single client activity and server inspection applied for only for given
     * requests.
     */
    public void testSimpleObserving() {
        Warp
            .initiate(activity)
            .observe(what)
            .inspect(inspection);
    }

    /**
     * The result of simplest possible execution is {@link Inspection} (modified
     * on a server).
     */
    public void testSimpleResult() {
        Inspection returnedInspection = Warp
            .initiate(activity)
            .inspect(inspection);
    }

    /**
     * Two requests caused by single client activity are verified in parallel.
     */
    public void testGroupOfTwoRequests() {
        Warp
            .initiate(activity)
            .group()
            .observe(what)
            .inspect(inspection)
            .group()
            .observe(what)
            .inspect(inspection)
            .execute();
    }

    /**
     * Complex Warp executions stores their results inside {@link WarpResult}
     * object where result of inspection and other details (e.g. observer hit
     * count) are stored.
     */
    public void testResultOfComplexGroupExecution() {
        WarpResult result = Warp
            .initiate(activity)
            .group("first")
            .observe(what)
            .inspect(inspection)
            .group("second")
            .observe(what)
            .inspect(inspection)
            .execute();

        Inspection firstInspection = result.getGroup("first").getInspection();

        int hitCount = result.getGroup("second").getHitCount();
    }

    /**
     * Test may specify multiple inspections verified in one request.
     * <p>
     * These inspections will preserve order of definition and execution.
     */
    public void testMultipleInspections() {
        WarpResult result = Warp
            .initiate(activity)
            .inspectAll(inspection, inspection, inspection);

        result = Warp
            .initiate(activity)
            .group()
            .observe(what)
            .inspect(inspection, inspection)
            .group()
            .observe(what)
            .inspect(inspection, inspection, inspection)
            .execute();
    }

    /**
     * Once group is defined then it can be configured either with observer or expected count
     */
    public void testExpectedCount() {
        Warp
            .initiate(activity)
            .group()
            .observe(what)
            .expectCount(2)
            .inspect(inspection, inspection, inspection)
            .execute();

        Warp
            .initiate(activity)
            .group()
            .expectCount(2)
            .observe(what)
            .inspect(inspection, inspection, inspection)
            .execute();
    }

    /**
     * Observers can be specified by annotation - they will be applied in any
     * Warp execution where no other observer was specified.
     */
    // TODO not implemented yet
    @Observe(WhatToObserve.class)
    public void testFilterSpecifiedByAnnotation() {
        Warp
            .initiate(activity)
            .inspect(inspection);
    }

    private abstract static class WhatToObserve implements RequestObserver {
    }

    /**
     * Inspections can be specified by annotation - all specified inspections
     * will be used during all Warp executions.
     */
    // TODO not implemented yet
    @Inspect({Inspection1.class, Inspection2.class})
    public void testSpecifyInspectionByAnnotation() {
        activity.perform();
    }

    private abstract static class Inspection1 extends Inspection {
    }

    private abstract static class Inspection2 extends Inspection {
    }
}
