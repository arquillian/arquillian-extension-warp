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

import org.jboss.arquillian.warp.client.execution.RequestExecutor;
import org.jboss.arquillian.warp.client.filter.RequestFilter;

/**
 * Utility class for invoking client action followed by server request, enriched with assertion.
 *
 * @author Lukas Fryc
 */
public final class Warp {
    
    static final ThreadLocal<RequestExecutor> executor = new ThreadLocal<RequestExecutor>();

    /**
     * Takes client action which should be fired in order to cause server request.
     *
     * @param action the client action to execute
     * @return {@link RequestExecution} instance
     */
    public static RequestExecutor execute(ClientAction action) {
        return executor.get().execute(action);
    }

    public static RequestExecutor filter(RequestFilter filter) {
        return executor.get().filter(filter);
    }
}