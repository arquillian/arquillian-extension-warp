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

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.client.result.WarpResult;
import org.jboss.arquillian.warp.exception.ServerWarpExecutionException;
import org.jboss.arquillian.warp.impl.client.event.AdvertiseEnrichment;
import org.jboss.arquillian.warp.impl.client.event.AwaitResponse;
import org.jboss.arquillian.warp.impl.client.event.CleanEnrichment;
import org.jboss.arquillian.warp.impl.client.event.FinishEnrichment;
import org.jboss.arquillian.warp.impl.client.execution.DefaultWarpRequestSpecifier.ActivityException;
import org.jboss.arquillian.warp.impl.utils.Rethrow;

/**
 * Default {@link WarpExecutor}
 *
 * @author Lukas Fryc
 */
public class DefaultWarpExecutor implements WarpExecutor {

    @Inject
    private Event<AdvertiseEnrichment> advertiseEnrichment;

    @Inject
    private Event<FinishEnrichment> finishEnrichment;

    @Inject
    private Event<CleanEnrichment> cleanEnrichment;

    @Inject
    private Event<AwaitResponse> awaitResponse;

    @Inject
    private Event<Activity> executeActivity;

    private RuntimeException activityException;

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.warp.impl.client.execution.WarpExecutor#execute(org.jboss.arquillian.warp.Activity, org.jboss.arquillian.warp.impl.client.execution.WarpContext)
     */
    @Override
    public WarpResult execute(Activity activity, WarpContext warpContext) {
        try {
            setupServerInspection();
            executeActivity(activity);
            awaitServerExecution(warpContext);
            checkActivityFailure();

            return warpContext.getResult();
        } finally {
            cleanup();
        }
    }

    private void setupServerInspection() {
        advertiseEnrichment.fire(new AdvertiseEnrichment());
        finishEnrichment.fire(new FinishEnrichment());
    }

    private void executeActivity(Activity activity) {
        activityException = null;
        try {
            executeActivity.fire(activity);
        } catch (Throwable e) {
            activityException = new ActivityException(e);
        }
    }

    private void checkActivityFailure() {
        if (activityException != null) {
            throw activityException;
        }
    }

    private void awaitServerExecution(WarpContext warpContext) {
        awaitResponse.fire(new AwaitResponse());

        TestResult testResult = warpContext.getFirstNonSuccessfulResult();

        if (testResult == null) {
            return;
        }

        switch (testResult.getStatus()) {
            case FAILED:
                propagateFailure(testResult);
                break;
            case SKIPPED:
                propagateSkip();
                break;
            case PASSED:
                throw new IllegalStateException("Passed test result should never be propagated as non-successful");
        }
    }

    private void cleanup() {
        cleanEnrichment.fire(new CleanEnrichment());
    }

    private void propagateFailure(TestResult testResult) {
        Throwable e = testResult.getThrowable();

        // Client errors are more important than server assertion errors
        if (e instanceof AssertionError) {
            checkActivityFailure();
        }

        propagateException(e);
    }

    private void propagateException(Throwable e) {
        Rethrow.asUnchecked(e, ServerWarpExecutionException.class);
    }

    private void propagateSkip() {
        throw new ServerWarpExecutionException("execution was skipped");
    }
}
