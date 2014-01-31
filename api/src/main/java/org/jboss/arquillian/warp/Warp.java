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
package org.jboss.arquillian.warp;

import org.jboss.arquillian.warp.client.execution.WarpActivityBuilder;
import org.jboss.arquillian.warp.client.execution.WarpExecutionBuilder;
import org.jboss.arquillian.warp.client.execution.WarpRuntime;

/**
 * Utility class for performing client activity followed by inspected server request.
 *
 * @author Lukas Fryc
 */
public final class Warp {

    /**
     * Takes client activity which should be performed in order to cause server request.
     *
     * @param activity the client activity to execute
     * @return {@link WarpActivityBuilder} instance
     */
    public static WarpExecutionBuilder initiate(Activity activity) {

        WarpRuntime runtime = WarpRuntime.getInstance();

        if (runtime == null) {
            throw new IllegalStateException(
                    "The Warp runtime isn't initialized. You need to make sure arquillian-warp-impl is on classpath and annotate a test class with @WarpTest in order to initialize Warp.");
        }

        return runtime.getWarpActivityBuilder().initiate(activity);
    }
}
