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

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.jboss.arquillian.warp.client.execution.GroupExecutionSpecifier;
import org.jboss.arquillian.warp.client.execution.GroupInspectionBuilder;
import org.jboss.arquillian.warp.client.execution.SingleInspectionSpecifier;
import org.jboss.arquillian.warp.client.execution.WarpExecutionBuilder;
import org.jboss.arquillian.warp.client.filter.http.HttpFilters;
import org.jboss.arquillian.warp.client.result.WarpGroupResult;
import org.jboss.arquillian.warp.client.result.WarpResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings({"unused", "serial"})
@ExtendWith(MockitoExtension.class)
public class TestExecutionAPI {

    private Activity activity;
    private Inspection inspection;
    private RequestObserver what;

    @Mock
    private WarpExecutionBuilder mockBuilder;

    @Mock
    private GroupExecutionSpecifier mockGroupExecutionSpecifier;

    @Mock
    private SingleInspectionSpecifier mockInspectionSpecifier;

    @Mock
    private GroupInspectionBuilder mockGroupInspectionBuilder;

    @Mock
    private WarpResult mockWarpResult;

    @Mock
    private WarpGroupResult mockWarpGroupResult;

    private MockedStatic<Warp> mockWarp;

    /**
     * Single client activity paired with single server inspection for most
     * simplest cases.
     */
    @Test
    public void testSimpleExecution() {
        // Prepare a lot of mocks:

        // "Warp.initiate(activity)"
        mockWarp.when(() -> Warp.initiate(activity)).thenReturn(mockBuilder);

        // The final step: ".inspect(inspection)":
        // Return value does not matter:
        when(mockBuilder.inspect(inspection)).thenReturn(inspection);

        // Now invoke the test:
        Warp
            .initiate(activity)
            .inspect(inspection);
    }

    /**
     * Single client activity and server inspection applied for only for given
     * requests.
     */
    @Test
    public void testSimpleObserving() {
        // Prepare a lot of mocks:

        // "Warp.initiate(activity)"
        mockWarp.when(() -> Warp.initiate(activity)).thenReturn(mockBuilder);

        // ".observe(..)" returns a "SingleInspectionSpecifier".
        when(mockBuilder.observe(what)).thenReturn(mockInspectionSpecifier);

        // The final step: ".inspect(inspection)":
        // Return value does not matter:
        when(mockInspectionSpecifier.inspect(inspection)).thenReturn(inspection);

        // Now invoke the test:
        Warp
            .initiate(activity)
            .observe(what)
            .inspect(inspection);
    }

    /**
     * The result of simplest possible execution is {@link Inspection} (modified
     * on a server).
     */
    @Test
    public void testSimpleResult() {
        // Prepare a lot of mocks:

        // "Warp.initiate(activity)"
        mockWarp.when(() -> Warp.initiate(activity)).thenReturn(mockBuilder);

        // The final step: ".inspect(inspection)":
        // Return value does not matter:
        when(mockBuilder.inspect(inspection)).thenReturn(inspection);

        // Now invoke the test:
        Inspection returnedInspection = Warp
            .initiate(activity)
            .inspect(inspection);
    }

    /**
     * Two requests caused by single client activity are verified in parallel.
     */
    @Test
    public void testGroupOfTwoRequests() {
        // Prepare a lot of mocks.

        // "Warp.initiate(activity)"
        mockWarp.when(() -> Warp.initiate(activity)).thenReturn(mockBuilder);

        //".group()" returns a "GroupExecutionSpecifier":
        when(mockBuilder.group()).thenReturn(mockGroupExecutionSpecifier);

        // ".observe(..)" returns again a "GroupExecutionSpecifier".
        when(mockGroupExecutionSpecifier.observe(what)).thenReturn(mockGroupExecutionSpecifier);

        // ".inspect(inspection)":
        when(mockGroupExecutionSpecifier.inspect(inspection)).thenReturn(mockGroupInspectionBuilder);

        // Invoking ".group()" on the ".inspect" result returns again a "GroupExecutionSpecifier":
        when(mockGroupInspectionBuilder.group()).thenReturn(mockGroupExecutionSpecifier);

        // ".observe" and ".inspect" are the same as above.

        // The final step: ".execute()" returns a "WarpResult" implementation.
        when(mockGroupInspectionBuilder.execute()).thenReturn(mockWarpResult);

        // Now invoke the test:
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
    @Test
    public void testResultOfComplexGroupExecution() {
        // Prepare a lot of mocks.

        // "Warp.initiate(activity)"
        mockWarp.when(() -> Warp.initiate(activity)).thenReturn(mockBuilder);

        //".group("first")" returns a "GroupExecutionSpecifier":
        when(mockBuilder.group("first")).thenReturn(mockGroupExecutionSpecifier);

        // ".observe(..)" returns again a "GroupExecutionSpecifier".
        when(mockGroupExecutionSpecifier.observe(what)).thenReturn(mockGroupExecutionSpecifier);

        // ".inspect(inspection"):
        // Return value does not matter:
        when(mockGroupExecutionSpecifier.inspect(inspection)).thenReturn(mockGroupInspectionBuilder);

        // Invoking ".group("second")" on the ".inspect" result returns again a "GroupExecutionSpecifier":
        when(mockGroupInspectionBuilder.group("second")).thenReturn(mockGroupExecutionSpecifier);

        // ".observe" and ".inspect" are the same as above.

        // The final step: ".execute()" returns a "WarpResult" implementation.
        when(mockGroupInspectionBuilder.execute()).thenReturn(mockWarpResult);

        // Evaluation of the result:
        // "WarpResult.getGroup":
        when(mockWarpResult.getGroup("first")).thenReturn(mockWarpGroupResult);

        // "WarpGroupResult.getInspection": simply return the "inspection" again.
        when(mockWarpGroupResult.getInspection()).thenReturn(inspection);

        // "WarpResult.getGroup":
        when(mockWarpResult.getGroup("second")).thenReturn(mockWarpGroupResult);

        // "WarpGroupResult.getHitCount":
        when(mockWarpGroupResult.getHitCount()).thenReturn(1);

        // Now invoke the test:
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
    @Test
    public void testMultipleInspections() {
        // Prepare a lot of mocks:

        // Invocation 1:
        // "Warp.initiate(activity)"
        mockWarp.when(() -> Warp.initiate(activity)).thenReturn(mockBuilder);

        // The final step: ".inspectinspectAll(inspection, inspection, inspection)":
        when(mockBuilder.inspectAll(inspection, inspection, inspection)).thenReturn(mockWarpResult);

        // Invocation 2:
        //".group()" returns a "GroupExecutionSpecifier":
        when(mockBuilder.group()).thenReturn(mockGroupExecutionSpecifier);

        // ".observe(..)" returns again a "GroupExecutionSpecifier".
        when(mockGroupExecutionSpecifier.observe(what)).thenReturn(mockGroupExecutionSpecifier);

        // ".inspect(inspection, inspection)":
        when(mockGroupExecutionSpecifier.inspect(inspection, inspection)).thenReturn(mockGroupInspectionBuilder);

        // Invoking ".group()" on the ".inspect" result returns again a "GroupExecutionSpecifier":
        when(mockGroupInspectionBuilder.group()).thenReturn(mockGroupExecutionSpecifier);

        // ".observe" is the same as above.

        // ".inspect(inspection, inspection, inspection)":
        when(mockGroupExecutionSpecifier.inspect(inspection, inspection, inspection)).thenReturn(mockGroupInspectionBuilder);

        // The final step: ".execute()" returns a "WarpResult" implementation.
        when(mockGroupInspectionBuilder.execute()).thenReturn(mockWarpResult);

        // Now invoke the test:
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
    @Test
    public void testExpectedCount() {
        // Prepare a lot of mocks:

        // "Warp.initiate(activity)"
        mockWarp.when(() -> Warp.initiate(activity)).thenReturn(mockBuilder);

        //".group()" returns a "GroupExecutionSpecifier":
        when(mockBuilder.group()).thenReturn(mockGroupExecutionSpecifier);

        // ".observe(..)" returns again a "GroupExecutionSpecifier".
        when(mockGroupExecutionSpecifier.observe(what)).thenReturn(mockGroupExecutionSpecifier);

        // ".expectedCount(2)":
        when(mockGroupExecutionSpecifier.expectCount(2)).thenReturn(mockGroupExecutionSpecifier);

        // ".inspect(inspection, inspection, inspection)":
        when(mockGroupExecutionSpecifier.inspect(inspection, inspection, inspection)).thenReturn(mockGroupInspectionBuilder);

        // The final step: ".execute()" returns a "WarpResult" implementation.
        when(mockGroupInspectionBuilder.execute()).thenReturn(mockWarpResult);

        // Now invoke the test:
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
    // TODO not implemented yet - https://issues.redhat.com/browse/ARQ-1238
    @Observe(WhatToObserve.class)
    @Test
    public void testFilterSpecifiedByAnnotation() {
        // Prepare a lot of mocks:
        // (see comment above - this test was probably not finished)

        // "Warp.initiate(activity)"
        mockWarp.when(() -> Warp.initiate(activity)).thenReturn(mockBuilder);

        // The final step: ".inspect(inspection)":
        // Return value does not matter:
        when(mockBuilder.inspect(inspection)).thenReturn(inspection);

        // Now invoke the test:
        Warp
            .initiate(activity)
            .inspect(inspection);
    }

    @BeforeEach
    public void before() {
        this.mockWarp = mockStatic(Warp.class);
    }

    @AfterEach
    public void after() {
        this.mockWarp.close();
    }

    private abstract static class WhatToObserve implements RequestObserver {
    }

    /**
     * Inspections can be specified by annotation - all specified inspections
     * will be used during all Warp executions.
     */
    // TODO not implemented yet - https://issues.redhat.com/browse/ARQ-1238
    @Inspect({Inspection1.class, Inspection2.class})
    public void testSpecifyInspectionByAnnotation() {
        activity.perform();
    }

    private abstract static class Inspection1 extends Inspection {
    }

    private abstract static class Inspection2 extends Inspection {
    }
}
